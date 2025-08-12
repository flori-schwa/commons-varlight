package me.shawlaf.varlight.util.pos;


import me.shawlaf.varlight.adapter.IWorld;

public record RegionCoords(int x, int z) {

    public ChunkPosition getRegionStartChunk() {
        return new ChunkPosition(x * 32, z * 32);
    }

    public ChunkPosition getRegionEndChunk() {
        return new ChunkPosition((x * 32) + 31, (z * 32) + 31);
    }

    public IntPosition getRegionStart(IWorld world) {
        return getRegionStartChunk().getChunkStart(world);
    }

    public IntPosition getRegionEnd(IWorld world) {
        return getRegionEndChunk().getChunkEnd(world);
    }

}
