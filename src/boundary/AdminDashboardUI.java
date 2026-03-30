package boundary;

import control.ReportController;
import javax.swing.*;
import java.awt.*;
import java.io.File;

public class AdminDashboardUI extends JFrame {
    private JTextField yearInput;
    private JTextArea outputArea;
    private ReportController controller;
    
    public AdminDashboardUI() {
        controller = new ReportController();
        setTitle("ParkWise Admin Dashboard");
        setSize(600, 400);
        setLayout(new BorderLayout(10, 10));
        
        // Top panel for input
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(new JLabel("Enter Year for Report:"));
        yearInput = new JTextField(10);
        inputPanel.add(yearInput);
        
        JButton btnGenerate = new JButton("Generate Annual Report");
        btnGenerate.addActionListener(e -> generateReport());
        inputPanel.add(btnGenerate);
        
        add(inputPanel, BorderLayout.NORTH);
        
        // Center panel for output messages
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        outputArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(outputArea);
        add(scrollPane, BorderLayout.CENTER);
        
        // Bottom panel with "Open Folder" button
        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton btnOpenFolder = new JButton("Open Reports Folder");
        btnOpenFolder.addActionListener(e -> openReportsFolder());
        bottomPanel.add(btnOpenFolder);
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Show initial path info
        displayInitialInfo();
        
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen
    }
    
    private void displayInitialInfo() {
        String outputDir = getOutputDirectory();
        outputArea.append("═══════════════════════════════════════════════════\n");
        outputArea.append("  ParkWise Admin Dashboard - Report Generator\n");
        outputArea.append("═══════════════════════════════════════════════════\n\n");
        outputArea.append("Reports will be saved to:\n");
        outputArea.append("📁 " + outputDir + "\n\n");
        outputArea.append("Ready to generate reports.\n");
        outputArea.append("═══════════════════════════════════════════════════\n\n");
    }
    
    private void generateReport() {
        try {
            int year = Integer.parseInt(yearInput.getText().trim());
            
            outputArea.append("\n🔄 Generating report for year " + year + "...\n");
            controller.generateAnnualSummary(year);
            
            String outputDir = getOutputDirectory();
            String pdfPath = outputDir + File.separator + "Annual_Summary_" + year + ".pdf";
            String xmlPath = outputDir + File.separator + "Annual_Summary_" + year + ".xml";
            
            outputArea.append("✅ Report generated successfully!\n\n");
            outputArea.append("📄 PDF Report:\n   " + pdfPath + "\n\n");
            outputArea.append("📄 XML Report:\n   " + xmlPath + "\n");
            outputArea.append("═══════════════════════════════════════════════════\n");
            
            // Scroll to bottom
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
            
            // Show success dialog
            JOptionPane.showMessageDialog(this, 
                "Report generated successfully!\n\nLocation:\n" + outputDir,
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
            
        } catch (NumberFormatException ex) {
            outputArea.append("❌ ERROR: Please enter a valid year (e.g., 2024)\n");
            outputArea.append("═══════════════════════════════════════════════════\n");
            JOptionPane.showMessageDialog(this, 
                "Please enter a valid year number!", 
                "Invalid Input", 
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            outputArea.append("❌ ERROR: " + ex.getMessage() + "\n");
            outputArea.append("═══════════════════════════════════════════════════\n");
            ex.printStackTrace();
        }
    }
    
    private void openReportsFolder() {
        try {
            String outputDir = getOutputDirectory();
            File folder = new File(outputDir);
            
            if (!folder.exists()) {
                folder.mkdirs();
            }
            
            // Open the folder in the system file explorer
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(folder);
                outputArea.append("📂 Opened reports folder in file explorer\n");
            } else {
                JOptionPane.showMessageDialog(this,
                    "Cannot open folder automatically.\n\nFolder location:\n" + outputDir,
                    "Folder Location",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            outputArea.append("❌ Could not open folder: " + ex.getMessage() + "\n");
            JOptionPane.showMessageDialog(this,
                "Could not open the folder.\n\nPlease navigate manually to:\n" + getOutputDirectory(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Get the output directory (same logic as ReportController)
     */
    private String getOutputDirectory() {
        String documentsPath = System.getProperty("user.home") + File.separator + "Documents" 
            + File.separator + "ParkWiseReports";
        
        File documentsDir = new File(documentsPath);
        if (documentsDir.exists() || documentsDir.mkdirs()) {
            return documentsPath;
        }
        
        return System.getProperty("user.home");
    }
}