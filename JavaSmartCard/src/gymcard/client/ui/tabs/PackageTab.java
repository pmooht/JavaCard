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
 * Tab xem các gói tập - Load từ Database với UI đẹp
 */
public class PackageTab extends BaseTabPanel {

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

        JLabel currentPackageLabel = new JLabel("Chưa có gói tập");
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

            String[] icons = { "[1M]", "[3M]", "[6M]", "[1Y]", "[10B]", "[30B]", "[VIP]" };
            Color[] colors = {
                    new Color(46, 204, 113),
                    new Color(52, 152, 219),
                    new Color(155, 89, 182),
                    new Color(230, 126, 34),
                    new Color(241, 196, 15),
                    new Color(231, 76, 60),
                    new Color(142, 68, 173)
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

        // Load current package button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(new Color(248, 249, 250));
        JButton loadBtn = createModernButton("Tải gói hiện tại từ thẻ", new Color(52, 152, 219), 14);
        loadBtn.setPreferredSize(new Dimension(240, 40));
        loadBtn.addActionListener(e -> {
            try {
                PackageInfo pkg = cardComm.getPackage();

                String packageInfo = String.format("%s | Đăng ký: %s | Hết hạn: %s",
                        pkg.getPackageTypeName(), pkg.registration, pkg.expiry);
                if (pkg.type == 2) {
                    packageInfo += " | Còn: " + pkg.remainingSessions + " buổi";
                }
                currentPackageLabel.setText(packageInfo);
                log("Đã tải thông tin gói tập hiện tại");

            } catch (Exception ex) {
                log("LỖI: " + ex.getMessage());
                JOptionPane.showMessageDialog(this,
                        "Lỗi tải thông tin: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(loadBtn);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createDBPackageCard(PlanInfo plan, String icon, Color color, JLabel currentPackageLabel) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2, true),
                new EmptyBorder(15, 15, 15, 15)));

        // Left: Icon
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        iconLabel.setForeground(color);
        iconLabel.setPreferredSize(new Dimension(80, 60));
        iconLabel.setMinimumSize(new Dimension(80, 60));
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

        String duration = "";
        if (plan.durationDays > 0) {
            duration = plan.durationDays + " ngày";
        } else if (plan.sessionCount > 0) {
            duration = plan.sessionCount + " buổi";
        }
        JLabel durationLabel = new JLabel("Thời hạn: " + duration);
        durationLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        durationLabel.setForeground(new Color(100, 100, 100));
        infoPanel.add(durationLabel);

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

        JButton buyBtn = new JButton("Mua ngay");
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
        // Kiểm tra gói hiện tại
        PackageInfo currentPkg = null;
        boolean hasExistingPackage = false;
        try {
            currentPkg = cardComm.getPackage();
            if (currentPkg != null && currentPkg.type > 0) {
                hasExistingPackage = true;
            }
        } catch (Exception ex) {
            // Không có gói
        }

        double originalPrice = plan.price;
        double finalPrice = originalPrice;
        String discountInfo = "";

        if (hasExistingPackage) {
            finalPrice = originalPrice * 0.5;
            discountInfo = "GIẢM 50% khi nâng cấp!";
        }

        // Tạo dialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Xác nhận mua gói", true);
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // Header
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, color, getWidth(), 0, color.darker());
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
        };
        headerPanel.setPreferredSize(new Dimension(0, 60));
        headerPanel.setLayout(new BorderLayout());
        JLabel titleLabel = new JLabel(plan.name, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Content
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        GridBagConstraints gbcContent = new GridBagConstraints();
        gbcContent.insets = new Insets(8, 10, 8, 10);
        gbcContent.anchor = GridBagConstraints.WEST;

        int row = 0;

        gbcContent.gridx = 0;
        gbcContent.gridy = row;
        contentPanel.add(new JLabel("Mô tả:"), gbcContent);
        gbcContent.gridx = 1;
        JLabel descValueLabel = new JLabel(plan.description);
        descValueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        contentPanel.add(descValueLabel, gbcContent);

        row++;
        gbcContent.gridx = 0;
        gbcContent.gridy = row;
        contentPanel.add(new JLabel("Giá gốc:"), gbcContent);
        gbcContent.gridx = 1;
        JLabel origPriceLabel = new JLabel(String.format("%,.0f VNĐ", originalPrice));
        if (hasExistingPackage) {
            origPriceLabel.setText("<html><s>" + String.format("%,.0f VNĐ", originalPrice) + "</s></html>");
            origPriceLabel.setForeground(Color.GRAY);
        }
        contentPanel.add(origPriceLabel, gbcContent);

        if (hasExistingPackage) {
            row++;
            gbcContent.gridx = 0;
            gbcContent.gridy = row;
            JLabel discountLabel = new JLabel(discountInfo);
            discountLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            discountLabel.setForeground(new Color(231, 76, 60));
            gbcContent.gridwidth = 2;
            contentPanel.add(discountLabel, gbcContent);
            gbcContent.gridwidth = 1;

            row++;
            gbcContent.gridx = 0;
            gbcContent.gridy = row;
            contentPanel.add(new JLabel("Thanh toán:"), gbcContent);
            gbcContent.gridx = 1;
            JLabel finalPriceLabel = new JLabel(String.format("%,.0f VNĐ", finalPrice));
            finalPriceLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            finalPriceLabel.setForeground(new Color(46, 204, 113));
            contentPanel.add(finalPriceLabel, gbcContent);
        }

        row++;
        gbcContent.gridx = 0;
        gbcContent.gridy = row;
        contentPanel.add(new JLabel("So du hien tai:"), gbcContent);
        gbcContent.gridx = 1;
        long currentBalance = 0;
        try {
            currentBalance = cardComm.getBalance();
        } catch (Exception ex) {
        }
        JLabel balanceLabel = new JLabel(String.format("%,d VND", currentBalance));
        balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        contentPanel.add(balanceLabel, gbcContent);

        // PIN field
        row++;
        gbcContent.gridx = 0;
        gbcContent.gridy = row;
        JLabel pinLabel = new JLabel("Nhập mã PIN:");
        pinLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pinLabel.setForeground(new Color(231, 76, 60));
        contentPanel.add(pinLabel, gbcContent);
        gbcContent.gridx = 1;
        JPasswordField pinField = new JPasswordField(10);
        pinField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pinField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(231, 76, 60), 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        contentPanel.add(pinField, gbcContent);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(Color.WHITE);

        double priceToDeduct = finalPrice;
        long priceInVND = (long) priceToDeduct;
        final long finalBalance = currentBalance;

        JButton confirmBtn = new JButton("Xác nhận thanh toán");
        confirmBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        confirmBtn.setBackground(color);
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setFocusPainted(false);
        confirmBtn.addActionListener(ev -> {
            try {
                String enteredPin = new String(pinField.getPassword()).trim();
                if (enteredPin.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                            "Vui lòng nhập mã PIN để xác thực!",
                            "Thiếu mã PIN", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (!enteredPin.matches("\\d{6}")) {
                    JOptionPane.showMessageDialog(dialog,
                            "Mã PIN phải gồm 6 chữ số!",
                            "PIN không hợp lệ", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                boolean pinValid = cardComm.verifyPin(enteredPin);
                if (!pinValid) {
                    JOptionPane.showMessageDialog(dialog,
                            "Mã PIN không chính xác!",
                            "Sai PIN", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (finalBalance < priceInVND) {
                    JOptionPane.showMessageDialog(dialog,
                            "So du khong du! Vui long nap them tien.",
                            "Khong du tien", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (cardComm.deductBalance(priceInVND)) {
                    byte packageType = 1;
                    if (plan.code.startsWith("SESSION")) {
                        packageType = 2;
                    } else if (plan.code.equals("VIP")) {
                        packageType = 3;
                    }

                    String today = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
                    Calendar cal = Calendar.getInstance();
                    if (plan.durationDays > 0) {
                        cal.add(Calendar.DAY_OF_MONTH, plan.durationDays);
                    } else {
                        cal.add(Calendar.YEAR, 1);
                    }
                    String expiry = new SimpleDateFormat("dd/MM/yyyy").format(cal.getTime());
                    short sessions = (short) plan.sessionCount;

                    cardComm.setPackage(packageType, expiry, today, sessions);

                    String pkgInfo = String.format("%s | Đăng ký: %s | Hết hạn: %s",
                            plan.name, today, expiry);
                    if (packageType == 2) {
                        pkgInfo += " | Còn: " + sessions + " buổi";
                    }
                    currentPackageLabel.setText(pkgInfo);

                    long newBalance = cardComm.getBalance();
                    balanceLabel.setText(String.format("%,d VND", newBalance));

                    log("Da mua thanh cong: " + plan.name + " - " + String.format("%,.0f", priceToDeduct) + " VND");
                    JOptionPane.showMessageDialog(dialog,
                            "Mua goi thanh cong!\n" + plan.name + "\nSo du con lai: "
                                    + String.format("%,d VND", newBalance),
                            "Thanh cong", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Lỗi: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelBtn = new JButton("Hủy");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cancelBtn.addActionListener(ev -> dialog.dispose());

        buttonPanel.add(confirmBtn);
        buttonPanel.add(cancelBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setContentPane(mainPanel);
        dialog.setVisible(true);
    }

    /**
     * Refresh data from database - reload all packages
     */
    public void refreshData() {
        // Remove all components and rebuild UI
        removeAll();
        initUI();
        revalidate();
        repaint();
        log("Da reload goi tap tu database");
    }
}
