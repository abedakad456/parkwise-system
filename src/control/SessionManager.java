package control;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;

import dbconnections.DatabaseConnection;

public class SessionManager {

    // --- PRICING CONSTANTS ---
    private static final double PRICE_FIRST_HOUR = 10.0;
    private static final double PRICE_ADDITIONAL_HOUR = 5.0;
    private static final double PRICE_FULL_DAY = 50.0;
    private static final double CLUB_MEMBER_DISCOUNT = 0.20; // 20% discount
    private static final String RATE_APPLIED_TEXT = "Std Rate: 10/1st + 5/add"; 
    private static final String RATE_APPLIED_MEMBER_TEXT = "Member Rate: 20% OFF"; 
    private static final int CURRENT_LOT_ID = 1;
    
    // --- TESTING: 5 seconds = 1 hour ---
    private static final int SECONDS_PER_HOUR = 5; // Change to 3600 for real time

    /**
     * 1. VEHICLE ENTRY
     */
    public boolean vehicleEntry(String licensePlate) {
        Connection conn = null;
        PreparedStatement pstmtCheck = null;
        PreparedStatement pstmtMaxId = null;
        PreparedStatement pstmtSpace = null;
        PreparedStatement pstmtInsert = null;
        ResultSet rsCheck = null;
        ResultSet rsMaxId = null;
        ResultSet rsSpace = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return false;
            
            // Check if vehicle is already inside
            String checkSql = "SELECT sessionID FROM TblSessions WHERE licensePlate = ? AND endTime IS NULL";
            pstmtCheck = conn.prepareStatement(checkSql);
            pstmtCheck.setString(1, licensePlate);
            rsCheck = pstmtCheck.executeQuery();
            if (rsCheck.next()) {
                return false;
            }
            rsCheck.close();
            pstmtCheck.close();
            
            // Get next session ID
            String maxIdSql = "SELECT MAX(sessionID) FROM TblSessions";
            pstmtMaxId = conn.prepareStatement(maxIdSql);
            rsMaxId = pstmtMaxId.executeQuery();
            int newID = 1;
            if (rsMaxId.next()) {
                newID = rsMaxId.getInt(1) + 1;
            }
            rsMaxId.close();
            pstmtMaxId.close();
            
            // Get a random VALID space ID from TblSpaces
            String spaceSql = "SELECT spaceID FROM TblSpaces WHERE lotNumber = ? ORDER BY RAND()";
            pstmtSpace = conn.prepareStatement(spaceSql);
            pstmtSpace.setInt(1, CURRENT_LOT_ID);
            rsSpace = pstmtSpace.executeQuery();
            
            int spaceID = 1; // Default
            if (rsSpace.next()) {
                spaceID = rsSpace.getInt("spaceID");
            }
            rsSpace.close();
            pstmtSpace.close();
            
            // Insert new session
            String insertSql = "INSERT INTO TblSessions (sessionID, licensePlate, lotNumber, startTime, finalSpaceID) VALUES (?, ?, ?, ?, ?)";
            pstmtInsert = conn.prepareStatement(insertSql);
            pstmtInsert.setInt(1, newID);
            pstmtInsert.setString(2, licensePlate);
            pstmtInsert.setInt(3, CURRENT_LOT_ID);
            pstmtInsert.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            pstmtInsert.setInt(5, spaceID);
            
            int rows = pstmtInsert.executeUpdate();
            return rows > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try { if (rsCheck != null) rsCheck.close(); } catch (SQLException e) {}
            try { if (rsMaxId != null) rsMaxId.close(); } catch (SQLException e) {}
            try { if (rsSpace != null) rsSpace.close(); } catch (SQLException e) {}
            try { if (pstmtCheck != null) pstmtCheck.close(); } catch (SQLException e) {}
            try { if (pstmtMaxId != null) pstmtMaxId.close(); } catch (SQLException e) {}
            try { if (pstmtSpace != null) pstmtSpace.close(); } catch (SQLException e) {}
            try { if (pstmtInsert != null) pstmtInsert.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }
    }

    /**
     * 2. CALCULATE PAYMENT
     * Now with: 5 seconds = 1 hour AND club member discount
     */
    public double calculatePayment(String licensePlate) {
        String sql = "SELECT startTime FROM TblSessions WHERE licensePlate = ? AND endTime IS NULL";
        double finalAmount = -1.0; 

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            if (conn == null) return -1.0;

            pstmt.setString(1, licensePlate);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Timestamp startTs = rs.getTimestamp("startTime");
                LocalDateTime startTime = startTs.toLocalDateTime();
                LocalDateTime endTime = LocalDateTime.now();

                // Calculate hours using SECONDS_PER_HOUR (5 seconds = 1 hour for testing)
                long seconds = Duration.between(startTime, endTime).getSeconds();
                int totalHours = (int) Math.ceil((double) seconds / SECONDS_PER_HOUR);
                if (totalHours == 0) totalHours = 1;

                // Calculate base price
                if (totalHours == 1) {
                    finalAmount = PRICE_FIRST_HOUR;
                } else {
                    finalAmount = PRICE_FIRST_HOUR + ((totalHours - 1) * PRICE_ADDITIONAL_HOUR);
                }
                if (finalAmount > PRICE_FULL_DAY) finalAmount = PRICE_FULL_DAY;

                // Apply club member discount if applicable
                if (isClubMember(licensePlate)) {
                    finalAmount = finalAmount * (1 - CLUB_MEMBER_DISCOUNT);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return finalAmount;
    }

    /**
     * 3. PROCESS PAYMENT & EXIT
     */
    public boolean processPayment(String licensePlate) {
        int sessionID = getActiveSessionID(licensePlate);
        double amount = calculatePayment(licensePlate);
        if (sessionID == -1 || amount < 0) return false;

        if (receiptExists(sessionID)) {

            String closeSessionSql =
                "UPDATE TblSessions SET endTime = ? WHERE sessionID = ? AND endTime IS NULL";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pst = conn.prepareStatement(closeSessionSql)) {

                pst.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                pst.setInt(2, sessionID);
                pst.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }

            System.out.println("Payment already processed. Session closed.");
            return true;
        }

        LocalDateTime now = LocalDateTime.now();
        Timestamp exitTime = Timestamp.valueOf(now);
        Timestamp paymentDate = Timestamp.valueOf(now);

        String updateSessionSql = "UPDATE TblSessions SET endTime = ? WHERE sessionID = ?";
        String insertReceiptSql = "INSERT INTO TblReceipts (receiptID, sessionID, totalHours, rateApplied, finalAmount, paymentDate) VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Get start time to calculate hours
            String getStartSql = "SELECT startTime FROM TblSessions WHERE sessionID = ?";
            int totalHours = 1;
            try (PreparedStatement pstGetStart = conn.prepareStatement(getStartSql)) {
                pstGetStart.setInt(1, sessionID);
                ResultSet rs = pstGetStart.executeQuery();
                if (rs.next()) {
                    Timestamp startTs = rs.getTimestamp("startTime");
                    LocalDateTime startTime = startTs.toLocalDateTime();
                    long seconds = Duration.between(startTime, now).getSeconds();
                    totalHours = (int) Math.ceil((double) seconds / SECONDS_PER_HOUR);
                    if (totalHours == 0) totalHours = 1;
                }
            }

            // Determine rate applied text
            boolean isMember = isClubMember(licensePlate);
            String rateText = isMember ? RATE_APPLIED_MEMBER_TEXT : RATE_APPLIED_TEXT;

            // 1. Update Session
            try (PreparedStatement pstUpdate = conn.prepareStatement(updateSessionSql)) {
                pstUpdate.setTimestamp(1, exitTime);
                pstUpdate.setInt(2, sessionID);
                pstUpdate.executeUpdate();
            }

            // 2. Insert Receipt
            int newReceiptID = getNextID(conn, "TblReceipts", "receiptID");

            try (PreparedStatement pstReceipt = conn.prepareStatement(insertReceiptSql)) {
                pstReceipt.setInt(1, newReceiptID);
                pstReceipt.setInt(2, sessionID);
                pstReceipt.setInt(3, totalHours);
                pstReceipt.setString(4, rateText);
                pstReceipt.setDouble(5, amount);
                pstReceipt.setTimestamp(6, paymentDate);
                pstReceipt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {}
        }
    }

    // --- HELPER: CHECK IF CLIENT IS CLUB MEMBER ---
    private boolean isClubMember(String licensePlate) {
        // First get clientID from TblClients using license plate
        String getClientSql = "SELECT clientID FROM TblVehicles WHERE licensePlate = ?";
        String checkMemberSql = "SELECT memberID FROM TblClupMembers WHERE clientID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstClient = conn.prepareStatement(getClientSql)) {
            
            if (conn == null) return false;
            
            pstClient.setString(1, licensePlate);
            ResultSet rsClient = pstClient.executeQuery();
            
            if (rsClient.next()) {
                int clientID = rsClient.getInt("clientID");
                
                // Check if this client is a club member
                try (PreparedStatement pstMember = conn.prepareStatement(checkMemberSql)) {
                    pstMember.setInt(1, clientID);
                    ResultSet rsMember = pstMember.executeQuery();
                    return rsMember.next(); // Returns true if member exists
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean receiptExists(int sessionID) {
        String sql = "SELECT receiptID FROM TblReceipts WHERE sessionID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) return false;
            pstmt.setInt(1, sessionID);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private int getNextID(Connection conn, String tableName, String idColumn) {
        String sql = "SELECT MAX([" + idColumn + "]) FROM " + tableName;
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                int max = rs.getInt(1);
                return max + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    private int getActiveSessionID(String licensePlate) {
        String sql = "SELECT sessionID FROM TblSessions WHERE licensePlate = ? AND endTime IS NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) return -1;
            pstmt.setString(1, licensePlate);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("sessionID");
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }
}