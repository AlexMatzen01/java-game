package blocks;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class BlockRegistry {
    private static final Map<Integer, Block> BLOCKS_BY_ID = new HashMap<>();

    private BlockRegistry() {
    }

    public static void register(Block block) {
        BLOCKS_BY_ID.put(block.id(), block);
    }

    public static Block get(int id) {
        return BLOCKS_BY_ID.getOrDefault(id, Blocks.AIR);
    }

    public static Collection<Block> all() {
        return BLOCKS_BY_ID.values();
    }

    public static Collection<String> allTextureNames() {
        Set<String> names = new HashSet<>();
        for (Block block : BLOCKS_BY_ID.values()) {
            if (block.topTexture() != null) {
                names.add(block.topTexture());
            }
            if (block.sideTexture() != null) {
                names.add(block.sideTexture());
            }
            if (block.bottomTexture() != null) {
                names.add(block.bottomTexture());
            }
        }
        return names;
    }

    public static void bootstrap() {
        if (!BLOCKS_BY_ID.isEmpty()) {
            return;
        }
        register(Blocks.AIR);
        register(Blocks.DIRT);
        register(Blocks.COBBLESTONE);
        register(Blocks.GRASS);
        register(Blocks.LOG);
        register(Blocks.LEAVES);
        register(Blocks.SAND);
    }
}
