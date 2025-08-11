package me.shawlaf.varlight.util.pos;

import me.shawlaf.varlight.util.*;

import java.util.Objects;

public record ChunkCoords(int x, int z) {

    public static final ChunkCoords ORIGIN = new ChunkCoords(0, 0);

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

    public int getCornerAX() {
        return x << 4;
    }

    public int getCornerAY() {
        return 0;
    }

    public int getCornerAZ() {
        return z << 4;
    }

    public int getCornerBX() {
        return getCornerAX() + 15;
    }

    public int getCornerBY() {
        return 255;
    }

    public int getCornerBZ() {
        return getCornerAZ() + 15;
    }

    public IntPosition getChunkStart() {
        return getRelative(0, 0, 0);
    }

    public IntPosition getChunkEnd() {
        return getRelative(15, 255, 15);
    }

    public IntPosition getRelative(int dx, int dy, int dz) {
        Preconditions.assertInRange("dx", dx, 0, 15);
        Preconditions.assertInRange("dy", dy, 0, 255);
        Preconditions.assertInRange("dz", dz, 0, 15);

        return new IntPosition(this.x * 16 + dx, dy, this.z * 16 + dz);
    }

    public ChunkCoords getRelativeChunk(int dx, int dz) {
        return new ChunkCoords(x + dx, z + dz);
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
