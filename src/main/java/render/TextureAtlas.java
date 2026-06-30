package render;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public final class TextureAtlas {
    public static int COLUMNS;
    public static int ROWS;

    private static final Map<String, Integer> TEXTURE_SLOTS = new HashMap<>();

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

    public static int getSlot(String name) {
        Integer slot = TEXTURE_SLOTS.get(name);
        if (slot == null) {
            throw new IllegalArgumentException("Unknown block texture: " + name);
        }
        return slot;
    }

    public static TextureAtlas load(Collection<String> textureNames) {
        List<String> names = new ArrayList<>(textureNames);
        Collections.sort(names);

        int count = names.size();
        COLUMNS = (int) Math.ceil(Math.sqrt(count));
        ROWS = (int) Math.ceil((double) count / COLUMNS);

        TEXTURE_SLOTS.clear();
        for (int i = 0; i < names.size(); i++) {
            TEXTURE_SLOTS.put(names.get(i), i);
        }

        LoadedTexture[] textures = new LoadedTexture[names.size()];
        int tileSize = 0;
        for (int i = 0; i < names.size(); i++) {
            textures[i] = loadTexture(names.get(i));
            tileSize = Math.max(tileSize, Math.max(textures[i].width, textures[i].height));
        }

        int atlasWidth = tileSize * COLUMNS;
        int atlasHeight = tileSize * ROWS;
        ByteBuffer atlasPixels = MemoryUtil.memCalloc(atlasWidth * atlasHeight * 4);

        for (int index = 0; index < textures.length; index++) {
            LoadedTexture texture = textures[index];
            int tileX = index % COLUMNS;
            int tileY = index / COLUMNS;
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

    public void free() {
        MemoryUtil.memFree(pixels);
    }

    private static LoadedTexture loadTexture(String name) {
        byte[] bytes = readTextureBytes(name);
        ByteBuffer encoded = MemoryUtil.memAlloc(bytes.length);
        encoded.put(bytes).flip();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer widthBuffer = stack.mallocInt(1);
            IntBuffer heightBuffer = stack.mallocInt(1);
            IntBuffer channelsBuffer = stack.mallocInt(1);
            STBImage.stbi_set_flip_vertically_on_load(true);
            ByteBuffer pixels = STBImage.stbi_load_from_memory(encoded, widthBuffer, heightBuffer, channelsBuffer, 4);
            if (pixels == null) {
                throw new IllegalStateException("Failed to load texture '" + name + "': " + STBImage.stbi_failure_reason());
            }
            return new LoadedTexture(widthBuffer.get(0), heightBuffer.get(0), pixels);
        } finally {
            MemoryUtil.memFree(encoded);
        }
    }

    private static byte[] readTextureBytes(String name) {
        Path path = Path.of("assets/cubic/" + name + ".png");
        if (Files.exists(path)) {
            try {
                return Files.readAllBytes(path);
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to read texture file: " + path, exception);
            }
        }
        throw new IllegalStateException("Missing texture file for: " + name);
    }

    private record LoadedTexture(int width, int height, ByteBuffer pixels) {
    }
}
