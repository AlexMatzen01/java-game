package blocks;

public final class Blocks {
    public static final Block AIR = new Block(0, "air", -1, false);
    public static final Block DIRT = new Block(1, "dirt", 0, true);
    public static final Block COBBLESTONE = new Block(2, "cobblestone", 1, true);

    private Blocks() {
    }
}
