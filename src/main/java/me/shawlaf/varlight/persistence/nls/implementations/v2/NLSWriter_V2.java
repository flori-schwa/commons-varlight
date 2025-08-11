package me.shawlaf.varlight.persistence.nls.implementations.v2;

import me.shawlaf.varlight.persistence.nls.common.ChunkSectionNibbleArray;
import me.shawlaf.varlight.persistence.nls.common.io.NLSCommonOutputStream;
import me.shawlaf.varlight.util.pos.ChunkPosition;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map.Entry;
import java.util.SortedMap;

/**
 *
 * <pre>
 * V2 File Format:
 *
 * [int32] MAGIC_VALUE 0x4E 0x41 0x4C 0x53
 * [int32] VERSION
 * [RegionData]
 *
 * RegionData:
 * [int32] REGION X
 * [int32] REGION Z
 * [Chunk[]] chunks
 * EOF
 *
 * Chunk:
 * [int16] POS IN REGION (ZZZZZ_XXXXX)
 * [int16] Amount of Sections
 * [ChunkSection[]] sections
 *
 * ChunkSection:
 * [int8] Section Y Position
 * [NibbleArray(4096)[]] LIGHT DATA (2048 Bytes)
 * </pre>
 */
public class NLSWriter_V2 implements AutoCloseable {

    private final NLSCommonOutputStream _out;

    public NLSWriter_V2(OutputStream out) {
        _out = new NLSCommonOutputStream(out);
    }

    @Override
    public void close() throws IOException {
        _out.close();
    }

    public void writeHeader(int regionX, int regionZ) throws IOException {
        _out.writeNLSMagic();

        // Version
        _out.writeInt(2); // Not using CURRENT_VERSION Constant because this class is explicitly for version 2

        _out.writeInt(regionX);
        _out.writeInt(regionZ);
    }

    public void writeChunk(ChunkLightStorage_V2 cls) throws IOException {
        _out.writeShort(encodePosition(cls.getChunkPosition()));
        final SortedMap<Integer, ChunkSectionNibbleArray> lightData = cls.getLightData();
        _out.writeShort(lightData.size());

        for (Entry<Integer, ChunkSectionNibbleArray> entry : lightData.entrySet()) {
            _out.writeByte(entry.getKey());
            _out.writeNibbleArray(entry.getValue());
        }
    }

    private int encodePosition(ChunkPosition chunkPosition) {
        int encoded = 0;

        encoded |= chunkPosition.getRegionRelativeX();
        encoded |= (chunkPosition.getRegionRelativeZ() << 5);

        return encoded;
    }
}
