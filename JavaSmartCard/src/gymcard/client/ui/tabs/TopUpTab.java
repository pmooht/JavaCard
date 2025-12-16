package gymcard.client.ui.tabs;

import gymcard.client.CardCommunicator;
import gymcard.client.ui.BaseTabPanel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Tab Nạp tiền
 */
public class TopUpTab extends BaseTabPanel {

    public TopUpTab(CardCommunicator cardComm) {
        super(cardComm);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(new Color(248, 249, 250));

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
        add(headerPanel, BorderLayout.NORTH);

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
            // Dialog xác thực admin
            JPasswordField passField = new JPasswordField(10);
            JPanel passPanel = new JPanel(new BorderLayout(5, 5));
            passPanel.add(new JLabel("Nhập mật khẩu Admin:"), BorderLayout.NORTH);
            passPanel.add(passField, BorderLayout.CENTER);

            int result = JOptionPane.showConfirmDialog(this, passPanel,
                    "Xác thực Admin", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result != JOptionPane.OK_OPTION)
                return;

            String adminPass = new String(passField.getPassword()).trim();
            if (adminPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập mật khẩu!", "Lỗi",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!adminPass.equals("123456")) {
                JOptionPane.showMessageDialog(this, "Mật khẩu Admin không chính xác!", "Sai mật khẩu",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                short amount = ((Number) amountSpinner.getValue()).shortValue();
                if (cardComm.addBalance(amount)) {
                    short newBalance = cardComm.getBalance();
                    balanceLabel.setText(String.format("%,d VNĐ", newBalance * 1000));
                    JOptionPane.showMessageDialog(this,
                            "Nạp tiền thành công!\n\nSố tiền: " + String.format("%,d", amount * 1000)
                                    + " VNĐ\nSố dư mới: " + String.format("%,d", newBalance * 1000) + " VNĐ",
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Nạp tiền thất bại!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        content.add(topUpBtn, gbc);

        add(content, BorderLayout.CENTER);
    }
}
