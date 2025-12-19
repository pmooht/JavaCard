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
 * User Panel - Dành cho hội viên với Sidebar navigation
 */
public class UserPanel extends JPanel {

    private final CardCommunicator cardComm;
    private final Runnable onBackClick;
    private CardLayout contentCardLayout;
    private JPanel contentPanel;
    private SidebarPanel sidebar;

    // Shared data
    private final List<String> purchasedServices = new ArrayList<>();

    // Tab panels
    private InfoTab infoTab;
    private TopUpTab topUpTab;
    private CheckInTab checkInTab;
    private PackageTab packageTab;
    private ServicesTab servicesTab;
    private StatisticsTab statisticsTab;
    private ChangePinTab changePinTab;

    public UserPanel(CardCommunicator cardComm, Runnable onBackClick) {
        this.cardComm = cardComm;
        this.onBackClick = onBackClick;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(248, 249, 250));

        // Header bar
        JPanel header = createHeader();
        add(header, BorderLayout.NORTH);

        // Main content area
        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(new Color(248, 249, 250));

        // Sidebar
        sidebar = new SidebarPanel(new Color(52, 152, 219));
        sidebar.addItem("🏠", "Trang chủ", () -> showPanel("home"));
        sidebar.addItem("👤", "Thông tin cá nhân", () -> showPanel("info"));
        sidebar.addItem("📦", "Gói tập", () -> showPanel("package"));
        sidebar.addItem("✅", "Check-in/out", () -> showPanel("checkin"));
        sidebar.addItem("💰", "Nạp tiền", () -> showPanel("topup"));
        sidebar.addItem("🛒", "Dịch vụ thêm", () -> showPanel("services"));
        sidebar.addSeparator();
        sidebar.addItem("📊", "Thống kê", () -> showPanel("statistics"));
        sidebar.addItem("🔑", "Đổi mã PIN", () -> showPanel("changepin"));
        mainArea.add(sidebar, BorderLayout.WEST);

        // Content panel with CardLayout
        contentCardLayout = new CardLayout();
        contentPanel = new JPanel(contentCardLayout);
        contentPanel.setBackground(new Color(248, 249, 250));

        // Create tab panels
        infoTab = new InfoTab(cardComm);
        topUpTab = new TopUpTab(cardComm);
        checkInTab = new CheckInTab(cardComm);
        packageTab = new PackageTab(cardComm);
        servicesTab = new ServicesTab(cardComm, purchasedServices);
        statisticsTab = new StatisticsTab(cardComm, purchasedServices);
        changePinTab = new ChangePinTab(cardComm);

        // Add home panel
        contentPanel.add(createHomePanel(), "home");
        contentPanel.add(infoTab, "info");
        contentPanel.add(packageTab, "package");
        contentPanel.add(checkInTab, "checkin");
        contentPanel.add(topUpTab, "topup");
        contentPanel.add(servicesTab, "services");
        contentPanel.add(statisticsTab, "statistics");
        contentPanel.add(changePinTab, "changepin");

        // Wrap content in a nice card
        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setBackground(new Color(248, 249, 250));
        contentWrapper.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel contentCard = new JPanel(new BorderLayout());
        contentCard.setBackground(Color.WHITE);
        contentCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(10, 10, 10, 10)));
        contentCard.add(contentPanel, BorderLayout.CENTER);

        contentWrapper.add(contentCard, BorderLayout.CENTER);
        mainArea.add(contentWrapper, BorderLayout.CENTER);

        add(mainArea, BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(52, 152, 219),
                        getWidth(), 0, new Color(41, 128, 185));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setPreferredSize(new Dimension(0, 60));
        header.setBorder(new EmptyBorder(10, 20, 10, 20));

        // Back button
        JButton backBtn = createHeaderButton("< Quay lại");
        backBtn.addActionListener(e -> onBackClick.run());
        header.add(backBtn, BorderLayout.WEST);

        // Title
        JLabel titleLabel = new JLabel("HỘI VIÊN - GYM CARD SYSTEM");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        header.add(titleLabel, BorderLayout.CENTER);

        // User info
        JLabel userLabel = new JLabel("Hội viên");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userLabel.setForeground(Color.WHITE);
        header.add(userLabel, BorderLayout.EAST);

        return header;
    }

    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));

        // Top section with welcome + dark mode toggle
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        // Welcome message
        JPanel welcomePanel = new JPanel();
        welcomePanel.setOpaque(false);
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));

        JLabel welcomeLabel = new JLabel("Chào mừng Hội viên!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(new Color(44, 62, 80));
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        welcomePanel.add(welcomeLabel);

        welcomePanel.add(Box.createVerticalStrut(8));

        String today = new SimpleDateFormat("EEEE, dd/MM/yyyy").format(new Date());
        JLabel dateLabel = new JLabel("📅 Hôm nay: " + today);
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dateLabel.setForeground(new Color(127, 140, 141));
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        welcomePanel.add(dateLabel);

        topPanel.add(welcomePanel, BorderLayout.WEST);
        panel.add(topPanel, BorderLayout.NORTH);

        // Quick action cards - 2 rows x 3 columns
        JPanel cardsPanel = new JPanel(new GridLayout(2, 3, 18, 18));
        cardsPanel.setOpaque(false);
        cardsPanel.setBorder(new EmptyBorder(25, 0, 0, 0));

        // Row 1
        cardsPanel.add(createQuickCard("Check-in", "Ghi nhận vào/ra phòng tập nhanh chóng qua thẻ hoặc mã QR.",
                "🎯", new Color(138, 97, 218), () -> showPanel("checkin")));
        cardsPanel.add(createQuickCard("Nạp tiền", "Nạp thêm số dư vào thẻ hội viên để sử dụng dịch vụ.",
                "💳", new Color(243, 156, 18), () -> showPanel("topup")));
        cardsPanel.add(createQuickCard("Gói tập", "Xem thông tin gói hiện tại và đăng ký gia hạn gói tập.",
                "📋", new Color(52, 152, 219), () -> showPanel("package")));

        // Row 2
        cardsPanel.add(createQuickCard("Dịch vụ", "Mua dịch vụ bổ sung như khăn, tủ đồ, nước uống.",
                "💎", new Color(46, 204, 113), () -> showPanel("services")));
        cardsPanel.add(createQuickCard("Thống kê", "Xem lịch sử tập luyện và tần suất đến phòng tập.",
                "📊", new Color(231, 76, 60), () -> showPanel("statistics")));
        cardsPanel.add(createQuickCard("Thông tin", "Cập nhật thông tin cá nhân và cài đặt tài khoản.",
                "👥", new Color(149, 165, 166), () -> showPanel("info")));

        panel.add(cardsPanel, BorderLayout.CENTER);

        // Footer
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        JLabel footerLabel = new JLabel("© SmartCard Gym System. Designed for Member Experience.");
        footerLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        footerLabel.setForeground(new Color(160, 170, 180));
        footerPanel.add(footerLabel);
        panel.add(footerPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createQuickCard(String title, String desc, String icon, Color iconColor, Runnable onClick) {
        JPanel card = new JPanel(new BorderLayout(15, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                // Subtle shadow effect
                g2d.setColor(new Color(0, 0, 0, 10));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 20, 20, 15));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Left side - Icon with colored background
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Light background tint
                g2d.setColor(new Color(iconColor.getRed(), iconColor.getGreen(), iconColor.getBlue(), 30));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
        };
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(55, 55));
        iconPanel.setLayout(new GridBagLayout());

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        iconPanel.add(iconLabel);

        card.add(iconPanel, BorderLayout.WEST);

        // Center - Title and description
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(new Color(44, 62, 80));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        textPanel.add(titleLabel);

        textPanel.add(Box.createVerticalStrut(6));

        JLabel descLabel = new JLabel("<html><div style='width:130px'>" + desc + "</div></html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descLabel.setForeground(new Color(127, 140, 141));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        textPanel.add(descLabel);

        card.add(textPanel, BorderLayout.CENTER);

        // Right - Arrow
        JLabel arrowLabel = new JLabel("→");
        arrowLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        arrowLabel.setForeground(new Color(180, 190, 200));
        card.add(arrowLabel, BorderLayout.EAST);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            private Color originalBg = Color.WHITE;

            public void mouseClicked(java.awt.event.MouseEvent e) {
                onClick.run();
            }

            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                card.repaint();
            }
        });

        return card;
    }

    private void showPanel(String name) {
        contentCardLayout.show(contentPanel, name);
        // Refresh data when showing
        refreshCurrentPanel(name);
    }

    private void refreshCurrentPanel(String name) {
        try {
            switch (name) {
                case "info":
                    infoTab.refreshData();
                    break;
                case "topup":
                    topUpTab.refreshData();
                    break;
                case "checkin":
                    checkInTab.refreshData();
                    break;
                case "package":
                    packageTab.refreshData();
                    break;
                case "services":
                    servicesTab.refreshData();
                    break;
                case "statistics":
                    statisticsTab.refreshData();
                    break;
            }
        } catch (Exception ex) {
            System.out.println("[UserPanel] Refresh error: " + ex.getMessage());
        }
    }

    private JButton createHeaderButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    public void onLogin() {
        showPanel("home");
        sidebar.selectItem(0);
    }

    public void refreshAllTabs() {
        // Refresh current visible panel
        refreshCurrentPanel("home");
    }
}
