package gymcard.client.ui;

import gymcard.client.*;
import gymcard.databaseManager.DatabaseManager;
import gymcard.databaseManager.DatabaseManager.PlanInfo;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.*;
import com.toedter.calendar.JCalendar;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Calendar;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * User Panel - Dành cho hội viên
 */
public class UserPanel extends JPanel {

    private final CardCommunicator cardComm;
    private JTextArea logArea;

    // Components
    private JPanel contentPanel;
    private CardLayout cardLayout;

    // Danh sách dịch vụ đã mua
    private final java.util.List<String> purchasedServices = new java.util.ArrayList<>();

    public UserPanel(CardCommunicator cardComm) {
        this.cardComm = cardComm;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10)); // giảm padding ngoài
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
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(46, 204, 113),
                        w, 0, new Color(26, 188, 156));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        headerPanel.setPreferredSize(new Dimension(0, 70)); // thấp hơn 1 chút
        headerPanel.setOpaque(false);
        // JLabel titleLabel = new JLabel("💪 HỘI VIÊN GYM CARD");
        // titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24)); // bớt to
        // titleLabel.setForeground(Color.WHITE);
        // headerPanel.add(titleLabel);
        // add(headerPanel, BorderLayout.NORTH);

        // Content area with CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        contentPanel.add(createLoginPanel(), "login");
        contentPanel.add(createMainPanel(), "main");

        add(contentPanel, BorderLayout.CENTER);

        // Show login panel first
        cardLayout.show(contentPanel, "login");
    }

    /**
     * Panel đăng nhập (UI gọn hơn, không chiếm quá nhiều chiều dọc)
     */
    private JPanel createLoginPanel() {

        // ===== Root =====
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(new Color(236, 240, 241)); // nền xám nhạt

        // ===== Card Panel =====
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(24, 28, 24, 28)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.CENTER;

        int row = 0;

        // ===== Icon =====
        JLabel iconLabel = new JLabel("");
        gbc.gridy = row++;
        card.add(iconLabel, gbc);

        // ===== Title =====
        JLabel titleLabel = new JLabel("ĐĂNG NHẬP BẰNG PIN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(44, 62, 80));
        gbc.gridy = row++;
        card.add(titleLabel, gbc);

        // ===== Subtitle =====
        JLabel subtitleLabel = new JLabel("Vui lòng nhập mã PIN gồm 6 chữ số");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(127, 140, 141));
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 6, 14, 6);
        card.add(subtitleLabel, gbc);

        // ===== PIN Field =====
        gbc.gridy = row++;
        gbc.insets = new Insets(8, 6, 6, 6);

        JPasswordField pinField = new JPasswordField(6);
        pinField.setFont(new Font("Segoe UI", Font.BOLD, 28));
        pinField.setHorizontalAlignment(JTextField.CENTER);
        pinField.setEchoChar('●');
        pinField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        pinField.setPreferredSize(new Dimension(220, 54));
        card.add(pinField, gbc);

        // ===== Tries Label =====
        JLabel triesLabel = new JLabel(" ");
        triesLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        triesLabel.setForeground(new Color(231, 76, 60));
        gbc.gridy = row++;
        gbc.insets = new Insets(4, 6, 10, 6);
        card.add(triesLabel, gbc);

        // ===== Login Button =====
        gbc.gridy = row++;
        gbc.insets = new Insets(8, 6, 6, 6);
        JButton loginBtn = createModernButton("Đăng nhập",
                new Color(52, 152, 219), 14);
        loginBtn.setPreferredSize(new Dimension(220, 42));
        card.add(loginBtn, gbc);

        // ===== Check tries =====
        gbc.gridy = row++;
        gbc.insets = new Insets(4, 6, 0, 6);
        JButton checkTriesBtn = createModernButton("Kiểm tra số lần thử",
                new Color(155, 89, 182), 12);
        checkTriesBtn.setPreferredSize(new Dimension(220, 36));
        card.add(checkTriesBtn, gbc);

        // ===== Add to root =====
        root.add(card);

        // ===== ACTION LISTENERS (GIỮ NGUYÊN LOGIC CỦA BẠN) =====
        ActionListener loginAction = e -> {
            try {
                if (!cardComm.isConnected()) {
                    JOptionPane.showMessageDialog(this,
                            "Vui lòng kết nối thẻ!",
                            "Lỗi", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String pin = new String(pinField.getPassword()).trim();

                if (!pin.matches("\\d{6}")) {
                    JOptionPane.showMessageDialog(this,
                            "Mã PIN phải gồm đúng 6 chữ số!",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int tries = cardComm.getPinTries();
                if (tries == 0) {
                    JOptionPane.showMessageDialog(this,
                            "Thẻ đã bị khóa!\nVui lòng liên hệ quản trị viên.",
                            "Thẻ bị khóa", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (cardComm.verifyPin(pin)) {
                    pinField.setText("");
                    triesLabel.setText(" ");
                    cardLayout.show(contentPanel, "main");
                    SwingUtilities.invokeLater(() -> clearUserInfoUI());
                } else {
                    tries = cardComm.getPinTries();
                    triesLabel.setText("Sai PIN! Còn " + tries + " lần thử");
                    pinField.setText("");
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Lỗi xác thực: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        };

        loginBtn.addActionListener(loginAction);
        pinField.addActionListener(loginAction);

        checkTriesBtn.addActionListener(e -> {
            try {
                if (!cardComm.isConnected())
                    return;

                int tries = cardComm.getPinTries();
                if (tries == 0) {
                    triesLabel.setText("Thẻ đã bị khóa!");
                } else {
                    triesLabel.setText("Còn " + tries + " lần nhập PIN");
                }
            } catch (Exception ignored) {
            }
        });

        return root;
    }

    /**
     * Panel chính sau khi đăng nhập
     */
    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(248, 249, 250));

        // Card trắng chứa tab
        JPanel tabsCard = new JPanel(new BorderLayout());
        tabsCard.setBackground(Color.WHITE);
        tabsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(10, 10, 5, 10)));

        // Tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(new Color(248, 249, 250));

        tabbedPane.addTab("Thông tin cá nhân", createInfoTab());
        tabbedPane.addTab("Gói tập", createPackageTab());
        tabbedPane.addTab("Check-in/Check-out", createCheckInTab());
        tabbedPane.addTab("Nạp tiền", createTopUpTab());
        tabbedPane.addTab("Dịch vụ thêm", createServicesTab());
        tabbedPane.addTab("Thống kê", createStatisticsTab());
        tabbedPane.addTab("Đổi PIN", createChangePinTab());

        tabsCard.add(tabbedPane, BorderLayout.CENTER);

        // Wrapper để không dính sát mép
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBackground(new Color(248, 249, 250));
        centerWrapper.setBorder(new EmptyBorder(10, 10, 0, 10));
        centerWrapper.add(tabsCard, BorderLayout.CENTER);

        panel.add(centerWrapper, BorderLayout.CENTER);

        // Logout button (footer nhỏ gọn)
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 8));
        bottomPanel.setBackground(new Color(248, 249, 250));
        JButton logoutBtn = createModernButton("Đăng xuất", new Color(149, 165, 166), 14);
        logoutBtn.setPreferredSize(new Dimension(140, 36));
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
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(new Color(248, 249, 250));

        JLabel titleLabel = new JLabel("THÔNG TIN CÁ NHÂN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(52, 73, 94));
        titleLabel.setBorder(new EmptyBorder(0, 5, 5, 5));
        panel.add(titleLabel, BorderLayout.NORTH);

        // ===== Card container =====
        JPanel cardPanel = new JPanel(new BorderLayout(15, 15));
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(15, 15, 15, 15)));

        // ===== Avatar left =====
        JPanel avatarPanel = new JPanel(new GridBagLayout());
        avatarPanel.setBackground(Color.WHITE);
        avatarPanel.setPreferredSize(new Dimension(180, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        avatarLabel = new JLabel("[Avatar]");
        avatarLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avatarLabel.setPreferredSize(new Dimension(120, 120));
        avatarLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        avatarLabel.setOpaque(true);
        avatarLabel.setBackground(new Color(240, 248, 255));
        gbc.gridx = 0;
        gbc.gridy = 0;
        avatarPanel.add(avatarLabel, gbc);

        JButton changeAvatarBtn = createModernButton("Đổi ảnh", new Color(155, 89, 182), 12);
        changeAvatarBtn.setPreferredSize(new Dimension(120, 34));
        gbc.gridy = 1;
        avatarPanel.add(changeAvatarBtn, gbc);

        cardPanel.add(avatarPanel, BorderLayout.WEST);

        // ===== Right: View/Edit cards via CardLayout =====
        CardLayout infoLayout = new CardLayout();
        JPanel rightStack = new JPanel(infoLayout);
        rightStack.setBackground(Color.WHITE);

        // ---------- VIEW PANEL ----------
        JPanel viewPanel = new JPanel(new BorderLayout(10, 10));
        viewPanel.setBackground(Color.WHITE);

        infoArea = new JTextArea(10, 40);
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setBackground(new Color(250, 250, 250));
        infoArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(infoArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));
        viewPanel.add(scrollPane, BorderLayout.CENTER);

        // ---------- EDIT PANEL ----------
        JPanel editPanel = new JPanel(new GridBagLayout());
        editPanel.setBackground(Color.WHITE);

        JTextField nameField = new JTextField(26);
        JTextField dobField = new JTextField(12);
        JTextField phoneField = new JTextField(16);
        JTextArea addressArea = new JTextArea(3, 26);

        Font inputFont = new Font("Segoe UI", Font.PLAIN, 13);
        nameField.setFont(inputFont);
        dobField.setFont(inputFont);
        phoneField.setFont(inputFont);
        addressArea.setFont(inputFont);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);

        // borders
        nameField.setBorder(fieldBorder());
        dobField.setBorder(fieldBorder());
        phoneField.setBorder(fieldBorder());
        addressArea.setBorder(fieldBorder());

        JLabel editHint = new JLabel("<html><i>Nhấn Lưu để ghi lên thẻ (yêu cầu bạn đã đăng nhập PIN).</i></html>");
        editHint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        editHint.setForeground(new Color(127, 140, 141));

        GridBagConstraints egbc = new GridBagConstraints();
        egbc.insets = new Insets(6, 6, 6, 6);
        egbc.fill = GridBagConstraints.HORIZONTAL;
        egbc.weightx = 1.0;

        int r = 0;
        addRow(editPanel, egbc, r++, "Họ và tên *", nameField);
        addRow(editPanel, egbc, r++, "Ngày sinh (dd/MM/yyyy) *", dobField);
        addRow(editPanel, egbc, r++, "Số điện thoại *", phoneField);

        egbc.gridx = 0;
        egbc.gridy = r;
        egbc.weightx = 0;
        egbc.anchor = GridBagConstraints.NORTHEAST;
        JLabel addrLabel = new JLabel("Địa chỉ");
        addrLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        editPanel.add(addrLabel, egbc);

        egbc.gridx = 1;
        egbc.weightx = 1.0;
        egbc.anchor = GridBagConstraints.WEST;
        JScrollPane addrScroll = new JScrollPane(addressArea);
        addrScroll.setBorder(null);
        editPanel.add(addrScroll, egbc);
        r++;

        egbc.gridx = 0;
        egbc.gridy = r;
        egbc.gridwidth = 2;
        editPanel.add(editHint, egbc);

        rightStack.add(viewPanel, "view");
        rightStack.add(editPanel, "edit");
        cardPanel.add(rightStack, BorderLayout.CENTER);

        panel.add(cardPanel, BorderLayout.CENTER);

        // ===== State for editing avatar bytes =====
        final byte[][] pendingAvatar = new byte[1][]; // pending avatarBytes to save
        final MemberInfo[] loadedMember = new MemberInfo[1];

        // ===== Bottom buttons =====
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(new Color(248, 249, 250));

        JButton loadBtn = createModernButton("Tải thông tin từ thẻ", new Color(52, 152, 219), 14);
        loadBtn.setPreferredSize(new Dimension(220, 40));

        JButton editBtn = createModernButton("Chỉnh sửa", new Color(241, 196, 15), 14);
        editBtn.setPreferredSize(new Dimension(150, 40));

        JButton saveBtn = createModernButton("Lưu lên thẻ", new Color(46, 204, 113), 14);
        saveBtn.setPreferredSize(new Dimension(170, 40));

        JButton cancelBtn = createModernButton("Hủy", new Color(149, 165, 166), 14);
        cancelBtn.setPreferredSize(new Dimension(120, 40));

        // Default: view mode => show load + edit
        saveBtn.setVisible(false);
        cancelBtn.setVisible(false);
        changeAvatarBtn.setVisible(false);

        loadBtn.addActionListener(e -> {
            try {
                MemberInfo member = cardComm.getMemberInfo();
                loadedMember[0] = member;
                pendingAvatar[0] = member.avatarBytes; // current avatar from card

                setAvatarToLabel(avatarLabel, member.avatarBytes);

                StringBuilder sb = new StringBuilder();
                sb.append("━━━━━━━━ THÔNG TIN CÁ NHÂN ━━━━━━━━\n\n");
                sb.append(String.format("Họ và tên   : %s\n\n", member.name));
                sb.append(String.format("Ngày sinh   : %s\n\n", member.birthDate));
                sb.append(String.format("Số điện thoại: %s\n\n", member.phone));
                sb.append(String.format("Địa chỉ     : %s\n", member.address));

                infoArea.setText(sb.toString());
                infoArea.setCaretPosition(0);

                log("Đã tải thông tin cá nhân từ thẻ");
            } catch (Exception ex) {
                log("LỖI: " + ex.getMessage());
                JOptionPane.showMessageDialog(this,
                        "Lỗi tải thông tin: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        editBtn.addActionListener(e -> {
            MemberInfo m = loadedMember[0];
            if (m == null) {
                JOptionPane.showMessageDialog(this,
                        "Bạn hãy bấm “Tải thông tin từ thẻ” trước.",
                        "Chưa có dữ liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // fill fields
            nameField.setText(m.name == null ? "" : m.name);
            dobField.setText(m.birthDate == null ? "" : m.birthDate);
            phoneField.setText(m.phone == null ? "" : m.phone);
            addressArea.setText(m.address == null ? "" : m.address);

            infoLayout.show(rightStack, "edit");
            loadBtn.setVisible(false);
            editBtn.setVisible(false);
            saveBtn.setVisible(true);
            cancelBtn.setVisible(true);
            changeAvatarBtn.setVisible(true);
            log("Chế độ chỉnh sửa thông tin cá nhân");
        });

        cancelBtn.addActionListener(e -> {
            infoLayout.show(rightStack, "view");
            loadBtn.setVisible(true);
            editBtn.setVisible(true);
            saveBtn.setVisible(false);
            cancelBtn.setVisible(false);
            changeAvatarBtn.setVisible(false);

            // reset avatar to last loaded (not pending)
            MemberInfo m = loadedMember[0];
            pendingAvatar[0] = (m == null ? null : m.avatarBytes);
            setAvatarToLabel(avatarLabel, pendingAvatar[0]);

            log("Đã hủy chỉnh sửa");
        });

        changeAvatarBtn.addActionListener(e -> {
            try {
                byte[] bytes = chooseAndCompressAvatar4096();
                if (bytes != null) {
                    pendingAvatar[0] = bytes;
                    setAvatarToLabel(avatarLabel, bytes);
                    log("Đã chọn ảnh mới (đã nén) bytes=" + bytes.length);
                }
            } catch (Exception ex) {
                log("LỖI chọn ảnh: " + ex.getMessage());
                JOptionPane.showMessageDialog(this,
                        "Lỗi chọn ảnh: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        saveBtn.addActionListener(e -> {
            try {
                // ===== validate =====
                String newName = nameField.getText().trim();
                String newDob = dobField.getText().trim();
                String newPhone = phoneField.getText().trim();
                String newAddr = addressArea.getText().trim();
                if (newAddr.isEmpty())
                    newAddr = "";

                validateName(newName);
                validateBirthDate(newDob);
                validatePhone(newPhone);

                byte[] av = pendingAvatar[0];
                if (av != null && av.length > 4096) {
                    throw new Exception("Avatar vượt 4096 bytes (hiện " + av.length + ")");
                }

                // write to card
                cardComm.setMemberInfo(newName, newDob, newPhone, newAddr, av);

                // update loaded snapshot
                MemberInfo m = new MemberInfo();
                m.name = newName;
                m.birthDate = newDob;
                m.phone = newPhone;
                m.address = newAddr;
                m.avatarBytes = av;
                loadedMember[0] = m;

                // back to view mode
                StringBuilder sb = new StringBuilder();
                sb.append("━━━━━━━━ THÔNG TIN CÁ NHÂN ━━━━━━━━\n\n");
                sb.append(String.format("Họ và tên   : %s\n\n", newName));
                sb.append(String.format("Ngày sinh   : %s\n\n", newDob));
                sb.append(String.format("Số điện thoại: %s\n\n", newPhone));
                sb.append(String.format("Địa chỉ     : %s\n", newAddr));
                infoArea.setText(sb.toString());
                infoArea.setCaretPosition(0);

                infoLayout.show(rightStack, "view");
                loadBtn.setVisible(true);
                editBtn.setVisible(true);
                saveBtn.setVisible(false);
                cancelBtn.setVisible(false);
                changeAvatarBtn.setVisible(false);

                log("Đã lưu thông tin cá nhân + avatar lên thẻ");
                JOptionPane.showMessageDialog(this,
                        "Lưu thông tin thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                log("LỖI lưu: " + ex.getMessage());
                JOptionPane.showMessageDialog(this,
                        "Lỗi lưu thông tin: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(loadBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private Border fieldBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10));
    }

    private void addRow(JPanel parent, GridBagConstraints gbc, int row, String label, JComponent input) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        parent.add(l, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        parent.add(input, gbc);
    }

    private void validateName(String name) throws Exception {
        if (name == null || name.trim().isEmpty())
            throw new Exception("Họ và tên không được để trống");
        if (name.trim().length() < 2)
            throw new Exception("Họ và tên quá ngắn");
    }

    private void validateBirthDate(String birthDate) throws Exception {
        if (birthDate == null || birthDate.trim().isEmpty())
            throw new Exception("Ngày sinh không được để trống");
        if (!birthDate.matches("\\d{2}/\\d{2}/\\d{4}"))
            throw new Exception("Ngày sinh phải theo dd/MM/yyyy");

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false);
        try {
            sdf.parse(birthDate);
        } catch (java.text.ParseException e) {
            throw new Exception("Ngày sinh không hợp lệ");
        }
    }

    private void validatePhone(String phone) throws Exception {
        if (phone == null || phone.trim().isEmpty())
            throw new Exception("Số điện thoại không được để trống");
        String normalized = phone.trim().replaceAll("\\s+", "");
        if (!normalized.matches("0\\d{9,10}"))
            throw new Exception("SĐT không hợp lệ (0xxxxxxxxx, 10–11 số)");
    }

    private byte[] chooseAndCompressAvatar4096() throws Exception {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn ảnh đại diện");
        chooser.setFileFilter(new FileNameExtensionFilter("Ảnh (JPG, PNG)", "jpg", "jpeg", "png"));

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION)
            return null;

        File file = chooser.getSelectedFile();
        BufferedImage src = ImageIO.read(file);
        if (src == null)
            throw new Exception("Không đọc được ảnh. Hãy chọn JPG/PNG hợp lệ.");

        // resize mục tiêu nhỏ để dễ <= 4KB
        int target = 96; // 96x96 thường đủ rõ, dễ nén <4KB
        BufferedImage scaled = new BufferedImage(target, target, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, target, target, null);
        g.dispose();

        // nén JPEG quality giảm dần
        for (float q = 0.70f; q >= 0.15f; q -= 0.05f) {
            byte[] jpg = encodeJpeg(scaled, q);
            if (jpg.length <= 4096)
                return jpg;
        }

        // nếu vẫn không được: resize nhỏ hơn
        int[] sizes = { 80, 72, 64 };
        for (int s : sizes) {
            BufferedImage smaller = new BufferedImage(s, s, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = smaller.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(src, 0, 0, s, s, null);
            g2.dispose();

            for (float q = 0.70f; q >= 0.10f; q -= 0.05f) {
                byte[] jpg = encodeJpeg(smaller, q);
                if (jpg.length <= 4096)
                    return jpg;
            }
        }

        throw new Exception("Không thể nén ảnh xuống <= 4096 bytes. Hãy chọn ảnh đơn giản hơn (ít chi tiết).");
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

    private boolean isAllZero(byte[] data) {
        if (data == null)
            return true;
        for (byte b : data)
            if (b != 0x00)
                return false;
        return true;
    }

    private String hexdumpHead(byte[] data, int n) {
        if (data == null)
            return "null";
        int len = Math.min(n, data.length);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(String.format("%02X ", data[i]));
        }
        return sb.toString().trim();
    }

    private JLabel avatarLabel;
    private JTextArea infoArea;

    private void refreshUserInfoFromCard() {
        try {
            // yêu cầu: đã connect + đã verify PIN
            MemberInfo member = cardComm.getMemberInfo();
            setAvatarToLabel(avatarLabel, member.avatarBytes);

            StringBuilder sb = new StringBuilder();
            sb.append("━━━━━━━━ THÔNG TIN CÁ NHÂN ━━━━━━━━\n\n");
            sb.append(String.format("Họ và tên   : %s\n\n", member.name));
            sb.append(String.format("Ngày sinh   : %s\n\n", member.birthDate));
            sb.append(String.format("Số điện thoại: %s\n\n", member.phone));
            sb.append(String.format("Địa chỉ     : %s\n", member.address));

            infoArea.setText(sb.toString());
            infoArea.setCaretPosition(0);

            log("Đã tải thông tin cá nhân từ thẻ");
        } catch (Exception ex) {
            log("LỖI tải thông tin: " + ex.getMessage());
        }
    }

    private void clearUserInfoUI() {
        if (infoArea != null)
            infoArea.setText("");

        if (avatarLabel != null) {
            avatarLabel.setIcon(null);
            avatarLabel.setText("[Avatar]");
        }
        log("Đã reset UI thông tin cá nhân (chưa tải từ thẻ).");
    }

    private void setAvatarToLabel(JLabel avatarLabel, byte[] avatarBytes) {
        try {
            log("[AVATAR] bytes=" + (avatarBytes == null ? "null" : avatarBytes.length));

            if (avatarBytes == null || avatarBytes.length == 0) {
                avatarLabel.setIcon(null);
                avatarLabel.setText("👤");
                return;
            }

            BufferedImage img = ImageIO.read(new java.io.ByteArrayInputStream(avatarBytes));
            if (img == null) {
                log("[AVATAR] ImageIO.read=null -> bytes not a valid jpg/png");
                avatarLabel.setIcon(null);
                avatarLabel.setText("👤");
                return;
            }

            Image scaled = img.getScaledInstance(120, 120, Image.SCALE_SMOOTH);
            avatarLabel.setText("");
            avatarLabel.setIcon(new ImageIcon(scaled));
            log("[AVATAR] rendered OK: " + img.getWidth() + "x" + img.getHeight());
        } catch (Exception e) {
            log("[AVATAR] exception: " + e.getMessage());
            avatarLabel.setIcon(null);
            avatarLabel.setText("👤");
        }
    }

    /**
     * Tab xem các gói tập - Load từ Database với UI đẹp
     */
    private JPanel createPackageTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
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

        panel.add(currentPackagePanel, BorderLayout.NORTH);

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

            // Icons và colors cho từng loại gói
            String[] icons = { "[1M]", "[3M]", "[6M]", "[1Y]", "[10B]", "[30B]", "[VIP]" };
            Color[] colors = {
                    new Color(46, 204, 113), // Xanh lá
                    new Color(52, 152, 219), // Xanh dương
                    new Color(155, 89, 182), // Tím
                    new Color(230, 126, 34), // Cam
                    new Color(241, 196, 15), // Vàng
                    new Color(231, 76, 60), // Đỏ
                    new Color(142, 68, 173) // Tím đậm (VIP)
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

        panel.add(scrollPane, BorderLayout.CENTER);

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

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Tạo card gói tập từ database với popup thanh toán
     */
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

    /**
     * Hiện popup thanh toán với giảm giá 50% nếu đã có gói
     */
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
            finalPrice = originalPrice * 0.5; // Giảm 50%
            discountInfo = "GIẢM 50% khi nâng cấp!";
        }

        // Tạo dialog - kích thước lớn hơn để chứa tất cả field
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
        contentPanel.add(new JLabel("Số dư hiện tại:"), gbcContent);
        gbcContent.gridx = 1;
        short currentBalance = 0;
        try {
            currentBalance = cardComm.getBalance();
        } catch (Exception ex) {
            // ignore
        }
        JLabel balanceLabel = new JLabel(String.format("%,d VNĐ", currentBalance * 1000));
        balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        contentPanel.add(balanceLabel, gbcContent);

        // PIN field - yêu cầu nhập lại mã PIN để xác thực
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
        short priceInK = (short) (priceToDeduct / 1000);
        final short finalBalance = currentBalance;

        JButton confirmBtn = new JButton("Xác nhận thanh toán");
        confirmBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        confirmBtn.setBackground(color);
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setFocusPainted(false);
        confirmBtn.addActionListener(ev -> {
            try {
                // Xác thực PIN trước
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

                // Verify PIN với thẻ
                boolean pinValid = cardComm.verifyPin(enteredPin);
                if (!pinValid) {
                    JOptionPane.showMessageDialog(dialog,
                            "Mã PIN không chính xác!",
                            "Sai PIN", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Kiểm tra đủ tiền
                if (finalBalance < priceInK) {
                    JOptionPane.showMessageDialog(dialog,
                            "Số dư không đủ! Vui lòng nạp thêm tiền.",
                            "Không đủ tiền", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Trừ tiền
                if (cardComm.deductBalance(priceInK)) {
                    // Cập nhật gói tập
                    byte packageType = 1; // Mặc định là gói tháng
                    if (plan.code.startsWith("SESSION")) {
                        packageType = 2; // Gói theo buổi
                    } else if (plan.code.equals("VIP")) {
                        packageType = 3; // Gói VIP
                    }

                    String today = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
                    Calendar cal = Calendar.getInstance();
                    if (plan.durationDays > 0) {
                        cal.add(Calendar.DAY_OF_MONTH, plan.durationDays);
                    } else {
                        cal.add(Calendar.YEAR, 1); // Gói buổi: 1 năm hết hạn
                    }
                    String expiry = new SimpleDateFormat("dd/MM/yyyy").format(cal.getTime());
                    short sessions = (short) plan.sessionCount;

                    cardComm.setPackage(packageType, expiry, today, sessions);

                    // Cập nhật UI gói tập
                    String pkgInfo = String.format("%s | Đăng ký: %s | Hết hạn: %s",
                            plan.name, today, expiry);
                    if (packageType == 2) {
                        pkgInfo += " | Còn: " + sessions + " buổi";
                    }
                    currentPackageLabel.setText(pkgInfo);

                    // Cập nhật số dư hiển thị
                    short newBalance = cardComm.getBalance();
                    balanceLabel.setText(String.format("%,d VNĐ", newBalance * 1000));

                    log("Đã mua thành công: " + plan.name + " - " + String.format("%,.0f", priceToDeduct) + " VNĐ");
                    JOptionPane.showMessageDialog(dialog,
                            "Mua gói thành công!\n" + plan.name + "\nSố dư còn lại: "
                                    + String.format("%,d VNĐ", newBalance * 1000),
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
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
     * Tab Thống kê - Giao diện cải tiến với grid 2x2 và dịch vụ đã mua
     */
    private JPanel createStatisticsTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(new Color(248, 249, 250));

        // Header
        JLabel headerLabel = new JLabel("THỐNG KÊ HOẠT ĐỘNG");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerLabel.setForeground(new Color(52, 73, 94));
        panel.add(headerLabel, BorderLayout.NORTH);

        // Main grid 2x2
        JPanel gridPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        gridPanel.setBackground(new Color(248, 249, 250));

        // ===== Card 1: Gói tập hiện tại =====
        JPanel packageCard = createStatCard("GÓI TẬP HIỆN TẠI", new Color(52, 152, 219));
        JLabel packageTypeLabel = new JLabel("Chưa có gói tập");
        packageTypeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        JLabel packageExpiryLabel = new JLabel("Hạn: --");
        packageExpiryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JLabel sessionsLabel = new JLabel("");
        sessionsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JPanel packageContent = (JPanel) packageCard.getComponent(1);
        packageContent.add(packageTypeLabel);
        packageContent.add(Box.createVerticalStrut(5));
        packageContent.add(packageExpiryLabel);
        packageContent.add(sessionsLabel);
        gridPanel.add(packageCard);

        // ===== Card 2: Số dư tài khoản =====
        JPanel balanceCard = createStatCard("SỐ DƯ TÀI KHOẢN", new Color(46, 204, 113));
        JLabel balanceValueLabel = new JLabel("0 VNĐ");
        balanceValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        balanceValueLabel.setForeground(new Color(46, 204, 113));

        JPanel balanceContent = (JPanel) balanceCard.getComponent(1);
        balanceContent.add(balanceValueLabel);
        gridPanel.add(balanceCard);

        // ===== Card 3: Hoạt động tập luyện =====
        JPanel activityCard = createStatCard("HOẠT ĐỘNG TẬP LUYỆN", new Color(155, 89, 182));
        JLabel checkInCountLabel = new JLabel("Số buổi tập: 0");
        checkInCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        JLabel lastCheckInLabel = new JLabel("Lần tập gần nhất: --");
        lastCheckInLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JPanel activityContent = (JPanel) activityCard.getComponent(1);
        activityContent.add(checkInCountLabel);
        activityContent.add(Box.createVerticalStrut(5));
        activityContent.add(lastCheckInLabel);
        gridPanel.add(activityCard);

        // ===== Card 4: Dịch vụ đã mua =====
        JPanel servicesCard = createStatCard("DỊCH VỤ ĐÃ MUA", new Color(231, 76, 60));
        DefaultListModel<String> servicesModel = new DefaultListModel<>();
        JList<String> servicesList = new JList<>(servicesModel);
        servicesList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        servicesList.setBackground(new Color(250, 250, 250));
        servicesList.setFixedCellHeight(24);
        JScrollPane servicesScroll = new JScrollPane(servicesList);
        servicesScroll.setPreferredSize(new Dimension(0, 100));
        servicesScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));

        JPanel servicesContent = (JPanel) servicesCard.getComponent(1);
        servicesContent.setLayout(new BorderLayout(5, 5));
        JLabel countLabel = new JLabel("Tổng: 0 dịch vụ");
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        servicesContent.add(countLabel, BorderLayout.NORTH);
        servicesContent.add(servicesScroll, BorderLayout.CENTER);
        gridPanel.add(servicesCard);

        panel.add(gridPanel, BorderLayout.CENTER);

        // Nút tải lại
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(new Color(248, 249, 250));
        JButton refreshBtn = createModernButton("Tải lại thống kê", new Color(52, 152, 219), 14);
        refreshBtn.setPreferredSize(new Dimension(180, 40));
        refreshBtn.addActionListener(e -> {
            try {
                // Load gói tập
                PackageInfo pkg = cardComm.getPackage();
                packageTypeLabel.setText(pkg.getPackageTypeName());
                packageExpiryLabel.setText("Hạn: " + (pkg.expiry.isEmpty() ? "--" : pkg.expiry));
                if (pkg.type == 2 && pkg.remainingSessions > 0) {
                    sessionsLabel.setText("Còn: " + pkg.remainingSessions + " buổi");
                } else {
                    sessionsLabel.setText("");
                }

                // Load số dư
                short balance = cardComm.getBalance();
                balanceValueLabel.setText(String.format("%,d VNĐ", balance * 1000));

                // Load check-in
                int checkInCount = cardComm.getCheckInCount();
                checkInCountLabel.setText("Số buổi tập: " + checkInCount);
                CheckInInfo lastCheckIn = cardComm.getLastCheckIn();
                if (lastCheckIn != null && !lastCheckIn.date.isEmpty()) {
                    lastCheckInLabel.setText("Gần nhất: " + lastCheckIn.date + " " + lastCheckIn.checkInTime);
                } else {
                    lastCheckInLabel.setText("Lần tập gần nhất: --");
                }

                // Load dịch vụ đã mua
                servicesModel.clear();
                for (String svc : purchasedServices) {
                    servicesModel.addElement(svc);
                }
                countLabel.setText("Tổng: " + purchasedServices.size() + " dịch vụ");

            } catch (Exception ex) {
                javax.swing.JOptionPane.showMessageDialog(panel,
                        "Lỗi tải thống kê: " + ex.getMessage(),
                        "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(refreshBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Tạo card thống kê với header gradient
     */
    private JPanel createStatCard(String title, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2, true),
                new EmptyBorder(0, 0, 0, 0)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        // Header
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, color, getWidth(), 0, color.darker());
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        header.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));
        header.setPreferredSize(new Dimension(0, 40));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);
        header.add(titleLabel);
        card.add(header, BorderLayout.NORTH);

        // Content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(15, 15, 15, 15));
        card.add(content, BorderLayout.CENTER);

        return card;
    }

    /**
     * Create package card
     */
    private JPanel createPackageCard(String name, String price, String duration,
            String type, Color color, JLabel currentPackageLabel) {
        JPanel card = new JPanel(new BorderLayout(5, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2, true),
                new EmptyBorder(15, 15, 15, 15)));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nameLabel.setForeground(color);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(nameLabel);

        contentPanel.add(Box.createVerticalStrut(5));

        JLabel priceLabel = new JLabel(price + "k VNĐ");
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        priceLabel.setForeground(new Color(52, 73, 94));
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(priceLabel);

        contentPanel.add(Box.createVerticalStrut(5));

        JLabel durationLabel = new JLabel(duration);
        durationLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        durationLabel.setForeground(new Color(127, 140, 141));
        durationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(durationLabel);

        card.add(contentPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        buttonPanel.setBackground(Color.WHITE);

        JButton buyBtn = createModernButton("Mua gói", color, 14);
        buyBtn.setPreferredSize(new Dimension(130, 36));
        buyBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(card,
                    "Bạn muốn mua " + name + " với giá " + price + "k VNĐ?\n" +
                            "Hệ thống sẽ chuyển bạn đến tab Thanh toán.",
                    "Xác nhận mua gói",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                Container parent = card.getParent();
                while (parent != null && !(parent instanceof JTabbedPane)) {
                    parent = parent.getParent();
                }
                if (parent instanceof JTabbedPane) {
                    ((JTabbedPane) parent).setSelectedIndex(4); // tab Thanh toán
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
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(248, 249, 250));

        JLabel titleLabel = new JLabel("LỊCH TẬP GYM & CHECK-IN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(52, 73, 94));
        titleLabel.setHorizontalAlignment(SwingConstants.LEFT);
        titleLabel.setBorder(new EmptyBorder(0, 5, 5, 5));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 0));
        mainPanel.setBackground(new Color(248, 249, 250));

        // LEFT: Calendar
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(10, 10, 10, 10)));
        leftPanel.setPreferredSize(new Dimension(750, 0));

        JCalendar calendar = new JCalendar();
        calendar.setBackground(Color.WHITE);
        calendar.setWeekOfYearVisible(false);
        calendar.getDayChooser().setFont(new Font("Segoe UI", Font.PLAIN, 13));
        calendar.getMonthChooser().getComboBox().setFont(new Font("Segoe UI", Font.BOLD, 14));
        calendar.getYearChooser().setFont(new Font("Segoe UI", Font.BOLD, 14));

        leftPanel.add(calendar, BorderLayout.CENTER);

        // Decorator & demo data (như cũ)
        CheckInDayDecorator decorator = new CheckInDayDecorator(calendar);
        java.util.Map<String, String[]> checkInTimes = new java.util.HashMap<>();
        checkInTimes.put("02/11/2025", new String[] { "08:00:00", "10:30:00" });
        checkInTimes.put("04/11/2025", new String[] { "07:45:00", "09:15:00" });
        checkInTimes.put("06/11/2025", new String[] { "18:30:00", "20:00:00" });
        checkInTimes.put("09/11/2025", new String[] { "08:15:00", "10:45:00" });
        checkInTimes.put("11/11/2025", new String[] { "19:00:00", "21:00:00" });
        checkInTimes.put("16/11/2025", new String[] { "08:30:00", "10:00:00" });
        checkInTimes.put("18/11/2025", new String[] { "07:30:00", "09:30:00" });
        checkInTimes.put("23/11/2025", new String[] { "17:45:00", "19:45:00" });
        checkInTimes.put("25/11/2025", new String[] { "08:00:00", "10:15:00" });
        checkInTimes.put("28/11/2025", new String[] { "18:00:00", "20:30:00" });

        decorator.addCheckInDates(checkInTimes.keySet());
        decorator.install();

        mainPanel.add(leftPanel, BorderLayout.CENTER);

        // RIGHT: Stats + buttons
        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        rightPanel.setBackground(new Color(248, 249, 250));
        rightPanel.setPreferredSize(new Dimension(260, 0));

        JPanel statsPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        statsPanel.setBackground(new Color(240, 248, 255));
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(52, 152, 219), 2, true),
                new EmptyBorder(15, 15, 15, 15)));

        JLabel statsTitle = new JLabel("THỐNG KÊ");
        statsTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statsTitle.setForeground(new Color(52, 73, 94));
        statsPanel.add(statsTitle);

        JLabel countLabel = new JLabel("Số ngày đã tập: 10 ngày");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statsPanel.add(countLabel);

        JLabel lastLabel = new JLabel("<html>Click vào ngày check-in<br>để xem chi tiết giờ vào - ra</html>");
        lastLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lastLabel.setForeground(new Color(127, 140, 141));
        statsPanel.add(lastLabel);

        calendar.getDayChooser().addPropertyChangeListener("day", evt -> {
            java.util.Calendar selectedCal = calendar.getCalendar();
            String selectedDate = new java.text.SimpleDateFormat("dd/MM/yyyy").format(selectedCal.getTime());
            if (checkInTimes.containsKey(selectedDate)) {
                String[] times = checkInTimes.get(selectedDate);
                lastLabel.setText(String.format(
                        "<html>Ngày: %s<br>Vào: %s | Ra: %s</html>",
                        selectedDate, times[0], times[1]));
                lastLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                lastLabel.setForeground(new Color(52, 73, 94));
                log("Xem chi tiết: " + selectedDate + " - " + times[0] + " -> " + times[1]);
            } else {
                lastLabel.setText("<html>Click vào ngày check-in<br>để xem chi tiết giờ vào - ra</html>");
                lastLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                lastLabel.setForeground(new Color(127, 140, 141));
            }
        });

        rightPanel.add(statsPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        buttonPanel.setBackground(new Color(248, 249, 250));
        buttonPanel.setBorder(new EmptyBorder(5, 0, 0, 0));

        JButton checkInBtn = createModernButton("CHECK-IN", new Color(46, 204, 113), 16);
        checkInBtn.setPreferredSize(new Dimension(0, 60));
        checkInBtn.addActionListener(e -> {
            try {
                String date = new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date());
                String time = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());

                if (cardComm.checkIn(date, time)) {
                    log("Check-in thành công!");

                    CheckInInfo info = cardComm.getLastCheckIn();
                    int count = cardComm.getCheckInCount();

                    countLabel.setText("Số ngày đã tập: " + count + " ngày");
                    lastLabel.setText(String.format(
                            "<html>Lần tập gần nhất:<br>%s<br>Vào: %s | Ra: %s</html>",
                            info.date, info.checkInTime, info.checkOutTime));

                    decorator.addCheckInDate(date);

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

        JButton checkOutBtn = createModernButton("CHECK-OUT", new Color(231, 76, 60), 16);
        checkOutBtn.setPreferredSize(new Dimension(0, 60));
        checkOutBtn.addActionListener(e -> {
            try {
                String time = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());

                if (cardComm.checkOut(time)) {
                    log("Check-out thành công!");

                    CheckInInfo info = cardComm.getLastCheckIn();
                    lastLabel.setText(String.format(
                            "<html>Lần tập gần nhất:<br>%s<br>Vào: %s | Ra: %s</html>",
                            info.date, info.checkInTime, info.checkOutTime));

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

        buttonPanel.add(checkInBtn);
        buttonPanel.add(checkOutBtn);

        rightPanel.add(buttonPanel, BorderLayout.CENTER);

        mainPanel.add(leftPanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);

        panel.add(mainPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Tab thay đổi PIN
     */
    private JPanel createChangePinTab() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(248, 249, 250));
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        // ===== Title =====
        JLabel title = new JLabel("Thay đổi mã PIN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(52, 73, 94));
        title.setBorder(new EmptyBorder(0, 5, 5, 5));
        root.add(title, BorderLayout.NORTH);

        // ===== Card trắng chứa form =====
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(15, 20, 15, 20)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;

        // Info label (mô tả ngắn)
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel infoLabel = new JLabel(
                "<html><b>Gợi ý:</b> Đổi PIN định kỳ để tăng bảo mật.<br>" +
                        "Mã PIN gồm 6 chữ số, dùng để bảo vệ dữ liệu trên thẻ.</html>");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        infoLabel.setForeground(new Color(127, 140, 141));
        panel.add(infoLabel, gbc);

        gbc.gridwidth = 1;

        // Old PIN
        row++;
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel oldPinLabel = new JLabel("Mã PIN hiện tại (6 số):");
        oldPinLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(oldPinLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JPasswordField oldPinField = new JPasswordField(12);
        oldPinField.setFont(new Font("Segoe UI", Font.BOLD, 18));
        oldPinField.setHorizontalAlignment(JTextField.CENTER);
        oldPinField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 2),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        panel.add(oldPinField, gbc);

        // New PIN
        row++;
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel newPinLabel = new JLabel("Mã PIN mới (6 số):");
        newPinLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(newPinLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JPasswordField newPinField = new JPasswordField(12);
        newPinField.setFont(new Font("Segoe UI", Font.BOLD, 18));
        newPinField.setHorizontalAlignment(JTextField.CENTER);
        newPinField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 2),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        panel.add(newPinField, gbc);

        // Confirm PIN
        row++;
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel confirmPinLabel = new JLabel("Xác nhận PIN mới:");
        confirmPinLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(confirmPinLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JPasswordField confirmPinField = new JPasswordField(12);
        confirmPinField.setFont(new Font("Segoe UI", Font.BOLD, 18));
        confirmPinField.setHorizontalAlignment(JTextField.CENTER);
        confirmPinField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 2),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        panel.add(confirmPinField, gbc);

        // Ghi chú dưới cùng
        row++;
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel noteLabel = new JLabel(
                "<html><i>Lưu ý: Sau khi đổi PIN, bạn sử dụng mã PIN mới cho mọi lần đăng nhập.</i></html>");
        noteLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        noteLabel.setForeground(new Color(149, 165, 166));
        panel.add(noteLabel, gbc);

        root.add(panel, BorderLayout.CENTER);

        // ===== Nút đổi PIN (footer) =====
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        bottom.setBackground(new Color(248, 249, 250));
        JButton changeBtn = createModernButton("Thay đổi PIN", new Color(52, 152, 219), 15);
        changeBtn.setPreferredSize(new Dimension(190, 40));

        changeBtn.addActionListener(e -> {
            try {
                String oldPin = new String(oldPinField.getPassword()).trim();
                String newPin = new String(newPinField.getPassword()).trim();
                String confirmPin = new String(confirmPinField.getPassword()).trim();

                if (oldPin.isEmpty() || newPin.isEmpty() || confirmPin.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Vui lòng nhập đầy đủ các trường PIN.",
                            "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (!oldPin.matches("\\d{6}") || !newPin.matches("\\d{6}")) {
                    JOptionPane.showMessageDialog(this,
                            "Mã PIN phải gồm đúng 6 chữ số (0-9)!",
                            "PIN không hợp lệ", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!newPin.equals(confirmPin)) {
                    JOptionPane.showMessageDialog(this,
                            "Mã PIN mới và xác nhận PIN không khớp!",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!cardComm.isConnected()) {
                    log("Vui lòng kết nối thẻ trước khi đổi PIN!");
                    JOptionPane.showMessageDialog(this,
                            "Vui lòng kết nối thẻ!",
                            "Lỗi", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (cardComm.changePin(oldPin, newPin)) {
                    log("Đã thay đổi PIN thành công (dữ liệu được mã hóa lại bằng PIN mới).");
                    JOptionPane.showMessageDialog(this,
                            "Thay đổi mã PIN thành công!\n" +
                                    "Dữ liệu trên thẻ đã được bảo vệ bằng mã PIN mới.",
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);

                    oldPinField.setText("");
                    newPinField.setText("");
                    confirmPinField.setText("");
                } else {
                    log("Thay đổi PIN thất bại (sai PIN hiện tại?)");
                    JOptionPane.showMessageDialog(this,
                            "Thay đổi PIN thất bại!\n" +
                                    "Vui lòng kiểm tra lại mã PIN hiện tại.",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception ex) {
                log("LỖI khi đổi PIN: " + ex.getMessage());
                JOptionPane.showMessageDialog(this,
                        "Lỗi thay đổi PIN: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        bottom.add(changeBtn);
        root.add(bottom, BorderLayout.SOUTH);

        return root;
    }

    /**
     * Tab thanh toán - Thiết kế mới với ScrollPane và layout đẹp hơn
     */
    private JPanel createPaymentTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(new Color(248, 249, 250));

        // ===== Main content trong ScrollPane để không bị cắt =====
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(new Color(248, 249, 250));

        // ===== CARD SỐ DƯ - Thiết kế premium với gradient và shadow =====
        JPanel balanceCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Shadow effect
                g2d.setColor(new Color(0, 0, 0, 30));
                g2d.fillRoundRect(4, 4, w - 4, h - 4, 20, 20);

                // Gradient background - màu vàng cam sang trọng
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(241, 196, 15),
                        w, h, new Color(230, 126, 34));
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, w - 4, h - 4, 20, 20);

                // Decorative circles
                g2d.setColor(new Color(255, 255, 255, 30));
                g2d.fillOval(w - 120, -40, 150, 150);
                g2d.fillOval(w - 80, 40, 100, 100);

                g2d.dispose();
            }
        };
        balanceCard.setLayout(new BorderLayout());
        balanceCard.setOpaque(false);
        balanceCard.setPreferredSize(new Dimension(0, 130));
        balanceCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        balanceCard.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Balance content
        JPanel balanceContent = new JPanel(new GridBagLayout());
        balanceContent.setOpaque(false);
        GridBagConstraints bgbc = new GridBagConstraints();

        // Left side - labels
        JPanel leftBalance = new JPanel();
        leftBalance.setLayout(new BoxLayout(leftBalance, BoxLayout.Y_AXIS));
        leftBalance.setOpaque(false);

        JLabel balanceTitle = new JLabel("SỐ DƯ TÀI KHOẢN");
        balanceTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        balanceTitle.setForeground(new Color(255, 255, 255, 200));
        balanceTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel balanceLabel = new JLabel("0 VNĐ");
        balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        balanceLabel.setForeground(Color.WHITE);
        balanceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel balanceHint = new JLabel("Nhấn 'Tải lại' để cập nhật số dư từ thẻ");
        balanceHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        balanceHint.setForeground(new Color(255, 255, 255, 180));
        balanceHint.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftBalance.add(balanceTitle);
        leftBalance.add(Box.createVerticalStrut(5));
        leftBalance.add(balanceLabel);
        leftBalance.add(Box.createVerticalStrut(3));
        leftBalance.add(balanceHint);

        bgbc.gridx = 0;
        bgbc.gridy = 0;
        bgbc.weightx = 1.0;
        bgbc.anchor = GridBagConstraints.WEST;
        balanceContent.add(leftBalance, bgbc);

        // Right side - reload button
        JButton reloadBtn = new JButton("Tải lại") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 40));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        reloadBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        reloadBtn.setForeground(Color.WHITE);
        reloadBtn.setFocusPainted(false);
        reloadBtn.setBorderPainted(false);
        reloadBtn.setContentAreaFilled(false);
        reloadBtn.setOpaque(false);
        reloadBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        reloadBtn.setPreferredSize(new Dimension(100, 36));
        reloadBtn.addActionListener(e -> {
            try {
                short balance = cardComm.getBalance();
                balanceLabel.setText(String.format("%,d VNĐ", balance * 1000));
                log("Số dư: " + balance + " nghìn VNĐ");
            } catch (Exception ex) {
                log("LỖI: " + ex.getMessage());
                JOptionPane.showMessageDialog(this,
                        "Lỗi tải số dư: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        bgbc.gridx = 1;
        bgbc.weightx = 0;
        bgbc.anchor = GridBagConstraints.EAST;
        balanceContent.add(reloadBtn, bgbc);

        balanceCard.add(balanceContent, BorderLayout.CENTER);
        mainContent.add(balanceCard);
        mainContent.add(Box.createVerticalStrut(20));

        // ===== HAI CARD: NẠP TIỀN và THANH TOÁN =====
        JPanel cardsRow = new JPanel(new GridLayout(1, 2, 20, 0));
        cardsRow.setBackground(new Color(248, 249, 250));
        cardsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        // Card Nạp tiền
        JPanel addCard = createStyledAddBalanceCard(balanceLabel);
        // Card Thanh toán
        JPanel payCard = createStyledPaymentServiceCard(balanceLabel);

        cardsRow.add(addCard);
        cardsRow.add(payCard);

        mainContent.add(cardsRow);
        mainContent.add(Box.createVerticalGlue());

        // Wrap trong ScrollPane
        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Tab Nạp tiền - Chỉ chứa phần nạp tiền
     */
    private JPanel createTopUpTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(248, 249, 250));

        // Header: Số dư hiện tại
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(46, 204, 113));
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("SỐ DƯ TÀI KHOẢN");
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(255, 255, 255, 200));

        JLabel balanceLabel = new JLabel("0 VNĐ");
        balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        balanceLabel.setForeground(Color.WHITE);

        JPanel balancePanel = new JPanel();
        balancePanel.setOpaque(false);
        balancePanel.setLayout(new BoxLayout(balancePanel, BoxLayout.Y_AXIS));
        balancePanel.add(titleLabel);
        balancePanel.add(balanceLabel);

        headerPanel.add(balancePanel, BorderLayout.WEST);

        JButton refreshBtn = new JButton("Tải lại");
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setBackground(new Color(39, 174, 96));
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> {
            try {
                short balance = cardComm.getBalance();
                balanceLabel.setText(String.format("%,d VNĐ", balance * 1000));
                log("Số dư: " + balance + " nghìn VNĐ");
            } catch (Exception ex) {
                log("LỖI: " + ex.getMessage());
            }
        });
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Content: Nạp tiền
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Quick amounts
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        JLabel quickLabel = new JLabel("Chọn mức nạp nhanh:");
        quickLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        content.add(quickLabel, gbc);

        JSpinner amountSpinner = new JSpinner(new SpinnerNumberModel(100, 10, 10000, 10));
        amountSpinner.setFont(new Font("Segoe UI", Font.BOLD, 18));

        String[] quickAmounts = { "50k", "100k", "200k", "500k" };
        int[] quickValues = { 50, 100, 200, 500 };
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        for (int i = 0; i < quickAmounts.length; i++) {
            gbc.gridx = i;
            final int val = quickValues[i];
            JButton qBtn = createModernButton(quickAmounts[i], new Color(46, 204, 113), 14);
            qBtn.setPreferredSize(new Dimension(80, 40));
            qBtn.addActionListener(e -> amountSpinner.setValue(val));
            content.add(qBtn, gbc);
        }

        // Input
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 4;
        JLabel inputLabel = new JLabel("Hoặc nhập số tiền (nghìn VNĐ):");
        inputLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        content.add(inputLabel, gbc);

        gbc.gridy = 3;
        content.add(amountSpinner, gbc);

        // Nút nạp
        gbc.gridy = 4;
        gbc.insets = new Insets(25, 10, 10, 10);
        JButton topUpBtn = createModernButton("NẠP TIỀN", new Color(46, 204, 113), 16);
        topUpBtn.setPreferredSize(new Dimension(0, 50));
        topUpBtn.addActionListener(e -> {
            // Tạo dialog với JPasswordField
            JPasswordField passField = new JPasswordField(10);
            JPanel passPanel = new JPanel(new BorderLayout(5, 5));
            passPanel.add(new JLabel("Nhập mật khẩu Admin:"), BorderLayout.NORTH);
            passPanel.add(passField, BorderLayout.CENTER);

            int result = javax.swing.JOptionPane.showConfirmDialog(panel, passPanel,
                    "Xác thực Admin", javax.swing.JOptionPane.OK_CANCEL_OPTION, javax.swing.JOptionPane.PLAIN_MESSAGE);

            if (result != javax.swing.JOptionPane.OK_OPTION)
                return;

            String adminPass = new String(passField.getPassword()).trim();
            if (adminPass.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(panel, "Vui lòng nhập mật khẩu!", "Lỗi",
                        javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!adminPass.equals("123456")) {
                javax.swing.JOptionPane.showMessageDialog(panel, "Mật khẩu Admin không chính xác!", "Sai mật khẩu",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                short amount = ((Number) amountSpinner.getValue()).shortValue();
                if (cardComm.addBalance(amount)) {
                    short newBalance = cardComm.getBalance();
                    balanceLabel.setText(String.format("%,d VNĐ", newBalance * 1000));
                    javax.swing.JOptionPane.showMessageDialog(panel,
                            "Nạp tiền thành công!\n\nSố tiền: " + String.format("%,d", amount * 1000)
                                    + " VNĐ\nSố dư mới: " + String.format("%,d", newBalance * 1000) + " VNĐ",
                            "Thành công", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                } else {
                    javax.swing.JOptionPane.showMessageDialog(panel, "Nạp tiền thất bại!", "Lỗi",
                            javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                javax.swing.JOptionPane.showMessageDialog(panel, "Lỗi: " + ex.getMessage(), "Lỗi",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        });
        content.add(topUpBtn, gbc);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Tab Dịch vụ thêm - Mua dịch vụ như PT, đồ uống, khăn...
     */
    private JPanel createServicesTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(248, 249, 250));

        // Header
        JLabel headerLabel = new JLabel("DỊCH VỤ THÊM");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setForeground(new Color(155, 89, 182));
        panel.add(headerLabel, BorderLayout.NORTH);

        // Services grid
        JPanel servicesPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        servicesPanel.setBackground(new Color(248, 249, 250));

        String[][] services = {
                { "PT 1 buổi", "150", "155, 89, 182" },
                { "Nước uống", "20", "52, 152, 219" },
                { "Khăn tập", "10", "46, 204, 113" },
                { "Tủ đồ tháng", "100", "241, 196, 15" },
                { "Massage 30p", "200", "231, 76, 60" },
                { "Bổ sung dinh dưỡng", "50", "26, 188, 156" }
        };

        for (String[] svc : services) {
            JPanel svcCard = new JPanel(new BorderLayout(5, 5));
            svcCard.setBackground(Color.WHITE);
            svcCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                    new EmptyBorder(15, 15, 15, 15)));

            JLabel nameLabel = new JLabel(svc[0]);
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

            JLabel priceLabel = new JLabel(svc[1] + "k VNĐ");
            priceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            priceLabel.setForeground(Color.GRAY);

            String[] rgb = svc[2].split(", ");
            Color svcColor = new Color(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2]));

            JButton buyBtn = new JButton("Mua");
            buyBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            buyBtn.setBackground(svcColor);
            buyBtn.setForeground(Color.WHITE);
            buyBtn.setFocusPainted(false);
            buyBtn.setBorderPainted(false);
            final String svcName = svc[0];
            final short svcPrice = Short.parseShort(svc[1]);
            buyBtn.addActionListener(e -> {
                try {
                    short currentBalance = cardComm.getBalance();
                    if (currentBalance < svcPrice) {
                        javax.swing.JOptionPane.showMessageDialog(panel,
                                "Không đủ tiền!\n\nSố dư: " + String.format("%,d", currentBalance * 1000)
                                        + " VNĐ\nCần: " + String.format("%,d", svcPrice * 1000) + " VNĐ",
                                "Không đủ tiền", javax.swing.JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    if (cardComm.deductBalance(svcPrice)) {
                        short newBalance = cardComm.getBalance();
                        String time = new SimpleDateFormat("HH:mm dd/MM").format(new Date());
                        purchasedServices.add(svcName + " - " + svcPrice + "k (" + time + ")");
                        javax.swing.JOptionPane.showMessageDialog(panel,
                                "Mua dịch vụ thành công!\n\nDịch vụ: " + svcName + "\nGiá: "
                                        + String.format("%,d", svcPrice * 1000) + " VNĐ\nSố dư còn: "
                                        + String.format("%,d", newBalance * 1000) + " VNĐ",
                                "Thành công", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        javax.swing.JOptionPane.showMessageDialog(panel, "Mua dịch vụ thất bại!", "Lỗi",
                                javax.swing.JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    javax.swing.JOptionPane.showMessageDialog(panel, "Lỗi: " + ex.getMessage(), "Lỗi",
                            javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            });

            JPanel infoPanel = new JPanel();
            infoPanel.setOpaque(false);
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.add(nameLabel);
            infoPanel.add(priceLabel);

            svcCard.add(infoPanel, BorderLayout.CENTER);
            svcCard.add(buyBtn, BorderLayout.EAST);

            servicesPanel.add(svcCard);
        }

        JScrollPane scroll = new JScrollPane(servicesPanel);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Styled Add Balance Card - Thiết kế mới đẹp hơn
     */
    private JPanel createStyledAddBalanceCard(JLabel balanceLabel) {
        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(46, 204, 113), 0),
                new EmptyBorder(0, 0, 0, 0)));

        // Header với gradient
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(46, 204, 113), getWidth(), 0,
                        new Color(39, 174, 96));
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight() + 10, 15, 15);
                g2d.dispose();
            }
        };
        header.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 12));
        header.setPreferredSize(new Dimension(0, 50));

        JLabel titleLabel = new JLabel("NẠP TIỀN VÀO TÀI KHOẢN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);
        header.add(titleLabel);

        card.add(header, BorderLayout.NORTH);

        // Content
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Quick amounts - các mức nạp nhanh
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        JLabel quickLabel = new JLabel("Chọn nhanh:");
        quickLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        quickLabel.setForeground(new Color(127, 140, 141));
        content.add(quickLabel, gbc);

        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;

        JSpinner amountSpinner = new JSpinner(new SpinnerNumberModel(100, 10, 10000, 10));
        amountSpinner.setFont(new Font("Segoe UI", Font.BOLD, 16));
        JComponent editor = amountSpinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) editor).getTextField().setBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(46, 204, 113), 2),
                            BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        }

        String[] quickAmounts = { "50k", "100k", "200k" };
        int[] quickValues = { 50, 100, 200 };

        for (int i = 0; i < quickAmounts.length; i++) {
            gbc.gridx = i;
            final int val = quickValues[i];
            JButton quickBtn = new JButton(quickAmounts[i]) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (getModel().isPressed()) {
                        g2d.setColor(new Color(46, 204, 113));
                    } else if (getModel().isRollover()) {
                        g2d.setColor(new Color(46, 204, 113, 40));
                    } else {
                        g2d.setColor(new Color(46, 204, 113, 20));
                    }
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2d.dispose();
                    super.paintComponent(g);
                }
            };
            quickBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            quickBtn.setForeground(new Color(46, 204, 113));
            quickBtn.setFocusPainted(false);
            quickBtn.setBorderPainted(false);
            quickBtn.setContentAreaFilled(false);
            quickBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            quickBtn.setPreferredSize(new Dimension(60, 32));
            quickBtn.addActionListener(e -> amountSpinner.setValue(val));
            content.add(quickBtn, gbc);
        }

        // Amount input
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        JLabel amountLabel = new JLabel("Hoặc nhập số tiền (nghìn VNĐ):");
        amountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        content.add(amountLabel, gbc);

        gbc.gridy = 3;
        amountSpinner.setPreferredSize(new Dimension(0, 44));
        content.add(amountSpinner, gbc);

        // Nút nạp tiền
        gbc.gridy = 4;
        gbc.insets = new Insets(20, 5, 5, 5);
        JButton addBtn = createModernButton("Nạp tiền ngay", new Color(46, 204, 113), 15);
        addBtn.setPreferredSize(new Dimension(0, 45));
        addBtn.addActionListener(e -> {
            // Hiện popup yêu cầu mật khẩu admin
            JPasswordField adminPinField = new JPasswordField(10);
            JPanel adminPanel = new JPanel(new GridBagLayout());
            GridBagConstraints adminGbc = new GridBagConstraints();
            adminGbc.insets = new Insets(5, 5, 5, 5);
            adminGbc.gridx = 0;
            adminGbc.gridy = 0;
            adminPanel.add(new JLabel("Nhập mật khẩu Admin:"), adminGbc);
            adminGbc.gridy = 1;
            adminGbc.fill = GridBagConstraints.HORIZONTAL;
            adminPanel.add(adminPinField, adminGbc);
            adminGbc.gridy = 2;
            JLabel hintLabel = new JLabel("<html><i>(Mặc định: ADMIN)</i></html>");
            hintLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            hintLabel.setForeground(Color.GRAY);
            adminPanel.add(hintLabel, adminGbc);

            int result = JOptionPane.showConfirmDialog(card, adminPanel,
                    "Xác thực Admin để nạp tiền", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result != JOptionPane.OK_OPTION) {
                return;
            }

            String adminPass = new String(adminPinField.getPassword()).trim();
            if (adminPass.isEmpty()) {
                JOptionPane.showMessageDialog(card, "Vui lòng nhập mật khẩu Admin!", "Lỗi",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                // Xác thực admin: chỉ cần so sánh mật khẩu (không dùng unlockPin vì nó reset
                // authenticated)
                if (!adminPass.equals("123456")) {
                    JOptionPane.showMessageDialog(card, "Mật khẩu Admin không chính xác!", "Sai mật khẩu",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                short amount = ((Number) amountSpinner.getValue()).shortValue();
                if (cardComm.addBalance(amount)) {
                    log("Nạp tiền thành công: " + amount + " nghìn VNĐ");
                    short newBalance = cardComm.getBalance();
                    balanceLabel.setText(String.format("%,d VNĐ", newBalance * 1000));
                    JOptionPane.showMessageDialog(card,
                            "Nạp tiền thành công!\nSố dư mới: " + String.format("%,d", newBalance * 1000) + " VNĐ",
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(card, "Nạp tiền thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(card, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        content.add(addBtn, gbc);

        card.add(content, BorderLayout.CENTER);

        // Thêm shadow cho toàn bộ card
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 3, new Color(0, 0, 0, 20)),
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1)));

        return card;
    }

    /**
     * Styled Payment Service Card - Thiết kế mới đẹp hơn
     */
    private JPanel createStyledPaymentServiceCard(JLabel balanceLabel) {
        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setBackground(Color.WHITE);

        // Header với gradient đỏ
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(231, 76, 60), getWidth(), 0,
                        new Color(192, 57, 43));
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight() + 10, 15, 15);
                g2d.dispose();
            }
        };
        header.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 12));
        header.setPreferredSize(new Dimension(0, 50));

        JLabel titleLabel = new JLabel("THANH TOÁN DỊCH VỤ");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);
        header.add(titleLabel);

        card.add(header, BorderLayout.NORTH);

        // Content
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(15, 20, 15, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        String[][] services = {
                { "HLV riêng (1 buổi)", "200" },
                { "Nước uống", "20" },
                { "Khăn tập", "10" },
                { "Protein shake", "50" },
                { "Tư vấn dinh dưỡng", "100" }
        };

        // Service combo với style đẹp
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel serviceLabel = new JLabel("Chọn dịch vụ:");
        serviceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        content.add(serviceLabel, gbc);

        gbc.gridy = 1;
        JComboBox<String> serviceCombo = new JComboBox<>();
        for (String[] service : services) {
            serviceCombo.addItem(service[0] + " - " + service[1] + "k");
        }
        serviceCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        serviceCombo.setPreferredSize(new Dimension(0, 38));
        serviceCombo.setBorder(BorderFactory.createLineBorder(new Color(231, 76, 60), 2));
        content.add(serviceCombo, gbc);

        // Quantity
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 5, 5, 5);
        JLabel quantityLabel = new JLabel("Số lượng:");
        quantityLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        content.add(quantityLabel, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(5, 5, 5, 5);
        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
        quantitySpinner.setFont(new Font("Segoe UI", Font.BOLD, 14));
        quantitySpinner.setPreferredSize(new Dimension(0, 38));
        content.add(quantitySpinner, gbc);

        // Total panel với highlight
        gbc.gridy = 4;
        gbc.insets = new Insets(12, 5, 5, 5);
        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setBackground(new Color(255, 245, 238));
        totalPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(231, 76, 60), 2),
                new EmptyBorder(10, 15, 10, 15)));

        JLabel totalLabel = new JLabel("Tổng: 200,000 VNĐ");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalLabel.setForeground(new Color(192, 57, 43));
        totalPanel.add(totalLabel, BorderLayout.CENTER);
        content.add(totalPanel, gbc);

        // Update total
        Runnable updateTotal = () -> {
            int idx = serviceCombo.getSelectedIndex();
            if (idx >= 0) {
                int price = Integer.parseInt(services[idx][1]);
                int qty = (Integer) quantitySpinner.getValue();
                totalLabel.setText(String.format("Tổng: %,d VNĐ", price * qty * 1000));
            }
        };
        serviceCombo.addActionListener(e -> updateTotal.run());
        quantitySpinner.addChangeListener(e -> updateTotal.run());

        // Nút thanh toán
        gbc.gridy = 5;
        gbc.insets = new Insets(15, 5, 5, 5);
        JButton payBtn = createModernButton("Thanh toán", new Color(231, 76, 60), 15);
        payBtn.setPreferredSize(new Dimension(0, 45));
        payBtn.addActionListener(e -> {
            try {
                int idx = serviceCombo.getSelectedIndex();
                int price = Integer.parseInt(services[idx][1]);
                int qty = (Integer) quantitySpinner.getValue();
                short total = (short) (price * qty);

                if (cardComm.deductBalance(total)) {
                    log("Thanh toán thành công: " + total + " nghìn VNĐ");
                    short newBalance = cardComm.getBalance();
                    balanceLabel.setText(String.format("%,d VNĐ", newBalance * 1000));
                    JOptionPane.showMessageDialog(card,
                            "Thanh toán thành công!\n" +
                                    "Dịch vụ: " + services[idx][0] + "\n" +
                                    "Số lượng: " + qty + "\n" +
                                    "Tổng tiền: " + String.format("%,d", total * 1000) + " VNĐ\n" +
                                    "Số dư còn lại: " + String.format("%,d", newBalance * 1000) + " VNĐ",
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    quantitySpinner.setValue(1);
                    updateTotal.run();
                } else {
                    JOptionPane.showMessageDialog(card, "Số dư không đủ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(card, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        content.add(payBtn, gbc);

        card.add(content, BorderLayout.CENTER);

        // Shadow
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 3, new Color(0, 0, 0, 20)),
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1)));

        return card;
    }

    /**
     * Create add balance card
     */
    private JPanel createAddBalanceCard(JLabel balanceLabel) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(46, 204, 113), 2, true),
                new EmptyBorder(15, 15, 15, 15)));

        // Title
        JLabel titleLabel = new JLabel("Nạp tiền vào tài khoản");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(46, 204, 113));
        card.add(titleLabel, BorderLayout.NORTH);

        // Content
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel amountLabel = new JLabel("Số tiền (nghìn VNĐ):");
        amountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        contentPanel.add(amountLabel, gbc);

        gbc.gridy = 1;
        JSpinner amountSpinner = new JSpinner(new SpinnerNumberModel(100, 10, 10000, 10));
        amountSpinner.setFont(new Font("Segoe UI", Font.BOLD, 16));
        JComponent editor = amountSpinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) editor).getTextField().setBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(189, 195, 199), 2),
                            BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        }
        amountSpinner.setPreferredSize(new Dimension(140, 36));
        contentPanel.add(amountSpinner, gbc);

        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(16, 8, 8, 8);
        JButton addBtn = createModernButton("Nạp tiền", new Color(46, 204, 113), 15);
        addBtn.setPreferredSize(new Dimension(170, 40));
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
                    JOptionPane.showMessageDialog(card,
                            "Nạp tiền thất bại!",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
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
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(231, 76, 60), 2, true),
                new EmptyBorder(15, 15, 15, 15)));

        // Title
        JLabel titleLabel = new JLabel("Thanh toán dịch vụ");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(231, 76, 60));
        card.add(titleLabel, BorderLayout.NORTH);

        // Service list panel
        JPanel serviceListPanel = new JPanel();
        serviceListPanel.setLayout(new BoxLayout(serviceListPanel, BoxLayout.Y_AXIS));
        serviceListPanel.setBackground(Color.WHITE);

        String[][] services = {
                { "HLV riêng", "200" },
                { "Nước uống", "20" },
                { "Khăn tập", "10" },
                { "Protein shake", "50" },
                { "Dinh dưỡng", "100" }
        };

        // Service combo
        serviceListPanel.add(Box.createVerticalStrut(5));
        JLabel serviceLabel = new JLabel("Chọn dịch vụ:");
        serviceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        serviceListPanel.add(serviceLabel);

        JComboBox<String> serviceCombo = new JComboBox<>();
        for (String[] service : services) {
            serviceCombo.addItem(service[0] + " - " + service[1] + "k");
        }
        serviceCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        serviceCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        serviceCombo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 2),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        serviceListPanel.add(serviceCombo);

        serviceListPanel.add(Box.createVerticalStrut(10));

        // Quantity
        JLabel quantityLabel = new JLabel("Số lượng:");
        quantityLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        serviceListPanel.add(quantityLabel);

        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
        quantitySpinner.setFont(new Font("Segoe UI", Font.BOLD, 16));
        quantitySpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JComponent editor = quantitySpinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) editor).getTextField().setBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(189, 195, 199), 2),
                            BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        }
        serviceListPanel.add(quantitySpinner);

        serviceListPanel.add(Box.createVerticalStrut(12));

        // Total amount display
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        totalPanel.setBackground(new Color(255, 250, 240));
        totalPanel.setBorder(BorderFactory.createLineBorder(new Color(243, 156, 18), 2, true));
        JLabel totalLabel = new JLabel("Tổng tiền: 200k VNĐ");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
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
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        buttonPanel.setBackground(Color.WHITE);
        JButton payBtn = createModernButton("Thanh toán", new Color(231, 76, 60), 15);
        payBtn.setPreferredSize(new Dimension(170, 40));
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
                    updateTotal.run();
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
     * Log message ra terminal
     */
    private void log(String message) {
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        System.out.println(String.format("[%s] %s", timestamp, message));
    }
}
