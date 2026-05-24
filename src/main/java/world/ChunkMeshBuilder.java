package world;

import blocks.Block;
import blocks.BlockRegistry;
import blocks.Blocks;

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
        List<Float> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        int vertexBase = 0;

        int chunkWorldX = chunk.position().x() * Chunk.SIZE;
        int chunkWorldY = chunk.position().y() * Chunk.SIZE;
        int chunkWorldZ = chunk.position().z() * Chunk.SIZE;

        for (int localY = 0; localY < Chunk.SIZE; localY++) {
            for (int localZ = 0; localZ < Chunk.SIZE; localZ++) {
                for (int localX = 0; localX < Chunk.SIZE; localX++) {
                    int blockId = chunk.getBlock(localX, localY, localZ);
                    Block block = BlockRegistry.get(blockId);
                    if (block == Blocks.AIR || !block.solid()) {
                        continue;
                    }

                    int worldX = chunkWorldX + localX;
                    int worldY = chunkWorldY + localY;
                    int worldZ = chunkWorldZ + localZ;
                    int textureSlot = block.textureSlot();

                    if (!world.isSolid(worldX, worldY + 1, worldZ)) {
                        vertexBase = addFace(vertices, indices, vertexBase, worldX, worldY, worldZ, textureSlot, Face.TOP);
                    }
                    if (!world.isSolid(worldX, worldY - 1, worldZ)) {
                        vertexBase = addFace(vertices, indices, vertexBase, worldX, worldY, worldZ, textureSlot, Face.BOTTOM);
                    }
                    if (!world.isSolid(worldX - 1, worldY, worldZ)) {
                        vertexBase = addFace(vertices, indices, vertexBase, worldX, worldY, worldZ, textureSlot, Face.WEST);
                    }
                    if (!world.isSolid(worldX + 1, worldY, worldZ)) {
                        vertexBase = addFace(vertices, indices, vertexBase, worldX, worldY, worldZ, textureSlot, Face.EAST);
                    }
                    if (!world.isSolid(worldX, worldY, worldZ + 1)) {
                        vertexBase = addFace(vertices, indices, vertexBase, worldX, worldY, worldZ, textureSlot, Face.SOUTH);
                    }
                    if (!world.isSolid(worldX, worldY, worldZ - 1)) {
                        vertexBase = addFace(vertices, indices, vertexBase, worldX, worldY, worldZ, textureSlot, Face.NORTH);
                    }
                }
            }
        }

        return new ChunkMeshData(toFloatArray(vertices), toIntArray(indices));
    }

    private static int addFace(List<Float> vertices, List<Integer> indices, int vertexBase, int x, int y, int z, int textureSlot, Face face) {
        float minX = x;
        float minY = y;
        float minZ = z;
        float maxX = x + 1.0f;
        float maxY = y + 1.0f;
        float maxZ = z + 1.0f;

        float u0 = (textureSlot % 2) * 0.5f;
        float v0 = (textureSlot / 2) * 0.5f;
        float u1 = u0 + 0.5f;
        float v1 = v0 + 0.5f;
        float[] normal = FACE_NORMALS[face.ordinal()];

        switch (face) {
            case TOP -> {
                addVertex(vertices, minX, maxY, minZ, u0, v0, normal);
                addVertex(vertices, minX, maxY, maxZ, u0, v1, normal);
                addVertex(vertices, maxX, maxY, maxZ, u1, v1, normal);
                addVertex(vertices, maxX, maxY, minZ, u1, v0, normal);
            }
            case BOTTOM -> {
                addVertex(vertices, minX, minY, minZ, u0, v0, normal);
                addVertex(vertices, maxX, minY, minZ, u1, v0, normal);
                addVertex(vertices, maxX, minY, maxZ, u1, v1, normal);
                addVertex(vertices, minX, minY, maxZ, u0, v1, normal);
            }
            case WEST -> {
                addVertex(vertices, minX, minY, minZ, u0, v1, normal);
                addVertex(vertices, minX, minY, maxZ, u1, v1, normal);
                addVertex(vertices, minX, maxY, maxZ, u1, v0, normal);
                addVertex(vertices, minX, maxY, minZ, u0, v0, normal);
            }
            case EAST -> {
                addVertex(vertices, maxX, minY, maxZ, u0, v1, normal);
                addVertex(vertices, maxX, minY, minZ, u1, v1, normal);
                addVertex(vertices, maxX, maxY, minZ, u1, v0, normal);
                addVertex(vertices, maxX, maxY, maxZ, u0, v0, normal);
            }
            case SOUTH -> {
                addVertex(vertices, minX, minY, maxZ, u0, v1, normal);
                addVertex(vertices, maxX, minY, maxZ, u1, v1, normal);
                addVertex(vertices, maxX, maxY, maxZ, u1, v0, normal);
                addVertex(vertices, minX, maxY, maxZ, u0, v0, normal);
            }
            case NORTH -> {
                addVertex(vertices, maxX, minY, minZ, u0, v1, normal);
                addVertex(vertices, minX, minY, minZ, u1, v1, normal);
                addVertex(vertices, minX, maxY, minZ, u1, v0, normal);
                addVertex(vertices, maxX, maxY, minZ, u0, v0, normal);
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

    private static void addVertex(List<Float> vertices, float x, float y, float z, float u, float v, float[] normal) {
        vertices.add(x);
        vertices.add(y);
        vertices.add(z);
        vertices.add(u);
        vertices.add(v);
        vertices.add(normal[0]);
        vertices.add(normal[1]);
        vertices.add(normal[2]);
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
