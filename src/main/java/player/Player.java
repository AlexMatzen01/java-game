package player;

import input.InputSystem;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import util.MathUtil;
import world.BlockPos;
import world.RaycastHit;
import world.World;

public final class Player {
    private static final float MOVE_SPEED = 6.0f;
    private static final float SPRINT_MULTIPLIER = 1.7f;
    private static final float MOUSE_SENSITIVITY = 0.12f;
    private static final float GRAVITY = 22.0f;
    private static final float JUMP_VELOCITY = 8.5f;
    private static final float WIDTH = 0.6f;
    private static final float HEIGHT = 1.8f;
    private static final float EYE_HEIGHT = 1.62f;
    private static final int DIRT_BLOCK_ID = 1;
    private static final int COBBLESTONE_BLOCK_ID = 2;

    private final Camera camera = new Camera();
    private final Vector3f velocity = new Vector3f();
    private final Vector3f forwardScratch = new Vector3f();
    private final Vector3f rightScratch = new Vector3f();
    private boolean onGround;
    private int placeBlockId = DIRT_BLOCK_ID;
    private final Vector3f spawnPosition = new Vector3f(8.5f, 16.0f, 8.5f);

    public Player() {
        camera.position().set(spawnPosition);
    }

    public void update(World world, InputSystem input, float deltaSeconds) {
        updateCursorMode(input);
        updateBlockSelection(input);
        if (input.isCursorLocked()) {
            updateLook(input);
        }
        updateMovement(world, input, deltaSeconds);
        if (camera.position().y < -32.0f) {
            resetToSpawn();
        }
    }

    public void handleBlockActions(World world, InputSystem input) {
        if (input.consumeMousePressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
            breakBlock(world);
        }
        if (input.consumeMousePressed(GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
            placeBlock(world);
        }
    }

    public Camera camera() {
        return camera;
    }

    public Vector3f eyePosition(Vector3f dest) {
        return dest.set(camera.position()).add(0.0f, EYE_HEIGHT, 0.0f);
    }

    public Matrix4f viewMatrix() {
        Vector3f eye = eyePosition(new Vector3f());
        return new Matrix4f()
                .rotateX((float) Math.toRadians(camera.pitch()))
                .rotateY((float) Math.toRadians(camera.yaw()))
                .translate(-eye.x, -eye.y, -eye.z);
    }

    public Vector3f lookDirection(Vector3f dest) {
        return camera.forward(dest);
    }

    public int selectedPlaceBlockId() {
        return placeBlockId;
    }

    private void updateLook(InputSystem input) {
        float deltaX = (float) input.consumeMouseDeltaX();
        float deltaY = (float) input.consumeMouseDeltaY();
        camera.setYaw(camera.yaw() + deltaX * MOUSE_SENSITIVITY);
        camera.setPitch(MathUtil.clamp(camera.pitch() + deltaY * MOUSE_SENSITIVITY, -89.5f, 89.5f));
    }

    private void updateBlockSelection(InputSystem input) {
        if (input.consumeKeyPressed(GLFW.GLFW_KEY_1)) {
            placeBlockId = DIRT_BLOCK_ID;
        }
        if (input.consumeKeyPressed(GLFW.GLFW_KEY_2)) {
            placeBlockId = COBBLESTONE_BLOCK_ID;
        }
    }

    private void updateCursorMode(InputSystem input) {
        if (input.consumeKeyPressed(GLFW.GLFW_KEY_LEFT_ALT) || input.consumeKeyPressed(GLFW.GLFW_KEY_RIGHT_ALT)) {
            input.toggleCursorLocked();
        }
    }

    private void updateMovement(World world, InputSystem input, float deltaSeconds) {
        forwardScratch.set(camera.forward(forwardScratch)).y = 0.0f;
        if (forwardScratch.lengthSquared() > 0.0f) {
            forwardScratch.normalize();
        }
        rightScratch.set(camera.right(rightScratch));

        float speed = MOVE_SPEED * (input.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) ? SPRINT_MULTIPLIER : 1.0f);
        Vector3f movement = new Vector3f();
        if (input.isKeyDown(GLFW.GLFW_KEY_W)) {
            movement.add(forwardScratch);
        }
        if (input.isKeyDown(GLFW.GLFW_KEY_S)) {
            movement.sub(forwardScratch);
        }
        if (input.isKeyDown(GLFW.GLFW_KEY_D)) {
            movement.add(rightScratch);
        }
        if (input.isKeyDown(GLFW.GLFW_KEY_A)) {
            movement.sub(rightScratch);
        }

        if (movement.lengthSquared() > 0.0f) {
            movement.normalize().mul(speed);
        }

        velocity.x = movement.x;
        velocity.z = movement.z;
        velocity.y -= GRAVITY * deltaSeconds;

        if (input.isKeyDown(GLFW.GLFW_KEY_SPACE) && onGround) {
            velocity.y = JUMP_VELOCITY;
            onGround = false;
        }

        moveAxis(world, deltaSeconds, 0.0f, 0.0f, velocity.x * deltaSeconds, Axis.X);
        moveAxis(world, deltaSeconds, 0.0f, velocity.y * deltaSeconds, 0.0f, Axis.Y);
        moveAxis(world, deltaSeconds, velocity.z * deltaSeconds, 0.0f, 0.0f, Axis.Z);
    }

    private void moveAxis(World world, float deltaSeconds, float moveZ, float moveY, float moveX, Axis axis) {
        Vector3f position = camera.position();
        float nextX = position.x + moveX;
        float nextY = position.y + moveY;
        float nextZ = position.z + moveZ;

        switch (axis) {
            case X -> {
                if (!collides(world, nextX, position.y, position.z)) {
                    position.x = nextX;
                } else {
                    velocity.x = 0.0f;
                }
            }
            case Y -> {
                if (!collides(world, position.x, nextY, position.z)) {
                    position.y = nextY;
                    onGround = false;
                } else {
                    if (velocity.y < 0.0f) {
                        onGround = true;
                    }
                    velocity.y = 0.0f;
                }
            }
            case Z -> {
                if (!collides(world, position.x, position.y, nextZ)) {
                    position.z = nextZ;
                } else {
                    velocity.z = 0.0f;
                }
            }
        }
    }

    private boolean collides(World world, float x, float y, float z) {
        float minX = x - WIDTH * 0.5f;
        float minY = y;
        float minZ = z - WIDTH * 0.5f;
        float maxX = x + WIDTH * 0.5f;
        float maxY = y + HEIGHT;
        float maxZ = z + WIDTH * 0.5f;

        int startX = (int) Math.floor(minX);
        int endX = (int) Math.floor(maxX);
        int startY = (int) Math.floor(minY);
        int endY = (int) Math.floor(maxY);
        int startZ = (int) Math.floor(minZ);
        int endZ = (int) Math.floor(maxZ);

        for (int blockY = startY; blockY <= endY; blockY++) {
            for (int blockZ = startZ; blockZ <= endZ; blockZ++) {
                for (int blockX = startX; blockX <= endX; blockX++) {
                    if (world.isSolid(blockX, blockY, blockZ)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void breakBlock(World world) {
        RaycastHit hit = world.raycast(eyePosition(new Vector3f()), lookDirection(new Vector3f()), 6.0f);
        if (hit != null) {
            world.setBlock(hit.blockPos().x(), hit.blockPos().y(), hit.blockPos().z(), 0);
        }
    }

    private void placeBlock(World world) {
        RaycastHit hit = world.raycast(eyePosition(new Vector3f()), lookDirection(new Vector3f()), 6.0f);
        if (hit != null) {
            BlockPos placePos = hit.adjacentPos();
            if (wouldCollideWithPlayer(placePos.x(), placePos.y(), placePos.z())) {
                return;
            }
            world.setBlock(placePos.x(), placePos.y(), placePos.z(), placeBlockId);
        }
    }

    private boolean wouldCollideWithPlayer(int blockX, int blockY, int blockZ) {
        Vector3f position = camera.position();
        float minX = position.x - WIDTH * 0.5f;
        float minY = position.y;
        float minZ = position.z - WIDTH * 0.5f;
        float maxX = position.x + WIDTH * 0.5f;
        float maxY = position.y + HEIGHT;
        float maxZ = position.z + WIDTH * 0.5f;

        float blockMinX = blockX;
        float blockMinY = blockY;
        float blockMinZ = blockZ;
        float blockMaxX = blockX + 1.0f;
        float blockMaxY = blockY + 1.0f;
        float blockMaxZ = blockZ + 1.0f;

        return minX < blockMaxX && maxX > blockMinX
                && minY < blockMaxY && maxY > blockMinY
                && minZ < blockMaxZ && maxZ > blockMinZ;
    }

    private void resetToSpawn() {
        camera.position().set(spawnPosition);
        velocity.set(0.0f, 0.0f, 0.0f);
        onGround = false;
    }

    private enum Axis {
        X,
        Y,
        Z
    }
}
