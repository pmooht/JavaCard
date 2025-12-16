package gymcard.client.ui.tabs;

import gymcard.client.CardCommunicator;
import gymcard.client.ui.BaseTabPanel;
import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Panel đăng nhập bằng PIN
 */
public class LoginPanel extends BaseTabPanel {

    private final Runnable onLoginSuccess;
    private JPasswordField pinField;
    private JLabel triesLabel;

    public LoginPanel(CardCommunicator cardComm, Runnable onLoginSuccess) {
        super(cardComm);
        this.onLoginSuccess = onLoginSuccess;
        initUI();
    }

    private void initUI() {
        setLayout(new GridBagLayout());
        setBackground(new Color(236, 240, 241));

        // Card Panel
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

        // Icon
        JLabel iconLabel = new JLabel("");
        gbc.gridy = row++;
        card.add(iconLabel, gbc);

        // Title
        JLabel titleLabel = new JLabel("ĐĂNG NHẬP BẰNG PIN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(44, 62, 80));
        gbc.gridy = row++;
        card.add(titleLabel, gbc);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Vui lòng nhập mã PIN gồm 6 chữ số");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(127, 140, 141));
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 6, 14, 6);
        card.add(subtitleLabel, gbc);

        // PIN Field
        gbc.gridy = row++;
        gbc.insets = new Insets(8, 6, 6, 6);
        pinField = new JPasswordField(6);
        pinField.setFont(new Font("Segoe UI", Font.BOLD, 28));
        pinField.setHorizontalAlignment(JTextField.CENTER);
        pinField.setEchoChar('●');
        pinField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        pinField.setPreferredSize(new Dimension(220, 54));
        card.add(pinField, gbc);

        // Tries Label
        triesLabel = new JLabel(" ");
        triesLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        triesLabel.setForeground(new Color(231, 76, 60));
        gbc.gridy = row++;
        gbc.insets = new Insets(4, 6, 10, 6);
        card.add(triesLabel, gbc);

        // Login Button
        gbc.gridy = row++;
        gbc.insets = new Insets(8, 6, 6, 6);
        JButton loginBtn = createModernButton("Đăng nhập", new Color(52, 152, 219), 14);
        loginBtn.setPreferredSize(new Dimension(220, 42));
        card.add(loginBtn, gbc);

        // Check tries button
        gbc.gridy = row++;
        gbc.insets = new Insets(4, 6, 0, 6);
        JButton checkTriesBtn = createModernButton("Kiểm tra số lần thử", new Color(155, 89, 182), 12);
        checkTriesBtn.setPreferredSize(new Dimension(220, 36));
        card.add(checkTriesBtn, gbc);

        add(card);

        // Action listeners
        ActionListener loginAction = e -> doLogin();
        loginBtn.addActionListener(loginAction);
        pinField.addActionListener(loginAction);

        checkTriesBtn.addActionListener(e -> checkTries());
    }

    private void doLogin() {
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
                if (onLoginSuccess != null) {
                    onLoginSuccess.run();
                }
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
    }

    private void checkTries() {
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
    }

    public void reset() {
        if (pinField != null)
            pinField.setText("");
        if (triesLabel != null)
            triesLabel.setText(" ");
    }
}
