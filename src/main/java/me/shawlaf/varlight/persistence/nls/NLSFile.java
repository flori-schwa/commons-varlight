package me.shawlaf.varlight.persistence.nls;

import me.shawlaf.varlight.persistence.IChunkCustomLightAccess;
import me.shawlaf.varlight.persistence.IRegionCustomLightAccess;
import me.shawlaf.varlight.persistence.nls.common.NLSConstants;
import me.shawlaf.varlight.persistence.nls.common.NLSHeader;
import me.shawlaf.varlight.persistence.nls.common.migrate.NLSMigration;
import me.shawlaf.varlight.persistence.nls.implementations.v2.ChunkLightStorage_V2;
import me.shawlaf.varlight.persistence.nls.implementations.v2.NLSReader_V2;
import me.shawlaf.varlight.persistence.nls.implementations.v2.NLSWriter_V2;
import me.shawlaf.varlight.util.io.FileUtil;
import me.shawlaf.varlight.util.pos.ChunkPosition;
import me.shawlaf.varlight.util.pos.IntPosition;
import me.shawlaf.varlight.util.pos.RegionCoords;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public final class NLSFile implements IRegionCustomLightAccess {

    public static String FILE_NAME_FORMAT = "r.%d.%d.nls";

    public final File _file;
    private final boolean _deflate;

    private final int _regionX, _regionZ;
    private final ChunkLightStorage_V2[] _chunks = new ChunkLightStorage_V2[32 * 32];

    private boolean _dirty;

    private NLSFile(@NotNull File file, int regionX, int regionZ, boolean deflate) {
        if (file.exists()) {
            throw new IllegalArgumentException("File already exists!");
        }

        _file = file;
        _deflate = deflate;

        _regionX = regionX;
        _regionZ = regionZ;
    }

    private NLSFile(@NotNull File file, boolean deflate) throws IOException {
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist");
        }

        _file = file;
        _deflate = deflate;

        boolean needsMigration = false;

        try (InputStream iStream = FileUtil.openStreamInflate(file)) {
            NLSHeader header = NLSHeader.readFromStream(iStream);

            if (header.getVersion() < NLSConstants.CURRENT_VERSION_NUM) {
                needsMigration = true;
            } else if (header.getVersion() > NLSConstants.CURRENT_VERSION_NUM) {
                throw new IllegalStateException(String.format("Cannot downgrade from future NLS Version %d, desired version: %d", header.getVersion(), NLSConstants.CURRENT_VERSION_NUM));
            }
        }

        if (needsMigration) {
            NLSMigration.migrateFileToVersion(NLSConstants.CURRENT_VERSION_NUM, file);
        }

        try (InputStream iStream = FileUtil.openStreamInflate(file)) {
            try (NLSReader_V2 reader = new NLSReader_V2(iStream)) {
                // Header already parsed and verified by constructor

                _regionX = reader.getRegionX();
                _regionZ = reader.getRegionZ();

                ChunkLightStorage_V2 chunk;

                while ((chunk = reader.readChunk()) != null) {
                    final int index = chunkIndex(chunk.getChunkPosition());

                    if (_chunks[index] != null) {
                        throw new IllegalStateException(String.format("Duplicate Chunk Information for Chunk %s found in File %s", chunk.getChunkPosition().toShortString(), file.getAbsolutePath()));
                    }

                    if (chunk.hasData()) {
                        _chunks[index] = chunk;
                    }
                }
            }
        }
    }

    public static File getFile(File parent, RegionCoords regionCoords) {
        return getFile(parent, regionCoords.x(), regionCoords.z());
    }

    public static File getFile(File parent, int regionX, int regionZ) {
        return new File(parent, String.format(FILE_NAME_FORMAT, regionX, regionZ));
    }

    public static NLSFile newFile(@NotNull File file, int regionX, int regionZ) {
        return new NLSFile(file, regionX, regionZ, true);
    }

    public static NLSFile newFile(@NotNull File file, int regionX, int regionZ, boolean deflate) {
        return new NLSFile(file, regionX, regionZ, deflate);
    }

    public static NLSFile existingFile(@NotNull File file) throws IOException {
        return existingFile(file, true);
    }

    public static NLSFile existingFile(@NotNull File file, boolean deflate) throws IOException {
        return new NLSFile(file, deflate);
    }

    public RegionCoords getRegionCoords() {
        return new RegionCoords(_regionX, _regionZ);
    }

    public int getRegionX() {
        return _regionX;
    }

    public int getRegionZ() {
        return _regionZ;
    }

    @Override
    public int getCustomLuminance(IntPosition position) {
        synchronized (this) {
            IChunkCustomLightAccess chunk = _chunks[chunkIndex(position.toChunkCoords())];

            if (chunk == null) {
                return 0;
            }

            return chunk.getCustomLuminance(position);
        }
    }

    @Override
    public int setCustomLuminance(IntPosition position, int value) {
        ChunkPosition chunkPosition = position.toChunkCoords();
        int index = chunkIndex(chunkPosition);

        synchronized (this) {
            ChunkLightStorage_V2 chunk = _chunks[index];

            if (chunk == null) {
                // No Data present

                if (value == 0) {
                    return 0;
                }

                chunk = new ChunkLightStorage_V2(chunkPosition);
                chunk.setCustomLuminance(position, value);

                // The value set is not 0 -> The chunk is not empty, if the value is illegal, an exception will be thrown

                _chunks[index] = chunk;
                _dirty = true;
                return 0;
            } else {
                final int ret = chunk.getCustomLuminance(position);

                if (ret == value) {
                    return ret;
                }

                chunk.setCustomLuminance(position, value);

                if (value == 0 && !chunk.hasData()) { // If the last Light source was removed
                    _chunks[index] = null;
                }
                _dirty = true;
                return ret;
            }
        }
    }

    @Override
    public int getNonEmptyChunks() {
        int count = 0;

        synchronized (this) {
            for (IChunkCustomLightAccess chunk : _chunks) {
                if (chunk == null) {
                    continue;
                }

                if (chunk.hasData()) {
                    ++count;
                }
            }
        }

        return count;
    }

    @Override
    public boolean hasChunkData(ChunkPosition chunkPosition) {
        final int chunkIndex = chunkIndex(chunkPosition);

        synchronized (this) {
            final IChunkCustomLightAccess chunkData = _chunks[chunkIndex];

            if (chunkData == null) {
                return false;
            }

            return chunkData.hasData();
        }
    }


    @Override
    public void clearChunk(ChunkPosition chunkPosition) {
        int index = chunkIndex(chunkPosition);

        synchronized (this) {
            if (_chunks[index] == null || !_chunks[index].hasData()) {
                return;
            }

            _chunks[index] = null;
            _dirty = true;
        }
    }

    public boolean save() throws IOException {
        synchronized (this) {
            if (!_dirty) {
                return false;
            }

            try (FileOutputStream fos = new FileOutputStream(_file)) {
                OutputStream oStream = _deflate ? new GZIPOutputStream(fos) : fos;

                try (NLSWriter_V2 out = new NLSWriter_V2(oStream)) {
                    out.writeHeader(_regionX, _regionZ);

                    for (ChunkLightStorage_V2 cls : _chunks) {
                        if (cls == null) {
                            continue;
                        }

                        out.writeChunk(cls);
                    }
                }
            }

            _dirty = false;
        }

        return true;
    }

    @Override
    public @NotNull List<ChunkPosition> getAffectedChunks() {
        List<ChunkPosition> list = new ArrayList<>();

        synchronized (this) {
            for (IChunkCustomLightAccess chunk : _chunks) {
                if (chunk == null) {
                    continue;
                }

                list.add(chunk.getChunkPosition());
            }
        }

        return list;
    }

    private int chunkIndex(ChunkPosition chunkPosition) {
        return chunkIndex(chunkPosition.getRegionRelativeX(), chunkPosition.getRegionRelativeZ());
    }

    private int chunkIndex(int cx, int cz) {
        return cz << 5 | cx;
    }
}
