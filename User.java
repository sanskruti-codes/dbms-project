import java.sql.*;

public class User {
    private int id;
    private String name;
    private String email;
    private String password;

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public static String authenticate(String email, String password) {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return "Email and password cannot be empty";
        }

        System.out.println("Attempting to authenticate user: " + email);
        String emailCheckQuery = "SELECT password FROM users WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                return "Unable to connect to database";
            }

            try (PreparedStatement pstmt = conn.prepareStatement(emailCheckQuery)) {
                pstmt.setString(1, email.trim());

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("User not found with email: " + email);
                        return "Invalid credentials";
                    }

                    String storedPassword = rs.getString("password");
                    if (!password.equals(storedPassword)) {
                        System.out.println("Invalid password for email: " + email);
                        return "Invalid credentials";
                    }

                    System.out.println("Authentication successful for user: " + email);
                    return "success";
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error during authentication: " + e.getMessage());
            e.printStackTrace();
            return "Database error: Please try again later";
        }
    }

    public String register() {
        // First check if email already exists
        String checkEmailQuery = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement checkStmt = conn.prepareStatement(checkEmailQuery)) {
                checkStmt.setString(1, email);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return "Email already registered";
                }
            }

            // If email doesn't exist, proceed with registration
            String query = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, name);
                pstmt.setString(2, email);
                pstmt.setString(3, password);
                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0 ? "success" : "Registration failed";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error: " + e.getMessage();
        }
    }

    public static int getUserId(String email) {
        String query = "SELECT id FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String deleteAccount(int userId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // First check if user exists
            String checkUserQuery = "SELECT COUNT(*) FROM users WHERE id = ?";
            try (PreparedStatement checkUserStmt = conn.prepareStatement(checkUserQuery)) {
                checkUserStmt.setInt(1, userId);
                ResultSet rs = checkUserStmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    return "User not found";
                }
            }

            // Then check if user has any bookings
            String checkBookingsQuery = "SELECT COUNT(*) FROM bookings WHERE user_id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkBookingsQuery)) {
                checkStmt.setInt(1, userId);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return "Cannot delete user: Please cancel all bookings first";
                }
            }

            // If no bookings exist, proceed with deletion
            String deleteQuery = "DELETE FROM users WHERE id = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
                deleteStmt.setInt(1, userId);
                int rowsAffected = deleteStmt.executeUpdate();
                if (rowsAffected > 0) {
                    conn.commit();
                    return "success";
                } else {
                    conn.rollback();
                    return "Failed to delete user";
                }
            }
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return "Database error: " + e.getMessage();
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}