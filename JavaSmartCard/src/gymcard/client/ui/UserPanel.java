package gymcard.client.ui;

import gymcard.client.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * User Panel - Dành cho hội viên
 */
public class UserPanel extends JPanel {
    
    private CardCommunicator cardComm;
    private JTextArea logArea;
    private boolean isAuthenticated = false;
    
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
        JLabel titleLabel = new JLabel("💪 HỘI VIÊN GYM CARD");
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
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(iconLabel, gbc);
        
        // Title
        JLabel titleLabel = new JLabel("Vui lòng nhập mã PIN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        gbc.gridy = 1;
        panel.add(titleLabel, gbc);
        
        // PIN field
        JPasswordField pinField = new JPasswordField(10);
        pinField.setFont(new Font("Segoe UI", Font.BOLD, 28));
        pinField.setHorizontalAlignment(JTextField.CENTER);
        pinField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        gbc.gridy = 2;
        panel.add(pinField, gbc);
        
        // Tries remaining label
        JLabel triesLabel = new JLabel("");
        triesLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        triesLabel.setForeground(Color.RED);
        gbc.gridy = 3;
        panel.add(triesLabel, gbc);
        
        // Login button
        JButton loginBtn = createModernButton("Đăng nhập", new Color(46, 204, 113), 16);
        loginBtn.setPreferredSize(new Dimension(180, 50));
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        panel.add(loginBtn, gbc);
        
        // Check tries button
        JButton checkTriesBtn = new JButton("Kiểm tra số lần còn lại");
        checkTriesBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
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
                    isAuthenticated = true;
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
                    triesLabel.setText("⚠ Thẻ đã bị khóa!");
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
        
        // Tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.PLAIN, 14));
        
        tabbedPane.addTab("Thông tin cá nhân", createInfoTab());
        tabbedPane.addTab("Check-in/Check-out", createCheckInTab());
        tabbedPane.addTab("Thay đổi PIN", createChangePinTab());
        tabbedPane.addTab("Thanh toán", createPaymentTab());
        
        panel.add(tabbedPane, BorderLayout.CENTER);
        
        // Logout button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton logoutBtn = new JButton("Đăng xuất");
        logoutBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        logoutBtn.addActionListener(e -> {
            isAuthenticated = false;
            cardLayout.show(contentPanel, "login");
            log("Đã đăng xuất");
        });
        bottomPanel.add(logoutBtn);
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Tab thông tin cá nhân
     */
    private JPanel createInfoTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JTextArea infoArea = new JTextArea(20, 50);
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane scrollPane = new JScrollPane(infoArea);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Load button
        JButton loadBtn = new JButton("Tải thông tin");
        loadBtn.setFont(new Font("Arial", Font.BOLD, 14));
        loadBtn.setBackground(new Color(52, 152, 219));
        loadBtn.setForeground(Color.WHITE);
        loadBtn.setFocusPainted(false);
        loadBtn.addActionListener(e -> {
            try {
                MemberInfo member = cardComm.getMemberInfo();
                PackageInfo pkg = cardComm.getPackage();
                CheckInInfo checkIn = cardComm.getLastCheckIn();
                int checkInCount = cardComm.getCheckInCount();
                
                StringBuilder sb = new StringBuilder();
                sb.append("╔═══════════════════════════════════════════════╗\n");
                sb.append("║         THÔNG TIN HỘI VIÊN GYM CARD          ║\n");
                sb.append("╚═══════════════════════════════════════════════╝\n\n");
                
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                sb.append("  THÔNG TIN CÁ NHÂN\n");
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
                sb.append(String.format("  Họ và tên      : %s\n", member.name));
                sb.append(String.format("  Ngày sinh      : %s\n", member.birthDate));
                sb.append(String.format("  Số điện thoại  : %s\n", member.phone));
                sb.append(String.format("  Địa chỉ        : %s\n\n", member.address));
                
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                sb.append("  THÔNG TIN GÓI TẬP\n");
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
                sb.append(String.format("  Loại gói       : %s\n", pkg.getPackageTypeName()));
                sb.append(String.format("  Ngày đăng ký   : %s\n", pkg.registration));
                sb.append(String.format("  Ngày hết hạn   : %s\n", pkg.expiry));
                if (pkg.type == 2) {
                    sb.append(String.format("  Buổi còn lại   : %d\n", pkg.remainingSessions));
                }
                sb.append("\n");
                
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                sb.append("  THỐNG KÊ CHECK-IN\n");
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
                sb.append(String.format("  Lần tập gần nhất : %s\n", checkIn.date));
                sb.append(String.format("  Giờ vào          : %s\n", checkIn.checkInTime));
                sb.append(String.format("  Giờ ra           : %s\n", checkIn.checkOutTime));
                sb.append(String.format("  Số ngày tập tháng này: %d ngày\n", checkInCount));
                
                infoArea.setText(sb.toString());
                log("Đã tải thông tin cá nhân");
                
            } catch (Exception ex) {
                log("LỖI: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, 
                    "Lỗi tải thông tin: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(loadBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Tab check-in/check-out
     */
    private JPanel createCheckInTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Status area
        JTextArea statusArea = new JTextArea(10, 50);
        statusArea.setEditable(false);
        statusArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(statusArea);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        JButton checkInBtn = createModernButton("✓ CHECK-IN", new Color(46, 204, 113), 18);
        checkInBtn.setPreferredSize(new Dimension(220, 70));
        checkInBtn.addActionListener(e -> {
            try {
                String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
                String time = new SimpleDateFormat("HHmmss").format(new Date());
                
                if (cardComm.checkIn(date, time)) {
                    log("Check-in thành công!");
                    
                    CheckInInfo info = cardComm.getLastCheckIn();
                    int count = cardComm.getCheckInCount();
                    
                    StringBuilder sb = new StringBuilder();
                    sb.append("✓ CHECK-IN THÀNH CÔNG!\n\n");
                    sb.append(String.format("Ngày: %s\n", info.date));
                    sb.append(String.format("Giờ vào: %s\n", info.checkInTime));
                    sb.append(String.format("\nSố ngày tập tháng này: %d\n", count));
                    sb.append("\nChúc bạn buổi tập tốt! 💪");
                    
                    statusArea.setText(sb.toString());
                    
                    JOptionPane.showMessageDialog(this, 
                        "Check-in thành công!\nChúc bạn buổi tập tốt!",
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
        checkOutBtn.setPreferredSize(new Dimension(220, 70));
        checkOutBtn.addActionListener(e -> {
            try {
                String time = new SimpleDateFormat("HHmmss").format(new Date());
                
                if (cardComm.checkOut(time)) {
                    log("Check-out thành công!");
                    
                    CheckInInfo info = cardComm.getLastCheckIn();
                    
                    StringBuilder sb = new StringBuilder();
                    sb.append("✓ CHECK-OUT THÀNH CÔNG!\n\n");
                    sb.append(String.format("Ngày: %s\n", info.date));
                    sb.append(String.format("Giờ vào: %s\n", info.checkInTime));
                    sb.append(String.format("Giờ ra: %s\n", info.checkOutTime));
                    sb.append("\nHẹn gặp lại! 👋");
                    
                    statusArea.setText(sb.toString());
                    
                    JOptionPane.showMessageDialog(this, 
                        "Check-out thành công!\nHẹn gặp lại!",
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
        
        JButton viewHistoryBtn = new JButton("Xem lịch sử");
        viewHistoryBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        viewHistoryBtn.addActionListener(e -> {
            try {
                CheckInInfo info = cardComm.getLastCheckIn();
                int count = cardComm.getCheckInCount();
                
                StringBuilder sb = new StringBuilder();
                sb.append("LỊCH SỬ CHECK-IN\n\n");
                sb.append(String.format("Lần tập gần nhất: %s\n", info.date));
                sb.append(String.format("Giờ vào: %s\n", info.checkInTime));
                sb.append(String.format("Giờ ra: %s\n", info.checkOutTime));
                sb.append(String.format("\nTổng số ngày tập tháng này: %d\n", count));
                
                statusArea.setText(sb.toString());
                log("Đã tải lịch sử check-in");
                
            } catch (Exception ex) {
                log("LỖI: " + ex.getMessage());
            }
        });
        
        buttonPanel.add(checkInBtn);
        buttonPanel.add(checkOutBtn);
        buttonPanel.add(viewHistoryBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Tab thay đổi PIN
     */
    private JPanel createChangePinTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Info label
        JLabel infoLabel = new JLabel("🔒 Thay đổi mã PIN của bạn");
        infoLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(infoLabel, gbc);
        
        gbc.gridwidth = 1;
        
        // Old PIN
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel oldPinLabel = new JLabel("Mã PIN hiện tại:");
        oldPinLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(oldPinLabel, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JPasswordField oldPinField = new JPasswordField(15);
        oldPinField.setFont(new Font("Arial", Font.PLAIN, 16));
        panel.add(oldPinField, gbc);
        
        // New PIN
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel newPinLabel = new JLabel("Mã PIN mới:");
        newPinLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(newPinLabel, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JPasswordField newPinField = new JPasswordField(15);
        newPinField.setFont(new Font("Arial", Font.PLAIN, 16));
        panel.add(newPinField, gbc);
        
        // Confirm PIN
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel confirmPinLabel = new JLabel("Xác nhận PIN mới:");
        confirmPinLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(confirmPinLabel, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JPasswordField confirmPinField = new JPasswordField(15);
        confirmPinField.setFont(new Font("Arial", Font.PLAIN, 16));
        panel.add(confirmPinField, gbc);
        
        // Change button
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton changeBtn = createModernButton("Thay đổi PIN", new Color(52, 152, 219), 16);
        changeBtn.setPreferredSize(new Dimension(200, 50));
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
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Balance display
        JPanel balancePanel = new JPanel();
        balancePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(241, 196, 15), 3), 
            "Số dư tài khoản",
            TitledBorder.CENTER, 
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 16)
        ));
        balancePanel.setPreferredSize(new Dimension(0, 100));
        
        JLabel balanceLabel = new JLabel("0 VNĐ");
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 36));
        balanceLabel.setForeground(new Color(241, 196, 15));
        balancePanel.add(balanceLabel);
        
        panel.add(balancePanel, BorderLayout.NORTH);
        
        // Transaction panel
        JPanel transPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Add balance section
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel addLabel = new JLabel("💳 Nạp tiền vào tài khoản");
        addLabel.setFont(new Font("Arial", Font.BOLD, 16));
        transPanel.add(addLabel, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel amountLabel1 = new JLabel("Số tiền (nghìn VNĐ):");
        transPanel.add(amountLabel1, gbc);
        
        gbc.gridx = 1;
        JSpinner addAmountSpinner = new JSpinner(new SpinnerNumberModel(100, 10, 10000, 10));
        addAmountSpinner.setFont(new Font("Arial", Font.PLAIN, 14));
        transPanel.add(addAmountSpinner, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        JButton addBtn = createModernButton("💳 Nạp tiền", new Color(46, 204, 113), 14);
        addBtn.setPreferredSize(new Dimension(150, 45));
        addBtn.addActionListener(e -> {
            try {
                short amount = ((Number) addAmountSpinner.getValue()).shortValue();
                
                if (cardComm.addBalance(amount)) {
                    log("Nạp tiền thành công: " + amount + " nghìn VNĐ");
                    short newBalance = cardComm.getBalance();
                    balanceLabel.setText(String.format("%,d VNĐ", newBalance * 1000));
                    JOptionPane.showMessageDialog(this, 
                        "Nạp tiền thành công!\nSố dư mới: " + (newBalance * 1000) + " VNĐ",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    log("Nạp tiền thất bại");
                }
                
            } catch (Exception ex) {
                log("LỖI: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, 
                    "Lỗi nạp tiền: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        transPanel.add(addBtn, gbc);
        
        // Separator
        gbc.gridy = 3;
        transPanel.add(new JSeparator(), gbc);
        
        // Deduct balance section
        gbc.gridy = 4;
        JLabel deductLabel = new JLabel("💰 Thanh toán dịch vụ");
        deductLabel.setFont(new Font("Arial", Font.BOLD, 16));
        transPanel.add(deductLabel, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 5;
        JLabel serviceLabel = new JLabel("Dịch vụ:");
        transPanel.add(serviceLabel, gbc);
        
        gbc.gridx = 1;
        JComboBox<String> serviceBox = new JComboBox<>(new String[]{
            "HLV riêng (200k)", "Nước uống (20k)", "Khăn tập (10k)", "Khác"
        });
        serviceBox.setFont(new Font("Arial", Font.PLAIN, 14));
        transPanel.add(serviceBox, gbc);
        
        gbc.gridx = 0; gbc.gridy = 6;
        JLabel amountLabel2 = new JLabel("Số tiền (nghìn VNĐ):");
        transPanel.add(amountLabel2, gbc);
        
        gbc.gridx = 1;
        JSpinner deductAmountSpinner = new JSpinner(new SpinnerNumberModel(20, 1, 1000, 1));
        deductAmountSpinner.setFont(new Font("Arial", Font.PLAIN, 14));
        transPanel.add(deductAmountSpinner, gbc);
        
        gbc.gridx = 0; gbc.gridy = 7;
        gbc.gridwidth = 2;
        JButton payBtn = createModernButton("💰 Thanh toán", new Color(231, 76, 60), 14);
        payBtn.setPreferredSize(new Dimension(150, 45));
        payBtn.addActionListener(e -> {
            try {
                short amount = ((Number) deductAmountSpinner.getValue()).shortValue();
                
                if (cardComm.deductBalance(amount)) {
                    log("Thanh toán thành công: " + amount + " nghìn VNĐ");
                    short newBalance = cardComm.getBalance();
                    balanceLabel.setText(String.format("%,d VNĐ", newBalance * 1000));
                    JOptionPane.showMessageDialog(this, 
                        "Thanh toán thành công!\n" +
                        "Dịch vụ: " + serviceBox.getSelectedItem() + "\n" +
                        "Số dư còn lại: " + (newBalance * 1000) + " VNĐ",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    log("Thanh toán thất bại - Không đủ số dư");
                    JOptionPane.showMessageDialog(this, 
                        "Thanh toán thất bại!\nSố dư không đủ.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (Exception ex) {
                log("LỖI: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, 
                    "Lỗi thanh toán: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        transPanel.add(payBtn, gbc);
        
        panel.add(transPanel, BorderLayout.CENTER);
        
        // Load balance button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton loadBalanceBtn = new JButton("Tải lại số dư");
        loadBalanceBtn.setFont(new Font("Arial", Font.PLAIN, 14));
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
     * Log message
     */
    private void log(String message) {
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        logArea.append(String.format("[%s] %s\n", timestamp, message));
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
