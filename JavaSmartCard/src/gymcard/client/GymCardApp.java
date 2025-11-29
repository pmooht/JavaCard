package gymcard.client;

import gymcard.client.ui.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Main application - Gym Card Management System
 */
public class GymCardApp extends JFrame {
    
    private CardCommunicator cardComm;
    private JTabbedPane mainTabbedPane;
    private AdminPanel adminPanel;
    private UserPanel userPanel;
    private JLabel statusLabel;
    private JButton connectBtn;
    
    public GymCardApp() {
        cardComm = new CardCommunicator();
        initUI();
    }
    
    private void initUI() {
        setTitle("Hệ thống Quản lý Thẻ Tập Gym - Gym Card System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Top toolbar
        JPanel toolbarPanel = createToolbar();
        mainPanel.add(toolbarPanel, BorderLayout.NORTH);
        
        // Tabbed pane
        mainTabbedPane = new JTabbedPane();
        mainTabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Admin panel
        adminPanel = new AdminPanel(cardComm);
        mainTabbedPane.addTab("👤 Quản trị viên", adminPanel);
        
        // User panel
        userPanel = new UserPanel(cardComm);
        mainTabbedPane.addTab("👥 Hội viên", userPanel);
        
        mainPanel.add(mainTabbedPane, BorderLayout.CENTER);
        
        // Status bar
        JPanel statusBar = createStatusBar();
        mainPanel.add(statusBar, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    /**
     * Create toolbar
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
                GradientPaint gp = new GradientPaint(0, 0, new Color(41, 128, 185), w, 0, new Color(109, 213, 250));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setOpaque(false);
        
        // Left side - title
        JLabel titleLabel = new JLabel("🏋️ GYM CARD MANAGEMENT SYSTEM");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel, BorderLayout.WEST);
        
        // Right side - connect button
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);
        
        connectBtn = createModernButton("Kết nối thẻ", new Color(46, 204, 113));
        connectBtn.addActionListener(e -> toggleConnection());
        
        rightPanel.add(connectBtn);
        panel.add(rightPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Create status bar
     */
    private JPanel createStatusBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        panel.setBackground(new Color(248, 249, 250));
        
        statusLabel = new JLabel("⚫ Chưa kết nối");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(statusLabel, BorderLayout.WEST);
        
        JLabel versionLabel = new JLabel("v1.0 - JavaCard Gym System");
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        versionLabel.setForeground(Color.GRAY);
        panel.add(versionLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Toggle card connection (Simulated)
     */
    private void toggleConnection() {
        if (!cardComm.isConnected()) {
            // Connect (simulated)
            try {
                cardComm.connect();
                statusLabel.setText("🟢 Đã kết nối với thẻ ảo (Mock Mode)");
                statusLabel.setForeground(new Color(46, 204, 113));
                connectBtn.setText("Ngắt kết nối");
                updateButtonColor(connectBtn, new Color(231, 76, 60));
                
                // Ask if want to load demo data
                int option = JOptionPane.showConfirmDialog(this, 
                    "Kết nối thẻ ảo thành công!\n\n" +
                    "Bạn có muốn tải dữ liệu mẫu để demo không?",
                    "Tải dữ liệu Demo", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
                
                if (option == JOptionPane.YES_OPTION) {
                    cardComm.loadDemoData();
                    JOptionPane.showMessageDialog(this, 
                        "Đã tải dữ liệu demo!\n\n" +
                        "Thông tin demo:\n" +
                        "- Hội viên: Nguyễn Văn Demo\n" +
                        "- Gói: VIP\n" +
                        "- Số dư: 500,000 VNĐ\n" +
                        "- Số ngày tập: 15\n\n" +
                        "PIN: 1234",
                        "Demo Data", JOptionPane.INFORMATION_MESSAGE);
                }
                
            } catch (Exception ex) {
                statusLabel.setText("🔴 Lỗi kết nối");
                statusLabel.setForeground(Color.RED);
                
                JOptionPane.showMessageDialog(this, 
                    "Không thể kết nối với thẻ:\n" + ex.getMessage(),
                    "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // Disconnect
            cardComm.disconnect();
            statusLabel.setText("⚫ Chưa kết nối");
            statusLabel.setForeground(Color.BLACK);
            connectBtn.setText("Kết nối thẻ");
            updateButtonColor(connectBtn, new Color(46, 204, 113));
            
            JOptionPane.showMessageDialog(this, 
                "Đã ngắt kết nối với thẻ ảo",
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
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2d.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(bgColor.brighter());
                } else {
                    g2d.setColor(bgColor);
                }
                
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(150, 40));
        return button;
    }
    
    /**
     * Update button color
     */
    private void updateButtonColor(JButton button, Color color) {
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
