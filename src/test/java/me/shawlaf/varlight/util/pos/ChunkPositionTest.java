package me.shawlaf.varlight.util.pos;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ChunkPositionTest {

    @Test
    public void testChunkCoords() {
        doTest(new ChunkPosition(1, 1), 0, 0, 1, 1);
        doTest(new ChunkPosition(1, -1), 0, -1, 1, 31);
        doTest(new ChunkPosition(-1, -1), -1, -1, 31, 31);
        doTest(new ChunkPosition(-1, 1), -1, 0, 31, 1);
    }

    private void doTest(
            ChunkPosition coords,
            int expectedRegionX,
            int expectedRegionZ,
            int expectedRegionRelativeX,
            int expectedRegionRelativeZ
    ) {
        assertEquals(expectedRegionX, coords.getRegionX());
        assertEquals(expectedRegionZ, coords.getRegionZ());

        assertEquals(expectedRegionRelativeX, coords.getRegionRelativeX());
        assertEquals(expectedRegionRelativeZ, coords.getRegionRelativeZ());

        assertThrows(IllegalArgumentException.class, () -> coords.getRelative(-1, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> coords.getRelative(16, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> coords.getRelative(0, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> coords.getRelative(0, 256, 0));
        assertThrows(IllegalArgumentException.class, () -> coords.getRelative(0, 0, -1));
        assertThrows(IllegalArgumentException.class, () -> coords.getRelative(0, 0, 16));

        IntPosition relative = coords.getRelative(1, 1, 1);

        assertEquals(coords.x() * 16 + 1, relative.x());
        assertEquals(1, relative.y());
        assertEquals(coords.z() * 16 + 1, relative.z());
    }

}
