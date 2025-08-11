package me.shawlaf.varlight.util.pos;

import me.shawlaf.varlight.util.MathUtil;
import me.shawlaf.varlight.util.Preconditions;

public record ChunkSectionPosition(int x, int y, int z) {

    public static final ChunkSectionPosition ORIGIN = new ChunkSectionPosition(0, 0, 0);

    public ChunkSectionPosition {
        Preconditions.assertInRange("y", y, 0, 15);
    }

    public ChunkSectionPosition(ChunkPosition coords, int y) {
        this(coords.x(), y, coords.z());
    }

    public ChunkSectionPosition toAbsolute(int regionX, int regionZ) {
        return new ChunkSectionPosition(regionX * 32 + x, y, regionZ * 32 + z);
    }

    public ChunkSectionPosition toRelative() {
        return new ChunkSectionPosition(getRegionRelativeX(), y, getRegionRelativeZ());
    }

    public int getRegionRelativeX() {
        return MathUtil.modulo(x, 32);
    }

    public int getRegionRelativeZ() {
        return MathUtil.modulo(z, 32);
    }

    public int encodeRegionRelative() {
        return (y << 10) | (getRegionRelativeZ() << 5) | getRegionRelativeX();
    }
}
