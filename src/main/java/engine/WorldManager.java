package engine;

import blocks.Blocks;
import player.Player;
import util.TickSystem;
import world.Chunk;
import world.ChunkPos;
import world.World;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WorldManager {
    private static final int SAVE_MAGIC = 0x57524C44;
    private static final int SAVE_VERSION = 2;
    private static final String SAVE_FILE = "world.bin";

    private WorldManager() {
    }

    public static Path savesDirectory() {
        Path path = Path.of("saves");
        try {
            Files.createDirectories(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to create saves directory", exception);
        }
        return path;
    }

    public static List<WorldSlot> listWorlds() {
        Path savesDir = savesDirectory();
        List<WorldSlot> worlds = new ArrayList<>();
        try (var stream = Files.list(savesDir)) {
            stream.filter(Files::isDirectory)
                    .forEach(dir -> worlds.add(new WorldSlot(dir.getFileName().toString(), dir)));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read saves directory", exception);
        }
        worlds.sort(Comparator.comparing(WorldSlot::name, String.CASE_INSENSITIVE_ORDER));
        return worlds;
    }

    public static WorldSlot createWorld(String worldName) {
        String cleaned = sanitizeName(worldName);
        if (cleaned.isBlank()) {
            throw new IllegalArgumentException("World name cannot be empty");
        }
        Path dir = savesDirectory().resolve(cleaned);
        if (Files.exists(dir)) {
            throw new IllegalArgumentException("A world with that name already exists");
        }
        try {
            Files.createDirectories(dir);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to create world directory", exception);
        }
        return new WorldSlot(cleaned, dir);
    }

    public static LoadedWorld loadWorld(WorldSlot slot) {
        World world = new World();
        Player player = new Player();
        TickSystem ticks = new TickSystem();

        Path savePath = slot.directory().resolve(SAVE_FILE);
        if (!Files.exists(savePath)) {
            world.ensureSpawnArea();
            player.setSpawnPosition(world.findSpawnPosition());
            player.camera().position().set(world.findSpawnPosition());
            return new LoadedWorld(world, player, ticks);
        }

        try (DataInputStream input = new DataInputStream(Files.newInputStream(savePath))) {
            int magic = input.readInt();
            int version = input.readInt();
            if (magic != SAVE_MAGIC || version < 1 || version > SAVE_VERSION) {
                throw new IllegalStateException("Unsupported save format for world: " + slot.name());
            }

            long worldTicks = input.readLong();
            double cycleSeconds = input.readDouble();

            float px = input.readFloat();
            float py = input.readFloat();
            float pz = input.readFloat();
            float yaw = input.readFloat();
            float pitch = input.readFloat();

            int modeOrdinal = Player.GameMode.SURVIVAL.ordinal();
            int selectedBlockId = Blocks.DIRT.id();
            if (version >= 2) {
                modeOrdinal = input.readInt();
                selectedBlockId = input.readInt();
            }

            int chunkCount = input.readInt();
            Map<ChunkPos, short[]> snapshot = new HashMap<>();
            for (int i = 0; i < chunkCount; i++) {
                int cx = input.readInt();
                int cy = input.readInt();
                int cz = input.readInt();
                short[] blocks = new short[Chunk.VOLUME];
                for (int block = 0; block < Chunk.VOLUME; block++) {
                    blocks[block] = input.readShort();
                }
                snapshot.put(new ChunkPos(cx, cy, cz), blocks);
            }

            world.loadSnapshot(snapshot);
            ticks.restoreState(worldTicks, cycleSeconds);
            player.camera().position().set(px, py, pz);
            player.camera().setYaw(yaw);
            player.camera().setPitch(pitch);
            player.setGameMode(Player.GameMode.values()[Math.max(0, Math.min(Player.GameMode.values().length - 1, modeOrdinal))]);
            player.setSelectedPlaceBlockId(selectedBlockId);
            player.setSpawnPosition(world.findSpawnPosition());
            return new LoadedWorld(world, player, ticks);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load world: " + slot.name(), exception);
        }
    }

    public static void saveWorld(WorldSlot slot, World world, Player player, TickSystem ticks) {
        Path worldDir = slot.directory();
        try {
            Files.createDirectories(worldDir);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to create world save directory", exception);
        }

        Path saveFile = worldDir.resolve(SAVE_FILE);
        Path tempFile = worldDir.resolve(SAVE_FILE + ".tmp");

        Map<ChunkPos, short[]> snapshot = world.snapshotChunks();

        try (DataOutputStream output = new DataOutputStream(Files.newOutputStream(tempFile))) {
            output.writeInt(SAVE_MAGIC);
            output.writeInt(SAVE_VERSION);

            output.writeLong(ticks.getWorldTicks());
            output.writeDouble(ticks.getCycleSeconds());

            output.writeFloat(player.camera().position().x);
            output.writeFloat(player.camera().position().y);
            output.writeFloat(player.camera().position().z);
            output.writeFloat(player.camera().yaw());
            output.writeFloat(player.camera().pitch());
            output.writeInt(player.gameMode().ordinal());
            output.writeInt(player.selectedPlaceBlockId());

            output.writeInt(snapshot.size());
            for (Map.Entry<ChunkPos, short[]> entry : snapshot.entrySet()) {
                ChunkPos pos = entry.getKey();
                output.writeInt(pos.x());
                output.writeInt(pos.y());
                output.writeInt(pos.z());
                short[] blocks = entry.getValue();
                for (short block : blocks) {
                    output.writeShort(block);
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to save world: " + slot.name(), exception);
        }

        try {
            Files.move(tempFile, saveFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to finalize world save: " + slot.name(), exception);
        }
    }

    private static String sanitizeName(String name) {
        String cleaned = name == null ? "" : name.trim();
        cleaned = cleaned.replaceAll("[\\\\/:*?\"<>|]", "_");
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        return cleaned;
    }

    public record WorldSlot(String name, Path directory) {
    }

    public record LoadedWorld(World world, Player player, TickSystem ticks) {
    }
}
