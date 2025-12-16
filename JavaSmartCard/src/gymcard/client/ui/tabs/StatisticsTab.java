package gymcard.client.ui.tabs;

import gymcard.client.CardCommunicator;
import gymcard.client.CheckInInfo;
import gymcard.client.PackageInfo;
import gymcard.client.ui.BaseTabPanel;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Tab Thống kê - Giao diện cải tiến với grid 2x2 và dịch vụ đã mua
 */
public class StatisticsTab extends BaseTabPanel {

    private final List<String> purchasedServices;

    public StatisticsTab(CardCommunicator cardComm, List<String> purchasedServices) {
        super(cardComm);
        this.purchasedServices = purchasedServices;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(248, 249, 250));

        // Header
        JLabel headerLabel = new JLabel("THỐNG KÊ HOẠT ĐỘNG");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerLabel.setForeground(new Color(52, 73, 94));
        add(headerLabel, BorderLayout.NORTH);

        // Main grid 2x2
        JPanel gridPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        gridPanel.setBackground(new Color(248, 249, 250));

        // Card 1: Gói tập hiện tại
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

        // Card 2: Số dư tài khoản
        JPanel balanceCard = createStatCard("SỐ DƯ TÀI KHOẢN", new Color(46, 204, 113));
        JLabel balanceValueLabel = new JLabel("0 VNĐ");
        balanceValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        balanceValueLabel.setForeground(new Color(46, 204, 113));

        JPanel balanceContent = (JPanel) balanceCard.getComponent(1);
        balanceContent.add(balanceValueLabel);
        gridPanel.add(balanceCard);

        // Card 3: Hoạt động tập luyện
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

        // Card 4: Dịch vụ đã mua
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

        add(gridPanel, BorderLayout.CENTER);

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
                JOptionPane.showMessageDialog(this,
                        "Lỗi tải thống kê: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(refreshBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}
