package me.shawlaf.varlight.persistence.old;

import me.shawlaf.varlight.persistence.LightPersistFailedException;
import me.shawlaf.varlight.persistence.old.vldb.VLDBFile;
import me.shawlaf.varlight.util.pos.ChunkPosition;
import me.shawlaf.varlight.util.pos.IntPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Deprecated
public abstract class RegionPersistor<L extends ICustomLightSource> {

    private static final int REGION_SIZE = 32;
    private static final int CHUNK_SIZE = 16 * 16 * 256;

    public final int regionX, regionZ;

    public final VLDBFile<L> file;

    private final Object chunkLock = new Object();

    private final L[][] chunkCache = createMultiArr(REGION_SIZE * REGION_SIZE);
    private final int[] chunkSizes = new int[REGION_SIZE * REGION_SIZE];
    private final List<ChunkPosition> dirtyChunks = new ArrayList<>(REGION_SIZE * REGION_SIZE);

    public RegionPersistor(@NotNull File vldbRoot, int regionX, int regionZ, boolean deflated) throws IOException {
        Objects.requireNonNull(vldbRoot);

        if (!vldbRoot.exists()) {
            if (!vldbRoot.mkdir()) {
                throw new LightPersistFailedException("Could not create directory \"" + vldbRoot.getAbsolutePath() + "\"");
            }
        }

        if (!vldbRoot.isDirectory()) {
            throw new IllegalArgumentException(String.format("\"%s\" is not a directory!", vldbRoot.getAbsolutePath()));
        }

        this.regionX = regionX;
        this.regionZ = regionZ;

        File vldbFile = new File(vldbRoot, String.format(VLDBFile.FILE_NAME_FORMAT, regionX, regionZ));

        if (!vldbFile.exists()) {
            this.file = new VLDBFile<L>(vldbFile, regionX, regionZ, deflated) {
                @NotNull
                @Override
                protected L[] createArray(int size) {
                    return RegionPersistor.this.createArray(size);
                }

                @NotNull
                @Override
                protected L createInstance(IntPosition position, int lightLevel, boolean migrated, String material) {
                    return RegionPersistor.this.createInstance(position, lightLevel, migrated, material);
                }
            };
        } else {
            this.file = new VLDBFile<L>(vldbFile, deflated) {
                @NotNull
                @Override
                protected L[] createArray(int size) {
                    return RegionPersistor.this.createArray(size);
                }

                @NotNull
                @Override
                protected L createInstance(IntPosition position, int lightLevel, boolean migrated, String material) {
                    return RegionPersistor.this.createInstance(position, lightLevel, migrated, material);
                }
            };
        }
    }

    /**
     * <p>Marks the Chunk containing the specified {@link IntPosition} as dirty.</p>
     *
     * @param position The Position, where changes to Light sources have occured.
     */
    public void markDirty(IntPosition position) {
        markDirty(position.toChunkCoords());
    }

    /**
     * <p>Marks the Chunk with the specified {@link ChunkPosition} as dirty.</p>
     *
     * @param chunkPosition The Position of the Chunk, where changes to Light sources have occured.
     */
    public void markDirty(ChunkPosition chunkPosition) {
        assertInRegion(chunkPosition);

        synchronized (chunkLock) {
            dirtyChunks.add(chunkPosition);
        }
    }

    /**
     * <p>Loads The Light sources of the specified Chunk from the raw data into the cache.</p>
     *
     * @param chunkPosition The Coordinates of the Chunk, whose Custom Light data should be loaded.
     * @throws IOException When an {@link IOException} occurs while reading the data.
     */
    public void loadChunk(@NotNull ChunkPosition chunkPosition) throws IOException {
        Objects.requireNonNull(chunkPosition);
        assertInRegion(chunkPosition);

        final int chunkIndex = chunkIndex(chunkPosition);

        synchronized (chunkLock) {
            L[] lightSources;

            synchronized (file) {
                lightSources = file.readChunk(chunkPosition);
            }

            L[] fullChunk = createArray(CHUNK_SIZE);

            int count = 0;

            for (L ls : lightSources) {
                int index = indexOf(ls.getPosition());

                if (fullChunk[index] != null) {
                    throw new IllegalStateException("Duplicate Lightsource at Position " + ls.getPosition().toShortString());
                }

                fullChunk[index] = ls;
                ++count;
            }

            chunkSizes[chunkIndex] = count;
            chunkCache[chunkIndex] = fullChunk;
        }
    }

    /**
     * <p>Checks if the Custom Light data for the Chunk with the specified {@link ChunkPosition} is currently loaded into the cache.</p>
     *
     * @param chunkPosition The {@link ChunkPosition} to check
     * @return true, if the Light data for the Chunk is loaded into the cache.
     */
    public boolean isChunkLoaded(@NotNull ChunkPosition chunkPosition) {
        Objects.requireNonNull(chunkPosition);
        assertInRegion(chunkPosition);

        synchronized (chunkLock) {
            return chunkCache[chunkIndex(chunkPosition)] != null;
        }
    }

    /**
     * <p>Unloads the Custom Light data for the Chunk with the specified {@link ChunkPosition} from the cache.</p>
     * <p>If the Chunk was marked as dirty in {@link RegionPersistor#markDirty(ChunkPosition)} or {@link RegionPersistor#markDirty(IntPosition)} the Chunk will be flushed.</p>
     *
     * @param chunkPosition The {@link ChunkPosition} of the Chunk, whose Light data should be unloaded from the cache.
     * @throws IOException If an {@link IOException} occurs while flushing the Chunk.
     */
    public void unloadChunk(@NotNull ChunkPosition chunkPosition) throws IOException {
        Objects.requireNonNull(chunkPosition);
        assertInRegion(chunkPosition);

        final int chunkIndex = chunkIndex(chunkPosition);

        synchronized (chunkLock) {
            L[] toUnload = chunkCache[chunkIndex];

            if (toUnload == null) { // There was no mapping for the chunk
                return;
            }

            if (dirtyChunks.contains(chunkPosition)) {
                flushChunk(chunkPosition, getNonNullFromChunk(chunkPosition));
            }

            chunkSizes[chunkIndex] = 0;
            chunkCache[chunkIndex] = null;
        }
    }

    /**
     * <p>Returns the current Cache for the Chunk with the specified {@link ChunkPosition} as a List of Light sources.</p>
     *
     * @param chunkPosition The {@link ChunkPosition} of the Chunk, whose cache is being queried.
     * @return A {@link List} of type {@code T}, the Light source data, currently in cache.
     */
    @NotNull
    public List<L> getCache(@NotNull ChunkPosition chunkPosition) {
        Objects.requireNonNull(chunkPosition);
        assertInRegion(chunkPosition);

        List<L> chunk;

        synchronized (chunkLock) {
            L[] chunkArray = chunkCache[chunkIndex(chunkPosition)];

            if (chunkArray == null) {
                chunk = new ArrayList<>();
            } else {
                chunk = new ArrayList<>(getNonNullFromChunk(chunkPosition));
            }
        }

        return Collections.unmodifiableList(chunk);
    }

    /**
     * <p>Looks up Custom Light source Data at the specified {@link IntPosition}, if the Chunk containing the Position is not currently loaded in the cache,
     * The Chunk will be loaded using {@link RegionPersistor#loadChunk(ChunkPosition)}</p>
     *
     * @param position The Position to look up.
     * @return An instance of {@code L} or {@code null} if there is now Light source at the given Position
     * @throws IOException If an {@link IOException} occurs during {@link RegionPersistor#loadChunk(ChunkPosition)}
     */
    @Nullable
    public L getLightSource(@NotNull IntPosition position) throws IOException {
        Objects.requireNonNull(position);
        assertInRegion(position);

        final ChunkPosition chunkPosition = position.toChunkCoords();
        final int chunkIndex = chunkIndex(chunkPosition);

        synchronized (chunkLock) {
            if (chunkCache[chunkIndex] == null) {
                loadChunk(chunkPosition);
            }

            return chunkCache[chunkIndex][indexOf(position)];
        }
    }

    /**
     * <p>Inserts the Lightsource at Position {@link ICustomLightSource#getPosition()} if no Light Source exists at that position yet.</p>
     * <p>Modifies the Lightsource at the Position, if a Light Source already exists at the Position and {@link ICustomLightSource#getCustomLuminance()} is {@code > 0}</p>
     * <p>Deletes the Lightsource at the Position, if {@link ICustomLightSource#getCustomLuminance()} is {@code 0}.</p>
     * <p>
     * <br />
     *
     * <p>If The Chunk containing {@link ICustomLightSource#getPosition()} is not yet loaded into the cache, {@link RegionPersistor#loadChunk(ChunkPosition)} will be called.</p>
     *
     * @param lightSource The Light source to insert
     * @throws IOException If an {@link IOException} occurs during {@link RegionPersistor#loadChunk(ChunkPosition)}
     */
    public void put(@NotNull L lightSource) throws IOException {
        Objects.requireNonNull(lightSource);
        assertInRegion(lightSource.getPosition());

        final ChunkPosition chunkPosition = lightSource.getPosition().toChunkCoords();
        final int chunkIndex = chunkIndex(chunkPosition);

        synchronized (chunkLock) {
            if (chunkCache[chunkIndex] == null) {
                loadChunk(chunkPosition);
            }

            putInternal(lightSource);
        }
    }

    @Deprecated
    public void removeLightSource(@NotNull IntPosition position) throws IOException {
        Objects.requireNonNull(position);
        assertInRegion(position);

        final ChunkPosition chunkPosition = position.toChunkCoords();
        final int chunkIndex = chunkIndex(chunkPosition);
        final int index = indexOf(position);

        synchronized (chunkLock) {
            if (chunkCache[chunkIndex] == null) {
                loadChunk(chunkPosition);
            }

            L[] chunkArray = chunkCache[chunkIndex];

            if (chunkArray[index] != null) {
                chunkArray[index] = null;
                --chunkSizes[chunkIndex];

                markDirty(chunkPosition);
            }
        }
    }

    /**
     * <p>Flushes all dirty chunks.</p>
     *
     * @throws IOException If an {@link IOException} occurs during flushing.
     */
    public void flushAll() throws IOException {
        synchronized (chunkLock) {
            synchronized (file) {
                for (ChunkPosition key : dirtyChunks.toArray(new ChunkPosition[dirtyChunks.size()])) {
                    flushChunk(key);
                }
            }
        }
    }

    /**
     * @return A {@link List} of {@link ChunkPosition} that contain Custom Light data inside the Region.
     */
    public List<ChunkPosition> getAffectedChunks() {
        synchronized (file) {
            return file.getChunksWithData();
        }
    }

    /**
     * @return A {@link List} containing all Light Sources inside this Region, all modified chunks will first be flushed.
     * @throws IOException If an {@link IOException} occurs while flushing or reading.
     */
    public List<L> loadAll() throws IOException {
        synchronized (file) {
            synchronized (chunkLock) {
                int cx, cz;

                for (int z = 0; z < REGION_SIZE; ++z) {
                    for (int x = 0; x < REGION_SIZE; ++x) {
                        int chunkIndex = chunkIndex(cx = regionX + x, cz = regionZ + z);
                        ChunkPosition chunkPosition = new ChunkPosition(cx, cz);

                        if (chunkCache[chunkIndex] != null && dirtyChunks.contains(chunkPosition)) {
                            flushChunk(chunkPosition, getNonNullFromChunk(chunkPosition));
                        }
                    }
                }

                return file.readAll();
            }
        }
    }

    /**
     * Saves the currently flushed data on the disk. Modified, but not yet flushed changes will not be saved.
     *
     * @return true, if data was written to the disk (changes have been made)
     * @throws IOException If an {@link IOException} occurs.
     */
    public boolean save() throws IOException {
        synchronized (file) {
            return file.save();
        }
    }

    private void assertInRegion(IntPosition position) {
        if (position.getRegionX() != regionX || position.getRegionZ() != regionZ) {
            throw new IllegalArgumentException(String.format("Position %s is not in region [%d, %d]", position.toShortString(), regionX, regionZ));
        }
    }

    private void assertInRegion(ChunkPosition chunkPosition) {
        if (chunkPosition.getRegionX() != regionX || chunkPosition.getRegionZ() != regionZ) {
            throw new IllegalArgumentException(String.format("Chunk %s is not in region [%d, %d]", chunkPosition.toShortString(), regionX, regionZ));
        }
    }

    private void flushChunk(ChunkPosition chunkPosition, Collection<L> lightData) throws IOException {
        final int chunkIndex = chunkIndex(chunkPosition);

        synchronized (chunkLock) {
            if (!dirtyChunks.contains(chunkPosition)) {
                return;
            }

            synchronized (file) {
                if (lightData.size() == 0) {
                    if (file.hasChunkData(chunkPosition)) {
                        file.removeChunk(chunkPosition);
                    }

                    chunkCache[chunkIndex] = null;
                    chunkSizes[chunkIndex] = 0;
                }

                file.putChunk(lightData.toArray(createArray(lightData.size())));
            }

            dirtyChunks.remove(chunkPosition);
        }
    }

    private void flushChunk(ChunkPosition chunkPosition) throws IOException {
        synchronized (chunkLock) {
            flushChunk(chunkPosition, getNonNullFromChunk(chunkPosition));
        }
    }

    private void putInternal(L lightSource) {
        Objects.requireNonNull(lightSource);

        final ChunkPosition chunkPosition = lightSource.getPosition().toChunkCoords();
        final int chunkIndex = chunkIndex(chunkPosition);
        final int index = indexOf(lightSource.getPosition());

        synchronized (chunkLock) {
            L[] chunkArray = chunkCache[chunkIndex];

            if (chunkArray == null) {
                throw new IllegalArgumentException("No Data present for chunk");
            }

            L removed = chunkArray[index];
            chunkArray[index] = null;

            if (lightSource.getCustomLuminance() > 0) { // New or modified
                chunkArray[index] = lightSource;

                if (removed == null) {
                    ++chunkSizes[chunkIndex]; // One new light source added
                }

                // When a light source was modified, aka removed != null, then the amount of Light sources stays the same

                markDirty(chunkPosition);
            } else { // Removed, or no-op
                if (removed != null) {
                    markDirty(chunkPosition);
                    --chunkSizes[chunkIndex]; // One Light source was removed
                }
            }
        }
    }

    private Collection<L> getNonNullFromChunk(ChunkPosition chunkPosition) {
        final int chunkIndex = chunkIndex(chunkPosition);

        synchronized (chunkLock) {
            int chunkSize = chunkSizes[chunkIndex];

            List<L> list = new ArrayList<>(chunkSize);
            L[] rawArr = chunkCache[chunkIndex];

            if (rawArr == null || rawArr.length == 0) {
                return list; // Will have size 0
            }

            int added = 0;

            for (L l : rawArr) {
                if (l == null) {
                    continue;
                }

                list.add(l);

                if (++added == chunkSize) {
                    break;
                }
            }

            return list;
        }
    }

    private int indexOf(IntPosition position) {
        return indexOf(position.getChunkRelativeX(), position.y(), position.getChunkRelativeZ());
    }

    private int indexOf(int x, int y, int z) {
        return (y << 8) | (z << 4) | x;
    }

    private int chunkIndex(ChunkPosition chunkPosition) {
        return chunkIndex(chunkPosition.getRegionRelativeX(), chunkPosition.getRegionRelativeZ());
    }

    private int chunkIndex(int cx, int cz) {
        return cz << 5 | cx;
    }

    @NotNull
    protected abstract L[] createArray(int size);

    @NotNull
    protected abstract L[][] createMultiArr(int size);

    @NotNull
    protected abstract L createInstance(IntPosition position, int lightLevel, boolean migrated, String material);

    /**
     * Unloads this Region, discarding any not-flushed changes.
     */
    public void unload() {
        Arrays.fill(chunkCache, null);
        Arrays.fill(chunkSizes, 0);

        dirtyChunks.clear();

        file.unload();
    }
}