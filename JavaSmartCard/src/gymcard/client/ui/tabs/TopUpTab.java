package gymcard.client.ui.tabs;

import gymcard.client.CardCommunicator;
import gymcard.client.ui.BaseTabPanel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Tab Nạp tiền - Modern UI Design matching provided mockup
 */
public class TopUpTab extends BaseTabPanel {

    private static final Color BG_LIGHT = new Color(248, 250, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color PRIMARY_GREEN = new Color(16, 185, 129);
    private static final Color PRIMARY_GREEN_DARK = new Color(5, 150, 105);
    private static final Color TEXT_DARK = new Color(30, 41, 59);
    private static final Color TEXT_GRAY = new Color(100, 116, 139);
    private static final Color BORDER_COLOR = new Color(226, 232, 240);

    private JLabel balanceLabel;
    private JLabel cardNumberLabel;
    private JTextField amountField;
    private JButton[] quickButtons;
    private int selectedQuickIndex = 1; // Default 100k selected

    public TopUpTab(CardCommunicator cardComm) {
        super(cardComm);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(BG_LIGHT);

        // Main scroll pane
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(BG_LIGHT);
        mainContent.setBorder(new EmptyBorder(20, 30, 20, 30));

        // 1. Header Card with balance and card visual
        mainContent.add(createHeaderCard());
        mainContent.add(Box.createVerticalStrut(25));

        // 2. Quick amount section
        mainContent.add(createQuickAmountSection());
        mainContent.add(Box.createVerticalStrut(20));

        // 3. Amount input section
        mainContent.add(createAmountInputSection());
        mainContent.add(Box.createVerticalStrut(20));

        // 4. Submit button
        mainContent.add(createSubmitButton());
        mainContent.add(Box.createVerticalStrut(25));

        // 5. Footer
        mainContent.add(createFooter());

        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(BG_LIGHT);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient background
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(30, 64, 80),
                        getWidth(), getHeight(), new Color(45, 90, 110));
                g2.setPaint(gradient);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout(20, 0));
        card.setBorder(new EmptyBorder(25, 30, 25, 30));
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(0, 160));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        // Left side - Balance info
        JPanel balancePanel = new JPanel();
        balancePanel.setLayout(new BoxLayout(balancePanel, BoxLayout.Y_AXIS));
        balancePanel.setOpaque(false);

        // Wallet icon + title
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);
        JLabel walletIcon = new JLabel("💳");
        walletIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        JLabel titleLabel = new JLabel("SỐ DƯ TÀI KHOẢN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(new Color(255, 255, 255, 180));
        titleRow.add(walletIcon);
        titleRow.add(titleLabel);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        balancePanel.add(titleRow);
        balancePanel.add(Box.createVerticalStrut(10));

        // Balance value
        balanceLabel = new JLabel("0 VND");
        balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        balanceLabel.setForeground(PRIMARY_GREEN);
        balanceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        balancePanel.add(balanceLabel);

        card.add(balancePanel, BorderLayout.WEST);

        // Right side - Card visual
        JPanel cardVisual = createCardVisual();
        card.add(cardVisual, BorderLayout.EAST);

        // Wrap in container for proper sizing
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(card, BorderLayout.CENTER);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        return wrapper;
    }

    private JPanel createCardVisual() {
        JPanel cardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Card gradient (green)
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(34, 197, 94),
                        getWidth(), getHeight(), new Color(22, 163, 74));
                g2.setPaint(gradient);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                g2.dispose();
            }
        };
        cardPanel.setLayout(new BorderLayout());
        cardPanel.setPreferredSize(new Dimension(200, 110));
        cardPanel.setBorder(new EmptyBorder(12, 15, 12, 15));
        cardPanel.setOpaque(false);

        // Top row - wifi icon + title
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel wifiIcon = new JLabel("📶");
        wifiIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        topRow.add(wifiIcon, BorderLayout.WEST);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        JLabel gymLabel = new JLabel("GYM MEMBERSHIP");
        gymLabel.setFont(new Font("Segoe UI", Font.PLAIN, 8));
        gymLabel.setForeground(new Color(255, 255, 255, 180));
        JLabel smartLabel = new JLabel("SMARTCARD");
        smartLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        smartLabel.setForeground(Color.WHITE);
        titlePanel.add(gymLabel);
        titlePanel.add(smartLabel);
        topRow.add(titlePanel, BorderLayout.EAST);

        cardPanel.add(topRow, BorderLayout.NORTH);

        // Middle - chip
        JPanel chipPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        chipPanel.setOpaque(false);
        JLabel chipLabel = new JLabel("▣");
        chipLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        chipLabel.setForeground(new Color(250, 204, 21));
        chipPanel.add(chipLabel);
        cardPanel.add(chipPanel, BorderLayout.CENTER);

        // Bottom - card number + reload button
        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setOpaque(false);

        JPanel numberPanel = new JPanel();
        numberPanel.setLayout(new BoxLayout(numberPanel, BoxLayout.Y_AXIS));
        numberPanel.setOpaque(false);
        JLabel cardNumTitle = new JLabel("CARD NUMBER");
        cardNumTitle.setFont(new Font("Segoe UI", Font.PLAIN, 7));
        cardNumTitle.setForeground(new Color(255, 255, 255, 150));
        cardNumberLabel = new JLabel("•••• 0000");
        cardNumberLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        cardNumberLabel.setForeground(Color.WHITE);
        numberPanel.add(cardNumTitle);
        numberPanel.add(cardNumberLabel);
        bottomRow.add(numberPanel, BorderLayout.WEST);

        // Reload button
        JLabel reloadBtn = new JLabel("↻ TẢI DỮ LIỆU") {
            private boolean hover = false;
            {
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        refreshData();
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hover = true;
                        setForeground(Color.WHITE);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hover = false;
                        setForeground(new Color(255, 255, 255, 200));
                    }
                });
            }
        };
        reloadBtn.setFont(new Font("Segoe UI", Font.BOLD, 9));
        reloadBtn.setForeground(new Color(255, 255, 255, 200));
        bottomRow.add(reloadBtn, BorderLayout.EAST);

        cardPanel.add(bottomRow, BorderLayout.SOUTH);

        return cardPanel;
    }

    private JPanel createQuickAmountSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Title with icon
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);
        JLabel icon = new JLabel("⚡");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        JLabel title = new JLabel("Chọn mức nạp nhanh");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(TEXT_DARK);
        titleRow.add(icon);
        titleRow.add(title);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(titleRow);
        section.add(Box.createVerticalStrut(15));

        // Quick buttons row
        JPanel buttonsRow = new JPanel(new GridLayout(1, 4, 15, 0));
        buttonsRow.setOpaque(false);
        buttonsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        buttonsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] amounts = { "50k", "100k", "200k", "500k" };
        long[] values = { 50000L, 100000L, 200000L, 500000L };
        quickButtons = new JButton[4];

        for (int i = 0; i < amounts.length; i++) {
            final int index = i;
            final long value = values[i];

            quickButtons[i] = new JButton(amounts[i]) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    if (selectedQuickIndex == index) {
                        // Selected state - filled green
                        g2.setColor(PRIMARY_GREEN);
                        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                        g2.setColor(Color.WHITE);
                    } else {
                        // Normal state - outlined
                        g2.setColor(CARD_BG);
                        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                        g2.setColor(BORDER_COLOR);
                        g2.setStroke(new BasicStroke(1.5f));
                        g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 12, 12));
                        g2.setColor(TEXT_DARK);
                    }

                    FontMetrics fm = g2.getFontMetrics(getFont());
                    int x = (getWidth() - fm.stringWidth(getText())) / 2;
                    int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(getText(), x, y);
                    g2.dispose();
                }
            };
            quickButtons[i].setFont(new Font("Segoe UI", Font.BOLD, 14));
            quickButtons[i].setPreferredSize(new Dimension(0, 50));
            quickButtons[i].setBorderPainted(false);
            quickButtons[i].setContentAreaFilled(false);
            quickButtons[i].setFocusPainted(false);
            quickButtons[i].setCursor(new Cursor(Cursor.HAND_CURSOR));
            quickButtons[i].addActionListener(e -> {
                selectedQuickIndex = index;
                amountField.setText(String.valueOf(value));
                for (JButton btn : quickButtons)
                    btn.repaint();
            });
            buttonsRow.add(quickButtons[i]);
        }

        section.add(buttonsRow);
        return section;
    }

    private JPanel createAmountInputSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Title with icon
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);
        JLabel icon = new JLabel("✏");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        JLabel title = new JLabel("Hoặc nhập số tiền (VND)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(TEXT_DARK);
        titleRow.add(icon);
        titleRow.add(title);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(titleRow);
        section.add(Box.createVerticalStrut(12));

        // Input field with prefix
        JPanel inputWrapper = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 12, 12));
                g2.dispose();
            }
        };
        inputWrapper.setLayout(new BorderLayout());
        inputWrapper.setOpaque(false);
        inputWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        inputWrapper.setPreferredSize(new Dimension(0, 55));
        inputWrapper.setBorder(new EmptyBorder(0, 15, 0, 15));
        inputWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Currency prefix
        JLabel prefixLabel = new JLabel("đ");
        prefixLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        prefixLabel.setForeground(TEXT_GRAY);
        prefixLabel.setBorder(new EmptyBorder(0, 0, 0, 10));
        inputWrapper.add(prefixLabel, BorderLayout.WEST);

        // Amount field
        amountField = new JTextField("100000");
        amountField.setFont(new Font("Segoe UI", Font.BOLD, 18));
        amountField.setForeground(TEXT_DARK);
        amountField.setBorder(null);
        amountField.setOpaque(false);
        amountField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                // Clear quick selection when typing custom amount
                try {
                    long val = Long.parseLong(amountField.getText().replace(",", ""));
                    long[] quickVals = { 50000L, 100000L, 200000L, 500000L };
                    selectedQuickIndex = -1;
                    for (int i = 0; i < quickVals.length; i++) {
                        if (quickVals[i] == val) {
                            selectedQuickIndex = i;
                            break;
                        }
                    }
                    for (JButton btn : quickButtons)
                        btn.repaint();
                } catch (NumberFormatException ex) {
                    selectedQuickIndex = -1;
                    for (JButton btn : quickButtons)
                        btn.repaint();
                }
            }
        });
        inputWrapper.add(amountField, BorderLayout.CENTER);

        section.add(inputWrapper);
        section.add(Box.createVerticalStrut(8));

        // Helper text
        JLabel helperText = new JLabel("Số tiền nạp tối thiểu là 10.000 VND");
        helperText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        helperText.setForeground(TEXT_GRAY);
        helperText.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(helperText);

        return section;
    }

    private JPanel createSubmitButton() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton submitBtn = new JButton("💳  NẠP TIỀN NGAY") {
            private boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hover = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hover = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color startColor = hover ? PRIMARY_GREEN : PRIMARY_GREEN;
                Color endColor = hover ? new Color(4, 120, 87) : PRIMARY_GREEN_DARK;

                GradientPaint gradient = new GradientPaint(
                        0, 0, startColor,
                        getWidth(), 0, endColor);
                g2.setPaint(gradient);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));

                // Text
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        submitBtn.setPreferredSize(new Dimension(0, 55));
        submitBtn.setBorderPainted(false);
        submitBtn.setContentAreaFilled(false);
        submitBtn.setFocusPainted(false);
        submitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitBtn.addActionListener(e -> processTopUp());

        wrapper.add(submitBtn, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel footerText = new JLabel("Cần hỗ trợ? Liên hệ hotline ");
        footerText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        footerText.setForeground(TEXT_GRAY);

        JLabel hotline = new JLabel("1900-1234");
        hotline.setFont(new Font("Segoe UI", Font.BOLD, 13));
        hotline.setForeground(PRIMARY_GREEN);

        footer.add(footerText);
        footer.add(hotline);

        return footer;
    }

    private void processTopUp() {
        // Validate amount
        long amount;
        try {
            String text = amountField.getText().replace(",", "").trim();
            amount = Long.parseLong(text);
            if (amount < 10000) {
                JOptionPane.showMessageDialog(this,
                        "Số tiền nạp tối thiểu là 10.000 VND!",
                        "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (amount > CardCommunicator.MAX_BALANCE) {
                JOptionPane.showMessageDialog(this,
                        "Số tiền vượt quá giới hạn cho phép!",
                        "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập số tiền hợp lệ!",
                    "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Process top-up directly without admin authentication
        try {
            if (cardComm.addBalance(amount)) {
                long newBalance = cardComm.getBalance();
                balanceLabel.setText(String.format("%,d VND", newBalance));

                // Update card number display
                try {
                    String cardId = cardComm.getMemberInfo().phone;
                    if (cardId != null && cardId.length() >= 4) {
                        cardNumberLabel.setText("•••• " + cardId.substring(cardId.length() - 4));
                    }
                } catch (Exception ignored) {
                }

                JOptionPane.showMessageDialog(this,
                        "Nạp tiền thành công!\n\nSố tiền: " + String.format("%,d", amount)
                                + " VND\nSố dư mới: " + String.format("%,d", newBalance) + " VND",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Nạp tiền thất bại!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Public method to refresh balance data (called by UserPanel on tab change)
     */
    public void refreshData() {
        try {
            long balance = cardComm.getBalance();
            balanceLabel.setText(String.format("%,d VND", balance));

            // Update card number
            try {
                String cardId = cardComm.getMemberInfo().phone;
                if (cardId != null && cardId.length() >= 4) {
                    cardNumberLabel.setText("•••• " + cardId.substring(cardId.length() - 4));
                }
            } catch (Exception ignored) {
            }

            log("Số dư: " + balance + " VND");
        } catch (Exception ex) {
            log("LỖI: " + ex.getMessage());
        }
    }
}
