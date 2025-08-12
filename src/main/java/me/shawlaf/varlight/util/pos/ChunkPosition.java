package me.shawlaf.varlight.util.pos;

import me.shawlaf.varlight.adapter.IWorld;
import me.shawlaf.varlight.util.*;

public record ChunkPosition(int x, int z) {

    public static final ChunkPosition ORIGIN = new ChunkPosition(0, 0);

    public int getRegionX() {
        return x >> 5;
    }

    public int getRegionRelativeX() {
        return MathUtil.modulo(x, 32);
    }

    public int getRegionZ() {
        return z >> 5;
    }

    public int getRegionRelativeZ() {
        return MathUtil.modulo(z, 32);
    }

    public IntPosition getChunkStart(IWorld world) {
        return getRelative(0, world.getMinHeight(), 0);
    }

    public IntPosition getChunkEnd(IWorld world) {
        return getRelative(15, world.getMaxHeight() - 1, 15);
    }

    public IntPosition getRelative(int dx, int y, int dz) {
        Preconditions.assertInRange("dx", dx, 0, 15);
        Preconditions.assertInRange("dz", dz, 0, 15);

        final int x = (x() * 16) + dx;
        final int z = (z() * 16) + dz;

        return new IntPosition(x, y, z);
    }

    public ChunkPosition getRelativeChunk(int dx, int dz) {
        return new ChunkPosition(x + dx, z + dz);
    }

    public String toShortString() {
        return String.format("[%d, %d]", x, z);
    }

    public RegionCoords toRegionCoords() {
        return new RegionCoords(getRegionX(), getRegionZ());
    }

    public ChunkSectionPosition toChunkSectionPosition(int y) {
        return new ChunkSectionPosition(this, y);
    }
}
