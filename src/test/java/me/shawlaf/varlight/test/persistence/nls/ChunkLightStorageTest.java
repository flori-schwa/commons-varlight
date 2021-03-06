package me.shawlaf.varlight.test.persistence.nls;

import me.shawlaf.varlight.persistence.nls.implementations.v1.ChunkLightStorage_V1;
import me.shawlaf.varlight.persistence.nls.common.exception.PositionOutOfBoundsException;
import me.shawlaf.varlight.util.pos.IntPosition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ChunkLightStorageTest {

    @Test
    public void testGetSetLightStorage() {
        ChunkLightStorage_V1 cls = new ChunkLightStorage_V1(0, 0);

        IntPosition position = new IntPosition(0, 0, 0);

        assertEquals(0, cls.getCustomLuminance(position));

        cls.setCustomLuminance(position, 5);

        assertEquals(5, cls.getCustomLuminance(position));

        cls.setCustomLuminance(position, 0);

        assertEquals(0, cls.getCustomLuminance(position));
    }

    @Test
    public void testOutOfBounds() {
        ChunkLightStorage_V1 cls = new ChunkLightStorage_V1(0, 0);

        assertThrows(PositionOutOfBoundsException.class, () -> cls.getCustomLuminance(new IntPosition(16, 0, 0))); // x out of bounds
        assertThrows(PositionOutOfBoundsException.class, () -> cls.getCustomLuminance(new IntPosition(0, 0, 16))); // z out of bounds
        assertThrows(PositionOutOfBoundsException.class, () -> cls.getCustomLuminance(new IntPosition(0, -1, 0))); // y too low
        assertThrows(PositionOutOfBoundsException.class, () -> cls.getCustomLuminance(new IntPosition(0, 256, 0))); // y too high

        assertThrows(PositionOutOfBoundsException.class, () -> cls.setCustomLuminance(new IntPosition(16, 0, 0), 5)); // x out of bounds
        assertThrows(PositionOutOfBoundsException.class, () -> cls.setCustomLuminance(new IntPosition(0, 0, 16), 5)); // z out of bounds
        assertThrows(PositionOutOfBoundsException.class, () -> cls.setCustomLuminance(new IntPosition(0, -1, 0), 5)); // y too low
        assertThrows(PositionOutOfBoundsException.class, () -> cls.setCustomLuminance(new IntPosition(0, 256, 0), 5)); // y too high
    }

}
