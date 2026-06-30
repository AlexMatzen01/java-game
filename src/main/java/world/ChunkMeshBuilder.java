package world;

import blocks.Block;
import blocks.BlockRegistry;
import blocks.Blocks;
import render.TextureAtlas;

import java.util.ArrayList;
import java.util.List;

public final class ChunkMeshBuilder {
    private static final float[][] FACE_NORMALS = {
        {0.0f, 1.0f, 0.0f},
        {0.0f, -1.0f, 0.0f},
        {-1.0f, 0.0f, 0.0f},
        {1.0f, 0.0f, 0.0f},
        {0.0f, 0.0f, 1.0f},
        {0.0f, 0.0f, -1.0f}
    };

    private ChunkMeshBuilder() {
    }

    public static ChunkMeshData build(Chunk chunk, World world) {
        return build(chunk, world, 0);
    }

    public static ChunkMeshData build(Chunk chunk, World world, int lodLevel) {
        List<Float> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        int vertexBase = 0;
        int step = Math.max(1, 1 << Math.max(0, lodLevel));

        int chunkWorldX = chunk.position().x() * Chunk.SIZE;
        int chunkWorldY = chunk.position().y() * Chunk.SIZE;
        int chunkWorldZ = chunk.position().z() * Chunk.SIZE;

        for (int localY = 0; localY < Chunk.SIZE; localY += step) {
            for (int localZ = 0; localZ < Chunk.SIZE; localZ += step) {
                for (int localX = 0; localX < Chunk.SIZE; localX += step) {
                    int blockId = sampleBlock(chunk, world, localX, localY, localZ, step);
                    Block block = BlockRegistry.get(blockId);
                    if (block == Blocks.AIR || !block.solid()) {
                        continue;
                    }

                    int worldX = chunkWorldX + localX;
                    int worldY = chunkWorldY + localY;
                    int worldZ = chunkWorldZ + localZ;
                    if (!sampleOpaque(chunk, world, localX, localY + step, localZ, step)) {
                        vertexBase = addFace(vertices, indices, vertexBase, world, worldX, worldY, worldZ, step, block.topTexture(), Face.TOP);
                    }
                    if (!sampleOpaque(chunk, world, localX, localY - step, localZ, step)) {
                        vertexBase = addFace(vertices, indices, vertexBase, world, worldX, worldY, worldZ, step, block.bottomTexture(), Face.BOTTOM);
                    }
                    if (!sampleOpaque(chunk, world, localX - step, localY, localZ, step)) {
                        vertexBase = addFace(vertices, indices, vertexBase, world, worldX, worldY, worldZ, step, block.sideTexture(), Face.WEST);
                    }
                    if (!sampleOpaque(chunk, world, localX + step, localY, localZ, step)) {
                        vertexBase = addFace(vertices, indices, vertexBase, world, worldX, worldY, worldZ, step, block.sideTexture(), Face.EAST);
                    }
                    if (!sampleOpaque(chunk, world, localX, localY, localZ + step, step)) {
                        vertexBase = addFace(vertices, indices, vertexBase, world, worldX, worldY, worldZ, step, block.sideTexture(), Face.SOUTH);
                    }
                    if (!sampleOpaque(chunk, world, localX, localY, localZ - step, step)) {
                        vertexBase = addFace(vertices, indices, vertexBase, world, worldX, worldY, worldZ, step, block.sideTexture(), Face.NORTH);
                    }
                }
            }
        }

        return new ChunkMeshData(toFloatArray(vertices), toIntArray(indices));
    }

    private static int addFace(
            List<Float> vertices,
            List<Integer> indices,
            int vertexBase,
            World world,
            int x,
            int y,
            int z,
            int size,
            String textureName,
            Face face
    ) {
        int textureSlot = TextureAtlas.getSlot(textureName);
        float minX = x;
        float minY = y;
        float minZ = z;
        float maxX = x + size;
        float maxY = y + size;
        float maxZ = z + size;

        float tileWidth = 1.0f / TextureAtlas.COLUMNS;
        float tileHeight = 1.0f / TextureAtlas.ROWS;
        float u0 = (textureSlot % TextureAtlas.COLUMNS) * tileWidth;
        float v0 = (textureSlot / TextureAtlas.COLUMNS) * tileHeight;
        float u1 = u0 + tileWidth;
        float v1 = v0 + tileHeight;
        float[] normal = FACE_NORMALS[face.ordinal()];

        switch (face) {
            case TOP -> {
                addVertex(vertices, minX, maxY, minZ, u0, v0, normal, vertexAo(world, x, y, z, face, -1, -1));
                addVertex(vertices, minX, maxY, maxZ, u0, v1, normal, vertexAo(world, x, y, z, face, -1, 1));
                addVertex(vertices, maxX, maxY, maxZ, u1, v1, normal, vertexAo(world, x, y, z, face, 1, 1));
                addVertex(vertices, maxX, maxY, minZ, u1, v0, normal, vertexAo(world, x, y, z, face, 1, -1));
            }
            case BOTTOM -> {
                addVertex(vertices, minX, minY, minZ, u0, v0, normal, vertexAo(world, x, y, z, face, -1, -1));
                addVertex(vertices, maxX, minY, minZ, u1, v0, normal, vertexAo(world, x, y, z, face, 1, -1));
                addVertex(vertices, maxX, minY, maxZ, u1, v1, normal, vertexAo(world, x, y, z, face, 1, 1));
                addVertex(vertices, minX, minY, maxZ, u0, v1, normal, vertexAo(world, x, y, z, face, -1, 1));
            }
            case WEST -> {
                addVertex(vertices, minX, minY, minZ, u0, v0, normal, vertexAo(world, x, y, z, face, -1, -1));
                addVertex(vertices, minX, minY, maxZ, u1, v0, normal, vertexAo(world, x, y, z, face, -1, 1));
                addVertex(vertices, minX, maxY, maxZ, u1, v1, normal, vertexAo(world, x, y, z, face, 1, 1));
                addVertex(vertices, minX, maxY, minZ, u0, v1, normal, vertexAo(world, x, y, z, face, 1, -1));
            }
            case EAST -> {
                addVertex(vertices, maxX, minY, maxZ, u0, v0, normal, vertexAo(world, x, y, z, face, -1, 1));
                addVertex(vertices, maxX, minY, minZ, u1, v0, normal, vertexAo(world, x, y, z, face, -1, -1));
                addVertex(vertices, maxX, maxY, minZ, u1, v1, normal, vertexAo(world, x, y, z, face, 1, -1));
                addVertex(vertices, maxX, maxY, maxZ, u0, v1, normal, vertexAo(world, x, y, z, face, 1, 1));
            }
            case SOUTH -> {
                addVertex(vertices, minX, minY, maxZ, u0, v0, normal, vertexAo(world, x, y, z, face, -1, -1));
                addVertex(vertices, maxX, minY, maxZ, u1, v0, normal, vertexAo(world, x, y, z, face, 1, -1));
                addVertex(vertices, maxX, maxY, maxZ, u1, v1, normal, vertexAo(world, x, y, z, face, 1, 1));
                addVertex(vertices, minX, maxY, maxZ, u0, v1, normal, vertexAo(world, x, y, z, face, -1, 1));
            }
            case NORTH -> {
                addVertex(vertices, maxX, minY, minZ, u0, v0, normal, vertexAo(world, x, y, z, face, 1, -1));
                addVertex(vertices, minX, minY, minZ, u1, v0, normal, vertexAo(world, x, y, z, face, -1, -1));
                addVertex(vertices, minX, maxY, minZ, u1, v1, normal, vertexAo(world, x, y, z, face, -1, 1));
                addVertex(vertices, maxX, maxY, minZ, u0, v1, normal, vertexAo(world, x, y, z, face, 1, 1));
            }
        }

        indices.add(vertexBase);
        indices.add(vertexBase + 1);
        indices.add(vertexBase + 2);
        indices.add(vertexBase);
        indices.add(vertexBase + 2);
        indices.add(vertexBase + 3);
        return vertexBase + 4;
    }

    private static int sampleBlock(Chunk chunk, World world, int localX, int localY, int localZ, int step) {
        return chunk.getBlock(localX, localY, localZ);
    }

    private static boolean sampleSolid(Chunk chunk, World world, int localX, int localY, int localZ, int step) {
        int worldX = chunk.position().x() * Chunk.SIZE + localX;
        int worldY = chunk.position().y() * Chunk.SIZE + localY;
        int worldZ = chunk.position().z() * Chunk.SIZE + localZ;
        return world.isSolid(worldX, worldY, worldZ);
    }

    private static boolean sampleOpaque(Chunk chunk, World world, int localX, int localY, int localZ, int step) {
        int worldX = chunk.position().x() * Chunk.SIZE + localX;
        int worldY = chunk.position().y() * Chunk.SIZE + localY;
        int worldZ = chunk.position().z() * Chunk.SIZE + localZ;
        return world.isOpaque(worldX, worldY, worldZ);
    }

    private static float vertexAo(World world, int x, int y, int z, Face face, int tangentA, int tangentB) {
        int nx = 0;
        int ny = 0;
        int nz = 0;
        int ax = 0;
        int ay = 0;
        int az = 0;
        int bx = 0;
        int by = 0;
        int bz = 0;

        switch (face) {
            case TOP -> {
                ny = 1;
                ax = tangentA;
                bz = tangentB;
            }
            case BOTTOM -> {
                ny = -1;
                ax = tangentA;
                bz = tangentB;
            }
            case WEST -> {
                nx = -1;
                ay = tangentA;
                bz = tangentB;
            }
            case EAST -> {
                nx = 1;
                ay = tangentA;
                bz = tangentB;
            }
            case SOUTH -> {
                nz = 1;
                ax = tangentA;
                by = tangentB;
            }
            case NORTH -> {
                nz = -1;
                ax = tangentA;
                by = tangentB;
            }
        }

        boolean sideA = world.isSolid(x + nx + ax, y + ny + ay, z + nz + az);
        boolean sideB = world.isSolid(x + nx + bx, y + ny + by, z + nz + bz);
        boolean corner = world.isSolid(x + nx + ax + bx, y + ny + ay + by, z + nz + az + bz);
        int occlusion = (sideA ? 1 : 0) + (sideB ? 1 : 0) + (corner ? 1 : 0);
        if (sideA && sideB) {
            return 0.46f;
        }
        return 1.0f - occlusion * 0.16f;
    }

    private static void addVertex(List<Float> vertices, float x, float y, float z, float u, float v, float[] normal, float ao) {
        vertices.add(x);
        vertices.add(y);
        vertices.add(z);
        vertices.add(u);
        vertices.add(v);
        vertices.add(normal[0]);
        vertices.add(normal[1]);
        vertices.add(normal[2]);
        vertices.add(ao);
    }

    private static float[] toFloatArray(List<Float> values) {
        float[] array = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            array[i] = values.get(i);
        }
        return array;
    }

    private static int[] toIntArray(List<Integer> values) {
        int[] array = new int[values.size()];
        for (int i = 0; i < values.size(); i++) {
            array[i] = values.get(i);
        }
        return array;
    }

    private enum Face {
        TOP,
        BOTTOM,
        WEST,
        EAST,
        SOUTH,
        NORTH
    }
}
