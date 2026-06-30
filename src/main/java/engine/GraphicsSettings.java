package engine;

public final class GraphicsSettings {
    private static final String[] NAMES = {
            "Lighting",
            "Exposure",
            "Ambient",
            "Contrast",
            "Shadows",
            "Shadow Radius",
            "Fog",
            "Sky Saturation",
            "Render Distance",
            "Build Budget"
    };

    private boolean realisticLighting = true;
    private float exposure = 0.70f;
    private float ambientBoost = 0.64f;
    private float contrast = 1.03f;
    private float shadowStrength = 0.58f;
    private float fogDensity = 0.48f;
    private float skySaturation = 0.92f;
    private int renderDistanceChunks = 4;
    private int chunkBuildBudget = 12;
    private int selectedIndex;

    public int count() {
        return NAMES.length;
    }

    public String name(int index) {
        return NAMES[index];
    }

    public int selectedIndex() {
        return selectedIndex;
    }

    public void selectNext() {
        selectedIndex = (selectedIndex + 1) % NAMES.length;
    }

    public void selectPrevious() {
        selectedIndex = (selectedIndex + NAMES.length - 1) % NAMES.length;
    }

    public void adjustSelected(float direction) {
        int step = direction >= 0.0f ? 1 : -1;
        switch (selectedIndex) {
            case 0 -> realisticLighting = !realisticLighting;
            case 1 -> exposure = clamp(exposure + direction * 0.05f, 0.55f, 1.70f);
            case 2 -> ambientBoost = clamp(ambientBoost + direction * 0.05f, 0.45f, 1.45f);
            case 3 -> contrast = clamp(contrast + direction * 0.04f, 0.75f, 1.45f);
            case 4 -> shadowStrength = clamp(shadowStrength + direction * 0.05f, 0.20f, 1.00f);
            case 5 -> shadowStrength = clamp(shadowStrength + direction * 0.15f, 0.20f, 3.00f);
            case 6 -> fogDensity = clamp(fogDensity + direction * 0.05f, 0.00f, 1.00f);
            case 7 -> skySaturation = clamp(skySaturation + direction * 0.05f, 0.60f, 1.40f);
            case 8 -> renderDistanceChunks = clamp(renderDistanceChunks + step, 2, 8);
            case 9 -> chunkBuildBudget = clamp(chunkBuildBudget + step * 2, 2, 32);
            default -> throw new IllegalStateException("Unknown graphics setting index: " + selectedIndex);
        }
    }

    public boolean realisticLighting() {
        return realisticLighting;
    }

    public float exposure() {
        return exposure;
    }

    public float ambientBoost() {
        return ambientBoost;
    }

    public float contrast() {
        return contrast;
    }

    public float shadowStrength() {
        return shadowStrength;
    }

    public float fogDensity() {
        return fogDensity;
    }

    public float skySaturation() {
        return skySaturation;
    }

    public int shadowFilterRadius() {
        if (!realisticLighting) {
            return 1;
        }
        return shadowStrength > 1.15f ? 3 : shadowStrength > 0.75f ? 2 : 1;
    }

    public int renderDistanceChunks() {
        return renderDistanceChunks;
    }

    public int chunkBuildBudget() {
        return chunkBuildBudget;
    }

    public String displayValue(int index) {
        return switch (index) {
            case 0 -> realisticLighting ? "ON" : "OFF";
            case 1 -> percent(exposure, 0.55f, 1.70f);
            case 2 -> percent(ambientBoost, 0.45f, 1.45f);
            case 3 -> percent(contrast, 0.75f, 1.45f);
            case 4 -> percent(shadowStrength, 0.20f, 1.00f);
            case 5 -> shadowStrength > 1.15f ? "HIGH" : shadowStrength > 0.75f ? "MED" : "LOW";
            case 6 -> percent(fogDensity, 0.00f, 1.00f);
            case 7 -> percent(skySaturation, 0.60f, 1.40f);
            case 8 -> renderDistanceChunks + " chunks";
            case 9 -> chunkBuildBudget + " / frame";
            default -> "";
        };
    }

    public float normalizedValue(int index) {
        return switch (index) {
            case 0 -> realisticLighting ? 1.0f : 0.0f;
            case 1 -> normalize(exposure, 0.55f, 1.70f);
            case 2 -> normalize(ambientBoost, 0.45f, 1.45f);
            case 3 -> normalize(contrast, 0.75f, 1.45f);
            case 4 -> normalize(shadowStrength, 0.20f, 1.00f);
            case 5 -> normalize(shadowStrength, 0.20f, 3.00f);
            case 6 -> normalize(fogDensity, 0.00f, 1.00f);
            case 7 -> normalize(skySaturation, 0.60f, 1.40f);
            case 8 -> normalize(renderDistanceChunks, 2.0f, 8.0f);
            case 9 -> normalize(chunkBuildBudget, 2.0f, 32.0f);
            default -> 0.0f;
        };
    }

    private static float normalize(float value, float min, float max) {
        return (value - min) / (max - min);
    }

    private static String percent(float value, float min, float max) {
        return Math.round(normalize(value, min, max) * 100.0f) + "%";
    }

    private static float normalize(int value, float min, float max) {
        return (value - min) / (max - min);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
