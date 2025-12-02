/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gymcard.databaseManager;

import java.sql.*;
import java.util.Base64;

/**
 * Quản lý kết nối SQLite và tạo các bảng cần thiết.
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:gym_system_db";

    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() throws SQLException {
        // Kết nối SQLite
        connection = DriverManager.getConnection(DB_URL);
        initSchema();
    }

    public static synchronized DatabaseManager getInstance() throws SQLException {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    /**
     * Tạo các bảng nếu chưa có.
     */
    private void initSchema() throws SQLException {
        try (Statement st = connection.createStatement()) {

            // Bảng users: map userId <-> cardPublicKey
            st.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  user_code TEXT UNIQUE NOT NULL," +
                " card_public_key TEXT NOT NULL," +
                "  status TEXT NOT NULL DEFAULT 'ACTIVE'," +
                "  created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "  updated_at DATETIME" +
                ");"
            );

            // Bảng membership_plans: các gói tập
            st.execute(
                "CREATE TABLE IF NOT EXISTS membership_plans (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  code TEXT UNIQUE NOT NULL," +
                "  name TEXT NOT NULL," +
                "  description TEXT," +
                "  duration_days INTEGER," +
                "  session_count INTEGER," +
                "  price REAL NOT NULL," +
                "  is_active INTEGER NOT NULL DEFAULT 1," +
                "  created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ");"
            );

            // Bảng user_memberships: user đang dùng gói nào
            st.execute(
                "CREATE TABLE IF NOT EXISTS user_memberships (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  user_id INTEGER NOT NULL," +
                "  plan_id INTEGER NOT NULL," +
                "  start_date DATE NOT NULL," +
                "  end_date DATE," +
                "  remaining_sessions INTEGER," +
                "  status TEXT NOT NULL DEFAULT 'ACTIVE'," +
                "  created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "  FOREIGN KEY (user_id) REFERENCES users(id)," +
                "  FOREIGN KEY (plan_id) REFERENCES membership_plans(id)" +
                ");"
            );

            // Bảng checkin_logs: lịch sử vào/ra
            st.execute(
                "CREATE TABLE IF NOT EXISTS checkin_logs (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  user_id INTEGER NOT NULL," +
                "  checkin_time DATETIME NOT NULL," +
                "  checkout_time DATETIME," +
                "  device_id TEXT," +
                "  note TEXT," +
                "  FOREIGN KEY (user_id) REFERENCES users(id)" +
                ");"
            );

            // Bảng transactions: nạp tiền, mua gói, dịch vụ thêm
            st.execute(
                "CREATE TABLE IF NOT EXISTS transactions (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  user_id INTEGER NOT NULL," +
                "  type TEXT NOT NULL," +
                "  amount REAL NOT NULL," +
                "  description TEXT," +
                "  related_plan_id INTEGER," +
                "  created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "  FOREIGN KEY (user_id) REFERENCES users(id)," +
                "  FOREIGN KEY (related_plan_id) REFERENCES membership_plans(id)" +
                ");"
            );
        }
    }
        public long insertUser(String userCode, String cardPublicKey) throws SQLException {
        String sql = "INSERT INTO users(user_code, card_public_key, status, created_at) " +
                     "VALUES (?, ?, 'ACTIVE', CURRENT_TIMESTAMP)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, userCode);
            ps.setString(2, cardPublicKey);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return -1;
    }

    /**
     * Overload: nhận public key dạng byte[] rồi tự encode Base64.
     */
    public long insertUser(String userCode, byte[] cardPublicKeyBytes) throws SQLException {
        String base64Key = Base64.getEncoder().encodeToString(cardPublicKeyBytes);
        return insertUser(userCode, base64Key);
    }

}
