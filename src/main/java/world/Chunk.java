package world;

import blocks.Blocks;

public final class Chunk {
    public static final int SIZE = 16;
    public static final int VOLUME = SIZE * SIZE * SIZE;

    private final ChunkPos position;
    private final short[] blocks = new short[VOLUME];
    private boolean dirty = true;

    public Chunk(ChunkPos position) {
        this.position = position;
    }

    public ChunkPos position() {
        return position;
    }

    public int getBlock(int localX, int localY, int localZ) {
        if (!isInside(localX, localY, localZ)) {
            return Blocks.AIR.id();
        }
        return blocks[index(localX, localY, localZ)];
    }

    public void setBlock(int localX, int localY, int localZ, int blockId) {
        if (!isInside(localX, localY, localZ)) {
            return;
        }
        blocks[index(localX, localY, localZ)] = (short) blockId;
        dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void clearDirty() {
        dirty = false;
    }

    public void markDirty() {
        dirty = true;
    }

    public void fill(short blockId) {
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = blockId;
        }
        dirty = true;
    }

    public short[] copyBlocks() {
        short[] copy = new short[blocks.length];
        System.arraycopy(blocks, 0, copy, 0, blocks.length);
        return copy;
    }

    public void loadBlocks(short[] source) {
        if (source == null || source.length != blocks.length) {
            throw new IllegalArgumentException("Invalid chunk block payload");
        }
        System.arraycopy(source, 0, blocks, 0, blocks.length);
        dirty = true;
    }

    private int index(int localX, int localY, int localZ) {
        return (localY * SIZE + localZ) * SIZE + localX;
    }

    private boolean isInside(int localX, int localY, int localZ) {
        return localX >= 0 && localX < SIZE && localY >= 0 && localY < SIZE && localZ >= 0 && localZ < SIZE;
    }
}
