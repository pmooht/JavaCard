package gymcard.client.ui;

import gymcard.client.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.*;
import com.toedter.calendar.JCalendar;
import java.util.Calendar;


/**
 * User Panel - Dành cho hội viên
 */
public class UserPanel extends JPanel {
    
    private final CardCommunicator cardComm;
    private JTextArea logArea;
    
    // Components
    private JPanel contentPanel;
    private CardLayout cardLayout;
    
    public UserPanel(CardCommunicator cardComm) {
        this.cardComm = cardComm;
        initUI();
    }
    
    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(new Color(248, 249, 250));
        
        // Header with gradient
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(46, 204, 113), w, 0, new Color(26, 188, 156));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("HỘI VIÊN GYM CARD");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);
        
        // Content area with CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        
        contentPanel.add(createLoginPanel(), "login");
        contentPanel.add(createMainPanel(), "main");
        
        add(contentPanel, BorderLayout.CENTER);
        
        // Log area
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBackground(Color.WHITE);
        logPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(149, 165, 166), 2), 
            "Thông báo",
            TitledBorder.LEFT, 
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13)
        ));
        
        logArea = new JTextArea(5, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(250, 250, 250));
        JScrollPane logScroll = new JScrollPane(logArea);
        logPanel.add(logScroll, BorderLayout.CENTER);
        
        add(logPanel, BorderLayout.SOUTH);
        
        // Show login panel first
        cardLayout.show(contentPanel, "login");
    }
    
    /**
     * Panel đăng nhập
     */
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(248, 249, 250));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        
        // Icon or logo
        JLabel iconLabel = new JLabel("🔐");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 90));
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(iconLabel, gbc);
        
        // Title
        JLabel titleLabel = new JLabel("Vui lòng nhập mã PIN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(52, 73, 94));
        gbc.gridy = 1;
        panel.add(titleLabel, gbc);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Mã PIN gồm 4 chữ số");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(127, 140, 141));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 15, 15, 15);
        panel.add(subtitleLabel, gbc);
        
        // PIN field
        gbc.gridy = 3;
        gbc.insets = new Insets(10, 15, 10, 15);
        JPasswordField pinField = new JPasswordField(10);
        pinField.setFont(new Font("Segoe UI", Font.BOLD, 32));
        pinField.setHorizontalAlignment(JTextField.CENTER);
        pinField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 3),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)));
        panel.add(pinField, gbc);
        
        // Tries remaining label
        JLabel triesLabel = new JLabel("");
        triesLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        triesLabel.setForeground(new Color(231, 76, 60));
        gbc.gridy = 4;
        panel.add(triesLabel, gbc);
        
        // Login button
        JButton loginBtn = createModernButton("Đăng nhập", new Color(46, 204, 113), 17);
        loginBtn.setPreferredSize(new Dimension(200, 55));
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        panel.add(loginBtn, gbc);
        
        // Check tries button
        JButton checkTriesBtn = createModernButton("Kiểm tra số lần thử", new Color(155, 89, 182), 12);
        checkTriesBtn.setPreferredSize(new Dimension(200, 40));
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 15, 15, 15);
        panel.add(checkTriesBtn, gbc);
        
        // Action listeners
        ActionListener loginAction = e -> {
            try {
                if (!cardComm.isConnected()) {
                    log("Vui lòng kết nối thẻ trước!");
                    JOptionPane.showMessageDialog(this, 
                        "Vui lòng kết nối thẻ!",
                        "Lỗi", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                String pin = new String(pinField.getPassword());
                
                if (pin.length() != 4) {
                    JOptionPane.showMessageDialog(this, 
                        "Mã PIN phải có 4 chữ số!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Check remaining tries first
                int tries = cardComm.getPinTries();
                if (tries == 0) {
                    log("Thẻ đã bị khóa!");
                    JOptionPane.showMessageDialog(this, 
                        "Thẻ của bạn đã bị khóa do nhập sai PIN quá nhiều lần!\n" +
                        "Vui lòng liên hệ quản trị viên để mở khóa.",
                        "Thẻ bị khóa", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (cardComm.verifyPin(pin)) {
                    log("Đăng nhập thành công!");
                    pinField.setText("");
                    cardLayout.show(contentPanel, "main");
                } else {
                    tries = cardComm.getPinTries();
                    log("Sai mã PIN! Còn " + tries + " lần thử");
                    triesLabel.setText("⚠ Sai mã PIN! Còn " + tries + " lần thử");
                    pinField.setText("");
                    
                    if (tries == 0) {
                        JOptionPane.showMessageDialog(this, 
                            "Thẻ của bạn đã bị khóa!\n" +
                            "Vui lòng liên hệ quản trị viên để mở khóa.",
                            "Thẻ bị khóa", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, 
                            "Mã PIN không đúng!\nCòn " + tries + " lần thử.",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
                
            } catch (Exception ex) {
                log("LỖI: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, 
                    "Lỗi xác thực: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        };
        
        loginBtn.addActionListener(loginAction);
        pinField.addActionListener(loginAction);
        
        checkTriesBtn.addActionListener(e -> {
            try {
                if (!cardComm.isConnected()) {
                    log("Vui lòng kết nối thẻ trước!");
                    return;
                }
                
                int tries = cardComm.getPinTries();
                if (tries == 0) {
                    triesLabel.setText("Thẻ đã bị khóa!");
                    log("Thẻ đã bị khóa");
                } else {
                    triesLabel.setText("Còn " + tries + " lần nhập PIN");
                    log("Còn " + tries + " lần nhập PIN");
                }
                
            } catch (Exception ex) {
                log("LỖI: " + ex.getMessage());
            }
        });
        
        return panel;
    }
    
    /**
     * Panel chính sau khi đăng nhập
     */
    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(248, 249, 250));
        
        // Tabbed pane with modern style
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(new Color(248, 249, 250));
        
        tabbedPane.addTab("Thông tin cá nhân", createInfoTab());
        tabbedPane.addTab("Gói tập", createPackageTab());
        tabbedPane.addTab("Check-in/Check-out", createCheckInTab());
        tabbedPane.addTab("Thay đổi PIN", createChangePinTab());
        tabbedPane.addTab("Thanh toán", createPaymentTab());
        
        panel.add(tabbedPane, BorderLayout.CENTER);
        
        // Logout button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        bottomPanel.setBackground(new Color(248, 249, 250));
        JButton logoutBtn = createModernButton("🚪 Đăng xuất", new Color(149, 165, 166), 14);
        logoutBtn.setPreferredSize(new Dimension(150, 40));
        logoutBtn.addActionListener(e -> {
            cardLayout.show(contentPanel, "login");
            log("Đã đăng xuất");
        });
        bottomPanel.add(logoutBtn);
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Tab thông tin cá nhân - với avatar
     */
    private JPanel createInfoTab() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));
        panel.setBackground(new Color(248, 249, 250));
        
        // Title
        JLabel titleLabel = new JLabel("THÔNG TIN CÁ NHÂN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(52, 73, 94));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Main card container
        JPanel cardPanel = new JPanel(new BorderLayout(20, 20));
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 3),
            new EmptyBorder(30, 30, 30, 30)));
        
        // Left side - Avatar
        JPanel avatarPanel = new JPanel(new GridBagLayout());
        avatarPanel.setBackground(Color.WHITE);
        avatarPanel.setPreferredSize(new Dimension(220, 0));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Avatar placeholder
        JLabel avatarLabel = new JLabel("");
        avatarLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avatarLabel.setPreferredSize(new Dimension(150, 150));
        avatarLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 3),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        avatarLabel.setOpaque(true);
        avatarLabel.setBackground(new Color(240, 248, 255));
        gbc.gridx = 0; gbc.gridy = 0;
        avatarPanel.add(avatarLabel, gbc);
        
        cardPanel.add(avatarPanel, BorderLayout.WEST);
        
        // Right side - Info display
        JPanel infoPanel = new JPanel(new BorderLayout(10, 10));
        infoPanel.setBackground(Color.WHITE);
        
        // Info text area
        JTextArea infoArea = new JTextArea(15, 40);
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoArea.setBackground(new Color(250, 250, 250));
        infoArea.setBorder(new EmptyBorder(15, 15, 15, 15));
        JScrollPane scrollPane = new JScrollPane(infoArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        
        infoPanel.add(scrollPane, BorderLayout.CENTER);
        
        cardPanel.add(infoPanel, BorderLayout.CENTER);
        
        panel.add(cardPanel, BorderLayout.CENTER);
        
        // Load button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(new Color(248, 249, 250));
        JButton loadBtn = createModernButton("📋 Tải thông tin", new Color(52, 152, 219), 15);
        loadBtn.setPreferredSize(new Dimension(200, 50));
        loadBtn.addActionListener(e -> {
            try {
                MemberInfo member = cardComm.getMemberInfo();
                
                StringBuilder sb = new StringBuilder();
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                sb.append("     THÔNG TIN CÁ NHÂN\n");
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
                sb.append(String.format("Họ và tên:      %s\n\n", member.name));
                sb.append(String.format("Ngày sinh:      %s\n\n", member.birthDate));
                sb.append(String.format("Số điện thoại:  %s\n\n", member.phone));
                sb.append(String.format("Địa chỉ:        %s\n", member.address));
                
                infoArea.setText(sb.toString());
                log("Đã tải thông tin cá nhân");
                
            } catch (Exception ex) {
                log("LỖI: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, 
                    "Lỗi tải thông tin: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(loadBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Tab xem các gói tập
     */
    private JPanel createPackageTab() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));
        panel.setBackground(new Color(248, 249, 250));
        
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
        currentPackagePanel.setPreferredSize(new Dimension(0, 150));
        currentPackagePanel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 20, 5, 20);
        
        JLabel currentTitleLabel = new JLabel("GÓI TẬP HIỆN TẠI");
        currentTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        currentTitleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        currentPackagePanel.add(currentTitleLabel, gbc);
        
        JLabel currentPackageLabel = new JLabel("Chưa có gói tập");
        currentPackageLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        currentPackageLabel.setForeground(Color.WHITE);
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 20, 15, 20);
        currentPackagePanel.add(currentPackageLabel, gbc);
        
        panel.add(currentPackagePanel, BorderLayout.NORTH);
        
        // Available packages cards
        JPanel packagesPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        packagesPanel.setBackground(new Color(248, 249, 250));
        
        // Package definitions: name, price, duration/sessions, type
        String[][] packages = {
            {"Gói Tháng", "300", "30 ngày", "1"},
            {"Gói Buổi", "500", "20 buổi", "2"},
            {"Gói VIP", "2000", "90 ngày", "3"}
        };
        
        Color[] colors = {
            new Color(46, 204, 113),  // Green
            new Color(52, 152, 219),  // Blue
            new Color(155, 89, 182)   // Purple
        };
        
        for (int i = 0; i < packages.length; i++) {
            packagesPanel.add(createPackageCard(packages[i][0], packages[i][1], 
                packages[i][2], packages[i][3], colors[i], currentPackageLabel));
        }
        
        panel.add(packagesPanel, BorderLayout.CENTER);
        
        // Load current package button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(new Color(248, 249, 250));
        JButton loadBtn = createModernButton("Tải thông tin gói hiện tại", new Color(52, 152, 219), 14);
        loadBtn.setPreferredSize(new Dimension(250, 45));
        loadBtn.addActionListener(e -> {
            try {
                PackageInfo pkg = cardComm.getPackage();
                
                String packageInfo = String.format("%s | Đăng ký: %s | Hết hạn: %s",
                    pkg.getPackageTypeName(), pkg.registration, pkg.expiry);
                currentPackageLabel.setText(packageInfo);
                
                if (pkg.type == 2) {
                    currentPackageLabel.setText(packageInfo + " | Còn: " + pkg.remainingSessions + " buổi");
                }
                
                log("Đã tải thông tin gói tập hiện tại");
                
            } catch (Exception ex) {
                log("LỖI: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, 
                    "Lỗi tải thông tin: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(loadBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Create package card
     */
    private JPanel createPackageCard(String name, String price, String duration, 
                                      String type, Color color, JLabel currentPackageLabel) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 3),
            new EmptyBorder(25, 25, 25, 25)));
        
        // Content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        
        // Package name
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        nameLabel.setForeground(color);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(nameLabel);
        
        contentPanel.add(Box.createVerticalStrut(15));
        
        // Price
        JLabel priceLabel = new JLabel(price + "k VNĐ");
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        priceLabel.setForeground(new Color(52, 73, 94));
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(priceLabel);
        
        contentPanel.add(Box.createVerticalStrut(10));
        
        // Duration
        JLabel durationLabel = new JLabel(duration);
        durationLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        durationLabel.setForeground(new Color(127, 140, 141));
        durationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(durationLabel);
        
        card.add(contentPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton buyBtn = createModernButton("Mua gói", color, 15);
        buyBtn.setPreferredSize(new Dimension(150, 45));
        buyBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(card,
                "Bạn muốn mua " + name + " với giá " + price + "k VNĐ?\n" +
                "Hệ thống sẽ chuyển bạn đến tab Thanh toán.",
                "Xác nhận mua gói",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                // Switch to payment tab - find JTabbedPane by traversing up
                Container parent = card.getParent();
                while (parent != null && !(parent instanceof JTabbedPane)) {
                    parent = parent.getParent();
                }
                
                if (parent instanceof JTabbedPane) {
                    JTabbedPane tabbedPane = (JTabbedPane) parent;
                    tabbedPane.setSelectedIndex(4); // Payment tab is index 4
                }
                
                JOptionPane.showMessageDialog(card,
                    "Vui lòng thanh toán " + price + "k VNĐ tại tab Thanh toán.\n" +
                    "Sau khi thanh toán thành công, gói tập sẽ được kích hoạt.",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
                
                log("Đã chọn mua " + name + " - " + price + "k VNĐ");
            }
        });
        buttonPanel.add(buyBtn);
        
        card.add(buttonPanel, BorderLayout.SOUTH);
        
        return card;
    }
    
    /**
     * Tab check-in/check-out với lịch
     */
    private JPanel createCheckInTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(new Color(248, 249, 250));
        
        // Title - smaller
        JLabel titleLabel = new JLabel("LỊCH TẬP GYM");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(52, 73, 94));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(5, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Main content - split left (calendar) and right (buttons)
        JPanel mainPanel = new JPanel(new BorderLayout(15, 0));
        mainPanel.setBackground(new Color(248, 249, 250));
        
        // LEFT SIDE - Calendar (3/4 width)
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 3),
            new EmptyBorder(15, 15, 15, 15)));
        leftPanel.setPreferredSize(new Dimension(900, 0));
        
        // JCalendar component
        JCalendar calendar = new JCalendar();
        calendar.setBackground(Color.WHITE);
        calendar.setWeekOfYearVisible(false);
        
        // Customize calendar appearance - make month/year chooser wider
        calendar.getDayChooser().setFont(new Font("Segoe UI", Font.PLAIN, 16));
        calendar.getDayChooser().setDecorationBackgroundColor(new Color(52, 152, 219));
        calendar.getDayChooser().setWeekdayForeground(new Color(52, 73, 94));
        calendar.getDayChooser().setDecorationBackgroundVisible(true);
        calendar.getMonthChooser().setFont(new Font("Segoe UI", Font.BOLD, 18));
        calendar.getMonthChooser().getComboBox().setFont(new Font("Segoe UI", Font.BOLD, 18));
        calendar.getYearChooser().setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        // Make month/year area more prominent
        calendar.getMonthChooser().setPreferredSize(new Dimension(150, 35));
        calendar.getYearChooser().setPreferredSize(new Dimension(100, 35));
        
        // Create decorator for highlighting check-in days
        CheckInDayDecorator decorator = new CheckInDayDecorator(calendar);
        
        // Demo check-in data with times (in real app, this would come from card)
        java.util.Map<String, String[]> checkInTimes = new java.util.HashMap<>();
        checkInTimes.put("02/11/2025", new String[]{"08:00:00", "10:30:00"});
        checkInTimes.put("04/11/2025", new String[]{"07:45:00", "09:15:00"});
        checkInTimes.put("06/11/2025", new String[]{"18:30:00", "20:00:00"});
        checkInTimes.put("09/11/2025", new String[]{"08:15:00", "10:45:00"});
        checkInTimes.put("11/11/2025", new String[]{"19:00:00", "21:00:00"});
        checkInTimes.put("16/11/2025", new String[]{"08:30:00", "10:00:00"});
        checkInTimes.put("18/11/2025", new String[]{"07:30:00", "09:30:00"});
        checkInTimes.put("23/11/2025", new String[]{"17:45:00", "19:45:00"});
        checkInTimes.put("25/11/2025", new String[]{"08:00:00", "10:15:00"});
        checkInTimes.put("28/11/2025", new String[]{"18:00:00", "20:30:00"});
        
        // Load demo check-in dates
        decorator.addCheckInDates(checkInTimes.keySet());
        decorator.install();
        
        leftPanel.add(calendar, BorderLayout.CENTER);
        
        // RIGHT SIDE - Buttons and Statistics (1/4 width)
        JPanel rightPanel = new JPanel(new BorderLayout(0, 15));
        rightPanel.setBackground(new Color(248, 249, 250));
        rightPanel.setPreferredSize(new Dimension(300, 0));
        
        // Statistics panel
        JPanel statsPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        statsPanel.setBackground(new Color(240, 248, 255));
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
            new EmptyBorder(20, 20, 20, 20)));
        
        JLabel statsTitle = new JLabel("THỐNG KÊ");
        statsTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        statsTitle.setForeground(new Color(52, 73, 94));
        statsPanel.add(statsTitle);
        
        JLabel countLabel = new JLabel("Số ngày đã tập: 10 ngày");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statsPanel.add(countLabel);
        
        JLabel lastLabel = new JLabel("<html>Click vào ngày check-in<br>để xem chi tiết<br>giờ vào - ra</html>");
        lastLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lastLabel.setForeground(new Color(127, 140, 141));
        statsPanel.add(lastLabel);
        
        // Add click listener to calendar to show time details AFTER lastLabel is created
        calendar.getDayChooser().addPropertyChangeListener("day", evt -> {
            java.util.Calendar selectedCal = calendar.getCalendar();
            String selectedDate = new SimpleDateFormat("dd/MM/yyyy").format(selectedCal.getTime());
            
            // Check if this date has check-in data
            if (checkInTimes.containsKey(selectedDate)) {
                String[] times = checkInTimes.get(selectedDate);
                lastLabel.setText(String.format("<html>Ngày đã chọn:<br>%s<br>Vào: %s | Ra: %s</html>", 
                    selectedDate, times[0], times[1]));
                lastLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                lastLabel.setForeground(new Color(52, 73, 94));
                log("Xem chi tiết: " + selectedDate + " - " + times[0] + " -> " + times[1]);
            } else {
                lastLabel.setText("<html>Click vào ngày check-in<br>để xem chi tiết<br>giờ vào - ra</html>");
                lastLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                lastLabel.setForeground(new Color(127, 140, 141));
            }
        });
        
        rightPanel.add(statsPanel, BorderLayout.NORTH);
        
        // Buttons panel - only 2 buttons
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 15));
        buttonPanel.setBackground(new Color(248, 249, 250));
        buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        JButton checkInBtn = createModernButton("✓ CHECK-IN", new Color(46, 204, 113), 18);
        checkInBtn.setPreferredSize(new Dimension(0, 80));
        checkInBtn.addActionListener(e -> {
            try {
                String date = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
                String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
                
                if (cardComm.checkIn(date, time)) {
                    log("Check-in thành công!");
                    
                    CheckInInfo info = cardComm.getLastCheckIn();
                    int count = cardComm.getCheckInCount();
                    
                    countLabel.setText("Số ngày đã tập: " + count + " ngày");
                    lastLabel.setText(String.format("<html>Lần tập gần nhất:<br>%s<br>Vào: %s | Ra: %s</html>", 
                        info.date, info.checkInTime, info.checkOutTime));
                    
                    // Add today to check-in calendar
                    decorator.addCheckInDate(date);
                    
                    JOptionPane.showMessageDialog(this, 
                        "Check-in thành công!\nChúc bạn buổi tập tốt! 💪",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    log("Check-in thất bại");
                }
                
            } catch (Exception ex) {
                log("LỖI: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, 
                    "Lỗi check-in: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JButton checkOutBtn = createModernButton("✗ CHECK-OUT", new Color(231, 76, 60), 18);
        checkOutBtn.setPreferredSize(new Dimension(0, 80));
        checkOutBtn.addActionListener(e -> {
            try {
                String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
                
                if (cardComm.checkOut(time)) {
                    log("Check-out thành công!");
                    
                    CheckInInfo info = cardComm.getLastCheckIn();
                    lastLabel.setText(String.format("<html>Lần tập gần nhất:<br>%s<br>Vào: %s | Ra: %s</html>", 
                        info.date, info.checkInTime, info.checkOutTime));
                    
                    JOptionPane.showMessageDialog(this, 
                        "Check-out thành công!\nHẹn gặp lại! 👋",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    log("Check-out thất bại");
                }
                
            } catch (Exception ex) {
                log("LỖI: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, 
                    "Lỗi check-out: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        buttonPanel.add(checkInBtn);
        buttonPanel.add(checkOutBtn);
        
        rightPanel.add(buttonPanel, BorderLayout.CENTER);
        
        // Add left and right panels to main
        mainPanel.add(leftPanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);
        
        panel.add(mainPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Tab thay đổi PIN
     */
    private JPanel createChangePinTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(40, 40, 40, 40));
        panel.setBackground(new Color(248, 249, 250));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Info label
        JLabel infoLabel = new JLabel("Thay đổi mã PIN của bạn");
        infoLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        infoLabel.setForeground(new Color(52, 73, 94));
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(infoLabel, gbc);
        
        gbc.gridwidth = 1;
        
        // Old PIN
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel oldPinLabel = new JLabel("Mã PIN hiện tại:");
        oldPinLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        panel.add(oldPinLabel, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JPasswordField oldPinField = new JPasswordField(15);
        oldPinField.setFont(new Font("Segoe UI", Font.BOLD, 18));
        oldPinField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        panel.add(oldPinField, gbc);
        
        // New PIN
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel newPinLabel = new JLabel("Mã PIN mới:");
        newPinLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        panel.add(newPinLabel, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JPasswordField newPinField = new JPasswordField(15);
        newPinField.setFont(new Font("Segoe UI", Font.BOLD, 18));
        newPinField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        panel.add(newPinField, gbc);
        
        // Confirm PIN
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel confirmPinLabel = new JLabel("Xác nhận PIN mới:");
        confirmPinLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        panel.add(confirmPinLabel, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JPasswordField confirmPinField = new JPasswordField(15);
        confirmPinField.setFont(new Font("Segoe UI", Font.BOLD, 18));
        confirmPinField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        panel.add(confirmPinField, gbc);
        
        // Change button
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(25, 12, 12, 12);
        JButton changeBtn = createModernButton("Thay đổi PIN", new Color(52, 152, 219), 16);
        changeBtn.setPreferredSize(new Dimension(220, 55));
        changeBtn.addActionListener(e -> {
            try {
                String oldPin = new String(oldPinField.getPassword());
                String newPin = new String(newPinField.getPassword());
                String confirmPin = new String(confirmPinField.getPassword());
                
                if (oldPin.length() != 4 || newPin.length() != 4) {
                    JOptionPane.showMessageDialog(this, 
                        "Mã PIN phải có 4 chữ số!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (!newPin.equals(confirmPin)) {
                    JOptionPane.showMessageDialog(this, 
                        "Mã PIN mới và xác nhận không khớp!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (cardComm.changePin(oldPin, newPin)) {
                    log("Đã thay đổi PIN thành công");
                    JOptionPane.showMessageDialog(this, 
                        "Thay đổi mã PIN thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    oldPinField.setText("");
                    newPinField.setText("");
                    confirmPinField.setText("");
                } else {
                    log("Thay đổi PIN thất bại");
                    JOptionPane.showMessageDialog(this, 
                        "Thay đổi PIN thất bại!\nVui lòng kiểm tra lại mã PIN hiện tại.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (Exception ex) {
                log("LỖI: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, 
                    "Lỗi thay đổi PIN: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(changeBtn, gbc);
        
        return panel;
    }
    
    /**
     * Tab thanh toán
     */
    private JPanel createPaymentTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(248, 249, 250));
        
        // Balance display with modern card style - full width at top
        JPanel balancePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(241, 196, 15), w, 0, new Color(243, 156, 18));
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, w, h, 20, 20);
            }
        };
        balancePanel.setOpaque(false);
        balancePanel.setPreferredSize(new Dimension(0, 120));
        balancePanel.setLayout(new BorderLayout());
        
        JPanel balanceContent = new JPanel();
        balanceContent.setOpaque(false);
        balanceContent.setLayout(new BoxLayout(balanceContent, BoxLayout.Y_AXIS));
        
        JLabel balanceTitleLabel = new JLabel("SỐ DƯ TÀI KHOẢN");
        balanceTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        balanceTitleLabel.setForeground(Color.WHITE);
        balanceTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        balanceContent.add(Box.createVerticalStrut(20));
        balanceContent.add(balanceTitleLabel);
        balanceContent.add(Box.createVerticalStrut(10));
        
        JLabel balanceLabel = new JLabel("0 VNĐ");
        balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        balanceLabel.setForeground(Color.WHITE);
        balanceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        balanceContent.add(balanceLabel);
        
        balancePanel.add(balanceContent, BorderLayout.CENTER);
        
        panel.add(balancePanel, BorderLayout.NORTH);
        
        // Transaction panel with two cards side by side
        JPanel transPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        transPanel.setBackground(new Color(248, 249, 250));
        
        // Left card - Add balance
        JPanel addBalanceCard = createAddBalanceCard(balanceLabel);
        
        // Right card - Payment service with list
        JPanel paymentCard = createPaymentServiceCard(balanceLabel);
        
        transPanel.add(addBalanceCard);
        transPanel.add(paymentCard);
        
        panel.add(transPanel, BorderLayout.CENTER);
        
        // Load balance button at bottom
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(new Color(248, 249, 250));
        JButton loadBalanceBtn = createModernButton("Tải lại số dư", new Color(52, 152, 219), 14);
        loadBalanceBtn.setPreferredSize(new Dimension(180, 45));
        loadBalanceBtn.addActionListener(e -> {
            try {
                short balance = cardComm.getBalance();
                balanceLabel.setText(String.format("%,d VNĐ", balance * 1000));
                log("Số dư: " + balance + " nghìn VNĐ");
            } catch (Exception ex) {
                log("LỖI: " + ex.getMessage());
            }
        });
        bottomPanel.add(loadBalanceBtn);
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Create add balance card
     */
    private JPanel createAddBalanceCard(JLabel balanceLabel) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(46, 204, 113), 2),
            new EmptyBorder(15, 20, 15, 20)));
        
        // Title
        JLabel titleLabel = new JLabel("Nạp tiền vào tài khoản");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(46, 204, 113));
        card.add(titleLabel, BorderLayout.NORTH);
        
        // Content
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel amountLabel = new JLabel("Số tiền (nghìn VNĐ):");
        amountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        contentPanel.add(amountLabel, gbc);
        
        gbc.gridy = 1;
        JSpinner amountSpinner = new JSpinner(new SpinnerNumberModel(100, 10, 10000, 10));
        amountSpinner.setFont(new Font("Segoe UI", Font.BOLD, 16));
        ((JSpinner.DefaultEditor) amountSpinner.getEditor()).getTextField().setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 2),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        contentPanel.add(amountSpinner, gbc);
        
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 12, 12, 12);
        JButton addBtn = createModernButton("Nạp tiền", new Color(46, 204, 113), 16);
        addBtn.setPreferredSize(new Dimension(200, 25));
        addBtn.addActionListener(e -> {
            try {
                short amount = ((Number) amountSpinner.getValue()).shortValue();
                
                if (cardComm.addBalance(amount)) {
                    log("Nạp tiền thành công: " + amount + " nghìn VNĐ");
                    short newBalance = cardComm.getBalance();
                    balanceLabel.setText(String.format("%,d VNĐ", newBalance * 1000));
                    JOptionPane.showMessageDialog(card, 
                        "Nạp tiền thành công!\nSố dư mới: " + (newBalance * 1000) + " VNĐ",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    log("Nạp tiền thất bại");
                }
                
            } catch (Exception ex) {
                log("LỖI: " + ex.getMessage());
                JOptionPane.showMessageDialog(card, 
                    "Lỗi nạp tiền: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        contentPanel.add(addBtn, gbc);
        
        card.add(contentPanel, BorderLayout.CENTER);
        
        return card;
    }
    
    /**
     * Create payment service card with service list
     */
    private JPanel createPaymentServiceCard(JLabel balanceLabel) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(231, 76, 60), 2),
            new EmptyBorder(15, 20, 15, 20)));
        
        // Title
        JLabel titleLabel = new JLabel("Thanh toán dịch vụ");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(231, 76, 60));
        card.add(titleLabel, BorderLayout.NORTH);
        
        // Service list panel
        JPanel serviceListPanel = new JPanel();
        serviceListPanel.setLayout(new BoxLayout(serviceListPanel, BoxLayout.Y_AXIS));
        serviceListPanel.setBackground(Color.WHITE);
        
        // Define services with prices
        String[][] services = {
            {"HLV riêng", "200"},
            {"Nước uống", "20"},
            {"Khăn tập", "10"},
            {"Protein shake", "50"},
            {"Dinh dưỡng", "100"}
        };
        
        JComboBox<String> serviceCombo = new JComboBox<>();
        for (String[] service : services) {
            serviceCombo.addItem(service[0] + " - " + service[1] + "k");
        }
        serviceCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        serviceCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        serviceCombo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        
        serviceListPanel.add(Box.createVerticalStrut(10));
        
        // Service selection
        JPanel servicePanel = new JPanel(new BorderLayout(10, 5));
        servicePanel.setBackground(Color.WHITE);
        servicePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        JLabel serviceLabel = new JLabel("Chọn dịch vụ:");
        serviceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        servicePanel.add(serviceLabel, BorderLayout.NORTH);
        servicePanel.add(serviceCombo, BorderLayout.CENTER);
        serviceListPanel.add(servicePanel);
        
        serviceListPanel.add(Box.createVerticalStrut(10));
        
        // Quantity
        JPanel quantityPanel = new JPanel(new BorderLayout(10, 5));
        quantityPanel.setBackground(Color.WHITE);
        quantityPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        JLabel quantityLabel = new JLabel("Số lượng:");
        quantityLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
        quantitySpinner.setFont(new Font("Segoe UI", Font.BOLD, 16));
        quantitySpinner.setPreferredSize(new Dimension(0, 40));
        ((JSpinner.DefaultEditor) quantitySpinner.getEditor()).getTextField().setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 2),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        quantityPanel.add(quantityLabel, BorderLayout.NORTH);
        quantityPanel.add(quantitySpinner, BorderLayout.CENTER);
        serviceListPanel.add(quantityPanel);
        
        serviceListPanel.add(Box.createVerticalStrut(15));
        
        // Total amount display
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        totalPanel.setBackground(new Color(255, 250, 240));
        totalPanel.setBorder(BorderFactory.createLineBorder(new Color(243, 156, 18), 2));
        totalPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        JLabel totalLabel = new JLabel("Tổng tiền: 200k VNĐ");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalLabel.setForeground(new Color(230, 126, 34));
        totalPanel.add(totalLabel);
        serviceListPanel.add(totalPanel);
        
        // Update total when service or quantity changes
        Runnable updateTotal = () -> {
            int selectedIndex = serviceCombo.getSelectedIndex();
            if (selectedIndex >= 0) {
                int price = Integer.parseInt(services[selectedIndex][1]);
                int quantity = (Integer) quantitySpinner.getValue();
                int total = price * quantity;
                totalLabel.setText(String.format("Tổng tiền: %dk VNĐ", total));
            }
        };
        
        serviceCombo.addActionListener(e -> updateTotal.run());
        quantitySpinner.addChangeListener(e -> updateTotal.run());
        
        card.add(serviceListPanel, BorderLayout.CENTER);
        
        // Payment button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);
        JButton payBtn = createModernButton("Thanh toán", new Color(231, 76, 60), 16);
        payBtn.setPreferredSize(new Dimension(200, 25));
        payBtn.addActionListener(e -> {
            try {
                int selectedIndex = serviceCombo.getSelectedIndex();
                int price = Integer.parseInt(services[selectedIndex][1]);
                int quantity = (Integer) quantitySpinner.getValue();
                short totalAmount = (short) (price * quantity);
                
                if (cardComm.deductBalance(totalAmount)) {
                    log("Thanh toán thành công: " + totalAmount + " nghìn VNĐ");
                    short newBalance = cardComm.getBalance();
                    balanceLabel.setText(String.format("%,d VNĐ", newBalance * 1000));
                    JOptionPane.showMessageDialog(card, 
                        "Thanh toán thành công!\n" +
                        "Dịch vụ: " + services[selectedIndex][0] + "\n" +
                        "Số lượng: " + quantity + "\n" +
                        "Tổng tiền: " + (totalAmount * 1000) + " VNĐ\n" +
                        "Số dư còn lại: " + (newBalance * 1000) + " VNĐ",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    quantitySpinner.setValue(1);
                } else {
                    log("Thanh toán thất bại - Không đủ số dư");
                    JOptionPane.showMessageDialog(card, 
                        "Thanh toán thất bại!\nSố dư không đủ.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (Exception ex) {
                log("LỖI: " + ex.getMessage());
                JOptionPane.showMessageDialog(card, 
                    "Lỗi thanh toán: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(payBtn);
        
        card.add(buttonPanel, BorderLayout.SOUTH);
        
        return card;
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
        logArea.setText("");
        log("Đã reset về màn hình đăng nhập");
    }
    
    /**
     * Log message
     */
    private void log(String message) {
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        logArea.append(String.format("[%s] %s\n", timestamp, message));
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
