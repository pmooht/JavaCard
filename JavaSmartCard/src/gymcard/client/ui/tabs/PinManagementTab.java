package gymcard.client.ui.tabs;

import gymcard.client.CardCommunicator;
import gymcard.client.ui.BaseTabPanel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Tab quản lý PIN (Đổi PIN + Mở khóa) - Dark theme design
 */
public class PinManagementTab extends BaseTabPanel {

    // Colors
    private static final Color BG_DARK = new Color(30, 35, 50);
    private static final Color CARD_BG = new Color(40, 45, 65);
    private static final Color INPUT_BG = new Color(50, 55, 75);
    private static final Color TEXT_WHITE = new Color(230, 230, 240);
    private static final Color TEXT_GRAY = new Color(140, 145, 165);
    private static final Color ACCENT_BLUE = new Color(52, 152, 219);
    private static final Color ACCENT_RED = new Color(231, 76, 60);

    private JPasswordField[] newPinFields = new JPasswordField[6];
    private JPasswordField[] confirmPinFields = new JPasswordField[6];

    public PinManagementTab(CardCommunicator cardComm) {
        super(cardComm);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(BG_DARK);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BG_DARK);
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Header
        JLabel titleLabel = new JLabel("Quản lý Thẻ & Bảo mật");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(titleLabel);

        mainPanel.add(Box.createVerticalStrut(5));

        JLabel subtitleLabel = new JLabel("Thực hiện thay đổi mã PIN hoặc mở khóa thẻ hội viên bị khóa.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(TEXT_GRAY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(subtitleLabel);

        mainPanel.add(Box.createVerticalStrut(25));

        // Cards panel - 2 columns
        JPanel cardsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 450));

        cardsPanel.add(createChangePinCard());
        cardsPanel.add(createUnlockCard());

        mainPanel.add(cardsPanel);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(BG_DARK);
        scrollPane.getViewport().setBackground(BG_DARK);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createChangePinCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(CARD_BG);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                // Top border accent
                g2d.setColor(ACCENT_BLUE);
                g2d.fillRoundRect(0, 0, getWidth(), 4, 4, 4);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Icon + Title
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        headerPanel.setOpaque(false);
        JLabel iconLabel = new JLabel("💬");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        headerPanel.add(iconLabel);
        JLabel titleLabel = new JLabel("Đổi mã PIN khi hội viên quên");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(ACCENT_BLUE);
        headerPanel.add(titleLabel);
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(headerPanel);

        card.add(Box.createVerticalStrut(5));

        JLabel descLabel = new JLabel("Dùng khi hội viên QUÊN mã PIN cũ.");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(TEXT_GRAY);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(descLabel);

        card.add(Box.createVerticalStrut(20));

        // Admin password
        JLabel adminLabel = new JLabel("Mật khẩu Admin *");
        adminLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        adminLabel.setForeground(TEXT_GRAY);
        adminLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(adminLabel);

        card.add(Box.createVerticalStrut(8));

        JPasswordField adminPassField = createDarkPasswordField("Nhập mật khẩu quản trị viên");
        adminPassField.setAlignmentX(Component.LEFT_ALIGNMENT);
        adminPassField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        card.add(adminPassField);

        JLabel hintLabel1 = new JLabel("Nhập mật khẩu admin để xác thực quyền thay đổi.");
        hintLabel1.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hintLabel1.setForeground(TEXT_GRAY);
        hintLabel1.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(hintLabel1);

        card.add(Box.createVerticalStrut(20));

        // New PIN
        JLabel newPinLabel = new JLabel("PIN mới (6 số) *");
        newPinLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        newPinLabel.setForeground(TEXT_GRAY);
        newPinLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(newPinLabel);

        card.add(Box.createVerticalStrut(8));

        JPanel newPinPanel = createPinBoxes(newPinFields);
        newPinPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(newPinPanel);

        card.add(Box.createVerticalStrut(15));

        // Confirm PIN
        JLabel confirmPinLabel = new JLabel("Xác nhận PIN mới *");
        confirmPinLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        confirmPinLabel.setForeground(TEXT_GRAY);
        confirmPinLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(confirmPinLabel);

        card.add(Box.createVerticalStrut(8));

        JPanel confirmPinPanel = createPinBoxes(confirmPinFields);
        confirmPinPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(confirmPinPanel);

        card.add(Box.createVerticalStrut(25));

        // Button
        JButton changePinBtn = createDarkButton("ĐỔI PIN (ADMIN)", ACCENT_BLUE);
        changePinBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        changePinBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        changePinBtn.addActionListener(e -> {
            try {
                if (!cardComm.isConnected()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng kết nối thẻ!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String adminPass = new String(adminPassField.getPassword()).trim();
                String newPin = getPinFromFields(newPinFields);
                String confirmPin = getPinFromFields(confirmPinFields);

                if (adminPass.isEmpty() || newPin.length() != 6) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ thông tin!", "Lỗi",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (!newPin.equals(confirmPin)) {
                    JOptionPane.showMessageDialog(this, "PIN xác nhận không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (cardComm.adminResetMemberPin(adminPass, newPin)) {
                    JOptionPane.showMessageDialog(this, "Đổi mã PIN thành công!", "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                    clearPinFields(newPinFields);
                    clearPinFields(confirmPinFields);
                    adminPassField.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Đổi PIN thất bại. Kiểm tra mật khẩu admin.", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        card.add(changePinBtn);

        return card;
    }

    private JPanel createUnlockCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(CARD_BG);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                // Top border accent
                g2d.setColor(ACCENT_RED);
                g2d.fillRoundRect(0, 0, getWidth(), 4, 4, 4);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Icon + Title
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        headerPanel.setOpaque(false);
        JLabel iconLabel = new JLabel("🔓");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        headerPanel.add(iconLabel);
        JLabel titleLabel = new JLabel("Mở khóa thẻ (Admin)");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(ACCENT_RED);
        headerPanel.add(titleLabel);
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(headerPanel);

        card.add(Box.createVerticalStrut(5));

        JLabel descLabel = new JLabel("Sử dụng khi thẻ bị khóa tạm thời.");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(TEXT_GRAY);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(descLabel);

        card.add(Box.createVerticalStrut(15));

        // Warning box
        JPanel warningBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(60, 40, 45));
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
            }
        };
        warningBox.setOpaque(false);
        warningBox.setLayout(new BoxLayout(warningBox, BoxLayout.Y_AXIS));
        warningBox.setBorder(new EmptyBorder(15, 15, 15, 15));
        warningBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        warningBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JLabel warningIcon = new JLabel("⚠ Hệ thống sẽ tự động khóa thẻ khi hội viên nhập");
        warningIcon.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        warningIcon.setForeground(new Color(255, 150, 150));
        warningBox.add(warningIcon);

        JLabel warningText = new JLabel("   sai PIN quá số lần cho phép (thường là 5 lần).");
        warningText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        warningText.setForeground(new Color(255, 150, 150));
        warningBox.add(warningText);

        JLabel warningText2 = new JLabel("   Chức năng này sẽ reset bộ đếm lỗi và kích hoạt lại thẻ.");
        warningText2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        warningText2.setForeground(new Color(255, 150, 150));
        warningBox.add(warningText2);

        card.add(warningBox);

        card.add(Box.createVerticalStrut(20));

        // Admin password
        JLabel adminLabel = new JLabel("Mật khẩu Admin *");
        adminLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        adminLabel.setForeground(TEXT_GRAY);
        adminLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(adminLabel);

        card.add(Box.createVerticalStrut(8));

        JPasswordField adminPassField2 = createDarkPasswordField("Nhập mật khẩu quản trị viên");
        adminPassField2.setAlignmentX(Component.LEFT_ALIGNMENT);
        adminPassField2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        card.add(adminPassField2);

        JLabel hintLabel2 = new JLabel("Xác nhận quyền admin để mở khóa thẻ.");
        hintLabel2.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hintLabel2.setForeground(TEXT_GRAY);
        hintLabel2.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(hintLabel2);

        card.add(Box.createVerticalStrut(30));

        // Button
        JButton unlockBtn = createDarkButton("MỞ KHÓA THẺ", ACCENT_RED);
        unlockBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        unlockBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        unlockBtn.addActionListener(e -> {
            try {
                if (!cardComm.isConnected()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng kết nối thẻ!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String adminPass = new String(adminPassField2.getPassword()).trim();
                if (adminPass.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập mật khẩu admin!", "Lỗi",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (cardComm.unlockPin(adminPass)) {
                    JOptionPane.showMessageDialog(this, "Mở khóa thẻ thành công!\nThẻ đã được reset bộ đếm lỗi.",
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    adminPassField2.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Mở khóa thất bại. Kiểm tra mật khẩu admin.", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        card.add(unlockBtn);

        card.add(Box.createVerticalGlue());

        return card;
    }

    private JPanel createPinBoxes(JPasswordField[] fields) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panel.setOpaque(false);

        for (int i = 0; i < 6; i++) {
            final int index = i;
            fields[i] = new JPasswordField() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(INPUT_BG);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    super.paintComponent(g);
                }
            };
            fields[i].setOpaque(false);
            fields[i].setFont(new Font("Segoe UI", Font.BOLD, 20));
            fields[i].setForeground(TEXT_WHITE);
            fields[i].setCaretColor(TEXT_WHITE);
            fields[i].setHorizontalAlignment(JTextField.CENTER);
            fields[i].setBorder(new EmptyBorder(10, 10, 10, 10));
            fields[i].setPreferredSize(new Dimension(50, 50));
            fields[i].setMinimumSize(new Dimension(50, 50));
            fields[i].setMaximumSize(new Dimension(50, 50));
            fields[i].setEchoChar('●');

            // Auto-move to next field
            fields[i].addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyTyped(java.awt.event.KeyEvent e) {
                    char c = e.getKeyChar();
                    if (!Character.isDigit(c)) {
                        e.consume();
                        return;
                    }
                    if (new String(fields[index].getPassword()).length() >= 1) {
                        e.consume();
                        if (index < 5)
                            fields[index + 1].requestFocus();
                    }
                }

                public void keyReleased(java.awt.event.KeyEvent e) {
                    if (new String(fields[index].getPassword()).length() == 1 && index < 5) {
                        fields[index + 1].requestFocus();
                    }
                }
            });
            panel.add(fields[i]);
        }
        return panel;
    }

    private JPasswordField createDarkPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(INPUT_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
            }
        };
        field.setOpaque(false);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(TEXT_WHITE);
        field.setCaretColor(TEXT_WHITE);
        field.setBorder(new EmptyBorder(12, 15, 12, 15));
        field.setEchoChar('●');
        return field;
    }

    private JButton createDarkButton(String text, Color bgColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getModel().isRollover() ? bgColor.brighter() : bgColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private String getPinFromFields(JPasswordField[] fields) {
        StringBuilder sb = new StringBuilder();
        for (JPasswordField f : fields) {
            sb.append(new String(f.getPassword()));
        }
        return sb.toString();
    }

    private void clearPinFields(JPasswordField[] fields) {
        for (JPasswordField f : fields) {
            f.setText("");
        }
    }
}
