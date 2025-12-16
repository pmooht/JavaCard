package gymcard.client.ui;

import gymcard.client.CardCommunicator;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.Border;

/**
 * Base class chứa các utilities chung cho tất cả các tab panels
 */
public abstract class BaseTabPanel extends JPanel {

    protected final CardCommunicator cardComm;

    public BaseTabPanel(CardCommunicator cardComm) {
        this.cardComm = cardComm;
    }

    /**
     * Tạo modern rounded button
     */
    protected JButton createModernButton(String text, Color bgColor, int fontSize) {
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
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    /**
     * Tạo border cho input fields
     */
    protected Border fieldBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10));
    }

    /**
     * Log message ra terminal
     */
    protected void log(String message) {
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        System.out.println(String.format("[%s] %s", timestamp, message));
    }

    /**
     * Tạo card thống kê với header gradient
     */
    protected JPanel createStatCard(String title, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2, true),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        // Header
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, color, getWidth(), 0, color.darker());
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        header.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));
        header.setPreferredSize(new Dimension(0, 40));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);
        header.add(titleLabel);
        card.add(header, BorderLayout.NORTH);

        // Content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        card.add(content, BorderLayout.CENTER);

        return card;
    }

    /**
     * Validate họ tên
     */
    protected void validateName(String name) throws Exception {
        if (name == null || name.trim().isEmpty())
            throw new Exception("Họ và tên không được để trống");
        if (name.trim().length() < 2)
            throw new Exception("Họ và tên quá ngắn");
    }

    /**
     * Validate ngày sinh
     */
    protected void validateBirthDate(String birthDate) throws Exception {
        if (birthDate == null || birthDate.trim().isEmpty())
            throw new Exception("Ngày sinh không được để trống");
        if (!birthDate.matches("\\d{2}/\\d{2}/\\d{4}"))
            throw new Exception("Ngày sinh phải theo dd/MM/yyyy");

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false);
        try {
            sdf.parse(birthDate);
        } catch (java.text.ParseException e) {
            throw new Exception("Ngày sinh không hợp lệ");
        }
    }

    /**
     * Validate số điện thoại
     */
    protected void validatePhone(String phone) throws Exception {
        if (phone == null || phone.trim().isEmpty())
            throw new Exception("Số điện thoại không được để trống");
        String normalized = phone.trim().replaceAll("\\s+", "");
        if (!normalized.matches("0\\d{9,10}"))
            throw new Exception("SĐT không hợp lệ (0xxxxxxxxx, 10–11 số)");
    }
}
