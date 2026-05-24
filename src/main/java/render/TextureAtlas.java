package render;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TextureAtlas {
    private static final String[] TEXTURE_FILES = {
            "dirt",
        "cobblestone"
    };

    private final int tileSize;
    private final int width;
    private final int height;
    private final ByteBuffer pixels;

    private TextureAtlas(int tileSize, int width, int height, ByteBuffer pixels) {
        this.tileSize = tileSize;
        this.width = width;
        this.height = height;
        this.pixels = pixels;
    }

    public static TextureAtlas load() {
        LoadedTexture[] textures = new LoadedTexture[TEXTURE_FILES.length];
        int tileSize = 0;
        for (int i = 0; i < TEXTURE_FILES.length; i++) {
            textures[i] = loadTexture(TEXTURE_FILES[i]);
            tileSize = Math.max(tileSize, Math.max(textures[i].width, textures[i].height));
        }

        int atlasWidth = tileSize * 2;
        int atlasHeight = tileSize * 2;
        ByteBuffer atlasPixels = MemoryUtil.memAlloc(atlasWidth * atlasHeight * 4);

        for (int index = 0; index < textures.length; index++) {
            LoadedTexture texture = textures[index];
            int tileX = index % 2;
            int tileY = index / 2;
            for (int y = 0; y < tileSize; y++) {
                int destinationRow = ((tileY * tileSize + y) * atlasWidth + tileX * tileSize) * 4;
                int sourceY = Math.min(texture.height - 1, (int) ((long) y * texture.height / tileSize));
                for (int x = 0; x < tileSize; x++) {
                    int sourceX = Math.min(texture.width - 1, (int) ((long) x * texture.width / tileSize));
                    int sourceIndex = (sourceY * texture.width + sourceX) * 4;
                    int destinationIndex = destinationRow + x * 4;
                    atlasPixels.put(destinationIndex, texture.pixels.get(sourceIndex));
                    atlasPixels.put(destinationIndex + 1, texture.pixels.get(sourceIndex + 1));
                    atlasPixels.put(destinationIndex + 2, texture.pixels.get(sourceIndex + 2));
                    atlasPixels.put(destinationIndex + 3, texture.pixels.get(sourceIndex + 3));
                }
            }
            STBImage.stbi_image_free(texture.pixels);
        }

        return new TextureAtlas(tileSize, atlasWidth, atlasHeight, atlasPixels);
    }

    public int tileSize() {
        return tileSize;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public ByteBuffer pixels() {
        return pixels;
    }

    public float[] regionUv(int slot, float u, float v) {
        int tileX = slot % 2;
        int tileY = slot / 2;
        float tileWidth = 1.0f / 2.0f;
        float tileHeight = 1.0f / 2.0f;
        return new float[]{tileX * tileWidth + u * tileWidth, tileY * tileHeight + v * tileHeight};
    }

    public void free() {
        MemoryUtil.memFree(pixels);
    }

    private static LoadedTexture loadTexture(String baseName) {
        byte[] bytes = readTextureBytes(baseName);
        ByteBuffer encoded = MemoryUtil.memAlloc(bytes.length);
        encoded.put(bytes).flip();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer widthBuffer = stack.mallocInt(1);
            IntBuffer heightBuffer = stack.mallocInt(1);
            IntBuffer channelsBuffer = stack.mallocInt(1);
            STBImage.stbi_set_flip_vertically_on_load(true);
            ByteBuffer pixels = STBImage.stbi_load_from_memory(encoded, widthBuffer, heightBuffer, channelsBuffer, 4);
            if (pixels == null) {
                throw new IllegalStateException("Failed to load texture '" + baseName + "': " + STBImage.stbi_failure_reason());
            }
            return new LoadedTexture(widthBuffer.get(0), heightBuffer.get(0), pixels);
        } finally {
            MemoryUtil.memFree(encoded);
        }
    }

    private static byte[] readTextureBytes(String baseName) {
        String[] candidates = {
                "assets/textures/" + baseName + ".png",
                "assets/textures/" + baseName + ".jpg",
                "src/main/resources/assets/textures/" + baseName + ".png",
                "src/main/resources/assets/textures/" + baseName + ".jpg"
        };
        for (String candidate : candidates) {
            Path path = Path.of(candidate);
            if (Files.exists(path)) {
                try {
                    return Files.readAllBytes(path);
                } catch (IOException exception) {
                    throw new IllegalStateException("Failed to read texture file: " + candidate, exception);
                }
            }
        }
        throw new IllegalStateException("Missing texture file for: " + baseName);
    }

    private record LoadedTexture(int width, int height, ByteBuffer pixels) {
    }
}
