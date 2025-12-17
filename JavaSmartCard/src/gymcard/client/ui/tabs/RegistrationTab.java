package gymcard.client.ui.tabs;

import gymcard.client.CardCommunicator;
import gymcard.client.ui.BaseTabPanel;
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
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Tab đăng ký hội viên mới (khởi tạo thẻ + lưu thông tin cơ bản)
 */
public class RegistrationTab extends BaseTabPanel {

    private JTextField nameField;
    private JTextField birthDateField;
    private JTextField phoneField;
    private JTextArea addressArea;
    private JPasswordField pinField;
    private JPasswordField confirmPinField;
    private JLabel avatarLabel;
    private String avatarPath;

    public RegistrationTab(CardCommunicator cardComm) {
        super(cardComm);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(new Color(248, 249, 250));

        // MAIN CONTENT
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(new Color(248, 249, 250));

        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(15, 15, 15, 15)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        Font labelFont = new Font("Segoe UI", Font.PLAIN, 13);
        Font inputFont = new Font("Segoe UI", Font.PLAIN, 13);

        // HỌ TÊN
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
        nameField.setBorder(fieldBorder());
        formPanel.add(nameField, gbc);

        // NGÀY SINH
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
        birthDateField = new JTextField(15);
        birthDateField.setFont(inputFont);
        birthDateField.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        birthDateField.setBorder(fieldBorder());
        formPanel.add(birthDateField, gbc);

        // SỐ ĐIỆN THOẠI
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
        phoneField.setBorder(fieldBorder());
        formPanel.add(phoneField, gbc);

        // ĐỊA CHỈ
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
        addressArea = new JTextArea(2, 25);
        addressArea.setFont(inputFont);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        addressArea.setBorder(fieldBorder());
        JScrollPane addressScroll = new JScrollPane(addressArea);
        addressScroll.setBorder(null);
        formPanel.add(addressScroll, gbc);

        // PIN
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
        pinField.setBorder(fieldBorder());
        formPanel.add(pinField, gbc);

        // XÁC NHẬN PIN
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
        confirmPinField.setBorder(fieldBorder());
        formPanel.add(confirmPinField, gbc);

        // AVATAR
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

        // GHI CHÚ BẢO MẬT
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

        JScrollPane formScroll = new JScrollPane(formPanel);
        formScroll.setBorder(null);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);
        contentPanel.add(formScroll, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);

        // NÚT BÊN DƯỚI
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

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void chooseAvatarImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn ảnh đại diện");
        chooser.setFileFilter(new FileNameExtensionFilter("Ảnh (JPG, PNG)", "jpg", "jpeg", "png"));

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            avatarPath = file.getAbsolutePath();

            try {
                BufferedImage img = ImageIO.read(file);
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

        int target = 256;
        float quality = 0.85f;

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
                quality = 0.85f;
            } else {
                return null;
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
            String birthDate = birthDateField.getText().trim();
            String phone = phoneField.getText().trim();
            String address = addressArea.getText().trim();
            String pin = new String(pinField.getPassword()).trim();
            String confirmPin = new String(confirmPinField.getPassword()).trim();

            if (name.isEmpty() || birthDate.isEmpty() || phone.isEmpty() || pin.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng điền đầy đủ các trường bắt buộc (*)",
                        "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Validate: Cho phep chu cai (Tieng Viet co dau), so va khoang trang
            // Regex: \p{L} = Unicode letter (bao gom tieng Viet), \s = khoang trang
            if (!name.matches("[\\p{L}0-9\\s]+")) {
                JOptionPane.showMessageDialog(this,
                        "Ho ten chi duoc chua chu cai (bao gom tieng Viet), so va khoang trang.\nKhong duoc chua ky tu dac biet!",
                        "Ho ten khong hop le", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validate phone: Chỉ số, 10-11 chữ số
            if (!phone.matches("[0-9]{10,11}")) {
                JOptionPane.showMessageDialog(this,
                        "Số điện thoại phải gồm 10-11 chữ số (0-9).\nKhông được chứa ký tự khác!",
                        "Số điện thoại không hợp lệ", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validate address: Cho phep chu (tieng Viet), so, khoang trang, dau phay, dau
            // cham
            if (!address.isEmpty() && !address.matches("[\\p{L}0-9\\s,./]+")) {
                JOptionPane.showMessageDialog(this,
                        "Dia chi chi duoc chua chu cai (bao gom tieng Viet), so, khoang trang va dau (,./)\nKhong duoc chua ky tu dac biet khac!",
                        "Dia chi khong hop le", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validate birth date format: D/M/YYYY or DD/MM/YYYY (1 or 2 digits for
            // day/month)
            if (!birthDate.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
                JOptionPane.showMessageDialog(this,
                        "Ngay sinh phai dung dinh dang D/M/YYYY hoac DD/MM/YYYY\nVi du: 7/2/1990 hoac 15/06/1990",
                        "Ngay sinh khong hop le", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validate birth date: Parse and check not in future
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("d/M/yyyy");
                sdf.setLenient(false); // Strict parsing
                java.util.Date birthDateParsed = sdf.parse(birthDate);
                java.util.Date today = new java.util.Date();

                if (birthDateParsed.after(today)) {
                    JOptionPane.showMessageDialog(this,
                            "Ngay sinh khong duoc vuot qua ngay hien tai!",
                            "Ngay sinh khong hop le", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (java.text.ParseException e) {
                JOptionPane.showMessageDialog(this,
                        "Ngay sinh khong hop le!\nVui long nhap dung dinh dang D/M/YYYY\nVi du: 7/2/1990 hoac 15/06/1990",
                        "Ngay sinh khong hop le", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validate PIN
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

            // Sinh CardID unique (GYM000001, GYM000002, ...)
            String cardId = gymcard.CardManager.CardIdGenerator.nextId();
            log("Đang khởi tạo thẻ (INIT_CARD) với CardID = " + cardId + " ...");
            cardComm.initNewCard(cardId, pin);
            log("Khởi tạo thẻ thành công.");

            log("Đang xác thực PIN...");
            if (!cardComm.verifyPin(pin)) {
                log("Xác thực PIN thất bại sau INIT_CARD!");
                JOptionPane.showMessageDialog(this,
                        "PIN không đúng hoặc thẻ chưa sẵn sàng.\nVui lòng thử lại.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            log("Xác thực PIN OK.");

            byte[] avatarBytes = null;
            if (avatarPath != null) {
                avatarBytes = compressAvatarToCardSize(avatarPath, 4096);
                if (avatarBytes == null) {
                    log("Không thể nén avatar xuống <= 4096 bytes, bỏ qua lưu avatar.");
                } else {
                    log("Avatar đã nén: " + avatarBytes.length + " bytes.");
                }
            }

            log("Đang lưu thông tin hội viên (mã hóa AES trên thẻ)...");
            boolean ok = cardComm.setMemberInfo(name, birthDate, phone, address, avatarBytes);
            if (!ok) {
                log("Lưu thông tin hội viên thất bại");
                JOptionPane.showMessageDialog(this,
                        "Không thể lưu thông tin hội viên lên thẻ.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Lưu card public key vào database để xác thực RSA sau này
            log("Đang lưu RSA public key của thẻ vào database...");
            try {
                long userId = cardComm.saveCardPublicKeyToDb(cardId);
                if (userId > 0) {
                    log("Đã lưu public key vào DB thành công (userId=" + userId + ")");
                } else if (userId == 0) {
                    log("User đã tồn tại trong DB, bỏ qua lưu public key.");
                } else {
                    log("Lưu public key thất bại!");
                }
            } catch (Exception pubKeyEx) {
                log("Cảnh báo: Không thể lưu public key vào DB: " + pubKeyEx.getMessage());
                // Tiếp tục vì đây không phải lỗi nghiêm trọng
            }

            log("Đăng ký hội viên & khởi tạo thẻ thành công!");
            JOptionPane.showMessageDialog(this,
                    "Đăng ký hội viên mới và khởi tạo thẻ thành công!\n\n" +
                            "Hội viên: " + name + "\n" +
                            "Mã thẻ (CardID): " + cardId + "\n" +
                            "Mã PIN: " + pin + "\n\n" +
                            "• CardID dùng để quản lý trong hệ thống.\n" +
                            "• PIN dùng để hội viên check-in và bảo vệ dữ liệu trên thẻ.\n" +
                            "• RSA public key đã được lưu để xác thực thẻ.\n",
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

    private void clearForm() {
        nameField.setText("");
        birthDateField.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        phoneField.setText("");
        addressArea.setText("");
        pinField.setText("");
        confirmPinField.setText("");
        avatarLabel.setIcon(null);
        avatarLabel.setText("Chưa chọn ảnh");
        avatarPath = null;
    }
}
