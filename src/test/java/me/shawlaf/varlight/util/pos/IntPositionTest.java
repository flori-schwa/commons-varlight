package me.shawlaf.varlight.util.pos;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class IntPositionTest {

    static Stream<Arguments> regionPositions() {
        return Stream.of(
                arguments(0, 0),
                arguments(511, 0),
                arguments(512, 1),
                arguments(-1, -1),
                arguments(-512, -1),
                arguments(-513, -2)
        );
    }

    @MethodSource("regionPositions")
    @ParameterizedTest
    void testRegionX(int xCoordinate, int expectedRegionX) {
        assertThat(new IntPosition(xCoordinate, 0, 0).getRegionX()).isEqualTo(expectedRegionX);
    }

    @MethodSource("regionPositions")
    @ParameterizedTest
    void testRegionZ(int zCoordinate, int expectedRegionZ) {
        assertThat(new IntPosition(0, 0, zCoordinate).getRegionZ()).isEqualTo(expectedRegionZ);
    }

    static Stream<Arguments> chunkPositions() {
        return Stream.of(
                arguments(0, 0),
                arguments(15, 0),
                arguments(16, 1),
                arguments(-1, -1),
                arguments(-16, -1),
                arguments(-17, -2)
        );
    }

    @MethodSource("chunkPositions")
    @ParameterizedTest
    void testChunkX(int xCoordinate, int expectedChunkX) {
        assertThat(new IntPosition(xCoordinate, 0, 0).getChunkX()).isEqualTo(expectedChunkX);
    }

    @MethodSource("chunkPositions")
    @ParameterizedTest
    void testChunkY(int yCoordinate, int expectedChunkY) {
        assertThat(new IntPosition(0, yCoordinate, 0).getChunkY()).isEqualTo(expectedChunkY);
    }

    @MethodSource("chunkPositions")
    @ParameterizedTest
    void testChunkZ(int zCoordinate, int expectedChunkZ) {
        assertThat(new IntPosition(0, 0, zCoordinate).getChunkZ()).isEqualTo(expectedChunkZ);
    }

    static Stream<Arguments> chunkRelatives() {
        return Stream.of(
                arguments(0, 0),
                arguments(15, 15),
                arguments(16, 0),
                arguments(-1, 15),
                arguments(-16, 0),
                arguments(-17, 15)
        );
    }

    @MethodSource("chunkRelatives")
    @ParameterizedTest
    void testChunkRelativeX(int xCoordinate, int expectedRelativeX) {
        assertThat(new IntPosition(xCoordinate, 0, 0).getChunkRelativeX()).isEqualTo(expectedRelativeX);
    }

    @MethodSource("chunkRelatives")
    @ParameterizedTest
    void testChunkSectionRelativeY(int yCoordinate, int expectedRelativeY) {
        assertThat(new IntPosition(0, yCoordinate, 0).getChunkSectionRelativeY()).isEqualTo(expectedRelativeY);
    }

    @MethodSource("chunkRelatives")
    @ParameterizedTest
    void testChunkRelativeZ(int zCoordinate, int expectedRelativeZ) {
        assertThat(new IntPosition(0, 0, zCoordinate).getChunkRelativeZ()).isEqualTo(expectedRelativeZ);
    }

    static Stream<Arguments> testManhattanDistance() {
        return Stream.of(
                arguments(new IntPosition(0, 0, 0), new IntPosition(1, 1, 1), 3),
                arguments(new IntPosition(-1, -1, -1), new IntPosition(0, 0, 0), 3),
                arguments(new IntPosition(-1, -1, -1), new IntPosition(1, 1, 1), 6),
                arguments(new IntPosition(1, -1, -1), new IntPosition(-1, 1, 1), 6),
                arguments(new IntPosition(-1, 1, -1), new IntPosition(1, -1, 1), 6),
                arguments(new IntPosition(-1, -1, 1), new IntPosition(1, 1, -1), 6)
        );
    }

    @MethodSource
    @ParameterizedTest
    void testManhattanDistance(IntPosition start, IntPosition target, int expected) {
        assertThat(start.manhattanDistance(target)).isEqualTo(expected);
        assertThat(target.manhattanDistance(start)).isEqualTo(expected);
    }

    static Stream<Arguments> testContainedInChunk() {
        return Stream.of(
                arguments(new IntPosition(0, 0, 0), new ChunkPosition(0, 0), true),
                arguments(new IntPosition(15, 0, 0), new ChunkPosition(0, 0), true),
                arguments(new IntPosition(0, 0, 15), new ChunkPosition(0, 0), true),
                arguments(new IntPosition(15, 0, 15), new ChunkPosition(0, 0), true),
                arguments(new IntPosition(-1, 0, 0), new ChunkPosition(0, 0), false),
                arguments(new IntPosition(0, 0, -1), new ChunkPosition(0, 0), false),
                arguments(new IntPosition(-1, 0, -1), new ChunkPosition(0, 0), false),
                arguments(new IntPosition(16, 0, 0), new ChunkPosition(0, 0), false),
                arguments(new IntPosition(0, 0, 16), new ChunkPosition(0, 0), false),
                arguments(new IntPosition(16, 0, 16), new ChunkPosition(0, 0), false)
        );
    }

    @MethodSource
    @ParameterizedTest
    void testContainedInChunk(IntPosition position, ChunkPosition chunkPosition, boolean contained) {
        if (contained) {
            assertThat(position.isContainedInChunk(chunkPosition)).isTrue();
        } else {
            assertThat(position.isContainedInChunk(chunkPosition)).isFalse();
        }
    }
}
