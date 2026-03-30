package control;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;
import dbconnections.DatabaseConnection;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;

/**
 * Control Class: Manages the generation of the Annual Summary Report.
 * Works both in Eclipse and as a runnable JAR.
 */
public class ReportController {
    
    public void generateAnnualSummary(int reportYear) {
        Connection conn = null;
        try {
            // 1. Get the Database Connection
            conn = DatabaseConnection.getConnection();
            if (conn == null) {
                System.err.println("Failed to get database connection!");
                return;
            }
            
            // 2. Load the compiled Jasper file
            JasperReport jasperReport = loadJasperReport();
            if (jasperReport == null) {
                System.err.println("Failed to load Jasper report file!");
                return;
            }
            
            // 3. Pass the year parameter to Jasper
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("ReportYear", reportYear);
            
            // 4. Fill the report with data
            JasperPrint jp = JasperFillManager.fillReport(jasperReport, parameters, conn);
            
            // 5. Determine output directory (user's home for guaranteed write access)
            String outputDir = getOutputDirectory();
            String pdfPath = outputDir + File.separator + "Annual_Summary_" + reportYear + ".pdf";
            String xmlPath = outputDir + File.separator + "Annual_Summary_" + reportYear + ".xml";
            
            // 6. Export to PDF and XML
            JasperExportManager.exportReportToPdfFile(jp, pdfPath);
            JasperExportManager.exportReportToXmlFile(jp, xmlPath, false);
            
            // 7. Confirm success with absolute paths
            System.out.println("✓ PDF Report saved to: " + pdfPath);
            System.out.println("✓ XML Report saved to: " + xmlPath);
            
            // 8. Optional: Preview the result
            // JasperViewer.viewReport(jp, false);
            
        } catch (JRException e) {
            System.err.println("Jasper Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // CRITICAL: Always close the connection!
            if (conn != null) {
                try {
                    conn.close();
                    System.out.println("Database connection closed.");
                } catch (Exception e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Load the Jasper report file from multiple possible locations.
     * Tries: 1) Classpath (works in JAR), 2) Current directory, 3) Project root
     */
    private JasperReport loadJasperReport() {
        try {
            // METHOD 1: Try loading from classpath (works when .jasper is packaged in JAR)
            InputStream reportStream = getClass().getClassLoader()
                .getResourceAsStream("AnnualSummary.jasper");
            
            if (reportStream != null) {
                System.out.println("Loading report from classpath (JAR)...");
                return (JasperReport) JRLoader.loadObject(reportStream);
            }
            
            // METHOD 2: Try current working directory
            String currentDir = System.getProperty("user.dir") + File.separator + "AnnualSummary.jasper";
            File reportFile = new File(currentDir);
            
            if (reportFile.exists()) {
                System.out.println("Loading report from: " + currentDir);
                return (JasperReport) JRLoader.loadObjectFromFile(currentDir);
            }
            
            // METHOD 3: Try project root (for Eclipse development)
            String projectRoot = "AnnualSummary.jasper";
            reportFile = new File(projectRoot);
            
            if (reportFile.exists()) {
                System.out.println("Loading report from project root: " + projectRoot);
                return (JasperReport) JRLoader.loadObjectFromFile(projectRoot);
            }
            
            System.err.println("Could not find AnnualSummary.jasper in any location!");
            System.err.println("Searched:");
            System.err.println("  - Classpath (inside JAR)");
            System.err.println("  - " + currentDir);
            System.err.println("  - " + new File(projectRoot).getAbsolutePath());
            
        } catch (JRException e) {
            System.err.println("Error loading Jasper report: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get a reliable output directory that works in both Eclipse and JAR.
     * Priority: 1) User's Documents folder, 2) User's home, 3) Current directory
     */
    private String getOutputDirectory() {
        // Try to use user's Documents folder for easy access
        String documentsPath = System.getProperty("user.home") + File.separator + "Documents" 
            + File.separator + "ParkWiseReports";
        
        File documentsDir = new File(documentsPath);
        if (documentsDir.exists() || documentsDir.mkdirs()) {
            System.out.println("Output directory: " + documentsPath);
            return documentsPath;
        }
        
        // Fallback to user's home directory
        String homePath = System.getProperty("user.home");
        System.out.println("Output directory: " + homePath);
        return homePath;
    }
}