package input;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

public final class InputSystem {
    private final boolean[] keys = new boolean[GLFW.GLFW_KEY_LAST + 1];
    private final boolean[] mouseButtons = new boolean[GLFW.GLFW_MOUSE_BUTTON_LAST + 1];
    private final boolean[] keyPressed = new boolean[GLFW.GLFW_KEY_LAST + 1];
    private final boolean[] mousePressed = new boolean[GLFW.GLFW_MOUSE_BUTTON_LAST + 1];
    private final StringBuilder typedChars = new StringBuilder();
    private double mouseDeltaX;
    private double mouseDeltaY;
    private double lastMouseX;
    private double lastMouseY;
    private boolean firstMouse = true;

    private long windowHandle;
    private boolean cursorLocked = true;
    public void install(long windowHandle) {
        GLFW.glfwSetInputMode(windowHandle, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
        this.windowHandle = windowHandle;
        setCursorLocked(true);

        GLFWKeyCallback keyCallback = GLFWKeyCallback.create((window, key, scancode, action, mods) -> {
            if (key < 0 || key >= keys.length) {
                return;
            }
            if (action == GLFW.GLFW_PRESS) {
                keys[key] = true;
                keyPressed[key] = true;
            } else if (action == GLFW.GLFW_RELEASE) {
                keys[key] = false;
            }
        });

        GLFWMouseButtonCallback mouseButtonCallback = GLFWMouseButtonCallback.create((window, button, action, mods) -> {
            if (button < 0 || button >= mouseButtons.length) {
                return;
            }
            if (action == GLFW.GLFW_PRESS) {
                mouseButtons[button] = true;
                mousePressed[button] = true;
            } else if (action == GLFW.GLFW_RELEASE) {
                mouseButtons[button] = false;
            }
        });

        GLFWCursorPosCallback cursorPosCallback = GLFWCursorPosCallback.create((window, xPos, yPos) -> {
            if (firstMouse) {
                lastMouseX = xPos;
                lastMouseY = yPos;
                firstMouse = false;
            }
            mouseDeltaX += xPos - lastMouseX;
            mouseDeltaY += yPos - lastMouseY;
            lastMouseX = xPos;
            lastMouseY = yPos;
        });

        GLFWCharCallback charCallback = GLFWCharCallback.create((window, codepoint) -> {
            typedChars.append((char) codepoint);
        });

        GLFW.glfwSetKeyCallback(windowHandle, keyCallback);
        GLFW.glfwSetMouseButtonCallback(windowHandle, mouseButtonCallback);
        GLFW.glfwSetCursorPosCallback(windowHandle, cursorPosCallback);
        GLFW.glfwSetCharCallback(windowHandle, charCallback);
    }

    public void setCursorLocked(boolean locked) {
        cursorLocked = locked;
        if (windowHandle != 0L) {
            GLFW.glfwSetInputMode(windowHandle, GLFW.GLFW_CURSOR, locked ? GLFW.GLFW_CURSOR_DISABLED : GLFW.GLFW_CURSOR_NORMAL);
        }
        firstMouse = true;
        mouseDeltaX = 0.0;
        mouseDeltaY = 0.0;
    }

    public void toggleCursorLocked() {
        setCursorLocked(!cursorLocked);
    }

    public boolean isCursorLocked() {
        return cursorLocked;
    }
    public void beginFrame() {
        mouseDeltaX = 0.0;
        mouseDeltaY = 0.0;
    }

    public boolean isKeyDown(int key) {
        return key >= 0 && key < keys.length && keys[key];
    }

    public boolean isMouseDown(int button) {
        return button >= 0 && button < mouseButtons.length && mouseButtons[button];
    }

    public boolean consumeKeyPressed(int key) {
        if (key < 0 || key >= keyPressed.length || !keyPressed[key]) {
            return false;
        }
        keyPressed[key] = false;
        return true;
    }

    public boolean consumeMousePressed(int button) {
        if (button < 0 || button >= mousePressed.length || !mousePressed[button]) {
            return false;
        }
        mousePressed[button] = false;
        return true;
    }

    public double consumeMouseDeltaX() {
        double delta = mouseDeltaX;
        mouseDeltaX = 0.0;
        return delta;
    }

    public double consumeMouseDeltaY() {
        double delta = mouseDeltaY;
        mouseDeltaY = 0.0;
        return delta;
    }

    public String consumeTypedChars() {
        if (typedChars.isEmpty()) {
            return "";
        }
        String chars = typedChars.toString();
        typedChars.setLength(0);
        return chars;
    }
}
