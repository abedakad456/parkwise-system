package boundary;

import javax.swing.*;
import java.awt.*;

public class mainDashBoard extends JFrame {

    public mainDashBoard() {
        // Window Setup
        setTitle("ParkWise Management System");
        setSize(450, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen

        // Main Panel with Padding
        JPanel mainPanel = new JPanel(new GridLayout(3, 1, 15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        mainPanel.setBackground(new Color(240, 248, 255)); // Alice Blue

        // 1. Title Label
        JLabel lblTitle = new JLabel("Welcome to ParkWise", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(25, 25, 112)); // Midnight Blue
        mainPanel.add(lblTitle);

        // 2. Administrator Button
        JButton btnAdmin = new JButton("Administrator Dashboard");
        styleButton(btnAdmin, new Color(70, 130, 180)); // Steel Blue
        btnAdmin.addActionListener(e -> {
            // Launch Admin Dashboard
            SwingUtilities.invokeLater(() -> {
                new AdminDashboardUI().setVisible(true);
            });
        });
        mainPanel.add(btnAdmin);

        // 3. Client Button
        JButton btnClient = new JButton("Client Parking Interface");
        styleButton(btnClient, new Color(60, 179, 113)); // Medium Sea Green
        btnClient.addActionListener(e -> {
            // Launch Client Interface
            SwingUtilities.invokeLater(() -> {
                new ClientParkingUI().setVisible(true);
            });
        });
        mainPanel.add(btnClient);

        // Add panel to frame
        add(mainPanel);
    }

    /**
     * Helper method to make buttons look professional
     */
    private void styleButton(JButton btn, Color bgColor) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(bgColor.darker(), 2));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}