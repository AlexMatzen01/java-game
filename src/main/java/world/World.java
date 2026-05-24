package world;

import blocks.BlockRegistry;
import blocks.Blocks;
import org.joml.Vector3f;
import util.MathUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class World {
    private static final int TERRAIN_BASE_HEIGHT = 12;
    private static final int TERRAIN_HEIGHT_VARIATION = 7;
    private static final int TERRAIN_DIRT_LAYERS = 2;
    private static final int RENDER_RADIUS = 4;

    private final Map<ChunkPos, Chunk> chunks = new HashMap<>();

    public World() {
        BlockRegistry.bootstrap();
        ensureSpawnArea();
    }

    public void tick(Vector3f playerPosition) {
        int chunkX = Math.floorDiv((int) Math.floor(playerPosition.x), Chunk.SIZE);
        int chunkY = Math.floorDiv((int) Math.floor(playerPosition.y), Chunk.SIZE);
        int chunkZ = Math.floorDiv((int) Math.floor(playerPosition.z), Chunk.SIZE);
        loadChunksAround(chunkX, chunkY, chunkZ, RENDER_RADIUS);
    }

    public void ensureSpawnArea() {
        loadChunksAround(0, 0, 0, RENDER_RADIUS);
    }

    public void loadChunksAround(int centerChunkX, int centerChunkY, int centerChunkZ, int radius) {
        for (int chunkY = centerChunkY - 1; chunkY <= centerChunkY + 1; chunkY++) {
            for (int chunkZ = centerChunkZ - radius; chunkZ <= centerChunkZ + radius; chunkZ++) {
                for (int chunkX = centerChunkX - radius; chunkX <= centerChunkX + radius; chunkX++) {
                    ensureChunk(new ChunkPos(chunkX, chunkY, chunkZ));
                }
            }
        }
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

    public void setBlock(int worldX, int worldY, int worldZ, int blockId) {
        ChunkPos chunkPos = chunkPosition(worldX, worldY, worldZ);
        Chunk chunk = ensureChunk(chunkPos);
        chunk.setBlock(localCoordinate(worldX), localCoordinate(worldY), localCoordinate(worldZ), blockId);
        dirtyNeighbors(chunkPos);
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
        for (int localY = 0; localY < Chunk.SIZE; localY++) {
            int worldY = baseWorldY + localY;
            for (int localZ = 0; localZ < Chunk.SIZE; localZ++) {
                for (int localX = 0; localX < Chunk.SIZE; localX++) {
                    int worldX = position.x() * Chunk.SIZE + localX;
                    int worldZ = position.z() * Chunk.SIZE + localZ;
                    chunk.setBlock(localX, localY, localZ, generateBlock(worldX, worldY, worldZ));
                }
            }
        }
        chunk.clearDirty();
        return chunk;
    }

    private int generateBlock(int worldX, int worldY, int worldZ) {
        int surfaceHeight = terrainHeight(worldX, worldZ);
        if (worldY < 0 || worldY > surfaceHeight) {
            return Blocks.AIR.id();
        }
        if (worldY >= surfaceHeight - TERRAIN_DIRT_LAYERS) {
            return Blocks.DIRT.id();
        }
        return Blocks.COBBLESTONE.id();
    }

    private int terrainHeight(int worldX, int worldZ) {
        float coarse = valueNoise(worldX, worldZ, 0.035f);
        float medium = valueNoise(worldX, worldZ, 0.075f);
        float detail = valueNoise(worldX, worldZ, 0.15f);
        float blended = coarse * 0.6f + medium * 0.3f + detail * 0.1f;
        int offset = Math.round((blended * 2.0f - 1.0f) * TERRAIN_HEIGHT_VARIATION);
        return TERRAIN_BASE_HEIGHT + offset;
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

    private static float fade(float t) {
        return t * t * (3.0f - 2.0f * t);
    }

    private static float hashToUnit(int x, int z) {
        int hash = x * 374761393 + z * 668265263;
        hash = (hash ^ (hash >>> 13)) * 1274126177;
        hash ^= hash >>> 16;
        return (hash & 0x7fffffff) / (float) Integer.MAX_VALUE;
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
