package world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;

import blocks.BlockRegistry;
import blocks.Blocks;
import util.MathUtil;

public final class World {
    private static final int TERRAIN_BASE_HEIGHT = 80;
    private static final int TERRAIN_HEIGHT_VARIATION = 56;
    private static final int TERRAIN_THICKNESS = 320;
    private static final int TERRAIN_DIRT_LAYERS = 3;
    private static final int TERRAIN_BOTTOM_HEIGHT = -160;
    private static final int TREE_CELL_SIZE = 8;
    private static final int TREE_LEAF_RADIUS = 2;
    private static final int RENDER_RADIUS = 4;
    private static final int VERTICAL_RENDER_RADIUS = 4;
    private static final float FALLING_BLOCK_GRAVITY = 25.0f;
    private static final float FALLING_BLOCK_TERMINAL_VELOCITY = 40.0f;

    private final Map<ChunkPos, Chunk> chunks = new HashMap<>();
    private final List<FallingBlock> fallingBlocks = new ArrayList<>();

    public World() {
        BlockRegistry.bootstrap();
        ensureSpawnArea();
    }

    public void tick(Vector3f playerPosition, int radius) {
        int chunkX = Math.floorDiv((int) Math.floor(playerPosition.x), Chunk.SIZE);
        int chunkY = Math.floorDiv((int) Math.floor(playerPosition.y), Chunk.SIZE);
        int chunkZ = Math.floorDiv((int) Math.floor(playerPosition.z), Chunk.SIZE);
        int renderRadius = Math.max(1, radius);
        loadChunksAround(chunkX, chunkY, chunkZ, renderRadius);
        unloadChunksOutside(chunkX, chunkY, chunkZ, renderRadius);
    }

    public void ensureSpawnArea() {
        loadChunksAround(0, 0, 0, RENDER_RADIUS);
    }

    public Vector3f findSpawnPosition() {
        int bestX = 8;
        int bestZ = 8;
        int bestSurface = terrainHeight(bestX, bestZ);

        for (int radius = 0; radius <= 24; radius++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    int candidateX = 8 + dx;
                    int candidateZ = 8 + dz;
                    int surfaceHeight = terrainHeight(candidateX, candidateZ);
                    if (spawnSpaceClear(candidateX, surfaceHeight + 1, candidateZ)) {
                        return new Vector3f(candidateX + 0.5f, surfaceHeight + 1.05f, candidateZ + 0.5f);
                    }
                    if (surfaceHeight > bestSurface) {
                        bestSurface = surfaceHeight;
                        bestX = candidateX;
                        bestZ = candidateZ;
                    }
                }
            }
        }

        return new Vector3f(bestX + 0.5f, bestSurface + 1.05f, bestZ + 0.5f);
    }

    public void loadChunksAround(int centerChunkX, int centerChunkY, int centerChunkZ, int radius) {
        for (int chunkY = centerChunkY - VERTICAL_RENDER_RADIUS; chunkY <= centerChunkY + VERTICAL_RENDER_RADIUS; chunkY++) {
            for (int chunkZ = centerChunkZ - radius; chunkZ <= centerChunkZ + radius; chunkZ++) {
                for (int chunkX = centerChunkX - radius; chunkX <= centerChunkX + radius; chunkX++) {
                    ensureChunk(new ChunkPos(chunkX, chunkY, chunkZ));
                }
            }
        }
    }

    public void unloadChunksOutside(int centerChunkX, int centerChunkY, int centerChunkZ, int radius) {
        int minChunkY = centerChunkY - VERTICAL_RENDER_RADIUS;
        int maxChunkY = centerChunkY + VERTICAL_RENDER_RADIUS;
        chunks.entrySet().removeIf(entry -> {
            ChunkPos position = entry.getKey();
            int dx = Math.abs(position.x() - centerChunkX);
            int dy = Math.abs(position.y() - centerChunkY);
            int dz = Math.abs(position.z() - centerChunkZ);
            return dx > radius || dz > radius || position.y() < minChunkY || position.y() > maxChunkY;
        });
    }

    public Collection<Chunk> getLoadedChunks() {
        return chunks.values();
    }

    public Chunk getChunk(ChunkPos position) {
        return chunks.get(position);
    }

    public Chunk ensureChunk(ChunkPos position) {
        return chunks.computeIfAbsent(position, this::generateChunk);
    }

    public Map<ChunkPos, short[]> snapshotChunks() {
        Map<ChunkPos, short[]> snapshot = new HashMap<>();
        for (Map.Entry<ChunkPos, Chunk> entry : chunks.entrySet()) {
            snapshot.put(entry.getKey(), entry.getValue().copyBlocks());
        }
        return snapshot;
    }

    public void loadSnapshot(Map<ChunkPos, short[]> snapshot) {
        chunks.clear();
        for (Map.Entry<ChunkPos, short[]> entry : snapshot.entrySet()) {
            Chunk chunk = new Chunk(entry.getKey());
            chunk.loadBlocks(entry.getValue());
            chunk.clearDirty();
            chunks.put(entry.getKey(), chunk);
        }
        if (chunks.isEmpty()) {
            ensureSpawnArea();
        }
    }

    public int getBlock(int worldX, int worldY, int worldZ) {
        ChunkPos chunkPos = chunkPosition(worldX, worldY, worldZ);
        Chunk chunk = chunks.get(chunkPos);
        if (chunk != null) {
            return chunk.getBlock(localCoordinate(worldX), localCoordinate(worldY), localCoordinate(worldZ));
        }
        return generateBlock(worldX, worldY, worldZ);
    }

    public boolean isSolid(int worldX, int worldY, int worldZ) {
        return Blocks.AIR.id() != getBlock(worldX, worldY, worldZ) && BlockRegistry.get(getBlock(worldX, worldY, worldZ)).solid();
    }

    public boolean isOpaque(int worldX, int worldY, int worldZ) {
        return Blocks.AIR.id() != getBlock(worldX, worldY, worldZ) && BlockRegistry.get(getBlock(worldX, worldY, worldZ)).opaque();
    }

    public void setBlock(int worldX, int worldY, int worldZ, int blockId) {
        int previousId = getBlock(worldX, worldY, worldZ);
        setBlockInternal(worldX, worldY, worldZ, blockId);

        if (blockId == Blocks.AIR.id() && previousId != Blocks.AIR.id()) {
            checkGravityAbove(worldX, worldY, worldZ);
        } else if (blockId != Blocks.AIR.id()) {
            if (BlockRegistry.get(blockId).gravity()) {
                applyGravityAt(worldX, worldY, worldZ);
            }
            checkGravityAbove(worldX, worldY, worldZ);
        }
    }

    private void setBlockInternal(int worldX, int worldY, int worldZ, int blockId) {
        ChunkPos chunkPos = chunkPosition(worldX, worldY, worldZ);
        Chunk chunk = ensureChunk(chunkPos);
        chunk.setBlock(localCoordinate(worldX), localCoordinate(worldY), localCoordinate(worldZ), blockId);
        dirtyNeighbors(chunkPos);
    }

    private void applyGravityAt(int x, int y, int z) {
        int blockId = getBlock(x, y, z);
        if (!BlockRegistry.get(blockId).gravity()) {
            return;
        }

        int belowId = getBlock(x, y - 1, z);
        if (belowId != Blocks.AIR.id() && BlockRegistry.get(belowId).solid()) {
            return;
        }

        setBlockInternal(x, y, z, Blocks.AIR.id());
        fallingBlocks.add(new FallingBlock(x + 0.5f, y + 0.5f, z + 0.5f, blockId));
    }

    private void checkGravityAbove(int x, int y, int z) {
        int maxY = TERRAIN_BOTTOM_HEIGHT + TERRAIN_THICKNESS;
        for (int checkY = y + 1; checkY <= maxY; checkY++) {
            int blockId = getBlock(x, checkY, z);
            if (blockId == Blocks.AIR.id()) {
                continue;
            }
            if (BlockRegistry.get(blockId).gravity()) {
                applyGravityAt(x, checkY, z);
                return;
            }
            break;
        }
    }

    public void updateFallingBlocks(float deltaSeconds) {
        var snapshot = List.copyOf(fallingBlocks);
        fallingBlocks.clear();
        for (FallingBlock fb : snapshot) {
            fb.velocityY = Math.max(fb.velocityY - FALLING_BLOCK_GRAVITY * deltaSeconds, -FALLING_BLOCK_TERMINAL_VELOCITY);
            float newY = fb.y + fb.velocityY * deltaSeconds;

            int gridX = (int) Math.floor(fb.x);
            int gridZ = (int) Math.floor(fb.z);
            int gridY = (int) Math.floor(newY);

            if (gridY <= TERRAIN_BOTTOM_HEIGHT || isSolid(gridX, gridY - 1, gridZ)) {
                if (getBlock(gridX, gridY, gridZ) == Blocks.AIR.id()) {
                    setBlock(gridX, gridY, gridZ, fb.blockId);
                }
            } else {
                fb.y = newY;
                fallingBlocks.add(fb);
            }
        }
    }

    public List<FallingBlock> getFallingBlocks() {
        return fallingBlocks;
    }

    public void addFallingBlock(FallingBlock fb) {
    fallingBlocks.add(fb);
    }

    private void dirtyNeighbors(ChunkPos chunkPos) {
        markDirtyIfLoaded(new ChunkPos(chunkPos.x() - 1, chunkPos.y(), chunkPos.z()));
        markDirtyIfLoaded(new ChunkPos(chunkPos.x() + 1, chunkPos.y(), chunkPos.z()));
        markDirtyIfLoaded(new ChunkPos(chunkPos.x(), chunkPos.y() - 1, chunkPos.z()));
        markDirtyIfLoaded(new ChunkPos(chunkPos.x(), chunkPos.y() + 1, chunkPos.z()));
        markDirtyIfLoaded(new ChunkPos(chunkPos.x(), chunkPos.y(), chunkPos.z() - 1));
        markDirtyIfLoaded(new ChunkPos(chunkPos.x(), chunkPos.y(), chunkPos.z() + 1));
    }

    private void markDirtyIfLoaded(ChunkPos chunkPos) {
        Chunk neighbor = chunks.get(chunkPos);
        if (neighbor != null) {
            neighbor.markDirty();
        }
    }

    public RaycastHit raycast(Vector3f origin, Vector3f direction, float maxDistance) {
        Vector3f dir = new Vector3f(direction);
        if (dir.lengthSquared() == 0.0f) {
            return null;
        }
        dir.normalize();

        float x = origin.x;
        float y = origin.y;
        float z = origin.z;

        int blockX = (int) Math.floor(x);
        int blockY = (int) Math.floor(y);
        int blockZ = (int) Math.floor(z);

        int stepX = dir.x > 0 ? 1 : -1;
        int stepY = dir.y > 0 ? 1 : -1;
        int stepZ = dir.z > 0 ? 1 : -1;

        float tMaxX = intBound(x, dir.x);
        float tMaxY = intBound(y, dir.y);
        float tMaxZ = intBound(z, dir.z);

        float tDeltaX = stepDelta(dir.x);
        float tDeltaY = stepDelta(dir.y);
        float tDeltaZ = stepDelta(dir.z);

        BlockPos previous = new BlockPos(blockX, blockY, blockZ);
        float distance = 0.0f;

        while (distance <= maxDistance) {
            int blockId = getBlock(blockX, blockY, blockZ);
            if (BlockRegistry.get(blockId).solid()) {
                return new RaycastHit(new BlockPos(blockX, blockY, blockZ), previous, blockId, distance);
            }

            previous = new BlockPos(blockX, blockY, blockZ);
            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    blockX += stepX;
                    distance = tMaxX;
                    tMaxX += tDeltaX;
                } else {
                    blockZ += stepZ;
                    distance = tMaxZ;
                    tMaxZ += tDeltaZ;
                }
            } else {
                if (tMaxY < tMaxZ) {
                    blockY += stepY;
                    distance = tMaxY;
                    tMaxY += tDeltaY;
                } else {
                    blockZ += stepZ;
                    distance = tMaxZ;
                    tMaxZ += tDeltaZ;
                }
            }
        }

        return null;
    }

    private Chunk generateChunk(ChunkPos position) {
        Chunk chunk = new Chunk(position);
        int baseWorldY = position.y() * Chunk.SIZE;
        int baseWorldX = position.x() * Chunk.SIZE;
        int baseWorldZ = position.z() * Chunk.SIZE;
        for (int localZ = 0; localZ < Chunk.SIZE; localZ++) {
            int worldZ = baseWorldZ + localZ;
            for (int localX = 0; localX < Chunk.SIZE; localX++) {
                int worldX = baseWorldX + localX;
                int surfaceHeight = terrainHeight(worldX, worldZ);
                for (int localY = 0; localY < Chunk.SIZE; localY++) {
                    int worldY = baseWorldY + localY;
                    chunk.setBlock(localX, localY, localZ, generateBlock(worldX, worldY, worldZ, surfaceHeight));
                }
            }
        }
        chunk.clearDirty();
        return chunk;
    }

    private int generateBlock(int worldX, int worldY, int worldZ) {
        return generateBlock(worldX, worldY, worldZ, terrainHeight(worldX, worldZ));
    }

    private int generateBlock(int worldX, int worldY, int worldZ, int surfaceHeight) {
        if (worldY >= TERRAIN_BOTTOM_HEIGHT && worldY < surfaceHeight - 4 && isCave(worldX, worldY, worldZ, surfaceHeight)) {
            return Blocks.AIR.id();
        }

        if (worldY >= TERRAIN_BOTTOM_HEIGHT && worldY <= surfaceHeight) {
            if (worldY == surfaceHeight) {
                return surfaceBlock(worldX, worldZ, surfaceHeight);
            }
            if (isDesert(worldX, worldZ)) {
                if (worldY >= surfaceHeight - TERRAIN_DIRT_LAYERS) {
                    return Blocks.SAND.id();
                }
            } else {
                if (worldY >= surfaceHeight - TERRAIN_DIRT_LAYERS) {
                    return Blocks.DIRT.id();
                }
            }
            return Blocks.COBBLESTONE.id();
        }

        if (worldY > surfaceHeight && !isDesert(worldX, worldZ)) {
            int treeBlock = generateTreeBlock(worldX, worldY, worldZ);
            if (treeBlock != Blocks.AIR.id()) {
                return treeBlock;
            }
        }
        return Blocks.AIR.id();
    }

    private int surfaceBlock(int worldX, int worldZ, int surfaceHeight) {
        if (isDesert(worldX, worldZ)) {
            return Blocks.SAND.id();
        }

        float climate = valueNoise(worldX - 9000, worldZ + 3000, 0.012f);
        float detail = valueNoise(worldX + 2500, worldZ - 7000, 0.02f);
        int slope = Math.max(
                Math.abs(surfaceHeight - terrainHeight(worldX + 1, worldZ)),
                Math.abs(surfaceHeight - terrainHeight(worldX, worldZ + 1))
        );

        if (surfaceHeight > 30 || slope >= 5 || detail > 0.72f) {
            return Blocks.COBBLESTONE.id();
        }
        if (climate < 0.28f && surfaceHeight > 16) {
            return Blocks.COBBLESTONE.id();
        }
        return Blocks.GRASS.id();
    }

    private boolean isDesert(int worldX, int worldZ) {
        float dryness = valueNoise(worldX + 5000, worldZ - 3000, 0.008f);
        return dryness > 0.50f;
    }

    private float forestDensity(int worldX, int worldZ) {
        return valueNoise(worldX - 12000, worldZ + 8000, 0.012f);
    }

    private boolean isCave(int worldX, int worldY, int worldZ, int surfaceHeight) {
        if (worldY < TERRAIN_BOTTOM_HEIGHT + 4) {
            return false;
        }
        int depthBelowSurface = surfaceHeight - worldY;
        if (depthBelowSurface < 2) {
            return false;
        }

        float depth = depthBelowSurface / (float) TERRAIN_THICKNESS;

        float smallPocket = valueNoise3(worldX + 3700, worldY - 5100, worldZ - 2400, 0.14f);
        float largeCavern = valueNoise3(worldX - 9200, worldY + 3400, worldZ + 1800, 0.07f);
        float longTunnel = valueNoise3(worldX + 11200, worldY + 1200, worldZ - 2600, 0.025f);
        float giantVoid = valueNoise3(worldX - 1400, worldY + 2400, worldZ + 6200, 0.014f);
        float tunnelMask = valueNoise(worldX - 2600, worldZ + 8600, 0.008f);

        boolean smallCave = depth > 0.06f && smallPocket > 0.88f - depth * 0.06f;
        boolean largeCave = depth > 0.10f && largeCavern > 0.82f - depth * 0.08f;
        boolean longCave = depth > 0.16f && longTunnel > 0.79f - depth * 0.05f && tunnelMask > 0.34f;
        boolean giantCave = depth > 0.24f && giantVoid > 0.72f - depth * 0.04f;

        return smallCave || largeCave || longCave || giantCave;
    }

    private int generateTreeBlock(int worldX, int worldY, int worldZ) {
        int minCellX = Math.floorDiv(worldX - TREE_LEAF_RADIUS, TREE_CELL_SIZE);
        int maxCellX = Math.floorDiv(worldX + TREE_LEAF_RADIUS, TREE_CELL_SIZE);
        int minCellZ = Math.floorDiv(worldZ - TREE_LEAF_RADIUS, TREE_CELL_SIZE);
        int maxCellZ = Math.floorDiv(worldZ + TREE_LEAF_RADIUS, TREE_CELL_SIZE);

        for (int cellZ = minCellZ; cellZ <= maxCellZ; cellZ++) {
            for (int cellX = minCellX; cellX <= maxCellX; cellX++) {
                Tree tree = treeForCell(cellX, cellZ);
                if (tree == null) {
                    continue;
                }
                int block = treeBlockAt(worldX, worldY, worldZ, tree);
                if (block != Blocks.AIR.id()) {
                    return block;
                }
            }
        }
        return Blocks.AIR.id();
    }

    private Tree treeForCell(int cellX, int cellZ) {
        int centerWorldX = cellX * TREE_CELL_SIZE + TREE_CELL_SIZE / 2;
        int centerWorldZ = cellZ * TREE_CELL_SIZE + TREE_CELL_SIZE / 2;

        float density = forestDensity(centerWorldX, centerWorldZ);
        float threshold = 0.95f - density * 0.27f;
        if (hashToUnit(cellX + 9157, cellZ - 4621) < threshold) {
            return null;
        }

        int offsetX = 2 + (int) (hashToUnit(cellX + 131, cellZ + 719) * (TREE_CELL_SIZE - 4));
        int offsetZ = 2 + (int) (hashToUnit(cellX - 353, cellZ + 977) * (TREE_CELL_SIZE - 4));
        int rootX = cellX * TREE_CELL_SIZE + offsetX;
        int rootZ = cellZ * TREE_CELL_SIZE + offsetZ;
        int surfaceY = terrainHeight(rootX, rootZ);
        int height = 4 + (int) (hashToUnit(cellX + 2027, cellZ - 811) * 3.0f);
        return new Tree(rootX, surfaceY + 1, rootZ, height);
    }

    private int treeBlockAt(int worldX, int worldY, int worldZ, Tree tree) {
        if (worldX == tree.rootX && worldZ == tree.rootZ && worldY >= tree.rootY && worldY < tree.rootY + tree.height) {
            return Blocks.LOG.id();
        }

        int leafY = worldY - (tree.rootY + tree.height - 2);
        if (leafY < 0 || leafY > 3) {
            return Blocks.AIR.id();
        }

        int dx = Math.abs(worldX - tree.rootX);
        int dz = Math.abs(worldZ - tree.rootZ);
        int radius = leafY == 3 ? 1 : TREE_LEAF_RADIUS;
        if (dx > radius || dz > radius) {
            return Blocks.AIR.id();
        }
        if (dx == TREE_LEAF_RADIUS && dz == TREE_LEAF_RADIUS && hashToUnit(worldX + 41, worldZ - 17) < 0.5f) {
            return Blocks.AIR.id();
        }
        return Blocks.LEAVES.id();
    }

    private int terrainHeight(int worldX, int worldZ) {
        float continents = valueNoise(worldX - 5000, worldZ + 1000, 0.010f);
        float hills = valueNoise(worldX + 3000, worldZ - 4000, 0.024f);
        float ridges = ridgedNoise(worldX - 1000, worldZ + 8000, 0.052f);
        float detail = valueNoise(worldX + 200, worldZ - 200, 0.11f);

        float dryness = valueNoise(worldX + 5000, worldZ - 3000, 0.008f);
        float desertBlend = MathUtil.clamp((dryness - 0.30f) / 0.30f, 0.0f, 1.0f);

        float continentAmp = TERRAIN_HEIGHT_VARIATION * (1.0f - desertBlend * 0.70f);
        float hillAmp = (TERRAIN_HEIGHT_VARIATION * 0.625f) * (1.0f - desertBlend * 0.85f);
        float ridgeAmp = (TERRAIN_HEIGHT_VARIATION * 0.5f) * (1.0f - desertBlend * 0.95f);
        float detailAmp = 10.0f * (1.0f - desertBlend * 0.50f);

        float base = TERRAIN_BASE_HEIGHT
                + (continents - 0.5f) * continentAmp
                + (hills - 0.5f) * hillAmp
                + (ridges - 0.5f) * ridgeAmp
                + (detail - 0.5f) * detailAmp;

        base -= desertBlend * 12.0f;

        int height = Math.round(base);
        return Math.max(TERRAIN_BOTTOM_HEIGHT, Math.min(TERRAIN_BOTTOM_HEIGHT + TERRAIN_THICKNESS - 1, height));
    }

    private boolean spawnSpaceClear(int blockX, int feetY, int blockZ) {
        float width = 0.6f;
        float height = 1.8f;
        float minX = blockX + 0.5f - width * 0.5f;
        float maxX = blockX + 0.5f + width * 0.5f;
        float minZ = blockZ + 0.5f - width * 0.5f;
        float maxZ = blockZ + 0.5f + width * 0.5f;
        float minY = feetY;
        float maxY = feetY + height;

        int startX = (int) Math.floor(minX);
        int endX = (int) Math.floor(maxX);
        int startY = (int) Math.floor(minY);
        int endY = (int) Math.floor(maxY);
        int startZ = (int) Math.floor(minZ);
        int endZ = (int) Math.floor(maxZ);

        for (int y = startY; y <= endY; y++) {
            for (int z = startZ; z <= endZ; z++) {
                for (int x = startX; x <= endX; x++) {
                    if (isSolid(x, y, z)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static float valueNoise(int worldX, int worldZ, float scale) {
        float scaledX = worldX * scale;
        float scaledZ = worldZ * scale;

        int x0 = (int) Math.floor(scaledX);
        int z0 = (int) Math.floor(scaledZ);
        int x1 = x0 + 1;
        int z1 = z0 + 1;

        float tx = fade(scaledX - x0);
        float tz = fade(scaledZ - z0);

        float v00 = hashToUnit(x0, z0);
        float v10 = hashToUnit(x1, z0);
        float v01 = hashToUnit(x0, z1);
        float v11 = hashToUnit(x1, z1);

        float a = MathUtil.lerp(v00, v10, tx);
        float b = MathUtil.lerp(v01, v11, tx);
        return MathUtil.lerp(a, b, tz);
    }

    private static float valueNoise3(int worldX, int worldY, int worldZ, float scale) {
        float scaledX = worldX * scale;
        float scaledY = worldY * scale;
        float scaledZ = worldZ * scale;

        int x0 = (int) Math.floor(scaledX);
        int y0 = (int) Math.floor(scaledY);
        int z0 = (int) Math.floor(scaledZ);
        int x1 = x0 + 1;
        int y1 = y0 + 1;
        int z1 = z0 + 1;

        float tx = fade(scaledX - x0);
        float ty = fade(scaledY - y0);
        float tz = fade(scaledZ - z0);

        float c000 = hashToUnit(x0, y0, z0);
        float c100 = hashToUnit(x1, y0, z0);
        float c010 = hashToUnit(x0, y1, z0);
        float c110 = hashToUnit(x1, y1, z0);
        float c001 = hashToUnit(x0, y0, z1);
        float c101 = hashToUnit(x1, y0, z1);
        float c011 = hashToUnit(x0, y1, z1);
        float c111 = hashToUnit(x1, y1, z1);

        float x00 = MathUtil.lerp(c000, c100, tx);
        float x10 = MathUtil.lerp(c010, c110, tx);
        float x01 = MathUtil.lerp(c001, c101, tx);
        float x11 = MathUtil.lerp(c011, c111, tx);

        float y0Blend = MathUtil.lerp(x00, x10, ty);
        float y1Blend = MathUtil.lerp(x01, x11, ty);
        return MathUtil.lerp(y0Blend, y1Blend, tz);
    }

    private static float ridgedNoise(int worldX, int worldZ, float scale) {
        float noise = valueNoise(worldX, worldZ, scale);
        return 1.0f - Math.abs(noise * 2.0f - 1.0f);
    }

    private static float fade(float t) {
        return t * t * (3.0f - 2.0f * t);
    }

    private static float hashToUnit(int x, int z) {
        int hash = x * 374761393 + z * 668265263;
        hash = (hash ^ (hash >>> 13)) * 1274126177;
        hash ^= hash >>> 16;
        return (hash & 0x7fffffff) / (float) Integer.MAX_VALUE;
    }

    private static float hashToUnit(int x, int y, int z) {
        int hash = x * 374761393 ^ y * 668265263 ^ z * 2147483647;
        hash = (hash ^ (hash >>> 13)) * 1274126177;
        hash ^= hash >>> 16;
        return (hash & 0x7fffffff) / (float) Integer.MAX_VALUE;
    }

    private record Tree(int rootX, int rootY, int rootZ, int height) {
    }

    private static ChunkPos chunkPosition(int worldX, int worldY, int worldZ) {
        return new ChunkPos(Math.floorDiv(worldX, Chunk.SIZE), Math.floorDiv(worldY, Chunk.SIZE), Math.floorDiv(worldZ, Chunk.SIZE));
    }

    private static int localCoordinate(int value) {
        return Math.floorMod(value, Chunk.SIZE);
    }

    private static float intBound(float s, float ds) {
        if (ds == 0.0f) {
            return Float.POSITIVE_INFINITY;
        }
        float sFrac = s - (float) Math.floor(s);
        if (ds > 0) {
            return (1.0f - sFrac) / ds;
        }
        return sFrac / -ds;
    }

    private static float stepDelta(float ds) {
        return ds == 0.0f ? Float.POSITIVE_INFINITY : Math.abs(1.0f / ds);
    }
}
