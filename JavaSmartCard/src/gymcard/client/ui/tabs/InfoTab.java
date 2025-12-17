package gymcard.client.ui.tabs;

import gymcard.client.CardCommunicator;
import gymcard.client.MemberInfo;
import gymcard.client.ui.BaseTabPanel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Tab thông tin cá nhân với avatar
 */
public class InfoTab extends BaseTabPanel {

    private JLabel avatarLabel;
    private JTextArea infoArea;

    public InfoTab(CardCommunicator cardComm) {
        super(cardComm);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(248, 249, 250));

        JLabel titleLabel = new JLabel("THÔNG TIN CÁ NHÂN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(52, 73, 94));
        titleLabel.setBorder(new EmptyBorder(0, 5, 5, 5));
        add(titleLabel, BorderLayout.NORTH);

        // Card container
        JPanel cardPanel = new JPanel(new BorderLayout(15, 15));
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(15, 15, 15, 15)));

        // Avatar left
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

        // Right: View/Edit cards via CardLayout
        CardLayout infoLayout = new CardLayout();
        JPanel rightStack = new JPanel(infoLayout);
        rightStack.setBackground(Color.WHITE);

        // VIEW PANEL
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

        // EDIT PANEL
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

        add(cardPanel, BorderLayout.CENTER);

        // State for editing avatar bytes
        final byte[][] pendingAvatar = new byte[1][];
        final MemberInfo[] loadedMember = new MemberInfo[1];

        // Bottom buttons
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

        // Default: view mode
        saveBtn.setVisible(false);
        cancelBtn.setVisible(false);
        changeAvatarBtn.setVisible(false);

        loadBtn.addActionListener(e -> {
            try {
                MemberInfo member = cardComm.getMemberInfo();
                loadedMember[0] = member;
                pendingAvatar[0] = member.avatarBytes;

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
                        "Bạn hãy bấm Tải thông tin từ thẻ trước.",
                        "Chưa có dữ liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }

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

                cardComm.setMemberInfo(newName, newDob, newPhone, newAddr, av);

                MemberInfo m = new MemberInfo();
                m.name = newName;
                m.birthDate = newDob;
                m.phone = newPhone;
                m.address = newAddr;
                m.avatarBytes = av;
                loadedMember[0] = m;

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

        add(buttonPanel, BorderLayout.SOUTH);
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

        int target = 96;
        BufferedImage scaled = new BufferedImage(target, target, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, target, target, null);
        g.dispose();

        for (float q = 0.70f; q >= 0.15f; q -= 0.05f) {
            byte[] jpg = encodeJpeg(scaled, q);
            if (jpg.length <= 4096)
                return jpg;
        }

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

        throw new Exception("Không thể nén ảnh xuống <= 4096 bytes. Hãy chọn ảnh đơn giản hơn.");
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

    private void setAvatarToLabel(JLabel avatarLabel, byte[] avatarBytes) {
        try {
            log("[AVATAR] bytes=" + (avatarBytes == null ? "null" : avatarBytes.length));

            if (avatarBytes == null || avatarBytes.length == 0) {
                avatarLabel.setIcon(null);
                avatarLabel.setText("👤");
                return;
            }

            BufferedImage img = ImageIO.read(new ByteArrayInputStream(avatarBytes));
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
     * Public method to refresh member info from card
     */
    public void refreshData() {
        try {
            MemberInfo member = cardComm.getMemberInfo();

            setAvatarToLabel(avatarLabel, member.avatarBytes);

            StringBuilder sb = new StringBuilder();
            sb.append("━━━━━━━━ THONG TIN CA NHAN ━━━━━━━━\n\n");
            sb.append(String.format("Ho va ten   : %s\n\n", member.name));
            sb.append(String.format("Ngay sinh   : %s\n\n", member.birthDate));
            sb.append(String.format("SDT         : %s\n\n", member.phone));
            sb.append(String.format("Dia chi     : %s\n", member.address));

            infoArea.setText(sb.toString());
            infoArea.setCaretPosition(0);

            log("Da tai thong tin ca nhan tu the");
        } catch (Exception ex) {
            log("LOI tai thong tin: " + ex.getMessage());
        }
    }

    public void clearUI() {
        if (infoArea != null)
            infoArea.setText("");
        if (avatarLabel != null) {
            avatarLabel.setIcon(null);
            avatarLabel.setText("[Avatar]");
        }
    }
}
