package blocks;

public final class Blocks {
    public static final Block AIR = new Block(0, "air", null, false);
    public static final Block DIRT = new Block(1, "dirt", "dirt", true);
    public static final Block COBBLESTONE = new Block(2, "cobblestone", "cobblestone", true);
    public static final Block GRASS = new Block(3, "grass", "grass_block_top", "grass_block_side", "dirt", true);
    public static final Block LOG = new Block(4, "log", "oak_log_top", "oak_log", "oak_log_top", true);
    public static final Block LEAVES = new Block(5, "leaves", "oak_leaves", true, false);
    public static final Block SAND = new Block(6, "sand", "sand", "sand", "sand", true, true, true);

    private Blocks() {
    }
}
