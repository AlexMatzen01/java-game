package blocks;

public record Block(int id, String name, String topTexture, String sideTexture, String bottomTexture, boolean solid, boolean opaque, boolean gravity) {
    public Block(int id, String name, String texture, boolean solid) {
        this(id, name, texture, texture, texture, solid, true, false);
    }

    public Block(int id, String name, String texture, boolean solid, boolean opaque) {
        this(id, name, texture, texture, texture, solid, opaque, false);
    }

    public Block(int id, String name, String texture, boolean solid, boolean opaque, boolean gravity) {
        this(id, name, texture, texture, texture, solid, opaque, gravity);
    }

    public Block(int id, String name, String topTexture, String sideTexture, String bottomTexture, boolean solid) {
        this(id, name, topTexture, sideTexture, bottomTexture, solid, true, false);
    }

    public Block(int id, String name, String topTexture, String sideTexture, String bottomTexture, boolean solid, boolean opaque) {
        this(id, name, topTexture, sideTexture, bottomTexture, solid, opaque, false);
    }
}
