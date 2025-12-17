package gymcard.client.ui;

import gymcard.client.CardCommunicator;
import gymcard.client.ui.tabs.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Admin Panel - Quản lý hệ thống với Sidebar navigation
 * Fix UI: icon card / emoji không bị đẩy lên hoặc bị cắt.
 */
public class AdminPanel extends JPanel {

    private final CardCommunicator cardComm;
    private final Runnable onBackClick;

    private CardLayout contentCardLayout;
    private JPanel contentPanel;

    private final List<SidebarMenuItem> menuItems = new ArrayList<>();
    private int selectedIndex = 0;

    // Tab panels
    private RegistrationTab registrationTab;
    private PackageManagementTab packageManagementTab;
    private ServiceManagementTab serviceManagementTab;
    private PinManagementTab pinManagementTab;

    public AdminPanel(CardCommunicator cardComm, Runnable onBackClick) {
        this.cardComm = cardComm;
        this.onBackClick = onBackClick;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(25, 25, 45));

        // Left sidebar
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // Main content area
        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(new Color(30, 30, 55));

        // Top bar
        JPanel topBar = createTopBar();
        mainArea.add(topBar, BorderLayout.NORTH);

        // Content panel with CardLayout
        contentCardLayout = new CardLayout();
        contentPanel = new JPanel(contentCardLayout);
        contentPanel.setOpaque(false);

        // Create tab panels
        registrationTab = new RegistrationTab(cardComm);
        packageManagementTab = new PackageManagementTab(cardComm);
        serviceManagementTab = new ServiceManagementTab(cardComm);
        pinManagementTab = new PinManagementTab(cardComm);

        // Add panels
        contentPanel.add(createHomePanel(), "home");
        contentPanel.add(wrapPanel(registrationTab), "registration");
        contentPanel.add(wrapPanel(packageManagementTab), "packages");
        contentPanel.add(wrapPanel(serviceManagementTab), "services");
        contentPanel.add(wrapPanel(pinManagementTab), "pin");

        mainArea.add(contentPanel, BorderLayout.CENTER);

        // Footer
        JPanel footer = createFooter();
        mainArea.add(footer, BorderLayout.SOUTH);

        add(mainArea, BorderLayout.CENTER);
    }

    private JPanel wrapPanel(JPanel panel) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(30, 30, 55));
        wrapper.setBorder(new EmptyBorder(20, 30, 20, 30));
        wrapper.add(panel, BorderLayout.CENTER);
        return wrapper;
    }

    // =========================================================
    // SIDEBAR
    // =========================================================

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(20, 20, 40));
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(new EmptyBorder(20, 15, 20, 15));

        // Logo section
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        logoPanel.setOpaque(false);
        logoPanel.setMaximumSize(new Dimension(220, 50));

        // Logo icon - draw a simple gym dumbbell
        JPanel logoIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(new Color(52, 152, 219));
                g2d.fillOval(0, 0, 35, 35);

                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawLine(10, 17, 25, 17);
                g2d.fillRoundRect(6, 12, 6, 11, 2, 2);
                g2d.fillRoundRect(23, 12, 6, 11, 2, 2);

                g2d.dispose();
                super.paintComponent(g);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(35, 35);
            }
        };
        logoIcon.setOpaque(false);
        logoPanel.add(logoIcon);

        JPanel logoText = new JPanel();
        logoText.setOpaque(false);
        logoText.setLayout(new BoxLayout(logoText, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel("GYM SYSTEM");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLbl.setForeground(Color.WHITE);

        JLabel subtitleLbl = new JLabel("SMART CARD");
        subtitleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        subtitleLbl.setForeground(new Color(52, 152, 219));

        logoText.add(titleLbl);
        logoText.add(subtitleLbl);
        logoPanel.add(logoText);

        sidebar.add(logoPanel);
        sidebar.add(Box.createVerticalStrut(25));

        // Menu items with icons
        addMenuItem(sidebar, "🏠", "Trang chủ", "home", 0);
        sidebar.add(Box.createVerticalStrut(15));

        // Section: QUẢN LÝ
        sidebar.add(createSectionLabel("QUẢN LÝ"));
        sidebar.add(Box.createVerticalStrut(8));

        addMenuItem(sidebar, "📝", "Đăng ký hội viên", "registration", 1);
        sidebar.add(Box.createVerticalStrut(3));
        addMenuItem(sidebar, "📦", "Quản lý gói tập", "packages", 2);
        sidebar.add(Box.createVerticalStrut(3));
        addMenuItem(sidebar, "🛒", "Quản lý dịch vụ", "services", 3);
        sidebar.add(Box.createVerticalStrut(15));

        // Section: HỆ THỐNG
        sidebar.add(createSectionLabel("HỆ THỐNG"));
        sidebar.add(Box.createVerticalStrut(8));

        addMenuItem(sidebar, "🔐", "Đổi PIN & Mở khóa", "pin", 4);

        sidebar.add(Box.createVerticalGlue());

        // Select first item
        selectMenuItem(0);

        return sidebar;
    }

    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        label.setForeground(new Color(100, 100, 130));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void addMenuItem(JPanel sidebar, String icon, String text, String panelName, int index) {
        SidebarMenuItem item = new SidebarMenuItem(icon, text, panelName, index);
        menuItems.add(item);
        sidebar.add(item);
    }

    private void selectMenuItem(int index) {
        selectedIndex = index;
        for (int i = 0; i < menuItems.size(); i++) {
            menuItems.get(i).setSelected(i == index);
        }
    }

    // Inner class for menu item
    private class SidebarMenuItem extends JPanel {
        private final int index;
        private final String panelName;

        private boolean selected = false;
        private boolean hover = false;

        public SidebarMenuItem(String icon, String text, String panelName, int index) {
            this.index = index;
            this.panelName = panelName;

            setOpaque(false);
            setLayout(new FlowLayout(FlowLayout.LEFT, 12, 10));
            setMaximumSize(new Dimension(200, 42));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            JLabel iconLabel = new JLabel(icon);
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
            iconLabel.setForeground(Color.WHITE);
            add(iconLabel);

            JLabel textLabel = new JLabel(text);
            textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            textLabel.setForeground(new Color(180, 180, 200));
            add(textLabel);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectMenuItem(index);
                    showPanel(panelName);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (selected) {
                g2d.setColor(new Color(52, 152, 219));
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
            } else if (hover) {
                g2d.setColor(new Color(40, 40, 70));
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
            }

            g2d.dispose();
            super.paintComponent(g);
        }
    }

    // =========================================================
    // TOP BAR
    // =========================================================

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(15, 30, 15, 30));

        JLabel breadcrumb = new JLabel("Hệ thống  >  Dashboard");
        breadcrumb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        breadcrumb.setForeground(new Color(120, 120, 150));
        topBar.add(breadcrumb, BorderLayout.WEST);

        JButton backBtn = new JButton("< Quay lại") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(60, 60, 90));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2d.dispose();

                super.paintComponent(g);
            }
        };
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        backBtn.setForeground(Color.WHITE);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setPreferredSize(new Dimension(100, 32));
        backBtn.addActionListener(e -> onBackClick.run());
        topBar.add(backBtn, BorderLayout.EAST);

        return topBar;
    }

    // =========================================================
    // HOME PANEL
    // =========================================================

    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 30, 20, 30));

        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel welcomeLabel = new JLabel("Chào mừng Quản trị viên!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.add(welcomeLabel);

        headerPanel.add(Box.createVerticalStrut(8));

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));
        JLabel dateLabel = new JLabel("Hôm nay: " + sdf.format(new Date()));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateLabel.setForeground(new Color(140, 140, 170));
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.add(dateLabel);

        panel.add(headerPanel, BorderLayout.NORTH);

        JPanel cardsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        cardsPanel.setOpaque(false);
        cardsPanel.setBorder(new EmptyBorder(30, 0, 0, 0));

        cardsPanel.add(createDashboardCard(
                "👤", new Color(230, 126, 34),
                "Đăng ký hội viên",
                "Tạo thẻ thành viên mới, nhập thông tin cá nhân và cấp phát thẻ NFC.",
                "Thực hiện",
                new Color(155, 89, 182),
                () -> {
                    selectMenuItem(1);
                    showPanel("registration");
                }));

        cardsPanel.add(createDashboardCard(
                "📦", new Color(155, 89, 182),
                "Quản lý gói tập",
                "Thêm mới, chỉnh sửa thông tin hoặc xóa các gói tập hiện có trong hệ thống.",
                "Quản lý ngay",
                new Color(52, 152, 219),
                () -> {
                    selectMenuItem(2);
                    showPanel("packages");
                }));

        cardsPanel.add(createDashboardCard(
                "🛒", new Color(46, 204, 113),
                "Quản lý dịch vụ",
                "Quản lý các dịch vụ bổ sung như thuê khăn, tủ đồ cá nhân và huấn luyện viên.",
                "Xem dịch vụ",
                new Color(46, 204, 113),
                () -> {
                    selectMenuItem(3);
                    showPanel("services");
                }));

        cardsPanel.add(createDashboardCard(
                "🔐", new Color(231, 76, 60),
                "Đổi PIN & Mở khóa",
                "Hỗ trợ thành viên reset mã PIN hoặc mở khóa thẻ khi bị khóa do nhập sai nhiều lần.",
                "Xử lý thẻ",
                new Color(231, 76, 60),
                () -> {
                    selectMenuItem(4);
                    showPanel("pin");
                }));

        panel.add(cardsPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Card dashboard: fix icon bị đẩy/cắt bằng GridBagLayout + BorderLayout, bỏ MaximumSize gây bóp.
     */
    private JPanel createDashboardCard(String iconEmoji, Color iconBgColor, String title, String desc,
                                       String buttonText, Color buttonColor, Runnable onClick) {

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(new Color(40, 40, 70));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                g2d.setColor(new Color(iconBgColor.getRed(), iconBgColor.getGreen(), iconBgColor.getBlue(), 100));
                g2d.setStroke(new BasicStroke(1f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);

                g2d.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Icon wrapper - dùng GridBagLayout để icon luôn ở giữa và không bị trôi
        JPanel iconWrapper = new JPanel(new GridBagLayout());
        iconWrapper.setOpaque(false);
        iconWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(iconBgColor);
                g2d.fillOval(0, 0, getWidth(), getHeight());

                g2d.dispose();
                super.paintComponent(g);
            }
        };
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(60, 60));
        iconPanel.setLayout(new BorderLayout());

        JLabel emojiLabel = new JLabel(iconEmoji, SwingConstants.CENTER);
        emojiLabel.setVerticalAlignment(SwingConstants.CENTER);
        emojiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        emojiLabel.setForeground(Color.WHITE);
        iconPanel.add(emojiLabel, BorderLayout.CENTER);

        iconWrapper.add(iconPanel);
        card.add(iconWrapper);

        card.add(Box.createVerticalStrut(18));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLabel);

        card.add(Box.createVerticalStrut(12));

        JLabel descLabel = new JLabel("<html><center>" + desc + "</center></html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(new Color(140, 140, 170));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(descLabel);

        card.add(Box.createVerticalGlue());
        card.add(Box.createVerticalStrut(18));

        JPanel btnWrapper = new JPanel(new GridBagLayout());
        btnWrapper.setOpaque(false);
        btnWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btn = new JButton(buttonText) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(getModel().isRollover() ? buttonColor.brighter() : buttonColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                g2d.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 38));
        btn.addActionListener(e -> onClick.run());

        btnWrapper.add(btn);
        card.add(btnWrapper);

        return card;
    }

    // =========================================================
    // FOOTER + NAV
    // =========================================================

    private JPanel createFooter() {
        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 0, 15, 0));

        JLabel footerLabel = new JLabel("© 2024 Gym Card Management System - JavaCard SmartCard. All rights reserved.");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footerLabel.setForeground(new Color(100, 100, 130));
        footer.add(footerLabel);

        return footer;
    }

    private void showPanel(String name) {
        contentCardLayout.show(contentPanel, name);
    }

    private void log(String message) {
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        System.out.println(String.format("[%s] %s", timestamp, message));
    }
}
