package engine;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.util.List;

public final class WorldLauncher {
    private WorldLauncher() {
    }

    public static WorldManager.WorldSlot chooseWorldSlot() {
        try {
            while (true) {
                Object[] options = {"New World", "Load World", "Cancel"};
                int choice = JOptionPane.showOptionDialog(
                        null,
                        "Choose what to do before entering the game:",
                        "World Select",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        options,
                        options[0]
                );

                if (choice == 0) {
                    String name = JOptionPane.showInputDialog(null, "Enter world name:", "New World", JOptionPane.PLAIN_MESSAGE);
                    if (name == null) {
                        continue;
                    }
                    try {
                        return WorldManager.createWorld(name);
                    } catch (IllegalArgumentException | IllegalStateException exception) {
                        JOptionPane.showMessageDialog(null, exception.getMessage(), "Cannot Create World", JOptionPane.ERROR_MESSAGE);
                    }
                } else if (choice == 1) {
                    List<WorldManager.WorldSlot> worlds = WorldManager.listWorlds();
                    if (worlds.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "No saved worlds found in the saves directory.", "No Worlds", JOptionPane.INFORMATION_MESSAGE);
                        continue;
                    }
                    String[] names = worlds.stream().map(WorldManager.WorldSlot::name).toArray(String[]::new);
                    String selected = (String) JOptionPane.showInputDialog(
                            null,
                            "Select a world to load:",
                            "Load World",
                            JOptionPane.PLAIN_MESSAGE,
                            null,
                            names,
                            names[0]
                    );
                    if (selected == null) {
                        continue;
                    }
                    for (WorldManager.WorldSlot slot : worlds) {
                        if (slot.name().equals(selected)) {
                            return slot;
                        }
                    }
                } else {
                    return null;
                }
            }
        } catch (HeadlessException exception) {
            return fallbackWorld();
        }
    }

    public static JDialog showLoadingDialog(String worldName) {
        try {
            JDialog dialog = new JDialog((Frame) null, "Loading", false);
            dialog.setLayout(new BorderLayout(10, 10));
            dialog.add(new javax.swing.JLabel("Loading world: " + worldName + "..."), BorderLayout.NORTH);

            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            dialog.add(progressBar, BorderLayout.CENTER);

            dialog.setSize(320, 110);
            dialog.setLocationRelativeTo(null);
            dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            dialog.setVisible(true);
            return dialog;
        } catch (HeadlessException exception) {
            return null;
        }
    }

    private static WorldManager.WorldSlot fallbackWorld() {
        List<WorldManager.WorldSlot> worlds = WorldManager.listWorlds();
        if (!worlds.isEmpty()) {
            return worlds.get(0);
        }
        return WorldManager.createWorld("World");
    }
}
