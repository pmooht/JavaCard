package gymcard.client.ui;

import gymcard.client.CardCommunicator;
import gymcard.client.ui.tabs.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Admin Panel - Quản lý hệ thống
 * Controller class quản lý và điều phối các tab panels cho admin
 */
public class AdminPanel extends JPanel {

    private final CardCommunicator cardComm;

    public AdminPanel(CardCommunicator cardComm) {
        this.cardComm = cardComm;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(new Color(248, 249, 250));

        // Main content with tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.setBackground(Color.WHITE);

        // Add tabs using separated tab classes
        tabbedPane.addTab("Đăng ký hội viên", new RegistrationTab(cardComm));
        tabbedPane.addTab("Quản lý gói tập", new PackageManagementTab(cardComm));
        tabbedPane.addTab("Quản lý dịch vụ", new ServiceManagementTab(cardComm));
        tabbedPane.addTab("Đổi PIN & Mở khóa", new PinManagementTab(cardComm));

        add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * Log message ra terminal
     */
    private void log(String message) {
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        System.out.println(String.format("[%s] %s", timestamp, message));
    }
}
