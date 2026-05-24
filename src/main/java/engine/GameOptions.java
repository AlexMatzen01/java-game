package engine;

public record GameOptions(int fpsCap, boolean vSync) {
    public GameOptions {
        if (fpsCap < 0) {
            throw new IllegalArgumentException("fpsCap must be non-negative");
        }
    }

    public static GameOptions defaults() {
        return new GameOptions(0, false);
    }

    public static GameOptions fromArgs(String[] args) {
        int fpsCap = 0;
        boolean vSync = false;

        for (String arg : args) {
            if (arg.startsWith("--fps-cap=")) {
                fpsCap = parseFpsCap(arg.substring("--fps-cap=".length()));
            } else if (arg.startsWith("--fps=")) {
                fpsCap = parseFpsCap(arg.substring("--fps=".length()));
            } else if (arg.equals("--v-sync") || arg.equals("--vsync")) {
                vSync = true;
            } else if (arg.equals("--no-v-sync") || arg.equals("--no-vsync")) {
                vSync = false;
            } else if (arg.startsWith("--v-sync=")) {
                vSync = Boolean.parseBoolean(arg.substring("--v-sync=".length()));
            } else if (arg.startsWith("--vsync=")) {
                vSync = Boolean.parseBoolean(arg.substring("--vsync=".length()));
            }
        }

        return new GameOptions(fpsCap, vSync);
    }

    private static int parseFpsCap(String value) {
        String normalized = value.trim().toLowerCase();
        if (normalized.startsWith("cap=")) {
            normalized = normalized.substring("cap=".length()).trim();
        }
        if (normalized.isEmpty() || normalized.equals("off") || normalized.equals("unlimited") || normalized.equals("none")) {
            return 0;
        }
        return Integer.parseInt(normalized);
    }
}