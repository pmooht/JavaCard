package gymcard.client.ui.tabs;

import gymcard.client.CardCommunicator;
import gymcard.client.PackageInfo;
import gymcard.client.ui.BaseTabPanel;
import gymcard.databaseManager.DatabaseManager;
import gymcard.databaseManager.DatabaseManager.PlanInfo;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Tab xem các gói tập - Gói theo ngày (15, 30, 60, 90 ngày)
 */
public class PackageTab extends BaseTabPanel {

    private JLabel currentPackageLabel;

    public PackageTab(CardCommunicator cardComm) {
        super(cardComm);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(248, 249, 250));

        // Current package info card at top
        JPanel currentPackagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(52, 152, 219), w, 0, new Color(41, 128, 185));
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, w, h, 20, 20);
            }
        };
        currentPackagePanel.setOpaque(false);
        currentPackagePanel.setPreferredSize(new Dimension(0, 110));
        currentPackagePanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 20, 3, 20);

        JLabel currentTitleLabel = new JLabel("GÓI TẬP HIỆN TẠI");
        currentTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        currentTitleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        currentPackagePanel.add(currentTitleLabel, gbc);

        currentPackageLabel = new JLabel("Chưa có gói tập");
        currentPackageLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        currentPackageLabel.setForeground(Color.WHITE);
        gbc.gridy = 1;
        currentPackagePanel.add(currentPackageLabel, gbc);

        add(currentPackagePanel, BorderLayout.NORTH);

        // Load packages from database
        JPanel packagesPanel = new JPanel();
        packagesPanel.setLayout(new BoxLayout(packagesPanel, BoxLayout.Y_AXIS));
        packagesPanel.setBackground(new Color(248, 249, 250));

        // Wrap in ScrollPane
        JScrollPane scrollPane = new JScrollPane(packagesPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(new Color(248, 249, 250));

        // Load gói tập từ DB
        try {
            DatabaseManager db = DatabaseManager.getInstance();
            List<PlanInfo> plans = db.getActivePlans();

            // Icons and colors for day-based packages
            String[] icons = { "📅", "📆", "🗓", "📋" };
            Color[] colors = {
                    new Color(46, 204, 113), // 15 days - green
                    new Color(52, 152, 219), // 30 days - blue
                    new Color(155, 89, 182), // 60 days - purple
                    new Color(230, 126, 34) // 90 days - orange
            };

            for (int i = 0; i < plans.size(); i++) {
                PlanInfo plan = plans.get(i);
                Color color = colors[i % colors.length];
                String icon = icons[i % icons.length];

                JPanel card = createDBPackageCard(plan, icon, color, currentPackageLabel);
                card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
                packagesPanel.add(card);
                packagesPanel.add(Box.createVerticalStrut(10));
            }

        } catch (SQLException ex) {
            JLabel errorLabel = new JLabel("Không thể load gói tập từ database: " + ex.getMessage());
            errorLabel.setForeground(Color.RED);
            packagesPanel.add(errorLabel);
        }

        add(scrollPane, BorderLayout.CENTER);

        // Auto-load current package from card
        loadCurrentPackage();
    }

    private JPanel createDBPackageCard(PlanInfo plan, String icon, Color color, JLabel currentPackageLabel) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2, true),
                new EmptyBorder(15, 15, 15, 15)));

        // Left: Icon
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        iconLabel.setForeground(color);
        iconLabel.setPreferredSize(new Dimension(60, 60));
        iconLabel.setMinimumSize(new Dimension(60, 60));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(iconLabel, BorderLayout.WEST);

        // Center: Info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);

        JLabel nameLabel = new JLabel(plan.name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setForeground(color);
        infoPanel.add(nameLabel);

        JLabel descLabel = new JLabel(plan.description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(Color.GRAY);
        infoPanel.add(descLabel);

        // Hàng thời hạn và thời lượng
        JLabel durationLabel = new JLabel("Thời hạn: " + plan.durationDays + " ngày | " + plan.getMaxDurationText());
        durationLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        durationLabel.setForeground(new Color(100, 100, 100));
        infoPanel.add(durationLabel);

        // Hiển thị số buổi nếu là gói theo lượt
        if (plan.sessionCount > 0) {
            JLabel sessionsLabel = new JLabel("⚙ " + plan.sessionCount + " buổi tập");
            sessionsLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            sessionsLabel.setForeground(new Color(155, 89, 182));
            infoPanel.add(sessionsLabel);
        }

        card.add(infoPanel, BorderLayout.CENTER);

        // Right: Price + Buy button
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Color.WHITE);

        JLabel priceLabel = new JLabel(String.format("%,.0f VNĐ", plan.price));
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        priceLabel.setForeground(color);
        priceLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        rightPanel.add(priceLabel);

        rightPanel.add(Box.createVerticalStrut(5));

        JButton buyBtn = new JButton("Mua / Gia hạn");
        buyBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        buyBtn.setBackground(color);
        buyBtn.setForeground(Color.WHITE);
        buyBtn.setFocusPainted(false);
        buyBtn.setBorderPainted(false);
        buyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        buyBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);

        buyBtn.addActionListener(e -> showPurchaseDialog(plan, color, currentPackageLabel));

        rightPanel.add(buyBtn);
        card.add(rightPanel, BorderLayout.EAST);

        return card;
    }

    private void showPurchaseDialog(PlanInfo plan, Color color, JLabel currentPackageLabel) {
        // Get current package to check if extending
        PackageInfo currentPkg = null;
        String currentExpiry = "";
        try {
            currentPkg = cardComm.getPackage();
            if (currentPkg != null && currentPkg.type > 0 && !currentPkg.expiry.isEmpty()) {
                currentExpiry = currentPkg.expiry;
            }
        } catch (Exception ex) {
            // No existing package
        }

        // Calculate new expiry date
        Calendar cal = Calendar.getInstance();
        String baseDate = "hôm nay";

        // If has existing package and not expired, add days to current expiry
        if (!currentExpiry.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                Date expiryDate = sdf.parse(currentExpiry);
                if (expiryDate.after(new Date())) {
                    // Still valid, extend from expiry date
                    cal.setTime(expiryDate);
                    baseDate = currentExpiry;
                }
            } catch (Exception e) {
                // Use today if parse fails
            }
        }

        cal.add(Calendar.DAY_OF_MONTH, plan.durationDays);
        String newExpiry = new SimpleDateFormat("dd/MM/yyyy").format(cal.getTime());

        // Get current balance
        long currentBalance = 0;
        try {
            currentBalance = cardComm.getBalance();
        } catch (Exception ex) {
        }

        // Create dialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Xác nhận mua gói", true);
        dialog.setSize(420, 320);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(Color.WHITE);

        // Header with gradient
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, color, getWidth(), 0, color.darker());
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setPreferredSize(new Dimension(0, 70));
        headerPanel.setLayout(new BorderLayout());

        JPanel headerContent = new JPanel();
        headerContent.setOpaque(false);
        headerContent.setLayout(new BoxLayout(headerContent, BoxLayout.Y_AXIS));
        headerContent.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel(plan.name);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(plan.durationDays + " ngày tập không giới hạn");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(255, 255, 255, 200));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        headerContent.add(titleLabel);
        headerContent.add(Box.createVerticalStrut(3));
        headerContent.add(subtitleLabel);
        headerPanel.add(headerContent, BorderLayout.WEST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Content
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Info rows
        contentPanel.add(createInfoRow("💰 Giá gói:", String.format("%,.0f VNĐ", plan.price), color, true));
        contentPanel.add(Box.createVerticalStrut(12));
        contentPanel.add(createInfoRow("📅 Hết hạn mới:", newExpiry, new Color(46, 204, 113), true));
        contentPanel.add(Box.createVerticalStrut(12));
        contentPanel.add(createInfoRow("📍 Cộng từ:", baseDate, new Color(100, 116, 139), false));
        contentPanel.add(Box.createVerticalStrut(12));
        contentPanel.add(createInfoRow("💳 Số dư hiện tại:", String.format("%,d VND", currentBalance),
                currentBalance >= (long) plan.price ? new Color(46, 204, 113) : new Color(239, 68, 68), true));

        // Warning if not enough balance
        if (currentBalance < (long) plan.price) {
            contentPanel.add(Box.createVerticalStrut(15));
            JLabel warningLabel = new JLabel("⚠️ Số dư không đủ! Vui lòng nạp thêm tiền.");
            warningLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            warningLabel.setForeground(new Color(239, 68, 68));
            warningLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPanel.add(warningLabel);
        }

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(0, 20, 20, 20));

        long priceInVND = (long) plan.price;
        final long finalBalance = currentBalance;
        final String finalNewExpiry = newExpiry;

        // Confirm button with custom painting
        JButton confirmBtn = new JButton("✓ Xác nhận mua") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2d.setColor(color.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(color.brighter());
                } else {
                    g2d.setColor(color);
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
            }
        };
        confirmBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        confirmBtn.setPreferredSize(new Dimension(160, 42));
        confirmBtn.setBorderPainted(false);
        confirmBtn.setContentAreaFilled(false);
        confirmBtn.setFocusPainted(false);
        confirmBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Button always enabled - show popup if insufficient balance when clicked

        confirmBtn.addActionListener(ev -> {
            try {
                if (finalBalance < priceInVND) {
                    JOptionPane.showMessageDialog(dialog,
                            "Số dư không đủ! Vui lòng nạp thêm tiền.",
                            "Không đủ tiền", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (cardComm.deductBalance(priceInVND)) {
                    // Use plan.id as package type to preserve the package name
                    byte packageType = (byte) plan.id;
                    if (packageType <= 0)
                        packageType = 1; // fallback
                    String today = new SimpleDateFormat("dd/MM/yyyy").format(new Date());

                    cardComm.setPackage(packageType, finalNewExpiry, today, (short) 0);

                    String pkgInfo = String.format("%s | Hết hạn: %s", plan.name, finalNewExpiry);
                    currentPackageLabel.setText(pkgInfo);

                    long newBalance = cardComm.getBalance();

                    log("Đã mua thành công: " + plan.name + " - " + String.format("%,.0f", plan.price) + " VND");
                    JOptionPane.showMessageDialog(dialog,
                            "🎉 Mua gói thành công!\n\n" + plan.name + "\nHết hạn: " + finalNewExpiry +
                                    "\nSố dư còn lại: " + String.format("%,d VND", newBalance),
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Lỗi: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Cancel button with custom painting
        JButton cancelBtn = new JButton("✕ Hủy") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color btnColor = new Color(100, 116, 139);
                if (getModel().isPressed()) {
                    g2d.setColor(btnColor.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(btnColor);
                } else {
                    g2d.setColor(new Color(226, 232, 240));
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                g2d.setColor(getModel().isRollover() || getModel().isPressed() ? Color.WHITE : new Color(71, 85, 105));
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
            }
        };
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cancelBtn.setPreferredSize(new Dimension(100, 42));
        cancelBtn.setBorderPainted(false);
        cancelBtn.setContentAreaFilled(false);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(ev -> dialog.dispose());

        buttonPanel.add(confirmBtn);
        buttonPanel.add(cancelBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setContentPane(mainPanel);
        dialog.setVisible(true);
    }

    private JPanel createInfoRow(String label, String value, Color valueColor, boolean bold) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        labelComp.setForeground(new Color(100, 116, 139));

        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, 14));
        valueComp.setForeground(valueColor);
        valueComp.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(labelComp, BorderLayout.WEST);
        row.add(valueComp, BorderLayout.EAST);

        return row;
    }

    /**
     * Load current package from card
     */
    private void loadCurrentPackage() {
        if (currentPackageLabel == null)
            return;

        try {
            PackageInfo pkg = cardComm.getPackage();
            if (pkg != null && pkg.type > 0) {
                String expiry = pkg.expiry.isEmpty() ? "--" : pkg.expiry;
                String packageInfo = String.format("%s | Hết hạn: %s", pkg.getPackageTypeName(), expiry);
                currentPackageLabel.setText(packageInfo);
                log("Đã tải gói tập hiện tại: " + pkg.getPackageTypeName());
            } else {
                currentPackageLabel.setText("Chưa có gói tập");
            }
        } catch (Exception ex) {
            // Silent fail if not authenticated yet - don't change label
            if (!ex.getMessage().contains("PIN")) {
                log("Không thể tải gói tập: " + ex.getMessage());
            }
        }
    }

    /**
     * Refresh data from database + load current package from card
     */
    public void refreshData() {
        removeAll();
        initUI(); // This already calls loadCurrentPackage()
        revalidate();
        repaint();
        log("Đã reload gói tập từ database");
    }
}
