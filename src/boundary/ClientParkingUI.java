package boundary;

import javax.swing.*;
import java.awt.*;
import control.SessionManager; // Ensure this imports your Control class

public class ClientParkingUI extends JFrame {

    private SessionManager controller;
    private JTextField txtLicensePlate;
    private JTextArea txtInfoLog; // To show messages like a real system log

    public ClientParkingUI() {
        // Initialize the Controller
        controller = new SessionManager();

        // Window Setup
        setTitle("ParkWise Gate System");
        setSize(500, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // --- Main Panel ---
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(245, 245, 245));

        // 1. Top Section: Input
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        topPanel.setOpaque(false);
        
        JLabel lblInstr = new JLabel("Enter License Plate Number:", SwingConstants.CENTER);
        lblInstr.setFont(new Font("Segoe UI", Font.BOLD, 16));
        topPanel.add(lblInstr);

        txtLicensePlate = new JTextField();
        txtLicensePlate.setFont(new Font("Monospaced", Font.BOLD, 24));
        txtLicensePlate.setHorizontalAlignment(JTextField.CENTER);
        topPanel.add(txtLicensePlate);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // 2. Middle Section: The Two Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        buttonPanel.setOpaque(false);

        JButton btnStart = new JButton("START PARKING");
        styleButton(btnStart, new Color(46, 204, 113)); // Green
        btnStart.addActionListener(e -> handleStartParking());

        JButton btnEnd = new JButton("END PARKING");
        styleButton(btnEnd, new Color(231, 76, 60)); // Red
        btnEnd.addActionListener(e -> handleEndParking());

        buttonPanel.add(btnStart);
        buttonPanel.add(btnEnd);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        // 3. Bottom Section: System Log
        txtInfoLog = new JTextArea(5, 20);
        txtInfoLog.setEditable(false);
        txtInfoLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        mainPanel.add(new JScrollPane(txtInfoLog), BorderLayout.SOUTH);

        add(mainPanel);
    }

    // --- LOGIC METHODS ---

    private void handleStartParking() {
        String plate = txtLicensePlate.getText().trim();
        if (plate.isEmpty()) {
            log("Error: Please enter a plate number.");
            return;
        }

        // 1. Call Controller to try and enter
        boolean success = controller.vehicleEntry(plate);

        if (success) {
            log("Gate Sensor: Vehicle " + plate + " entered successfully.");
            log("Gate opened.");
            txtLicensePlate.setText(""); // Clear for next car
        } else {
            // This happens if vehicle is ALREADY in the DB with status 'PENDING'
            log("Error: Vehicle " + plate + " is already inside!");
            JOptionPane.showMessageDialog(this, "Vehicle is already in the lot!", "Entry Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleEndParking() {
        String plate = txtLicensePlate.getText().trim();
        if (plate.isEmpty()) {
            log("Error: Please enter a plate number.");
            return;
        }

        // 1. Calculate Price First
        double price = controller.calculatePayment(plate);

        if (price == -1) {
            log("Error: Vehicle " + plate + " is NOT in the parking lot.");
            JOptionPane.showMessageDialog(this, "Vehicle not found!", "Exit Error", JOptionPane.WARNING_MESSAGE);
        } else {
            // 2. Show Price & Confirm Payment (Simulates Payment Gateway)
            String msg = String.format("Vehicle: %s\nDuration: (Calculated)\nAmount Due: $%.2f\n\nPay now?", plate, price);
            int choice = JOptionPane.showConfirmDialog(this, msg, "Payment Gateway", JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                // 3. Process Payment & Open Gate
                boolean paySuccess = controller.processPayment(plate);
                if (paySuccess) {
                    log("Payment approved: $" + price);
                    log("Gate Sensor: Vehicle " + plate + " exited.");
                    log("Gate opened.");
                    txtLicensePlate.setText("");
                } else {
                    log("Critical Error: Payment failed update.");
                }
            } else {
                log("Payment cancelled. Gate remains closed.");
            }
        }
    }

    private void log(String message) {
        txtInfoLog.append("> " + message + "\n");
        txtInfoLog.setCaretPosition(txtInfoLog.getDocument().getLength()); // Auto-scroll
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createRaisedBevelBorder());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientParkingUI().setVisible(true));
    }
}