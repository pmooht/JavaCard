package gymcard.client.ui.tabs;

import gymcard.client.CardCommunicator;
import gymcard.client.CheckInInfo;
import gymcard.client.CheckInLogEntry;
import gymcard.client.PackageInfo;
import gymcard.client.ui.BaseTabPanel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Tab Thống kê - Modern Dashboard Design
 */
public class StatisticsTab extends BaseTabPanel {

    private static final Color BG_LIGHT = new Color(248, 250, 252);
    private static final Color TEXT_DARK = new Color(30, 41, 59);
    private static final Color TEXT_GRAY = new Color(100, 116, 139);
    private static final Color PRIMARY_BLUE = new Color(59, 130, 246);
    private static final Color PRIMARY_GREEN = new Color(34, 197, 94);
    private static final Color PRIMARY_PINK = new Color(236, 72, 153);

    private final List<String> purchasedServices;

    // Labels for dynamic data
    private JLabel packageNameLabel;
    private JLabel packageExpiryLabel;
    private JLabel packageStatusLabel;
    private JLabel balanceValueLabel;
    private JLabel servicesCountLabel;
    private JLabel servicesInfoLabel;
    private JLabel totalSessionsLabel;
    private JLabel avgTimeLabel;
    private JPanel activityLogPanel;
    private JLabel lastUpdateLabel;
    private JPanel chartPanel;
    private String lastCheckInDate = ""; // Store last check-in date for chart

    public StatisticsTab(CardCommunicator cardComm, List<String> purchasedServices) {
        super(cardComm);
        this.purchasedServices = purchasedServices;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(BG_LIGHT);

        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(BG_LIGHT);
        mainContent.setBorder(new EmptyBorder(25, 30, 25, 30));

        // Header
        mainContent.add(createHeader());
        mainContent.add(Box.createVerticalStrut(25));

        // Stat cards row
        mainContent.add(createStatCardsRow());
        mainContent.add(Box.createVerticalStrut(25));

        // Bottom section: Activity chart + Activity log
        mainContent.add(createBottomSection());

        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(BG_LIGHT);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Left - Title
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("THỐNG KÊ HOẠT ĐỘNG");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_DARK);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Tổng quan về tình trạng thẻ và hiệu suất tập luyện của bạn.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(TEXT_GRAY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitleLabel);
        header.add(titlePanel, BorderLayout.WEST);

        // Right - Last update + Refresh button
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);

        lastUpdateLabel = new JLabel("Cập nhật lần cuối: --");
        lastUpdateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lastUpdateLabel.setForeground(TEXT_GRAY);
        rightPanel.add(lastUpdateLabel);

        JButton refreshBtn = createRefreshButton();
        rightPanel.add(refreshBtn);

        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    private JButton createRefreshButton() {
        JButton btn = new JButton("↻  Tải lại dữ liệu") {
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
        btn.setPreferredSize(new Dimension(140, 38));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> refreshData());
        return btn;
    }

    private JPanel createStatCardsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 20, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        row.setPreferredSize(new Dimension(0, 160));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        row.add(createPackageCard());
        row.add(createBalanceCard());
        row.add(createServicesCard());

        return row;
    }

    private JPanel createPackageCard() {
        JPanel card = createCardBase();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        // Top row
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel categoryLabel = new JLabel("GÓI TẬP HIỆN TẠI");
        categoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        categoryLabel.setForeground(PRIMARY_BLUE);
        topRow.add(categoryLabel, BorderLayout.WEST);

        // Icon
        JPanel iconPanel = createIconCircle(new Color(219, 234, 254), "✈");
        topRow.add(iconPanel, BorderLayout.EAST);

        // Content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(10, 0, 0, 0));

        packageNameLabel = new JLabel("Chưa có gói tập");
        packageNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        packageNameLabel.setForeground(TEXT_DARK);
        packageNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel expiryRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        expiryRow.setOpaque(false);
        expiryRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel calIcon = new JLabel("📅");
        calIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        JLabel expiryTitle = new JLabel("Hết hạn:");
        expiryTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        expiryTitle.setForeground(TEXT_GRAY);
        packageExpiryLabel = new JLabel("--");
        packageExpiryLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        packageExpiryLabel.setForeground(TEXT_DARK);
        expiryRow.add(calIcon);
        expiryRow.add(expiryTitle);
        expiryRow.add(packageExpiryLabel);

        // Status badge
        packageStatusLabel = new JLabel("Chưa kích hoạt") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(220, 252, 231));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        packageStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        packageStatusLabel.setForeground(new Color(22, 163, 74));
        packageStatusLabel.setBorder(new EmptyBorder(4, 10, 4, 10));
        packageStatusLabel.setOpaque(false);

        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bottomRow.setOpaque(false);
        bottomRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottomRow.add(expiryRow);
        bottomRow.add(packageStatusLabel);

        content.add(packageNameLabel);
        content.add(Box.createVerticalStrut(8));
        content.add(bottomRow);

        card.add(topRow, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);

        return card;
    }

    private JPanel createBalanceCard() {
        JPanel card = createCardBase();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        // Top row
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel categoryLabel = new JLabel("SỐ DƯ TÀI KHOẢN");
        categoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        categoryLabel.setForeground(PRIMARY_GREEN);
        topRow.add(categoryLabel, BorderLayout.WEST);

        JPanel iconPanel = createIconCircle(new Color(220, 252, 231), "💳");
        topRow.add(iconPanel, BorderLayout.EAST);

        // Content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(10, 0, 0, 0));

        JPanel balanceRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        balanceRow.setOpaque(false);
        balanceRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        balanceValueLabel = new JLabel("0");
        balanceValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        balanceValueLabel.setForeground(TEXT_DARK);

        JLabel currencyLabel = new JLabel(" VND");
        currencyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        currencyLabel.setForeground(TEXT_GRAY);

        balanceRow.add(balanceValueLabel);
        balanceRow.add(currencyLabel);

        JLabel availableLabel = new JLabel("● Khả dụng cho mọi dịch vụ");
        availableLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        availableLabel.setForeground(TEXT_GRAY);
        availableLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(balanceRow);
        content.add(Box.createVerticalStrut(8));
        content.add(availableLabel);

        card.add(topRow, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);

        return card;
    }

    private JPanel createServicesCard() {
        JPanel card = createCardBase();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        // Top row
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel categoryLabel = new JLabel("DỊCH VỤ ĐÃ MUA");
        categoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        categoryLabel.setForeground(PRIMARY_PINK);
        topRow.add(categoryLabel, BorderLayout.WEST);

        JPanel iconPanel = createIconCircle(new Color(252, 231, 243), "🛍");
        topRow.add(iconPanel, BorderLayout.EAST);

        // Content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(10, 0, 0, 0));

        JPanel countRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        countRow.setOpaque(false);
        countRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        servicesCountLabel = new JLabel("0");
        servicesCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        servicesCountLabel.setForeground(TEXT_DARK);

        JLabel unitLabel = new JLabel(" dịch vụ");
        unitLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        unitLabel.setForeground(TEXT_GRAY);

        countRow.add(servicesCountLabel);
        countRow.add(unitLabel);

        // Service info label (shows service names with counts)
        servicesInfoLabel = new JLabel("Chưa đăng ký dịch vụ bổ sung");
        servicesInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        servicesInfoLabel.setForeground(TEXT_GRAY);
        servicesInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(countRow);
        content.add(Box.createVerticalStrut(8));
        content.add(servicesInfoLabel);

        card.add(topRow, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);

        return card;
    }

    private JPanel createBottomSection() {
        JPanel section = new JPanel(new GridLayout(1, 2, 20, 0));
        section.setOpaque(false);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        section.add(createActivityChartPanel());
        section.add(createActivityLogPanel());

        return section;
    }

    private JPanel createActivityChartPanel() {
        JPanel card = createCardBase();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);
        JLabel chartIcon = new JLabel("📊");
        chartIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        JLabel chartTitle = new JLabel("Hoạt động tập luyện");
        chartTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        chartTitle.setForeground(TEXT_DARK);
        titleRow.add(chartIcon);
        titleRow.add(chartTitle);

        JLabel chartSubtitle = new JLabel("Thống kê số buổi tập trong 7 ngày qua");
        chartSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        chartSubtitle.setForeground(TEXT_GRAY);

        titlePanel.add(titleRow);
        titlePanel.add(Box.createVerticalStrut(3));
        titlePanel.add(chartSubtitle);
        header.add(titlePanel, BorderLayout.WEST);

        // Stats badges
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        statsPanel.setOpaque(false);

        JPanel totalPanel = new JPanel();
        totalPanel.setLayout(new BoxLayout(totalPanel, BoxLayout.Y_AXIS));
        totalPanel.setOpaque(false);
        JLabel totalLabel = new JLabel("TỔNG BUỔI");
        totalLabel.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        totalLabel.setForeground(TEXT_GRAY);
        totalSessionsLabel = new JLabel("0");
        totalSessionsLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalSessionsLabel.setForeground(TEXT_DARK);
        totalPanel.add(totalLabel);
        totalPanel.add(totalSessionsLabel);

        JPanel avgPanel = new JPanel();
        avgPanel.setLayout(new BoxLayout(avgPanel, BoxLayout.Y_AXIS));
        avgPanel.setOpaque(false);
        JLabel avgLabel = new JLabel("TRUNG BÌNH");
        avgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        avgLabel.setForeground(TEXT_GRAY);
        avgTimeLabel = new JLabel("~60p");
        avgTimeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        avgTimeLabel.setForeground(TEXT_DARK);
        avgPanel.add(avgLabel);
        avgPanel.add(avgTimeLabel);

        statsPanel.add(totalPanel);
        statsPanel.add(avgPanel);
        header.add(statsPanel, BorderLayout.EAST);

        // Chart area (bar chart based on check-in data)
        chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int barWidth = (w - 80) / 7;
                int maxBarHeight = h - 50;

                // Determine which day of week had check-in
                int[] data = new int[7];
                int todayDayOfWeek = -1;

                // Parse lastCheckInDate to determine day of week
                if (lastCheckInDate != null && !lastCheckInDate.isEmpty()) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        Date checkInDate = sdf.parse(lastCheckInDate);
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        cal.setTime(checkInDate);
                        int dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK);
                        // Convert to 0=Monday...6=Sunday
                        int index = (dayOfWeek == java.util.Calendar.SUNDAY) ? 6 : dayOfWeek - 2;
                        if (index >= 0 && index < 7) {
                            data[index] = 1;
                            todayDayOfWeek = index;
                        }
                    } catch (Exception e) {
                        // Ignore parse errors
                    }
                }

                String[] days = { "Th 2", "Th 3", "Th 4", "Th 5", "Th 6", "Th 7", "CN" };

                for (int i = 0; i < 7; i++) {
                    int barHeight = data[i] > 0 ? (int) (maxBarHeight * 0.7) : (int) (maxBarHeight * 0.1);
                    if (barHeight < 10)
                        barHeight = 10;
                    int x = 40 + i * barWidth;
                    int y = h - 30 - barHeight;

                    // Bar - highlight the check-in day
                    if (data[i] > 0) {
                        g2.setColor(PRIMARY_BLUE);
                    } else {
                        g2.setColor(new Color(219, 234, 254));
                    }
                    g2.fillRoundRect(x, y, barWidth - 10, barHeight, 6, 6);

                    // Day label - highlight current day
                    g2.setColor(i == todayDayOfWeek ? PRIMARY_BLUE : TEXT_GRAY);
                    g2.setFont(new Font("Segoe UI", i == todayDayOfWeek ? Font.BOLD : Font.PLAIN, 11));
                    FontMetrics fm = g2.getFontMetrics();
                    int labelX = x + (barWidth - 10 - fm.stringWidth(days[i])) / 2;
                    g2.drawString(days[i], labelX, h - 10);
                }

                g2.dispose();
            }
        };
        chartPanel.setOpaque(false);
        chartPanel.setPreferredSize(new Dimension(0, 180));

        card.add(header, BorderLayout.NORTH);
        card.add(chartPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createActivityLogPanel() {
        JPanel card = createCardBase();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);
        JLabel logIcon = new JLabel("🕐");
        logIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        JLabel logTitle = new JLabel("Nhật ký vào ra");
        logTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        logTitle.setForeground(TEXT_DARK);
        titleRow.add(logIcon);
        titleRow.add(logTitle);
        header.add(titleRow, BorderLayout.WEST);

        JLabel viewAllLink = new JLabel("Xem tất cả");
        viewAllLink.setFont(new Font("Segoe UI", Font.BOLD, 11));
        viewAllLink.setForeground(PRIMARY_BLUE);
        viewAllLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        header.add(viewAllLink, BorderLayout.EAST);

        // Log entries
        activityLogPanel = new JPanel();
        activityLogPanel.setLayout(new BoxLayout(activityLogPanel, BoxLayout.Y_AXIS));
        activityLogPanel.setOpaque(false);
        activityLogPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        // Add placeholder
        JLabel placeholderLabel = new JLabel("Chưa có hoạt động nào");
        placeholderLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        placeholderLabel.setForeground(TEXT_GRAY);
        activityLogPanel.add(placeholderLabel);

        // Footer
        JLabel footerLink = new JLabel("Xem lịch sử đầy đủ");
        footerLink.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footerLink.setForeground(TEXT_GRAY);
        footerLink.setHorizontalAlignment(SwingConstants.CENTER);
        footerLink.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JScrollPane scrollPane = new JScrollPane(activityLogPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        card.add(header, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        card.add(footerLink, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createCardBase() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }
        };
    }

    private JPanel createIconCircle(Color bgColor, String icon) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        panel.setLayout(new GridBagLayout());
        panel.setPreferredSize(new Dimension(40, 40));
        panel.setOpaque(false);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        panel.add(iconLabel);

        return panel;
    }

    private void addLogEntry(String type, String time, String location, String date) {
        JPanel entry = new JPanel(new BorderLayout());
        entry.setOpaque(false);
        entry.setBorder(new EmptyBorder(8, 0, 8, 0));
        entry.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        entry.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Left - Icon and info
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);

        boolean isCheckIn = type.equals("Check-in");
        Color iconBg = isCheckIn ? new Color(220, 252, 231) : new Color(254, 226, 226);
        Color iconFg = isCheckIn ? PRIMARY_GREEN : PRIMARY_PINK;

        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(iconBg);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        iconPanel.setLayout(new GridBagLayout());
        iconPanel.setPreferredSize(new Dimension(35, 35));
        iconPanel.setOpaque(false);
        JLabel arrow = new JLabel(isCheckIn ? "→" : "←");
        arrow.setFont(new Font("Segoe UI", Font.BOLD, 14));
        arrow.setForeground(iconFg);
        iconPanel.add(arrow);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        JLabel typeLabel = new JLabel(type);
        typeLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        typeLabel.setForeground(TEXT_DARK);
        JLabel detailLabel = new JLabel(time + " • " + location);
        detailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        detailLabel.setForeground(TEXT_GRAY);
        infoPanel.add(typeLabel);
        infoPanel.add(detailLabel);

        leftPanel.add(iconPanel);
        leftPanel.add(infoPanel);

        // Right - Date
        JLabel dateLabel = new JLabel(date);
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLabel.setForeground(TEXT_GRAY);

        entry.add(leftPanel, BorderLayout.WEST);
        entry.add(dateLabel, BorderLayout.EAST);

        activityLogPanel.add(entry);
    }

    /**
     * Public method to refresh statistics data (called by UserPanel on tab change)
     */
    public void refreshData() {
        try {
            // Reload purchased services from card
            cardComm.loadPurchasedServices(purchasedServices);

            // Load package info
            PackageInfo pkg = cardComm.getPackage();
            packageNameLabel.setText(pkg.getPackageTypeName());
            packageExpiryLabel.setText(pkg.expiry.isEmpty() ? "--" : pkg.expiry);
            if (pkg.type > 0) {
                packageStatusLabel.setText("Đang hoạt động");
            } else {
                packageStatusLabel.setText("Chưa kích hoạt");
            }

            // Load balance
            long balance = cardComm.getBalance();
            balanceValueLabel.setText(String.format("%,d", balance));

            // Load services with count using new method
            java.util.Map<String, Integer> servicesMap = cardComm.getPurchasedServicesWithCount();
            int totalServices = 0;
            for (int count : servicesMap.values()) {
                totalServices += count;
            }
            servicesCountLabel.setText(String.valueOf(totalServices));

            if (!servicesMap.isEmpty()) {
                // Build service list with counts: "Khăn tập x2, Nước uống x1"
                StringBuilder sb = new StringBuilder();
                int shown = 0;
                for (java.util.Map.Entry<String, Integer> entry : servicesMap.entrySet()) {
                    if (shown > 0)
                        sb.append(", ");
                    sb.append(entry.getKey()).append(" x").append(entry.getValue());
                    shown++;
                    if (shown >= 3)
                        break; // Show max 3 services
                }
                if (servicesMap.size() > 3) {
                    sb.append(" +").append(servicesMap.size() - 3).append(" khác");
                }
                servicesInfoLabel.setText(sb.toString());
            } else {
                servicesInfoLabel.setText("Chưa đăng ký dịch vụ bổ sung");
            }

            // Load check-in count
            int checkInCount = cardComm.getCheckInCount();
            totalSessionsLabel.setText(String.valueOf(checkInCount));

            // Update activity log - hiển thị lịch sử từ thẻ (tối đa 10 entries)
            activityLogPanel.removeAll();
            CheckInInfo lastCheckIn = cardComm.getLastCheckIn();
            java.util.List<CheckInLogEntry> history = cardComm.getCheckInHistory();
            if (history != null && !history.isEmpty()) {
                for (CheckInLogEntry entry : history) {
                    if (entry != null && entry.date != null && !entry.date.isEmpty()) {
                        addLogEntry("Tập gym", entry.checkInTime + " - " + entry.checkOutTime,
                                entry.getTotalTimeText(), entry.date);
                    }
                }
                lastCheckInDate = lastCheckIn != null ? lastCheckIn.date : "";
            } else if (lastCheckIn != null && !lastCheckIn.date.isEmpty()) {
                lastCheckInDate = lastCheckIn.date;
                addLogEntry("Check-in", lastCheckIn.checkInTime, "Cổng chính", lastCheckIn.date);
                if (lastCheckIn.checkOutTime != null && !lastCheckIn.checkOutTime.isEmpty()) {
                    addLogEntry("Check-out", lastCheckIn.checkOutTime, "Cổng chính", lastCheckIn.date);
                }
            } else {
                lastCheckInDate = "";
                JLabel placeholderLabel = new JLabel("Chưa có hoạt động nào");
                placeholderLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                placeholderLabel.setForeground(TEXT_GRAY);
                activityLogPanel.add(placeholderLabel);
            }
            activityLogPanel.revalidate();
            activityLogPanel.repaint();

            // Repaint chart with new data
            if (chartPanel != null) {
                chartPanel.repaint();
            }

            // Update last update time
            String now = new SimpleDateFormat("HH:mm").format(new Date());
            lastUpdateLabel.setText("Cập nhật lần cuối: " + now);

            log("Đã tải thống kê từ thẻ");

        } catch (Exception ex) {
            log("LỖI tải thống kê: " + ex.getMessage());
        }
    }
}
