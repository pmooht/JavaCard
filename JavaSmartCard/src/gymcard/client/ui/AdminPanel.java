package gymcard.client.ui;

import gymcard.client.CardCommunicator;
import gymcard.client.ui.tabs.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Admin Panel - Quản lý hệ thống với Sidebar navigation
 */
public class AdminPanel extends JPanel {

    private final CardCommunicator cardComm;
    private final Runnable onBackClick;
    private CardLayout contentCardLayout;
    private JPanel contentPanel;
    private SidebarPanel sidebar;

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
        setBackground(new Color(248, 249, 250));

        // Header bar
        JPanel header = createHeader();
        add(header, BorderLayout.NORTH);

        // Main content area
        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(new Color(248, 249, 250));

        // Sidebar (purple theme for admin)
        sidebar = new SidebarPanel(new Color(142, 68, 173));
        sidebar.addItem("🏠", "Trang chủ", () -> showPanel("home"));
        sidebar.addItem("📝", "Đăng ký hội viên", () -> showPanel("registration"));
        sidebar.addItem("📦", "Quản lý gói tập", () -> showPanel("packages"));
        sidebar.addItem("🛒", "Quản lý dịch vụ", () -> showPanel("services"));
        sidebar.addSeparator();
        sidebar.addItem("🔐", "Đổi PIN & Mở khóa", () -> showPanel("pin"));
        mainArea.add(sidebar, BorderLayout.WEST);

        // Content panel with CardLayout
        contentCardLayout = new CardLayout();
        contentPanel = new JPanel(contentCardLayout);
        contentPanel.setBackground(new Color(248, 249, 250));

        // Create tab panels
        registrationTab = new RegistrationTab(cardComm);
        packageManagementTab = new PackageManagementTab(cardComm);
        serviceManagementTab = new ServiceManagementTab(cardComm);
        pinManagementTab = new PinManagementTab(cardComm);

        // Add panels
        contentPanel.add(createHomePanel(), "home");
        contentPanel.add(registrationTab, "registration");
        contentPanel.add(packageManagementTab, "packages");
        contentPanel.add(serviceManagementTab, "services");
        contentPanel.add(pinManagementTab, "pin");

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
                GradientPaint gp = new GradientPaint(0, 0, new Color(142, 68, 173),
                        getWidth(), 0, new Color(102, 51, 153));
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
        JLabel titleLabel = new JLabel("QUẢN TRỊ VIÊN - GYM CARD SYSTEM");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        header.add(titleLabel, BorderLayout.CENTER);

        // Admin badge
        JLabel adminLabel = new JLabel("Admin");
        adminLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        adminLabel.setForeground(Color.WHITE);
        header.add(adminLabel, BorderLayout.EAST);

        return header;
    }

    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Welcome message
        JPanel welcomePanel = new JPanel();
        welcomePanel.setOpaque(false);
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));

        JLabel welcomeLabel = new JLabel("Chào mừng Quản trị viên!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(new Color(44, 62, 80));
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        welcomePanel.add(welcomeLabel);

        welcomePanel.add(Box.createVerticalStrut(10));

        String today = new SimpleDateFormat("EEEE, dd/MM/yyyy").format(new Date());
        JLabel dateLabel = new JLabel("Hôm nay: " + today);
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateLabel.setForeground(new Color(127, 140, 141));
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        welcomePanel.add(dateLabel);

        panel.add(welcomePanel, BorderLayout.NORTH);

        // Quick action cards
        JPanel cardsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        cardsPanel.setOpaque(false);
        cardsPanel.setBorder(new EmptyBorder(30, 0, 0, 0));

        cardsPanel.add(createQuickCard("Đăng ký hội viên", "Tạo thẻ mới cho hội viên",
                new Color(46, 204, 113), () -> showPanel("registration")));
        cardsPanel.add(createQuickCard("Quản lý gói tập", "Thêm, sửa, xóa gói tập",
                new Color(52, 152, 219), () -> showPanel("packages")));
        cardsPanel.add(createQuickCard("Quản lý dịch vụ", "Quản lý các dịch vụ thêm",
                new Color(155, 89, 182), () -> showPanel("services")));
        cardsPanel.add(createQuickCard("Đổi PIN & Mở khóa", "Reset PIN, mở khóa thẻ",
                new Color(231, 76, 60), () -> showPanel("pin")));

        panel.add(cardsPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createQuickCard(String title, String desc, Color color, Runnable onClick) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                new EmptyBorder(30, 25, 30, 25)));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(color);
        textPanel.add(titleLabel);

        textPanel.add(Box.createVerticalStrut(10));

        JLabel descLabel = new JLabel("<html>" + desc + "</html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(new Color(127, 140, 141));
        textPanel.add(descLabel);

        card.add(textPanel, BorderLayout.CENTER);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                onClick.run();
            }

            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(248, 249, 250));
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(Color.WHITE);
            }
        });

        return card;
    }

    private void showPanel(String name) {
        contentCardLayout.show(contentPanel, name);
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

    private void log(String message) {
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        System.out.println(String.format("[%s] %s", timestamp, message));
    }
}
