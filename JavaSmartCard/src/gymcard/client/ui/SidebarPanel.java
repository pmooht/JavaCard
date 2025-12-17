package gymcard.client.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Sidebar Panel - Component sidebar có thể tái sử dụng
 */
public class SidebarPanel extends JPanel {

    private final List<SidebarItem> items = new ArrayList<>();
    private final JPanel itemsPanel;
    private final Color accentColor;
    private int selectedIndex = 0;

    public SidebarPanel(Color accentColor) {
        this.accentColor = accentColor;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(220, 0));
        setBackground(new Color(44, 62, 80));

        // Items panel
        itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setOpaque(false);
        itemsPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        add(itemsPanel, BorderLayout.NORTH);
    }

    public void addItem(String icon, String text, Runnable onClick) {
        SidebarItem item = new SidebarItem(icon, text, onClick, items.size());
        items.add(item);
        itemsPanel.add(item);
        itemsPanel.add(Box.createVerticalStrut(5));

        if (items.size() == 1) {
            selectItem(0);
        }
    }

    public void addSeparator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(52, 73, 94));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        itemsPanel.add(Box.createVerticalStrut(10));
        itemsPanel.add(sep);
        itemsPanel.add(Box.createVerticalStrut(10));
    }

    public void selectItem(int index) {
        if (index >= 0 && index < items.size()) {
            selectedIndex = index;
            for (int i = 0; i < items.size(); i++) {
                items.get(i).setSelected(i == index);
            }
        }
    }

    private class SidebarItem extends JPanel {
        private final int index;
        private boolean selected = false;
        private final Runnable onClick;

        public SidebarItem(String icon, String text, Runnable onClick, int index) {
            this.onClick = onClick;
            this.index = index;
            setOpaque(false);
            setLayout(new FlowLayout(FlowLayout.LEFT, 15, 12));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            JLabel iconLabel = new JLabel(icon);
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
            iconLabel.setForeground(Color.WHITE);
            add(iconLabel);

            JLabel textLabel = new JLabel(text);
            textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            textLabel.setForeground(Color.WHITE);
            add(textLabel);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectItem(index);
                    if (onClick != null) {
                        onClick.run();
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!selected) {
                        setBackground(new Color(52, 73, 94));
                        setOpaque(true);
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!selected) {
                        setOpaque(false);
                        repaint();
                    }
                }
            });
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            if (selected) {
                setBackground(accentColor);
                setOpaque(true);
            } else {
                setOpaque(false);
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (isOpaque()) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(5, 0, getWidth() - 10, getHeight(), 10, 10);
            }
            super.paintComponent(g);
        }
    }
}
