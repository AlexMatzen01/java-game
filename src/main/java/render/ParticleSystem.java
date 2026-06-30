package render;

import blocks.Block;
import blocks.BlockRegistry;
import blocks.Blocks;
import world.BlockPos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public final class ParticleSystem {
    private final List<Particle> particles = new ArrayList<>();
    private final Random random = new Random();

    public void spawnBlockBreak(BlockPos pos, int blockId) {
        Block block = BlockRegistry.get(blockId);
        if (block == Blocks.AIR) {
            return;
        }
        String textureName = block.sideTexture();
        if (textureName == null) {
            textureName = block.topTexture();
        }
        if (textureName == null) {
            return;
        }
        int slot = TextureAtlas.getSlot(textureName);

        float cx = pos.x() + 0.5f;
        float cy = pos.y() + 0.5f;
        float cz = pos.z() + 0.5f;

        for (int i = 0; i < 15; i++) {
            float x = pos.x() + random.nextFloat();
            float y = pos.y() + random.nextFloat();
            float z = pos.z() + random.nextFloat();
            float vx = (x - cx) * (1.5f + random.nextFloat() * 2.0f);
            float vy = (y - cy) * (1.5f + random.nextFloat() * 2.0f) + 1.0f;
            float vz = (z - cz) * (1.5f + random.nextFloat() * 2.0f);
            float life = 0.5f + random.nextFloat() * 0.8f;
            float size = 0.08f + random.nextFloat() * 0.10f;
            particles.add(new Particle(x, y, z, vx, vy, vz, life, size, slot));
        }
    }

    public void update(float delta) {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.velocity.y -= 15.0f * delta;
            p.position.x += p.velocity.x * delta;
            p.position.y += p.velocity.y * delta;
            p.position.z += p.velocity.z * delta;
            p.life -= delta;
            if (p.life <= 0) {
                it.remove();
            }
        }
    }

    public List<Particle> getParticles() {
        return particles;
    }

    public boolean isEmpty() {
        return particles.isEmpty();
    }
}
