package me.shawlaf.varlight.persistence;

import me.shawlaf.varlight.util.pos.ChunkPosition;
import me.shawlaf.varlight.util.pos.IntPosition;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public interface IChunkCustomLightAccess {

    int getCustomLuminance(IntPosition position);

    void setCustomLuminance(IntPosition position, int value);

    ChunkPosition getChunkPosition();

    boolean hasData();

    void clear();

}
