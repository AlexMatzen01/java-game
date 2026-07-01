package engine;

import input.InputSystem;
import org.lwjgl.glfw.GLFW;
import player.Player;
import render.OpenGlRenderer;
import render.ParticleSystem;
import util.TickSystem;
import world.World;

import java.util.List;
import java.util.concurrent.locks.LockSupport;

public final class Game {
    private final GameOptions options;
    private final GraphicsSettings graphicsSettings = new GraphicsSettings();
    private final Window window = new Window(1280, 720, "game");
    private final InputSystem input = new InputSystem();
    private final TickSystem ticks = new TickSystem();

    private OpenGlRenderer renderer;
    private WorldManager.WorldSlot worldSlot;
    private World world;
    private Player player;
    private ParticleSystem particleSystem;
    private boolean graphicsGuiOpen;
    private boolean menuOpen = true;
    private boolean loadingWorld;
    private boolean namingWorld;
    private final StringBuilder worldNameInput = new StringBuilder();
    private boolean chatOpen;
    private final StringBuilder chatInput = new StringBuilder();
    private final java.util.List<ChatMessage> chatMessages = new java.util.ArrayList<>();
    private List<WorldManager.WorldSlot> worlds = List.of();
    private int selectedWorldIndex;

    public Game() {
        this(GameOptions.defaults());
    }

    public Game(GameOptions options) {
        this.options = options;
    }

    public void run() {
        try {
            window.create();
            input.install(window.handle());
            renderer = new OpenGlRenderer(window, options);
            renderer.init();
            input.setCursorLocked(false);
            refreshWorldList();
            loop();
        } finally {
            if (worldSlot != null && world != null && player != null) {
                try {
                    WorldManager.saveWorld(worldSlot, world, player, ticks);
                } catch (RuntimeException exception) {
                    System.err.println("Failed to save world '" + worldSlot.name() + "': " + exception.getMessage());
                }
            }
            if (renderer != null) {
                renderer.cleanup();
            }
            window.destroy();
        }
    }

    private void loop() {
        double lastTime = GLFW.glfwGetTime();
        double targetFrameSeconds = options.fpsCap() > 0 ? 1.0 / options.fpsCap() : 0.0;
        while (!window.shouldClose()) {
            double frameStart = GLFW.glfwGetTime();
            input.beginFrame();
            GLFW.glfwPollEvents();

            double now = GLFW.glfwGetTime();
            double deltaSeconds = now - lastTime;
            lastTime = now;

            ticks.consumeTicks(deltaSeconds);

            if (menuOpen) {
                handleMenuInput();
            } else {
                world.tick(player.camera().position(), graphicsSettings.renderDistanceChunks());
                handleGraphicsGuiInput();
                if (!graphicsGuiOpen && !chatOpen) {
                    player.update(world, input, (float) deltaSeconds);
                    player.handleBlockActions(world, input);
                    if (particleSystem != null) {
                        particleSystem.update((float) deltaSeconds);
                    }
                    world.updateFallingBlocks((float) deltaSeconds);
                }
            }

            if (input.consumeKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
                if (chatOpen) {
                    chatInput.setLength(0);
                    setChatOpen(false);
                } else if (graphicsGuiOpen) {
                    setGraphicsGuiOpen(false);
                } else if (menuOpen) {
                    GLFW.glfwSetWindowShouldClose(window.handle(), true);
                } else {
                    setMenuOpen(true);
                }
            }

            if (!menuOpen && !graphicsGuiOpen && !chatOpen && input.consumeKeyPressed(GLFW.GLFW_KEY_F5)) {
                setChatOpen(true);
            }

            if (chatOpen) {
                String typed = input.consumeTypedChars();
                for (int i = 0; i < typed.length(); i++) {
                    char c = typed.charAt(i);
                    if (c >= 32 && c <= 126) chatInput.append(c);
                }
                if (input.consumeKeyPressed(GLFW.GLFW_KEY_BACKSPACE) && chatInput.length() > 0) {
                    chatInput.setLength(chatInput.length() - 1);
                }
                if (input.consumeKeyPressed(GLFW.GLFW_KEY_ENTER)) {
                    String msg = chatInput.toString().trim();
                    if (!msg.isEmpty()) {
                        processChatMessage(msg);
                    }
                    chatInput.setLength(0);
                    setChatOpen(false);
                }
            }

            renderer.render(
                    world,
                    player,
                    ticks,
                    window.consumeResized(),
                    graphicsSettings,
                    graphicsGuiOpen,
                    menuOpen,
                    worlds,
                    selectedWorldIndex,
                    loadingWorld,
                    namingWorld,
                    worldNameInput.toString(),
                    chatOpen,
                    chatMessages,
                    chatInput.toString()
            );

            if (targetFrameSeconds > 0.0) {
                double elapsed = GLFW.glfwGetTime() - frameStart;
                double remaining = targetFrameSeconds - elapsed;
                if (remaining > 0.0) {
                    LockSupport.parkNanos((long) (remaining * 1_000_000_000L));
                }
            }
        }
    }

    private void handleMenuInput() {
        if (namingWorld) {
            String chars = input.consumeTypedChars();
            for (int i = 0; i < chars.length(); i++) {
                char c = chars.charAt(i);
                if (c >= 32 && c <= 126) {
                    worldNameInput.append(c);
                }
            }
            if (input.consumeKeyPressed(GLFW.GLFW_KEY_BACKSPACE) && worldNameInput.length() > 0) {
                worldNameInput.setLength(worldNameInput.length() - 1);
            }
            if (input.consumeKeyPressed(GLFW.GLFW_KEY_ENTER)) {
                String name = worldNameInput.toString().trim();
                if (!name.isEmpty()) {
                    namingWorld = false;
                    createAndLoadWorld(name);
                }
            }
            if (input.consumeKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
                namingWorld = false;
                worldNameInput.setLength(0);
            }
            return;
        }

        if (input.consumeKeyPressed(GLFW.GLFW_KEY_UP) && !worlds.isEmpty()) {
            selectedWorldIndex = Math.max(0, selectedWorldIndex - 1);
        }
        if (input.consumeKeyPressed(GLFW.GLFW_KEY_DOWN) && !worlds.isEmpty()) {
            selectedWorldIndex = Math.min(worlds.size() - 1, selectedWorldIndex + 1);
        }
        if (input.consumeKeyPressed(GLFW.GLFW_KEY_F5)) {
            refreshWorldList();
        }
        if (input.consumeKeyPressed(GLFW.GLFW_KEY_G)) {
            setGraphicsGuiOpen(true);
        }
        if (input.consumeKeyPressed(GLFW.GLFW_KEY_N)) {
            namingWorld = true;
            worldNameInput.setLength(0);
        }
        if (input.consumeKeyPressed(GLFW.GLFW_KEY_ENTER)) {
            loadSelectedWorld();
        }
    }

    private void handleGraphicsGuiInput() {
        if (input.consumeKeyPressed(GLFW.GLFW_KEY_F3)) {
            setGraphicsGuiOpen(!graphicsGuiOpen);
        }

        if (!graphicsGuiOpen) {
            return;
        }

        if (input.consumeKeyPressed(GLFW.GLFW_KEY_UP)) {
            graphicsSettings.selectPrevious();
        }
        if (input.consumeKeyPressed(GLFW.GLFW_KEY_DOWN)) {
            graphicsSettings.selectNext();
        }
        if (input.consumeKeyPressed(GLFW.GLFW_KEY_LEFT)) {
            graphicsSettings.adjustSelected(-1.0f);
        }
        if (input.consumeKeyPressed(GLFW.GLFW_KEY_RIGHT) || input.consumeKeyPressed(GLFW.GLFW_KEY_ENTER)) {
            graphicsSettings.adjustSelected(1.0f);
        }
    }

    private void refreshWorldList() {
        worlds = WorldManager.listWorlds();
        if (selectedWorldIndex >= worlds.size()) {
            selectedWorldIndex = Math.max(0, worlds.size() - 1);
        }
    }

    private void loadSelectedWorld() {
        if (worlds.isEmpty()) {
            namingWorld = true;
            worldNameInput.setLength(0);
            return;
        }
        loadWorldSlot(worlds.get(selectedWorldIndex));
    }

    private void createAndLoadWorld(String name) {
        loadWorldSlot(WorldManager.createWorld(name));
    }

    private void loadWorldSlot(WorldManager.WorldSlot slot) {
        loadingWorld = true;
        renderer.renderMenu(graphicsSettings, worlds, selectedWorldIndex, true, false, "");
        WorldManager.LoadedWorld loadedWorld = WorldManager.loadWorld(slot);
        worldSlot = slot;
        world = loadedWorld.world();
        player = loadedWorld.player();
        particleSystem = new ParticleSystem();
        player.setParticleSystem(particleSystem);
        renderer.setParticleSystem(particleSystem);
        ticks.restoreState(loadedWorld.ticks().getWorldTicks(), loadedWorld.ticks().getCycleSeconds());
        player.setSpawnPosition(world.findSpawnPosition());
        player.camera().position().set(world.findSpawnPosition());
        menuOpen = false;
        loadingWorld = false;
        setGraphicsGuiOpen(false);
        input.setCursorLocked(true);
    }

    private void processChatMessage(String msg) {
        chatMessages.add(new ChatMessage(msg));
        if (chatMessages.size() > 50) {
            chatMessages.remove(0);
        }
        if (msg.startsWith("/tp ")) {
            String[] parts = msg.substring(4).trim().split("\\s+");
            if (parts.length >= 3) {
                try {
                    float x = Float.parseFloat(parts[0]);
                    float y = Float.parseFloat(parts[1]);
                    float z = Float.parseFloat(parts[2]);
                    player.teleport(x, y, z);
                } catch (NumberFormatException ignored) {
                }
            }
        } else if (msg.startsWith("/time ")) {
            String arg = msg.substring(6).trim().toLowerCase();
            double cs = 1440.0;
            double target;
            switch (arg) {
                case "noon" -> target = cs * 0.0;
                case "sunset" -> target = cs * 0.25;
                case "midnight" -> target = cs * 0.5;
                case "sunrise" -> target = cs * 0.75;
                case "day" -> target = 0.0;
                case "night" -> target = cs * 0.5;
                default -> {
                    try {
                        int h = Integer.parseInt(arg);
                        if (h < 0 || h > 23) return;
                        double hoursProgress = ((h - 12.0 + 24.0) % 24.0) / 24.0;
                        target = cs * hoursProgress;
                    } catch (NumberFormatException ignored) {
                        return;
                    }
                }
            }
            ticks.setCycleSeconds(target);
            double clockHours = (target / cs * 24.0 + 12.0) % 24.0;
            int hours = (int) clockHours;
            int minutes = (int) ((clockHours - hours) * 60.0);
            String timeStr = String.format("%02d:%02d", hours, minutes);
            chatMessages.add(new ChatMessage("Time set to " + timeStr));
            if (chatMessages.size() > 50) {
                chatMessages.remove(0);
            }
        }
    }

    private void setGraphicsGuiOpen(boolean open) {
        graphicsGuiOpen = open;
        input.setCursorLocked(!open && !menuOpen && !chatOpen);
    }

    private void setMenuOpen(boolean open) {
        menuOpen = open;
        input.setCursorLocked(!open && !graphicsGuiOpen && !chatOpen);
    }

    private void setChatOpen(boolean open) {
        chatOpen = open;
        input.setCursorLocked(!open && !graphicsGuiOpen && !menuOpen);
    }
}
