package render;

import engine.GameOptions;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import player.Player;
import util.TickSystem;
import world.Chunk;
import world.ChunkMeshBuilder;
import world.ChunkMeshData;
import world.ChunkPos;
import world.RaycastHit;
import world.World;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL33C.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public final class OpenGlRenderer {
    private static final float FIELD_OF_VIEW_DEGREES = 70.0f;
    private static final float NEAR_PLANE = 0.1f;
    private static final float FAR_PLANE = 256.0f;
    private static final int SHADOW_MAP_SIZE = 4096;
    private static final int CASCADE_COUNT = 4;
    private static final float[] CASCADE_SPLITS = {18.0f, 48.0f, 112.0f, 220.0f};
    private static final int PROBE_GRID_X = 20;
    private static final int PROBE_GRID_Y = 10;
    private static final int PROBE_GRID_Z = 20;
    private static final float PROBE_CELL_SIZE = 8.0f;
    private static final int PROBE_UPDATES_PER_FRAME = 1200;

    private final engine.Window window;
    private final GameOptions options;
    private final Map<ChunkPos, Mesh> meshes = new HashMap<>();

    private TextureAtlas atlas;
    private int atlasTexture;
    private int sunTexture;
    private int moonTexture;

    private int worldProgram;
    private int worldMvpLocation;
        private final int[] worldLightMvpLocations = new int[CASCADE_COUNT];
        private int worldCascadeSplitsLocation;
    private int worldAtlasLocation;
    private int worldShadowMapLocation;
    private int worldLightDirLocation;
    private int worldLightColorLocation;
    private int worldLightIntensityLocation;
        private int worldShadowTexelSizeLocation;
        private int worldCameraPosLocation;
        private int worldProbeVolumeLocation;
        private int worldProbeOriginLocation;
        private int worldProbeExtentLocation;

    private int shadowProgram;
    private int shadowMvpLocation;
    private int shadowFbo;
    private int shadowDepthTexture;
            private final Matrix4f[] cascadeLightMvp = createCascadeMatrices();

    private int skyProgram;
    private int skyVao;
    private int skyVbo;
    private int skyTopColorLocation;
    private int skyBottomColorLocation;
    private int skySunDirLocation;
    private int skySunColorLocation;
    private int skyMoonDirLocation;
    private int skyMoonColorLocation;
    private int skySunTextureLocation;
    private int skyMoonTextureLocation;
    private int skyViewRightLocation;
    private int skyViewUpLocation;
    private int skyViewForwardLocation;

    private int outlineProgram;
    private int outlineVao;
    private int outlineVbo;
    private int outlineMvpLocation;

    private int hudProgram;
    private int hudVao;
    private int hudVbo;
    private int hudScreenSizeLocation;

        private int probeTexture;
        private final float[] probeData = new float[PROBE_GRID_X * PROBE_GRID_Y * PROBE_GRID_Z * 3];
        private final Vector3f probeOrigin = new Vector3f();
        private final Vector3f probeExtent = new Vector3f(
            PROBE_CELL_SIZE * (PROBE_GRID_X - 1),
            PROBE_CELL_SIZE * (PROBE_GRID_Y - 1),
            PROBE_CELL_SIZE * (PROBE_GRID_Z - 1)
        );
        private int probeUpdateCursor;

    private double fpsSampleTime = GLFW.glfwGetTime();
    private int fpsSampleFrames;
    private int displayedFps;

    public OpenGlRenderer(engine.Window window, GameOptions options) {
        this.window = window;
        this.options = options;
    }

    public void init() {
        GLFW.glfwMakeContextCurrent(window.handle());
        GLFW.glfwSwapInterval(1);
        GL.createCapabilities();

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GLFW.glfwSwapInterval(options.vSync() ? 1 : 0);

        atlas = TextureAtlas.load();
        atlasTexture = createAtlasTexture(atlas);
        sunTexture = createRgbaTexture("assets/textures/sun.png", "sun");
        moonTexture = createRgbaTexture("assets/textures/moon.png", "moon");

        shadowProgram = createShadowProgram();
        shadowMvpLocation = glGetUniformLocation(shadowProgram, "uLightMvp");
        createShadowResources();

        worldProgram = createWorldProgram();
        worldMvpLocation = glGetUniformLocation(worldProgram, "uMvp");
        for (int i = 0; i < CASCADE_COUNT; i++) {
            worldLightMvpLocations[i] = glGetUniformLocation(worldProgram, "uLightMvp[" + i + "]");
        }
        worldCascadeSplitsLocation = glGetUniformLocation(worldProgram, "uCascadeSplits");
        worldAtlasLocation = glGetUniformLocation(worldProgram, "uAtlas");
        worldShadowMapLocation = glGetUniformLocation(worldProgram, "uShadowMap");
        worldLightDirLocation = glGetUniformLocation(worldProgram, "uLightDir");
        worldLightColorLocation = glGetUniformLocation(worldProgram, "uLightColor");
        worldLightIntensityLocation = glGetUniformLocation(worldProgram, "uLightIntensity");
        worldShadowTexelSizeLocation = glGetUniformLocation(worldProgram, "uShadowTexelSize");
        worldCameraPosLocation = glGetUniformLocation(worldProgram, "uCameraPos");
        worldProbeVolumeLocation = glGetUniformLocation(worldProgram, "uProbeVolume");
        worldProbeOriginLocation = glGetUniformLocation(worldProgram, "uProbeOrigin");
        worldProbeExtentLocation = glGetUniformLocation(worldProgram, "uProbeExtent");

        skyProgram = createSkyProgram();
        skyTopColorLocation = glGetUniformLocation(skyProgram, "uTopColor");
        skyBottomColorLocation = glGetUniformLocation(skyProgram, "uBottomColor");
        skySunDirLocation = glGetUniformLocation(skyProgram, "uSunDir");
        skySunColorLocation = glGetUniformLocation(skyProgram, "uSunColor");
        skyMoonDirLocation = glGetUniformLocation(skyProgram, "uMoonDir");
        skyMoonColorLocation = glGetUniformLocation(skyProgram, "uMoonColor");
        skySunTextureLocation = glGetUniformLocation(skyProgram, "uSunTex");
        skyMoonTextureLocation = glGetUniformLocation(skyProgram, "uMoonTex");
        skyViewRightLocation = glGetUniformLocation(skyProgram, "uViewRight");
        skyViewUpLocation = glGetUniformLocation(skyProgram, "uViewUp");
        skyViewForwardLocation = glGetUniformLocation(skyProgram, "uViewForward");
        createSkyGeometry();

        outlineProgram = createOutlineProgram();
        outlineMvpLocation = glGetUniformLocation(outlineProgram, "uMvp");
        createOutlineGeometry();

        hudProgram = createHudProgram();
        hudScreenSizeLocation = glGetUniformLocation(hudProgram, "uScreenSize");
        createHudGeometry();
        createProbeResources();

        glViewport(0, 0, window.width(), window.height());
    }

    public void render(World world, Player player, TickSystem ticks, boolean resized) {
        if (resized) {
            glViewport(0, 0, window.width(), window.height());
        }

        syncChunkMeshes(world);

        Vector3f sunDirection = sunDirection(ticks);
        Vector3f moonDirection = moonDirection(sunDirection);
        float sunStrength = sunStrength(sunDirection);
        float moonStrength = moonStrength(sunDirection);
        Vector3f activeLightDirection = sunStrength >= moonStrength ? sunDirection : moonDirection;

        buildCascadeLightMvps(player, activeLightDirection);
        updateProbeVolume(world, player, ticks, sunDirection, moonDirection);

        renderShadowMaps();

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, window.width(), window.height());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderSky(player, ticks, sunDirection, moonDirection);
        renderWorld(player, sunDirection, moonDirection);
        renderTargetOutline(world, player);
        renderHotbar(player);
        renderCrosshair();
        renderFpsCounter();

        GLFW.glfwSwapBuffers(window.handle());
    }

    public void cleanup() {
        for (Mesh mesh : meshes.values()) {
            mesh.destroy();
        }
        meshes.clear();

        if (shadowDepthTexture != 0) {
            glDeleteTextures(shadowDepthTexture);
            shadowDepthTexture = 0;
        }
        if (probeTexture != 0) {
            glDeleteTextures(probeTexture);
            probeTexture = 0;
        }
        if (shadowFbo != 0) {
            glDeleteFramebuffers(shadowFbo);
            shadowFbo = 0;
        }

        if (skyVbo != 0) {
            glDeleteBuffers(skyVbo);
            skyVbo = 0;
        }
        if (skyVao != 0) {
            glDeleteVertexArrays(skyVao);
            skyVao = 0;
        }

        if (outlineVbo != 0) {
            glDeleteBuffers(outlineVbo);
            outlineVbo = 0;
        }
        if (outlineVao != 0) {
            glDeleteVertexArrays(outlineVao);
            outlineVao = 0;
        }

        if (hudVbo != 0) {
            glDeleteBuffers(hudVbo);
            hudVbo = 0;
        }
        if (hudVao != 0) {
            glDeleteVertexArrays(hudVao);
            hudVao = 0;
        }

        if (worldProgram != 0) {
            glDeleteProgram(worldProgram);
            worldProgram = 0;
        }
        if (shadowProgram != 0) {
            glDeleteProgram(shadowProgram);
            shadowProgram = 0;
        }
        if (skyProgram != 0) {
            glDeleteProgram(skyProgram);
            skyProgram = 0;
        }
        if (outlineProgram != 0) {
            glDeleteProgram(outlineProgram);
            outlineProgram = 0;
        }
        if (hudProgram != 0) {
            glDeleteProgram(hudProgram);
            hudProgram = 0;
        }

        if (atlasTexture != 0) {
            glDeleteTextures(atlasTexture);
            atlasTexture = 0;
        }
        if (sunTexture != 0) {
            glDeleteTextures(sunTexture);
            sunTexture = 0;
        }
        if (moonTexture != 0) {
            glDeleteTextures(moonTexture);
            moonTexture = 0;
        }
        if (atlas != null) {
            atlas.free();
            atlas = null;
        }

        GLFW.glfwMakeContextCurrent(NULL);
    }

    private void renderShadowMaps() {
        glBindFramebuffer(GL_FRAMEBUFFER, shadowFbo);
        glViewport(0, 0, SHADOW_MAP_SIZE, SHADOW_MAP_SIZE);
        glUseProgram(shadowProgram);
        glEnable(GL_POLYGON_OFFSET_FILL);
        glPolygonOffset(1.2f, 2.0f);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer matrixBuffer = stack.mallocFloat(16);
            for (int cascade = 0; cascade < CASCADE_COUNT; cascade++) {
                glFramebufferTextureLayer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, shadowDepthTexture, 0, cascade);
                glClear(GL_DEPTH_BUFFER_BIT);
                cascadeLightMvp[cascade].get(matrixBuffer.clear());
                glUniformMatrix4fv(shadowMvpLocation, false, matrixBuffer);

                for (Mesh mesh : meshes.values()) {
                    if (mesh.indexCount <= 0) {
                        continue;
                    }
                    glBindVertexArray(mesh.vao);
                    glDrawElements(GL_TRIANGLES, mesh.indexCount, GL_UNSIGNED_INT, 0L);
                }
            }
        }

        glBindVertexArray(0);
        glDisable(GL_POLYGON_OFFSET_FILL);
    }

    private void renderWorld(Player player, Vector3f sunDirection, Vector3f moonDirection) {
        float aspect = (float) window.width() / (float) window.height();
        Matrix4f mvp = new Matrix4f()
                .perspective((float) Math.toRadians(FIELD_OF_VIEW_DEGREES), aspect, NEAR_PLANE, FAR_PLANE)
                .mul(player.viewMatrix());
        Vector3f cameraPos = player.eyePosition(new Vector3f());
        float sunStrength = sunStrength(sunDirection);
        float moonStrength = moonStrength(sunDirection);
        Vector3f activeLightDirection = sunStrength >= moonStrength ? sunDirection : moonDirection;

        float sunBlend = sunStrength / Math.max(sunStrength + moonStrength, 0.0001f);
        float moonBlend = 1.0f - sunBlend;
        float lightIntensity = sunStrength * 1.0f + moonStrength * 0.28f;
        float lightR = 1.00f * sunBlend + 0.64f * moonBlend;
        float lightG = 0.97f * sunBlend + 0.72f * moonBlend;
        float lightB = 0.90f * sunBlend + 1.00f * moonBlend;

        glUseProgram(worldProgram);
        glUniform1i(worldAtlasLocation, 0);
        glUniform1i(worldShadowMapLocation, 1);
        glUniform1i(worldProbeVolumeLocation, 2);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, atlasTexture);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D_ARRAY, shadowDepthTexture);
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_3D, probeTexture);

        glUniform3f(worldLightDirLocation, activeLightDirection.x, activeLightDirection.y, activeLightDirection.z);
        glUniform3f(worldLightColorLocation, lightR, lightG, lightB);
        glUniform1f(worldLightIntensityLocation, lightIntensity);
        glUniform1fv(worldCascadeSplitsLocation, CASCADE_SPLITS);
        glUniform1f(worldShadowTexelSizeLocation, 1.0f / SHADOW_MAP_SIZE);
        glUniform3f(worldCameraPosLocation, cameraPos.x, cameraPos.y, cameraPos.z);
        glUniform3f(worldProbeOriginLocation, probeOrigin.x, probeOrigin.y, probeOrigin.z);
        glUniform3f(worldProbeExtentLocation, probeExtent.x, probeExtent.y, probeExtent.z);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer mvpBuffer = stack.mallocFloat(16);
            mvp.get(mvpBuffer);
            glUniformMatrix4fv(worldMvpLocation, false, mvpBuffer);

            FloatBuffer lightBuffer = stack.mallocFloat(16);
            for (int i = 0; i < CASCADE_COUNT; i++) {
                cascadeLightMvp[i].get(lightBuffer.clear());
                glUniformMatrix4fv(worldLightMvpLocations[i], false, lightBuffer);
            }
        }

        for (Mesh mesh : meshes.values()) {
            if (mesh.indexCount <= 0) {
                continue;
            }
            glBindVertexArray(mesh.vao);
            glDrawElements(GL_TRIANGLES, mesh.indexCount, GL_UNSIGNED_INT, 0L);
        }
        glBindVertexArray(0);

        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_3D, 0);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D_ARRAY, 0);
        glActiveTexture(GL_TEXTURE0);
    }

    private void renderSky(Player player, TickSystem ticks, Vector3f sunDirection, Vector3f moonDirection) {
        float brightness = ticks.getSkyBrightness();
        Vector3f forward = player.lookDirection(new Vector3f());
        Vector3f right = player.camera().right(new Vector3f());
        Vector3f up = new Vector3f(right).cross(forward).normalize();

        float topR = 0.02f + brightness * 0.20f;
        float topG = 0.03f + brightness * 0.32f;
        float topB = 0.07f + brightness * 0.62f;

        float bottomR = 0.04f + brightness * 0.52f;
        float bottomG = 0.06f + brightness * 0.48f;
        float bottomB = 0.11f + brightness * 0.38f;

        float sunIntensity = sunStrength(sunDirection) * 1.4f;
        float moonIntensity = moonStrength(sunDirection) * 0.80f;

        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        glUseProgram(skyProgram);
        glUniform1i(skySunTextureLocation, 3);
        glUniform1i(skyMoonTextureLocation, 4);
        glUniform3f(skyTopColorLocation, topR, topG, topB);
        glUniform3f(skyBottomColorLocation, bottomR, bottomG, bottomB);
        glUniform3f(skySunDirLocation, sunDirection.x, sunDirection.y, sunDirection.z);
        glUniform3f(skySunColorLocation, 1.0f * sunIntensity, 0.90f * sunIntensity, 0.18f * sunIntensity);
        glUniform3f(skyMoonDirLocation, moonDirection.x, moonDirection.y, moonDirection.z);
        glUniform3f(skyMoonColorLocation, 0.62f * moonIntensity, 0.68f * moonIntensity, 0.86f * moonIntensity);
        glUniform3f(skyViewRightLocation, right.x, right.y, right.z);
        glUniform3f(skyViewUpLocation, up.x, up.y, up.z);
        glUniform3f(skyViewForwardLocation, forward.x, forward.y, forward.z);

        glActiveTexture(GL_TEXTURE3);
        glBindTexture(GL_TEXTURE_2D, sunTexture);
        glActiveTexture(GL_TEXTURE4);
        glBindTexture(GL_TEXTURE_2D, moonTexture);

        glBindVertexArray(skyVao);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);

        glActiveTexture(GL_TEXTURE4);
        glBindTexture(GL_TEXTURE_2D, 0);
        glActiveTexture(GL_TEXTURE3);
        glBindTexture(GL_TEXTURE_2D, 0);
        glActiveTexture(GL_TEXTURE0);

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
    }

    private void renderTargetOutline(World world, Player player) {
        RaycastHit hit = world.raycast(player.eyePosition(new Vector3f()), player.lookDirection(new Vector3f()), 6.0f);
        if (hit == null) {
            return;
        }

        int x = hit.blockPos().x();
        int y = hit.blockPos().y();
        int z = hit.blockPos().z();

        float minX = x - 0.0025f;
        float minY = y - 0.0025f;
        float minZ = z - 0.0025f;
        float maxX = x + 1.0025f;
        float maxY = y + 1.0025f;
        float maxZ = z + 1.0025f;

        float[] vertices = {
                minX, minY, minZ, 0.0f, 0.0f, 0.0f, 1.0f,
                maxX, minY, minZ, 0.0f, 0.0f, 0.0f, 1.0f,
                maxX, minY, minZ, 0.0f, 0.0f, 0.0f, 1.0f,
                maxX, minY, maxZ, 0.0f, 0.0f, 0.0f, 1.0f,
                maxX, minY, maxZ, 0.0f, 0.0f, 0.0f, 1.0f,
                minX, minY, maxZ, 0.0f, 0.0f, 0.0f, 1.0f,
                minX, minY, maxZ, 0.0f, 0.0f, 0.0f, 1.0f,
                minX, minY, minZ, 0.0f, 0.0f, 0.0f, 1.0f,

                minX, maxY, minZ, 0.0f, 0.0f, 0.0f, 1.0f,
                maxX, maxY, minZ, 0.0f, 0.0f, 0.0f, 1.0f,
                maxX, maxY, minZ, 0.0f, 0.0f, 0.0f, 1.0f,
                maxX, maxY, maxZ, 0.0f, 0.0f, 0.0f, 1.0f,
                maxX, maxY, maxZ, 0.0f, 0.0f, 0.0f, 1.0f,
                minX, maxY, maxZ, 0.0f, 0.0f, 0.0f, 1.0f,
                minX, maxY, maxZ, 0.0f, 0.0f, 0.0f, 1.0f,
                minX, maxY, minZ, 0.0f, 0.0f, 0.0f, 1.0f,

                minX, minY, minZ, 0.0f, 0.0f, 0.0f, 1.0f,
                minX, maxY, minZ, 0.0f, 0.0f, 0.0f, 1.0f,
                maxX, minY, minZ, 0.0f, 0.0f, 0.0f, 1.0f,
                maxX, maxY, minZ, 0.0f, 0.0f, 0.0f, 1.0f,

                minX, minY, maxZ, 0.0f, 0.0f, 0.0f, 1.0f,
                minX, maxY, maxZ, 0.0f, 0.0f, 0.0f, 1.0f,
                maxX, minY, maxZ, 0.0f, 0.0f, 0.0f, 1.0f,
                maxX, maxY, maxZ, 0.0f, 0.0f, 0.0f, 1.0f
        };

        glEnable(GL_DEPTH_TEST);
        glDepthMask(false);
        glUseProgram(outlineProgram);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer matrixBuffer = stack.mallocFloat(16);
            worldViewProjection(player).get(matrixBuffer);
            glUniformMatrix4fv(outlineMvpLocation, false, matrixBuffer);
        }

        glBindVertexArray(outlineVao);
        glBindBuffer(GL_ARRAY_BUFFER, outlineVbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_DYNAMIC_DRAW);
        glDrawArrays(GL_LINES, 0, vertices.length / 7);
        glBindVertexArray(0);
        glDepthMask(true);
    }

    private void renderCrosshair() {
        float centerX = window.width() * 0.5f;
        float centerY = window.height() * 0.5f;
        float halfLength = 7.0f;
        float thickness = 2.0f;
        float gap = 2.0f;

        float[] vertices = {
                centerX - halfLength - gap, centerY - thickness * 0.5f, 1.0f, 1.0f, 1.0f, 1.0f,
                centerX - gap, centerY - thickness * 0.5f, 1.0f, 1.0f, 1.0f, 1.0f,
                centerX - halfLength - gap, centerY + thickness * 0.5f, 1.0f, 1.0f, 1.0f, 1.0f,
                centerX - gap, centerY - thickness * 0.5f, 1.0f, 1.0f, 1.0f, 1.0f,
                centerX - gap, centerY + thickness * 0.5f, 1.0f, 1.0f, 1.0f, 1.0f,
                centerX - halfLength - gap, centerY + thickness * 0.5f, 1.0f, 1.0f, 1.0f, 1.0f,

                centerX + gap, centerY - thickness * 0.5f, 1.0f, 1.0f, 1.0f, 1.0f,
                centerX + halfLength + gap, centerY - thickness * 0.5f, 1.0f, 1.0f, 1.0f, 1.0f,
                centerX + gap, centerY + thickness * 0.5f, 1.0f, 1.0f, 1.0f, 1.0f,
                centerX + halfLength + gap, centerY - thickness * 0.5f, 1.0f, 1.0f, 1.0f, 1.0f,
                centerX + halfLength + gap, centerY + thickness * 0.5f, 1.0f, 1.0f, 1.0f, 1.0f,
                centerX + gap, centerY + thickness * 0.5f, 1.0f, 1.0f, 1.0f, 1.0f,

                centerX - thickness * 0.5f, centerY - halfLength - gap, 1.0f, 1.0f, 1.0f, 1.0f,
                centerX + thickness * 0.5f, centerY - halfLength - gap, 1.0f, 1.0f, 1.0f, 1.0f,
                centerX - thickness * 0.5f, centerY - gap, 1.0f, 1.0f, 1.0f, 1.0f,
                centerX + thickness * 0.5f, centerY - halfLength - gap, 1.0f, 1.0f, 1.0f, 1.0f,
                centerX + thickness * 0.5f, centerY - gap, 1.0f, 1.0f, 1.0f, 1.0f,
                centerX - thickness * 0.5f, centerY - gap, 1.0f, 1.0f, 1.0f, 1.0f,

                centerX - thickness * 0.5f, centerY + gap, 1.0f, 1.0f, 1.0f, 1.0f,
                centerX + thickness * 0.5f, centerY + gap, 1.0f, 1.0f, 1.0f, 1.0f,
                centerX - thickness * 0.5f, centerY + halfLength + gap, 1.0f, 1.0f, 1.0f, 1.0f,
                centerX + thickness * 0.5f, centerY + gap, 1.0f, 1.0f, 1.0f, 1.0f,
                centerX + thickness * 0.5f, centerY + halfLength + gap, 1.0f, 1.0f, 1.0f, 1.0f,
                centerX - thickness * 0.5f, centerY + halfLength + gap, 1.0f, 1.0f, 1.0f, 1.0f
        };

        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        glUseProgram(hudProgram);
        glUniform2f(hudScreenSizeLocation, window.width(), window.height());
        glBindVertexArray(hudVao);
        glBindBuffer(GL_ARRAY_BUFFER, hudVbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_DYNAMIC_DRAW);
        glDrawArrays(GL_TRIANGLES, 0, 24);
        glBindVertexArray(0);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
    }

    private void renderHotbar(Player player) {
        final int dirtId = 1;
        final int cobblestoneId = 2;

        float slotSize = 54.0f;
        float slotGap = 12.0f;
        float barPadding = 10.0f;
        float frameThickness = 3.0f;
        float marginBottom = 28.0f;
        float swatchInset = 8.0f;

        float barWidth = slotSize * 2.0f + slotGap + barPadding * 2.0f;
        float barHeight = slotSize + barPadding * 2.0f;
        float barX = (window.width() - barWidth) * 0.5f;
        float barY = window.height() - marginBottom - barHeight;

        float slot1X = barX + barPadding;
        float slot2X = slot1X + slotSize + slotGap;
        float slotY = barY + barPadding;

        int selectedId = player.selectedPlaceBlockId();

        FloatArrayBuilder vertices = new FloatArrayBuilder(1024);

        addRect(vertices, barX, barY, barWidth, barHeight, 0.04f, 0.04f, 0.05f, 0.70f);
        addRect(vertices, barX, barY, barWidth, frameThickness, 0.16f, 0.16f, 0.18f, 0.90f);
        addRect(vertices, barX, barY + barHeight - frameThickness, barWidth, frameThickness, 0.16f, 0.16f, 0.18f, 0.90f);
        addRect(vertices, barX, barY, frameThickness, barHeight, 0.16f, 0.16f, 0.18f, 0.90f);
        addRect(vertices, barX + barWidth - frameThickness, barY, frameThickness, barHeight, 0.16f, 0.16f, 0.18f, 0.90f);

        addRect(vertices, slot1X, slotY, slotSize, slotSize, 0.10f, 0.10f, 0.11f, 0.88f);
        addRect(vertices, slot2X, slotY, slotSize, slotSize, 0.10f, 0.10f, 0.11f, 0.88f);

        addRect(vertices,
                slot1X + swatchInset,
                slotY + swatchInset,
                slotSize - swatchInset * 2.0f,
                slotSize - swatchInset * 2.0f,
                0.56f, 0.42f, 0.30f, 1.0f);
        addRect(vertices,
                slot2X + swatchInset,
                slotY + swatchInset,
                slotSize - swatchInset * 2.0f,
                slotSize - swatchInset * 2.0f,
                0.44f, 0.45f, 0.47f, 1.0f);

        float selectedX = selectedId == cobblestoneId ? slot2X : slot1X;
        addRect(vertices, selectedX - 2.0f, slotY - 2.0f, slotSize + 4.0f, 3.0f, 0.95f, 0.88f, 0.52f, 0.98f);
        addRect(vertices, selectedX - 2.0f, slotY + slotSize - 1.0f, slotSize + 4.0f, 3.0f, 0.95f, 0.88f, 0.52f, 0.98f);
        addRect(vertices, selectedX - 2.0f, slotY - 2.0f, 3.0f, slotSize + 4.0f, 0.95f, 0.88f, 0.52f, 0.98f);
        addRect(vertices, selectedX + slotSize - 1.0f, slotY - 2.0f, 3.0f, slotSize + 4.0f, 0.95f, 0.88f, 0.52f, 0.98f);

        addGlyph(vertices, '1', slot1X + slotSize * 0.5f - 5.0f, slotY + slotSize + 6.0f, 10.0f, 14.0f, 2.0f);
        addGlyph(vertices, '2', slot2X + slotSize * 0.5f - 5.0f, slotY + slotSize + 6.0f, 10.0f, 14.0f, 2.0f);

        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        glUseProgram(hudProgram);
        glUniform2f(hudScreenSizeLocation, window.width(), window.height());
        glBindVertexArray(hudVao);
        glBindBuffer(GL_ARRAY_BUFFER, hudVbo);
        glBufferData(GL_ARRAY_BUFFER, vertices.toArray(), GL_DYNAMIC_DRAW);
        glDrawArrays(GL_TRIANGLES, 0, vertices.size() / 6);
        glBindVertexArray(0);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
    }

    private void renderFpsCounter() {
        updateFpsCounter();

        String text = "FPS " + displayedFps;
        float glyphWidth = 14.0f;
        float glyphHeight = 22.0f;
        float thickness = 3.0f;
        float gap = 3.0f;
        float padding = 12.0f;
        float textWidth = text.length() * glyphWidth + Math.max(0, text.length() - 1) * gap;
        float startX = window.width() - padding - textWidth;
        float startY = padding;

        FloatArrayBuilder vertices = new FloatArrayBuilder(1024);
        float cursorX = startX;
        for (int i = 0; i < text.length(); i++) {
            addGlyph(vertices, text.charAt(i), cursorX, startY, glyphWidth, glyphHeight, thickness);
            cursorX += glyphWidth + gap;
        }

        if (vertices.size() == 0) {
            return;
        }

        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        glUseProgram(hudProgram);
        glUniform2f(hudScreenSizeLocation, window.width(), window.height());
        glBindVertexArray(hudVao);
        glBindBuffer(GL_ARRAY_BUFFER, hudVbo);
        glBufferData(GL_ARRAY_BUFFER, vertices.toArray(), GL_DYNAMIC_DRAW);
        glDrawArrays(GL_TRIANGLES, 0, vertices.size() / 6);
        glBindVertexArray(0);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
    }

    private void updateFpsCounter() {
        fpsSampleFrames++;
        double now = GLFW.glfwGetTime();
        double elapsed = now - fpsSampleTime;
        if (elapsed >= 1.0) {
            displayedFps = (int) Math.round(fpsSampleFrames / elapsed);
            fpsSampleFrames = 0;
            fpsSampleTime = now;
        }
    }

    private void addGlyph(FloatArrayBuilder vertices, char glyph, float x, float y, float width, float height, float thickness) {
        int segments = glyphSegments(glyph);
        float colorR = 1.0f;
        float colorG = 1.0f;
        float colorB = 1.0f;
        float colorA = 1.0f;

        if ((segments & SEGMENT_A) != 0) {
            addRect(vertices, x + thickness, y, width - thickness * 2.0f, thickness, colorR, colorG, colorB, colorA);
        }
        if ((segments & SEGMENT_B) != 0) {
            addRect(vertices, x + width - thickness, y + thickness, thickness, height * 0.5f - thickness, colorR, colorG, colorB, colorA);
        }
        if ((segments & SEGMENT_C) != 0) {
            addRect(vertices, x + width - thickness, y + height * 0.5f, thickness, height * 0.5f - thickness, colorR, colorG, colorB, colorA);
        }
        if ((segments & SEGMENT_D) != 0) {
            addRect(vertices, x + thickness, y + height - thickness, width - thickness * 2.0f, thickness, colorR, colorG, colorB, colorA);
        }
        if ((segments & SEGMENT_E) != 0) {
            addRect(vertices, x, y + height * 0.5f, thickness, height * 0.5f - thickness, colorR, colorG, colorB, colorA);
        }
        if ((segments & SEGMENT_F) != 0) {
            addRect(vertices, x, y + thickness, thickness, height * 0.5f - thickness, colorR, colorG, colorB, colorA);
        }
        if ((segments & SEGMENT_G) != 0) {
            addRect(vertices, x + thickness, y + height * 0.5f - thickness * 0.5f, width - thickness * 2.0f, thickness, colorR, colorG, colorB, colorA);
        }
    }

    private int glyphSegments(char glyph) {
        return switch (Character.toUpperCase(glyph)) {
            case '0' -> SEGMENT_A | SEGMENT_B | SEGMENT_C | SEGMENT_D | SEGMENT_E | SEGMENT_F;
            case '1' -> SEGMENT_B | SEGMENT_C;
            case '2' -> SEGMENT_A | SEGMENT_B | SEGMENT_G | SEGMENT_E | SEGMENT_D;
            case '3' -> SEGMENT_A | SEGMENT_B | SEGMENT_C | SEGMENT_D | SEGMENT_G;
            case '4' -> SEGMENT_F | SEGMENT_G | SEGMENT_B | SEGMENT_C;
            case '5' -> SEGMENT_A | SEGMENT_F | SEGMENT_G | SEGMENT_C | SEGMENT_D;
            case '6' -> SEGMENT_A | SEGMENT_F | SEGMENT_E | SEGMENT_D | SEGMENT_C | SEGMENT_G;
            case '7' -> SEGMENT_A | SEGMENT_B | SEGMENT_C;
            case '8' -> SEGMENT_A | SEGMENT_B | SEGMENT_C | SEGMENT_D | SEGMENT_E | SEGMENT_F | SEGMENT_G;
            case '9' -> SEGMENT_A | SEGMENT_B | SEGMENT_C | SEGMENT_D | SEGMENT_F | SEGMENT_G;
            case 'F' -> SEGMENT_A | SEGMENT_F | SEGMENT_E | SEGMENT_G;
            case 'P' -> SEGMENT_A | SEGMENT_B | SEGMENT_F | SEGMENT_E | SEGMENT_G;
            case 'S' -> SEGMENT_A | SEGMENT_F | SEGMENT_G | SEGMENT_C | SEGMENT_D;
            case ' ' -> 0;
            default -> SEGMENT_G;
        };
    }

    private void addRect(FloatArrayBuilder vertices, float left, float top, float width, float height, float r, float g, float b, float a) {
        float right = left + width;
        float bottom = top + height;

        addVertex(vertices, left, top, r, g, b, a);
        addVertex(vertices, left, bottom, r, g, b, a);
        addVertex(vertices, right, bottom, r, g, b, a);

        addVertex(vertices, left, top, r, g, b, a);
        addVertex(vertices, right, bottom, r, g, b, a);
        addVertex(vertices, right, top, r, g, b, a);
    }

    private void addVertex(FloatArrayBuilder vertices, float x, float y, float r, float g, float b, float a) {
        vertices.add(x);
        vertices.add(y);
        vertices.add(r);
        vertices.add(g);
        vertices.add(b);
        vertices.add(a);
    }

    private void syncChunkMeshes(World world) {
        for (Chunk chunk : world.getLoadedChunks()) {
            if (!chunk.isDirty() && meshes.containsKey(chunk.position())) {
                continue;
            }

            ChunkMeshData meshData = ChunkMeshBuilder.build(chunk, world);
            Mesh mesh = meshes.computeIfAbsent(chunk.position(), ignored -> new Mesh());
            mesh.upload(meshData);
            chunk.clearDirty();
        }
    }

    private Vector3f sunDirection(TickSystem ticks) {
        float angle = ticks.getSunAngleRadians();
        Vector3f dir = new Vector3f(
                (float) Math.cos(angle) * 0.7f,
                (float) Math.sin(angle),
                (float) Math.sin(angle) * 0.35f - 0.6f
        );
        return dir.normalize();
    }

    private Vector3f moonDirection(Vector3f sunDirection) {
        return new Vector3f(sunDirection).negate();
    }

    private float sunStrength(Vector3f sunDirection) {
        return Math.max(0.0f, -sunDirection.y);
    }

    private float moonStrength(Vector3f sunDirection) {
        return Math.max(0.0f, sunDirection.y);
    }

    private void buildCascadeLightMvps(Player player, Vector3f sunDirection) {
        Vector3f eye = player.eyePosition(new Vector3f());
        for (int i = 0; i < CASCADE_COUNT; i++) {
            float split = CASCADE_SPLITS[i];
            float radius = split * 1.1f;
            float texelWorld = (radius * 2.0f) / SHADOW_MAP_SIZE;
            Vector3f center = new Vector3f(eye).add(0.0f, -4.0f + i * 2.0f, 0.0f);
            center.x = (float) Math.floor(center.x / texelWorld) * texelWorld;
            center.y = (float) Math.floor(center.y / texelWorld) * texelWorld;
            center.z = (float) Math.floor(center.z / texelWorld) * texelWorld;
            Vector3f lightPos = new Vector3f(sunDirection).mul(-(radius + 40.0f)).add(center);

            cascadeLightMvp[i].identity()
                    .ortho(-radius, radius, -radius, radius, 1.0f, radius * 4.0f)
                    .mul(new Matrix4f().lookAt(lightPos, center, new Vector3f(0.0f, 1.0f, 0.0f)));
        }
    }

    private void updateProbeVolume(World world, Player player, TickSystem ticks, Vector3f sunDirection, Vector3f moonDirection) {
        Vector3f eye = player.eyePosition(new Vector3f());
        float startX = (float) Math.floor((eye.x - probeExtent.x * 0.5f) / PROBE_CELL_SIZE) * PROBE_CELL_SIZE;
        float startY = (float) Math.floor((eye.y - probeExtent.y * 0.5f) / PROBE_CELL_SIZE) * PROBE_CELL_SIZE;
        float startZ = (float) Math.floor((eye.z - probeExtent.z * 0.5f) / PROBE_CELL_SIZE) * PROBE_CELL_SIZE;

        if (Math.abs(startX - probeOrigin.x) >= PROBE_CELL_SIZE
                || Math.abs(startY - probeOrigin.y) >= PROBE_CELL_SIZE
                || Math.abs(startZ - probeOrigin.z) >= PROBE_CELL_SIZE) {
            probeOrigin.set(startX, startY, startZ);
            probeUpdateCursor = 0;
        }

        float skyBrightness = ticks.getSkyBrightness();
        int total = PROBE_GRID_X * PROBE_GRID_Y * PROBE_GRID_Z;
        for (int i = 0; i < PROBE_UPDATES_PER_FRAME; i++) {
            int probeIndex = (probeUpdateCursor + i) % total;
            int px = probeIndex % PROBE_GRID_X;
            int py = (probeIndex / PROBE_GRID_X) % PROBE_GRID_Y;
            int pz = probeIndex / (PROBE_GRID_X * PROBE_GRID_Y);

            float wx = probeOrigin.x + px * PROBE_CELL_SIZE;
            float wy = probeOrigin.y + py * PROBE_CELL_SIZE;
            float wz = probeOrigin.z + pz * PROBE_CELL_SIZE;

            Vector3f probePos = new Vector3f(wx + 0.5f, wy + 0.5f, wz + 0.5f);

            float upVisibility = visibilityAlong(world, probePos, new Vector3f(0.0f, 1.0f, 0.0f), 22.0f);
            float sideA = visibilityAlong(world, probePos, new Vector3f(1.0f, 0.2f, 0.0f).normalize(), 16.0f);
            float sideB = visibilityAlong(world, probePos, new Vector3f(-1.0f, 0.2f, 0.0f).normalize(), 16.0f);
            float sideC = visibilityAlong(world, probePos, new Vector3f(0.0f, 0.2f, 1.0f).normalize(), 16.0f);
            float sideD = visibilityAlong(world, probePos, new Vector3f(0.0f, 0.2f, -1.0f).normalize(), 16.0f);
            float sunVis = visibilityAlong(world, probePos, new Vector3f(sunDirection).negate(), 80.0f);
            float moonVis = visibilityAlong(world, probePos, new Vector3f(moonDirection).negate(), 64.0f);

            float sun = sunStrength(sunDirection) * sunVis;
            float moon = moonStrength(sunDirection) * moonVis;

            float ambient = (0.08f + skyBrightness * 0.44f) * (0.30f + 0.36f * upVisibility + 0.085f * (sideA + sideB + sideC + sideD));
            float sunTerm = sun * (0.55f + 0.45f * skyBrightness);
            float moonTerm = moon * 0.17f;

            int base = probeIndex * 3;
            float nextR = ambient * 0.58f + sunTerm * 1.00f + moonTerm * 0.60f;
            float nextG = ambient * 0.62f + sunTerm * 0.93f + moonTerm * 0.70f;
            float nextB = ambient * 0.75f + sunTerm * 0.78f + moonTerm * 0.95f;
            float blend = 0.22f;
            probeData[base] = probeData[base] + (nextR - probeData[base]) * blend;
            probeData[base + 1] = probeData[base + 1] + (nextG - probeData[base + 1]) * blend;
            probeData[base + 2] = probeData[base + 2] + (nextB - probeData[base + 2]) * blend;
        }
        probeUpdateCursor = (probeUpdateCursor + PROBE_UPDATES_PER_FRAME) % total;

        glBindTexture(GL_TEXTURE_3D, probeTexture);
        glTexSubImage3D(
                GL_TEXTURE_3D,
                0,
                0,
                0,
                0,
                PROBE_GRID_X,
                PROBE_GRID_Y,
                PROBE_GRID_Z,
                GL_RGB,
                GL_FLOAT,
                probeData
        );
        glBindTexture(GL_TEXTURE_3D, 0);
    }

    private float visibilityAlong(World world, Vector3f start, Vector3f dir, float maxDistance) {
        RaycastHit hit = world.raycast(new Vector3f(start), new Vector3f(dir), maxDistance);
        if (hit == null) {
            return 1.0f;
        }
        return Math.max(0.0f, Math.min(1.0f, hit.distance() / maxDistance));
    }

    private Matrix4f worldViewProjection(Player player) {
        float aspect = (float) window.width() / (float) window.height();
        return new Matrix4f()
                .perspective((float) Math.toRadians(FIELD_OF_VIEW_DEGREES), aspect, NEAR_PLANE, FAR_PLANE)
                .mul(player.viewMatrix());
    }

    private void createShadowResources() {
        shadowFbo = glGenFramebuffers();
        shadowDepthTexture = glGenTextures();

        glBindTexture(GL_TEXTURE_2D_ARRAY, shadowDepthTexture);
        glTexImage3D(
            GL_TEXTURE_2D_ARRAY,
                0,
                GL_DEPTH_COMPONENT24,
                SHADOW_MAP_SIZE,
                SHADOW_MAP_SIZE,
            CASCADE_COUNT,
            0,
                GL_DEPTH_COMPONENT,
                GL_FLOAT,
                0L
        );
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
        glTexParameterfv(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_BORDER_COLOR, new float[]{1.0f, 1.0f, 1.0f, 1.0f});

        glBindFramebuffer(GL_FRAMEBUFFER, shadowFbo);
        glFramebufferTextureLayer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, shadowDepthTexture, 0, 0);
        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("Shadow framebuffer is incomplete");
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glBindTexture(GL_TEXTURE_2D_ARRAY, 0);
    }

    private void createProbeResources() {
        probeTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_3D, probeTexture);
        glTexImage3D(
                GL_TEXTURE_3D,
                0,
                GL_RGB16F,
                PROBE_GRID_X,
                PROBE_GRID_Y,
                PROBE_GRID_Z,
                0,
                GL_RGB,
                GL_FLOAT,
                probeData
        );
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        glBindTexture(GL_TEXTURE_3D, 0);
    }

    private void createSkyGeometry() {
        skyVao = glGenVertexArrays();
        skyVbo = glGenBuffers();

        float[] quad = {
                -1.0f, -1.0f,
                1.0f, -1.0f,
                1.0f, 1.0f,
                -1.0f, -1.0f,
                1.0f, 1.0f,
                -1.0f, 1.0f
        };

        glBindVertexArray(skyVao);
        glBindBuffer(GL_ARRAY_BUFFER, skyVbo);
        glBufferData(GL_ARRAY_BUFFER, quad, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0L);
        glEnableVertexAttribArray(0);
        glBindVertexArray(0);
    }

    private int createAtlasTexture(TextureAtlas atlas) {
        int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_RGBA8,
                atlas.width(),
                atlas.height(),
                0,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                atlas.pixels()
        );
        glBindTexture(GL_TEXTURE_2D, 0);
        return texture;
    }

    private int createRgbaTexture(String preferredPath, String baseName) {
        byte[] bytes = readTextureBytes(preferredPath, baseName);
        ByteBuffer encoded = MemoryUtil.memAlloc(bytes.length);
        encoded.put(bytes).flip();

        ByteBuffer pixels = null;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer widthBuffer = stack.mallocInt(1);
            IntBuffer heightBuffer = stack.mallocInt(1);
            IntBuffer channelsBuffer = stack.mallocInt(1);
            STBImage.stbi_set_flip_vertically_on_load(false);
            pixels = STBImage.stbi_load_from_memory(encoded, widthBuffer, heightBuffer, channelsBuffer, 4);
            if (pixels == null) {
                throw new IllegalStateException("Failed to load texture '" + preferredPath + "': " + STBImage.stbi_failure_reason());
            }

            int texture = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, texture);
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexImage2D(
                    GL_TEXTURE_2D,
                    0,
                    GL_RGBA8,
                    widthBuffer.get(0),
                    heightBuffer.get(0),
                    0,
                    GL_RGBA,
                    GL_UNSIGNED_BYTE,
                    pixels
            );
            glBindTexture(GL_TEXTURE_2D, 0);
            return texture;
        } finally {
            if (pixels != null) {
                STBImage.stbi_image_free(pixels);
            }
            MemoryUtil.memFree(encoded);
        }
    }

    private byte[] readTextureBytes(String preferredPath, String baseName) {
        String[] candidates = {
                preferredPath,
                "assets/textures/" + baseName + ".png",
                "assets/textures/" + baseName + ".jpg",
                "src/main/resources/assets/textures/" + baseName + ".png",
                "src/main/resources/assets/textures/" + baseName + ".jpg"
        };
        for (String candidate : candidates) {
            Path path = Path.of(candidate);
            if (Files.exists(path)) {
                try {
                    return Files.readAllBytes(path);
                } catch (IOException exception) {
                    throw new IllegalStateException("Failed to read texture file: " + candidate, exception);
                }
            }
        }
        throw new IllegalStateException("Missing texture file for: " + baseName);
    }

    private void createHudGeometry() {
        hudVao = glGenVertexArrays();
        hudVbo = glGenBuffers();
        glBindVertexArray(hudVao);
        glBindBuffer(GL_ARRAY_BUFFER, hudVbo);
        glBufferData(GL_ARRAY_BUFFER, 6L * 6L * 4L * Float.BYTES, GL_DYNAMIC_DRAW);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 6 * Float.BYTES, 0L);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 4, GL_FLOAT, false, 6 * Float.BYTES, 2L * Float.BYTES);
        glEnableVertexAttribArray(1);
        glBindVertexArray(0);
    }

    private void createOutlineGeometry() {
        outlineVao = glGenVertexArrays();
        outlineVbo = glGenBuffers();
        glBindVertexArray(outlineVao);
        glBindBuffer(GL_ARRAY_BUFFER, outlineVbo);
        glBufferData(GL_ARRAY_BUFFER, 24L * 7L * Float.BYTES, GL_DYNAMIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 7 * Float.BYTES, 0L);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 4, GL_FLOAT, false, 7 * Float.BYTES, 3L * Float.BYTES);
        glEnableVertexAttribArray(1);
        glBindVertexArray(0);
    }

    private int createWorldProgram() {
        String vertexShader = """
                #version 330 core
                layout(location = 0) in vec3 aPosition;
                layout(location = 1) in vec2 aUv;
                layout(location = 2) in vec3 aNormal;

                uniform mat4 uMvp;
                uniform mat4 uLightMvp[4];

                out vec2 vUv;
                out vec3 vNormal;
                out vec3 vWorldPos;
                out vec4 vShadowCoord[4];

                void main() {
                    vUv = aUv;
                    vNormal = aNormal;
                    vWorldPos = aPosition;
                    for (int i = 0; i < 4; i++) {
                        vShadowCoord[i] = uLightMvp[i] * vec4(aPosition, 1.0);
                    }
                    gl_Position = uMvp * vec4(aPosition, 1.0);
                }
                """;

        String fragmentShader = """
                #version 330 core
                in vec2 vUv;
                in vec3 vNormal;
                in vec3 vWorldPos;
                in vec4 vShadowCoord[4];

                uniform sampler2D uAtlas;
                uniform sampler2DArray uShadowMap;
                uniform sampler3D uProbeVolume;
                uniform vec3 uLightDir;
                uniform vec3 uLightColor;
                uniform float uLightIntensity;
                uniform float uCascadeSplits[4];
                uniform float uShadowTexelSize;
                uniform vec3 uCameraPos;
                uniform vec3 uProbeOrigin;
                uniform vec3 uProbeExtent;

                out vec4 fragColor;

                const int CASCADE_COUNT = 4;

                int selectCascade(float viewDistance) {
                    if (viewDistance < uCascadeSplits[0]) {
                        return 0;
                    }
                    if (viewDistance < uCascadeSplits[1]) {
                        return 1;
                    }
                    if (viewDistance < uCascadeSplits[2]) {
                        return 2;
                    }
                    return 3;
                }

                float shadowFactor(int cascade, vec4 shadowCoord, vec3 normal, vec3 lightDir) {
                    vec3 projected = shadowCoord.xyz / max(shadowCoord.w, 0.0001);
                    projected = projected * 0.5 + 0.5;

                    if (projected.x < 0.0 || projected.x > 1.0 || projected.y < 0.0 || projected.y > 1.0 || projected.z > 1.0) {
                        return 1.0;
                    }

                    float ndotl = max(dot(normal, lightDir), 0.0);
                    float bias = max(0.00005, mix(0.00045, 0.00008, ndotl));
                    float currentDepth = projected.z - bias;
                    float visibility = 0.0;
                    float sampleCount = 0.0;

                    for (int y = -2; y <= 2; y++) {
                        for (int x = -2; x <= 2; x++) {
                            vec2 offset = vec2(float(x), float(y)) * uShadowTexelSize;
                            vec2 uv = projected.xy + offset;
                            if (uv.x < 0.0 || uv.x > 1.0 || uv.y < 0.0 || uv.y > 1.0) {
                                visibility += 1.0;
                                sampleCount += 1.0;
                                continue;
                            }
                            float closestDepth = texture(uShadowMap, vec3(uv, float(cascade))).r;
                            visibility += currentDepth <= closestDepth ? 1.0 : 0.15;
                            sampleCount += 1.0;
                        }
                    }

                    return visibility / max(sampleCount, 1.0);
                }

                void main() {
                    vec4 albedo = texture(uAtlas, vUv);
                    if (albedo.a < 0.01) {
                        discard;
                    }

                    vec3 normal = normalize(vNormal);
                    vec3 lightDir = normalize(-uLightDir);
                    float diffuse = max(dot(normal, lightDir), 0.0);
                    float viewDistance = distance(vWorldPos, uCameraPos);
                    int cascade = selectCascade(viewDistance);
                    float visibility = shadowFactor(cascade, vShadowCoord[cascade], normal, lightDir);

                    if (cascade < CASCADE_COUNT - 1) {
                        float blendStart = uCascadeSplits[cascade] - 6.0;
                        if (viewDistance > blendStart) {
                            float t = clamp((viewDistance - blendStart) / 6.0, 0.0, 1.0);
                            float nextVisibility = shadowFactor(cascade + 1, vShadowCoord[cascade + 1], normal, lightDir);
                            visibility = mix(visibility, nextVisibility, t);
                        }
                    }

                    vec3 probeUv = clamp((vWorldPos - uProbeOrigin) / uProbeExtent, vec3(0.0), vec3(0.999));
                    vec3 probeLight = texture(uProbeVolume, probeUv).rgb;

                    float direct = diffuse * visibility * uLightIntensity;
                    vec3 lighting = clamp(probeLight + uLightColor * direct, vec3(0.0), vec3(1.6));

                    fragColor = vec4(albedo.rgb * lighting, albedo.a);
                }
                """;

        return createProgram(vertexShader, fragmentShader);
    }

    private int createShadowProgram() {
        String vertexShader = """
                #version 330 core
                layout(location = 0) in vec3 aPosition;

                uniform mat4 uLightMvp;

                void main() {
                    gl_Position = uLightMvp * vec4(aPosition, 1.0);
                }
                """;

        String fragmentShader = """
                #version 330 core
                void main() {
                }
                """;

        return createProgram(vertexShader, fragmentShader);
    }

    private int createSkyProgram() {
        String vertexShader = """
                #version 330 core
                layout(location = 0) in vec2 aPosition;
                out vec2 vPosition;

                void main() {
                    vPosition = aPosition;
                    gl_Position = vec4(aPosition, 0.999, 1.0);
                }
                """;

        String fragmentShader = """
                #version 330 core
                in vec2 vPosition;

                uniform vec3 uTopColor;
                uniform vec3 uBottomColor;
                uniform vec3 uSunDir;
                uniform vec3 uSunColor;
                uniform vec3 uMoonDir;
                uniform vec3 uMoonColor;
                uniform sampler2D uSunTex;
                uniform sampler2D uMoonTex;
                uniform vec3 uViewRight;
                uniform vec3 uViewUp;
                uniform vec3 uViewForward;

                out vec4 fragColor;

                void main() {
                    float t = clamp(vPosition.y * 0.5 + 0.5, 0.0, 1.0);
                    vec3 sky = mix(uBottomColor, uTopColor, t);

                    vec3 viewDir = normalize(uViewForward * 1.35 + uViewRight * vPosition.x + uViewUp * vPosition.y);
                    float sunDisk = pow(max(dot(viewDir, normalize(-uSunDir)), 0.0), 160.0);
                    float sunGlow = pow(max(dot(viewDir, normalize(-uSunDir)), 0.0), 24.0) * 0.25;
                    float moonDisk = pow(max(dot(viewDir, normalize(-uMoonDir)), 0.0), 220.0);
                    float moonGlow = pow(max(dot(viewDir, normalize(-uMoonDir)), 0.0), 30.0) * 0.20;
                    sky += uSunColor * (sunDisk + sunGlow);
                    sky += uMoonColor * (moonDisk + moonGlow);

                    vec3 worldUp = vec3(0.0, 1.0, 0.0);

                    vec3 sunCenter = normalize(-uSunDir);
                    vec3 sunRight = cross(worldUp, sunCenter);
                    if (length(sunRight) < 0.001) {
                        sunRight = cross(vec3(1.0, 0.0, 0.0), sunCenter);
                    }
                    sunRight = normalize(sunRight);
                    vec3 sunUp = normalize(cross(sunCenter, sunRight));
                    vec2 sunLocal = vec2(dot(viewDir, sunRight), dot(viewDir, sunUp));
                    vec2 sunUv = sunLocal / 0.085 + 0.5;
                    float sunFacing = dot(viewDir, sunCenter);
                    if (sunFacing > 0.0 && sunUv.x >= 0.0 && sunUv.x <= 1.0 && sunUv.y >= 0.0 && sunUv.y <= 1.0) {
                        vec4 sunTexel = texture(uSunTex, sunUv);
                        sky = mix(sky, sunTexel.rgb * (uSunColor + vec3(0.35, 0.28, 0.08)), sunTexel.a);
                    }

                    vec3 moonCenter = normalize(-uMoonDir);
                    vec3 moonRight = cross(worldUp, moonCenter);
                    if (length(moonRight) < 0.001) {
                        moonRight = cross(vec3(1.0, 0.0, 0.0), moonCenter);
                    }
                    moonRight = normalize(moonRight);
                    vec3 moonUp = normalize(cross(moonCenter, moonRight));
                    vec2 moonLocal = vec2(dot(viewDir, moonRight), dot(viewDir, moonUp));
                    vec2 moonUv = moonLocal / 0.08 + 0.5;
                    float moonFacing = dot(viewDir, moonCenter);
                    if (moonFacing > 0.0 && moonUv.x >= 0.0 && moonUv.x <= 1.0 && moonUv.y >= 0.0 && moonUv.y <= 1.0) {
                        vec4 moonTexel = texture(uMoonTex, moonUv);
                        sky = mix(sky, moonTexel.rgb * (uMoonColor + vec3(0.18, 0.2, 0.24)), moonTexel.a);
                    }

                    fragColor = vec4(sky, 1.0);
                }
                """;

        return createProgram(vertexShader, fragmentShader);
    }

    private int createOutlineProgram() {
        String vertexShader = """
                #version 330 core
                layout(location = 0) in vec3 aPosition;
                layout(location = 1) in vec4 aColor;

                uniform mat4 uMvp;

                out vec4 vColor;

                void main() {
                    vColor = aColor;
                    gl_Position = uMvp * vec4(aPosition, 1.0);
                }
                """;

        String fragmentShader = """
                #version 330 core
                in vec4 vColor;
                out vec4 fragColor;

                void main() {
                    fragColor = vColor;
                }
                """;

        return createProgram(vertexShader, fragmentShader);
    }

    private int createHudProgram() {
        String vertexShader = """
                #version 330 core
                layout(location = 0) in vec2 aPosition;
                layout(location = 1) in vec4 aColor;

                uniform vec2 uScreenSize;

                out vec4 vColor;

                void main() {
                    vec2 ndc = vec2(
                            (aPosition.x / uScreenSize.x) * 2.0 - 1.0,
                            1.0 - (aPosition.y / uScreenSize.y) * 2.0
                    );
                    vColor = aColor;
                    gl_Position = vec4(ndc, 0.0, 1.0);
                }
                """;

        String fragmentShader = """
                #version 330 core
                in vec4 vColor;
                out vec4 fragColor;

                void main() {
                    fragColor = vColor;
                }
                """;

        return createProgram(vertexShader, fragmentShader);
    }

    private int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = compileShader(GL_VERTEX_SHADER, vertexSource);
        int fragmentShader = compileShader(GL_FRAGMENT_SHADER, fragmentSource);

        int program = glCreateProgram();
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
        glLinkProgram(program);

        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            String log = glGetProgramInfoLog(program);
            glDeleteShader(vertexShader);
            glDeleteShader(fragmentShader);
            glDeleteProgram(program);
            throw new IllegalStateException("Shader program link failed: " + log);
        }

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
        return program;
    }

    private int compileShader(int shaderType, String source) {
        int shader = glCreateShader(shaderType);
        glShaderSource(shader, source);
        glCompileShader(shader);

        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            String log = glGetShaderInfoLog(shader);
            glDeleteShader(shader);
            throw new IllegalStateException("Shader compile failed: " + log);
        }

        return shader;
    }

    private static final int SEGMENT_A = 1 << 0;
    private static final int SEGMENT_B = 1 << 1;
    private static final int SEGMENT_C = 1 << 2;
    private static final int SEGMENT_D = 1 << 3;
    private static final int SEGMENT_E = 1 << 4;
    private static final int SEGMENT_F = 1 << 5;
    private static final int SEGMENT_G = 1 << 6;

    private static Matrix4f[] createCascadeMatrices() {
        Matrix4f[] matrices = new Matrix4f[CASCADE_COUNT];
        for (int i = 0; i < CASCADE_COUNT; i++) {
            matrices[i] = new Matrix4f();
        }
        return matrices;
    }

    private static final class Mesh {
        private int vao;
        private int vbo;
        private int ebo;
        private int indexCount;

        void upload(ChunkMeshData meshData) {
            if (vao == 0) {
                vao = glGenVertexArrays();
                vbo = glGenBuffers();
                ebo = glGenBuffers();

                glBindVertexArray(vao);
                glBindBuffer(GL_ARRAY_BUFFER, vbo);
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
                glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * Float.BYTES, 0L);
                glEnableVertexAttribArray(0);
                glVertexAttribPointer(1, 2, GL_FLOAT, false, 8 * Float.BYTES, 3L * Float.BYTES);
                glEnableVertexAttribArray(1);
                glVertexAttribPointer(2, 3, GL_FLOAT, false, 8 * Float.BYTES, 5L * Float.BYTES);
                glEnableVertexAttribArray(2);
                glBindVertexArray(0);
            }

            glBindVertexArray(vao);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, meshData.vertices(), GL_STATIC_DRAW);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, meshData.indices(), GL_STATIC_DRAW);
            glBindVertexArray(0);
            indexCount = meshData.indices().length;
        }

        void destroy() {
            if (ebo != 0) {
                glDeleteBuffers(ebo);
                ebo = 0;
            }
            if (vbo != 0) {
                glDeleteBuffers(vbo);
                vbo = 0;
            }
            if (vao != 0) {
                glDeleteVertexArrays(vao);
                vao = 0;
            }
            indexCount = 0;
        }
    }

    private static final class FloatArrayBuilder {
        private float[] data;
        private int size;

        FloatArrayBuilder(int initialCapacity) {
            data = new float[Math.max(16, initialCapacity)];
        }

        void add(float value) {
            ensureCapacity(size + 1);
            data[size++] = value;
        }

        int size() {
            return size;
        }

        float[] toArray() {
            float[] out = new float[size];
            System.arraycopy(data, 0, out, 0, size);
            return out;
        }

        private void ensureCapacity(int required) {
            if (required <= data.length) {
                return;
            }
            int newCapacity = data.length;
            while (newCapacity < required) {
                newCapacity *= 2;
            }
            float[] replacement = new float[newCapacity];
            System.arraycopy(data, 0, replacement, 0, size);
            data = replacement;
        }
    }
}
