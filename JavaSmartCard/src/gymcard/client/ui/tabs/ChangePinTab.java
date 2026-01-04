package gymcard.client.ui.tabs;

import gymcard.client.CardCommunicator;
import gymcard.client.ui.BaseTabPanel;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Tab thay đổi PIN
 */
public class ChangePinTab extends BaseTabPanel {

        public ChangePinTab(CardCommunicator cardComm) {
                super(cardComm);
                initUI();
        }

        private void initUI() {
                setLayout(new BorderLayout());
                setBackground(new Color(248, 249, 250));
                setBorder(new EmptyBorder(10, 10, 10, 10));

                // Title
                JLabel title = new JLabel("Thay đổi mã PIN");
                title.setFont(new Font("Segoe UI", Font.BOLD, 18));
                title.setForeground(new Color(52, 73, 94));
                title.setBorder(new EmptyBorder(0, 5, 5, 5));
                add(title, BorderLayout.NORTH);

                // Card trắng chứa form
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

                // Info label
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

                // Note
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

                add(panel, BorderLayout.CENTER);

                // Button
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
                                        log("Đã thay đổi PIN thành công.");
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
                add(bottom, BorderLayout.SOUTH);
        }
}
