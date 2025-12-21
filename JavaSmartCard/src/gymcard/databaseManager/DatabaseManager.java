/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gymcard.databaseManager;

import java.sql.*;
import java.util.Base64;
import java.util.ArrayList;
import java.util.List;

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
        insertDefaultData();
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
                            ");");

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
                            ");");

            // Bảng services: dịch vụ bổ sung (HLV riêng, nước uống...)
            st.execute(
                    "CREATE TABLE IF NOT EXISTS services (" +
                            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "  code TEXT UNIQUE NOT NULL," +
                            "  name TEXT NOT NULL," +
                            "  description TEXT," +
                            "  price REAL NOT NULL," +
                            "  is_active INTEGER NOT NULL DEFAULT 1," +
                            "  created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                            ");");

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
                            ");");

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
                            ");");

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
                            ");");
        }
    }

    /**
     * Insert dữ liệu mặc định nếu chưa có.
     */
    private void insertDefaultData() throws SQLException {
        // Insert default membership plans
        String checkPlan = "SELECT COUNT(*) FROM membership_plans";
        try (Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(checkPlan)) {
            if (rs.next() && rs.getInt(1) == 0) {
                insertDefaultPlans();
            }
        }

        // Insert default services
        String checkService = "SELECT COUNT(*) FROM services";
        try (Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(checkService)) {
            if (rs.next() && rs.getInt(1) == 0) {
                insertDefaultServices();
            }
        }
    }

    /**
     * Insert các gói tập mặc định.
     */
    private void insertDefaultPlans() throws SQLException {
        String sql = "INSERT INTO membership_plans(code, name, description, duration_days, session_count, price) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            // Gói 15 ngày
            ps.setString(1, "DAY_15");
            ps.setString(2, "Gói 15 ngày");
            ps.setString(3, "Tập không giới hạn trong 15 ngày");
            ps.setInt(4, 15);
            ps.setNull(5, Types.INTEGER);
            ps.setDouble(6, 300000);
            ps.executeUpdate();

            // Gói 30 ngày
            ps.setString(1, "DAY_30");
            ps.setString(2, "Gói 30 ngày");
            ps.setString(3, "Tập không giới hạn trong 30 ngày");
            ps.setInt(4, 30);
            ps.setNull(5, Types.INTEGER);
            ps.setDouble(6, 500000);
            ps.executeUpdate();

            // Gói 60 ngày
            ps.setString(1, "DAY_60");
            ps.setString(2, "Gói 60 ngày");
            ps.setString(3, "Tập không giới hạn trong 60 ngày");
            ps.setInt(4, 60);
            ps.setNull(5, Types.INTEGER);
            ps.setDouble(6, 900000);
            ps.executeUpdate();

            // Gói 90 ngày
            ps.setString(1, "DAY_90");
            ps.setString(2, "Gói 90 ngày");
            ps.setString(3, "Tập không giới hạn trong 90 ngày");
            ps.setInt(4, 90);
            ps.setNull(5, Types.INTEGER);
            ps.setDouble(6, 1200000);
            ps.executeUpdate();
        }
    }

    /**
     * Insert các dịch vụ mặc định.
     */
    private void insertDefaultServices() throws SQLException {
        String sql = "INSERT INTO services(code, name, description, price) VALUES(?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, "PT_SESSION");
            ps.setString(2, "HLV riêng (1 buổi)");
            ps.setString(3, "Tập với huấn luyện viên cá nhân trong 1 buổi");
            ps.setDouble(4, 200000);
            ps.executeUpdate();

            ps.setString(1, "DRINK");
            ps.setString(2, "Nước uống");
            ps.setString(3, "Nước suối/nước tăng lực");
            ps.setDouble(4, 20000);
            ps.executeUpdate();

            ps.setString(1, "TOWEL");
            ps.setString(2, "Khăn tập");
            ps.setString(3, "Thuê khăn tập");
            ps.setDouble(4, 10000);
            ps.executeUpdate();

            ps.setString(1, "PROTEIN_SHAKE");
            ps.setString(2, "Protein shake");
            ps.setString(3, "Đồ uống bổ sung protein");
            ps.setDouble(4, 50000);
            ps.executeUpdate();

            ps.setString(1, "NUTRITION");
            ps.setString(2, "Tư vấn dinh dưỡng");
            ps.setString(3, "Tư vấn chế độ dinh dưỡng với chuyên gia");
            ps.setDouble(4, 100000);
            ps.executeUpdate();

            ps.setString(1, "LOCKER");
            ps.setString(2, "Thuê tủ khóa");
            ps.setString(3, "Thuê tủ khóa cá nhân 1 tháng");
            ps.setDouble(4, 50000);
            ps.executeUpdate();
        }
    }

    /**
     * Lấy danh sách gói tập đang hoạt động.
     * 
     * @return List<PlanInfo> với id, code, name, description, durationDays,
     *         sessionCount, price
     */
    public List<PlanInfo> getActivePlans() throws SQLException {
        List<PlanInfo> plans = new ArrayList<>();
        String sql = "SELECT id, code, name, description, duration_days, session_count, price FROM membership_plans WHERE is_active = 1 ORDER BY price";
        try (Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                PlanInfo plan = new PlanInfo();
                plan.id = rs.getInt("id");
                plan.code = rs.getString("code");
                plan.name = rs.getString("name");
                plan.description = rs.getString("description");
                plan.durationDays = rs.getInt("duration_days");
                plan.sessionCount = rs.getInt("session_count");
                plan.price = rs.getDouble("price");
                plans.add(plan);
            }
        }
        return plans;
    }

    /**
     * Lấy danh sách dịch vụ đang hoạt động.
     */
    public List<ServiceInfo> getActiveServices() throws SQLException {
        List<ServiceInfo> services = new ArrayList<>();
        String sql = "SELECT id, code, name, description, price FROM services WHERE is_active = 1 ORDER BY price";
        try (Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ServiceInfo svc = new ServiceInfo();
                svc.id = rs.getInt("id");
                svc.code = rs.getString("code");
                svc.name = rs.getString("name");
                svc.description = rs.getString("description");
                svc.price = rs.getDouble("price");
                services.add(svc);
            }
        }
        return services;
    }

    // ===== Data classes =====
    public static class PlanInfo {
        public int id;
        public String code;
        public String name;
        public String description;
        public int durationDays;
        public int sessionCount;
        public double price;
        public boolean isActive = true;
    }

    public static class ServiceInfo {
        public int id;
        public String code;
        public String name;
        public String description;
        public double price;
        public boolean isActive = true;
    }

    // ===== User methods =====
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

    /**
     * Lấy card public key (Base64) theo user code.
     * 
     * @return Base64 string của public key, hoặc null nếu không tìm thấy
     */
    public String getCardPublicKey(String userCode) throws SQLException {
        String sql = "SELECT card_public_key FROM users WHERE user_code = ? AND status = 'ACTIVE'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("card_public_key");
                }
            }
        }
        return null;
    }

    /**
     * Lấy card public key dạng byte[] theo user code.
     * 
     * @return byte[] của public key (decoded từ Base64), hoặc null nếu không tìm
     *         thấy
     */
    public byte[] getCardPublicKeyBytes(String userCode) throws SQLException {
        String base64 = getCardPublicKey(userCode);
        if (base64 == null || base64.isEmpty()) {
            return null;
        }
        return Base64.getDecoder().decode(base64);
    }

    /**
     * Kiểm tra user đã tồn tại chưa.
     */
    public boolean userExists(String userCode) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE user_code = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Cập nhật public key của user (dùng khi re-register với thẻ mới).
     * 
     * @param userCode           Mã user
     * @param cardPublicKeyBytes Public key modulus mới
     * @return true nếu update thành công
     */
    public boolean updateCardPublicKey(String userCode, byte[] cardPublicKeyBytes) throws SQLException {
        String base64Key = Base64.getEncoder().encodeToString(cardPublicKeyBytes);
        String sql = "UPDATE users SET card_public_key = ? WHERE user_code = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, base64Key);
            ps.setString(2, userCode);
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    // ===== Package Management methods =====

    /**
     * Lấy tất cả gói tập (bao gồm cả không active).
     */
    public List<PlanInfo> getAllPlans() throws SQLException {
        List<PlanInfo> plans = new ArrayList<>();
        String sql = "SELECT id, code, name, description, duration_days, session_count, price, is_active FROM membership_plans ORDER BY price";
        try (Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                PlanInfo plan = new PlanInfo();
                plan.id = rs.getInt("id");
                plan.code = rs.getString("code");
                plan.name = rs.getString("name");
                plan.description = rs.getString("description");
                plan.durationDays = rs.getInt("duration_days");
                plan.sessionCount = rs.getInt("session_count");
                plan.price = rs.getDouble("price");
                plan.isActive = rs.getInt("is_active") == 1;
                plans.add(plan);
            }
        }
        return plans;
    }

    /**
     * Thêm gói tập mới.
     */
    public int addPlan(String code, String name, String description, Integer durationDays, Integer sessionCount,
            double price) throws SQLException {
        String sql = "INSERT INTO membership_plans(code, name, description, duration_days, session_count, price, is_active) VALUES(?,?,?,?,?,?,1)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, code);
            ps.setString(2, name);
            ps.setString(3, description);
            if (durationDays != null) {
                ps.setInt(4, durationDays);
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            if (sessionCount != null) {
                ps.setInt(5, sessionCount);
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setDouble(6, price);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    /**
     * Cập nhật gói tập.
     */
    public boolean updatePlan(int id, String code, String name, String description, Integer durationDays,
            Integer sessionCount, double price) throws SQLException {
        String sql = "UPDATE membership_plans SET code=?, name=?, description=?, duration_days=?, session_count=?, price=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setString(2, name);
            ps.setString(3, description);
            if (durationDays != null) {
                ps.setInt(4, durationDays);
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            if (sessionCount != null) {
                ps.setInt(5, sessionCount);
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setDouble(6, price);
            ps.setInt(7, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Xóa gói tập (xóa vĩnh viễn).
     */
    public boolean deletePlan(int id) throws SQLException {
        String sql = "DELETE FROM membership_plans WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Bật/tắt trạng thái active của gói tập.
     */
    public boolean togglePlanActive(int id, boolean active) throws SQLException {
        String sql = "UPDATE membership_plans SET is_active=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, active ? 1 : 0);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ===== Service Management methods =====

    /**
     * Lấy tất cả dịch vụ (bao gồm cả không active).
     */
    public List<ServiceInfo> getAllServices() throws SQLException {
        List<ServiceInfo> services = new ArrayList<>();
        String sql = "SELECT id, code, name, description, price, is_active FROM services ORDER BY price";
        try (Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ServiceInfo svc = new ServiceInfo();
                svc.id = rs.getInt("id");
                svc.code = rs.getString("code");
                svc.name = rs.getString("name");
                svc.description = rs.getString("description");
                svc.price = rs.getDouble("price");
                svc.isActive = rs.getInt("is_active") == 1;
                services.add(svc);
            }
        }
        return services;
    }

    /**
     * Thêm dịch vụ mới.
     */
    public int addService(String code, String name, String description, double price) throws SQLException {
        String sql = "INSERT INTO services(code, name, description, price, is_active) VALUES(?,?,?,?,1)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, code);
            ps.setString(2, name);
            ps.setString(3, description);
            ps.setDouble(4, price);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    /**
     * Cập nhật dịch vụ.
     */
    public boolean updateService(int id, String code, String name, String description, double price)
            throws SQLException {
        String sql = "UPDATE services SET code=?, name=?, description=?, price=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setString(2, name);
            ps.setString(3, description);
            ps.setDouble(4, price);
            ps.setInt(5, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Xóa dịch vụ (xóa vĩnh viễn).
     */
    public boolean deleteService(int id) throws SQLException {
        String sql = "DELETE FROM services WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Bật/tắt trạng thái active của dịch vụ.
     */
    public boolean toggleServiceActive(int id, boolean active) throws SQLException {
        String sql = "UPDATE services SET is_active=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, active ? 1 : 0);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

}
