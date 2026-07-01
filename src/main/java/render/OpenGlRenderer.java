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
import static org.lwjgl.opengl.GL11C.GL_RED;
import static org.lwjgl.opengl.GL11C.GL_REPEAT;
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
import static org.lwjgl.opengl.GL11C.glClearColor;
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
import static org.lwjgl.opengl.GL13C.GL_TEXTURE7;
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
import static org.lwjgl.opengl.GL15C.glBufferSubData;
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
import static org.lwjgl.opengl.GL20C.glUniform3i;
import static org.lwjgl.opengl.GL20C.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20C.glUseProgram;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30C.GL_DEPTH_ATTACHMENT;
import static org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER_COMPLETE;
import static org.lwjgl.opengl.GL30C.GL_RGB16F;
import static org.lwjgl.opengl.GL30C.GL_RGBA16F;
import static org.lwjgl.opengl.GL30C.GL_TEXTURE_2D_ARRAY;
import static org.lwjgl.opengl.GL30C.glBindFramebuffer;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.GL_DRAW_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30C.GL_READ_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30C.glBlitFramebuffer;
import static org.lwjgl.opengl.GL30C.glCheckFramebufferStatus;
import static org.lwjgl.opengl.GL30C.glDeleteFramebuffers;
import static org.lwjgl.opengl.GL30C.glDeleteRenderbuffers;
import static org.lwjgl.opengl.GL30C.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30C.glFramebufferTexture2D;
import static org.lwjgl.opengl.GL30C.glFramebufferTextureLayer;
import static org.lwjgl.opengl.GL30C.glGenFramebuffers;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;
import static org.lwjgl.opengl.GL30C.glBindBufferBase;
import static org.lwjgl.opengl.GL42C.GL_WRITE_ONLY;
import static org.lwjgl.opengl.GL42C.glBindImageTexture;
import static org.lwjgl.opengl.GL43C.GL_ALL_BARRIER_BITS;
import static org.lwjgl.opengl.GL43C.GL_COMPUTE_SHADER;
import static org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BUFFER;
import static org.lwjgl.opengl.GL43C.glDispatchCompute;
import static org.lwjgl.opengl.GL43C.glMemoryBarrier;
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
    private static final int PROBE_UPDATES_PER_FRAME = 1600;

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
        particleSystem.setGpuMode(true);
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
    private int skyViewRightLocation;
    private int skyViewUpLocation;
    private int skyViewForwardLocation;
    private int skySaturationLocation;
    private int skyHScreenScaleLocation;
    private int skyVScreenScaleLocation;

    private int froxelAccumProgram;
    private int froxelCompositeProgram;
    private int[] froxelTex = new int[2];
    private int froxelActiveIndex;
    private int froxelWidth;
    private int froxelHeight;
    private int froxelDepth;
    private int currentFroxelQuality = -1;
    private int volumetricFrameCount;

    private int froxelGridSizeLocation;
    private int froxelCameraPosLocation;
    private int froxelViewRightLocation;
    private int froxelViewUpLocation;
    private int froxelViewForwardLocation;
    private int froxelNearFarLocation;
    private int froxelScreenScaleLocation;
    private int froxelLightDirLocation;
    private int froxelLightColorLocation;
    private int froxelLightIntensityLocation;
    private int froxelShadowMapLocation;
    private final int[] froxelLightMvpLocations = new int[CASCADE_COUNT];
    private int froxelCascadeSplitsLocation;
    private int froxelShadowTexelSizeLocation;

    private int froxelCompVolumeLocation;
    private int froxelCompPrevVolumeLocation;
    private int froxelCompDepthLocation;
    private int froxelCompCameraPosLocation;
    private int froxelCompViewRightLocation;
    private int froxelCompViewUpLocation;
    private int froxelCompViewForwardLocation;
    private int froxelCompNearFarLocation;
    private int froxelCompScreenScaleLocation;
    private int froxelCompPrevViewProjLocation;
    private int froxelCompGridSizeLocation;

    private final Matrix4f prevViewProj = new Matrix4f();

    private int outlineProgram;
    private int outlineVao;
    private int outlineVbo;
    private int outlineMvpLocation;

    private int cloudProgram;
    private int cloudCameraPosLocation;
    private int cloudViewRightLocation;
    private int cloudViewUpLocation;
    private int cloudViewForwardLocation;
    private int cloudHScreenScaleLocation;
    private int cloudVScreenScaleLocation;
    private int cloudSunDirLocation;
    private int cloudSunColorLocation;
    private int cloudTimeLocation;
    private int cloudCoverageLocation;
    private int cloudBaseAltLocation;
    private int cloudTopAltLocation;
    private int cloudWindDirLocation;
    private int cloudWindSpeedLocation;

    private int cloudFbo;
    private int cloudColorTex;
    private int cloudBackupFbo;
    private int cloudBackupTex;

    private int compositeProgram;
    private int compositeSkyTexLocation;
    private int compositeCloudTexLocation;

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

    private int particleComputeProgram;
    private int particleComputeDeltaLocation;
    private int particleRenderProgram;
    private int particleRenderMvpLocation;
    private int particleRenderCameraRightLocation;
    private int particleRenderCameraUpLocation;
    private int particleRenderTileULocation;
    private int particleRenderTileVLocation;
    private int particleRenderLightDirLocation;
    private int particleRenderLightColorLocation;
    private int particleRenderLightIntensityLocation;
    private int particleSsbo;
    private int gpuParticleCount;
    private static final int MAX_GPU_PARTICLES = 8192;
    private static final int PARTICLE_STRIDE = 12;
    private double lastParticleFrameTime;

    private int textProgram;
    private int textVao;
    private int textVbo;
    private int textTexture;
    private int textScreenSizeLocation;
    private int textAlphaLocation;
    private float textAlpha = 1.0f;
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
    private int probeShiftCooldown;

    private double fpsSampleTime = GLFW.glfwGetTime();
    private int fpsSampleFrames;
    private int displayedFps;

    private int mainFbo;
    private int mainColorTex;
    private int mainDepthRbo;

    private int outputProgram;
    private int outputTexLocation;
    private int outputUseTonemapLocation;
    private int outputContrastLocation;
    private int outputExposureLocation;

    private final Matrix4f currentProj = new Matrix4f();
    private final Matrix4f currentView = new Matrix4f();
    private final Matrix4f jitteredViewProj = new Matrix4f();

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
        skyViewRightLocation = glGetUniformLocation(skyProgram, "uViewRight");
        skyViewUpLocation = glGetUniformLocation(skyProgram, "uViewUp");
        skyViewForwardLocation = glGetUniformLocation(skyProgram, "uViewForward");
        skySaturationLocation = glGetUniformLocation(skyProgram, "uSkySaturation");
        skyHScreenScaleLocation = glGetUniformLocation(skyProgram, "uHScreenScale");
        skyVScreenScaleLocation = glGetUniformLocation(skyProgram, "uVScreenScale");
        createSkyGeometry();

        froxelAccumProgram = createFroxelAccumProgram();
        froxelGridSizeLocation = glGetUniformLocation(froxelAccumProgram, "uGridSize");
        froxelCameraPosLocation = glGetUniformLocation(froxelAccumProgram, "uCameraPos");
        froxelViewRightLocation = glGetUniformLocation(froxelAccumProgram, "uViewRight");
        froxelViewUpLocation = glGetUniformLocation(froxelAccumProgram, "uViewUp");
        froxelViewForwardLocation = glGetUniformLocation(froxelAccumProgram, "uViewForward");
        froxelNearFarLocation = glGetUniformLocation(froxelAccumProgram, "uNearFar");
        froxelScreenScaleLocation = glGetUniformLocation(froxelAccumProgram, "uScreenScale");
        froxelLightDirLocation = glGetUniformLocation(froxelAccumProgram, "uLightDir");
        froxelLightColorLocation = glGetUniformLocation(froxelAccumProgram, "uLightColor");
        froxelLightIntensityLocation = glGetUniformLocation(froxelAccumProgram, "uLightIntensity");
        froxelShadowMapLocation = glGetUniformLocation(froxelAccumProgram, "uShadowMap");
        for (int i = 0; i < CASCADE_COUNT; i++) {
            froxelLightMvpLocations[i] = glGetUniformLocation(froxelAccumProgram, "uLightMvp[" + i + "]");
        }
        froxelCascadeSplitsLocation = glGetUniformLocation(froxelAccumProgram, "uCascadeSplits");
        froxelShadowTexelSizeLocation = glGetUniformLocation(froxelAccumProgram, "uShadowTexelSize");

        froxelCompositeProgram = createFroxelCompositeProgram();
        froxelCompVolumeLocation = glGetUniformLocation(froxelCompositeProgram, "uFroxelTex");
        froxelCompPrevVolumeLocation = glGetUniformLocation(froxelCompositeProgram, "uPrevFroxelTex");
        froxelCompDepthLocation = glGetUniformLocation(froxelCompositeProgram, "uDepthTex");
        froxelCompCameraPosLocation = glGetUniformLocation(froxelCompositeProgram, "uCameraPos");
        froxelCompViewRightLocation = glGetUniformLocation(froxelCompositeProgram, "uViewRight");
        froxelCompViewUpLocation = glGetUniformLocation(froxelCompositeProgram, "uViewUp");
        froxelCompViewForwardLocation = glGetUniformLocation(froxelCompositeProgram, "uViewForward");
        froxelCompNearFarLocation = glGetUniformLocation(froxelCompositeProgram, "uNearFar");
        froxelCompScreenScaleLocation = glGetUniformLocation(froxelCompositeProgram, "uScreenScale");
        froxelCompPrevViewProjLocation = glGetUniformLocation(froxelCompositeProgram, "uPrevViewProj");
        froxelCompGridSizeLocation = glGetUniformLocation(froxelCompositeProgram, "uGridSize");

        cloudProgram = createCloudProgram();
        cloudCameraPosLocation = glGetUniformLocation(cloudProgram, "uCameraPos");
        cloudViewRightLocation = glGetUniformLocation(cloudProgram, "uViewRight");
        cloudViewUpLocation = glGetUniformLocation(cloudProgram, "uViewUp");
        cloudViewForwardLocation = glGetUniformLocation(cloudProgram, "uViewForward");
        cloudHScreenScaleLocation = glGetUniformLocation(cloudProgram, "uHScreenScale");
        cloudVScreenScaleLocation = glGetUniformLocation(cloudProgram, "uVScreenScale");
        cloudSunDirLocation = glGetUniformLocation(cloudProgram, "uSunDir");
        cloudSunColorLocation = glGetUniformLocation(cloudProgram, "uSunColor");
        cloudTimeLocation = glGetUniformLocation(cloudProgram, "uTime");
        cloudCoverageLocation = glGetUniformLocation(cloudProgram, "uCoverage");
        cloudBaseAltLocation = glGetUniformLocation(cloudProgram, "uBaseAlt");
        cloudTopAltLocation = glGetUniformLocation(cloudProgram, "uTopAlt");
        cloudWindDirLocation = glGetUniformLocation(cloudProgram, "uWindDir");
        cloudWindSpeedLocation = glGetUniformLocation(cloudProgram, "uWindSpeed");

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
        textAlphaLocation = glGetUniformLocation(textProgram, "uAlpha");
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

        particleComputeProgram = createComputeProgram(PARTICLE_COMPUTE_SOURCE);
        particleComputeDeltaLocation = glGetUniformLocation(particleComputeProgram, "uDelta");
        particleRenderProgram = createParticleRenderProgram();
        particleRenderMvpLocation = glGetUniformLocation(particleRenderProgram, "uMvp");
        particleRenderCameraRightLocation = glGetUniformLocation(particleRenderProgram, "uCameraRight");
        particleRenderCameraUpLocation = glGetUniformLocation(particleRenderProgram, "uCameraUp");
        particleRenderTileULocation = glGetUniformLocation(particleRenderProgram, "uTileU");
        particleRenderTileVLocation = glGetUniformLocation(particleRenderProgram, "uTileV");
        particleRenderLightDirLocation = glGetUniformLocation(particleRenderProgram, "uLightDir");
        particleRenderLightColorLocation = glGetUniformLocation(particleRenderProgram, "uLightColor");
        particleRenderLightIntensityLocation = glGetUniformLocation(particleRenderProgram, "uLightIntensity");
        int particleRenderAtlasLoc = glGetUniformLocation(particleRenderProgram, "uAtlas");
        glUseProgram(particleRenderProgram);
        glUniform1i(particleRenderAtlasLoc, 0);
        glUseProgram(0);
        particleVao = glGenVertexArrays();
        createParticleSsbo();
        lastParticleFrameTime = GLFW.glfwGetTime();
        gpuParticleCount = 0;

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

        mainFbo = 0;
        mainColorTex = 0;
        mainDepthRbo = 0;
        createMainFbo();
        createCloudFbos();
        froxelActiveIndex = 0;
        volumetricFrameCount = 0;

        outputProgram = createOutputProgram();
        outputTexLocation = glGetUniformLocation(outputProgram, "uTex");
        outputUseTonemapLocation = glGetUniformLocation(outputProgram, "uUseTonemap");
        outputContrastLocation = glGetUniformLocation(outputProgram, "uContrast");
        outputExposureLocation = glGetUniformLocation(outputProgram, "uExposure");

        compositeProgram = createCompositeProgram();
        compositeSkyTexLocation = glGetUniformLocation(compositeProgram, "uSkyTex");
        compositeCloudTexLocation = glGetUniformLocation(compositeProgram, "uCloudTex");

        glViewport(0, 0, window.width(), window.height());
    }

    public void render(World world, Player player, TickSystem ticks, boolean resized, GraphicsSettings graphicsSettings, boolean graphicsGuiOpen, boolean menuOpen, List<engine.WorldManager.WorldSlot> worlds, int selectedWorldIndex, boolean loadingWorld, boolean namingWorld, String worldNameInput, boolean chatOpen, List<engine.ChatMessage> chatMessages, String chatInput) {
        if (resized) {
            glViewport(0, 0, window.width(), window.height());
            resizeFbos();
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

            glBindFramebuffer(GL_FRAMEBUFFER, mainFbo);
            glViewport(0, 0, window.width(), window.height());
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            currentView.set(player.viewMatrix());
            float aspect = (float) window.width() / (float) window.height();
            currentProj.identity().perspective((float) Math.toRadians(FIELD_OF_VIEW_DEGREES), aspect, NEAR_PLANE, FAR_PLANE);
            prevViewProj.set(jitteredViewProj);
            jitteredViewProj.set(currentProj).mul(currentView);

            renderSky(player, ticks, sunDirection, moonDirection, graphicsSettings);
            renderClouds(player, ticks, sunDirection, graphicsSettings);
            compositeClouds();
            renderWorld(world, player, sunDirection, moonDirection, graphicsSettings);

            if (graphicsSettings.realisticLighting() && graphicsSettings.volumetricQuality() > 0) {
                renderVolumetricLight(player, sunDirection, moonDirection, graphicsSettings);
            }
            renderTargetOutline(world, player);

            float pSunBlend = sunStrength / Math.max(sunStrength + moonStrength, 0.0001f);
            float pMoonBlend = 1.0f - pSunBlend;
            float pLowSunWarmth = 1.0f - Math.min(1.0f, sunStrength * 2.2f);
            float pLightR = (1.00f + pLowSunWarmth * 0.22f) * pSunBlend + 0.56f * pMoonBlend;
            float pLightG = (0.96f - pLowSunWarmth * 0.12f) * pSunBlend + 0.66f * pMoonBlend;
            float pLightB = (0.86f - pLowSunWarmth * 0.26f) * pSunBlend + 1.00f * pMoonBlend;
            float pLightIntensity = sunStrength * 0.92f + moonStrength * 0.16f;
            renderParticles(player, activeLightDirection, pLightR, pLightG, pLightB, pLightIntensity);

            glBindFramebuffer(GL_FRAMEBUFFER, 0);
            glViewport(0, 0, window.width(), window.height());
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            renderOutput(graphicsSettings.contrast(), graphicsSettings.exposure());

            renderHotbar(player);
            renderCrosshair();
            renderFpsCounter();
            renderCoordinates(player);
            renderChat(chatMessages, chatInput, chatOpen);
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

        if (mainColorTex != 0) {
            glDeleteTextures(mainColorTex);
            mainColorTex = 0;
        }
        if (mainDepthRbo != 0) {
            glDeleteRenderbuffers(mainDepthRbo);
            mainDepthRbo = 0;
        }
        if (mainFbo != 0) {
            glDeleteFramebuffers(mainFbo);
            mainFbo = 0;
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
        if (froxelAccumProgram != 0) {
            glDeleteProgram(froxelAccumProgram);
            froxelAccumProgram = 0;
        }
        if (froxelCompositeProgram != 0) {
            glDeleteProgram(froxelCompositeProgram);
            froxelCompositeProgram = 0;
        }
        for (int i = 0; i < 2; i++) {
            if (froxelTex[i] != 0) {
                glDeleteTextures(froxelTex[i]);
                froxelTex[i] = 0;
            }
        }
        if (cloudProgram != 0) {
            glDeleteProgram(cloudProgram);
            cloudProgram = 0;
        }
        if (compositeProgram != 0) {
            glDeleteProgram(compositeProgram);
            compositeProgram = 0;
        }
        if (cloudColorTex != 0) {
            glDeleteTextures(cloudColorTex);
            cloudColorTex = 0;
        }
        if (cloudBackupTex != 0) {
            glDeleteTextures(cloudBackupTex);
            cloudBackupTex = 0;
        }
        if (cloudFbo != 0) {
            glDeleteFramebuffers(cloudFbo);
            cloudFbo = 0;
        }
        if (cloudBackupFbo != 0) {
            glDeleteFramebuffers(cloudBackupFbo);
            cloudBackupFbo = 0;
        }
        if (outputProgram != 0) {
            glDeleteProgram(outputProgram);
            outputProgram = 0;
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
        if (particleComputeProgram != 0) {
            glDeleteProgram(particleComputeProgram);
            particleComputeProgram = 0;
        }
        if (particleRenderProgram != 0) {
            glDeleteProgram(particleRenderProgram);
            particleRenderProgram = 0;
        }
        if (particleSsbo != 0) {
            glDeleteBuffers(particleSsbo);
            particleSsbo = 0;
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
        Matrix4f mvp = new Matrix4f(jitteredViewProj);
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

    private void renderClouds(Player player, TickSystem ticks, Vector3f sunDirection, GraphicsSettings graphicsSettings) {
        Vector3f forward = player.lookDirection(new Vector3f());
        Vector3f right = player.camera().right(new Vector3f());
        Vector3f up = new Vector3f(right).cross(forward).normalize();
        Vector3f cameraPos = player.eyePosition(new Vector3f());

        float tanHalfFov = (float) Math.tan(Math.toRadians(FIELD_OF_VIEW_DEGREES * 0.5));
        float aspect = (float) window.width() / (float) window.height();

        int cw = Math.max(1, window.width() / 2);
        int ch = Math.max(1, window.height() / 2);
        glBindFramebuffer(GL_FRAMEBUFFER, cloudFbo);
        glViewport(0, 0, cw, ch);

        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        glDisable(GL_BLEND);

        float sunStrength = sunStrength(sunDirection);
        float sunIntensity = sunStrength * 1.4f;

        glUseProgram(cloudProgram);
        glUniform3f(cloudCameraPosLocation, cameraPos.x, cameraPos.y, cameraPos.z);
        glUniform3f(cloudViewRightLocation, right.x, right.y, right.z);
        glUniform3f(cloudViewUpLocation, up.x, up.y, up.z);
        glUniform3f(cloudViewForwardLocation, forward.x, forward.y, forward.z);
        glUniform1f(cloudHScreenScaleLocation, aspect * tanHalfFov);
        glUniform1f(cloudVScreenScaleLocation, tanHalfFov);
        glUniform3f(cloudSunDirLocation, sunDirection.x, sunDirection.y, sunDirection.z);
        glUniform3f(cloudSunColorLocation, sunIntensity, 0.90f * sunIntensity, 0.18f * sunIntensity);
        glUniform1f(cloudTimeLocation, (float) GLFW.glfwGetTime());

        glUniform1f(cloudCoverageLocation, graphicsSettings.cloudCoverage());
        glUniform1f(cloudBaseAltLocation, 120.0f);
        glUniform1f(cloudTopAltLocation, 260.0f);
        glUniform2f(cloudWindDirLocation, 0.7f, 0.3f);
        glUniform1f(cloudWindSpeedLocation, 8.0f);

        glBindVertexArray(skyVao);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);

        glEnable(GL_BLEND);
        glBindFramebuffer(GL_FRAMEBUFFER, mainFbo);
        glViewport(0, 0, window.width(), window.height());
    }

    private void compositeClouds() {
        int w = window.width();
        int h = window.height();

        glBindFramebuffer(GL_READ_FRAMEBUFFER, mainFbo);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, cloudBackupFbo);
        glBlitFramebuffer(0, 0, w, h, 0, 0, w, h, GL_COLOR_BUFFER_BIT, GL_LINEAR);

        glBindFramebuffer(GL_FRAMEBUFFER, mainFbo);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        glDisable(GL_BLEND);

        glUseProgram(compositeProgram);
        glUniform1i(compositeSkyTexLocation, 0);
        glUniform1i(compositeCloudTexLocation, 1);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, cloudBackupTex);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, cloudColorTex);

        glBindVertexArray(skyVao);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, 0);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, 0);

        glEnable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
    }

    private void renderVolumetricLight(Player player, Vector3f sunDirection, Vector3f moonDirection, GraphicsSettings graphicsSettings) {
        float sunStr = sunStrength(sunDirection);
        float moonStr = moonStrength(sunDirection);
        Vector3f lightDirection = sunStr >= moonStr ? sunDirection : moonDirection;
        float intensity = sunStr >= moonStr ? sunStr * 0.50f : moonStr * 0.12f;
        if (intensity <= 0.005f) {
            return;
        }

        int quality = graphicsSettings.volumetricQuality();
        ensureFroxelTextures(quality);

        Vector3f forward = player.lookDirection(new Vector3f());
        Vector3f right = player.camera().right(new Vector3f());
        Vector3f up = new Vector3f(right).cross(forward).normalize();
        Vector3f cameraPos = player.eyePosition(new Vector3f());

        float sunBlend = sunStr / Math.max(sunStr + moonStr, 0.0001f);
        float moonBlend = 1.0f - sunBlend;
        float lightR = 1.00f * sunBlend + 0.50f * moonBlend;
        float lightG = 0.92f * sunBlend + 0.60f * moonBlend;
        float lightB = 0.74f * sunBlend + 1.00f * moonBlend;

        float tanHalfFov = (float) Math.tan(Math.toRadians(FIELD_OF_VIEW_DEGREES * 0.5));
        float aspect = (float) window.width() / (float) window.height();

        // --- Stage 1: Populate + Integrate via Compute ---
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        glDisable(GL_BLEND);

        glUseProgram(froxelAccumProgram);
        glUniform3f(froxelCameraPosLocation, cameraPos.x, cameraPos.y, cameraPos.z);
        glUniform3f(froxelViewRightLocation, right.x, right.y, right.z);
        glUniform3f(froxelViewUpLocation, up.x, up.y, up.z);
        glUniform3f(froxelViewForwardLocation, forward.x, forward.y, forward.z);
        glUniform2f(froxelNearFarLocation, NEAR_PLANE, FAR_PLANE);
        glUniform2f(froxelScreenScaleLocation, aspect * tanHalfFov, tanHalfFov);
        glUniform3f(froxelLightDirLocation, lightDirection.x, lightDirection.y, lightDirection.z);
        glUniform3f(froxelLightColorLocation, lightR, lightG, lightB);
        glUniform1f(froxelLightIntensityLocation, intensity);
        glUniform1i(froxelShadowMapLocation, 1);
        glUniform1fv(froxelCascadeSplitsLocation, CASCADE_SPLITS);
        glUniform1f(froxelShadowTexelSizeLocation, 1.0f / SHADOW_MAP_SIZE);
        glUniform3i(froxelGridSizeLocation, froxelWidth, froxelHeight, froxelDepth);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer lightBuffer = stack.mallocFloat(16);
            for (int i = 0; i < CASCADE_COUNT; i++) {
                cascadeLightMvp[i].get(lightBuffer.clear());
                glUniformMatrix4fv(froxelLightMvpLocations[i], false, lightBuffer);
            }
        }

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D_ARRAY, shadowDepthTexture);

        int writeIdx = froxelActiveIndex;
        glBindImageTexture(0, froxelTex[writeIdx], 0, true, 0, GL_WRITE_ONLY, GL_RGBA16F);

        int groupsX = (froxelWidth + 7) / 8;
        int groupsY = (froxelHeight + 7) / 8;
        glDispatchCompute(groupsX, groupsY, 1);
        glMemoryBarrier(GL_ALL_BARRIER_BITS);

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D_ARRAY, 0);

        // --- Stage 2: Composite with temporal reprojection ---
        glBindFramebuffer(GL_FRAMEBUFFER, mainFbo);
        glViewport(0, 0, window.width(), window.height());
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE);

        glUseProgram(froxelCompositeProgram);
        glUniform1i(froxelCompVolumeLocation, 0);
        glUniform1i(froxelCompPrevVolumeLocation, 1);
        glUniform1i(froxelCompDepthLocation, 2);
        glUniform3f(froxelCompCameraPosLocation, cameraPos.x, cameraPos.y, cameraPos.z);
        glUniform3f(froxelCompViewRightLocation, right.x, right.y, right.z);
        glUniform3f(froxelCompViewUpLocation, up.x, up.y, up.z);
        glUniform3f(froxelCompViewForwardLocation, forward.x, forward.y, forward.z);
        glUniform2f(froxelCompNearFarLocation, NEAR_PLANE, FAR_PLANE);
        glUniform2f(froxelCompScreenScaleLocation, aspect * tanHalfFov, tanHalfFov);
        glUniform3i(froxelCompGridSizeLocation, froxelWidth, froxelHeight, froxelDepth);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer prevViewProjBuf = stack.mallocFloat(16);
            prevViewProj.get(prevViewProjBuf);
            glUniformMatrix4fv(froxelCompPrevViewProjLocation, false, prevViewProjBuf);
        }

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_3D, froxelTex[writeIdx]);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_3D, froxelTex[1 - writeIdx]);
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, mainDepthRbo);

        glBindVertexArray(skyVao);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);

        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, 0);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_3D, 0);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_3D, 0);

        froxelActiveIndex = 1 - froxelActiveIndex;
        volumetricFrameCount++;

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
    }

    private void ensureFroxelTextures(int quality) {
        if (quality <= 0 || quality == currentFroxelQuality) {
            return;
        }
        for (int i = 0; i < 2; i++) {
            if (froxelTex[i] != 0) {
                glDeleteTextures(froxelTex[i]);
                froxelTex[i] = 0;
            }
        }
        switch (quality) {
            case 1 -> { froxelWidth = 80; froxelHeight = 45; froxelDepth = 32; }
            case 2 -> { froxelWidth = 120; froxelHeight = 67; froxelDepth = 48; }
            default -> { froxelWidth = 160; froxelHeight = 90; froxelDepth = 64; }
        }
        for (int i = 0; i < 2; i++) {
            froxelTex[i] = glGenTextures();
            glBindTexture(GL_TEXTURE_3D, froxelTex[i]);
            glTexImage3D(GL_TEXTURE_3D, 0, GL_RGBA16F, froxelWidth, froxelHeight, froxelDepth, 0, GL_RGBA, GL_FLOAT, 0L);
            glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        }
        glBindTexture(GL_TEXTURE_3D, 0);
        currentFroxelQuality = quality;
        froxelActiveIndex = 0;
        volumetricFrameCount = 0;
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

    private void renderParticles(Player player, Vector3f lightDirection, float lightR, float lightG, float lightB, float lightIntensity) {
        if (particleSystem == null) {
            return;
        }

        double now = GLFW.glfwGetTime();
        float delta = (float) (now - lastParticleFrameTime);
        lastParticleFrameTime = now;
        delta = Math.min(delta, 0.1f);

        List<Particle> newParticles = particleSystem.drainParticles();
        if (!newParticles.isEmpty() && gpuParticleCount < MAX_GPU_PARTICLES) {
            float[] data = particlesToFloatArray(newParticles);
            int uploadCount = Math.min(newParticles.size(), MAX_GPU_PARTICLES - gpuParticleCount);
            int floatCount = uploadCount * PARTICLE_STRIDE;
            glBindBuffer(GL_SHADER_STORAGE_BUFFER, particleSsbo);
            try (MemoryStack stack = MemoryStack.stackPush()) {
                java.nio.FloatBuffer uploadBuf = stack.mallocFloat(floatCount);
                uploadBuf.put(data, 0, floatCount);
                uploadBuf.flip();
                glBufferSubData(GL_SHADER_STORAGE_BUFFER,
                        (long) gpuParticleCount * PARTICLE_STRIDE * Float.BYTES,
                        uploadBuf);
            }
            gpuParticleCount += uploadCount;
            glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
        }

        if (gpuParticleCount <= 0) {
            return;
        }

        glUseProgram(particleComputeProgram);
        glUniform1f(particleComputeDeltaLocation, delta);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, particleSsbo);
        glDispatchCompute((gpuParticleCount + 255) / 256, 1, 1);
        glMemoryBarrier(GL_ALL_BARRIER_BITS);

        Matrix4f mvp = new Matrix4f(jitteredViewProj);
        Vector3f forward = player.lookDirection(new Vector3f());
        Vector3f right = player.camera().right(new Vector3f());
        Vector3f up = new Vector3f(right).cross(forward).normalize();

        glUseProgram(particleRenderProgram);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buf = stack.mallocFloat(16);
            mvp.get(buf);
            glUniformMatrix4fv(particleRenderMvpLocation, false, buf);
        }
        glUniform3f(particleRenderCameraRightLocation, right.x, right.y, right.z);
        glUniform3f(particleRenderCameraUpLocation, up.x, up.y, up.z);
        glUniform1f(particleRenderTileULocation, 1.0f / TextureAtlas.COLUMNS);
        glUniform1f(particleRenderTileVLocation, 1.0f / TextureAtlas.ROWS);
        glUniform3f(particleRenderLightDirLocation, lightDirection.x, lightDirection.y, lightDirection.z);
        glUniform3f(particleRenderLightColorLocation, lightR, lightG, lightB);
        glUniform1f(particleRenderLightIntensityLocation, lightIntensity);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, atlasTexture);

        glDisable(GL_CULL_FACE);
        glDepthMask(false);
        glBindVertexArray(particleVao);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, particleSsbo);
        glDrawArrays(GL_TRIANGLES, 0, gpuParticleCount * 6);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, 0);
        glBindVertexArray(0);
        glDepthMask(true);
        glEnable(GL_CULL_FACE);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, 0);
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

    private void renderCoordinates(Player player) {
        Vector3f pos = player.eyePosition(new Vector3f());
        String text = String.format("XYZ: %.1f / %.1f / %.1f", pos.x, pos.y, pos.z);
        float glyphHeight = 22.0f;
        float padding = 12.0f;
        float startX = padding;
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

    private void renderChat(List<engine.ChatMessage> messages, String inputText, boolean chatOpen) {
        float glyphHeight = 22.0f;
        float padding = 12.0f;
        float lineHeight = glyphHeight + 4.0f;
        float startX = padding;
        float maxWidth = window.width() * 0.5f;

        double now = System.currentTimeMillis() / 1000.0;
        float batchAlpha = 1.0f;

        FloatArrayBuilder textVertices = new FloatArrayBuilder(512);

        float startY = window.height() - padding - lineHeight;
        if (chatOpen) {
            String inputLine = "> " + inputText + "_";
            inputLine = truncateText(inputLine, maxWidth, glyphHeight, 14.0f);
            drawText(textVertices, inputLine, startX, startY, 14.0f, glyphHeight, 3.0f);
            startY -= lineHeight;
        }

        int visibleCount = Math.min(messages.size(), chatOpen ? 8 : 3);
        for (int i = messages.size() - visibleCount; i < messages.size(); i++) {
            engine.ChatMessage msg = messages.get(i);
            double age = now - msg.timestamp();
            float alpha = 1.0f;
            if (!chatOpen && age > 10.0) {
                alpha = (float) Math.max(0.0, 1.0 - (age - 10.0) / 3.0);
            }
            batchAlpha = Math.min(batchAlpha, alpha);
            if (alpha > 0.001f) {
                String text = truncateText(msg.text(), maxWidth, glyphHeight, 14.0f);
                drawText(textVertices, text, startX, startY, 14.0f, glyphHeight, 3.0f);
            }
            startY -= lineHeight;
        }

        if (textVertices.size() == 0) {
            return;
        }

        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        textAlpha = batchAlpha;
        renderTextBatch(textVertices);
        textAlpha = 1.0f;
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
    }

    private void renderGraphicsGui(GraphicsSettings settings) {
        float rowHeight = 42.0f;
        float panelHeight = 82.0f + settings.count() * rowHeight;
        float panelX = 32.0f;
        float panelY = 58.0f;

        float panelWidth = 470.0f;
        for (int i = 0; i < settings.count(); i++) {
            float nameWidth = measureTextWidth(settings.name(i), 14.0f, 9.0f);
            float valueWidth = measureTextWidth(settings.displayValue(i), 14.0f, 9.0f);
            panelWidth = Math.max(panelWidth, panelX + 34.0f + nameWidth + 24.0f);
            panelWidth = Math.max(panelWidth, panelX + 310.0f + valueWidth + 34.0f);
        }

        FloatArrayBuilder vertices = new FloatArrayBuilder(8192);
        FloatArrayBuilder textVertices = new FloatArrayBuilder(4096);
        addRect(vertices, panelX, panelY, panelWidth, panelHeight, 0.02f, 0.025f, 0.032f, 0.84f);
        addRect(vertices, panelX, panelY, panelWidth, 3.0f, 0.42f, 0.62f, 0.72f, 0.95f);
        addRect(vertices, panelX, panelY + panelHeight - 3.0f, panelWidth, 3.0f, 0.12f, 0.15f, 0.17f, 0.95f);
        addRect(vertices, panelX, panelY, 3.0f, panelHeight, 0.12f, 0.15f, 0.17f, 0.95f);
        addRect(vertices, panelX + panelWidth - 3.0f, panelY, 3.0f, panelHeight, 0.12f, 0.15f, 0.17f, 0.95f);

        drawText(textVertices, "GRAPHICS", panelX + 22.0f, panelY + 18.0f, 12.0f, 18.0f, 2.5f);
        drawText(textVertices, "F3 CLOSE", panelX + panelWidth - measureTextWidth("F3 CLOSE", 12.0f, 8.0f) - 22.0f, panelY + 20.0f, 8.0f, 12.0f, 2.0f);

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
        for (int i = 0; i < worlds.size(); i++) {
            float nameWidth = measureTextWidth(worlds.get(i).name(), 14.0f, 10.0f);
            listWidth = Math.max(listWidth, nameWidth + 48.0f);
        }
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
            String displayName = worldNameInput.isEmpty() ? "Type a name..." : worldNameInput;
            String cursorText = displayName + (worldNameInput.isEmpty() ? "" : "_");
            float titleWidth = measureTextWidth("NAME YOUR WORLD", 18.0f, 12.0f);
            float inputWidth = measureTextWidth(cursorText, 14.0f, 10.0f);
            float helpWidth = measureTextWidth("ENTER CONFIRM   ESC CANCEL", 10.0f, 7.0f);
            float minBoxWidth = 520.0f;
            float contentWidth = Math.max(titleWidth, Math.max(inputWidth + 52.0f, helpWidth + 56.0f));
            float boxWidth = Math.max(minBoxWidth, contentWidth);
            float boxHeight = 150.0f;
            float boxX = window.width() * 0.5f - boxWidth * 0.5f;
            float boxY = window.height() * 0.5f - boxHeight * 0.5f;
            addRect(vertices, 0.0f, 0.0f, window.width(), window.height(), 0.0f, 0.0f, 0.0f, 0.6f);
            addRect(vertices, boxX, boxY, boxWidth, boxHeight, 0.12f, 0.14f, 0.18f, 0.97f);
            addRect(vertices, boxX + 2.0f, boxY + 2.0f, boxWidth - 4.0f, boxHeight - 4.0f, 0.18f, 0.20f, 0.24f, 0.95f);
            drawText(textVertices, "NAME YOUR WORLD", boxX + 28.0f, boxY + 20.0f, 12.0f, 18.0f, 2.4f);
            float textX = boxX + 28.0f;
            float textY = boxY + 64.0f;
            float inputBoxWidth = Math.min(inputWidth + 52.0f, boxWidth - 56.0f);
            addRect(vertices, textX - 6.0f, textY - 18.0f, inputBoxWidth, 26.0f, 0.08f, 0.09f, 0.12f, 0.90f);
            drawText(textVertices, cursorText, textX, textY, 10.0f, 14.0f, 2.0f);
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
        float cursorX = x;
        float scale = glyphHeight / textAscent;

        for (int i = 0; i < text.length(); i++) {
            TextGlyph glyph = lookupTextGlyph(text.charAt(i));
            if (glyph == null) {
                continue;
            }

            if (glyph.width > 0.0f && glyph.height > 0.0f) {
                float drawX = Math.round(cursorX + glyph.xOffset * scale);
                float drawY = Math.round(y + glyphHeight + glyph.yOffset * scale);
                addTextQuad(vertices, drawX, drawY, glyph.width * scale, glyph.height * scale, glyph.u0, glyph.v0, glyph.u1, glyph.v1);
            }

            float advance = glyph.advance * scale;
            cursorX += advance;
        }
    }

    private float measureTextWidth(String text, float glyphHeight, float glyphWidth) {
        float width = 0.0f;
        float scale = glyphHeight / textAscent;

        for (int i = 0; i < text.length(); i++) {
            TextGlyph glyph = lookupTextGlyph(text.charAt(i));
            if (glyph == null) {
                continue;
            }
            width += glyph.advance * scale;
        }

        return width;
    }

    private String truncateText(String text, float maxWidth, float glyphHeight, float glyphWidth) {
        if (measureTextWidth(text, glyphHeight, glyphWidth) <= maxWidth) {
            return text;
        }
        float ellipsisWidth = measureTextWidth("...", glyphHeight, glyphWidth);
        int end = text.length();
        while (end > 0 && measureTextWidth(text.substring(0, end), glyphHeight, glyphWidth) + ellipsisWidth > maxWidth) {
            end--;
        }
        return text.substring(0, end) + "...";
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
        glUniform1f(textAlphaLocation, textAlpha);
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
        this.textAscent = metrics.getAscent();

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
                #version 430 core
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
                #version 430 core
                uniform sampler2D uFont;
                uniform float uAlpha;

                in vec2 vUv;
                out vec4 fragColor;

                void main() {
                    vec4 texel = texture(uFont, vUv);
                    fragColor = vec4(texel.rgb, texel.a * uAlpha);
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
            probeShiftCooldown = (PROBE_GRID_X * PROBE_GRID_Y * PROBE_GRID_Z) / PROBE_UPDATES_PER_FRAME + 1;
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
            float downVisibility = visibilityAlong(world, probePos, new Vector3f(0.0f, -1.0f, 0.0f), 16.0f);
            float sideA = visibilityAlong(world, probePos, new Vector3f(1.0f, 0.0f, 0.0f).normalize(), 16.0f);
            float sideB = visibilityAlong(world, probePos, new Vector3f(-1.0f, 0.0f, 0.0f).normalize(), 16.0f);
            float sideC = visibilityAlong(world, probePos, new Vector3f(0.0f, 0.0f, 1.0f).normalize(), 16.0f);
            float sideD = visibilityAlong(world, probePos, new Vector3f(0.0f, 0.0f, -1.0f).normalize(), 16.0f);
            float diagA = visibilityAlong(world, probePos, new Vector3f(1.0f, -0.5f, 1.0f).normalize(), 14.0f);
            float diagB = visibilityAlong(world, probePos, new Vector3f(-1.0f, -0.5f, -1.0f).normalize(), 14.0f);
            float diagC = visibilityAlong(world, probePos, new Vector3f(1.0f, -0.5f, -1.0f).normalize(), 14.0f);
            float diagD = visibilityAlong(world, probePos, new Vector3f(-1.0f, -0.5f, 1.0f).normalize(), 14.0f);
            float sunVis = visibilityAlong(world, probePos, new Vector3f(sunDirection).negate(), 80.0f);
            float moonVis = visibilityAlong(world, probePos, new Vector3f(moonDirection).negate(), 64.0f);

            float sun = sunStrength(sunDirection) * sunVis;
            float moon = moonStrength(sunDirection) * moonVis;

            float ambient = (0.08f + skyBrightness * 0.44f) * (0.30f + 0.36f * upVisibility + 0.10f * downVisibility + 0.06f * (sideA + sideB + sideC + sideD + diagA + diagB + diagC + diagD));
            float sunTerm = sun * (0.55f + 0.45f * skyBrightness);
            float moonTerm = moon * 0.17f;

            int base = probeIndex * 3;
            float nextR = ambient * 0.58f + sunTerm * 1.00f + moonTerm * 0.60f;
            float nextG = ambient * 0.62f + sunTerm * 0.93f + moonTerm * 0.70f;
            float nextB = ambient * 0.75f + sunTerm * 0.78f + moonTerm * 0.95f;
            float blend = probeShiftCooldown > 0 ? 0.6f : 0.22f;
            probeData[base] = probeData[base] + (nextR - probeData[base]) * blend;
            probeData[base + 1] = probeData[base + 1] + (nextG - probeData[base + 1]) * blend;
            probeData[base + 2] = probeData[base + 2] + (nextB - probeData[base + 2]) * blend;
        }
        probeUpdateCursor = (probeUpdateCursor + PROBE_UPDATES_PER_FRAME) % total;
        if (probeShiftCooldown > 0) {
            probeShiftCooldown--;
        }

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
        float normalizedDist = hit.distance() / maxDistance;
        return Math.max(0.0f, Math.min(1.0f, normalizedDist * normalizedDist));
    }

    private Matrix4f worldViewProjection(Player player) {
        return new Matrix4f(currentProj).mul(currentView);
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
                #version 430 core
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
                    vec3 normalPos = aPosition + aNormal * 0.05;
                    for (int i = 0; i < 4; i++) {
                        vShadowCoord[i] = uLightMvp[i] * vec4(normalPos, 1.0);
                    }
                    gl_Position = uMvp * vec4(aPosition, 1.0);
                }
                """;

        String fragmentShader = """
                #version 430 core
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
                    float bias = max(0.00015, mix(0.00160, 0.00035, ndotl));
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
                            visibility += currentDepth <= closestDepth ? 1.0 : 0.0;
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
                    vec3 albedoLinear = pow(albedo.rgb, vec3(2.2));

                    vec3 normal = normalize(vNormal);
                    vec3 lightDir = normalize(-uLightDir);
                    float ndotl = max(dot(normal, lightDir), 0.0);
                    float diffuse = uRealisticLighting == 1 ? pow(ndotl, 1.18) : ndotl;
                    float viewDistance = distance(vWorldPos, uCameraPos);
                    int cascade = selectCascade(viewDistance);
                    float visibility = shadowFactor(cascade, vShadowCoord[cascade], normal, lightDir);

                    float blendWidth = 16.0;
                    if (cascade < CASCADE_COUNT - 1) {
                        float split = uCascadeSplits[cascade];
                        float t = clamp((viewDistance - (split - blendWidth * 0.5)) / blendWidth, 0.0, 1.0);
                        if (t > 0.0) {
                            float nextVis = shadowFactor(cascade + 1, vShadowCoord[cascade + 1], normal, lightDir);
                            visibility = mix(visibility, nextVis, t);
                        }
                    }
                    if (cascade > 0) {
                        float split = uCascadeSplits[cascade - 1];
                        float t = clamp((viewDistance - (split - blendWidth * 0.5)) / blendWidth, 0.0, 1.0);
                        if (t < 1.0) {
                            float prevVis = shadowFactor(cascade - 1, vShadowCoord[cascade - 1], normal, lightDir);
                            visibility = mix(prevVis, visibility, t);
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

                    fragColor = vec4(color, albedo.a);
                }
                """;

        return createProgram(vertexShader, fragmentShader);
    }

    private int createFroxelAccumProgram() {
        String computeSource = """
                #version 430 core
                layout(local_size_x = 8, local_size_y = 8) in;

                layout(rgba16f, binding = 0) uniform writeonly image3D uFroxelTex;

                uniform vec3 uCameraPos;
                uniform vec3 uViewRight;
                uniform vec3 uViewUp;
                uniform vec3 uViewForward;
                uniform vec2 uNearFar;
                uniform vec2 uScreenScale;
                uniform vec3 uLightDir;
                uniform vec3 uLightColor;
                uniform float uLightIntensity;
                uniform sampler2DArray uShadowMap;
                uniform mat4 uLightMvp[4];
                uniform float uCascadeSplits[4];
                uniform float uShadowTexelSize;
                uniform ivec3 uGridSize;

                float densityAt(vec3 pos) {
                    return 0.0;
                }

                int selectCascade(float d) {
                    if (d < uCascadeSplits[0]) return 0;
                    if (d < uCascadeSplits[1]) return 1;
                    if (d < uCascadeSplits[2]) return 2;
                    return 3;
                }

                float shadowPCF(vec3 wpos, float vd) {
                    int c = selectCascade(vd);
                    vec4 sc = uLightMvp[c] * vec4(wpos, 1.0);
                    vec3 p = sc.xyz / max(sc.w, 0.0001) * 0.5 + 0.5;
                    if (p.x < 0.0 || p.x > 1.0 || p.y < 0.0 || p.y > 1.0 || p.z > 1.0) return 1.0;
                    float depth = p.z - 0.0014;
                    float v = 0.0;
                    for (int y = -1; y <= 1; y++) {
                        for (int x = -1; x <= 1; x++) {
                            vec2 uv = p.xy + vec2(float(x), float(y)) * uShadowTexelSize * 2.0;
                            if (uv.x < 0.0 || uv.x > 1.0 || uv.y < 0.0 || uv.y > 1.0) {
                                v += 1.0;
                            } else {
                                float cd = texture(uShadowMap, vec3(uv, float(c))).r;
                                v += depth <= cd ? 1.0 : 0.08;
                            }
                        }
                    }
                    return v / 9.0;
                }

                void main() {
                    ivec2 xy = ivec2(gl_GlobalInvocationID.xy);
                    if (xy.x >= uGridSize.x || xy.y >= uGridSize.y) return;

                    float near = uNearFar.x;
                    float far = uNearFar.y;

                    float ndcX = (float(xy.x) + 0.5) / float(uGridSize.x) * 2.0 - 1.0;
                    float ndcY = (float(xy.y) + 0.5) / float(uGridSize.y) * 2.0 - 1.0;
                    vec3 rayDir = normalize(uViewForward + uViewRight * ndcX * uScreenScale.x + uViewUp * ndcY * uScreenScale.y);

                    float transmittance = 1.0;
                    vec3 lightAccum = vec3(0.0);

                    for (int z = 0; z < uGridSize.z; z++) {
                        float zn0 = float(z) / float(uGridSize.z);
                        float zn1 = float(z + 1) / float(uGridSize.z);
                        float d0 = near * pow(far / near, zn0);
                        float d1 = near * pow(far / near, zn1);
                        float dMid = (d0 + d1) * 0.5;
                        float stepLen = d1 - d0;

                        vec3 wpos = uCameraPos + rayDir * dMid;
                        float vd = length(wpos - uCameraPos);

                        float density = densityAt(wpos);

                        if (density > 0.0001) {
                            float sv = shadowPCF(wpos, vd);

                            vec3 ld = normalize(-uLightDir);
                            float od = 0.0;
                            vec3 ls = ld * 3.0;
                            vec3 sp = wpos + ls;
                            for (int j = 0; j < 4; j++) {
                                od += densityAt(sp) * 3.0;
                                sp += ls;
                            }
                            float ss = exp(-od * 0.4);
                            float lf = mix(sv * ss, 1.0, 0.12);

                            vec3 is = uLightColor * density * lf * uLightIntensity * 0.15;
                            float ex = density * 0.6;

                            lightAccum += is * transmittance * stepLen;
                            transmittance *= exp(-ex * stepLen);
                        }

                        imageStore(uFroxelTex, ivec3(xy.x, xy.y, z), vec4(lightAccum, transmittance));

                        if (transmittance < 0.005) {
                            for (int z2 = z + 1; z2 < uGridSize.z; z2++) {
                                imageStore(uFroxelTex, ivec3(xy.x, xy.y, z2), vec4(lightAccum, transmittance));
                            }
                            break;
                        }
                    }
                }
                """;
        return createComputeProgram(computeSource);
    }

    private int createFroxelCompositeProgram() {
        String vertexShader = """
                #version 430 core
                layout(location = 0) in vec2 aPosition;
                out vec2 vPosition;
                void main() {
                    vPosition = aPosition;
                    gl_Position = vec4(aPosition, 0.997, 1.0);
                }
                """;

        String fragmentShader = """
                #version 430 core
                in vec2 vPosition;
                out vec4 fragColor;

                uniform sampler3D uFroxelTex;
                uniform sampler3D uPrevFroxelTex;
                uniform sampler2D uDepthTex;
                uniform vec3 uCameraPos;
                uniform vec3 uViewRight;
                uniform vec3 uViewUp;
                uniform vec3 uViewForward;
                uniform vec2 uNearFar;
                uniform vec2 uScreenScale;
                uniform mat4 uPrevViewProj;
                uniform ivec3 uGridSize;

                float linearizeDepth(float d) {
                    float near = uNearFar.x;
                    float far = uNearFar.y;
                    return near * far / (far - d * (far - near));
                }

                void main() {
                    vec2 uv = vPosition * 0.5 + 0.5;
                    float depth = texture(uDepthTex, uv).r;

                    if (depth >= 1.0) {
                        fragColor = vec4(0.0);
                        return;
                    }

                    float linDepth = linearizeDepth(depth);
                    float near = uNearFar.x;
                    float far = uNearFar.y;
                    float zNorm = clamp(log(linDepth / near) / log(far / near), 0.0, 1.0);

                    vec3 texSize = vec3(textureSize(uFroxelTex, 0));
                    vec3 volUV = (vec3(uv, zNorm) * vec3(uGridSize) + 0.5) / texSize;
                    vec4 currentVol = texture(uFroxelTex, volUV);

                    vec3 rayDir = normalize(uViewForward + uViewRight * vPosition.x * uScreenScale.x + uViewUp * vPosition.y * uScreenScale.y);
                    vec3 worldPos = uCameraPos + rayDir * linDepth;

                    vec4 prevClip = uPrevViewProj * vec4(worldPos, 1.0);
                    vec3 prevNdc = prevClip.xyz / prevClip.w;
                    vec2 prevUv = prevNdc.xy * 0.5 + 0.5;

                    if (prevUv.x >= 0.0 && prevUv.x <= 1.0 && prevUv.y >= 0.0 && prevUv.y <= 1.0) {
                        float prevLinDepth = linearizeDepth(prevNdc.z * 0.5 + 0.5);
                        float depthDiff = abs(prevLinDepth - linDepth) / max(linDepth, 0.001);
                        if (depthDiff <= 0.10) {
                            float prevZ = clamp(log(prevLinDepth / near) / log(far / near), 0.0, 1.0);
                            vec3 prevTexSize = vec3(textureSize(uPrevFroxelTex, 0));
                            vec3 prevVolUV = (vec3(prevUv, prevZ) * vec3(uGridSize) + 0.5) / prevTexSize;
                            vec4 prevVol = texture(uPrevFroxelTex, prevVolUV);
                            float blend = 0.18;
                            currentVol = mix(prevVol, currentVol, blend);
                        }
                    }

                    fragColor = vec4(currentVol.rgb, 1.0);
                }
                """;

        return createProgram(vertexShader, fragmentShader);
    }

    private int createComputeProgram(String computeSource) {
        int computeShader = compileShader(GL_COMPUTE_SHADER, computeSource);
        int program = glCreateProgram();
        glAttachShader(program, computeShader);
        glLinkProgram(program);
        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            String log = glGetProgramInfoLog(program);
            glDeleteShader(computeShader);
            glDeleteProgram(program);
            throw new IllegalStateException("Compute program link failed: " + log);
        }
        glDeleteShader(computeShader);
        return program;
    }

    private int createParticleRenderProgram() {
        String vertexShader = """
                #version 430 core
                layout(std430, binding = 0) buffer ParticleBuffer {
                    float particles[];
                };
                uniform mat4 uMvp;
                uniform vec3 uCameraRight;
                uniform vec3 uCameraUp;
                uniform float uTileU;
                uniform float uTileV;
                uniform vec3 uLightDir;
                uniform vec3 uLightColor;
                uniform float uLightIntensity;
                out vec2 vUv;
                out float vAlpha;
                out vec3 vLighting;
                void main() {
                    int idx = gl_VertexID / 6;
                    int corner = gl_VertexID - idx * 6;
                    int base = idx * 12;
                    float life = particles[base + 3];
                    if (life <= 0.0) {
                        gl_Position = vec4(-2.0, -2.0, -2.0, 1.0);
                        vAlpha = 0.0;
                        vUv = vec2(0.0);
                        vLighting = vec3(0.0);
                        return;
                    }
                    float maxLife = particles[base + 7];
                    float size = particles[base + 8];
                    int texSlot = int(particles[base + 9]);
                    float px = particles[base + 0];
                    float py = particles[base + 1];
                    float pz = particles[base + 2];
                    float alpha = clamp(life / maxLife, 0.0, 1.0);
                    float hx = size * 0.5;
                    vec2 off;
                    vec2 uv;
                    if (corner == 0) { off = vec2(-hx, -hx); uv = vec2(0, 0); }
                    else if (corner == 1) { off = vec2(-hx, hx); uv = vec2(0, 1); }
                    else if (corner == 2) { off = vec2(hx, hx); uv = vec2(1, 1); }
                    else if (corner == 3) { off = vec2(-hx, -hx); uv = vec2(0, 0); }
                    else if (corner == 4) { off = vec2(hx, hx); uv = vec2(1, 1); }
                    else { off = vec2(hx, -hx); uv = vec2(1, 0); }
                    vec3 worldPos = vec3(px, py, pz) + uCameraRight * off.x + uCameraUp * off.y;
                    gl_Position = uMvp * vec4(worldPos, 1.0);
                    float col = float(texSlot - (texSlot / 16) * 16);
                    float row = float(texSlot / 16);
                    vUv = vec2((col + uv.x) * uTileU, (row + uv.y) * uTileV);
                    vAlpha = alpha;
                    vec3 lightDirN = normalize(-uLightDir);
                    float ndotl = max(lightDirN.y * 0.5 + 0.5, 0.0);
                    vLighting = uLightColor * ndotl * uLightIntensity;
                }
                """;
        String fragmentShader = """
                #version 430 core
                in vec2 vUv;
                in float vAlpha;
                in vec3 vLighting;
                out vec4 fragColor;
                uniform sampler2D uAtlas;
                void main() {
                    vec4 color = texture(uAtlas, vUv);
                    if (color.a < 0.01) discard;
                    vec3 lit = color.rgb * vLighting;
                    fragColor = vec4(lit, color.a * vAlpha);
                }
                """;
        return createProgram(vertexShader, fragmentShader);
    }

    private void createParticleSsbo() {
        particleSsbo = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, particleSsbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER, (long) MAX_GPU_PARTICLES * PARTICLE_STRIDE * Float.BYTES, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }

    private float[] particlesToFloatArray(List<Particle> particles) {
        float[] data = new float[particles.size() * PARTICLE_STRIDE];
        int idx = 0;
        for (Particle p : particles) {
            data[idx++] = p.position.x;
            data[idx++] = p.position.y;
            data[idx++] = p.position.z;
            data[idx++] = p.life;
            data[idx++] = p.velocity.x;
            data[idx++] = p.velocity.y;
            data[idx++] = p.velocity.z;
            data[idx++] = p.maxLife;
            data[idx++] = p.size;
            data[idx++] = (float) p.textureSlot;
            data[idx++] = 0.0f;
            data[idx++] = 0.0f;
        }
        return data;
    }

    private int createShadowProgram() {
        String vertexShader = """
                #version 430 core
                layout(location = 0) in vec3 aPosition;

                uniform mat4 uLightMvp;

                void main() {
                    gl_Position = uLightMvp * vec4(aPosition, 1.0);
                }
                """;

        String fragmentShader = """
                #version 430 core
                void main() {
                }
                """;

        return createProgram(vertexShader, fragmentShader);
    }

    private int createSkyProgram() {
        String vertexShader = """
                #version 430 core
                layout(location = 0) in vec2 aPosition;
                out vec2 vPosition;

                void main() {
                    vPosition = aPosition;
                    gl_Position = vec4(aPosition, 0.999, 1.0);
                }
                """;

        String fragmentShader = """
                #version 430 core
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
                    fragColor = vec4(sky, 1.0);
                }
                """;

        return createProgram(vertexShader, fragmentShader);
    }

    private int createOutlineProgram() {
        String vertexShader = """
                #version 430 core
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
                #version 430 core
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
                #version 430 core
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
                #version 430 core
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
                #version 430 core
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
                #version 430 core
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
                #version 430 core
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
                #version 430 core
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

    private void createMainFbo() {
        int w = window.width();
        int h = window.height();

        mainColorTex = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, mainColorTex);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, w, h, 0, GL_RGBA, GL_FLOAT, 0L);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        mainDepthRbo = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, mainDepthRbo);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT24, w, h, 0, GL_DEPTH_COMPONENT, GL_FLOAT, 0L);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        mainFbo = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, mainFbo);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, mainColorTex, 0);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, mainDepthRbo, 0);

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("Main framebuffer is incomplete");
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    private void createCloudFbos() {
        int cw = Math.max(1, window.width() / 2);
        int ch = Math.max(1, window.height() / 2);

        cloudColorTex = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, cloudColorTex);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, cw, ch, 0, GL_RGBA, GL_FLOAT, 0L);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        cloudFbo = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, cloudFbo);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, cloudColorTex, 0);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("Cloud framebuffer is incomplete");
        }

        int w = window.width();
        int h = window.height();
        cloudBackupTex = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, cloudBackupTex);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, w, h, 0, GL_RGBA, GL_FLOAT, 0L);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        cloudBackupFbo = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, cloudBackupFbo);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, cloudBackupTex, 0);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("Cloud backup framebuffer is incomplete");
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    private void resizeFbos() {
        if (mainColorTex != 0) {
            glDeleteTextures(mainColorTex);
        }
        if (mainDepthRbo != 0) {
            glDeleteTextures(mainDepthRbo);
        }
        if (mainFbo != 0) {
            glDeleteFramebuffers(mainFbo);
        }
        if (cloudColorTex != 0) {
            glDeleteTextures(cloudColorTex);
        }
        if (cloudBackupTex != 0) {
            glDeleteTextures(cloudBackupTex);
        }
        if (cloudFbo != 0) {
            glDeleteFramebuffers(cloudFbo);
        }
        if (cloudBackupFbo != 0) {
            glDeleteFramebuffers(cloudBackupFbo);
        }
        for (int i = 0; i < 2; i++) {
            if (froxelTex[i] != 0) {
                glDeleteTextures(froxelTex[i]);
                froxelTex[i] = 0;
            }
        }
        mainColorTex = 0;
        mainDepthRbo = 0;
        mainFbo = 0;
        cloudColorTex = 0;
        cloudBackupTex = 0;
        cloudFbo = 0;
        cloudBackupFbo = 0;
        createMainFbo();
        createCloudFbos();
        currentFroxelQuality = -1;
    }

    private void renderOutput(float contrast, float exposure) {
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);

        glUseProgram(outputProgram);
        glUniform1i(outputTexLocation, 0);
        glUniform1i(outputUseTonemapLocation, 1);
        glUniform1f(outputContrastLocation, contrast);
        glUniform1f(outputExposureLocation, exposure);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, mainColorTex);

        glBindVertexArray(skyVao);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D, 0);

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
    }

    private int createCloudProgram() {
        String vertexShader = """
                #version 430 core
                layout(location = 0) in vec2 aPosition;
                out vec2 vPosition;
                void main() {
                    vPosition = aPosition;
                    gl_Position = vec4(aPosition, 0.998, 1.0);
                }
                """;

        String fragmentShader = """
                #version 430 core
                in vec2 vPosition;

                uniform vec3 uCameraPos;
                uniform vec3 uViewRight;
                uniform vec3 uViewUp;
                uniform vec3 uViewForward;
                uniform float uHScreenScale;
                uniform float uVScreenScale;
                uniform vec3 uSunDir;
                uniform vec3 uSunColor;
                uniform float uTime;
                uniform float uCoverage;
                uniform float uBaseAlt;
                uniform float uTopAlt;
                uniform vec2 uWindDir;
                uniform float uWindSpeed;

                out vec4 fragColor;

                const float PI = 3.14159265;

                float hash(vec3 p) {
                    p = fract(p * 0.3183099 + 0.1);
                    p *= 17.0;
                    return fract(p.x * p.y * p.z * (p.x + p.y + p.z));
                }

                float noise3(vec3 p) {
                    vec3 i = floor(p);
                    vec3 f = fract(p);
                    f = f * f * (3.0 - 2.0 * f);
                    float a = hash(i);
                    float b = hash(i + vec3(1.0, 0.0, 0.0));
                    float c = hash(i + vec3(0.0, 1.0, 0.0));
                    float d = hash(i + vec3(1.0, 1.0, 0.0));
                    float e = hash(i + vec3(0.0, 0.0, 1.0));
                    float f_ = hash(i + vec3(1.0, 0.0, 1.0));
                    float g = hash(i + vec3(0.0, 1.0, 1.0));
                    float h = hash(i + vec3(1.0, 1.0, 1.0));
                    float mix1 = mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
                    float mix2 = mix(mix(e, f_, f.x), mix(g, h, f.x), f.y);
                    return mix(mix1, mix2, f.z);
                }

                float fbm(vec3 p) {
                    float value = 0.0;
                    float amp = 0.5;
                    float freq = 1.0;
                    for (int i = 0; i < 5; i++) {
                        value += amp * noise3(p * freq);
                        freq *= 2.1;
                        amp *= 0.5;
                    }
                    return value;
                }

                float cloudDensity(vec3 p) {
                    vec3 windOffset = vec3(uWindDir * uWindSpeed * uTime, 0.0);
                    vec3 pos = p + windOffset;

                    float base = fbm(pos * 0.0012);
                    if (base < 0.03) return 0.0;

                    vec3 warp = vec3(
                        noise3(pos * 0.008 + 100.0),
                        noise3(pos * 0.008 + 200.0),
                        0.0
                    ) * 2.5;
                    pos += warp;

                    float detail = 0.0;
                    float damp = 0.8;
                    float dfreq = 0.025;
                    for (int i = 0; i < 4; i++) {
                        detail += damp * noise3(pos * dfreq + vec3(0.0, uTime * 0.006, 0.0));
                        dfreq *= 2.5;
                        damp *= 0.45;
                    }

                    float height = smoothstep(uBaseAlt, uTopAlt, p.y);
                    float density = max(base - 0.2, 0.0) * 2.0;
                    density *= 1.0 - smoothstep(0.3, 0.9, detail);
                    density *= height;

                    float weather = noise3(p * 0.0005 + 50.0);
                    density *= smoothstep(1.0 - uCoverage * 0.9, 1.0, weather);

                    return max(density - 0.02, 0.0);
                }

                float henyeyGreenstein(float cosTheta, float g) {
                    float gg = g * g;
                    return (1.0 - gg) / (4.0 * PI * pow(1.0 + gg - 2.0 * g * cosTheta, 1.5));
                }

                void main() {
                    vec3 rayDir = normalize(uViewForward + uViewRight * vPosition.x * uHScreenScale + uViewUp * vPosition.y * uVScreenScale);

                    float tNear = (uBaseAlt - uCameraPos.y) / rayDir.y;
                    float tFar = (uTopAlt - uCameraPos.y) / rayDir.y;
                    if (rayDir.y < 0.0) {
                        float tmp = tNear; tNear = tFar; tFar = tmp;
                    }
                    tNear = max(tNear, 0.0);
                    float maxDist = 180.0;
                    tFar = min(tFar, maxDist);
                    if (tNear >= tFar) {
                        fragColor = vec4(0.0);
                        return;
                    }

                    float stepSize = (tFar - tNear) / 32.0;
                    float jitter = fract(sin(dot(vPosition, vec2(12.989, 78.233))) * 43758.5453);
                    float t = tNear + jitter * stepSize;

                    vec3 accumulatedLight = vec3(0.0);
                    float transmittance = 1.0;
                    float extinction = 0.04;

                    vec3 sunDirNorm = normalize(-uSunDir);

                    for (int i = 0; i < 32; i++) {
                        vec3 pos = uCameraPos + rayDir * t;
                        float density = cloudDensity(pos);

                        if (density > 0.001) {
                            float cosTheta = dot(rayDir, sunDirNorm);
                            float phase = henyeyGreenstein(cosTheta, 0.6);

                            vec3 lightPos = pos + sunDirNorm * 30.0;
                            float shadowDensity = cloudDensity(lightPos) * 0.4;
                            shadowDensity += cloudDensity(lightPos + sunDirNorm * 30.0) * 0.3;
                            shadowDensity += cloudDensity(lightPos + sunDirNorm * 60.0) * 0.2;
                            shadowDensity += cloudDensity(lightPos + sunDirNorm * 90.0) * 0.1;
                            float lightTrans = exp(-shadowDensity * 0.5);

                            float scatter = density * phase * 1.2;
                            float alpha = density * stepSize * extinction;
                            vec3 contrib = uSunColor * scatter * lightTrans * stepSize * 0.5;

                            vec3 ambient = vec3(0.15, 0.20, 0.30) * density * 0.06 * stepSize;

                            accumulatedLight += (contrib + ambient) * transmittance;
                            transmittance *= exp(-alpha);
                        }

                        t += stepSize;
                        if (transmittance < 0.005) break;
                    }

                    float horizonFade = smoothstep(0.0, 0.04, rayDir.y);
                    float cloudAlpha = (1.0 - transmittance) * horizonFade;

                    fragColor = vec4(accumulatedLight, clamp(cloudAlpha, 0.0, 1.0));
                }
                """;

        return createProgram(vertexShader, fragmentShader);
    }

    private int createCompositeProgram() {
        String vertexShader = """
                #version 430 core
                layout(location = 0) in vec2 aPosition;
                out vec2 vUv;
                void main() {
                    vUv = aPosition * 0.5 + 0.5;
                    gl_Position = vec4(aPosition, 0.0, 1.0);
                }
                """;

        String fragmentShader = """
                #version 430 core
                in vec2 vUv;
                uniform sampler2D uSkyTex;
                uniform sampler2D uCloudTex;
                out vec4 fragColor;
                void main() {
                    vec3 skyColor = texture(uSkyTex, vUv).rgb;
                    vec4 cloud = texture(uCloudTex, vUv);
                    vec3 result = cloud.rgb + skyColor * (1.0 - cloud.a);
                    fragColor = vec4(result, 1.0);
                }
                """;

        return createProgram(vertexShader, fragmentShader);
    }

    private int createOutputProgram() {
        String vertexShader = """
                #version 430 core
                layout(location = 0) in vec2 aPosition;
                out vec2 vUv;
                void main() {
                    vUv = aPosition * 0.5 + 0.5;
                    gl_Position = vec4(aPosition, 0.0, 1.0);
                }
                """;

        String fragmentShader = """
                #version 430 core
                in vec2 vUv;
                uniform sampler2D uTex;
                uniform int uUseTonemap;
                out vec4 fragColor;
                uniform float uContrast;
                uniform float uExposure;
                void main() {
                    vec3 color = texture(uTex, vUv).rgb;
                    color *= uExposure;
                    if (uUseTonemap == 1) {
                        color = color / (color + vec3(1.0));
                        color = pow(color, vec3(1.0 / 2.2));
                        color = (color - 0.5) * uContrast + 0.5;
                    }
                    fragColor = vec4(color, 1.0);
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

    private static final String PARTICLE_COMPUTE_SOURCE = """
            #version 430 core
            layout(local_size_x = 256) in;
            layout(std430, binding = 0) buffer ParticleBuffer {
                float particles[];
            };
            uniform float uDelta;
            void main() {
                uint idx = gl_GlobalInvocationID.x;
                uint base = idx * 12u;
                if (base + 11u >= particles.length()) return;
                float life = particles[base + 3u];
                if (life <= 0.0) return;
                float vy = particles[base + 5u];
                vy -= 15.0 * uDelta;
                particles[base + 0u] += particles[base + 4u] * uDelta;
                particles[base + 1u] += vy * uDelta;
                particles[base + 2u] += particles[base + 6u] * uDelta;
                particles[base + 5u] = vy;
                particles[base + 3u] -= uDelta;
            }
            """;

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
