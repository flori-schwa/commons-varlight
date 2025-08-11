package me.shawlaf.varlight.persistence.nls.implementations.v2;

import me.shawlaf.varlight.persistence.IChunkCustomLightAccess;
import me.shawlaf.varlight.persistence.nls.common.ChunkSectionNibbleArray;
import me.shawlaf.varlight.persistence.nls.common.exception.PositionOutOfBoundsException;
import me.shawlaf.varlight.util.pos.ChunkPosition;
import me.shawlaf.varlight.util.pos.IntPosition;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

public class ChunkLightStorage_V2 implements IChunkCustomLightAccess {

    private final @NotNull ChunkPosition _chunkPosition;

    private SortedMap<Integer, ChunkSectionNibbleArray> _lightData = new TreeMap<>();

    public ChunkLightStorage_V2(ChunkPosition chunkPosition) {
        _chunkPosition = Objects.requireNonNull(chunkPosition);
    }

    private void verifyInBounds(IntPosition position) {
        if (!position.isContainedInChunk(_chunkPosition)) {
            throw PositionOutOfBoundsException.notInChunk(position, _chunkPosition);
        }
    }

    @Override
    public int getCustomLuminance(IntPosition position) {
        verifyInBounds(position);
        ChunkSectionNibbleArray nibbleArray = _lightData.get(position.getChunkY());

        if (nibbleArray == null) {
            return 0;
        } else {
            return nibbleArray.get(indexOf(position));
        }
    }

    @Override
    public void setCustomLuminance(IntPosition position, int value) {
        verifyInBounds(position);
        final int chunkY = position.getChunkY();
        ChunkSectionNibbleArray nibbleArray = _lightData.get(chunkY);

        if (nibbleArray == null) {
            if (value == 0) {
                return;
            }

            nibbleArray = new ChunkSectionNibbleArray();
            _lightData.put(chunkY, nibbleArray);
        }

        nibbleArray.set(indexOf(position), value);
        if (value == 0 && nibbleArray.isEmpty()) {
            _lightData.remove(chunkY);
        }
    }

    @Override
    public @NotNull ChunkPosition getChunkPosition() {
        return _chunkPosition;
    }

    @Override
    public boolean hasData() {
        return !_lightData.isEmpty();
    }

    @Override
    public void clear() {
        _lightData = new TreeMap<>();
    }

    public SortedMap<Integer, ChunkSectionNibbleArray> getLightData() {
        return _lightData;
    }

    private int indexOf(IntPosition position) {
        return indexOf(position.getChunkRelativeX(), position.getChunkSectionRelativeY(), position.getChunkRelativeZ());
    }

    private int indexOf(int x, int y, int z) {
        return (y << 8) | (z << 4) | x;
    }
}
