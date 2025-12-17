package gymcard.client;

import gymcard.client.ui.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Main application - Gym Card Management System (SmartCard + AES + RSA)
 */
public class GymCardApp extends JFrame {

    private final CardCommunicator cardComm;
    private JTabbedPane mainTabbedPane;
    private AdminPanel adminPanel;
    private UserPanel userPanel;
    private JLabel statusLabel;
    private JLabel securityLabel;
    private JButton connectBtn;

    public GymCardApp() {
        cardComm = new CardCommunicator(); // TODO: trong CardCommunicator.connect() bạn thực hiện: kết nối reader +
                                           // select applet + bắt tay RSA
        initUI();
        // KHÔNG cho cửa sổ nhỏ hơn layout “an toàn”
        setMinimumSize(new Dimension(1000, 650));
    }

    private void initUI() {
        setTitle("Hệ thống Quản lý Thẻ Tập Gym - SmartCard Gym System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);

        // Nền chung
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(236, 240, 241));

        // Top toolbar (gradient)
        JPanel toolbarPanel = createToolbar();
        mainPanel.add(toolbarPanel, BorderLayout.NORTH);

        // ===== CENTER: bọc TabbedPane vào một "card" trắng =====
        mainTabbedPane = new JTabbedPane();
        mainTabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        mainTabbedPane.setBackground(new Color(248, 249, 250));

        // Admin panel (quản trị hệ thống, init thẻ, đổi PIN, unlock, v.v.)
        adminPanel = new AdminPanel(cardComm);
        mainTabbedPane.addTab("Quản trị viên", adminPanel);

        // User panel (check-in, xem thông tin, ... )
        userPanel = new UserPanel(cardComm);
        mainTabbedPane.addTab("Hội viên", userPanel);

        // Listener de refresh data khi chuyen tu Admin sang User
        mainTabbedPane.addChangeListener(e -> {
            int selectedIndex = mainTabbedPane.getSelectedIndex();
            // Khi chuyen sang tab "Hoi vien" (index 1), refresh du lieu
            if (selectedIndex == 1 && userPanel != null) {
                userPanel.refreshAllTabs();
            }
        });

        // Card trắng ở giữa
        JPanel cardPanel = new JPanel(new BorderLayout());
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 215, 218), 1, true),
                new EmptyBorder(15, 15, 15, 15)));
        cardPanel.add(mainTabbedPane, BorderLayout.CENTER);

        // Wrapper có padding, giúp “thẻ trắng” không dính sát mép
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBackground(new Color(236, 240, 241));
        centerWrapper.setBorder(new EmptyBorder(15, 15, 10, 15));
        centerWrapper.add(cardPanel, BorderLayout.CENTER);

        mainPanel.add(centerWrapper, BorderLayout.CENTER);

        // Status bar
        JPanel statusBar = createStatusBar();
        mainPanel.add(statusBar, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    /**
     * Create toolbar (gradient header + nút Kết nối)
     */
    private JPanel createToolbar() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(40, 116, 166),
                        w, 0, new Color(93, 173, 226));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        panel.setBorder(new EmptyBorder(12, 18, 10, 18));
        panel.setOpaque(false);

        // Left side - title & subtitle
        JPanel leftPanel = new JPanel();
        leftPanel.setOpaque(false);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("GYM SMARTCARD MANAGEMENT SYSTEM");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);

        JLabel subTitle = new JLabel("JavaCard • AES-128 on-card • RSA secure channel • PIN-based access control");
        subTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subTitle.setForeground(new Color(230, 248, 255));

        leftPanel.add(titleLabel);
        leftPanel.add(Box.createVerticalStrut(4));
        leftPanel.add(subTitle);

        panel.add(leftPanel, BorderLayout.WEST);

        // Right side - connect button + security status
        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonRow.setOpaque(false);

        connectBtn = createModernButton("Kết nối thẻ", new Color(46, 204, 113));
        connectBtn.addActionListener(e -> toggleConnection());
        connectBtn.setPreferredSize(new Dimension(150, 36));
        buttonRow.add(connectBtn);

        rightPanel.add(buttonRow);
        rightPanel.add(Box.createVerticalStrut(4));

        securityLabel = new JLabel("Chưa thiết lập kênh bảo mật (chưa kết nối thẻ)");
        securityLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        securityLabel.setForeground(new Color(230, 248, 255));
        securityLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        rightPanel.add(securityLabel);

        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    /**
     * Create status bar (bottom)
     */
    /**
     * Create status bar (bottom)
     */
    private JPanel createStatusBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
        panel.setBackground(new Color(248, 249, 250));

        // Trái: trạng thái kết nối
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        JLabel dotLabel = new JLabel("●");
        dotLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dotLabel.setForeground(Color.GRAY);

        statusLabel = new JLabel("Chưa kết nối đầu đọc / thẻ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        left.add(dotLabel);
        left.add(statusLabel);

        // Phải: version
        JLabel versionLabel = new JLabel("v1.0  •  SmartCard Gym System (Java SE + JavaCard)");
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        versionLabel.setForeground(Color.GRAY);

        panel.add(left, BorderLayout.WEST);
        panel.add(versionLabel, BorderLayout.EAST);

        return panel;
    }

    /**
     * Toggle card connection (REAL smartcard logic)
     *
     * Gợi ý: trong CardCommunicator.connect():
     * - Kết nối terminal + card
     * - SELECT applet
     * - Sinh / load keypair RSA của app
     * - Lấy cardPublicKey từ thẻ
     * - Gửi appPublicKey xuống thẻ
     */
    private void toggleConnection() {
        if (!cardComm.isConnected()) {
            // Connect REAL card
            try {
                cardComm.connect(); // TODO: triển khai trong CardCommunicator cho đúng logic

                statusLabel.setText("[OK] Đã kết nối thẻ JavaCard - Sẵn sàng thao tác PIN & dữ liệu");
                statusLabel.setForeground(new Color(39, 174, 96));
                connectBtn.setText("Ngắt kết nối");
                updateButtonColor(connectBtn, new Color(231, 76, 60));

                // Cập nhật text bảo mật
                securityLabel.setText("Kênh bảo mật: AES-128 trên thẻ + RSA trên đường truyền (App - Card)");

                JOptionPane.showMessageDialog(this,
                        "Đã kết nối thành công với thẻ JavaCard.\n\n" +
                                "Các bước tiếp theo:\n" +
                                "1. Vào tab \"Quản trị viên\" để khởi tạo thẻ (INIT_CARD) cho hội viên mới.\n" +
                                "2. Đặt PIN cho thẻ (mặc định ban đầu có thể là 123456).\n" +
                                "3. Sau khi INIT_CARD, dùng các chức năng: Verify PIN, Ghi thông tin, Đọc thông tin.\n\n"
                                +
                                "Lưu ý: Mọi thông tin trên thẻ đều được mã hóa bằng AES với khóa sinh từ PIN.",
                        "Kết nối thành công", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                ex.printStackTrace();
                statusLabel.setText("[LOI] Lỗi kết nối thẻ: " + ex.getMessage());
                statusLabel.setForeground(Color.RED);
                securityLabel.setText("Chưa thiết lập kênh bảo mật (kết nối thất bại)");

                JOptionPane.showMessageDialog(this,
                        "Không thể kết nối với thẻ / đầu đọc:\n" + ex.getMessage(),
                        "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // Disconnect
            try {
                cardComm.disconnect();
            } catch (Exception e) {
                // ignore, chỉ log
                e.printStackTrace();
            }

            statusLabel.setText("Chưa kết nối đầu đọc / thẻ");
            statusLabel.setForeground(Color.BLACK);
            connectBtn.setText("Kết nối thẻ");
            updateButtonColor(connectBtn, new Color(46, 204, 113));
            securityLabel.setText("Chưa thiết lập kênh bảo mật (chưa kết nối thẻ)");

            // Reset giao diện người dùng về trạng thái chưa login
            if (userPanel != null) {
                userPanel.resetToLogin();
            }

            JOptionPane.showMessageDialog(this,
                    "Đã ngắt kết nối với thẻ.",
                    "Ngắt kết nối", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Create modern rounded button
     */
    private JButton createModernButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Color base = (Color) getClientProperty("baseColor");
                if (base == null) {
                    base = new Color(52, 152, 219); // fallback
                }

                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setColor(base.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(base.brighter());
                } else {
                    g2d.setColor(base);
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        button.putClientProperty("baseColor", bgColor);

        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(160, 40));
        return button;
    }

    /**
     * Update button color
     */
    // private void updateButtonColor(JButton button, Color color) {
    // button.putClientProperty("baseColor", color);
    // button.repaint();
    // }

    /**
     * Update button color
     */
    private void updateButtonColor(JButton button, Color color) {
        button.putClientProperty("bgColor", color); // nếu sau này muốn lấy lại
        button.setBackground(color);
        button.repaint();
    }

    /**
     * Main method
     */
    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Run application
        SwingUtilities.invokeLater(() -> {
            GymCardApp app = new GymCardApp();
            app.setVisible(true);
        });
    }
}
