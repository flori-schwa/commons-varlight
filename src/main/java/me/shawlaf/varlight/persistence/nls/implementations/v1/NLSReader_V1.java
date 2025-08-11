package me.shawlaf.varlight.persistence.nls.implementations.v1;

import me.shawlaf.varlight.persistence.nls.common.NLSHeader;
import me.shawlaf.varlight.persistence.nls.common.io.NLSCommonInputStream;
import org.jetbrains.annotations.Nullable;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class NLSReader_V1 implements AutoCloseable {

    private final NLSCommonInputStream _in;

    private final int regionX, regionZ;

    /**
     * Constructs a new NLSReader for Version 1, parses and verifies the Header from the InputStream
     *
     * @param in The Stream to read from
     * @throws IOException if an {@link IOException} occurs
     */
    public NLSReader_V1(InputStream in) throws IOException {
        this(NLSHeader.readFromStream(in), in);
    }

    private NLSReader_V1(NLSHeader header, InputStream in) throws IOException {
        header.validRequired();

        if (header.getVersion() != 1) {
            throw new IllegalArgumentException("Expected NLS Version 1, got " + header.getVersion());
        }

        this._in = new NLSCommonInputStream(in);

        this.regionX = this._in.readInt();
        this.regionZ = this._in.readInt();
    }

    public int getRegionX() {
        return regionX;
    }

    public int getRegionZ() {
        return regionZ;
    }

    @Override
    public void close() throws IOException {
        _in.close();
    }

    public @Nullable ChunkLightStorage_V1 readChunk() throws IOException {
        final int encodedPosition;

        try {
            encodedPosition = _in.readShort();
        } catch (EOFException e) {
            return null;
        }

        int chunkX = (encodedPosition & 0x1F);
        int chunkZ = (encodedPosition >>> 5) & 0x1F;

        ChunkLightStorage_V1 cls = new ChunkLightStorage_V1((32 * regionX) + chunkX, (32 * regionZ) + chunkZ);

        int mask = _in.readShort();

        for (int y = 0; y < 16; ++y) {
            if ((mask & (1 << y)) == 0) {
                continue;
            }

            cls.getLightData()[y] = _in.readChunkSectionNibbleArray();
        }

        return cls;
    }

}
