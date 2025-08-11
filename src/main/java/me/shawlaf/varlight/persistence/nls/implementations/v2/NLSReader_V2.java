package me.shawlaf.varlight.persistence.nls.implementations.v2;

import me.shawlaf.varlight.persistence.nls.common.ChunkSectionNibbleArray;
import me.shawlaf.varlight.persistence.nls.common.NLSHeader;
import me.shawlaf.varlight.persistence.nls.common.io.NLSCommonInputStream;
import me.shawlaf.varlight.util.pos.ChunkPosition;
import org.jetbrains.annotations.Nullable;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class NLSReader_V2 implements AutoCloseable {

    private final NLSCommonInputStream _in;

    private final int _regionX;

    private final int _regionZ;

    public NLSReader_V2(InputStream in) throws IOException {
        this(NLSHeader.readFromStream(in), in);
    }

    private NLSReader_V2(NLSHeader header, InputStream in) throws IOException {
        header.validRequired();

        if (header.getVersion() != 2) {
            throw new IllegalArgumentException("Expected NLS Version 2, got " + header.getVersion());
        }

        _in = new NLSCommonInputStream(in);

        _regionX = _in.readInt();
        _regionZ = _in.readInt();
    }

    @Override
    public void close() throws IOException {
        _in.close();
    }

    public int getRegionX() {
        return _regionX;
    }

    public int getRegionZ() {
        return _regionZ;
    }

    public @Nullable ChunkLightStorage_V2 readChunk() throws IOException {
        final int encodedPosition;

        try {
            encodedPosition = _in.readShort();
        } catch (EOFException e) {
            return null;
        }

        final int offsetX = (encodedPosition & 0x1F);
        final int offsetZ = (encodedPosition >>> 5) & 0x1F;

        final ChunkLightStorage_V2 cls = new ChunkLightStorage_V2(new ChunkPosition((32 * _regionX) + offsetX, (32 * _regionZ) + offsetZ));

        final int chunkCount = _in.readUnsignedShort();

        for (int i = 0; i < chunkCount; i++) {
            final int sectionY = _in.readByte();
            final ChunkSectionNibbleArray lightData = _in.readChunkSectionNibbleArray();

            cls.getLightData().put(sectionY, lightData);
        }

        return cls;
    }
}
