package gymcard.client.ui;

import gymcard.client.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Admin Panel - Quản lý hệ thống
 */
public class AdminPanel extends JPanel {
    
    private CardCommunicator cardComm;
    private JTextArea logArea;
    
    // Member registration components
    private JTextField nameField;
    private JTextField birthDateField;
    private JTextField phoneField;
    private JTextArea addressArea;
    private JComboBox<String> packageTypeBox;
    private JTextField expiryDateField;
    private JSpinner sessionSpinner;
    
    public AdminPanel(CardCommunicator cardComm) {
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
                GradientPaint gp = new GradientPaint(0, 0, new Color(52, 152, 219), w, 0, new Color(155, 89, 182));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("👥 QUẢN TRỊ HỆ THỐNG GYM CARD");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content with tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.setBackground(Color.WHITE);
        
        tabbedPane.addTab("Đăng ký hội viên", createRegistrationPanel());
        tabbedPane.addTab("Quản lý thẻ", createCardManagementPanel());
        tabbedPane.addTab("Quản lý gói tập", createPackageManagementPanel());
        tabbedPane.addTab("Mở khóa thẻ", createUnlockPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Log area
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBackground(Color.WHITE);
        logPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(149, 165, 166), 2), 
            "Nhật ký hệ thống",
            TitledBorder.LEFT, 
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13)
        ));
        
        logArea = new JTextArea(6, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(250, 250, 250));
        JScrollPane logScroll = new JScrollPane(logArea);
        logPanel.add(logScroll, BorderLayout.CENTER);
        
        add(logPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Panel đăng ký hội viên mới
     */
    private JPanel createRegistrationPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Họ tên
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel nameLabel = new JLabel("Họ và tên:");
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(nameLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        nameField = new JTextField(30);
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        formPanel.add(nameField, gbc);
        
        // Ngày sinh
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel birthLabel = new JLabel("Ngày sinh (YYYYMMDD):");
        birthLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(birthLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        birthDateField = new JTextField(30);
        birthDateField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        birthDateField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        birthDateField.setText(new SimpleDateFormat("yyyyMMdd").format(new Date()));
        formPanel.add(birthDateField, gbc);
        
        // Số điện thoại
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel phoneLabel = new JLabel("Số điện thoại:");
        phoneLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(phoneLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        phoneField = new JTextField(30);
        phoneField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        phoneField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        formPanel.add(phoneField, gbc);
        
        // Địa chỉ
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        JLabel addressLabel = new JLabel("Địa chỉ:");
        addressLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(addressLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        addressArea = new JTextArea(3, 30);
        addressArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        addressArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        JScrollPane addressScroll = new JScrollPane(addressArea);
        addressScroll.setBorder(null);
        formPanel.add(addressScroll, gbc);
        
        // Loại gói
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel packageLabel = new JLabel("Loại gói tập:");
        packageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(packageLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        packageTypeBox = new JComboBox<>(new String[]{
            "Gói Tháng", "Gói Theo Buổi", "Gói VIP"
        });
        packageTypeBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(packageTypeBox, gbc);
        
        // Ngày hết hạn
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel expiryLabel = new JLabel("Ngày hết hạn (YYYYMMDD):");
        expiryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(expiryLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        expiryDateField = new JTextField(30);
        expiryDateField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        expiryDateField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        formPanel.add(expiryDateField, gbc);
        
        // Số buổi (cho gói theo buổi)
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel sessionLabel = new JLabel("Số buổi tập:");
        sessionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(sessionLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        sessionSpinner = new JSpinner(new SpinnerNumberModel(20, 0, 200, 1));
        sessionSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(sessionSpinner, gbc);
        
        panel.add(formPanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton registerBtn = createModernButton("Đăng ký hội viên mới", new Color(46, 204, 113), 16);
        registerBtn.setPreferredSize(new Dimension(220, 50));
        registerBtn.addActionListener(e -> registerMember());
        
        JButton clearBtn = createModernButton("Xóa form", new Color(149, 165, 166), 14);
        clearBtn.setPreferredSize(new Dimension(120, 50));
        clearBtn.addActionListener(e -> clearForm());
        
        buttonPanel.add(registerBtn);
        buttonPanel.add(clearBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Panel quản lý thẻ
     */
    private JPanel createCardManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Info display panel
        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        infoPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 2), 
            "Thông tin thẻ hiện tại",
            TitledBorder.LEFT, 
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        
        JTextArea infoArea = new JTextArea(15, 50);
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane infoScroll = new JScrollPane(infoArea);
        infoPanel.add(infoScroll);
        
        panel.add(infoPanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton loadBtn = createModernButton("Tải thông tin thẻ", new Color(52, 152, 219), 14);
        loadBtn.setPreferredSize(new Dimension(180, 45));
        loadBtn.addActionListener(e -> {
            try {
                if (!cardComm.isConnected()) {
                    log("Vui lòng kết nối thẻ trước!");
                    return;
                }
                
                // Get member info
                MemberInfo member = cardComm.getMemberInfo();
                PackageInfo pkg = cardComm.getPackage();
                CheckInInfo checkIn = cardComm.getLastCheckIn();
                short balance = cardComm.getBalance();
                int checkInCount = cardComm.getCheckInCount();
                
                StringBuilder sb = new StringBuilder();
                sb.append("═══════════════════════════════════════════════════\n");
                sb.append("           THÔNG TIN HỘI VIÊN GYM CARD\n");
                sb.append("═══════════════════════════════════════════════════\n\n");
                
                sb.append("THÔNG TIN CÁ NHÂN:\n");
                sb.append("  • Họ và tên: ").append(member.name).append("\n");
                sb.append("  • Ngày sinh: ").append(member.birthDate).append("\n");
                sb.append("  • Điện thoại: ").append(member.phone).append("\n");
                sb.append("  • Địa chỉ: ").append(member.address).append("\n\n");
                
                sb.append("THÔNG TIN GÓI TẬP:\n");
                sb.append("  • Loại gói: ").append(pkg.getPackageTypeName()).append("\n");
                sb.append("  • Ngày đăng ký: ").append(pkg.registration).append("\n");
                sb.append("  • Ngày hết hạn: ").append(pkg.expiry).append("\n");
                if (pkg.type == 2) {
                    sb.append("  • Số buổi còn lại: ").append(pkg.remainingSessions).append("\n");
                }
                sb.append("\n");
                
                sb.append("LỊCH SỬ CHECK-IN:\n");
                sb.append("  • Ngày tập gần nhất: ").append(checkIn.date).append("\n");
                sb.append("  • Giờ vào: ").append(checkIn.checkInTime).append("\n");
                sb.append("  • Giờ ra: ").append(checkIn.checkOutTime).append("\n");
                sb.append("  • Số ngày tập tháng này: ").append(checkInCount).append("\n\n");
                
                sb.append("SỐ DỮ TÀI KHOẢN:\n");
                sb.append("  • Số dư: ").append(balance).append(" nghìn VNĐ\n");
                sb.append("\n═══════════════════════════════════════════════════\n");
                
                infoArea.setText(sb.toString());
                log("Đã tải thông tin thẻ thành công");
                
            } catch (Exception ex) {
                log("LỖI: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, 
                    "Không thể đọc thông tin thẻ: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JButton viewTransBtn = createModernButton("Xem lịch sử giao dịch", new Color(155, 89, 182), 14);
        viewTransBtn.setPreferredSize(new Dimension(200, 45));
        viewTransBtn.addActionListener(e -> viewTransactions());
        
        buttonPanel.add(loadBtn);
        buttonPanel.add(viewTransBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Panel quản lý gói tập
     */
    private JPanel createPackageManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Nâng cấp/Chuyển đổi gói tập:"), gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Loại gói mới:"), gbc);
        
        gbc.gridx = 1;
        JComboBox<String> newPackageBox = new JComboBox<>(new String[]{
            "Gói Tháng", "Gói Theo Buổi", "Gói VIP"
        });
        newPackageBox.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(newPackageBox, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Ngày hết hạn mới:"), gbc);
        
        gbc.gridx = 1;
        JTextField newExpiryField = new JTextField(20);
        newExpiryField.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(newExpiryField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Số buổi:"), gbc);
        
        gbc.gridx = 1;
        JSpinner newSessionSpinner = new JSpinner(new SpinnerNumberModel(30, 0, 200, 1));
        newSessionSpinner.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(newSessionSpinner, gbc);
        
        panel.add(formPanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        JButton upgradeBtn = createModernButton("Nâng cấp gói", new Color(230, 126, 34), 14);
        upgradeBtn.setPreferredSize(new Dimension(180, 45));
        upgradeBtn.addActionListener(e -> {
            try {
                if (!cardComm.isConnected()) {
                    log("Vui lòng kết nối thẻ trước!");
                    return;
                }
                
                byte packageType = (byte) (newPackageBox.getSelectedIndex() + 1);
                String expiry = newExpiryField.getText();
                short sessions = ((Number) newSessionSpinner.getValue()).shortValue();
                
                if (cardComm.setPackage(packageType, expiry, 
                        new SimpleDateFormat("yyyyMMdd").format(new Date()), sessions)) {
                    log("Đã nâng cấp gói tập thành công");
                    JOptionPane.showMessageDialog(this, 
                        "Nâng cấp gói tập thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    log("Nâng cấp gói tập thất bại");
                }
                
            } catch (Exception ex) {
                log("LỖI: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, 
                    "Lỗi nâng cấp gói: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        buttonPanel.add(upgradeBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Panel mở khóa thẻ
     */
    private JPanel createUnlockPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel warningLabel = new JLabel("⚠ MỞ KHÓA THẺ BỊ KHÓA");
        warningLabel.setFont(new Font("Arial", Font.BOLD, 20));
        warningLabel.setForeground(new Color(231, 76, 60));
        warningLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel infoLabel = new JLabel("<html><center>Chức năng này dùng để mở khóa thẻ<br>" +
                                      "khi hội viên nhập sai PIN quá 3 lần.<br><br>" +
                                      "Cần có mã PIN quản trị viên.</center></html>");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        infoPanel.add(warningLabel);
        infoPanel.add(Box.createVerticalStrut(20));
        infoPanel.add(infoLabel);
        
        panel.add(infoPanel, BorderLayout.CENTER);
        
        // Form panel
        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        JLabel pinLabel = new JLabel("Mã PIN Admin:");
        pinLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        JPasswordField adminPinField = new JPasswordField(10);
        adminPinField.setFont(new Font("Arial", Font.PLAIN, 16));
        
        JButton unlockBtn = createModernButton("Mở khóa thẻ", new Color(231, 76, 60), 14);
        unlockBtn.setPreferredSize(new Dimension(150, 45));
        unlockBtn.addActionListener(e -> {
            try {
                if (!cardComm.isConnected()) {
                    log("Vui lòng kết nối thẻ trước!");
                    return;
                }
                
                String adminPin = new String(adminPinField.getPassword());
                
                if (adminPin.length() != 4) {
                    JOptionPane.showMessageDialog(this, 
                        "Mã PIN phải có 4 chữ số!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (cardComm.unlockPin(adminPin)) {
                    log("Đã mở khóa thẻ thành công");
                    JOptionPane.showMessageDialog(this, 
                        "Mở khóa thẻ thành công!\nThẻ đã được khôi phục.",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    adminPinField.setText("");
                } else {
                    log("Mở khóa thất bại - Sai mã PIN Admin");
                    JOptionPane.showMessageDialog(this, 
                        "Mã PIN Admin không đúng!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (Exception ex) {
                log("LỖI: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, 
                    "Lỗi mở khóa: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        formPanel.add(pinLabel);
        formPanel.add(adminPinField);
        formPanel.add(unlockBtn);
        
        panel.add(formPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Đăng ký hội viên mới
     */
    private void registerMember() {
        try {
            if (!cardComm.isConnected()) {
                log("Vui lòng kết nối thẻ trước!");
                JOptionPane.showMessageDialog(this, 
                    "Vui lòng kết nối thẻ trước khi đăng ký!",
                    "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Validate inputs
            String name = nameField.getText().trim();
            String birthDate = birthDateField.getText().trim();
            String phone = phoneField.getText().trim();
            String address = addressArea.getText().trim();
            
            if (name.isEmpty() || birthDate.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Vui lòng điền đầy đủ thông tin bắt buộc!",
                    "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Set member info
            log("Đang lưu thông tin hội viên...");
            if (!cardComm.setMemberInfo(name, birthDate, phone, address)) {
                log("Lưu thông tin hội viên thất bại");
                return;
            }
            
            // Set package
            byte packageType = (byte) (packageTypeBox.getSelectedIndex() + 1);
            String expiry = expiryDateField.getText().trim();
            String registration = new SimpleDateFormat("yyyyMMdd").format(new Date());
            short sessions = ((Number) sessionSpinner.getValue()).shortValue();
            
            log("Đang thiết lập gói tập...");
            if (!cardComm.setPackage(packageType, expiry, registration, sessions)) {
                log("Thiết lập gói tập thất bại");
                return;
            }
            
            log("Đăng ký hội viên thành công!");
            JOptionPane.showMessageDialog(this, 
                "Đăng ký hội viên mới thành công!\n" +
                "Hội viên: " + name + "\n" +
                "Gói: " + packageTypeBox.getSelectedItem(),
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
            
            clearForm();
            
        } catch (Exception ex) {
            log("LỖI: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Lỗi đăng ký: " + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Xem lịch sử giao dịch
     */
    private void viewTransactions() {
        try {
            if (!cardComm.isConnected()) {
                log("Vui lòng kết nối thẻ trước!");
                return;
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("LỊCH SỬ GIAO DỊCH\n");
            sb.append("═══════════════════════════════════════════════\n\n");
            
            for (byte i = 0; i < 10; i++) {
                TransactionInfo trans = cardComm.getTransaction(i);
                if (trans == null) break;
                
                sb.append(String.format("Giao dịch #%d:\n", i + 1));
                sb.append(String.format("  Ngày: %s - %s\n", trans.date, trans.time));
                sb.append(String.format("  Loại: %s\n", trans.getTypeName()));
                sb.append(String.format("  Số tiền: %d nghìn VNĐ\n", trans.amount));
                sb.append("\n");
            }
            
            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 400));
            
            JOptionPane.showMessageDialog(this, scrollPane, 
                "Lịch sử giao dịch", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception ex) {
            log("LỖI: " + ex.getMessage());
        }
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
     * Clear form
     */
    private void clearForm() {
        nameField.setText("");
        birthDateField.setText(new SimpleDateFormat("yyyyMMdd").format(new Date()));
        phoneField.setText("");
        addressArea.setText("");
        packageTypeBox.setSelectedIndex(0);
        expiryDateField.setText("");
        sessionSpinner.setValue(20);
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
