package me.shawlaf.varlight.persistence.nls.common.exception;

import me.shawlaf.varlight.util.pos.IntPosition;

public class PositionOutOfBoundsException extends RuntimeException {

    private final IntPosition position;

    public PositionOutOfBoundsException(IntPosition position) {
        super(String.format("Position %s out of Range for Chunk [%d, %d]", position.toShortString(), position.getChunkX(), position.getChunkZ()));
        this.position = position;
    }

    public IntPosition getPosition() {
        return position;
    }
}
