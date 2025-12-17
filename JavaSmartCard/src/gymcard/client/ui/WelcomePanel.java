package gymcard.client.ui;

import gymcard.client.CardCommunicator;
import java.awt.*;
import java.awt.event.*;
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
                GradientPaint gp = new GradientPaint(0, 0, new Color(44, 62, 80),
                        getWidth(), getHeight(), new Color(142, 68, 173));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        // Top bar with connect button
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(15, 20, 10, 20));

        // Logo/Title on left
        JLabel titleLabel = new JLabel("GYM CARD SYSTEM");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        topBar.add(titleLabel, BorderLayout.WEST);

        // Connect button on right
        JPanel connectPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        connectPanel.setOpaque(false);

        statusLabel = new JLabel("Chưa kết nối");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(231, 76, 60));
        connectPanel.add(statusLabel);

        connectBtn = createStyledButton("Kết nối thẻ", new Color(52, 152, 219), 14);
        connectBtn.setPreferredSize(new Dimension(150, 40));
        connectBtn.addActionListener(e -> {
            onConnectClick.run();
            updateConnectionStatus();
        });
        connectPanel.add(connectBtn);

        topBar.add(connectPanel, BorderLayout.EAST);
        mainPanel.add(topBar, BorderLayout.NORTH);

        // Center panel with two big buttons
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 40, 20, 40);

        // Admin button
        JPanel adminCard = createBigCard("QUẢN TRỊ VIÊN", "Quản lý hệ thống, đăng ký thẻ",
                new Color(46, 204, 113), e -> onAdminClick.run());
        gbc.gridx = 0;
        gbc.gridy = 0;
        centerPanel.add(adminCard, gbc);

        // User button
        JPanel userCard = createBigCard("HỘI VIÊN", "Check-in, xem thông tin, nạp tiền",
                new Color(52, 152, 219), e -> {
                    if (!cardComm.isConnected()) {
                        JOptionPane.showMessageDialog(this,
                                "Vui lòng kết nối thẻ trước!",
                                "Chưa kết nối", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    onUserClick.run();
                });
        gbc.gridx = 1;
        centerPanel.add(userCard, gbc);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Bottom info
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(10, 20, 20, 20));

        JLabel infoLabel = new JLabel("© 2024 Gym Card Management System - JavaCard SmartCard");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoLabel.setForeground(new Color(189, 195, 199));
        bottomPanel.add(infoLabel);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createBigCard(String title, String description, Color color, ActionListener onClick) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 20));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2d.setColor(color);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 30, 30);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(50, 50, 50, 50));
        card.setPreferredSize(new Dimension(280, 250));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLabel);

        card.add(Box.createVerticalStrut(20));

        // Description
        JLabel descLabel = new JLabel("<html><center>" + description + "</center></html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descLabel.setForeground(new Color(189, 195, 199));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(descLabel);

        card.add(Box.createVerticalStrut(30));

        // Button
        JButton btn = createStyledButton("Vào", color, 16);
        btn.setPreferredSize(new Dimension(140, 45));
        btn.setMaximumSize(new Dimension(140, 45));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.addActionListener(onClick);
        card.add(btn);

        // Hover effect
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBorder(new EmptyBorder(45, 45, 45, 45));
                card.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBorder(new EmptyBorder(50, 50, 50, 50));
                card.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                onClick.actionPerformed(null);
            }
        });

        return card;
    }

    private JButton createStyledButton(String text, Color bgColor, int fontSize) {
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
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    public void updateConnectionStatus() {
        if (cardComm.isConnected()) {
            statusLabel.setText("Đã kết nối");
            statusLabel.setForeground(new Color(46, 204, 113));
            connectBtn.setText("Ngắt kết nối");
        } else {
            statusLabel.setText("Chưa kết nối");
            statusLabel.setForeground(new Color(231, 76, 60));
            connectBtn.setText("Kết nối thẻ");
        }
    }
}
