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
 * Tab Thong ke - Giao dien cai tien voi grid 2x2 va dich vu da mua
 */
public class StatisticsTab extends BaseTabPanel {

    private final List<String> purchasedServices;

    // Fields for labels that need to be updated
    private JLabel packageTypeLabel;
    private JLabel packageExpiryLabel;
    private JLabel sessionsLabel;
    private JLabel balanceValueLabel;
    private JLabel checkInCountLabel;
    private JLabel lastCheckInLabel;
    private JLabel countLabel;
    private DefaultListModel<String> servicesModel;

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
        JLabel headerLabel = new JLabel("THONG KE HOAT DONG");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerLabel.setForeground(new Color(52, 73, 94));
        add(headerLabel, BorderLayout.NORTH);

        // Main grid 2x2
        JPanel gridPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        gridPanel.setBackground(new Color(248, 249, 250));

        // Card 1: Goi tap hien tai
        JPanel packageCard = createStatCard("GOI TAP HIEN TAI", new Color(52, 152, 219));
        packageTypeLabel = new JLabel("Chua co goi tap");
        packageTypeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        packageExpiryLabel = new JLabel("Han: --");
        packageExpiryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sessionsLabel = new JLabel("");
        sessionsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JPanel packageContent = (JPanel) packageCard.getComponent(1);
        packageContent.add(packageTypeLabel);
        packageContent.add(Box.createVerticalStrut(5));
        packageContent.add(packageExpiryLabel);
        packageContent.add(sessionsLabel);
        gridPanel.add(packageCard);

        // Card 2: So du tai khoan
        JPanel balanceCard = createStatCard("SO DU TAI KHOAN", new Color(46, 204, 113));
        balanceValueLabel = new JLabel("0 VND");
        balanceValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        balanceValueLabel.setForeground(new Color(46, 204, 113));

        JPanel balanceContent = (JPanel) balanceCard.getComponent(1);
        balanceContent.add(balanceValueLabel);
        gridPanel.add(balanceCard);

        // Card 3: Hoat dong tap luyen
        JPanel activityCard = createStatCard("HOAT DONG TAP LUYEN", new Color(155, 89, 182));
        checkInCountLabel = new JLabel("So buoi tap: 0");
        checkInCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lastCheckInLabel = new JLabel("Lan tap gan nhat: --");
        lastCheckInLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JPanel activityContent = (JPanel) activityCard.getComponent(1);
        activityContent.add(checkInCountLabel);
        activityContent.add(Box.createVerticalStrut(5));
        activityContent.add(lastCheckInLabel);
        gridPanel.add(activityCard);

        // Card 4: Dich vu da mua
        JPanel servicesCard = createStatCard("DICH VU DA MUA", new Color(231, 76, 60));
        servicesModel = new DefaultListModel<>();
        JList<String> servicesList = new JList<>(servicesModel);
        servicesList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        servicesList.setBackground(new Color(250, 250, 250));
        servicesList.setFixedCellHeight(24);
        JScrollPane servicesScroll = new JScrollPane(servicesList);
        servicesScroll.setPreferredSize(new Dimension(0, 100));
        servicesScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));

        JPanel servicesContent = (JPanel) servicesCard.getComponent(1);
        servicesContent.setLayout(new BorderLayout(5, 5));
        countLabel = new JLabel("Tong: 0 dich vu");
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        servicesContent.add(countLabel, BorderLayout.NORTH);
        servicesContent.add(servicesScroll, BorderLayout.CENTER);
        gridPanel.add(servicesCard);

        add(gridPanel, BorderLayout.CENTER);

        // Nut tai lai
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(new Color(248, 249, 250));
        JButton refreshBtn = createModernButton("Tai lai thong ke", new Color(52, 152, 219), 14);
        refreshBtn.setPreferredSize(new Dimension(180, 40));
        refreshBtn.addActionListener(e -> refreshData());
        buttonPanel.add(refreshBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Public method to refresh statistics data (called by UserPanel on tab change)
     */
    public void refreshData() {
        try {
            // Load goi tap
            PackageInfo pkg = cardComm.getPackage();
            packageTypeLabel.setText(pkg.getPackageTypeName());
            packageExpiryLabel.setText("Han: " + (pkg.expiry.isEmpty() ? "--" : pkg.expiry));
            if (pkg.type == 2 && pkg.remainingSessions > 0) {
                sessionsLabel.setText("Con: " + pkg.remainingSessions + " buoi");
            } else {
                sessionsLabel.setText("");
            }

            // Load so du
            long balance = cardComm.getBalance();
            balanceValueLabel.setText(String.format("%,d VND", balance));

            // Load check-in
            int checkInCount = cardComm.getCheckInCount();
            checkInCountLabel.setText("So buoi tap: " + checkInCount);
            CheckInInfo lastCheckIn = cardComm.getLastCheckIn();
            if (lastCheckIn != null && !lastCheckIn.date.isEmpty()) {
                lastCheckInLabel.setText("Gan nhat: " + lastCheckIn.date + " " + lastCheckIn.checkInTime);
            } else {
                lastCheckInLabel.setText("Lan tap gan nhat: --");
            }

            // Load dich vu da mua
            servicesModel.clear();
            for (String svc : purchasedServices) {
                servicesModel.addElement(svc);
            }
            countLabel.setText("Tong: " + purchasedServices.size() + " dich vu");

            log("Da tai thong ke tu the");

        } catch (Exception ex) {
            log("LOI tai thong ke: " + ex.getMessage());
        }
    }
}
