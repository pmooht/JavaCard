package gymcard.client.ui.tabs;

import gymcard.client.CardCommunicator;
import gymcard.client.MemberInfo;
import gymcard.client.ui.BaseTabPanel;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Tab thông tin cá nhân - Modern UI Design
 */
public class InfoTab extends BaseTabPanel {

    // Colors
    private static final Color BG_LIGHT = new Color(248, 250, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(44, 62, 80);
    private static final Color TEXT_GRAY = new Color(127, 140, 141);
    private static final Color ACCENT_BLUE = new Color(52, 152, 219);
    private static final Color ACCENT_GREEN = new Color(46, 204, 113);
    private static final Color ACCENT_ORANGE = new Color(243, 156, 18);

    private JLabel avatarLabel;
    private JLabel nameLabel;
    private JLabel nameValueLabel, dobValueLabel, phoneValueLabel, genderValueLabel, addressValueLabel;
    private JLabel balanceValueLabel, cardIdValueLabel;
    private MemberInfo currentMember;
    private byte[] pendingAvatar;

    public InfoTab(CardCommunicator cardComm) {
        super(cardComm);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(BG_LIGHT);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BG_LIGHT);
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Title
        JLabel titleLabel = new JLabel("THÔNG TIN CÁ NHÂN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(TEXT_DARK);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(titleLabel);

        mainPanel.add(Box.createVerticalStrut(20));

        // Main card - profile + info
        JPanel mainCard = createMainCard();
        mainCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(mainCard);

        mainPanel.add(Box.createVerticalStrut(20));

        // Bottom stat cards
        JPanel statsPanel = createStatsPanel();
        statsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(statsPanel);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(BG_LIGHT);
        scrollPane.getViewport().setBackground(BG_LIGHT);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createMainCard() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(CARD_BG);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(25, 25, 25, 25));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));

        // Left - Profile section
        JPanel leftPanel = createProfileSection();
        card.add(leftPanel, BorderLayout.WEST);

        // Right - Info details
        JPanel rightPanel = createInfoSection();
        card.add(rightPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createProfileSection() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(250, 0));
        panel.setBorder(new EmptyBorder(20, 20, 20, 30));

        // Avatar
        avatarLabel = new JLabel("👤") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Circle background
               // g2d.setColor(new Color(230, 240, 250));
               // g2d.fillOval(0, 0, getWidth(), getHeight());
                // Draw image or text
                super.paintComponent(g);
            }
        };
        avatarLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avatarLabel.setPreferredSize(new Dimension(130, 130));
        avatarLabel.setMinimumSize(new Dimension(130, 130));
        avatarLabel.setMaximumSize(new Dimension(130, 130));
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(avatarLabel);

        panel.add(Box.createVerticalStrut(15));

        // Name
        nameLabel = new JLabel("Họ và tên");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nameLabel.setForeground(TEXT_DARK);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(nameLabel);

        panel.add(Box.createVerticalStrut(15));

        // Status badge - only active
        JLabel activeBadge = createBadge("HOẠT ĐỘNG", ACCENT_GREEN);
        activeBadge.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(activeBadge);

        return panel;
    }

    private JLabel createBadge(String text, Color color) {
        JLabel badge = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badge.setForeground(Color.WHITE);
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        badge.setBorder(new EmptyBorder(5, 12, 5, 12));
        return badge;
    }

    private JPanel createInfoSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 20, 0, 0));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        JLabel infoTitle = new JLabel("Thông tin chi tiết");
        infoTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        infoTitle.setForeground(TEXT_DARK);
        titlePanel.add(infoTitle);
        JLabel infoSubtitle = new JLabel("Quản lý thông tin cá nhân và liên hệ");
        infoSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoSubtitle.setForeground(TEXT_GRAY);
        titlePanel.add(infoSubtitle);
        headerPanel.add(titlePanel, BorderLayout.WEST);

        JButton editQuickBtn = createLinkButton("Chỉnh sửa nhanh", ACCENT_BLUE);
        headerPanel.add(editQuickBtn, BorderLayout.EAST);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Fields grid
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);
        fieldsPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 30);
        gbc.anchor = GridBagConstraints.WEST;

        // Row 1: Name + DOB
        gbc.gridx = 0;
        gbc.gridy = 0;
        fieldsPanel.add(createFieldCard("HỌ VÀ TÊN", "👤", nameValueLabel = new JLabel("---")), gbc);
        gbc.gridx = 1;
        fieldsPanel.add(createFieldCard("NGÀY SINH", "📅", dobValueLabel = new JLabel("---")), gbc);

        // Row 2: Phone + Gender
        gbc.gridx = 0;
        gbc.gridy = 1;
        fieldsPanel.add(createFieldCard("SỐ ĐIỆN THOẠI", "📞", phoneValueLabel = new JLabel("---")), gbc);
        gbc.gridx = 1;
        fieldsPanel.add(createFieldCard("GIỚI TÍNH", "👤", genderValueLabel = new JLabel("---")), gbc);

        // Row 3: Address (full width)
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        fieldsPanel.add(createFieldCard("ĐỊA CHỈ", "🏠", addressValueLabel = new JLabel("---")), gbc);

        panel.add(fieldsPanel, BorderLayout.CENTER);

        // Footer buttons
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton loadBtn = createActionButton("Tải thông tin từ thẻ", ACCENT_BLUE);
        loadBtn.addActionListener(e -> refreshData());
        footerPanel.add(loadBtn);

        JButton editBtn = createActionButton("Chỉnh sửa", ACCENT_ORANGE);
        editBtn.addActionListener(e -> showEditDialog());
        footerPanel.add(editBtn);

        panel.add(footerPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void showEditDialog() {
        // Create edit dialog with modern styling
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chỉnh sửa thông tin", true);
        dialog.setSize(480, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(52, 73, 94));
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel headerLabel = new JLabel("Chỉnh sửa thông tin cá nhân");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel, BorderLayout.WEST);
        dialog.add(headerPanel, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(25, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Avatar section
        final byte[][] newAvatarBytes = new byte[1][];
        JLabel avatarPreview = new JLabel();
        avatarPreview.setPreferredSize(new Dimension(90, 90));
        avatarPreview.setHorizontalAlignment(SwingConstants.CENTER);
        avatarPreview.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        avatarPreview.setOpaque(true);
        avatarPreview.setBackground(new Color(245, 248, 250));
        avatarPreview.setText("Ảnh");

        if (currentMember != null && currentMember.avatarBytes != null) {
            try {
                java.awt.image.BufferedImage img = javax.imageio.ImageIO
                        .read(new java.io.ByteArrayInputStream(currentMember.avatarBytes));
                if (img != null) {
                    Image scaled = img.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                    avatarPreview.setIcon(new ImageIcon(scaled));
                    avatarPreview.setText("");
                }
            } catch (Exception e) {
                avatarPreview.setText("Ảnh");
            }
        }

        JButton changeAvatarBtn = new JButton("Đổi ảnh");
        changeAvatarBtn.setBackground(new Color(52, 152, 219));
        changeAvatarBtn.setForeground(Color.BLUE);
        changeAvatarBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        changeAvatarBtn.setFocusPainted(false);
        changeAvatarBtn.setPreferredSize(new Dimension(90, 32));
        changeAvatarBtn.addActionListener(ev -> {
            try {
                byte[] newAvatar = chooseAndCompressAvatar();
                if (newAvatar != null) {
                    newAvatarBytes[0] = newAvatar;
                    java.awt.image.BufferedImage img = javax.imageio.ImageIO
                            .read(new java.io.ByteArrayInputStream(newAvatar));
                    if (img != null) {
                        Image scaled = img.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                        avatarPreview.setIcon(new ImageIcon(scaled));
                        avatarPreview.setText("");
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi chọn ảnh: " + ex.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel avatarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        avatarPanel.setOpaque(false);
        avatarPanel.add(avatarPreview);
        avatarPanel.add(changeAvatarBtn);

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel avatarLbl = new JLabel("Ảnh đại diện:");
        avatarLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(avatarLbl, gbc);
        gbc.gridx = 1;
        formPanel.add(avatarPanel, gbc);

        // Name field
        JTextField nameField = new JTextField(20);
        nameField.setText(nameValueLabel.getText().equals("---") ? "" : nameValueLabel.getText());
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        addFormRow(formPanel, gbc, 1, "Họ và tên *", nameField);

        // DOB field
        JTextField dobField = new JTextField(20);
        dobField.setText(dobValueLabel.getText().equals("---") ? "" : dobValueLabel.getText());
        dobField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dobField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        addFormRow(formPanel, gbc, 2, "Ngày sinh (dd/MM/yyyy) *", dobField);

        // Phone field
        JTextField phoneField = new JTextField(20);
        phoneField.setText(phoneValueLabel.getText().equals("---") ? "" : phoneValueLabel.getText());
        phoneField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        phoneField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        addFormRow(formPanel, gbc, 3, "Số điện thoại *", phoneField);

        // Address field
        JTextArea addressField = new JTextArea(3, 20);
        addressField.setText(addressValueLabel.getText().equals("---") ? "" : addressValueLabel.getText());
        addressField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        addressField.setLineWrap(true);
        addressField.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        JScrollPane addressScroll = new JScrollPane(addressField);
        addressScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel addrLbl = new JLabel("Địa chỉ:");
        addrLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(addrLbl, gbc);
        gbc.gridx = 1;
        formPanel.add(addressScroll, gbc);

        dialog.add(formPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(new Color(248, 249, 250));
        buttonPanel.setBorder(new EmptyBorder(5, 20, 10, 20));

        JButton cancelBtn = new JButton("Hủy");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancelBtn.setPreferredSize(new Dimension(90, 38));
        cancelBtn.setFocusPainted(false);
        cancelBtn.addActionListener(ev -> dialog.dispose());
        buttonPanel.add(cancelBtn);

        JButton saveBtn = new JButton("Lưu lên thẻ");
        saveBtn.setBackground(new Color(0, 128, 64));
        saveBtn.setForeground(Color.BLUE);
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveBtn.setPreferredSize(new Dimension(130, 38));
        saveBtn.setFocusPainted(false);
        saveBtn.addActionListener(ev -> {
            try {
                String name = nameField.getText().trim();
                String dob = dobField.getText().trim();
                String phone = phoneField.getText().trim();
                String address = addressField.getText().trim();

                if (name.isEmpty() || dob.isEmpty() || phone.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đầy đủ thông tin bắt buộc!", "Lỗi",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                byte[] avatarToSave = newAvatarBytes[0] != null ? newAvatarBytes[0] : null;
                cardComm.setMemberInfo(name, dob, phone, address, avatarToSave);

                if (newAvatarBytes[0] != null) {
                    setAvatarToLabel(avatarLabel, newAvatarBytes[0]);
                }

                nameLabel.setText(name);
                nameValueLabel.setText(name);
                dobValueLabel.setText(dob);
                phoneValueLabel.setText(phone);
                addressValueLabel.setText(address.isEmpty() ? "---" : address);

                JOptionPane.showMessageDialog(dialog, "Đã lưu thông tin lên thẻ thành công!", "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi lưu thông tin: " + ex.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(saveBtn);

        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(field, gbc);
    }

    private JPanel createFieldCard(String label, String icon, JLabel valueLabel) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(248, 250, 252));
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(12, 15, 12, 15));
        card.setPreferredSize(new Dimension(200, 70));

        JLabel labelL = new JLabel(label);
        labelL.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        labelL.setForeground(TEXT_GRAY);
        labelL.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(labelL);

        card.add(Box.createVerticalStrut(5));

        JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        valuePanel.setOpaque(false);
        valuePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel iconL = new JLabel(icon);
        iconL.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        valuePanel.add(iconL);

        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        valueLabel.setForeground(TEXT_DARK);
        valuePanel.add(valueLabel);

        card.add(valuePanel);

        return card;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 15, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        panel.add(createStatCard("SO DU", balanceValueLabel = new JLabel("-- d"), new Color(243, 156, 18)));
        panel.add(createStatCard("MA THE", cardIdValueLabel = new JLabel("---"), new Color(155, 89, 182)));

        return panel;
    }

    private JPanel createStatCard(String label, JLabel valueLabel, Color iconColor) {
        JPanel card = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(CARD_BG);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(10, 15, 10, 15));

        // Icon box
        JPanel iconBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(iconColor.getRed(), iconColor.getGreen(), iconColor.getBlue(), 30));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }
        };
        iconBox.setOpaque(false);
        iconBox.setPreferredSize(new Dimension(45, 45));
        iconBox.setLayout(new GridBagLayout());
        JLabel iconLabel = new JLabel("📋");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        iconBox.add(iconLabel);
        card.add(iconBox);

        // Text
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel labelL = new JLabel(label);
        labelL.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        labelL.setForeground(TEXT_GRAY);
        textPanel.add(labelL);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        valueLabel.setForeground(TEXT_DARK);
        textPanel.add(valueLabel);

        card.add(textPanel);

        return card;
    }

    private JButton createLinkButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(color);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createOutlineButton(String text, Color color) {
        JButton btn = new JButton("📥 " + text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(CARD_BG);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2d.setColor(color);
                g2d.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(color);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 36));
        return btn;
    }

    private JButton createFilledButton(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(110, 36));
        return btn;
    }

    private JButton createActionButton(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 38));
        return btn;
    }

    private void changeAvatar() {
        try {
            byte[] newAvatar = chooseAndCompressAvatar();
            if (newAvatar != null) {
                pendingAvatar = newAvatar;
                setAvatarToLabel(avatarLabel, newAvatar);
                // TODO: Save to card
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private byte[] chooseAndCompressAvatar() throws Exception {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn ảnh đại diện");
        chooser.setFileFilter(new FileNameExtensionFilter("Ảnh (JPG, PNG)", "jpg", "jpeg", "png"));

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
            return null;

        File file = chooser.getSelectedFile();
        BufferedImage src = ImageIO.read(file);
        if (src == null)
            throw new Exception("Không đọc được ảnh.");

        int target = 96;
        BufferedImage scaled = new BufferedImage(target, target, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, target, target, null);
        g.dispose();

        for (float q = 0.70f; q >= 0.15f; q -= 0.05f) {
            byte[] jpg = encodeJpeg(scaled, q);
            if (jpg.length <= 4096)
                return jpg;
        }

        throw new Exception("Không thể nén ảnh xuống <= 4096 bytes.");
    }

    private byte[] encodeJpeg(BufferedImage img, float quality) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(img, null, null), param);
        } finally {
            writer.dispose();
        }
        return baos.toByteArray();
    }

    private void setAvatarToLabel(JLabel label, byte[] avatarBytes) {
        try {
            if (avatarBytes == null || avatarBytes.length == 0) {
                label.setIcon(null);
                label.setText("👤");
                return;
            }

            BufferedImage img = ImageIO.read(new ByteArrayInputStream(avatarBytes));
            if (img == null) {
                label.setIcon(null);
                label.setText("👤");
                return;
            }

            Image scaled = img.getScaledInstance(120, 120, Image.SCALE_SMOOTH);
            label.setText("");
            label.setIcon(new ImageIcon(scaled));
        } catch (Exception e) {
            label.setIcon(null);
            label.setText("👤");
        }
    }

    public void refreshData() {
        try {
            currentMember = cardComm.getMemberInfo();

            // Update avatar
            setAvatarToLabel(avatarLabel, currentMember.avatarBytes);

            // Update labels
            nameLabel.setText(currentMember.name != null ? currentMember.name : "Hội viên");
            nameValueLabel.setText(currentMember.name != null ? currentMember.name : "---");
            dobValueLabel.setText(currentMember.birthDate != null ? currentMember.birthDate : "---");
            phoneValueLabel.setText(currentMember.phone != null ? currentMember.phone : "---");
            genderValueLabel.setText("Nam"); // Default
            addressValueLabel.setText(currentMember.address != null ? currentMember.address : "---");

            // Get balance and expiry
            try {
                long balance = cardComm.getBalance();
                balanceValueLabel.setText(String.format("%,d đ", balance));
            } catch (Exception e) {
                balanceValueLabel.setText("-- đ");
            }

            try {
                String cardId = currentMember.phone;
                cardIdValueLabel
                        .setText(cardId != null ? "GYM-" + cardId.substring(Math.max(0, cardId.length() - 4)) : "---");
            } catch (Exception e) {
                cardIdValueLabel.setText("---");
            }

            System.out.println("[INFO] Da tai thong tin ca nhan tu the");
        } catch (Exception ex) {
            System.out.println("[INFO] LOI tai thong tin: " + ex.getMessage());
        }
    }

    public void clearUI() {
        if (nameLabel != null)
            nameLabel.setText("Hội viên");
        if (nameValueLabel != null)
            nameValueLabel.setText("---");
        if (dobValueLabel != null)
            dobValueLabel.setText("---");
        if (phoneValueLabel != null)
            phoneValueLabel.setText("---");
        if (avatarLabel != null) {
            avatarLabel.setIcon(null);
            avatarLabel.setText("👤");
        }
    }
}
