package world;

public final class FallingBlock {
    public float x;
    public float y;
    public float z;
    public float velocityY;
    public int blockId;

    public FallingBlock(float x, float y, float z, int blockId) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockId = blockId;
        this.velocityY = 0.0f;
    }
}
