package engine;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.system.MemoryUtil;

public final class Window {
    private final int initialWidth;
    private final int initialHeight;
    private final String title;
    private GLFWErrorCallback errorCallback;
    private GLFWFramebufferSizeCallback framebufferCallback;
    private long handle;
    private int width;
    private int height;
    private boolean resized;

    public Window(int initialWidth, int initialHeight, String title) {
        this.initialWidth = initialWidth;
        this.initialHeight = initialHeight;
        this.title = title;
    }

    public void create() {
        errorCallback = GLFWErrorCallback.createPrint(System.err);
        errorCallback.set();
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_OPENGL_API);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);

        handle = GLFW.glfwCreateWindow(initialWidth, initialHeight, title, MemoryUtil.NULL, MemoryUtil.NULL);
        if (handle == MemoryUtil.NULL) {
            throw new IllegalStateException("Failed to create GLFW window");
        }

        width = initialWidth;
        height = initialHeight;
        framebufferCallback = GLFWFramebufferSizeCallback.create((window, newWidth, newHeight) -> {
            width = Math.max(1, newWidth);
            height = Math.max(1, newHeight);
            resized = true;
        });
        GLFW.glfwSetFramebufferSizeCallback(handle, framebufferCallback);
    }

    public long handle() {
        return handle;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public boolean shouldClose() {
        return GLFW.glfwWindowShouldClose(handle);
    }

    public boolean consumeResized() {
        boolean wasResized = resized;
        resized = false;
        return wasResized;
    }

    public void destroy() {
        if (handle != MemoryUtil.NULL) {
            GLFW.glfwDestroyWindow(handle);
            handle = MemoryUtil.NULL;
        }
        if (framebufferCallback != null) {
            framebufferCallback.free();
            framebufferCallback = null;
        }
        if (errorCallback != null) {
            errorCallback.free();
            errorCallback = null;
        }
        GLFW.glfwTerminate();
        GLFW.glfwSetErrorCallback(null);
    }
}
