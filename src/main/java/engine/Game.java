package engine;

import input.InputSystem;
import org.lwjgl.glfw.GLFW;
import render.OpenGlRenderer;
import util.TickSystem;
import world.World;
import player.Player;

import java.util.concurrent.locks.LockSupport;

public final class Game {
    private final GameOptions options;
    private final WorldManager.WorldSlot worldSlot;
    private final Window window = new Window(1280, 720, "game");
    private final InputSystem input = new InputSystem();
    private final TickSystem ticks;
    private final World world;
    private final Player player;
    private OpenGlRenderer renderer;

    public Game() {
        this(GameOptions.defaults(), new World(), new Player(), new TickSystem(), null);
    }

    public Game(GameOptions options) {
        this(options, new World(), new Player(), new TickSystem(), null);
    }

    public Game(
            GameOptions options,
            World world,
            Player player,
            TickSystem ticks,
            WorldManager.WorldSlot worldSlot
    ) {
        this.options = options;
        this.world = world;
        this.player = player;
        this.ticks = ticks;
        this.worldSlot = worldSlot;
    }

    public void run() {
        try {
            window.create();
            input.install(window.handle());
            renderer = new OpenGlRenderer(window, options);
            renderer.init();
            input.setCursorLocked(true);
            loop();
        } finally {
            if (worldSlot != null) {
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
            world.tick(player.camera().position());
            player.update(world, input, (float) deltaSeconds);
            player.handleBlockActions(world, input);
            if (input.consumeKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
                GLFW.glfwSetWindowShouldClose(window.handle(), true);
            }

            renderer.render(world, player, ticks, window.consumeResized());

            if (targetFrameSeconds > 0.0) {
                double elapsed = GLFW.glfwGetTime() - frameStart;
                double remaining = targetFrameSeconds - elapsed;
                if (remaining > 0.0) {
                    LockSupport.parkNanos((long) (remaining * 1_000_000_000L));
                }
            }
        }
    }
}
