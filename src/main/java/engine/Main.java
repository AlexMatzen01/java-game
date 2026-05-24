package engine;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        if ("true".equalsIgnoreCase(System.getProperty("java.awt.headless"))) {
            System.setProperty("java.awt.headless", "false");
        }

        WorldManager.WorldSlot slot = WorldLauncher.chooseWorldSlot();
        if (slot == null) {
            return;
        }

        JDialog loading = WorldLauncher.showLoadingDialog(slot.name());
        WorldManager.LoadedWorld loadedWorld;
        try {
            loadedWorld = WorldManager.loadWorld(slot);
        } finally {
            if (loading != null) {
                SwingUtilities.invokeLater(loading::dispose);
            }
        }

        new Game(
                GameOptions.fromArgs(args),
                loadedWorld.world(),
                loadedWorld.player(),
                loadedWorld.ticks(),
                slot
        ).run();
    }
}
