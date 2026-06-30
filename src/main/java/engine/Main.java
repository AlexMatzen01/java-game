package engine;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        if ("true".equalsIgnoreCase(System.getProperty("java.awt.headless"))) {
            System.setProperty("java.awt.headless", "false");
        }

        new Game(GameOptions.fromArgs(args)).run();
    }
}
