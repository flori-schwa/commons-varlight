package me.shawlaf.varlight.persistence;

import me.shawlaf.varlight.util.pos.ChunkPosition;
import me.shawlaf.varlight.util.pos.IntPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public interface IRegionCustomLightAccess {

    int getCustomLuminance(IntPosition position);

    int setCustomLuminance(IntPosition position, int value);

    int getNonEmptyChunks();

    default boolean hasChunkData(ChunkPosition chunkPosition) {
        return Optional.ofNullable(getChunk(chunkPosition)).map(IChunkCustomLightAccess::hasData).orElse(false);
    }

    default void clearChunk(ChunkPosition chunkPosition) {
        Optional.ofNullable(getChunk(chunkPosition)).ifPresent(IChunkCustomLightAccess::clear);
    }

    @Deprecated
    int getMask(ChunkPosition chunkPosition);

    @Nullable IChunkCustomLightAccess getChunk(ChunkPosition chunkPosition);

    @NotNull List<ChunkPosition> getAffectedChunks();

    @NotNull Iterator<IntPosition> iterateAllLightSources();

}
