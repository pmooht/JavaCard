package gymcard.client.ui.tabs;

import gymcard.client.CardCommunicator;
import gymcard.client.ui.BaseTabPanel;
import gymcard.databaseManager.DatabaseManager;
import gymcard.databaseManager.DatabaseManager.ServiceInfo;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Tab Dịch vụ thêm - Load từ Database
 */
public class ServicesTab extends BaseTabPanel {

    private final List<String> purchasedServices;
    private JPanel servicesPanel;
    private DatabaseManager db;

    public ServicesTab(CardCommunicator cardComm, List<String> purchasedServices) {
        super(cardComm);
        this.purchasedServices = purchasedServices;
        try {
            db = DatabaseManager.getInstance();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(new Color(248, 249, 250));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(248, 249, 250));

        JLabel headerLabel = new JLabel("DỊCH VỤ THÊM");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setForeground(new Color(155, 89, 182));
        headerPanel.add(headerLabel, BorderLayout.WEST);

        JButton refreshBtn = createModernButton("Tải lại", new Color(52, 152, 219), 12);
        refreshBtn.setPreferredSize(new Dimension(100, 32));
        refreshBtn.addActionListener(e -> loadServices());
        headerPanel.add(refreshBtn, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Services panel
        servicesPanel = new JPanel();
        servicesPanel.setBackground(new Color(248, 249, 250));
        servicesPanel.setLayout(new GridLayout(0, 3, 15, 15));

        JScrollPane scroll = new JScrollPane(servicesPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        // Load services on init
        loadServices();
    }

    private void loadServices() {
        servicesPanel.removeAll();

        try {
            List<ServiceInfo> services = db.getActiveServices();

            // Colors for services
            Color[] colors = {
                    new Color(155, 89, 182),
                    new Color(52, 152, 219),
                    new Color(46, 204, 113),
                    new Color(241, 196, 15),
                    new Color(231, 76, 60),
                    new Color(26, 188, 156)
            };

            int colorIndex = 0;
            for (ServiceInfo svc : services) {
                Color svcColor = colors[colorIndex % colors.length];
                colorIndex++;

                JPanel svcCard = new JPanel(new BorderLayout(5, 5));
                svcCard.setBackground(Color.WHITE);
                svcCard.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(svcColor, 2),
                        new EmptyBorder(15, 15, 15, 15)));

                JLabel nameLabel = new JLabel(svc.name);
                nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                nameLabel.setForeground(svcColor);

                int priceK = (int) (svc.price / 1000);
                JLabel priceLabel = new JLabel(priceK + "k VNĐ");
                priceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                priceLabel.setForeground(Color.GRAY);

                JLabel descLabel = new JLabel(
                        "<html><i>" + (svc.description != null ? svc.description : "") + "</i></html>");
                descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                descLabel.setForeground(new Color(127, 140, 141));

                JButton buyBtn = new JButton("Mua");
                buyBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
                buyBtn.setBackground(svcColor);
                buyBtn.setForeground(Color.WHITE);
                buyBtn.setFocusPainted(false);
                buyBtn.setBorderPainted(false);
                buyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

                final String svcName = svc.name;
                final long svcPrice = (long) svc.price; // VND

                buyBtn.addActionListener(e -> {
                    try {
                        long currentBalance = cardComm.getBalance();
                        if (currentBalance < svcPrice) {
                            JOptionPane.showMessageDialog(this,
                                    "Khong du tien!\n\nSo du: " + String.format("%,d", currentBalance)
                                            + " VND\nCan: " + String.format("%,d", svcPrice) + " VND",
                                    "Khong du tien", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        if (cardComm.deductBalance(svcPrice)) {
                            long newBalance = cardComm.getBalance();
                            String time = new SimpleDateFormat("HH:mm dd/MM").format(new Date());
                            purchasedServices
                                    .add(svcName + " - " + String.format("%,d", svcPrice) + " VND (" + time + ")");
                            log("Da mua dich vu: " + svcName + " - " + String.format("%,d", svcPrice) + " VND");
                            JOptionPane.showMessageDialog(this,
                                    "Mua dich vu thanh cong!\n\nDich vu: " + svcName + "\nGia: "
                                            + String.format("%,d", svcPrice) + " VND\nSo du con: "
                                            + String.format("%,d", newBalance) + " VND",
                                    "Thanh cong", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(this, "Mua dịch vụ thất bại!", "Lỗi",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });

                JPanel infoPanel = new JPanel();
                infoPanel.setOpaque(false);
                infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
                infoPanel.add(nameLabel);
                infoPanel.add(priceLabel);
                infoPanel.add(Box.createVerticalStrut(5));
                infoPanel.add(descLabel);

                svcCard.add(infoPanel, BorderLayout.CENTER);
                svcCard.add(buyBtn, BorderLayout.EAST);

                servicesPanel.add(svcCard);
            }

            if (services.isEmpty()) {
                JLabel emptyLabel = new JLabel("Chưa có dịch vụ nào. Admin vui lòng thêm dịch vụ trong tab quản lý.");
                emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
                emptyLabel.setForeground(new Color(127, 140, 141));
                servicesPanel.add(emptyLabel);
            }

            servicesPanel.revalidate();
            servicesPanel.repaint();
            log("Đã tải " + services.size() + " dịch vụ từ database");

        } catch (SQLException e) {
            log("LỖI tải dịch vụ: " + e.getMessage());
            JLabel errorLabel = new JLabel("Lỗi tải dịch vụ: " + e.getMessage());
            errorLabel.setForeground(Color.RED);
            servicesPanel.add(errorLabel);
        }
    }
}
