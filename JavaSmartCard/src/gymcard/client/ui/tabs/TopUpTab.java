package gymcard.client.ui.tabs;

import gymcard.client.CardCommunicator;
import gymcard.client.ui.BaseTabPanel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Tab Nap tien
 */
public class TopUpTab extends BaseTabPanel {

    private JLabel balanceLabel;

    public TopUpTab(CardCommunicator cardComm) {
        super(cardComm);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(new Color(248, 249, 250));

        // Header: So du hien tai
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(46, 204, 113));
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("SO DU TAI KHOAN");
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(255, 255, 255, 200));

        balanceLabel = new JLabel("0 VND");
        balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        balanceLabel.setForeground(Color.WHITE);

        JPanel balancePanel = new JPanel();
        balancePanel.setOpaque(false);
        balancePanel.setLayout(new BoxLayout(balancePanel, BoxLayout.Y_AXIS));
        balancePanel.add(titleLabel);
        balancePanel.add(balanceLabel);

        headerPanel.add(balancePanel, BorderLayout.WEST);

        JButton refreshBtn = new JButton("Tai lai");
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setBackground(new Color(39, 174, 96));
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> refreshData());
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Content: Nap tien
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
        JLabel quickLabel = new JLabel("Chon muc nap nhanh:");
        quickLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        content.add(quickLabel, gbc);

        // Spinner cho phep nhap so lon (long) - don vi VND
        // Max = MAX_BALANCE (999 ty)
        JSpinner amountSpinner = new JSpinner(new SpinnerNumberModel(
                100000L, // Gia tri mac dinh: 100.000 VND
                1000L, // Min: 1.000 VND
                CardCommunicator.MAX_BALANCE, // Max: 999 ty VND
                10000L // Step: 10.000 VND
        ));
        amountSpinner.setFont(new Font("Segoe UI", Font.BOLD, 18));
        // Format hien thi so
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(amountSpinner, "#,##0");
        amountSpinner.setEditor(editor);

        // Quick amounts (VND)
        String[] quickAmounts = { "50k", "100k", "200k", "500k" };
        long[] quickValues = { 50000L, 100000L, 200000L, 500000L };
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        for (int i = 0; i < quickAmounts.length; i++) {
            gbc.gridx = i;
            final long val = quickValues[i];
            JButton qBtn = createModernButton(quickAmounts[i], new Color(46, 204, 113), 14);
            qBtn.setPreferredSize(new Dimension(80, 40));
            qBtn.addActionListener(e -> amountSpinner.setValue(val));
            content.add(qBtn, gbc);
        }

        // Input
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 4;
        JLabel inputLabel = new JLabel("Hoac nhap so tien (VND):");
        inputLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        content.add(inputLabel, gbc);

        gbc.gridy = 3;
        content.add(amountSpinner, gbc);

        // Nut nap
        gbc.gridy = 4;
        gbc.insets = new Insets(25, 10, 10, 10);
        JButton topUpBtn = createModernButton("NAP TIEN", new Color(46, 204, 113), 16);
        topUpBtn.setPreferredSize(new Dimension(0, 50));
        topUpBtn.addActionListener(e -> {
            // Dialog xac thuc admin
            JPasswordField passField = new JPasswordField(10);
            JPanel passPanel = new JPanel(new BorderLayout(5, 5));
            passPanel.add(new JLabel("Nhap mat khau Admin:"), BorderLayout.NORTH);
            passPanel.add(passField, BorderLayout.CENTER);

            int result = JOptionPane.showConfirmDialog(this, passPanel,
                    "Xac thuc Admin", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result != JOptionPane.OK_OPTION)
                return;

            String adminPass = new String(passField.getPassword()).trim();
            if (adminPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui long nhap mat khau!", "Loi",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!adminPass.equals("123456")) {
                JOptionPane.showMessageDialog(this, "Mat khau Admin khong chinh xac!", "Sai mat khau",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                long amount = ((Number) amountSpinner.getValue()).longValue();
                if (cardComm.addBalance(amount)) {
                    long newBalance = cardComm.getBalance();
                    balanceLabel.setText(String.format("%,d VND", newBalance));
                    JOptionPane.showMessageDialog(this,
                            "Nap tien thanh cong!\n\nSo tien: " + String.format("%,d", amount)
                                    + " VND\nSo du moi: " + String.format("%,d", newBalance) + " VND",
                            "Thanh cong", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Nap tien that bai!", "Loi",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Loi: " + ex.getMessage(), "Loi",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        content.add(topUpBtn, gbc);

        add(content, BorderLayout.CENTER);
    }

    /**
     * Public method to refresh balance data (called by UserPanel on tab change)
     */
    public void refreshData() {
        try {
            long balance = cardComm.getBalance();
            balanceLabel.setText(String.format("%,d VND", balance));
            log("So du: " + balance + " VND");
        } catch (Exception ex) {
            log("LOI: " + ex.getMessage());
        }
    }
}
