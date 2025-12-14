package gymcard.client.ui;

import gymcard.client.*;
import gymcard.databaseManager.DatabaseManager;
import com.toedter.calendar.JDateChooser;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
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
    private JDateChooser birthDateChooser;
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
        // add(headerPanel, BorderLayout.NORTH);

        // Main content with tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.setBackground(Color.WHITE);

        tabbedPane.addTab("Đăng ký hội viên", createRegistrationPanel());
        tabbedPane.addTab("Đổi PIN & Mở khóa", createPinManagementPanel());
        // tabbedPane.addTab("📊 Quản lý thẻ", createCardManagementPanel());

        add(tabbedPane, BorderLayout.CENTER);

        // Log area
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBackground(Color.WHITE);
        logPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(149, 165, 166), 2),
                "Nhật ký hệ thống",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13)));

        logArea = new JTextArea(6, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(250, 250, 250));
        JScrollPane logScroll = new JScrollPane(logArea);
        logPanel.add(logScroll, BorderLayout.CENTER);

        add(logPanel, BorderLayout.SOUTH);
    }

    /**
     * Panel đăng ký hội viên mới (khởi tạo thẻ + lưu thông tin cơ bản)
     */
    private JPanel createRegistrationPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(248, 249, 250));

        // ===== MAIN CONTENT =====
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(new Color(248, 249, 250));

        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(15, 15, 15, 15))); // giảm padding

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8); // giảm khoảng cách giữa các dòng
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        Font labelFont = new Font("Segoe UI", Font.PLAIN, 13);
        Font inputFont = new Font("Segoe UI", Font.PLAIN, 13);

        // ===== HỌ TÊN =====
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel nameLabel = new JLabel("Họ và tên: *");
        nameLabel.setFont(labelFont);
        formPanel.add(nameLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        nameField = new JTextField(25);
        nameField.setFont(inputFont);
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        formPanel.add(nameField, gbc);

        // ===== NGÀY SINH (Date Picker) =====
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel birthLabel = new JLabel("Ngày sinh: *");
        birthLabel.setFont(labelFont);
        formPanel.add(birthLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        birthDateChooser = new JDateChooser();
        birthDateChooser.setDateFormatString("dd/MM/yyyy");
        birthDateChooser.setDate(new Date());
        birthDateChooser.setFont(inputFont);
        birthDateChooser.setPreferredSize(new Dimension(200, 34));
        birthDateChooser.getCalendarButton().setBackground(new Color(52, 152, 219));
        birthDateChooser.getCalendarButton().setForeground(Color.WHITE);
        formPanel.add(birthDateChooser, gbc);

        // ===== SỐ ĐIỆN THOẠI =====
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel phoneLabel = new JLabel("Số điện thoại: *");
        phoneLabel.setFont(labelFont);
        formPanel.add(phoneLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        phoneField = new JTextField(25);
        phoneField.setFont(inputFont);
        phoneField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        formPanel.add(phoneField, gbc);

        // ===== ĐỊA CHỈ =====
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        JLabel addressLabel = new JLabel("Địa chỉ:");
        addressLabel.setFont(labelFont);
        formPanel.add(addressLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        addressArea = new JTextArea(2, 25); // bớt cao
        addressArea.setFont(inputFont);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        addressArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        JScrollPane addressScroll = new JScrollPane(addressArea);
        addressScroll.setBorder(null);
        formPanel.add(addressScroll, gbc);

        // ===== PIN =====
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel pinLabel = new JLabel("Mã PIN (6 chữ số): *");
        pinLabel.setFont(labelFont);
        formPanel.add(pinLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        pinField = new JPasswordField(25);
        pinField.setFont(new Font("Segoe UI", Font.BOLD, 16));
        pinField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        formPanel.add(pinField, gbc);

        // ===== XÁC NHẬN PIN =====
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel confirmPinLabel = new JLabel("Xác nhận PIN: *");
        confirmPinLabel.setFont(labelFont);
        formPanel.add(confirmPinLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        confirmPinField = new JPasswordField(25);
        confirmPinField.setFont(new Font("Segoe UI", Font.BOLD, 16));
        confirmPinField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        formPanel.add(confirmPinField, gbc);
        // ===== AVATAR =====
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        JLabel avatarTitle = new JLabel("Ảnh đại diện (tùy chọn):");
        avatarTitle.setFont(labelFont);
        formPanel.add(avatarTitle, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;

        JPanel avatarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        avatarPanel.setBackground(Color.WHITE);

        avatarLabel = new JLabel("Chưa chọn ảnh");
        avatarLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        avatarLabel.setForeground(new Color(127, 140, 141));
        avatarLabel.setPreferredSize(new Dimension(90, 90));
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avatarLabel.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));

        JButton chooseAvatarBtn = new JButton("Chọn ảnh...");
        chooseAvatarBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chooseAvatarBtn.addActionListener(e -> chooseAvatarImage());

        avatarPanel.add(avatarLabel);
        avatarPanel.add(chooseAvatarBtn);

        formPanel.add(avatarPanel, gbc);
        // ===== GHI CHÚ BẢO MẬT (nhỏ lại) =====
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel hintLabel = new JLabel(
                "<html><i>Ghi chú: Mã PIN được bảo vệ giới hạn số lần thử.<br>" +
                        "Dữ liệu cá nhân trên thẻ được mã hóa AES-128 bằng khóa chủ bọc bởi PIN của bạn.</i></html>");
        hintLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hintLabel.setForeground(new Color(127, 140, 141));
        formPanel.add(hintLabel, gbc);

        // ==== cho FORM vào SCROLLPANE ====
        JScrollPane formScroll = new JScrollPane(formPanel);
        formScroll.setBorder(null);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);
        contentPanel.add(formScroll, BorderLayout.CENTER);

        panel.add(contentPanel, BorderLayout.CENTER);

        // ===== NÚT BÊN DƯỚI (luôn thấy được) =====
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        buttonPanel.setBackground(new Color(248, 249, 250));

        JButton registerBtn = createModernButton("Đăng ký hội viên (Init thẻ)", new Color(46, 204, 113), 15);
        registerBtn.setPreferredSize(new Dimension(240, 42));
        registerBtn.addActionListener(e -> registerMember());

        JButton clearBtn = createModernButton("Xóa form", new Color(149, 165, 166), 13);
        clearBtn.setPreferredSize(new Dimension(130, 42));
        clearBtn.addActionListener(e -> clearForm());

        buttonPanel.add(registerBtn);
        buttonPanel.add(clearBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void chooseAvatarImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn ảnh đại diện");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Ảnh (JPG, PNG)", "jpg", "jpeg", "png"));

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            avatarPath = file.getAbsolutePath();

            try {
                BufferedImage img = javax.imageio.ImageIO.read(file);
                if (img == null) {
                    JOptionPane.showMessageDialog(this,
                            "Không đọc được ảnh này. Vui lòng chọn file JPG/PNG.",
                            "Lỗi ảnh", JOptionPane.ERROR_MESSAGE);
                    avatarPath = null;
                    return;
                }

                Image scaled = img.getScaledInstance(90, 90, Image.SCALE_SMOOTH);
                avatarLabel.setIcon(new ImageIcon(scaled));
                avatarLabel.setText("");
                log("Đã chọn avatar: " + avatarPath);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Lỗi đọc ảnh: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                avatarPath = null;
            }
        }
    }

    private byte[] compressAvatarToCardSize(String path, int maxBytes) throws Exception {
        BufferedImage src = ImageIO.read(new File(path));
        if (src == null)
            return null;

        int target = 256; // bắt đầu 256px
        float quality = 0.85f; // bắt đầu quality cao

        while (true) {
            BufferedImage scaled = new BufferedImage(target, target, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = scaled.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(src, 0, 0, target, target, null);
            g.dispose();

            byte[] jpg = encodeJpeg(scaled, quality);

            if (jpg.length <= maxBytes)
                return jpg;

            if (quality > 0.25f) {
                quality -= 0.10f;
            } else if (target > 64) {
                target = (int) (target * 0.80);
                quality = 0.85f; // reset quality khi giảm size
            } else {
                return null; // không thể nén nhỏ hơn
            }
        }
    }

    private byte[] encodeJpeg(BufferedImage img, float quality) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(img, null, null), param);
        } finally {
            writer.dispose();
        }
        return baos.toByteArray();
    }

    /**
     * Panel quản lý PIN (Đổi PIN + Mở khóa)
     */
    /**
     * Panel quản lý PIN (Đổi PIN + Mở khóa)
     * - Cột trái: Đổi PIN hội viên (có PIN cũ)
     * - Cột phải: Admin mở khóa thẻ (khi nhập sai quá số lần cho phép)
     */
    private JPanel createPinManagementPanel() {
        // Panel ngoài dùng BorderLayout để dễ resize
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBorder(new EmptyBorder(15, 15, 15, 15));
        outer.setBackground(new Color(248, 249, 250));

        JPanel panel = new JPanel(new GridLayout(1, 2, 12, 0));
        panel.setOpaque(false);

        // ========== LEFT CARD: ĐỔI PIN KHI HỘI VIÊN QUÊN ==========
        JPanel changePinCard = new JPanel(new BorderLayout(10, 10));
        changePinCard.setBackground(Color.WHITE);
        changePinCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                new EmptyBorder(15, 15, 15, 15)));

        JLabel changePinTitle = new JLabel("Đổi mã PIN khi hội viên quên");
        changePinTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        changePinTitle.setForeground(new Color(52, 152, 219));
        changePinCard.add(changePinTitle, BorderLayout.NORTH);

        JPanel changePinForm = new JPanel(new GridBagLayout());
        changePinForm.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;

        // Info
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JLabel changePinInfo = new JLabel(
                "<html><center>Dùng khi hội viên QUÊN mã PIN.<br>" +
                        "Nhập mật khẩu admin và PIN mới cho thẻ.</center></html>");
        changePinInfo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        changePinInfo.setForeground(new Color(127, 140, 141));
        changePinForm.add(changePinInfo, gbc);

        gbc.gridwidth = 1;

        // Admin pass
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel adminPinLabel1 = new JLabel("Mật khẩu admin:");
        adminPinLabel1.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        changePinForm.add(adminPinLabel1, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JPasswordField adminPinField1 = new JPasswordField(10);
        adminPinField1.setFont(new Font("Segoe UI", Font.BOLD, 14));
        adminPinField1.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        changePinForm.add(adminPinField1, gbc);

        // New PIN
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel newPinLabel = new JLabel("PIN mới (6 số):");
        newPinLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        changePinForm.add(newPinLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JPasswordField newPinField = new JPasswordField(10);
        newPinField.setFont(new Font("Segoe UI", Font.BOLD, 16));
        newPinField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        changePinForm.add(newPinField, gbc);

        // Confirm PIN
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel confirmPinLabel = new JLabel("Xác nhận PIN mới:");
        confirmPinLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        changePinForm.add(confirmPinLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JPasswordField confirmPinField = new JPasswordField(10);
        confirmPinField.setFont(new Font("Segoe UI", Font.BOLD, 16));
        confirmPinField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        changePinForm.add(confirmPinField, gbc);

        // Button
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(12, 6, 6, 6);
        JButton changePinBtn = createModernButton("Đổi PIN (Admin)", new Color(52, 152, 219), 14);
        changePinBtn.setPreferredSize(new Dimension(180, 38));
        changePinBtn.addActionListener(e -> {
            try {
                if (!cardComm.isConnected()) {
                    log("Vui lòng kết nối thẻ!");
                    JOptionPane.showMessageDialog(this, "Vui lòng kết nối thẻ!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String adminPass = new String(adminPinField1.getPassword()).trim();
                String newPin = new String(newPinField.getPassword()).trim();
                String confirmPin = new String(confirmPinField.getPassword()).trim();

                if (adminPass.isEmpty() || newPin.isEmpty() || confirmPin.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Vui lòng nhập đầy đủ mật khẩu admin và PIN mới.",
                            "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (!newPin.matches("\\d{6}")) {
                    JOptionPane.showMessageDialog(this,
                            "PIN mới phải gồm đúng 6 chữ số (0–9).",
                            "PIN không hợp lệ", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!newPin.equals(confirmPin)) {
                    JOptionPane.showMessageDialog(this,
                            "PIN mới và xác nhận không khớp!",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (cardComm.adminResetMemberPin(adminPass, newPin)) {
                    log("Admin đã đổi PIN hội viên (quên PIN)");
                    JOptionPane.showMessageDialog(this,
                            "Đổi mã PIN thành công!\nThẻ đã được đặt PIN mới cho hội viên.",
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    adminPinField1.setText("");
                    newPinField.setText("");
                    confirmPinField.setText("");
                } else {
                    log("Đổi PIN (admin) thất bại");
                    JOptionPane.showMessageDialog(this,
                            "Đổi PIN thất bại.\nVui lòng kiểm tra lại mật khẩu admin.",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception ex) {
                log("LỖI đổi PIN (admin): " + ex.getMessage());
                JOptionPane.showMessageDialog(this,
                        "Lỗi đổi PIN: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        changePinForm.add(changePinBtn, gbc);

        changePinCard.add(changePinForm, BorderLayout.CENTER);

        // ========== RIGHT CARD: UNLOCK CARD (ADMIN) ==========
        JPanel unlockCard = new JPanel(new BorderLayout(10, 10));
        unlockCard.setBackground(Color.WHITE);
        unlockCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(231, 76, 60), 2),
                new EmptyBorder(15, 15, 15, 15)));

        JLabel unlockTitle = new JLabel("Mở khóa thẻ (Admin)");
        unlockTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        unlockTitle.setForeground(new Color(231, 76, 60));
        unlockCard.add(unlockTitle, BorderLayout.NORTH);

        JPanel unlockForm = new JPanel(new GridBagLayout());
        unlockForm.setBackground(Color.WHITE);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row2 = 0;

        // Info
        gbc.gridx = 0;
        gbc.gridy = row2++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JLabel unlockInfo = new JLabel(
                "<html><center>Mở khóa thẻ khi hội viên nhập sai PIN quá số lần cho phép.<br>" +
                        "Mật khẩu admin mặc định là &quot;ADMIN&quot;.</center></html>");
        unlockInfo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        unlockInfo.setForeground(new Color(127, 140, 141));
        unlockForm.add(unlockInfo, gbc);

        // Admin pass
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = row2;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel adminPinLabel2 = new JLabel("Mật khẩu admin:");
        adminPinLabel2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        unlockForm.add(adminPinLabel2, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JPasswordField adminPinField2 = new JPasswordField(10);
        adminPinField2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        adminPinField2.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        unlockForm.add(adminPinField2, gbc);

        // Unlock button
        row2++;
        gbc.gridx = 0;
        gbc.gridy = row2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(12, 6, 6, 6);
        JButton unlockBtn = createModernButton("Mở khóa thẻ", new Color(231, 76, 60), 14);
        unlockBtn.setPreferredSize(new Dimension(180, 38));
        unlockBtn.addActionListener(e -> {
            try {
                if (!cardComm.isConnected()) {
                    log("Vui lòng kết nối thẻ!");
                    JOptionPane.showMessageDialog(this,
                            "Vui lòng kết nối thẻ!",
                            "Lỗi", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String adminPass = new String(adminPinField2.getPassword()).trim();
                if (adminPass.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Vui lòng nhập mật khẩu admin (ví dụ: ADMIN).",
                            "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (cardComm.unlockPin(adminPass)) {
                    log("Đã mở khóa thẻ thành công (reset số lần thử PIN)");
                    JOptionPane.showMessageDialog(this,
                            "Mở khóa thẻ thành công!\n" +
                                    "Thẻ đã được reset số lần thử PIN về mặc định.\n" +
                                    "Lưu ý: PIN của hội viên không thay đổi.",
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    adminPinField2.setText("");
                } else {
                    log("Mở khóa thẻ thất bại - sai mật khẩu admin");
                    JOptionPane.showMessageDialog(this,
                            "Mở khóa thẻ thất bại.\nVui lòng kiểm tra lại mật khẩu admin.",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception ex) {
                log("LỖI mở khóa thẻ: " + ex.getMessage());
                JOptionPane.showMessageDialog(this,
                        "Lỗi mở khóa thẻ: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        unlockForm.add(unlockBtn, gbc);

        unlockCard.add(unlockForm, BorderLayout.CENTER);

        // Add 2 card vào panel chia đôi
        panel.add(changePinCard);
        panel.add(unlockCard);

        outer.add(panel, BorderLayout.CENTER);
        return outer;
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
                new Font("Arial", Font.BOLD, 14)));

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
     * Đăng ký hội viên mới
     */
    /**
     * Đăng ký hội viên mới + khởi tạo thẻ
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

            String name = nameField.getText().trim();
            String birthDate = birthDateChooser.getDate() != null
                    ? new SimpleDateFormat("dd/MM/yyyy").format(birthDateChooser.getDate())
                    : "";
            String phone = phoneField.getText().trim();
            String address = addressArea.getText().trim();
            String pin = new String(pinField.getPassword()).trim();
            String confirmPin = new String(confirmPinField.getPassword()).trim();

            // validate như cũ...
            if (name.isEmpty() || birthDate.isEmpty() || phone.isEmpty() || pin.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng điền đầy đủ các trường bắt buộc (*)",
                        "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!pin.matches("\\d{6}")) {
                JOptionPane.showMessageDialog(this,
                        "Mã PIN phải gồm đúng 6 chữ số (0-9)!",
                        "PIN không hợp lệ", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!pin.equals(confirmPin)) {
                JOptionPane.showMessageDialog(this,
                        "Mã PIN và xác nhận PIN không khớp!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 2. Khởi tạo thẻ: cardId dùng luôn số điện thoại (tối đa 32 byte)
            String cardId = phone;
            log("Đang khởi tạo thẻ (INIT_CARD) với CardID = " + cardId + " ...");
            cardComm.initNewCard(cardId, pin); // <-- bạn implement trong CardCommunicator, gọi
                                               // CardManager.initCard(...)
            log("Khởi tạo thẻ thành công.");

            // 3. VERIFY_PIN (BẮT BUỘC trước khi ghi dữ liệu)
            log("Đang xác thực PIN...");
            if (!cardComm.verifyPin(pin)) {
                log("Xác thực PIN thất bại sau INIT_CARD!");
                JOptionPane.showMessageDialog(this,
                        "PIN không đúng hoặc thẻ chưa sẵn sàng.\n" +
                                "Vui lòng thử lại.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            log("Xác thực PIN OK.");

            // 4. Nén avatar (nếu có chọn)
            byte[] avatarBytes = null;
            if (avatarPath != null) {
                avatarBytes = compressAvatarToCardSize(avatarPath, 4096); // AVATAR_LEN
                if (avatarBytes == null) {
                    log("Không thể nén avatar xuống <= 192 bytes, bỏ qua lưu avatar.");
                } else {
                    log("Avatar đã nén: " + avatarBytes.length + " bytes.");
                }
            }

            // 5. Ghi thông tin hội viên xuống thẻ (thẻ tự mã hóa AES-128)
            log("Đang lưu thông tin hội viên (mã hóa AES trên thẻ)...");
            boolean ok = cardComm.setMemberInfo(name, birthDate, phone, address, avatarBytes);
            if (!ok) {
                log("Lưu thông tin hội viên thất bại");
                JOptionPane.showMessageDialog(this,
                        "Không thể lưu thông tin hội viên lên thẻ.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 6. Thông báo
            log("Đăng ký hội viên & khởi tạo thẻ thành công!");
            JOptionPane.showMessageDialog(this,
                    "Đăng ký hội viên mới và khởi tạo thẻ thành công!\n\n" +
                            "Hội viên: " + name + "\n" +
                            "Mã thẻ (CardID): " + cardId + "\n" +
                            "Mã PIN: " + pin + "\n\n" +
                            "• CardID dùng để quản lý trong hệ thống.\n" +
                            "• PIN dùng để hội viên check-in và bảo vệ dữ liệu trên thẻ.\n",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);

            clearForm();

        } catch (Exception ex) {
            log("LỖI: " + ex.getMessage());
            ex.printStackTrace();
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
                if (trans == null)
                    break;

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
        birthDateChooser.setDate(new Date());
        phoneField.setText("");
        addressArea.setText("");
        pinField.setText("");
        confirmPinField.setText("");
        avatarLabel.setIcon(null);
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
