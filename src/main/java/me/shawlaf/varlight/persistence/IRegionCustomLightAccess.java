package me.shawlaf.varlight.persistence;

import me.shawlaf.varlight.util.pos.ChunkPosition;
import me.shawlaf.varlight.util.pos.IntPosition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IRegionCustomLightAccess {

    int getCustomLuminance(IntPosition position);

    int setCustomLuminance(IntPosition position, int value);

    int getNonEmptyChunks();

    boolean hasChunkData(ChunkPosition chunkPosition);

    void clearChunk(ChunkPosition chunkPosition);

    @NotNull List<ChunkPosition> getAffectedChunks();

}
