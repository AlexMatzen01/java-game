package util;

public final class TickSystem {
    public static final int TICKS_PER_SECOND = 20;
    public static final float TICK_SECONDS = 1.0f / TICKS_PER_SECOND;
    private static final long DAY_TICKS = 12L * 60L * TICKS_PER_SECOND;
    private static final long NIGHT_TICKS = 12L * 60L * TICKS_PER_SECOND;
    private static final long CYCLE_TICKS = DAY_TICKS + NIGHT_TICKS;
    private static final double CYCLE_SECONDS = CYCLE_TICKS * TICK_SECONDS;

    private long worldTicks;
    private double accumulator;
    private double cycleSeconds;

    public int consumeTicks(double deltaSeconds) {
        cycleSeconds += deltaSeconds;
        if (cycleSeconds >= CYCLE_SECONDS) {
            cycleSeconds %= CYCLE_SECONDS;
        }

        accumulator += deltaSeconds;
        int ticks = 0;
        while (accumulator >= TICK_SECONDS) {
            accumulator -= TICK_SECONDS;
            worldTicks++;
            ticks++;
        }
        return ticks;
    }

    public long getWorldTicks() {
        return worldTicks;
    }

    public double getCycleSeconds() {
        return cycleSeconds;
    }

    public void restoreState(long ticks, double cycleSeconds) {
        this.worldTicks = Math.max(0L, ticks);
        this.cycleSeconds = cycleSeconds % CYCLE_SECONDS;
        if (this.cycleSeconds < 0.0) {
            this.cycleSeconds += CYCLE_SECONDS;
        }
        this.accumulator = 0.0;
    }

    public float getCycleProgress() {
        return (float) (cycleSeconds / CYCLE_SECONDS);
    }

    public float getSkyBrightness() {
        float angle = (float) (getCycleProgress() * Math.PI * 2.0);
        float normalized = (float) ((Math.sin(angle - Math.PI / 2.0) + 1.0) * 0.5);
        return 0.15f + 0.85f * normalized;
    }

    public boolean isDaytime() {
        return getSkyBrightness() > 0.5f;
    }

    public void setCycleSeconds(double seconds) {
        this.cycleSeconds = seconds % CYCLE_SECONDS;
        if (this.cycleSeconds < 0.0) {
            this.cycleSeconds += CYCLE_SECONDS;
        }
        this.accumulator = 0.0;
    }

    public float getSunAngleRadians() {
        return (float) (getCycleProgress() * Math.PI * 2.0 - Math.PI / 2.0);
    }
}
