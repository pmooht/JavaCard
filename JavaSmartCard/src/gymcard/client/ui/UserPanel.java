package gymcard.client.ui;

import gymcard.client.CardCommunicator;
import gymcard.client.ui.tabs.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * User Panel - Dành cho hội viên
 * Controller class quản lý và điều phối các tab panels
 */
public class UserPanel extends JPanel {

    private final CardCommunicator cardComm;
    private JPanel contentPanel;
    private CardLayout cardLayout;

    // Shared data giữa các tabs
    private final List<String> purchasedServices = new ArrayList<>();

    // Tab references for cleanup
    private LoginPanel loginPanel;
    private InfoTab infoTab;

    public UserPanel(CardCommunicator cardComm) {
        this.cardComm = cardComm;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(new Color(248, 249, 250));

        // Content area with CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Login panel
        loginPanel = new LoginPanel(cardComm, () -> {
            cardLayout.show(contentPanel, "main");
            if (infoTab != null) {
                infoTab.clearUI();
            }
        });
        contentPanel.add(loginPanel, "login");

        // Main panel with tabs
        contentPanel.add(createMainPanel(), "main");

        add(contentPanel, BorderLayout.CENTER);

        // Show login panel first
        cardLayout.show(contentPanel, "login");
    }

    /**
     * Panel chính sau khi đăng nhập
     */
    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(248, 249, 250));

        // Card trắng chứa tab
        JPanel tabsCard = new JPanel(new BorderLayout());
        tabsCard.setBackground(Color.WHITE);
        tabsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(10, 10, 5, 10)));

        // Tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(new Color(248, 249, 250));

        // Add tabs using separated tab classes
        infoTab = new InfoTab(cardComm);
        tabbedPane.addTab("Thông tin cá nhân", infoTab);
        tabbedPane.addTab("Gói tập", new PackageTab(cardComm));
        tabbedPane.addTab("Check-in/Check-out", new CheckInTab(cardComm));
        tabbedPane.addTab("Nạp tiền", new TopUpTab(cardComm));
        tabbedPane.addTab("Dịch vụ thêm", new ServicesTab(cardComm, purchasedServices));
        tabbedPane.addTab("Thống kê", new StatisticsTab(cardComm, purchasedServices));
        tabbedPane.addTab("Đổi PIN", new ChangePinTab(cardComm));

        tabsCard.add(tabbedPane, BorderLayout.CENTER);

        // Wrapper để không dính sát mép
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBackground(new Color(248, 249, 250));
        centerWrapper.setBorder(new EmptyBorder(10, 10, 0, 10));
        centerWrapper.add(tabsCard, BorderLayout.CENTER);

        panel.add(centerWrapper, BorderLayout.CENTER);

        // Logout button (footer nhỏ gọn)
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 8));
        bottomPanel.setBackground(new Color(248, 249, 250));
        JButton logoutBtn = createModernButton("Đăng xuất", new Color(149, 165, 166), 14);
        logoutBtn.setPreferredSize(new Dimension(140, 36));
        logoutBtn.addActionListener(e -> {
            cardLayout.show(contentPanel, "login");
            log("Đã đăng xuất");
        });
        bottomPanel.add(logoutBtn);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Create modern rounded button
     */
    private JButton createModernButton(String text, Color bgColor, int fontSize) {
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

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    /**
     * Reset về màn hình login khi ngắt kết nối
     */
    public void resetToLogin() {
        cardLayout.show(contentPanel, "login");
        if (loginPanel != null) {
            loginPanel.reset();
        }
        log("Đã reset về màn hình đăng nhập");
    }

    /**
     * Log message ra terminal
     */
    private void log(String message) {
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        System.out.println(String.format("[%s] %s", timestamp, message));
    }
}
