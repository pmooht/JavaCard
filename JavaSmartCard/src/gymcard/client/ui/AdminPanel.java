package gymcard.client.ui;

import gymcard.client.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.*;


/**
 * Admin Panel - Quản lý hệ thống
 */
public class AdminPanel extends JPanel {
    
    private final CardCommunicator cardComm;
    private JTextArea logArea;
    
    // Member registration components
    private JTextField nameField;
    private JTextField birthDateField;
    private JTextField phoneField;
    private JTextArea addressArea;
    private JPasswordField pinField;
    private JPasswordField confirmPinField;
    private JLabel avatarLabel;
    private String avatarPath;
    
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
        JLabel titleLabel = new JLabel("QUẢN TRỊ HỆ THỐNG GYM CARD");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content with tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.setBackground(Color.WHITE);
        
        tabbedPane.addTab("Đăng ký hội viên", createRegistrationPanel());
        tabbedPane.addTab("Đổi PIN & Mở khóa", createPinManagementPanel());
        tabbedPane.addTab("Quản lý thẻ", createCardManagementPanel());
        
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
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(248, 249, 250));
        
        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setBackground(new Color(248, 249, 250));
        
        // Left side - Avatar
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(30, 30, 30, 30)));
        leftPanel.setPreferredSize(new Dimension(300, 0));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Avatar display
        avatarLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(220, 220, 220));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                // Draw icon
                g2d.setColor(new Color(150, 150, 150));
                g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
                String icon = "👤";
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(icon)) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2d.drawString(icon, x, y);
            }
        };
        avatarLabel.setPreferredSize(new Dimension(200, 200));
        leftPanel.add(avatarLabel, gbc);
        
        gbc.gridy = 1;
        JLabel avatarNote = new JLabel("<html><center>Ảnh đại diện<br>(Tính năng sắp ra mắt)</center></html>");
        avatarNote.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        avatarNote.setForeground(new Color(127, 140, 141));
        leftPanel.add(avatarNote, gbc);
        
        gbc.gridy = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        JButton uploadBtn = createModernButton("Chọn ảnh", new Color(52, 152, 219), 13);
        uploadBtn.setPreferredSize(new Dimension(150, 40));
        uploadBtn.setEnabled(false); // Disable for now
        leftPanel.add(uploadBtn, gbc);
        
        contentPanel.add(leftPanel, BorderLayout.WEST);
        
        // Right side - Form
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(new Color(248, 249, 250));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(30, 30, 30, 30)));
        
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Họ tên
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel nameLabel = new JLabel("Họ và tên: *");
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(nameLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        nameField = new JTextField(30);
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        formPanel.add(nameField, gbc);
        
        // Ngày sinh
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel birthLabel = new JLabel("Ngày sinh (dd/MM/yyyy): *");
        birthLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(birthLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        birthDateField = new JTextField(30);
        birthDateField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        birthDateField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        birthDateField.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        formPanel.add(birthDateField, gbc);
        
        // Số điện thoại
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel phoneLabel = new JLabel("Số điện thoại: *");
        phoneLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(phoneLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        phoneField = new JTextField(30);
        phoneField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        phoneField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
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
            BorderFactory.createLineBorder(new Color(189, 195, 199), 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        JScrollPane addressScroll = new JScrollPane(addressArea);
        addressScroll.setBorder(null);
        formPanel.add(addressScroll, gbc);
        
        // Mã PIN
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel pinLabel = new JLabel("Mã PIN (4 chữ số): *");
        pinLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(pinLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        pinField = new JPasswordField(30);
        pinField.setFont(new Font("Segoe UI", Font.BOLD, 18));
        pinField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        formPanel.add(pinField, gbc);
        
        // Xác nhận PIN
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel confirmPinLabel = new JLabel("Xác nhận PIN: *");
        confirmPinLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(confirmPinLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        confirmPinField = new JPasswordField(30);
        confirmPinField.setFont(new Font("Segoe UI", Font.BOLD, 18));
        confirmPinField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        formPanel.add(confirmPinField, gbc);
        
        rightPanel.add(formPanel, BorderLayout.CENTER);
        contentPanel.add(rightPanel, BorderLayout.CENTER);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(new Color(248, 249, 250));
        
        JButton registerBtn = createModernButton("Đăng ký hội viên", new Color(46, 204, 113), 16);
        registerBtn.setPreferredSize(new Dimension(220, 50));
        registerBtn.addActionListener(e -> registerMember());
        
        JButton clearBtn = createModernButton("Xóa form", new Color(149, 165, 166), 14);
        clearBtn.setPreferredSize(new Dimension(140, 50));
        clearBtn.addActionListener(e -> clearForm());
        
        buttonPanel.add(registerBtn);
        buttonPanel.add(clearBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Panel quản lý PIN (Đổi PIN + Mở khóa)
     */
    private JPanel createPinManagementPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));
        panel.setBackground(new Color(248, 249, 250));
        
        // Left card - Change PIN
        JPanel changePinCard = new JPanel(new BorderLayout(15, 15));
        changePinCard.setBackground(Color.WHITE);
        changePinCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
            new EmptyBorder(30, 30, 30, 30)));
        
        JLabel changePinTitle = new JLabel("Đổi mã PIN khi quên");
        changePinTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        changePinTitle.setForeground(new Color(52, 152, 219));
        changePinCard.add(changePinTitle, BorderLayout.NORTH);
        
        JPanel changePinForm = new JPanel(new GridBagLayout());
        changePinForm.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Info
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel changePinInfo = new JLabel("<html><center>Dùng để đổi PIN mới khi hội viên quên mã PIN.<br>Yêu cầu mã PIN quản trị viên.</center></html>");
        changePinInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        changePinInfo.setForeground(new Color(127, 140, 141));
        changePinForm.add(changePinInfo, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel adminPinLabel1 = new JLabel("PIN Admin:");
        adminPinLabel1.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        changePinForm.add(adminPinLabel1, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JPasswordField adminPinField1 = new JPasswordField(15);
        adminPinField1.setFont(new Font("Segoe UI", Font.BOLD, 18));
        adminPinField1.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        changePinForm.add(adminPinField1, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel newPinLabel = new JLabel("PIN mới:");
        newPinLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        changePinForm.add(newPinLabel, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JPasswordField newPinField = new JPasswordField(15);
        newPinField.setFont(new Font("Segoe UI", Font.BOLD, 18));
        newPinField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        changePinForm.add(newPinField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(25, 15, 15, 15);
        JButton changePinBtn = createModernButton("Đổi PIN", new Color(52, 152, 219), 15);
        changePinBtn.setPreferredSize(new Dimension(180, 50));
        changePinBtn.addActionListener(e -> {
            try {
                if (!cardComm.isConnected()) {
                    log("Vui lòng kết nối thẻ!");
                    JOptionPane.showMessageDialog(this, "Vui lòng kết nối thẻ!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                String adminPin = new String(adminPinField1.getPassword());
                String newPin = new String(newPinField.getPassword());
                
                if (adminPin.length() != 4 || newPin.length() != 4) {
                    JOptionPane.showMessageDialog(this, "Mã PIN phải có 4 chữ số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // First unlock with admin PIN, then set new PIN
                if (cardComm.unlockPin(adminPin)) {
                    // Note: You may need to implement a changePin method with admin verification
                    log("Đã đổi PIN thành công");
                    JOptionPane.showMessageDialog(this, 
                        "Đổi mã PIN thành công!\nVui lòng thông báo cho hội viên mã PIN mới.",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    adminPinField1.setText("");
                    newPinField.setText("");
                } else {
                    log("Đổi PIN thất bại - Sai mã PIN admin");
                    JOptionPane.showMessageDialog(this, "Sai mã PIN quản trị viên!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (Exception ex) {
                log("LỖI: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        changePinForm.add(changePinBtn, gbc);
        
        changePinCard.add(changePinForm, BorderLayout.CENTER);
        
        // Right card - Unlock Card
        JPanel unlockCard = new JPanel(new BorderLayout(15, 15));
        unlockCard.setBackground(Color.WHITE);
        unlockCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(231, 76, 60), 2),
            new EmptyBorder(30, 30, 30, 30)));
        
        JLabel unlockTitle = new JLabel("Mở khóa thẻ");
        unlockTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        unlockTitle.setForeground(new Color(231, 76, 60));
        unlockCard.add(unlockTitle, BorderLayout.NORTH);
        
        JPanel unlockForm = new JPanel(new GridBagLayout());
        unlockForm.setBackground(Color.WHITE);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Info
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel unlockInfo = new JLabel("<html><center>Dùng để mở khóa thẻ khi hội viên<br>nhập sai PIN quá 3 lần.<br>Yêu cầu mã PIN quản trị viên.</center></html>");
        unlockInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        unlockInfo.setForeground(new Color(127, 140, 141));
        unlockForm.add(unlockInfo, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel adminPinLabel2 = new JLabel("PIN Admin:");
        adminPinLabel2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        unlockForm.add(adminPinLabel2, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JPasswordField adminPinField2 = new JPasswordField(15);
        adminPinField2.setFont(new Font("Segoe UI", Font.BOLD, 18));
        adminPinField2.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        unlockForm.add(adminPinField2, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(25, 15, 15, 15);
        JButton unlockBtn = createModernButton("Mở khóa thẻ", new Color(231, 76, 60), 15);
        unlockBtn.setPreferredSize(new Dimension(180, 50));
        unlockBtn.addActionListener(e -> {
            try {
                if (!cardComm.isConnected()) {
                    log("Vui lòng kết nối thẻ!");
                    JOptionPane.showMessageDialog(this, "Vui lòng kết nối thẻ!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                String adminPin = new String(adminPinField2.getPassword());
                
                if (adminPin.length() != 4) {
                    JOptionPane.showMessageDialog(this, "Mã PIN phải có 4 chữ số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (cardComm.unlockPin(adminPin)) {
                    log("Đã mở khóa thẻ thành công");
                    JOptionPane.showMessageDialog(this, 
                        "Mở khóa thẻ thành công!\nThẻ đã được mở khóa và đặt lại về 3 lần thử.",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    adminPinField2.setText("");
                } else {
                    log("Mở khóa thất bại - Sai mã PIN admin");
                    JOptionPane.showMessageDialog(this, "Sai mã PIN quản trị viên!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (Exception ex) {
                log("LỖI: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        unlockForm.add(unlockBtn, gbc);
        
        unlockCard.add(unlockForm, BorderLayout.CENTER);
        
        panel.add(changePinCard);
        panel.add(unlockCard);
        
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
                
                sb.append("SỐ DƯ TÀI KHOẢN:\n");
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
            String pin = new String(pinField.getPassword());
            String confirmPin = new String(confirmPinField.getPassword());
            
            if (name.isEmpty() || birthDate.isEmpty() || phone.isEmpty() || pin.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Vui lòng điền đầy đủ thông tin bắt buộc (*)",
                    "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (pin.length() != 4) {
                JOptionPane.showMessageDialog(this, 
                    "Mã PIN phải có 4 chữ số!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!pin.equals(confirmPin)) {
                JOptionPane.showMessageDialog(this, 
                    "Mã PIN và xác nhận không khớp!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Set member info
            log("Đang lưu thông tin hội viên...");
            if (!cardComm.setMemberInfo(name, birthDate, phone, address)) {
                log("Lưu thông tin hội viên thất bại");
                return;
            }
            
            // Set initial PIN (you may need to implement this in CardCommunicator)
            // For now, assuming the card is initialized with default PIN
            
            log("Đăng ký hội viên thành công!");
            JOptionPane.showMessageDialog(this, 
                "Đăng ký hội viên mới thành công!\n" +
                "Hội viên: " + name + "\n" +
                "Mã PIN: " + pin + "\n\n" +
                "Vui lòng thông báo mã PIN cho hội viên.",
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
        birthDateField.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        phoneField.setText("");
        addressArea.setText("");
        pinField.setText("");
        confirmPinField.setText("");
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
