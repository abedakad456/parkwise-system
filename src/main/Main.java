package main;

import boundary.mainDashBoard;
import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        // Set a modern Look and Feel (Nimbus) for the whole app
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus isn't available, fall back to default
            System.err.println("Could not apply Nimbus Look and Feel: " + e.getMessage());
        }

        // Launch the Application
        SwingUtilities.invokeLater(() -> {
            try {
            	mainDashBoard launcher = new mainDashBoard();
                launcher.setVisible(true);
                System.out.println("ParkWise System launched successfully.");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Critical Error: " + e.getMessage());
            }
        });
    }
}