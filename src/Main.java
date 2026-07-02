
import view.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

// The absolute entry point of the Java Cafe POS application.
// This single class is responsible for bootstrapping the entire system when the user double-clicks the jar or runs the script.
// It handles the very initial setup, configures the visual styling of the operating system,
// and safely launches the first graphical window on the correct processor thread.
// It implements Runnable to avoid creating anonymous inner classes.
public class Main implements Runnable {

    // The main execution method called by the Java Virtual Machine.
    public static void main(String[] args) {
        // Step 1: Apply native and modern Look and Feel.
        // We iterate through the installed visual themes trying to find "Nimbus",
        // which provides a much cleaner, modernized look compared to the old gray Metal default.
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            // Overrides the default font across all buttons, labels, and tables to "Segoe UI" for a more premium feel.
            UIManager.getLookAndFeelDefaults().put("defaultFont", new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        } catch (Exception e) {
            // If the modern Look and Feel fails to load, we quietly catch the exception.
            // The system will just fall back to the default OS theme, which is not a critical error.
        }

        // Step 2: Start the graphical interface.
        // In Java Swing, all graphical components must be created and modified strictly
        // inside the Event Dispatch Thread (EDT) to prevent freezing and screen tearing.
        // invokeLater ensures our main window is pushed into this dedicated thread.
        SwingUtilities.invokeLater(new Main());
    }

    // Overridden run method required by the Runnable interface.
    // This executes on the Swing Event Dispatch Thread.
    @Override
    public void run() {
        // Instantiates the main container window that holds all the tabs and panels.
        MainFrame frame = new MainFrame();
        // Finally, reveals the application to the user.
        frame.setVisible(true);
    }
}
