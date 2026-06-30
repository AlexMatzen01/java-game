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
                if (!graphicsGuiOpen) {
                    player.update(world, input, (float) deltaSeconds);
                    player.handleBlockActions(world, input);
                    if (particleSystem != null) {
                        particleSystem.update((float) deltaSeconds);
                    }
                    world.updateFallingBlocks((float) deltaSeconds);
                }
            }

            if (input.consumeKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
                if (graphicsGuiOpen) {
                    setGraphicsGuiOpen(false);
                } else if (menuOpen) {
                    GLFW.glfwSetWindowShouldClose(window.handle(), true);
                } else {
                    setMenuOpen(true);
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
                    worldNameInput.toString()
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

    private void setGraphicsGuiOpen(boolean open) {
        graphicsGuiOpen = open;
        input.setCursorLocked(!open && !menuOpen);
    }

    private void setMenuOpen(boolean open) {
        menuOpen = open;
        input.setCursorLocked(!open && !graphicsGuiOpen);
    }
}
