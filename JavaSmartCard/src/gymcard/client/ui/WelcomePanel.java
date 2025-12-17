package gymcard.client.ui;

import gymcard.client.CardCommunicator;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Welcome Panel - Màn hình chính với 2 nút Admin và User
 */
public class WelcomePanel extends JPanel {

    private final CardCommunicator cardComm;
    private final Runnable onAdminClick;
    private final Runnable onUserClick;
    private final Runnable onConnectClick;
    private JButton connectBtn;
    private JLabel statusLabel;

    public WelcomePanel(CardCommunicator cardComm, Runnable onAdminClick, Runnable onUserClick,
            Runnable onConnectClick) {
        this.cardComm = cardComm;
        this.onAdminClick = onAdminClick;
        this.onUserClick = onUserClick;
        this.onConnectClick = onConnectClick;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Main gradient background panel
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient from dark blue-purple
                GradientPaint gp = new GradientPaint(0, 0, new Color(30, 30, 60),
                        getWidth(), getHeight(), new Color(80, 40, 120));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(20, 30, 15, 30));

        // Logo on left
        JLabel logoLabel = new JLabel("GYM CARD SYSTEM");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        logoLabel.setForeground(Color.WHITE);
        topBar.add(logoLabel, BorderLayout.WEST);

        // Connect panel on right
        JPanel connectPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        connectPanel.setOpaque(false);

        statusLabel = new JLabel("Chưa kết nối");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(255, 100, 100));
        statusLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 100, 100, 100), 1, true),
                new EmptyBorder(5, 12, 5, 12)));
        connectPanel.add(statusLabel);

        connectBtn = createConnectButton("Kết nối thẻ");
        connectBtn.addActionListener(e -> {
            onConnectClick.run();
            updateConnectionStatus();
        });
        connectPanel.add(connectBtn);

        topBar.add(connectPanel, BorderLayout.EAST);
        mainPanel.add(topBar, BorderLayout.NORTH);

        // Center content
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(new EmptyBorder(30, 50, 30, 50));

        // Welcome title
        JLabel titleLabel = new JLabel("Chào mừng trở lại");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(titleLabel);

        centerPanel.add(Box.createVerticalStrut(15));

        // Subtitle
        JLabel subtitleLabel = new JLabel("Vui lòng chọn vai trò để tiếp tục truy cập hệ thống");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(180, 180, 200));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(subtitleLabel);

        centerPanel.add(Box.createVerticalStrut(50));

        // Cards panel
        JPanel cardsPanel = new JPanel(new GridLayout(1, 2, 30, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setMaximumSize(new Dimension(750, 320));

        // Admin card
        JPanel adminCard = createRoleCard(
                new Color(46, 204, 113),
                "QUẢN TRỊ VIÊN",
                "Quản lý hệ thống, đăng ký thẻ thành viên mới và quản lý thông tin phòng tập.",
                "Vào trang quản trị",
                new Color(155, 89, 182),
                e -> onAdminClick.run());
        cardsPanel.add(adminCard);

        // User card
        JPanel userCard = createRoleCard(
                new Color(155, 89, 182),
                "HỘI VIÊN",
                "Check-in ra vào, xem thông tin cá nhân, lịch sử tập luyện và nạp tiền vào tài khoản.",
                "Vào trang hội viên",
                new Color(52, 152, 219),
                e -> {
                    if (!cardComm.isConnected()) {
                        JOptionPane.showMessageDialog(WelcomePanel.this,
                                "Vui lòng kết nối thẻ trước!",
                                "Chưa kết nối", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    onUserClick.run();
                });
        cardsPanel.add(userCard);

        // Wrapper to center the cards
        JPanel cardsWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        cardsWrapper.setOpaque(false);
        cardsWrapper.add(cardsPanel);
        centerPanel.add(cardsWrapper);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Footer
        JPanel footerPanel = new JPanel();
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(10, 20, 25, 20));

        JLabel footerLabel = new JLabel("© 2024 Gym Card Management System - JavaCard SmartCard. All rights reserved.");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footerLabel.setForeground(new Color(140, 140, 160));
        footerPanel.add(footerLabel);

        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createRoleCard(Color iconColor, String title, String description,
            String buttonText, Color buttonColor, ActionListener onClick) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Semi-transparent dark background
                g2d.setColor(new Color(40, 40, 70, 200));
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(35, 30, 35, 30));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Icon circle
        boolean isAdmin = title.contains("QUẢN");
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw circle background
                g2d.setColor(iconColor);
                g2d.fillOval(0, 0, 50, 50);

                // Draw icon inside
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2));

                if (isAdmin) {
                    // Draw 3 horizontal lines (menu icon)
                    int startX = 15, endX = 35;
                    g2d.drawLine(startX, 18, endX, 18);
                    g2d.drawLine(startX, 25, endX, 25);
                    g2d.drawLine(startX, 32, endX, 32);
                } else {
                    // Draw card icon (rectangle with chip)
                    g2d.drawRoundRect(12, 15, 26, 20, 4, 4);
                    g2d.fillRect(16, 20, 8, 6);
                }
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(50, 50);
            }

            @Override
            public Dimension getMaximumSize() {
                return new Dimension(50, 50);
            }
        };
        iconPanel.setOpaque(false);
        iconPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(iconPanel);

        card.add(Box.createVerticalStrut(20));

        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLabel);

        card.add(Box.createVerticalStrut(15));

        // Description
        JLabel descLabel = new JLabel("<html><center>" + description + "</center></html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(new Color(180, 180, 200));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(descLabel);

        card.add(Box.createVerticalStrut(25));

        // Button
        JButton btn = createRoleButton(buttonText, buttonColor);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.addActionListener(onClick);
        card.add(btn);

        // Hover effect
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onClick.actionPerformed(null);
            }
        });

        return card;
    }

    private JButton createRoleButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2d.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(bgColor.brighter());
                } else {
                    g2d.setColor(bgColor);
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(180, 40));
        button.setMaximumSize(new Dimension(180, 40));
        return button;
    }

    private JButton createConnectButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bgColor = new Color(80, 80, 120);
                if (getModel().isPressed()) {
                    g2d.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(bgColor.brighter());
                } else {
                    g2d.setColor(bgColor);
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(100, 32));
        return button;
    }

    public void updateConnectionStatus() {
        if (cardComm.isConnected()) {
            statusLabel.setText("Đã kết nối");
            statusLabel.setForeground(new Color(100, 220, 100));
            statusLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(100, 220, 100, 100), 1, true),
                    new EmptyBorder(5, 12, 5, 12)));
            connectBtn.setText("Ngắt kết nối");
        } else {
            statusLabel.setText("Chưa kết nối");
            statusLabel.setForeground(new Color(255, 100, 100));
            statusLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(255, 100, 100, 100), 1, true),
                    new EmptyBorder(5, 12, 5, 12)));
            connectBtn.setText("Kết nối thẻ");
        }
    }
}
