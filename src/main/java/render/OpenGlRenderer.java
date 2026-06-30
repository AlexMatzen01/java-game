package render;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL11C.GL_BACK;
import static org.lwjgl.opengl.GL11C.GL_BLEND;
import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_COMPONENT;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11C.GL_FALSE;
import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL11C.GL_LINEAR;
import static org.lwjgl.opengl.GL11C.GL_LINES;
import static org.lwjgl.opengl.GL11C.GL_NEAREST;
import static org.lwjgl.opengl.GL11C.GL_NONE;
import static org.lwjgl.opengl.GL11C.GL_ONE;
import static org.lwjgl.opengl.GL11C.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11C.GL_POLYGON_OFFSET_FILL;
import static org.lwjgl.opengl.GL11C.GL_RGB;
import static org.lwjgl.opengl.GL11C.GL_RGBA;
import static org.lwjgl.opengl.GL11C.GL_RGBA8;
import static org.lwjgl.opengl.GL11C.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_BORDER_COLOR;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11C.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11C.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL11C.glBlendFunc;
import static org.lwjgl.opengl.GL11C.glClear;
import static org.lwjgl.opengl.GL11C.glCullFace;
import static org.lwjgl.opengl.GL11C.glDeleteTextures;
import static org.lwjgl.opengl.GL11C.glDepthMask;
import static org.lwjgl.opengl.GL11C.glDisable;
import static org.lwjgl.opengl.GL11C.glDrawArrays;
import static org.lwjgl.opengl.GL11C.glDrawBuffer;
import static org.lwjgl.opengl.GL11C.glDrawElements;
import static org.lwjgl.opengl.GL11C.glEnable;
import static org.lwjgl.opengl.GL11C.glGenTextures;
import static org.lwjgl.opengl.GL11C.glPixelStorei;
import static org.lwjgl.opengl.GL11C.glPolygonOffset;
import static org.lwjgl.opengl.GL11C.glReadBuffer;
import static org.lwjgl.opengl.GL11C.glTexImage2D;
import static org.lwjgl.opengl.GL11C.glTexParameterfv;
import static org.lwjgl.opengl.GL11C.glTexParameteri;
import static org.lwjgl.opengl.GL11C.glViewport;
import static org.lwjgl.opengl.GL12C.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL12C.GL_TEXTURE_3D;
import static org.lwjgl.opengl.GL12C.GL_TEXTURE_WRAP_R;
import static org.lwjgl.opengl.GL12C.glTexImage3D;
import static org.lwjgl.opengl.GL12C.glTexSubImage3D;
import static org.lwjgl.opengl.GL13C.GL_CLAMP_TO_BORDER;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE1;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE2;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE3;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE4;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL14C.GL_DEPTH_COMPONENT24;
import static org.lwjgl.opengl.GL15C.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15C.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15C.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15C.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15C.glBindBuffer;
import static org.lwjgl.opengl.GL15C.glBufferData;
import static org.lwjgl.opengl.GL15C.glDeleteBuffers;
import static org.lwjgl.opengl.GL15C.glGenBuffers;
import static org.lwjgl.opengl.GL20C.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20C.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20C.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20C.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20C.glAttachShader;
import static org.lwjgl.opengl.GL20C.glCompileShader;
import static org.lwjgl.opengl.GL20C.glCreateProgram;
import static org.lwjgl.opengl.GL20C.glCreateShader;
import static org.lwjgl.opengl.GL20C.glDeleteProgram;
import static org.lwjgl.opengl.GL20C.glDeleteShader;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20C.glGetProgrami;
import static org.lwjgl.opengl.GL20C.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20C.glGetShaderi;
import static org.lwjgl.opengl.GL20C.glGetUniformLocation;
import static org.lwjgl.opengl.GL20C.glLinkProgram;
import static org.lwjgl.opengl.GL20C.glShaderSource;
import static org.lwjgl.opengl.GL20C.glUniform1f;
import static org.lwjgl.opengl.GL20C.glUniform1fv;
import static org.lwjgl.opengl.GL20C.glUniform1i;
import static org.lwjgl.opengl.GL20C.glUniform2f;
import static org.lwjgl.opengl.GL20C.glUniform3f;
import static org.lwjgl.opengl.GL20C.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20C.glUseProgram;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.GL_DEPTH_ATTACHMENT;
import static org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER_COMPLETE;
import static org.lwjgl.opengl.GL30C.GL_RGB16F;
import static org.lwjgl.opengl.GL30C.GL_TEXTURE_2D_ARRAY;
import static org.lwjgl.opengl.GL30C.glBindFramebuffer;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glCheckFramebufferStatus;
import static org.lwjgl.opengl.GL30C.glDeleteFramebuffers;
import static org.lwjgl.opengl.GL30C.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30C.glFramebufferTextureLayer;
import static org.lwjgl.opengl.GL30C.glGenFramebuffers;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import static org.lwjgl.system.MemoryUtil.NULL;

import blocks.Block;
import blocks.BlockRegistry;
import blocks.Blocks;
import engine.GameOptions;
import engine.GraphicsSettings;
import player.Player;
import util.TickSystem;
import world.Chunk;
import world.ChunkMeshBuilder;
import world.ChunkMeshData;
import world.ChunkPos;
import world.FallingBlock;
import world.RaycastHit;
import world.World;

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

    private ParticleSystem particleSystem;

    public void setParticleSystem(ParticleSystem particleSystem) {
        this.particleSystem = particleSystem;
    }

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
        private int worldExposureLocation;
        private int worldAmbientBoostLocation;
        private int worldContrastLocation;
        private int worldShadowStrengthLocation;
        private int worldShadowFilterRadiusLocation;
        private int worldRealisticLightingLocation;
    private int worldFogDensityLocation;

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
    private int skyCameraPosLocation;
    private int skyCloudTimeLocation;
    private int skyViewRightLocation;
    private int skyViewUpLocation;
    private int skyViewForwardLocation;
    private int skySaturationLocation;
    private int skyHScreenScaleLocation;
    private int skyVScreenScaleLocation;

    private int volumetricProgram;
    private int volumetricCameraPosLocation;
    private int volumetricViewRightLocation;
    private int volumetricViewUpLocation;
    private int volumetricViewForwardLocation;
    private int volumetricLightDirLocation;
    private int volumetricLightColorLocation;
    private int volumetricLightIntensityLocation;
    private int volumetricShadowMapLocation;
    private final int[] volumetricLightMvpLocations = new int[CASCADE_COUNT];
    private int volumetricCascadeSplitsLocation;
    private int volumetricShadowTexelSizeLocation;
    private int volumetricHScreenScaleLocation;
    private int volumetricVScreenScaleLocation;

    private int outlineProgram;
    private int outlineVao;
    private int outlineVbo;
    private int outlineMvpLocation;

    private int hudProgram;
    private int hudVao;
    private int hudVbo;
    private int hudScreenSizeLocation;

    private int hudTextureProgram;
    private int hudTextureScreenSizeLocation;
    private int hudTextureVao;
    private int hudTextureVbo;

    private int particleProgram;
    private int particleMvpLocation;
    private int particleAtlasLocation;
    private int particleVao;
    private int particleVbo;
    private int fallingBlockVao;
    private int fallingBlockVbo;
    private int fallingBlockEbo;

    private int textProgram;
    private int textVao;
    private int textVbo;
    private int textTexture;
    private int textScreenSizeLocation;
    private final TextGlyph[] textGlyphs = new TextGlyph[127];
    private float textBaseFontSize;
    private float textAscent;

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

        BlockRegistry.bootstrap();
        atlas = TextureAtlas.load(BlockRegistry.allTextureNames());
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
        worldExposureLocation = glGetUniformLocation(worldProgram, "uExposure");
        worldAmbientBoostLocation = glGetUniformLocation(worldProgram, "uAmbientBoost");
        worldContrastLocation = glGetUniformLocation(worldProgram, "uContrast");
        worldShadowStrengthLocation = glGetUniformLocation(worldProgram, "uShadowStrength");
        worldShadowFilterRadiusLocation = glGetUniformLocation(worldProgram, "uShadowFilterRadius");
        worldRealisticLightingLocation = glGetUniformLocation(worldProgram, "uRealisticLighting");
        worldFogDensityLocation = glGetUniformLocation(worldProgram, "uFogDensity");

        skyProgram = createSkyProgram();
        skyTopColorLocation = glGetUniformLocation(skyProgram, "uTopColor");
        skyBottomColorLocation = glGetUniformLocation(skyProgram, "uBottomColor");
        skySunDirLocation = glGetUniformLocation(skyProgram, "uSunDir");
        skySunColorLocation = glGetUniformLocation(skyProgram, "uSunColor");
        skyMoonDirLocation = glGetUniformLocation(skyProgram, "uMoonDir");
        skyMoonColorLocation = glGetUniformLocation(skyProgram, "uMoonColor");
        skySunTextureLocation = glGetUniformLocation(skyProgram, "uSunTex");
        skyMoonTextureLocation = glGetUniformLocation(skyProgram, "uMoonTex");
        skyCameraPosLocation = glGetUniformLocation(skyProgram, "uCameraPos");
        skyCloudTimeLocation = glGetUniformLocation(skyProgram, "uCloudTime");
        skyViewRightLocation = glGetUniformLocation(skyProgram, "uViewRight");
        skyViewUpLocation = glGetUniformLocation(skyProgram, "uViewUp");
        skyViewForwardLocation = glGetUniformLocation(skyProgram, "uViewForward");
        skySaturationLocation = glGetUniformLocation(skyProgram, "uSkySaturation");
        skyHScreenScaleLocation = glGetUniformLocation(skyProgram, "uHScreenScale");
        skyVScreenScaleLocation = glGetUniformLocation(skyProgram, "uVScreenScale");
        createSkyGeometry();

        volumetricProgram = createVolumetricProgram();
        volumetricCameraPosLocation = glGetUniformLocation(volumetricProgram, "uCameraPos");
        volumetricViewRightLocation = glGetUniformLocation(volumetricProgram, "uViewRight");
        volumetricViewUpLocation = glGetUniformLocation(volumetricProgram, "uViewUp");
        volumetricViewForwardLocation = glGetUniformLocation(volumetricProgram, "uViewForward");
        volumetricLightDirLocation = glGetUniformLocation(volumetricProgram, "uLightDir");
        volumetricLightColorLocation = glGetUniformLocation(volumetricProgram, "uLightColor");
        volumetricLightIntensityLocation = glGetUniformLocation(volumetricProgram, "uLightIntensity");
        volumetricShadowMapLocation = glGetUniformLocation(volumetricProgram, "uShadowMap");
        for (int i = 0; i < CASCADE_COUNT; i++) {
            volumetricLightMvpLocations[i] = glGetUniformLocation(volumetricProgram, "uLightMvp[" + i + "]");
        }
        volumetricCascadeSplitsLocation = glGetUniformLocation(volumetricProgram, "uCascadeSplits");
        volumetricShadowTexelSizeLocation = glGetUniformLocation(volumetricProgram, "uShadowTexelSize");
        volumetricHScreenScaleLocation = glGetUniformLocation(volumetricProgram, "uHScreenScale");
        volumetricVScreenScaleLocation = glGetUniformLocation(volumetricProgram, "uVScreenScale");

        outlineProgram = createOutlineProgram();
        outlineMvpLocation = glGetUniformLocation(outlineProgram, "uMvp");
        createOutlineGeometry();

        hudProgram = createHudProgram();
        hudScreenSizeLocation = glGetUniformLocation(hudProgram, "uScreenSize");
        createHudGeometry();

        hudTextureProgram = createHudTextureProgram();
        hudTextureScreenSizeLocation = glGetUniformLocation(hudTextureProgram, "uScreenSize");
        createHudTextureGeometry();

        textTexture = createTextTexture();
        textProgram = createTextProgram();
        textScreenSizeLocation = glGetUniformLocation(textProgram, "uScreenSize");
        int textFontLocation = glGetUniformLocation(textProgram, "uFont");
        glUseProgram(textProgram);
        glUniform1i(textFontLocation, 0);
        glUseProgram(0);
        textVao = glGenVertexArrays();
        textVbo = glGenBuffers();
        glBindVertexArray(textVao);
        glBindBuffer(GL_ARRAY_BUFFER, textVbo);
        glBufferData(GL_ARRAY_BUFFER, 0L, GL_DYNAMIC_DRAW);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0L);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2L * Float.BYTES);
        glEnableVertexAttribArray(1);
        glBindVertexArray(0);

        particleProgram = createParticleProgram();
        particleMvpLocation = glGetUniformLocation(particleProgram, "uMvp");
        particleAtlasLocation = glGetUniformLocation(particleProgram, "uAtlas");
        particleVao = glGenVertexArrays();
        particleVbo = glGenBuffers();
        glBindVertexArray(particleVao);
        glBindBuffer(GL_ARRAY_BUFFER, particleVbo);
        glBufferData(GL_ARRAY_BUFFER, 0L, GL_DYNAMIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.BYTES, 0L);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 6 * Float.BYTES, 3L * Float.BYTES);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(2, 1, GL_FLOAT, false, 6 * Float.BYTES, 5L * Float.BYTES);
        glEnableVertexAttribArray(2);
        glBindVertexArray(0);

        fallingBlockVao = glGenVertexArrays();
        fallingBlockVbo = glGenBuffers();
        fallingBlockEbo = glGenBuffers();
        glBindVertexArray(fallingBlockVao);
        glBindBuffer(GL_ARRAY_BUFFER, fallingBlockVbo);
        glBufferData(GL_ARRAY_BUFFER, 0L, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, fallingBlockEbo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, 0L, GL_DYNAMIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 9 * Float.BYTES, 0L);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 9 * Float.BYTES, 3L * Float.BYTES);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(2, 3, GL_FLOAT, false, 9 * Float.BYTES, 5L * Float.BYTES);
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(3, 1, GL_FLOAT, false, 9 * Float.BYTES, 8L * Float.BYTES);
        glEnableVertexAttribArray(3);
        glBindVertexArray(0);

        createProbeResources();

        glViewport(0, 0, window.width(), window.height());
    }

    public void render(World world, Player player, TickSystem ticks, boolean resized, GraphicsSettings graphicsSettings, boolean graphicsGuiOpen, boolean menuOpen, List<engine.WorldManager.WorldSlot> worlds, int selectedWorldIndex, boolean loadingWorld, boolean namingWorld, String worldNameInput) {
        if (resized) {
            glViewport(0, 0, window.width(), window.height());
        }

        if (!menuOpen && world != null && player != null) {
            syncChunkMeshes(world, player, graphicsSettings);

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

            renderSky(player, ticks, sunDirection, moonDirection, graphicsSettings);
            renderWorld(world, player, sunDirection, moonDirection, graphicsSettings);
            if (graphicsSettings.realisticLighting()) {
                renderVolumetricLight(player, sunDirection, moonDirection);
            }
            renderTargetOutline(world, player);
            renderParticles(player);
            renderHotbar(player);
            renderCrosshair();
            renderFpsCounter();
        } else {
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
            glViewport(0, 0, window.width(), window.height());
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            renderMenu(graphicsSettings, worlds, selectedWorldIndex, loadingWorld, namingWorld, worldNameInput);
        }
        if (graphicsGuiOpen) {
            renderGraphicsGui(graphicsSettings);
        }

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
        if (volumetricProgram != 0) {
            glDeleteProgram(volumetricProgram);
            volumetricProgram = 0;
        }
        if (outlineProgram != 0) {
            glDeleteProgram(outlineProgram);
            outlineProgram = 0;
        }
        if (hudProgram != 0) {
            glDeleteProgram(hudProgram);
            hudProgram = 0;
        }
        if (hudTextureVao != 0) {
            glDeleteVertexArrays(hudTextureVao);
            hudTextureVao = 0;
        }
        if (hudTextureProgram != 0) {
            glDeleteProgram(hudTextureProgram);
            hudTextureProgram = 0;
        }
        if (particleVbo != 0) {
            glDeleteBuffers(particleVbo);
            particleVbo = 0;
        }
        if (particleVao != 0) {
            glDeleteVertexArrays(particleVao);
            particleVao = 0;
        }
        if (particleProgram != 0) {
            glDeleteProgram(particleProgram);
            particleProgram = 0;
        }
        if (fallingBlockEbo != 0) {
            glDeleteBuffers(fallingBlockEbo);
            fallingBlockEbo = 0;
        }
        if (fallingBlockVbo != 0) {
            glDeleteBuffers(fallingBlockVbo);
            fallingBlockVbo = 0;
        }
        if (fallingBlockVao != 0) {
            glDeleteVertexArrays(fallingBlockVao);
            fallingBlockVao = 0;
        }
        if (textVbo != 0) {
            glDeleteBuffers(textVbo);
            textVbo = 0;
        }
        if (textVao != 0) {
            glDeleteVertexArrays(textVao);
            textVao = 0;
        }
        if (textProgram != 0) {
            glDeleteProgram(textProgram);
            textProgram = 0;
        }

        if (atlasTexture != 0) {
            glDeleteTextures(atlasTexture);
            atlasTexture = 0;
        }
        if (textTexture != 0) {
            glDeleteTextures(textTexture);
            textTexture = 0;
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
        glPolygonOffset(3.0f, 8.0f);

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

    private void renderWorld(World world, Player player, Vector3f sunDirection, Vector3f moonDirection, GraphicsSettings graphicsSettings) {
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
        float lightIntensity = sunStrength * 0.92f + moonStrength * 0.16f;
        float lowSunWarmth = 1.0f - Math.min(1.0f, sunStrength * 2.2f);
        float lightR = (1.00f + lowSunWarmth * 0.22f) * sunBlend + 0.56f * moonBlend;
        float lightG = (0.96f - lowSunWarmth * 0.12f) * sunBlend + 0.66f * moonBlend;
        float lightB = (0.86f - lowSunWarmth * 0.26f) * sunBlend + 1.00f * moonBlend;

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
        glUniform1f(worldExposureLocation, graphicsSettings.exposure());
        glUniform1f(worldAmbientBoostLocation, graphicsSettings.ambientBoost());
        glUniform1f(worldContrastLocation, graphicsSettings.contrast());
        glUniform1f(worldShadowStrengthLocation, graphicsSettings.shadowStrength());
        glUniform1i(worldShadowFilterRadiusLocation, graphicsSettings.shadowFilterRadius());
        glUniform1i(worldRealisticLightingLocation, graphicsSettings.realisticLighting() ? 1 : 0);
        glUniform1f(worldFogDensityLocation, graphicsSettings.fogDensity());

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

        renderFallingBlocks(world, player);

        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_3D, 0);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D_ARRAY, 0);
        glActiveTexture(GL_TEXTURE0);
    }

    private void renderFallingBlocks(World world, Player player) {
        List<FallingBlock> blocks = world.getFallingBlocks();
        if (blocks.isEmpty()) {
            return;
        }

        float tileU = 1.0f / TextureAtlas.COLUMNS;
        float tileV = 1.0f / TextureAtlas.ROWS;

        int totalVerts = blocks.size() * 24 * 9;
        int totalIdx = blocks.size() * 36;
        float[] vertices = new float[totalVerts];
        int[] indices = new int[totalIdx];
        int vi = 0;
        int ii = 0;
        int vertexBase = 0;

        for (FallingBlock fb : blocks) {
            Block block = BlockRegistry.get(fb.blockId);
            if (block == Blocks.AIR) {
                continue;
            }

            String texName = block.sideTexture();
            if (texName == null) {
                continue;
            }
            int slot = TextureAtlas.getSlot(texName);
            float u0 = (slot % TextureAtlas.COLUMNS) * tileU;
            float v0 = (slot / TextureAtlas.COLUMNS) * tileV;
            float u1 = u0 + tileU;
            float v1 = v0 + tileV;

            float minX = fb.x - 0.5f;
            float minY = fb.y - 0.5f;
            float minZ = fb.z - 0.5f;
            float maxX = fb.x + 0.5f;
            float maxY = fb.y + 0.5f;
            float maxZ = fb.z + 0.5f;

            // TOP
            addFallingVertex(vertices, vi, minX, maxY, minZ, u0, v0, 0, 1, 0, 1.0f); vi += 9;
            addFallingVertex(vertices, vi, minX, maxY, maxZ, u0, v1, 0, 1, 0, 1.0f); vi += 9;
            addFallingVertex(vertices, vi, maxX, maxY, maxZ, u1, v1, 0, 1, 0, 1.0f); vi += 9;
            addFallingVertex(vertices, vi, maxX, maxY, minZ, u1, v0, 0, 1, 0, 1.0f); vi += 9;
            indices[ii++] = vertexBase; indices[ii++] = vertexBase + 1; indices[ii++] = vertexBase + 2;
            indices[ii++] = vertexBase; indices[ii++] = vertexBase + 2; indices[ii++] = vertexBase + 3;
            vertexBase += 4;

            // BOTTOM
            addFallingVertex(vertices, vi, minX, minY, minZ, u0, v0, 0, -1, 0, 1.0f); vi += 9;
            addFallingVertex(vertices, vi, maxX, minY, minZ, u1, v0, 0, -1, 0, 1.0f); vi += 9;
            addFallingVertex(vertices, vi, maxX, minY, maxZ, u1, v1, 0, -1, 0, 1.0f); vi += 9;
            addFallingVertex(vertices, vi, minX, minY, maxZ, u0, v1, 0, -1, 0, 1.0f); vi += 9;
            indices[ii++] = vertexBase; indices[ii++] = vertexBase + 1; indices[ii++] = vertexBase + 2;
            indices[ii++] = vertexBase; indices[ii++] = vertexBase + 2; indices[ii++] = vertexBase + 3;
            vertexBase += 4;

            // WEST
            addFallingVertex(vertices, vi, minX, minY, minZ, u0, v0, -1, 0, 0, 1.0f); vi += 9;
            addFallingVertex(vertices, vi, minX, minY, maxZ, u1, v0, -1, 0, 0, 1.0f); vi += 9;
            addFallingVertex(vertices, vi, minX, maxY, maxZ, u1, v1, -1, 0, 0, 1.0f); vi += 9;
            addFallingVertex(vertices, vi, minX, maxY, minZ, u0, v1, -1, 0, 0, 1.0f); vi += 9;
            indices[ii++] = vertexBase; indices[ii++] = vertexBase + 1; indices[ii++] = vertexBase + 2;
            indices[ii++] = vertexBase; indices[ii++] = vertexBase + 2; indices[ii++] = vertexBase + 3;
            vertexBase += 4;

            // EAST
            addFallingVertex(vertices, vi, maxX, minY, maxZ, u0, v0, 1, 0, 0, 1.0f); vi += 9;
            addFallingVertex(vertices, vi, maxX, minY, minZ, u1, v0, 1, 0, 0, 1.0f); vi += 9;
            addFallingVertex(vertices, vi, maxX, maxY, minZ, u1, v1, 1, 0, 0, 1.0f); vi += 9;
            addFallingVertex(vertices, vi, maxX, maxY, maxZ, u0, v1, 1, 0, 0, 1.0f); vi += 9;
            indices[ii++] = vertexBase; indices[ii++] = vertexBase + 1; indices[ii++] = vertexBase + 2;
            indices[ii++] = vertexBase; indices[ii++] = vertexBase + 2; indices[ii++] = vertexBase + 3;
            vertexBase += 4;

            // SOUTH
            addFallingVertex(vertices, vi, minX, minY, maxZ, u0, v0, 0, 0, 1, 1.0f); vi += 9;
            addFallingVertex(vertices, vi, maxX, minY, maxZ, u1, v0, 0, 0, 1, 1.0f); vi += 9;
            addFallingVertex(vertices, vi, maxX, maxY, maxZ, u1, v1, 0, 0, 1, 1.0f); vi += 9;
            addFallingVertex(vertices, vi, minX, maxY, maxZ, u0, v1, 0, 0, 1, 1.0f); vi += 9;
            indices[ii++] = vertexBase; indices[ii++] = vertexBase + 1; indices[ii++] = vertexBase + 2;
            indices[ii++] = vertexBase; indices[ii++] = vertexBase + 2; indices[ii++] = vertexBase + 3;
            vertexBase += 4;

            // NORTH
            addFallingVertex(vertices, vi, maxX, minY, minZ, u0, v0, 0, 0, -1, 1.0f); vi += 9;
            addFallingVertex(vertices, vi, minX, minY, minZ, u1, v0, 0, 0, -1, 1.0f); vi += 9;
            addFallingVertex(vertices, vi, minX, maxY, minZ, u1, v1, 0, 0, -1, 1.0f); vi += 9;
            addFallingVertex(vertices, vi, maxX, maxY, minZ, u0, v1, 0, 0, -1, 1.0f); vi += 9;
            indices[ii++] = vertexBase; indices[ii++] = vertexBase + 1; indices[ii++] = vertexBase + 2;
            indices[ii++] = vertexBase; indices[ii++] = vertexBase + 2; indices[ii++] = vertexBase + 3;
            vertexBase += 4;
        }

        if (ii == 0) {
            return;
        }

        glBindVertexArray(fallingBlockVao);
        glBindBuffer(GL_ARRAY_BUFFER, fallingBlockVbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, fallingBlockEbo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_DYNAMIC_DRAW);
        glDrawElements(GL_TRIANGLES, ii, GL_UNSIGNED_INT, 0L);
        glBindVertexArray(0);
    }

    private static void addFallingVertex(float[] vertices, int offset, float x, float y, float z, float u, float v, float nx, float ny, float nz, float ao) {
        vertices[offset] = x;
        vertices[offset + 1] = y;
        vertices[offset + 2] = z;
        vertices[offset + 3] = u;
        vertices[offset + 4] = v;
        vertices[offset + 5] = nx;
        vertices[offset + 6] = ny;
        vertices[offset + 7] = nz;
        vertices[offset + 8] = ao;
    }

    private void renderSky(Player player, TickSystem ticks, Vector3f sunDirection, Vector3f moonDirection, GraphicsSettings graphicsSettings) {
        float brightness = ticks.getSkyBrightness();
        Vector3f forward = player.lookDirection(new Vector3f());
        Vector3f right = player.camera().right(new Vector3f());
        Vector3f up = new Vector3f(right).cross(forward).normalize();
        Vector3f cameraPos = player.eyePosition(new Vector3f());

        float topR = 0.02f + brightness * 0.10f;
        float topG = 0.04f + brightness * 0.34f;
        float topB = 0.10f + brightness * 0.78f;

        float bottomR = 0.06f + brightness * 0.46f;
        float bottomG = 0.08f + brightness * 0.62f;
        float bottomB = 0.14f + brightness * 0.78f;

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
        glUniform3f(skyCameraPosLocation, cameraPos.x, cameraPos.y, cameraPos.z);
        glUniform1f(skyCloudTimeLocation, (float) GLFW.glfwGetTime());
        glUniform3f(skyViewRightLocation, right.x, right.y, right.z);
        glUniform3f(skyViewUpLocation, up.x, up.y, up.z);
        glUniform3f(skyViewForwardLocation, forward.x, forward.y, forward.z);
        glUniform1f(skySaturationLocation, graphicsSettings.skySaturation());

        float tanHalfFov = (float) Math.tan(Math.toRadians(FIELD_OF_VIEW_DEGREES * 0.5));
        float aspect = (float) window.width() / (float) window.height();
        glUniform1f(skyHScreenScaleLocation, aspect * tanHalfFov);
        glUniform1f(skyVScreenScaleLocation, tanHalfFov);

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

    private void renderVolumetricLight(Player player, Vector3f sunDirection, Vector3f moonDirection) {
        float sunStrength = sunStrength(sunDirection);
        float moonStrength = moonStrength(sunDirection);
        Vector3f lightDirection = sunStrength >= moonStrength ? sunDirection : moonDirection;
        float intensity = sunStrength >= moonStrength ? sunStrength * 0.42f : moonStrength * 0.10f;
        if (intensity <= 0.01f) {
            return;
        }

        Vector3f forward = player.lookDirection(new Vector3f());
        Vector3f right = player.camera().right(new Vector3f());
        Vector3f up = new Vector3f(right).cross(forward).normalize();
        Vector3f cameraPos = player.eyePosition(new Vector3f());

        float sunBlend = sunStrength / Math.max(sunStrength + moonStrength, 0.0001f);
        float moonBlend = 1.0f - sunBlend;
        float lightR = 1.00f * sunBlend + 0.50f * moonBlend;
        float lightG = 0.92f * sunBlend + 0.60f * moonBlend;
        float lightB = 0.74f * sunBlend + 1.00f * moonBlend;

        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE);
        glUseProgram(volumetricProgram);
        glUniform3f(volumetricCameraPosLocation, cameraPos.x, cameraPos.y, cameraPos.z);
        glUniform3f(volumetricViewRightLocation, right.x, right.y, right.z);
        glUniform3f(volumetricViewUpLocation, up.x, up.y, up.z);
        glUniform3f(volumetricViewForwardLocation, forward.x, forward.y, forward.z);

        float tanHalfFov = (float) Math.tan(Math.toRadians(FIELD_OF_VIEW_DEGREES * 0.5));
        float aspect = (float) window.width() / (float) window.height();
        glUniform1f(volumetricHScreenScaleLocation, aspect * tanHalfFov);
        glUniform1f(volumetricVScreenScaleLocation, tanHalfFov);

        glUniform3f(volumetricLightDirLocation, lightDirection.x, lightDirection.y, lightDirection.z);
        glUniform3f(volumetricLightColorLocation, lightR, lightG, lightB);
        glUniform1f(volumetricLightIntensityLocation, intensity);
        glUniform1i(volumetricShadowMapLocation, 1);
        glUniform1fv(volumetricCascadeSplitsLocation, CASCADE_SPLITS);
        glUniform1f(volumetricShadowTexelSizeLocation, 1.0f / SHADOW_MAP_SIZE);

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D_ARRAY, shadowDepthTexture);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer lightBuffer = stack.mallocFloat(16);
            for (int i = 0; i < CASCADE_COUNT; i++) {
                cascadeLightMvp[i].get(lightBuffer.clear());
                glUniformMatrix4fv(volumetricLightMvpLocations[i], false, lightBuffer);
            }
        }

        glBindVertexArray(skyVao);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D_ARRAY, 0);
        glActiveTexture(GL_TEXTURE0);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
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

    private void renderParticles(Player player) {
        if (particleSystem == null || particleSystem.isEmpty()) {
            return;
        }

        float aspect = (float) window.width() / (float) window.height();
        Matrix4f mvp = new Matrix4f()
                .perspective((float) Math.toRadians(FIELD_OF_VIEW_DEGREES), aspect, NEAR_PLANE, FAR_PLANE)
                .mul(player.viewMatrix());

        Vector3f forward = player.lookDirection(new Vector3f());
        Vector3f right = player.camera().right(new Vector3f());
        Vector3f up = new Vector3f(right).cross(forward).normalize();

        float tileU = 1.0f / TextureAtlas.COLUMNS;
        float tileV = 1.0f / TextureAtlas.ROWS;

        List<Particle> particles = particleSystem.getParticles();
        float[] vertices = new float[particles.size() * 36];
        int idx = 0;
        for (Particle p : particles) {
            float alpha = Math.max(0.0f, p.life / p.maxLife);
            if (alpha <= 0.0f) {
                continue;
            }

            float hw = p.size * 0.5f;
            float rx = right.x * hw, ry = right.y * hw, rz = right.z * hw;
            float ux = up.x * hw, uy = up.y * hw, uz = up.z * hw;

            float px = p.position.x, py = p.position.y, pz = p.position.z;

            int slot = p.textureSlot;
            float u0 = (slot % TextureAtlas.COLUMNS) * tileU;
            float v0 = (slot / TextureAtlas.COLUMNS) * tileV;
            float u1 = u0 + tileU;
            float v1 = v0 + tileV;

            // bottom-left
            vertices[idx++] = px - rx - ux; vertices[idx++] = py - ry - uy; vertices[idx++] = pz - rz - uz;
            vertices[idx++] = u0; vertices[idx++] = v0;
            vertices[idx++] = alpha;
            // bottom-right
            vertices[idx++] = px + rx - ux; vertices[idx++] = py + ry - uy; vertices[idx++] = pz + rz - uz;
            vertices[idx++] = u1; vertices[idx++] = v0;
            vertices[idx++] = alpha;
            // top-right
            vertices[idx++] = px + rx + ux; vertices[idx++] = py + ry + uy; vertices[idx++] = pz + rz + uz;
            vertices[idx++] = u1; vertices[idx++] = v1;
            vertices[idx++] = alpha;
            // bottom-left (second tri)
            vertices[idx++] = px - rx - ux; vertices[idx++] = py - ry - uy; vertices[idx++] = pz - rz - uz;
            vertices[idx++] = u0; vertices[idx++] = v0;
            vertices[idx++] = alpha;
            // top-right (second tri)
            vertices[idx++] = px + rx + ux; vertices[idx++] = py + ry + uy; vertices[idx++] = pz + rz + uz;
            vertices[idx++] = u1; vertices[idx++] = v1;
            vertices[idx++] = alpha;
            // top-left
            vertices[idx++] = px - rx + ux; vertices[idx++] = py - ry + uy; vertices[idx++] = pz - rz + uz;
            vertices[idx++] = u0; vertices[idx++] = v1;
            vertices[idx++] = alpha;
        }

        if (idx == 0) {
            return;
        }

        glUseProgram(particleProgram);
        glUniform1i(particleAtlasLocation, 0);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, atlasTexture);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buf = stack.mallocFloat(16);
            mvp.get(buf);
            glUniformMatrix4fv(particleMvpLocation, false, buf);
        }

        glDisable(GL_CULL_FACE);
        glDepthMask(false);

        glBindVertexArray(particleVao);
        glBindBuffer(GL_ARRAY_BUFFER, particleVbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_DYNAMIC_DRAW);
        glDrawArrays(GL_TRIANGLES, 0, idx / 6);
        glBindVertexArray(0);

        glDepthMask(true);
        glEnable(GL_CULL_FACE);
    }

    private void renderHotbar(Player player) {
        final int dirtId = 1;
        final int cobblestoneId = 2;
        final int grassId = 3;
        final int leavesId = 5;
        final int sandId = 6;

        int slotCount = 5;
        float slotSize = 54.0f;
        float slotGap = 12.0f;
        float barPadding = 10.0f;
        float frameThickness = 3.0f;
        float marginBottom = 28.0f;
        float swatchInset = 8.0f;

        float barWidth = slotSize * slotCount + slotGap * (slotCount - 1) + barPadding * 2.0f;
        float barHeight = slotSize + barPadding * 2.0f;
        float barX = (window.width() - barWidth) * 0.5f;
        float barY = window.height() - marginBottom - barHeight;

        float[] slotXs = new float[slotCount];
        for (int i = 0; i < slotCount; i++) {
            slotXs[i] = barX + barPadding + i * (slotSize + slotGap);
        }
        float slotY = barY + barPadding;

        int selectedId = player.selectedPlaceBlockId();

        FloatArrayBuilder vertices = new FloatArrayBuilder(1024);
        FloatArrayBuilder textVertices = new FloatArrayBuilder(256);
        FloatArrayBuilder iconVertices = new FloatArrayBuilder(5 * 6 * 4);

        addRect(vertices, barX, barY, barWidth, barHeight, 0.04f, 0.04f, 0.05f, 0.70f);
        addRect(vertices, barX, barY, barWidth, frameThickness, 0.16f, 0.16f, 0.18f, 0.90f);
        addRect(vertices, barX, barY + barHeight - frameThickness, barWidth, frameThickness, 0.16f, 0.16f, 0.18f, 0.90f);
        addRect(vertices, barX, barY, frameThickness, barHeight, 0.16f, 0.16f, 0.18f, 0.90f);
        addRect(vertices, barX + barWidth - frameThickness, barY, frameThickness, barHeight, 0.16f, 0.16f, 0.18f, 0.90f);

        int[] slotIds = {dirtId, cobblestoneId, grassId, leavesId, sandId};
        String[] iconTextures = {"dirt", "cobblestone", "grass_block_top", "oak_leaves", "sand"};

        float tileU = 1.0f / TextureAtlas.COLUMNS;
        float tileV = 1.0f / TextureAtlas.ROWS;

        for (int i = 0; i < slotCount; i++) {
            float sx = slotXs[i];
            addRect(vertices, sx, slotY, slotSize, slotSize, 0.10f, 0.10f, 0.11f, 0.88f);

            int slot = TextureAtlas.getSlot(iconTextures[i]);
            float u0 = (slot % TextureAtlas.COLUMNS) * tileU;
            float v0 = (slot / TextureAtlas.COLUMNS) * tileV;
            float u1 = u0 + tileU;
            float v1 = v0 + tileV;

            addTextQuad(iconVertices,
                    sx + swatchInset,
                    slotY + swatchInset,
                    slotSize - swatchInset * 2.0f,
                    slotSize - swatchInset * 2.0f,
                    u0, v0, u1, v1);
        }

        float selectedX = slotXs[0];
        for (int i = 0; i < slotCount; i++) {
            if (selectedId == slotIds[i]) {
                selectedX = slotXs[i];
                break;
            }
        }

        addRect(vertices, selectedX - 2.0f, slotY - 2.0f, slotSize + 4.0f, 3.0f, 0.95f, 0.88f, 0.52f, 0.98f);
        addRect(vertices, selectedX - 2.0f, slotY + slotSize - 1.0f, slotSize + 4.0f, 3.0f, 0.95f, 0.88f, 0.52f, 0.98f);
        addRect(vertices, selectedX - 2.0f, slotY - 2.0f, 3.0f, slotSize + 4.0f, 0.95f, 0.88f, 0.52f, 0.98f);
        addRect(vertices, selectedX + slotSize - 1.0f, slotY - 2.0f, 3.0f, slotSize + 4.0f, 0.95f, 0.88f, 0.52f, 0.98f);

        for (int i = 0; i < slotCount; i++) {
            drawText(textVertices, String.valueOf(i + 1),
                    slotXs[i] + slotSize * 0.5f - 5.0f,
                    slotY + slotSize + 6.0f,
                    10.0f, 14.0f, 2.0f);
        }

        drawText(textVertices,
                "MODE " + player.gameMode().name(),
                barX,
                barY - 24.0f,
                9.0f,
                12.0f,
                2.0f);
        drawText(textVertices,
                "F6 CYCLE MODE",
                barX + barWidth - 150.0f,
                barY - 24.0f,
                8.0f,
                12.0f,
                1.8f);

        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);

        glUseProgram(hudProgram);
        glUniform2f(hudScreenSizeLocation, window.width(), window.height());
        glBindVertexArray(hudVao);
        glBindBuffer(GL_ARRAY_BUFFER, hudVbo);
        glBufferData(GL_ARRAY_BUFFER, vertices.toArray(), GL_DYNAMIC_DRAW);
        glDrawArrays(GL_TRIANGLES, 0, vertices.size() / 6);

        if (iconVertices.size() > 0) {
            glUseProgram(hudTextureProgram);
            glUniform2f(hudTextureScreenSizeLocation, window.width(), window.height());
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, atlasTexture);
            glBindVertexArray(hudTextureVao);
            glBindBuffer(GL_ARRAY_BUFFER, hudTextureVbo);
            glBufferData(GL_ARRAY_BUFFER, iconVertices.toArray(), GL_DYNAMIC_DRAW);
            glDrawArrays(GL_TRIANGLES, 0, iconVertices.size() / 4);
        }

        renderTextBatch(textVertices);
        glBindVertexArray(0);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
    }

    private void renderFpsCounter() {
        updateFpsCounter();

        String text = "FPS " + displayedFps;
        float glyphHeight = 22.0f;
        float padding = 12.0f;
        float textWidth = measureTextWidth(text, glyphHeight, 14.0f);
        float startX = window.width() - padding - textWidth;
        float startY = padding;

        FloatArrayBuilder textVertices = new FloatArrayBuilder(256);
        drawText(textVertices, text, startX, startY, 14.0f, glyphHeight, 3.0f);

        if (textVertices.size() == 0) {
            return;
        }

        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        renderTextBatch(textVertices);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
    }

    private void renderGraphicsGui(GraphicsSettings settings) {
        float panelWidth = 470.0f;
        float rowHeight = 42.0f;
        float panelHeight = 82.0f + settings.count() * rowHeight;
        float panelX = 32.0f;
        float panelY = 58.0f;

        FloatArrayBuilder vertices = new FloatArrayBuilder(8192);
        FloatArrayBuilder textVertices = new FloatArrayBuilder(4096);
        addRect(vertices, panelX, panelY, panelWidth, panelHeight, 0.02f, 0.025f, 0.032f, 0.84f);
        addRect(vertices, panelX, panelY, panelWidth, 3.0f, 0.42f, 0.62f, 0.72f, 0.95f);
        addRect(vertices, panelX, panelY + panelHeight - 3.0f, panelWidth, 3.0f, 0.12f, 0.15f, 0.17f, 0.95f);
        addRect(vertices, panelX, panelY, 3.0f, panelHeight, 0.12f, 0.15f, 0.17f, 0.95f);
        addRect(vertices, panelX + panelWidth - 3.0f, panelY, 3.0f, panelHeight, 0.12f, 0.15f, 0.17f, 0.95f);

        drawText(textVertices, "GRAPHICS", panelX + 22.0f, panelY + 18.0f, 12.0f, 18.0f, 2.5f);
        drawText(textVertices, "F3 CLOSE", panelX + panelWidth - 126.0f, panelY + 20.0f, 8.0f, 12.0f, 2.0f);

        float rowY = panelY + 62.0f;
        for (int i = 0; i < settings.count(); i++) {
            boolean selected = i == settings.selectedIndex();
            float y = rowY + i * rowHeight;
            if (selected) {
                addRect(vertices, panelX + 12.0f, y - 5.0f, panelWidth - 24.0f, rowHeight - 5.0f, 0.18f, 0.25f, 0.27f, 0.88f);
                addRect(vertices, panelX + 18.0f, y + 8.0f, 8.0f, 12.0f, 0.95f, 0.84f, 0.48f, 1.0f);
            }

            drawText(textVertices, settings.name(i), panelX + 34.0f, y + 4.0f, 9.0f, 14.0f, 2.0f);
            drawText(textVertices, settings.displayValue(i), panelX + 310.0f, y + 4.0f, 9.0f, 14.0f, 2.0f);

            float barX = panelX + 178.0f;
            float barY = y + 10.0f;
            float barWidth = 106.0f;
            addRect(vertices, barX, barY, barWidth, 8.0f, 0.07f, 0.08f, 0.09f, 0.92f);
            addRect(vertices, barX, barY, barWidth * settings.normalizedValue(i), 8.0f, 0.42f, 0.62f, 0.42f, 0.98f);
        }

        drawText(textVertices, "UP DOWN SELECT", panelX + 22.0f, panelY + panelHeight - 28.0f, 7.0f, 10.0f, 1.7f);
        drawText(textVertices, "LEFT RIGHT CHANGE", panelX + 200.0f, panelY + panelHeight - 28.0f, 7.0f, 10.0f, 1.7f);

        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        glUseProgram(hudProgram);
        glUniform2f(hudScreenSizeLocation, window.width(), window.height());
        glBindVertexArray(hudVao);
        glBindBuffer(GL_ARRAY_BUFFER, hudVbo);
        glBufferData(GL_ARRAY_BUFFER, vertices.toArray(), GL_DYNAMIC_DRAW);
        glDrawArrays(GL_TRIANGLES, 0, vertices.size() / 6);
        renderTextBatch(textVertices);
        glBindVertexArray(0);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
    }

    public void renderMenu(GraphicsSettings settings, List<engine.WorldManager.WorldSlot> worlds, int selectedWorldIndex, boolean loadingWorld, boolean namingWorld, String worldNameInput) {
        FloatArrayBuilder vertices = new FloatArrayBuilder(8192);
        FloatArrayBuilder textVertices = new FloatArrayBuilder(4096);
        addRect(vertices, 0.0f, 0.0f, window.width(), window.height(), 0.03f, 0.035f, 0.05f, 1.0f);
        addRect(vertices, 36.0f, 36.0f, window.width() - 72.0f, window.height() - 72.0f, 0.06f, 0.07f, 0.10f, 0.95f);
        drawText(textVertices, "WORLD SELECT", 60.0f, 60.0f, 14.0f, 20.0f, 2.8f);
        drawText(textVertices, "ENTER LOAD   N NEW   F5 REFRESH   G GRAPHICS   ESC QUIT", 60.0f, 94.0f, 7.0f, 10.0f, 1.7f);

        float listX = 60.0f;
        float listY = 138.0f;
        float rowHeight = 34.0f;
        float listWidth = 440.0f;
        addRect(vertices, listX - 12.0f, listY - 12.0f, listWidth, Math.max(140.0f, worlds.size() * rowHeight + 24.0f), 0.08f, 0.09f, 0.12f, 0.90f);

        for (int i = 0; i < worlds.size(); i++) {
            float rowY = listY + i * rowHeight;
            if (i == selectedWorldIndex) {
                addRect(vertices, listX - 8.0f, rowY - 4.0f, listWidth - 20.0f, rowHeight - 2.0f, 0.16f, 0.24f, 0.28f, 0.95f);
            }
            drawText(textVertices, worlds.get(i).name(), listX, rowY, 10.0f, 14.0f, 2.0f);
        }

        if (worlds.isEmpty()) {
            drawText(textVertices, "NO SAVED WORLDS FOUND", listX, listY, 10.0f, 14.0f, 2.0f);
        }

        if (loadingWorld) {
            addRect(vertices, window.width() * 0.5f - 120.0f, window.height() * 0.5f - 36.0f, 240.0f, 72.0f, 0.05f, 0.05f, 0.07f, 0.96f);
            drawText(textVertices, "LOADING...", window.width() * 0.5f - 70.0f, window.height() * 0.5f - 10.0f, 12.0f, 18.0f, 2.4f);
        }

        if (namingWorld) {
            float boxWidth = 520.0f;
            float boxHeight = 150.0f;
            float boxX = window.width() * 0.5f - boxWidth * 0.5f;
            float boxY = window.height() * 0.5f - boxHeight * 0.5f;
            addRect(vertices, 0.0f, 0.0f, window.width(), window.height(), 0.0f, 0.0f, 0.0f, 0.6f);
            addRect(vertices, boxX, boxY, boxWidth, boxHeight, 0.12f, 0.14f, 0.18f, 0.97f);
            addRect(vertices, boxX + 2.0f, boxY + 2.0f, boxWidth - 4.0f, boxHeight - 4.0f, 0.18f, 0.20f, 0.24f, 0.95f);
            drawText(textVertices, "NAME YOUR WORLD", boxX + 28.0f, boxY + 20.0f, 12.0f, 18.0f, 2.4f);
            String displayName = worldNameInput.isEmpty() ? "Type a name..." : worldNameInput;
            float textX = boxX + 28.0f;
            float textY = boxY + 64.0f;
            addRect(vertices, textX - 6.0f, textY - 6.0f, 460.0f, 38.0f, 0.08f, 0.09f, 0.12f, 0.90f);
            drawText(textVertices, displayName + (worldNameInput.isEmpty() ? "" : "_"), textX, textY, 10.0f, 14.0f, 2.0f);
            drawText(textVertices, "ENTER CONFIRM   ESC CANCEL", boxX + 28.0f, boxY + 110.0f, 7.0f, 10.0f, 1.7f);
        }

        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        glUseProgram(hudProgram);
        glUniform2f(hudScreenSizeLocation, window.width(), window.height());
        glBindVertexArray(hudVao);
        glBindBuffer(GL_ARRAY_BUFFER, hudVbo);
        glBufferData(GL_ARRAY_BUFFER, vertices.toArray(), GL_DYNAMIC_DRAW);
        glDrawArrays(GL_TRIANGLES, 0, vertices.size() / 6);
        renderTextBatch(textVertices);
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

    private void drawText(FloatArrayBuilder vertices, String text, float x, float y, float glyphWidth, float glyphHeight, float thickness) {
        float gap = Math.max(1.0f, glyphWidth * 0.12f);
        float cursorX = x;

        for (int i = 0; i < text.length(); i++) {
            TextGlyph glyph = lookupTextGlyph(text.charAt(i));
            if (glyph == null) {
                continue;
            }

            if (glyph.width > 0.0f && glyph.height > 0.0f) {
                float scale = glyphHeight / Math.max(1.0f, glyph.height);
                float drawX = cursorX + glyph.xOffset * scale;
                float drawY = y - glyph.yOffset * scale;
                addTextQuad(vertices, drawX, drawY, glyph.width * scale, glyph.height * scale, glyph.u0, glyph.v0, glyph.u1, glyph.v1);
            }

            float scale = glyph.height > 0.0f ? glyphHeight / Math.max(1.0f, glyph.height) : 1.0f;
            float advance = Math.max(glyph.advance * scale, glyphWidth * 0.85f);
            cursorX += advance + gap;
        }
    }

    private float measureTextWidth(String text, float glyphHeight, float glyphWidth) {
        float gap = Math.max(1.0f, glyphWidth * 0.12f);
        float width = 0.0f;

        for (int i = 0; i < text.length(); i++) {
            TextGlyph glyph = lookupTextGlyph(text.charAt(i));
            if (glyph == null) {
                continue;
            }
            float scale = glyph.height > 0.0f ? glyphHeight / Math.max(1.0f, glyph.height) : 1.0f;
            width += Math.max(glyph.advance * scale, glyphWidth * 0.85f) + gap;
        }

        return Math.max(0.0f, width - gap);
    }

    private TextGlyph lookupTextGlyph(char glyph) {
        if (glyph < textGlyphs.length && textGlyphs[glyph] != null) {
            return textGlyphs[glyph];
        }
        if (glyph != '?' && '?' < textGlyphs.length) {
            return textGlyphs['?'];
        }
        return null;
    }

    private void addTextQuad(FloatArrayBuilder vertices, float left, float top, float width, float height, float u0, float v0, float u1, float v1) {
        addTextVertex(vertices, left, top, u0, v0);
        addTextVertex(vertices, left, top + height, u0, v1);
        addTextVertex(vertices, left + width, top + height, u1, v1);

        addTextVertex(vertices, left, top, u0, v0);
        addTextVertex(vertices, left + width, top + height, u1, v1);
        addTextVertex(vertices, left + width, top, u1, v0);
    }

    private void addTextVertex(FloatArrayBuilder vertices, float x, float y, float u, float v) {
        vertices.add(x);
        vertices.add(y);
        vertices.add(u);
        vertices.add(v);
    }

    private void renderTextBatch(FloatArrayBuilder vertices) {
        if (vertices.size() == 0) {
            return;
        }

        glUseProgram(textProgram);
        glUniform2f(textScreenSizeLocation, window.width(), window.height());
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textTexture);
        glBindVertexArray(textVao);
        glBindBuffer(GL_ARRAY_BUFFER, textVbo);
        glBufferData(GL_ARRAY_BUFFER, vertices.toArray(), GL_DYNAMIC_DRAW);
        glDrawArrays(GL_TRIANGLES, 0, vertices.size() / 4);
        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    private int createTextTexture() {
        Font font = loadTextFont().deriveFont(Font.PLAIN, 64.0f);
        BufferedImage scratch = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D scratchGraphics = scratch.createGraphics();
        scratchGraphics.setFont(font);
        scratchGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        scratchGraphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        FontRenderContext fontRenderContext = scratchGraphics.getFontRenderContext();
        java.awt.FontMetrics metrics = scratchGraphics.getFontMetrics(font);

        final int firstChar = 32;
        final int lastChar = 126;
        final int columns = 16;
        final int padding = 10;

        int cellWidth = 0;
        int cellHeight = metrics.getHeight() + padding * 2;
        for (int ch = firstChar; ch <= lastChar; ch++) {
            GlyphVector glyphVector = font.createGlyphVector(fontRenderContext, new char[]{(char) ch});
            Rectangle bounds = glyphVector.getPixelBounds(fontRenderContext, 0.0f, 0.0f);
            cellWidth = Math.max(cellWidth, Math.max(bounds.width, metrics.charWidth(ch)) + padding * 2);
        }

        int count = lastChar - firstChar + 1;
        int rows = (count + columns - 1) / columns;
        int atlasWidth = cellWidth * columns;
        int atlasHeight = cellHeight * rows;
        BufferedImage atlasImage = new BufferedImage(atlasWidth, atlasHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D atlasGraphics = atlasImage.createGraphics();
        atlasGraphics.setFont(font);
        atlasGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        atlasGraphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        atlasGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        for (int ch = firstChar; ch <= lastChar; ch++) {
            int index = ch - firstChar;
            int cellX = (index % columns) * cellWidth;
            int cellY = (index / columns) * cellHeight;
            char character = (char) ch;
            GlyphVector glyphVector = font.createGlyphVector(fontRenderContext, new char[]{character});
            Rectangle bounds = glyphVector.getPixelBounds(fontRenderContext, 0.0f, 0.0f);

            if (character != ' ') {
                atlasGraphics.drawGlyphVector(glyphVector, cellX + padding - bounds.x, cellY + padding - bounds.y);
            }

            TextGlyph glyph = new TextGlyph();
            glyph.xOffset = bounds.x;
            glyph.yOffset = bounds.y;
            glyph.width = bounds.width;
            glyph.height = bounds.height;
            glyph.advance = Math.max(1.0f, glyphVector.getGlyphMetrics(0).getAdvance());
            glyph.u0 = (float) (cellX + padding) / atlasWidth;
            glyph.v0 = (float) (cellY + padding) / atlasHeight;
            glyph.u1 = (float) (cellX + padding + Math.max(bounds.width, 1)) / atlasWidth;
            glyph.v1 = (float) (cellY + padding + Math.max(bounds.height, 1)) / atlasHeight;
            if (character == ' ') {
                glyph.width = 0.0f;
                glyph.height = 0.0f;
            }
            textGlyphs[ch] = glyph;
        }

        atlasGraphics.dispose();
        scratchGraphics.dispose();

        int[] pixels = atlasImage.getRGB(0, 0, atlasWidth, atlasHeight, null, 0, atlasWidth);
        ByteBuffer pixelBuffer = MemoryUtil.memAlloc(atlasWidth * atlasHeight * 4);
        try {
            for (int pixel : pixels) {
                pixelBuffer.put((byte) ((pixel >> 16) & 0xFF));
                pixelBuffer.put((byte) ((pixel >> 8) & 0xFF));
                pixelBuffer.put((byte) (pixel & 0xFF));
                pixelBuffer.put((byte) ((pixel >> 24) & 0xFF));
            }
            pixelBuffer.flip();

            int texture = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, texture);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
            glTexImage2D(
                    GL_TEXTURE_2D,
                    0,
                    GL_RGBA8,
                    atlasWidth,
                    atlasHeight,
                    0,
                    GL_RGBA,
                    GL_UNSIGNED_BYTE,
                    pixelBuffer
            );
            glBindTexture(GL_TEXTURE_2D, 0);
            return texture;
        } finally {
            MemoryUtil.memFree(pixelBuffer);
        }
    }

    private Font loadTextFont() {
        Path fontPath = Path.of("assets/fonts/minecraft.ttf");
        if (Files.exists(fontPath)) {
            try (InputStream inputStream = Files.newInputStream(fontPath)) {
                return Font.createFont(Font.TRUETYPE_FONT, inputStream);
            } catch (IOException | FontFormatException exception) {
                throw new IllegalStateException("Failed to load text font from " + fontPath, exception);
            }
        }
        return new Font("Arial", Font.PLAIN, 64);
    }

    private int createTextProgram() {
        String vertexShader = """
                #version 330 core
                layout(location = 0) in vec2 aPosition;
                layout(location = 1) in vec2 aUv;

                uniform vec2 uScreenSize;

                out vec2 vUv;

                void main() {
                    vec2 ndc = vec2(
                            (aPosition.x / uScreenSize.x) * 2.0 - 1.0,
                            1.0 - (aPosition.y / uScreenSize.y) * 2.0
                    );
                    vUv = aUv;
                    gl_Position = vec4(ndc, 0.0, 1.0);
                }
                """;

        String fragmentShader = """
                #version 330 core
                uniform sampler2D uFont;

                in vec2 vUv;
                out vec4 fragColor;

                void main() {
                    vec4 texel = texture(uFont, vUv);
                    fragColor = texel;
                }
                """;

        return createProgram(vertexShader, fragmentShader);
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

    private void syncChunkMeshes(World world, Player player, GraphicsSettings graphicsSettings) {
        Vector3f cameraPos = player.camera().position();
        int cameraChunkX = Math.floorDiv((int) Math.floor(cameraPos.x), Chunk.SIZE);
        int cameraChunkY = Math.floorDiv((int) Math.floor(cameraPos.y), Chunk.SIZE);
        int cameraChunkZ = Math.floorDiv((int) Math.floor(cameraPos.z), Chunk.SIZE);
        int buildBudget = graphicsSettings.chunkBuildBudget();
        int keepRadius = graphicsSettings.renderDistanceChunks();

        meshes.entrySet().removeIf(entry -> {
            ChunkPos position = entry.getKey();
            if (world.getChunk(position) != null) {
                return false;
            }
            entry.getValue().destroy();
            return true;
        });

        for (Chunk chunk : world.getLoadedChunks()) {
            ChunkPos position = chunk.position();
            Mesh mesh = meshes.computeIfAbsent(position, ignored -> new Mesh());
            if (!chunk.isDirty() && mesh.vao != 0) {
                continue;
            }

            if (buildBudget <= 0) {
                continue;
            }

            ChunkMeshData meshData = ChunkMeshBuilder.build(chunk, world);
            mesh.upload(meshData);
            chunk.clearDirty();
            buildBudget--;
        }
    }

    private Vector3f sunDirection(TickSystem ticks) {
        float angle = ticks.getSunAngleRadians();
        Vector3f dir = new Vector3f(
                (float) Math.cos(angle),
                (float) Math.sin(angle),
                (float) Math.cos(angle) * 0.3f
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
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
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
                "assets/cubic/" + baseName + ".png",
                "assets/cubic/" + baseName + ".jpg",
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

    private void createHudTextureGeometry() {
        hudTextureVao = glGenVertexArrays();
        hudTextureVbo = glGenBuffers();
        glBindVertexArray(hudTextureVao);
        glBindBuffer(GL_ARRAY_BUFFER, hudTextureVbo);
        glBufferData(GL_ARRAY_BUFFER, 5L * 6L * 4L * Float.BYTES, GL_DYNAMIC_DRAW);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0L);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2L * Float.BYTES);
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
                layout(location = 3) in float aAo;

                uniform mat4 uMvp;
                uniform mat4 uLightMvp[4];

                out vec2 vUv;
                out vec3 vNormal;
                out vec3 vWorldPos;
                out float vAo;
                out vec4 vShadowCoord[4];

                void main() {
                    vUv = aUv;
                    vNormal = aNormal;
                    vWorldPos = aPosition;
                    vAo = aAo;
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
                in float vAo;
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
                uniform float uExposure;
                uniform float uAmbientBoost;
                uniform float uContrast;
                uniform float uShadowStrength;
                uniform int uShadowFilterRadius;
                uniform int uRealisticLighting;
                uniform float uFogDensity;

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
                    float bias = max(0.00035, mix(0.00320, 0.00055, ndotl));
                    float currentDepth = projected.z - bias;
                    float visibility = 0.0;
                    float sampleCount = 0.0;

                    for (int y = -2; y <= 2; y++) {
                        for (int x = -2; x <= 2; x++) {
                            if (abs(x) > uShadowFilterRadius || abs(y) > uShadowFilterRadius) {
                                continue;
                            }
                            vec2 offset = vec2(float(x), float(y)) * uShadowTexelSize * 1.35;
                            vec2 uv = projected.xy + offset;
                            if (uv.x < 0.0 || uv.x > 1.0 || uv.y < 0.0 || uv.y > 1.0) {
                                visibility += 1.0;
                                sampleCount += 1.0;
                                continue;
                            }
                            float closestDepth = texture(uShadowMap, vec3(uv, float(cascade))).r;
                            visibility += currentDepth <= closestDepth ? 1.0 : 0.48;
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
                    vec3 albedoLinear = uRealisticLighting == 1 ? pow(albedo.rgb, vec3(2.2)) : albedo.rgb;

                    vec3 normal = normalize(vNormal);
                    vec3 lightDir = normalize(-uLightDir);
                    float ndotl = max(dot(normal, lightDir), 0.0);
                    float diffuse = uRealisticLighting == 1 ? pow(ndotl, 1.18) : ndotl;
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
                    visibility = mix(1.0, visibility, uShadowStrength);

                    vec3 probeUv = clamp((vWorldPos - uProbeOrigin) / uProbeExtent, vec3(0.0), vec3(0.999));
                    vec3 probeLight = texture(uProbeVolume, probeUv).rgb * uAmbientBoost;

                    vec3 skyTint = vec3(0.45, 0.58, 0.78);
                    vec3 groundBounce = vec3(0.38, 0.30, 0.20) * (1.0 - max(normal.y, 0.0)) * 0.12;
                    float skyWrap = clamp(normal.y * 0.5 + 0.5, 0.0, 1.0);
                    vec3 ambientWrap = skyTint * skyWrap * 0.045 + groundBounce * 0.72;

                    float direct = diffuse * visibility * uLightIntensity;
                    float faceShade = 0.86 + normal.y * 0.12 + abs(normal.x) * 0.035;
                    vec3 lighting = (probeLight + ambientWrap) * mix(0.58, 1.0, vAo) + uLightColor * direct;

                    vec3 color = albedoLinear * lighting * faceShade;
                    if (uRealisticLighting == 1) {
                        vec3 fogColor = mix(vec3(0.44, 0.54, 0.66), uLightColor, 0.12);
                        float fogAmount = 1.0 - exp(-viewDistance * uFogDensity * 0.0033);
                        color = mix(color, fogColor * 0.36, clamp(fogAmount, 0.0, 0.16));
                        color *= uExposure;
                        color = color / (color + vec3(1.0));
                        color = pow(color, vec3(1.0 / 2.2));
                        color = (color - 0.5) * uContrast + 0.5;
                    }

                    fragColor = vec4(clamp(color, vec3(0.0), vec3(1.0)), albedo.a);
                }
                """;

        return createProgram(vertexShader, fragmentShader);
    }

    private int createVolumetricProgram() {
        String vertexShader = """
                #version 330 core
                layout(location = 0) in vec2 aPosition;
                out vec2 vPosition;

                void main() {
                    vPosition = aPosition;
                    gl_Position = vec4(aPosition, 0.997, 1.0);
                }
                """;

        String fragmentShader = """
                #version 330 core
                in vec2 vPosition;

                uniform vec3 uCameraPos;
                uniform vec3 uViewRight;
                uniform vec3 uViewUp;
                uniform vec3 uViewForward;
                uniform vec3 uLightDir;
                uniform vec3 uLightColor;
                uniform float uLightIntensity;
                uniform sampler2DArray uShadowMap;
                uniform mat4 uLightMvp[4];
                uniform float uCascadeSplits[4];
                uniform float uShadowTexelSize;
                uniform float uHScreenScale;
                uniform float uVScreenScale;

                out vec4 fragColor;

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

                float shadowVisibility(vec3 worldPos, float viewDistance) {
                    int cascade = selectCascade(viewDistance);
                    vec4 shadowCoord = uLightMvp[cascade] * vec4(worldPos, 1.0);
                    vec3 projected = shadowCoord.xyz / max(shadowCoord.w, 0.0001);
                    projected = projected * 0.5 + 0.5;
                    if (projected.x < 0.0 || projected.x > 1.0 || projected.y < 0.0 || projected.y > 1.0 || projected.z > 1.0) {
                        return 1.0;
                    }

                    float currentDepth = projected.z - 0.0014;
                    float visibility = 0.0;
                    for (int y = -1; y <= 1; y++) {
                        for (int x = -1; x <= 1; x++) {
                            vec2 uv = projected.xy + vec2(float(x), float(y)) * uShadowTexelSize * 2.0;
                            if (uv.x < 0.0 || uv.x > 1.0 || uv.y < 0.0 || uv.y > 1.0) {
                                visibility += 1.0;
                            } else {
                                float closestDepth = texture(uShadowMap, vec3(uv, float(cascade))).r;
                                visibility += currentDepth <= closestDepth ? 1.0 : 0.08;
                            }
                        }
                    }
                    return visibility / 9.0;
                }

                void main() {
                    vec3 rayDir = normalize(uViewForward + uViewRight * vPosition.x * uHScreenScale + uViewUp * vPosition.y * uVScreenScale);
                    vec3 lightToScene = normalize(-uLightDir);
                    float forwardScatter = pow(max(dot(rayDir, lightToScene), 0.0), 8.0);
                    float sideScatter = pow(max(dot(rayDir, lightToScene), 0.0), 1.4) * 0.18;
                    float phase = forwardScatter + sideScatter;

                    if (phase <= 0.002) {
                        discard;
                    }

                    float accumulated = 0.0;
                    float transmittance = 1.0;
                    float stepLength = 5.0;
                    float start = 2.0;

                    for (int i = 0; i < 24; i++) {
                        float dist = start + (float(i) + 0.5) * stepLength;
                        vec3 samplePos = uCameraPos + rayDir * dist;
                        float heightFade = smoothstep(-10.0, 18.0, samplePos.y) * (1.0 - smoothstep(120.0, 180.0, samplePos.y));
                        float distanceFade = exp(-dist * 0.020);
                        float visibility = shadowVisibility(samplePos, dist);
                        float density = 0.030 * heightFade * distanceFade;
                        float contribution = density * visibility * transmittance;
                        accumulated += contribution;
                        transmittance *= exp(-density * 0.55);
                    }

                    float alpha = clamp(accumulated * phase * uLightIntensity, 0.0, 0.22);
                    vec3 color = uLightColor * (1.2 + phase * 1.8);
                    fragColor = vec4(color, alpha);
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
                uniform vec3 uCameraPos;
                uniform float uCloudTime;
                uniform vec3 uViewRight;
                uniform vec3 uViewUp;
                uniform vec3 uViewForward;
                uniform float uSkySaturation;
                uniform float uHScreenScale;
                uniform float uVScreenScale;

                out vec4 fragColor;

                float hash(vec2 p) {
                    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
                }

                float noise(vec2 p) {
                    vec2 i = floor(p);
                    vec2 f = fract(p);
                    vec2 u = f * f * (3.0 - 2.0 * f);
                    return mix(
                            mix(hash(i), hash(i + vec2(1.0, 0.0)), u.x),
                            mix(hash(i + vec2(0.0, 1.0)), hash(i + vec2(1.0, 1.0)), u.x),
                            u.y
                    );
                }

                float fbm(vec2 p) {
                    float value = 0.0;
                    float amplitude = 0.55;
                    mat2 m = mat2(1.6, -1.2, 1.2, 1.6);
                    for (int i = 0; i < 4; i++) {
                        value += noise(p) * amplitude;
                        p = m * p + vec2(17.7, 9.2);
                        amplitude *= 0.50;
                    }
                    return value;
                }

                float hash3(vec3 p) {
                    return fract(sin(dot(p, vec3(127.1, 311.7, 74.7))) * 43758.5453);
                }

                float noise3(vec3 p) {
                    vec3 i = floor(p);
                    vec3 f = fract(p);
                    vec3 u = f * f * (3.0 - 2.0 * f);

                    float x00 = mix(hash3(i + vec3(0.0, 0.0, 0.0)), hash3(i + vec3(1.0, 0.0, 0.0)), u.x);
                    float x10 = mix(hash3(i + vec3(0.0, 1.0, 0.0)), hash3(i + vec3(1.0, 1.0, 0.0)), u.x);
                    float x01 = mix(hash3(i + vec3(0.0, 0.0, 1.0)), hash3(i + vec3(1.0, 0.0, 1.0)), u.x);
                    float x11 = mix(hash3(i + vec3(0.0, 1.0, 1.0)), hash3(i + vec3(1.0, 1.0, 1.0)), u.x);
                    float y0 = mix(x00, x10, u.y);
                    float y1 = mix(x01, x11, u.y);
                    return mix(y0, y1, u.z);
                }

                float fbm3(vec3 p) {
                    float value = 0.0;
                    float amplitude = 0.56;
                    mat3 m = mat3(
                        0.00,  0.80,  0.60,
                       -0.80,  0.36, -0.48,
                       -0.60, -0.48,  0.64
                    );
                    for (int i = 0; i < 5; i++) {
                        value += noise3(p) * amplitude;
                        p = m * p * 2.04 + vec3(19.1, 7.7, 13.3);
                        amplitude *= 0.50;
                    }
                    return value;
                }

                float cloudDensity(vec3 worldPos) {
                    const float cloudBottom = 62.0;
                    const float cloudTop = 178.0;
                    float height01 = clamp((worldPos.y - cloudBottom) / (cloudTop - cloudBottom), 0.0, 1.0);
                    float flatBase = smoothstep(0.00, 0.12, height01);
                    float softTop = 1.0 - smoothstep(0.58, 1.0, height01);
                    float billowRise = smoothstep(0.06, 0.48, height01);
                    float heightShape = flatBase * softTop;

                    vec3 wind = vec3(uCloudTime * 0.0024, 0.0, uCloudTime * 0.0011);
                    vec2 sheet = worldPos.xz * 0.0045 + wind.xz;
                    float cover = fbm(sheet);
                    float islands = smoothstep(0.42, 0.64, cover);

                    vec3 broadPos = vec3(worldPos.x * 0.014, worldPos.y * 0.030, worldPos.z * 0.014) + wind * 4.0;
                    vec3 detailPos = broadPos * 2.8 + vec3(31.0, 11.0, 47.0);
                    float billow = fbm3(broadPos);
                    float detail = fbm3(detailPos);
                    float puffs = smoothstep(0.42, 0.68, billow + billowRise * 0.16);
                    float erosion = smoothstep(0.40, 0.85, detail);
                    float density = islands * puffs - erosion * 0.24;
                    return clamp(density, 0.0, 1.0) * heightShape;
                }

                vec3 renderClouds(vec3 rayDir, vec3 sky, float dayAmount) {
                    if (rayDir.y <= 0.018 || dayAmount <= 0.04) {
                        return sky;
                    }

                    const float cloudBottom = 62.0;
                    const float cloudTop = 178.0;
                    float enterDistance = max((cloudBottom - uCameraPos.y) / rayDir.y, 0.0);
                    float exitDistance = (cloudTop - uCameraPos.y) / rayDir.y;
                    if (exitDistance <= enterDistance) {
                        return sky;
                    }

                    exitDistance = min(exitDistance, 1900.0);
                    float stepLength = (exitDistance - enterDistance) / 24.0;
                    float dither = fract(sin(dot(vPosition.xy + uCloudTime * 0.01, vec2(12.9898, 78.233))) * 43758.5453);
                    float transmittance = 1.0;
                    float alpha = 0.0;
                    vec3 cloudColor = vec3(0.0);
                    vec3 sunDir = normalize(-uSunDir);

                    for (int i = 0; i < 24; i++) {
                        float distanceAlongRay = enterDistance + (float(i) + dither) * stepLength;
                        vec3 samplePos = uCameraPos + rayDir * distanceAlongRay;
                        float density = cloudDensity(samplePos);
                        if (density > 0.001) {
                            float shadow = 0.0;
                            shadow += cloudDensity(samplePos + sunDir * 18.0);
                            shadow += cloudDensity(samplePos + sunDir * 44.0) * 0.65;
                            shadow += cloudDensity(samplePos + sunDir * 84.0) * 0.35;
                            shadow = clamp(shadow * 0.44, 0.0, 1.0);
                            float silverLining = pow(max(dot(rayDir, sunDir), 0.0), 7.0) * (1.0 - shadow);
                            float forwardLight = 0.42 + 0.58 * max(dot(rayDir, sunDir), 0.0);
                            float topLight = smoothstep(cloudBottom, cloudTop, samplePos.y);
                            vec3 shadeColor = vec3(0.38, 0.39, 0.40);
                            vec3 sunColor = vec3(1.0, 0.96, 0.84);
                            vec3 litCloud = mix(shadeColor, sunColor, clamp(forwardLight * 0.46 + topLight * 0.42 + silverLining * 0.46 - shadow * 0.36, 0.0, 1.0));
                            float sampleAlpha = density * stepLength * 0.016 * dayAmount;
                            sampleAlpha = clamp(sampleAlpha, 0.0, 0.25);
                            cloudColor += litCloud * sampleAlpha * transmittance;
                            alpha += sampleAlpha * transmittance;
                            transmittance *= exp(-sampleAlpha * 2.35);
                        }
                    }

                    float horizonFade = smoothstep(0.018, 0.08, rayDir.y);
                    alpha = clamp(alpha * horizonFade, 0.0, 0.88);
                    vec3 resolvedCloud = cloudColor / max(alpha, 0.001);
                    return mix(sky, resolvedCloud, alpha);
                }

                void main() {
                    vec3 viewDir = normalize(uViewForward + uViewRight * vPosition.x * uHScreenScale + uViewUp * vPosition.y * uVScreenScale);
                    float t = clamp(viewDir.y * 0.5 + 0.5, 0.0, 1.0);
                    vec3 sky = mix(uBottomColor, uTopColor, smoothstep(0.0, 1.0, t));
                    float horizon = pow(1.0 - clamp(abs(viewDir.y), 0.0, 1.0), 3.0);
                    float dayAmount = clamp(length(uSunColor) * 0.55, 0.0, 1.0);
                    sky += vec3(0.28, 0.22, 0.16) * horizon * dayAmount;

                    if (dayAmount < 0.22) {
                        vec2 starUv = normalize(viewDir).xz / max(0.08, abs(viewDir.y));
                        float star = step(0.996, hash(floor(starUv * 180.0)));
                        sky += vec3(0.60, 0.68, 0.90) * star * (0.22 - dayAmount) * 2.4;
                    }

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

                    vec3 gray = vec3(dot(sky, vec3(0.299, 0.587, 0.114)));
                    sky = mix(gray, sky, uSkySaturation);
                    sky = renderClouds(viewDir, sky, dayAmount);
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

    private int createParticleProgram() {
        String vertexShader = """
                #version 330 core
                layout(location = 0) in vec3 aPosition;
                layout(location = 1) in vec2 aUv;
                layout(location = 2) in float aAlpha;

                uniform mat4 uMvp;

                out vec2 vUv;
                out float vAlpha;

                void main() {
                    vUv = aUv;
                    vAlpha = aAlpha;
                    gl_Position = uMvp * vec4(aPosition, 1.0);
                }
                """;

        String fragmentShader = """
                #version 330 core
                in vec2 vUv;
                in float vAlpha;
                out vec4 fragColor;

                uniform sampler2D uAtlas;

                void main() {
                    vec4 color = texture(uAtlas, vUv);
                    fragColor = vec4(color.rgb, color.a * vAlpha);
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

    private int createHudTextureProgram() {
        String vertexShader = """
                #version 330 core
                layout(location = 0) in vec2 aPosition;
                layout(location = 1) in vec2 aUv;

                uniform vec2 uScreenSize;

                out vec2 vUv;

                void main() {
                    vec2 ndc = vec2(
                            (aPosition.x / uScreenSize.x) * 2.0 - 1.0,
                            1.0 - (aPosition.y / uScreenSize.y) * 2.0
                    );
                    vUv = aUv;
                    gl_Position = vec4(ndc, 0.0, 1.0);
                }
                """;

        String fragmentShader = """
                #version 330 core
                uniform sampler2D uTex;

                in vec2 vUv;
                out vec4 fragColor;

                void main() {
                    fragColor = texture(uTex, vUv);
                }
                """;

        int program = createProgram(vertexShader, fragmentShader);
        glUseProgram(program);
        glUniform1i(glGetUniformLocation(program, "uTex"), 0);
        glUseProgram(0);
        return program;
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
                glVertexAttribPointer(0, 3, GL_FLOAT, false, 9 * Float.BYTES, 0L);
                glEnableVertexAttribArray(0);
                glVertexAttribPointer(1, 2, GL_FLOAT, false, 9 * Float.BYTES, 3L * Float.BYTES);
                glEnableVertexAttribArray(1);
                glVertexAttribPointer(2, 3, GL_FLOAT, false, 9 * Float.BYTES, 5L * Float.BYTES);
                glEnableVertexAttribArray(2);
                glVertexAttribPointer(3, 1, GL_FLOAT, false, 9 * Float.BYTES, 8L * Float.BYTES);
                glEnableVertexAttribArray(3);
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

    private static final class TextGlyph {
        private float u0;
        private float v0;
        private float u1;
        private float v1;
        private float xOffset;
        private float yOffset;
        private float width;
        private float height;
        private float advance;
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
