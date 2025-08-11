package me.shawlaf.varlight.util.pos;


public record RegionCoords(int x, int z) {

    public ChunkCoords getRegionStartChunk() {
        return new ChunkCoords(x * 32, z * 32);
    }

    public ChunkCoords getRegionEndChunk() {
        return new ChunkCoords((x * 32) + 31, (z * 32) + 31);
    }

    public IntPosition getRegionStart() {
        return getRegionStartChunk().getChunkStart();
    }

    public IntPosition getRegionEnd() {
        return getRegionEndChunk().getChunkEnd();
    }

}
