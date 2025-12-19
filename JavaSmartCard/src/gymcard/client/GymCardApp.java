package gymcard.client;

import gymcard.client.ui.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Main application - Gym Card Management System (SmartCard + AES + RSA)
 * Redesigned with Welcome Screen and Sidebar Navigation
 */
public class GymCardApp extends JFrame {

    private final CardCommunicator cardComm;
    private CardLayout mainCardLayout;
    private JPanel mainContentPanel;
    private WelcomePanel welcomePanel;
    private AdminPanel adminPanel;
    private UserPanel userPanel;

    public GymCardApp() {
        cardComm = new CardCommunicator();
        initUI();
        setMinimumSize(new Dimension(1100, 700));
    }

    private void initUI() {
        setTitle("Hệ thống Quản lý Thẻ Gym - SmartCard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);

        // Main CardLayout for switching between Welcome, Admin, User
        mainCardLayout = new CardLayout();
        mainContentPanel = new JPanel(mainCardLayout);

        // Welcome Panel
        welcomePanel = new WelcomePanel(cardComm,
                this::showAdminPanel,
                this::showUserPanel,
                this::toggleConnection);
        mainContentPanel.add(welcomePanel, "welcome");

        // Admin Panel (will be created with sidebar)
        adminPanel = new AdminPanel(cardComm, this::showWelcome);
        mainContentPanel.add(adminPanel, "admin");

        // User Panel (will be created with sidebar)
        userPanel = new UserPanel(cardComm, this::showWelcome);
        mainContentPanel.add(userPanel, "user");

        // Show welcome by default
        mainCardLayout.show(mainContentPanel, "welcome");

        setContentPane(mainContentPanel);
    }

    private void showWelcome() {
        mainCardLayout.show(mainContentPanel, "welcome");
        welcomePanel.updateConnectionStatus();
    }

    private void showAdminPanel() {
        mainCardLayout.show(mainContentPanel, "admin");
    }

    private void showUserPanel() {
        if (!cardComm.isConnected()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng kết nối thẻ trước khi vào!",
                    "Chưa kết nối", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if card is locked first
        try {
            int triesRemaining = cardComm.getPinTries();
            if (triesRemaining == 0) {
                JOptionPane.showMessageDialog(this,
                        "Thẻ đã bị KHÓA do nhập sai PIN quá 3 lần!\n\n" +
                                "Vui lòng liên hệ Quản trị viên để mở khóa thẻ.",
                        "Thẻ bị khóa", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (Exception e) {
            // Ignore - will handle during verify
        }

        // Show PIN dialog
        JPasswordField pinField = new JPasswordField(6);
        pinField.setFont(new Font("Segoe UI", Font.BOLD, 24));
        pinField.setHorizontalAlignment(JTextField.CENTER);

        JPanel pinPanel = new JPanel(new BorderLayout(10, 10));
        pinPanel.setBorder(new EmptyBorder(20, 20, 10, 20));
        pinPanel.add(new JLabel("Nhập mã PIN (6 số):"), BorderLayout.NORTH);
        pinPanel.add(pinField, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, pinPanel,
                "Xác thực Hội viên", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String pin = new String(pinField.getPassword());
            try {
                System.out.println("[LOGIN] === BAT DAU DANG NHAP USER ===");
                System.out.println("[LOGIN] Dang goi verifyPinWithCardAuth...");

                CardCommunicator.AuthResult authResult = cardComm.verifyPinWithCardAuth(pin);

                System.out.println("[LOGIN] Ket qua: " + authResult);

                if (!authResult.pinVerified) {
                    // PIN sai
                    int triesLeft = cardComm.getPinTries();
                    System.out.println("[LOGIN] PIN SAI! Con " + triesLeft + " lan thu");

                    if (triesLeft == 0) {
                        JOptionPane.showMessageDialog(this,
                                "Mã PIN không đúng!\n\n" +
                                        "THẺ ĐÃ BỊ KHÓA!\n" +
                                        "Bạn đã nhập sai PIN quá 3 lần.\n\n" +
                                        "Vui lòng liên hệ Quản trị viên để mở khóa.",
                                "Thẻ bị khóa", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Mã PIN không đúng!\n\n" +
                                        "Số lần nhập còn lại: " + triesLeft + "\n" +
                                        (triesLeft == 1 ? "CẢNH BÁO: Thẻ sẽ bị khóa nếu nhập sai thêm 1 lần!" : ""),
                                "Lỗi xác thực", JOptionPane.WARNING_MESSAGE);
                    }
                } else if (authResult.rsaVerified || authResult.rsaSkipped) {
                    // PIN dung + RSA OK hoac skip
                    System.out.println("[LOGIN] THANH CONG! Vao trang User...");
                    mainCardLayout.show(mainContentPanel, "user");
                    userPanel.onLogin();
                } else {
                    // PIN dung nhung RSA FAIL - THE GIA MAO!
                    System.out.println("[LOGIN] !!! CANH BAO: RSA THAT BAI - THE CO THE GIA MAO !!!");
                    JOptionPane.showMessageDialog(this,
                            "XÁC THỰC THẺ THẤT BẠI!\n\n" +
                                    "Mã PIN đúng, nhưng thẻ không vượt qua xác thực RSA.\n\n" +
                                    "Nguyên nhân có thể:\n" +
                                    "• Thẻ bị sao chép (clone)\n" +
                                    "• Thẻ không hợp lệ\n" +
                                    "• Thẻ bị thay đổi trái phép\n\n" +
                                    "Vui lòng liên hệ quản trị viên để được hỗ trợ.",
                            "Xác thực thẻ thất bại", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                System.out.println("[LOGIN] LOI: " + ex.getMessage());
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Lỗi xác thực: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void toggleConnection() {
        if (!cardComm.isConnected()) {
            try {
                cardComm.connect();
                welcomePanel.updateConnectionStatus();
                JOptionPane.showMessageDialog(this,
                        "Đã kết nối thành công với thẻ JavaCard!\n\n" +
                                "Bạn có thể:\n" +
                                "- Vào QUẢN TRỊ VIÊN để quản trị hệ thống\n" +
                                "- Vào HỘI VIÊN để check-in và sử dụng dịch vụ",
                        "Kết nối thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Không thể kết nối với thẻ:\n" + ex.getMessage(),
                        "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            try {
                cardComm.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            welcomePanel.updateConnectionStatus();
            JOptionPane.showMessageDialog(this,
                    "Đã ngắt kết nối với thẻ.",
                    "Ngắt kết nối", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            GymCardApp app = new GymCardApp();
            app.setVisible(true);
        });
    }
}
