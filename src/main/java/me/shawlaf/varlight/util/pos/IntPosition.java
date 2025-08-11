package me.shawlaf.varlight.util.pos;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record IntPosition(int x, int y, int z) implements Comparable<IntPosition> {

    public static final IntPosition ORIGIN = new IntPosition(0, 0, 0);

    public int getChunkRelativeX() {
        return x & 0xF;
    }

    public int getChunkRelativeZ() {
        return z & 0xF;
    }

    public int getChunkX() {
        return x >> 4;
    }

    public int getChunkZ() {
        return z >> 4;
    }

    public int getRegionX() {
        return getChunkX() >> 5;
    }

    public int getRegionZ() {
        return getChunkZ() >> 5;
    }

    public int xDistanceTo(IntPosition other) {
        return Math.abs(other.x - this.x);
    }

    public int yDistanceTo(IntPosition other) {
        return Math.abs(other.y - this.y);
    }

    public int zDistanceTo(IntPosition other) {
        return Math.abs(other.z - this.z);
    }

    public ChunkSectionPosition getChunkSection() {
        return new ChunkSectionPosition(getChunkX(), y >> 4, getChunkZ());
    }

    public int manhattanDistance(IntPosition other) {
        Objects.requireNonNull(other);

        int total = 0;

        total += Math.abs(x - other.x);
        total += Math.abs(y - other.y);
        total += Math.abs(z - other.z);

        return total;
    }

    public IntPosition getRelative(int dx, int dy, int dz) {
        return new IntPosition(x + dx, y + dy, z + dz);
    }

    public ChunkCoords toChunkCoords() {
        return new ChunkCoords(getChunkX(), getChunkZ());
    }

    public RegionCoords toRegionCoords() {
        return new RegionCoords(getRegionX(), getRegionZ());
    }

    public <R> R convert(ConversionFunction<R> func) {
        return func.convert(this);
    }

    public String toShortString() {
        return String.format("[%d, %d, %d]", x, y, z);
    }

    @Override
    public int compareTo(@NotNull IntPosition o) {
        return Integer.compare(this.manhattanDistance(ORIGIN), o.manhattanDistance(ORIGIN));
    }

    @FunctionalInterface
    public interface ConversionFunction<R> {
        R convert(int x, int y, int z);

        default R convert(IntPosition from) {
            return convert(from.x, from.y, from.z);
        }
    }
}
