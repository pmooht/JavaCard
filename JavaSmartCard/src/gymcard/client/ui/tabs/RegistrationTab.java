package gymcard.client.ui.tabs;

import gymcard.client.CardCommunicator;
import gymcard.client.ui.BaseTabPanel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
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
 * Tab đăng ký hội viên mới - Dark theme design
 */
public class RegistrationTab extends BaseTabPanel {

    private JTextField nameField;
    private JTextField birthDateField;
    private JTextField phoneField;
    private JTextArea addressArea;
    private JPasswordField[] pinFields = new JPasswordField[6];
    private JPasswordField[] confirmPinFields = new JPasswordField[6];
    private JLabel avatarLabel;
    private String avatarPath;

    // Colors
    private static final Color BG_DARK = new Color(30, 35, 50);
    private static final Color CARD_BG = new Color(40, 45, 65);
    private static final Color INPUT_BG = new Color(50, 55, 75);
    private static final Color TEXT_WHITE = new Color(230, 230, 240);
    private static final Color TEXT_GRAY = new Color(140, 145, 165);
    private static final Color ACCENT_RED = new Color(231, 76, 60);
    private static final Color ACCENT_GRAY = new Color(100, 105, 125);

    public RegistrationTab(CardCommunicator cardComm) {
        super(cardComm);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(BG_DARK);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BG_DARK);
        mainPanel.setBorder(new EmptyBorder(15, 30, 15, 30));

        // Header
        JLabel titleLabel = new JLabel("Đăng ký hội viên");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(TEXT_WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(titleLabel);

        mainPanel.add(Box.createVerticalStrut(5));

        JLabel subtitleLabel = new JLabel("Điền thông tin chi tiết để khởi tạo thẻ thành viên mới.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(TEXT_GRAY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(subtitleLabel);

        mainPanel.add(Box.createVerticalStrut(15));

        // Form card
        JPanel formCard = createFormCard();
        formCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(formCard);

        mainPanel.add(Box.createVerticalStrut(15));

        // Buttons
        JPanel buttonPanel = createButtonPanel();
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(buttonPanel);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(BG_DARK);
        scrollPane.getViewport().setBackground(BG_DARK);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createFormCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(CARD_BG);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(30, 35, 30, 35));

        // Row 1: Name, Birth, Phone
        JPanel row1 = new JPanel(new GridLayout(1, 3, 25, 0));
        row1.setOpaque(false);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);

        row1.add(createDarkFieldPanel("Họ và tên:", true, nameField = createDarkTextField("Nguyễn Văn A")));
        row1.add(createDarkFieldPanel("Ngày sinh:", true, birthDateField = createDarkTextField("")));
        row1.add(createDarkFieldPanel("Số điện thoại:", true, phoneField = createDarkTextField("")));

        birthDateField.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));

        card.add(row1);
        card.add(Box.createVerticalStrut(25));

        // Row 2: Address
        JPanel addressPanel = new JPanel(new BorderLayout(0, 8));
        addressPanel.setOpaque(false);
        addressPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        addressPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JLabel addressLabel = new JLabel("Địa chỉ:");
        addressLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        addressLabel.setForeground(TEXT_GRAY);
        addressPanel.add(addressLabel, BorderLayout.NORTH);

        addressArea = new JTextArea(2, 30);
        addressArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        addressArea.setBackground(INPUT_BG);
        addressArea.setForeground(TEXT_WHITE);
        addressArea.setCaretColor(TEXT_WHITE);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        addressArea.setBorder(new EmptyBorder(12, 15, 12, 15));
        JScrollPane addressScroll = new JScrollPane(addressArea);
        addressScroll.setBorder(BorderFactory.createLineBorder(new Color(70, 75, 95), 1));
        addressPanel.add(addressScroll, BorderLayout.CENTER);

        card.add(addressPanel);
        card.add(Box.createVerticalStrut(30));

        // Row 3: PIN and Avatar side by side
        JPanel row3 = new JPanel(new GridLayout(1, 2, 50, 0));
        row3.setOpaque(false);
        row3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        row3.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Left: PIN boxes
        JPanel pinSection = new JPanel();
        pinSection.setLayout(new BoxLayout(pinSection, BoxLayout.Y_AXIS));
        pinSection.setOpaque(false);

        pinSection.add(createPinBoxesPanel("Mã PIN (6 chữ số):", pinFields));
        pinSection.add(Box.createVerticalStrut(20));
        pinSection.add(createPinBoxesPanel("Xác nhận PIN:", confirmPinFields));

        row3.add(pinSection);

        // Right: Avatar
        JPanel avatarSection = createAvatarPanel();
        row3.add(avatarSection);

        card.add(row3);
        card.add(Box.createVerticalStrut(25));

        // Notes
        JPanel notePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 40, 0));
        notePanel.setOpaque(false);
        notePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        notePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel note1 = new JLabel("ℹ  Ghi chú: Mã PIN được bảo vệ giới hạn số lần thử.");
        note1.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        note1.setForeground(TEXT_GRAY);
        notePanel.add(note1);

        JLabel note2 = new JLabel("🔒  Dữ liệu cá nhân trên thẻ được mã hóa AES-128.");
        note2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        note2.setForeground(TEXT_GRAY);
        notePanel.add(note2);

        card.add(notePanel);

        return card;
    }

    private JPanel createDarkFieldPanel(String labelText, boolean required, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText + (required ? " *" : ""));
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(TEXT_GRAY);
        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);

        return panel;
    }

    private JTextField createDarkTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(INPUT_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        field.setOpaque(false);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(TEXT_WHITE);
        field.setCaretColor(TEXT_WHITE);
        field.setBorder(new EmptyBorder(12, 15, 12, 15));
        if (!placeholder.isEmpty()) {
            field.setText(placeholder);
            field.setForeground(new Color(100, 105, 125));
            field.addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusGained(java.awt.event.FocusEvent e) {
                    if (field.getText().equals(placeholder)) {
                        field.setText("");
                        field.setForeground(TEXT_WHITE);
                    }
                }

                public void focusLost(java.awt.event.FocusEvent e) {
                    if (field.getText().isEmpty()) {
                        field.setText(placeholder);
                        field.setForeground(new Color(100, 105, 125));
                    }
                }
            });
        }
        return field;
    }

    private JPanel createPinBoxesPanel(String labelText, JPasswordField[] fields) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText + " *");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(TEXT_GRAY);
        panel.add(label, BorderLayout.NORTH);

        JPanel boxesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        boxesPanel.setOpaque(false);

        for (int i = 0; i < 6; i++) {
            final int index = i;
            fields[i] = new JPasswordField() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(INPUT_BG);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    super.paintComponent(g);
                }
            };
            fields[i].setOpaque(false);
            fields[i].setFont(new Font("Segoe UI", Font.BOLD, 24));
            fields[i].setForeground(TEXT_WHITE);
            fields[i].setCaretColor(TEXT_WHITE);
            fields[i].setHorizontalAlignment(JTextField.CENTER);
            fields[i].setBorder(new EmptyBorder(12, 12, 12, 12));
            fields[i].setPreferredSize(new Dimension(55, 55));
            fields[i].setMinimumSize(new Dimension(55, 55));
            fields[i].setMaximumSize(new Dimension(55, 55));
            fields[i].setEchoChar('●');

            // Auto-move to next field
            fields[i].addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyTyped(java.awt.event.KeyEvent e) {
                    char c = e.getKeyChar();
                    if (!Character.isDigit(c)) {
                        e.consume();
                        return;
                    }
                    if (new String(fields[index].getPassword()).length() >= 1) {
                        e.consume();
                        if (index < 5) {
                            fields[index + 1].requestFocus();
                        }
                    }
                }

                public void keyReleased(java.awt.event.KeyEvent e) {
                    if (new String(fields[index].getPassword()).length() == 1 && index < 5) {
                        fields[index + 1].requestFocus();
                    }
                }
            });
            boxesPanel.add(fields[i]);
        }

        panel.add(boxesPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAvatarPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JLabel label = new JLabel("Chọn Avatar:");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(TEXT_GRAY);
        panel.add(label, BorderLayout.NORTH);

        JPanel content = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        content.setOpaque(false);

        // Preview box
        avatarLabel = new JLabel("PREVIEW") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(INPUT_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                // Dashed border
                float[] dash = { 5, 5 };
                g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, dash, 0));
                g2d.setColor(new Color(100, 105, 125));
                g2d.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 8, 8);
                super.paintComponent(g);
            }
        };
        avatarLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        avatarLabel.setForeground(TEXT_GRAY);
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avatarLabel.setPreferredSize(new Dimension(80, 80));
        content.add(avatarLabel);

        // Buttons panel
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.Y_AXIS));
        btnPanel.setOpaque(false);

        JButton chooseBtn = new JButton("Chọn ảnh...") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(ACCENT_GRAY);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        chooseBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chooseBtn.setForeground(TEXT_WHITE);
        chooseBtn.setContentAreaFilled(false);
        chooseBtn.setBorderPainted(false);
        chooseBtn.setFocusPainted(false);
        chooseBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chooseBtn.setPreferredSize(new Dimension(100, 32));
        chooseBtn.addActionListener(e -> chooseAvatarImage());
        btnPanel.add(chooseBtn);

        btnPanel.add(Box.createVerticalStrut(10));

        JLabel hint = new JLabel("Hỗ trợ: JPG, PNG (Max 5MB)");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        hint.setForeground(TEXT_GRAY);
        btnPanel.add(hint);

        content.add(btnPanel);
        panel.add(content, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        // Clear button
        JButton clearBtn = new JButton("Xóa form") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(ACCENT_GRAY);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        clearBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        clearBtn.setForeground(TEXT_WHITE);
        clearBtn.setContentAreaFilled(false);
        clearBtn.setBorderPainted(false);
        clearBtn.setFocusPainted(false);
        clearBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearBtn.setPreferredSize(new Dimension(130, 42));
        clearBtn.addActionListener(e -> clearForm());
        panel.add(clearBtn);

        // Register button
        JButton registerBtn = new JButton("Đăng ký hội viên (Init thẻ)") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2d.setColor(ACCENT_RED.brighter());
                } else {
                    g2d.setColor(ACCENT_RED);
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        registerBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setContentAreaFilled(false);
        registerBtn.setBorderPainted(false);
        registerBtn.setFocusPainted(false);
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerBtn.setPreferredSize(new Dimension(240, 42));
        registerBtn.addActionListener(e -> registerMember());
        panel.add(registerBtn);

        return panel;
    }

    private String getPinFromFields(JPasswordField[] fields) {
        StringBuilder sb = new StringBuilder();
        for (JPasswordField f : fields) {
            sb.append(new String(f.getPassword()));
        }
        return sb.toString();
    }

    private void chooseAvatarImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn ảnh đại diện");
        chooser.setFileFilter(new FileNameExtensionFilter("Ảnh (JPG, PNG)", "jpg", "jpeg", "png"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            avatarPath = file.getAbsolutePath();
            try {
                ImageIcon icon = new ImageIcon(avatarPath);
                Image scaled = icon.getImage().getScaledInstance(75, 75, Image.SCALE_SMOOTH);
                avatarLabel.setIcon(new ImageIcon(scaled));
                avatarLabel.setText("");
            } catch (Exception ex) {
                avatarLabel.setIcon(null);
                avatarLabel.setText("Lỗi");
            }
        }
    }

    private byte[] compressAvatarToCardSize(String path, int maxBytes) throws Exception {
        BufferedImage original = ImageIO.read(new File(path));
        int size = Math.min(original.getWidth(), original.getHeight());
        int x = (original.getWidth() - size) / 2;
        int y = (original.getHeight() - size) / 2;
        BufferedImage crop = original.getSubimage(x, y, size, size);
        int targetSize = 64;
        BufferedImage resized = new BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(crop, 0, 0, targetSize, targetSize, null);
        g2d.dispose();
        float quality = 0.5f;
        byte[] result = encodeJpeg(resized, quality);
        while (result.length > maxBytes && quality > 0.1f) {
            quality -= 0.1f;
            result = encodeJpeg(resized, quality);
        }
        return result.length > maxBytes ? null : result;
    }

    private byte[] encodeJpeg(BufferedImage img, float quality) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
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
                log("Vui long ket noi the truoc!");
                JOptionPane.showMessageDialog(this, "Vui lòng kết nối thẻ trước!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String name = nameField.getText().trim();
            if (name.equals("Nguyễn Văn A"))
                name = "";
            String birthDate = birthDateField.getText().trim();
            String phone = phoneField.getText().trim();
            String address = addressArea.getText().trim();
            String pin = getPinFromFields(pinFields);
            String confirmPin = getPinFromFields(confirmPinFields);

            if (name.isEmpty() || birthDate.isEmpty() || phone.isEmpty() || pin.length() != 6) {
                JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin!", "Thiếu thông tin",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!name.matches("[\\p{L}0-9\\s]+")) {
                JOptionPane.showMessageDialog(this, "Họ tên không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!phone.matches("[0-9]{10,11}")) {
                JOptionPane.showMessageDialog(this, "SĐT phải gồm 10-11 chữ số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!pin.equals(confirmPin)) {
                JOptionPane.showMessageDialog(this, "Mã PIN không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String cardId = gymcard.CardManager.CardIdGenerator.nextId();
            log("Khoi tao the voi CardID = " + cardId);
            cardComm.initNewCard(cardId, pin);

            if (!cardComm.verifyPin(pin)) {
                JOptionPane.showMessageDialog(this, "Xác thực PIN thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            byte[] avatarBytes = null;
            if (avatarPath != null) {
                avatarBytes = compressAvatarToCardSize(avatarPath, 4096);
            }

            boolean ok = cardComm.setMemberInfo(name, birthDate, phone, address, avatarBytes);
            if (!ok) {
                JOptionPane.showMessageDialog(this, "Không thể lưu thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                cardComm.saveCardPublicKeyToDb(cardId);
            } catch (Exception e) {
                log("Khong the luu public key: " + e.getMessage());
            }

            JOptionPane.showMessageDialog(this,
                    "Đăng ký thành công!\n\nHội viên: " + name + "\nMã thẻ: " + cardId + "\nPIN: " + pin,
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            clearForm();

        } catch (Exception ex) {
            log("LOI: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        nameField.setText("Nguyễn Văn A");
        nameField.setForeground(new Color(100, 105, 125));
        birthDateField.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        phoneField.setText("");
        addressArea.setText("");
        for (JTextField f : pinFields)
            f.setText("");
        for (JTextField f : confirmPinFields)
            f.setText("");
        avatarLabel.setIcon(null);
        avatarLabel.setText("PREVIEW");
        avatarPath = null;
    }
}
