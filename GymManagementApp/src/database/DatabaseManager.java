package database;

import java.sql.*;

/**
 * Quản lý kết nối và khởi tạo database
 */
public class DatabaseManager {
    
    private static DatabaseManager instance;
    private Connection connection;
    private static final String DB_URL = "jdbc:sqlite:gym_system.db";
    
    private DatabaseManager() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            createTables();
            insertSampleData();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Không thể kết nối database: " + e.getMessage());
        }
    }
    
    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    public Connection getConnection() {
        return connection;
    }
    
    /**
     * Tạo các bảng trong database
     */
    private void createTables() throws SQLException {
        Statement stmt = connection.createStatement();
        
        // Bảng hội viên
        stmt.execute("CREATE TABLE IF NOT EXISTS members (" +
            "member_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "card_id TEXT UNIQUE," +
            "full_name TEXT NOT NULL," +
            "birth_date TEXT," +
            "phone TEXT," +
            "address TEXT," +
            "photo BLOB," +
            "created_date TEXT," +
            "status TEXT DEFAULT 'active')");
        
        // Bảng gói tập
        stmt.execute("CREATE TABLE IF NOT EXISTS packages (" +
            "package_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "package_name TEXT NOT NULL," +
            "package_type TEXT," +
            "duration_days INTEGER," +
            "session_count INTEGER," +
            "price REAL," +
            "description TEXT)");
        
        // Bảng đăng ký gói
        stmt.execute("CREATE TABLE IF NOT EXISTS member_packages (" +
            "mp_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "member_id INTEGER," +
            "package_id INTEGER," +
            "start_date TEXT," +
            "end_date TEXT," +
            "sessions_remaining INTEGER," +
            "status TEXT DEFAULT 'active'," +
            "FOREIGN KEY(member_id) REFERENCES members(member_id)," +
            "FOREIGN KEY(package_id) REFERENCES packages(package_id))");
        
        // Bảng check-in/out
        stmt.execute("CREATE TABLE IF NOT EXISTS checkin_log (" +
            "log_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "member_id INTEGER," +
            "checkin_time TEXT," +
            "checkout_time TEXT," +
            "FOREIGN KEY(member_id) REFERENCES members(member_id))");
        
        // Bảng giao dịch
        stmt.execute("CREATE TABLE IF NOT EXISTS transactions (" +
            "transaction_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "member_id INTEGER," +
            "transaction_type TEXT," +
            "amount REAL," +
            "description TEXT," +
            "transaction_date TEXT," +
            "FOREIGN KEY(member_id) REFERENCES members(member_id))");
        
        // Bảng thông tin thẻ
        stmt.execute("CREATE TABLE IF NOT EXISTS card_info (" +
            "card_id TEXT PRIMARY KEY," +
            "member_id INTEGER," +
            "pin_hash TEXT," +
            "failed_attempts INTEGER DEFAULT 0," +
            "is_locked INTEGER DEFAULT 0," +
            "FOREIGN KEY(member_id) REFERENCES members(member_id))");
        
        stmt.close();
    }
    
    /**
     * Thêm dữ liệu mẫu
     */
    private void insertSampleData() throws SQLException {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM packages");
        rs.next();
        
        if (rs.getInt(1) == 0) {
            PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO packages (package_name, package_type, duration_days, session_count, price, description) " +
                "VALUES (?, ?, ?, ?, ?, ?)");
            
            String[][] packages = {
                {"Gói 1 tháng", "monthly", "30", "0", "500000", "Tập không giới hạn 1 tháng"},
                {"Gói 3 tháng", "monthly", "90", "0", "1350000", "Tập không giới hạn 3 tháng"},
                {"Gói 6 tháng", "monthly", "180", "0", "2400000", "Tập không giới hạn 6 tháng"},
                {"Gói 12 buổi", "session", "0", "12", "600000", "12 buổi tập"},
                {"Gói 24 buổi", "session", "0", "24", "1100000", "24 buổi tập"},
                {"Gói VIP 1 tháng", "vip", "30", "0", "2000000", "VIP - HLV riêng + không giới hạn"},
                {"Gói VIP 3 tháng", "vip", "90", "0", "5400000", "VIP - HLV riêng 3 tháng"}
            };
            
            for (String[] pkg : packages) {
                pstmt.setString(1, pkg[0]);
                pstmt.setString(2, pkg[1]);
                pstmt.setInt(3, Integer.parseInt(pkg[2]));
                pstmt.setInt(4, Integer.parseInt(pkg[3]));
                pstmt.setDouble(5, Double.parseDouble(pkg[4]));
                pstmt.setString(6, pkg[5]);
                pstmt.executeUpdate();
            }
            pstmt.close();
        }
        
        stmt.close();
    }
    
    /**
     * Đóng kết nối
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}