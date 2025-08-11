package me.shawlaf.varlight.persistence.nls.implementations.v2;

import me.shawlaf.varlight.persistence.nls.common.ChunkSectionNibbleArray;
import me.shawlaf.varlight.persistence.nls.common.migrate.INLSMigrator;
import me.shawlaf.varlight.persistence.nls.implementations.v1.ChunkLightStorage_V1;
import me.shawlaf.varlight.persistence.nls.implementations.v1.NLSReader_V1;
import me.shawlaf.varlight.util.io.FileUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public class NLS2Migrator implements INLSMigrator {

    @Override
    public void migrateFile(File file) throws IOException {
        final File legacyFile = Files.move(file.toPath(), Path.of(file.getAbsolutePath() + ".original")).toFile();

        final boolean deflated = FileUtil.isDeflated(legacyFile);
        final int regionX;
        final int regionZ;

        final List<ChunkLightStorage_V1> legacyChunks = new ArrayList<>();

        try (InputStream in = FileUtil.openStreamInflate(legacyFile)) {
            try (NLSReader_V1 legacyReader = new NLSReader_V1(in)) {
                regionX = legacyReader.getRegionX();
                regionZ = legacyReader.getRegionZ();

                ChunkLightStorage_V1 chunk;

                while ((chunk = legacyReader.readChunk()) != null) {
                    legacyChunks.add(chunk);
                }
            }
        }

        final List<ChunkLightStorage_V2> migratedChunks = migrateChunks(legacyChunks);

        try (OutputStream out = new FileOutputStream(file)) {
            OutputStream oStream = deflated ? new GZIPOutputStream(out) : out;

            try (NLSWriter_V2 writer = new NLSWriter_V2(oStream)) {
                writer.writeHeader(regionX, regionZ);

                for (final ChunkLightStorage_V2 migratedChunk : migratedChunks) {
                    writer.writeChunk(migratedChunk);
                }
            }
        }
    }

    private List<ChunkLightStorage_V2> migrateChunks(List<ChunkLightStorage_V1> legacyChunks) {
        List<ChunkLightStorage_V2> result = new ArrayList<>(legacyChunks.size());

        for (ChunkLightStorage_V1 legacyChunk : legacyChunks) {
            final ChunkLightStorage_V2 migratedChunk = new ChunkLightStorage_V2(legacyChunk.getChunkPosition());
            result.add(migratedChunk);
            final int mask = legacyChunk.getMask();

            for (int sectionY = 0; sectionY < 16; ++sectionY) {
                if ((mask & (1 << sectionY)) == 0) {
                    continue;
                }

                migratedChunk.getLightData().put(sectionY, new ChunkSectionNibbleArray(legacyChunk.getLightData()[sectionY]));
            }
        }

        return result;
    }

}
