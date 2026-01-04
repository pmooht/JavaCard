package gymcard.client.ui.tabs;

import gymcard.client.CardCommunicator;
import gymcard.client.ui.BaseTabPanel;
import gymcard.databaseManager.DatabaseManager;
import gymcard.databaseManager.DatabaseManager.ServiceInfo;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Tab Dịch vụ thêm - Modern Card Design
 */
public class ServicesTab extends BaseTabPanel {

    private static final Color BG_LIGHT = new Color(248, 250, 252);
    private static final Color TEXT_DARK = new Color(30, 41, 59);
    private static final Color TEXT_GRAY = new Color(100, 116, 139);
    private static final Color PRIMARY_BLUE = new Color(59, 130, 246);

    // Service category colors
    private static final Color COLOR_DRINK = new Color(251, 191, 36); // Amber/Yellow for drinks
    private static final Color COLOR_NUTRITION = new Color(59, 130, 246); // Blue for nutrition
    private static final Color COLOR_UTILITY = new Color(34, 197, 94); // Green for utility
    private static final Color COLOR_SERVICE = new Color(168, 85, 247); // Purple for services
    private static final Color COLOR_PREMIUM = new Color(239, 68, 68); // Red for premium

    private final List<String> purchasedServices;
    private JPanel servicesPanel;
    private JPanel purchasedServicesPanel;
    private DatabaseManager db;

    public ServicesTab(CardCommunicator cardComm, List<String> purchasedServices) {
        super(cardComm);
        this.purchasedServices = purchasedServices;
        try {
            db = DatabaseManager.getInstance();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(BG_LIGHT);

        // Main content panel
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(BG_LIGHT);
        mainContent.setBorder(new EmptyBorder(25, 30, 25, 30));

        // Header
        mainContent.add(createHeader());
        mainContent.add(Box.createVerticalStrut(25));

        // Purchased services section (new)
        purchasedServicesPanel = new JPanel();
        purchasedServicesPanel.setLayout(new BoxLayout(purchasedServicesPanel, BoxLayout.Y_AXIS));
        purchasedServicesPanel.setBackground(BG_LIGHT);
        purchasedServicesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel purchasedWrapper = new JPanel(new BorderLayout());
        purchasedWrapper.setBackground(BG_LIGHT);
        purchasedWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        purchasedWrapper.add(purchasedServicesPanel, BorderLayout.WEST);
        mainContent.add(purchasedWrapper);

        // Services grid
        servicesPanel = new JPanel(new GridLayout(0, 3, 20, 20));
        servicesPanel.setBackground(BG_LIGHT);
        servicesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel servicesWrapper = new JPanel(new BorderLayout());
        servicesWrapper.setBackground(BG_LIGHT);
        servicesWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        servicesWrapper.add(servicesPanel, BorderLayout.CENTER);

        mainContent.add(servicesWrapper);

        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(BG_LIGHT);
        add(scrollPane, BorderLayout.CENTER);

        // Load services on init
        loadServices();
        loadPurchasedServicesPanel();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Left side - Title with accent bar
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setOpaque(false);

        // Blue accent bar
        JPanel accentBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PRIMARY_BLUE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.dispose();
            }
        };
        accentBar.setPreferredSize(new Dimension(4, 50));
        accentBar.setOpaque(false);
        titlePanel.add(accentBar);
        titlePanel.add(Box.createHorizontalStrut(15));

        // Title and subtitle
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("DỊCH VỤ THÊM");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(TEXT_DARK);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Mua các gói bổ sung, đồ uống và dịch vụ tiện ích");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(TEXT_GRAY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(subtitleLabel);
        titlePanel.add(textPanel);

        header.add(titlePanel, BorderLayout.WEST);

        // Right side - Refresh button
        JButton refreshBtn = createRefreshButton();
        header.add(refreshBtn, BorderLayout.EAST);

        return header;
    }

    private JButton createRefreshButton() {
        JButton btn = new JButton("Tải lại") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(new Color(226, 232, 240));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 10, 10));

                g2.setColor(TEXT_DARK);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setPreferredSize(new Dimension(100, 38));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> loadServices());
        return btn;
    }

    private void loadServices() {
        servicesPanel.removeAll();

        try {
            List<ServiceInfo> services = db.getActiveServices();

            for (int i = 0; i < services.size(); i++) {
                ServiceInfo svc = services.get(i);

                // Determine category and color based on service code or index
                String category;
                Color iconColor;
                Color iconBgColor;
                String icon;

                // Assign categories based on service characteristics
                String codeLower = svc.code != null ? svc.code.toLowerCase() : "";
                String nameLower = svc.name != null ? svc.name.toLowerCase() : "";

                if (codeLower.contains("drink") || nameLower.contains("nước") || nameLower.contains("uống")) {
                    category = "ĐỒ UỐNG";
                    iconColor = COLOR_DRINK;
                    iconBgColor = new Color(254, 243, 199);
                    icon = "🏋";
                } else if (codeLower.contains("nutri") || nameLower.contains("protein") || nameLower.contains("shake")
                        || nameLower.contains("dinh dưỡng")) {
                    category = "DINH DƯỠNG";
                    iconColor = COLOR_NUTRITION;
                    iconBgColor = new Color(219, 234, 254);
                    icon = "🥛";
                } else if (codeLower.contains("locker") || nameLower.contains("tủ") || nameLower.contains("khóa")) {
                    category = "TIỆN ÍCH";
                    iconColor = COLOR_UTILITY;
                    iconBgColor = new Color(220, 252, 231);
                    icon = "🔐";
                } else if (codeLower.contains("pt") || codeLower.contains("trainer") || nameLower.contains("hlv")
                        || nameLower.contains("huấn luyện")) {
                    category = "CAO CẤP";
                    iconColor = COLOR_PREMIUM;
                    iconBgColor = new Color(254, 226, 226);
                    icon = "🏋";
                } else {
                    category = "DỊCH VỤ";
                    iconColor = COLOR_SERVICE;
                    iconBgColor = new Color(243, 232, 255);
                    icon = "🏋";
                }

                JPanel card = createServiceCard(svc, icon, iconColor, iconBgColor, category);
                servicesPanel.add(card);
            }

            if (services.isEmpty()) {
                JLabel emptyLabel = new JLabel("Chưa có dịch vụ nào. Admin vui lòng thêm dịch vụ trong tab quản lý.");
                emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
                emptyLabel.setForeground(TEXT_GRAY);
                servicesPanel.add(emptyLabel);
            }

            servicesPanel.revalidate();
            servicesPanel.repaint();
            log("Đã tải " + services.size() + " dịch vụ từ database");

        } catch (SQLException e) {
            log("LỖI tải dịch vụ: " + e.getMessage());
            JLabel errorLabel = new JLabel("Lỗi tải dịch vụ: " + e.getMessage());
            errorLabel.setForeground(Color.RED);
            servicesPanel.add(errorLabel);
        }
    }

    private JPanel createServiceCard(ServiceInfo svc, String icon, Color iconColor, Color iconBgColor,
            String category) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                // Shadow effect
                g2.setColor(new Color(0, 0, 0, 10));
                g2.fill(new RoundRectangle2D.Float(2, 2, getWidth() - 2, getHeight() - 2, 16, 16));
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 2, getHeight() - 2, 16, 16));
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(0, 200));

        // Top row - icon and category
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        // Icon circle
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(iconBgColor);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        iconPanel.setLayout(new GridBagLayout());
        iconPanel.setPreferredSize(new Dimension(45, 45));
        iconPanel.setOpaque(false);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        iconPanel.add(iconLabel);

        topRow.add(iconPanel, BorderLayout.WEST);

        // Category badge
        JLabel categoryLabel = new JLabel(category) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(241, 245, 249));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        categoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 9));
        categoryLabel.setForeground(TEXT_GRAY);
        categoryLabel.setBorder(new EmptyBorder(4, 8, 4, 8));
        categoryLabel.setOpaque(false);

        JPanel categoryWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        categoryWrapper.setOpaque(false);
        categoryWrapper.add(categoryLabel);
        topRow.add(categoryWrapper, BorderLayout.EAST);

        // Content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        // Service name
        JLabel nameLabel = new JLabel(svc.name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setForeground(TEXT_DARK);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Price
        int priceK = (int) (svc.price / 1000);
        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pricePanel.setOpaque(false);
        pricePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel priceValue = new JLabel(priceK + "k");
        priceValue.setFont(new Font("Segoe UI", Font.BOLD, 20));
        priceValue.setForeground(iconColor);

        JLabel priceCurrency = new JLabel(" VND");
        priceCurrency.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        priceCurrency.setForeground(TEXT_GRAY);

        pricePanel.add(priceValue);
        pricePanel.add(priceCurrency);

        // Description
        String desc = svc.description != null ? svc.description : "";
        JLabel descLabel = new JLabel("<html><div style='width:150px'>" + desc + "</div></html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descLabel.setForeground(TEXT_GRAY);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        contentPanel.add(nameLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(pricePanel);
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(descLabel);

        // Buy button
        final String svcName = svc.name;
        final long svcPrice = (long) svc.price;
        final Color btnColor = iconColor;

        JButton buyBtn = new JButton("Mua ngay  →") {
            private boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
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

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bgColor = hover ? new Color(btnColor.getRed(), btnColor.getGreen(), btnColor.getBlue(), 30)
                        : new Color(btnColor.getRed(), btnColor.getGreen(), btnColor.getBlue(), 20);
                g2.setColor(bgColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));

                g2.setColor(btnColor);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        buyBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        buyBtn.setForeground(iconColor);
        buyBtn.setPreferredSize(new Dimension(0, 38));
        buyBtn.setBorderPainted(false);
        buyBtn.setContentAreaFilled(false);
        buyBtn.setFocusPainted(false);
        buyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        buyBtn.addActionListener(e -> purchaseService(svcName, svcPrice));

        // Assemble card
        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setOpaque(false);
        topWrapper.add(topRow, BorderLayout.NORTH);
        topWrapper.add(contentPanel, BorderLayout.CENTER);

        card.add(topWrapper, BorderLayout.CENTER);
        card.add(buyBtn, BorderLayout.SOUTH);

        return card;
    }

    private void purchaseService(String svcName, long svcPrice) {
        try {
            long currentBalance = cardComm.getBalance();
            if (currentBalance < svcPrice) {
                JOptionPane.showMessageDialog(this,
                        "Không đủ tiền!\n\nSố dư: " + String.format("%,d", currentBalance)
                                + " VND\nCần: " + String.format("%,d", svcPrice) + " VND",
                        "Không đủ tiền", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (cardComm.deductBalance(svcPrice)) {
                long newBalance = cardComm.getBalance();

                // Save to card using new method (chỉ lưu tên dịch vụ)
                if (!cardComm.addPurchasedService(svcName)) {
                    JOptionPane.showMessageDialog(this,
                            "Cảnh báo: Không thể lưu dịch vụ vào thẻ!\nCó thể Applet chưa được cập nhật tính năng này.",
                            "Lỗi lưu thẻ", JOptionPane.WARNING_MESSAGE);
                }

                log("Đã mua dịch vụ: " + svcName + " - " + String.format("%,d", svcPrice) + " VND");
                JOptionPane.showMessageDialog(this,
                        "Mua dịch vụ thành công!\n\nDịch vụ: " + svcName + "\nGiá: "
                                + String.format("%,d", svcPrice) + " VND\nSố dư còn: "
                                + String.format("%,d", newBalance) + " VND",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);

                // Refresh purchased services panel
                loadPurchasedServicesPanel();
            } else {
                JOptionPane.showMessageDialog(this, "Mua dịch vụ thất bại!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Load and display purchased services panel with Use buttons
     */
    private void loadPurchasedServicesPanel() {
        purchasedServicesPanel.removeAll();

        try {
            java.util.Map<String, Integer> servicesMap = cardComm.getPurchasedServicesWithCount();

            if (servicesMap.isEmpty()) {
                purchasedServicesPanel.revalidate();
                purchasedServicesPanel.repaint();
                return;
            }

            // Header for purchased services
            JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            headerPanel.setOpaque(false);
            headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel headerLabel = new JLabel("DỊCH VỤ ĐÃ MUA (" + servicesMap.size() + " loại)");
            headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            headerLabel.setForeground(new Color(34, 197, 94)); // Green
            headerPanel.add(headerLabel);

            purchasedServicesPanel.add(headerPanel);
            purchasedServicesPanel.add(Box.createVerticalStrut(15));

            // Grid of purchased services - use FlowLayout LEFT for left alignment
            JPanel grid = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
            grid.setBackground(BG_LIGHT);
            grid.setAlignmentX(Component.LEFT_ALIGNMENT);

            for (java.util.Map.Entry<String, Integer> entry : servicesMap.entrySet()) {
                String serviceName = entry.getKey();
                int count = entry.getValue();

                JPanel card = createPurchasedServiceCard(serviceName, count);
                grid.add(card);
            }

            purchasedServicesPanel.add(grid);
            purchasedServicesPanel.add(Box.createVerticalStrut(20));

            // Separator line
            JPanel separator = new JPanel();
            separator.setBackground(new Color(226, 232, 240));
            separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            separator.setPreferredSize(new Dimension(100, 1));
            separator.setAlignmentX(Component.LEFT_ALIGNMENT);
            purchasedServicesPanel.add(separator);
            purchasedServicesPanel.add(Box.createVerticalStrut(20));

        } catch (Exception e) {
            log("Lỗi load dịch vụ đã mua: " + e.getMessage());
            e.printStackTrace();
        }

        purchasedServicesPanel.revalidate();
        purchasedServicesPanel.repaint();
    }

    /**
     * Create a card for purchased service with Use button
     */
    private JPanel createPurchasedServiceCard(String serviceName, int count) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Light green background
                g2.setColor(new Color(240, 253, 244));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                // Green border
                g2.setColor(new Color(34, 197, 94, 100));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 12, 12));
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout(10, 8));
        card.setBorder(new EmptyBorder(12, 15, 12, 15));
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(180, 75));

        // Left: Service info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(serviceName);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLabel.setForeground(TEXT_DARK);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel countLabel = new JLabel("Còn " + count + " lượt");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        countLabel.setForeground(new Color(34, 197, 94));
        countLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(countLabel);

        // Right: Use button
        final String svcNameFinal = serviceName;
        JButton useBtn = new JButton("Sử dụng") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(34, 197, 94));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        useBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        useBtn.setPreferredSize(new Dimension(75, 32));
        useBtn.setBorderPainted(false);
        useBtn.setContentAreaFilled(false);
        useBtn.setFocusPainted(false);
        useBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        useBtn.addActionListener(e -> useServiceUI(svcNameFinal));

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(useBtn, BorderLayout.EAST);

        return card;
    }

    /**
     * Use a purchased service (decrement count and save to card)
     */
    private void useServiceUI(String serviceName) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Sử dụng dịch vụ: " + serviceName + "?",
                "Xác nhận sử dụng", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean success = cardComm.useService(serviceName);
            if (success) {
                log("Đã sử dụng dịch vụ: " + serviceName);
                JOptionPane.showMessageDialog(this,
                        "Đã sử dụng dịch vụ: " + serviceName,
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                // Refresh panel
                loadPurchasedServicesPanel();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Không thể sử dụng dịch vụ này!\nCó thể đã hết lượt.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Public method to refresh services from database on tab change
     */
    public void refreshData() {
        loadServices();
        loadPurchasedServicesPanel();
    }
}
