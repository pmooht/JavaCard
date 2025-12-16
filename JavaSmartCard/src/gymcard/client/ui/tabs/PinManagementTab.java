package gymcard.client.ui.tabs;

import gymcard.client.CardCommunicator;
import gymcard.client.ui.BaseTabPanel;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Tab quản lý PIN (Đổi PIN + Mở khóa)
 * - Cột trái: Đổi PIN hội viên (có PIN cũ)
 * - Cột phải: Admin mở khóa thẻ (khi nhập sai quá số lần cho phép)
 */
public class PinManagementTab extends BaseTabPanel {

    public PinManagementTab(CardCommunicator cardComm) {
        super(cardComm);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(248, 249, 250));

        JPanel panel = new JPanel(new GridLayout(1, 2, 12, 0));
        panel.setOpaque(false);

        // LEFT CARD: ĐỔI PIN KHI HỘI VIÊN QUÊN
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
        adminPinField1.setBorder(fieldBorder());
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
        newPinField.setBorder(fieldBorder());
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
        confirmPinField.setBorder(fieldBorder());
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

        // RIGHT CARD: UNLOCK CARD (ADMIN)
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
        adminPinField2.setBorder(fieldBorder());
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

        add(panel, BorderLayout.CENTER);
    }
}
