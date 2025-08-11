package me.shawlaf.varlight.persistence.nls.common.exception;

import me.shawlaf.varlight.util.pos.ChunkPosition;
import me.shawlaf.varlight.util.pos.IntPosition;

public class PositionOutOfBoundsException extends RuntimeException {

    public PositionOutOfBoundsException(IntPosition position) {
        super(String.format("Position %s out of Range for Chunk [%d, %d]", position.toShortString(), position.getChunkX(), position.getChunkZ()));
    }

    public PositionOutOfBoundsException(String message) {
        super(message);
    }

    public static PositionOutOfBoundsException belowWorld(IntPosition position, int minSectionY) {
        return new PositionOutOfBoundsException("Position %s (Chunk Section Y: %d) is out of bounds (Minimum Section Y = %d)".formatted(position.toShortString(), position.getChunkY(), minSectionY));
    }

    public static PositionOutOfBoundsException notInChunk(IntPosition position, ChunkPosition chunk) {
        return new PositionOutOfBoundsException("Position %s (Actual Chunk: %s) is out of bounds of chunk %s".formatted(position.toShortString(), position.toChunkCoords().toShortString(), chunk.toShortString()));
    }
}
