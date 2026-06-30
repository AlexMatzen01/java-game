package render;

import org.joml.Vector3f;

public final class Particle {
    public final Vector3f position = new Vector3f();
    public final Vector3f velocity = new Vector3f();
    public float life;
    public float maxLife;
    public float size;
    public int textureSlot;

    public Particle(float x, float y, float z, float vx, float vy, float vz, float life, float size, int textureSlot) {
        this.position.set(x, y, z);
        this.velocity.set(vx, vy, vz);
        this.life = life;
        this.maxLife = life;
        this.size = size;
        this.textureSlot = textureSlot;
    }
}
