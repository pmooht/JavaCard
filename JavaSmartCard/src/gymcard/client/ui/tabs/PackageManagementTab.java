package gymcard.client.ui.tabs;

import gymcard.client.CardCommunicator;
import gymcard.client.ui.BaseTabPanel;
import gymcard.databaseManager.DatabaseManager;
import gymcard.databaseManager.DatabaseManager.PlanInfo;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;

/**
 * Tab quản lý gói tập (Admin) - Dark theme design
 */
public class PackageManagementTab extends BaseTabPanel {

    private JTable planTable;
    private DefaultTableModel tableModel;
    private DatabaseManager db;

    // Colors
    private static final Color BG_DARK = new Color(30, 35, 50);
    private static final Color CARD_BG = new Color(40, 45, 65);
    private static final Color TEXT_WHITE = new Color(230, 230, 240);
    private static final Color TEXT_GRAY = new Color(140, 145, 165);
    private static final Color ACCENT_GREEN = new Color(46, 204, 113);
    private static final Color ACCENT_BLUE = new Color(52, 152, 219);
    private static final Color ACCENT_YELLOW = new Color(241, 196, 15);
    private static final Color ACCENT_RED = new Color(231, 76, 60);

    public PackageManagementTab(CardCommunicator cardComm) {
        super(cardComm);
        try {
            db = DatabaseManager.getInstance();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        initUI();
        loadPlans();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(BG_DARK);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BG_DARK);
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Header
        JPanel header = createHeader();
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(header);

        mainPanel.add(Box.createVerticalStrut(20));

        // Stats cards
        JPanel statsPanel = createStatsPanel();
        statsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(statsPanel);

        mainPanel.add(Box.createVerticalStrut(20));

        // Table
        JPanel tablePanel = createTablePanel();
        tablePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(tablePanel);

        mainPanel.add(Box.createVerticalStrut(15));

        // Footer
        JLabel footerLabel = new JLabel(
                "⚡ Gói tập được lưu trong database. Thay đổi sẽ được cập nhật ngay đến User sau khi làm mới.");
        footerLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        footerLabel.setForeground(TEXT_GRAY);
        footerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(footerLabel);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(BG_DARK);
        scrollPane.getViewport().setBackground(BG_DARK);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Quản lý gói tập");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_WHITE);
        leftPanel.add(titleLabel);

        leftPanel.add(Box.createVerticalStrut(5));

        JLabel subtitleLabel = new JLabel("Quản lý các gói đăng ký, giá và trạng thái hoạt động.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(TEXT_GRAY);
        leftPanel.add(subtitleLabel);

        header.add(leftPanel, BorderLayout.WEST);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton refreshBtn = createDarkButton("Tải lại", new Color(60, 65, 85));
        refreshBtn.addActionListener(e -> loadPlans());
        buttonPanel.add(refreshBtn);

        JButton addBtn = createDarkButton("+ Thêm gói mới", ACCENT_GREEN);
        addBtn.addActionListener(e -> showAddEditDialog(null));
        buttonPanel.add(addBtn);

        header.add(buttonPanel, BorderLayout.EAST);

        return header;
    }

    private JButton createDarkButton(String text, Color bgColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getModel().isRollover() ? bgColor.brighter() : bgColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(130, 35));
        return btn;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 15, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        // Get stats
        int total = 0, active = 0;
        double maxPrice = 0;
        try {
            List<PlanInfo> plans = db.getAllPlans();
            total = plans.size();
            for (PlanInfo p : plans) {
                if (p.isActive)
                    active++;
                if (p.price > maxPrice)
                    maxPrice = p.price;
            }
        } catch (SQLException e) {
        }

        panel.add(createStatCard("Tổng số gói", String.valueOf(total), "📦", ACCENT_BLUE));
        panel.add(createStatCard("Đang hoạt động", String.valueOf(active), "✅", ACCENT_GREEN));
        panel.add(createStatCard("Giá cao nhất", formatPrice(maxPrice), "💰", ACCENT_YELLOW));

        return panel;
    }

    private String formatPrice(double price) {
        if (price >= 1000000) {
            return String.format("%.1fM", price / 1000000) + " VND";
        }
        return String.format("%,.0f", price) + " VND";
    }

    private JPanel createStatCard(String title, String value, String icon, Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(CARD_BG);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                // Left accent border
                g2d.setColor(accentColor);
                g2d.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(15, 0));
        card.setBorder(new EmptyBorder(15, 20, 15, 15));

        // Text panel
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        titleLbl.setForeground(TEXT_GRAY);
        textPanel.add(titleLbl);

        textPanel.add(Box.createVerticalStrut(5));

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLbl.setForeground(TEXT_WHITE);
        textPanel.add(valueLbl);

        card.add(textPanel, BorderLayout.CENTER);

        // Icon
        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        card.add(iconLbl, BorderLayout.EAST);

        return card;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        String[] columns = { "ID", "MÃ GÓI", "TÊN GÓI", "MÔ TẢ", "NGÀY", "BUỔI", "GIÁ (VND)", "TRẠNG THÁI",
                "THAO TÁC" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 8;
            }
        };

        planTable = new JTable(tableModel);
        planTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        planTable.setRowHeight(45);
        planTable.setBackground(CARD_BG);
        planTable.setForeground(TEXT_WHITE);
        planTable.setGridColor(new Color(60, 65, 85));
        planTable.setSelectionBackground(new Color(60, 65, 95));
        planTable.setSelectionForeground(TEXT_WHITE);
        planTable.setShowGrid(true);

        // Header styling
        JTableHeader header = planTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setBackground(new Color(35, 40, 55));
        header.setForeground(TEXT_GRAY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 65, 85)));

        // Hide ID column
        planTable.getColumnModel().getColumn(0).setMinWidth(0);
        planTable.getColumnModel().getColumn(0).setMaxWidth(0);
        planTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        // Set column widths
        planTable.getColumnModel().getColumn(1).setPreferredWidth(90);
        planTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        planTable.getColumnModel().getColumn(3).setPreferredWidth(180);
        planTable.getColumnModel().getColumn(4).setPreferredWidth(50);
        planTable.getColumnModel().getColumn(5).setPreferredWidth(50);
        planTable.getColumnModel().getColumn(6).setPreferredWidth(90);
        planTable.getColumnModel().getColumn(7).setPreferredWidth(50);
        planTable.getColumnModel().getColumn(8).setPreferredWidth(150);

        // Custom renderers
        planTable.setDefaultRenderer(Object.class, new DarkTableCellRenderer());
        planTable.getColumn("THAO TÁC").setCellRenderer(new ActionButtonRenderer());
        planTable.getColumn("THAO TÁC").setCellEditor(new ActionButtonEditor());

        JScrollPane scrollPane = new JScrollPane(planTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(50, 55, 75), 1));
        scrollPane.getViewport().setBackground(CARD_BG);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private class DarkTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBackground(isSelected ? new Color(60, 65, 95) : CARD_BG);
            setForeground(TEXT_WHITE);
            setBorder(new EmptyBorder(5, 10, 5, 10));

            // Status column styling
            if (column == 7 && value != null) {
                String status = value.toString();
                if (status.contains("Active")) {
                    setForeground(ACCENT_GREEN);
                } else {
                    setForeground(ACCENT_RED);
                }
            }
            return this;
        }
    }

    private void loadPlans() {
        try {
            tableModel.setRowCount(0);
            List<PlanInfo> plans = db.getAllPlans();
            for (PlanInfo plan : plans) {
                Object[] row = {
                        plan.id,
                        plan.code,
                        plan.name,
                        plan.description,
                        plan.durationDays > 0 ? plan.durationDays : "-",
                        plan.sessionCount > 0 ? plan.sessionCount : "-",
                        String.format("%,.0f", plan.price),
                        plan.isActive ? "Active" : "Inactive",
                        "actions"
                };
                tableModel.addRow(row);
            }
            log("Da tai " + plans.size() + " goi tap");
        } catch (SQLException e) {
            log("LOI tai goi tap: " + e.getMessage());
        }
    }

    private void showAddEditDialog(PlanInfo existing) {
        boolean isEdit = existing != null;
        String title = isEdit ? "Sửa gói tập" : "Thêm gói tập mới";

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setSize(450, 420);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(CARD_BG);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Segoe UI", Font.PLAIN, 13);
        int row = 0;

        // Code
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        JLabel codeLbl = new JLabel("Mã gói:");
        codeLbl.setForeground(TEXT_WHITE);
        formPanel.add(codeLbl, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextField codeField = createDarkTextField();
        if (isEdit)
            codeField.setText(existing.code);
        formPanel.add(codeField, gbc);

        // Name
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        JLabel nameLbl = new JLabel("Tên gói:");
        nameLbl.setForeground(TEXT_WHITE);
        formPanel.add(nameLbl, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextField nameField = createDarkTextField();
        if (isEdit)
            nameField.setText(existing.name);
        formPanel.add(nameField, gbc);

        // Description
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        JLabel descLbl = new JLabel("Mô tả:");
        descLbl.setForeground(TEXT_WHITE);
        formPanel.add(descLbl, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextField descField = createDarkTextField();
        if (isEdit)
            descField.setText(existing.description);
        formPanel.add(descField, gbc);

        // Duration
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        JLabel daysLbl = new JLabel("Số ngày:");
        daysLbl.setForeground(TEXT_WHITE);
        formPanel.add(daysLbl, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JSpinner daysSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 365 * 5, 1));
        if (isEdit && existing.durationDays > 0)
            daysSpinner.setValue(existing.durationDays);
        formPanel.add(daysSpinner, gbc);

        // Sessions
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        JLabel sessLbl = new JLabel("Số buổi:");
        sessLbl.setForeground(TEXT_WHITE);
        formPanel.add(sessLbl, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JSpinner sessSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 500, 1));
        if (isEdit && existing.sessionCount > 0)
            sessSpinner.setValue(existing.sessionCount);
        formPanel.add(sessSpinner, gbc);

        // Price
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        JLabel priceLbl = new JLabel("Giá (VNĐ):");
        priceLbl.setForeground(TEXT_WHITE);
        formPanel.add(priceLbl, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JSpinner priceSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100000000.0, 10000.0));
        if (isEdit)
            priceSpinner.setValue(existing.price);
        formPanel.add(priceSpinner, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);

        JButton saveBtn = createDarkButton(isEdit ? "Lưu" : "Thêm", ACCENT_GREEN);
        saveBtn.addActionListener(e -> {
            try {
                String code = codeField.getText().trim();
                String name = nameField.getText().trim();
                String desc = descField.getText().trim();
                int days = (Integer) daysSpinner.getValue();
                int sessions = (Integer) sessSpinner.getValue();
                double price = ((Number) priceSpinner.getValue()).doubleValue();

                if (code.isEmpty() || name.isEmpty() || price <= 0) {
                    JOptionPane.showMessageDialog(dialog, "Vui lòng điền đủ thông tin!", "Lỗi",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (isEdit) {
                    db.updatePlan(existing.id, code, name, desc, days > 0 ? days : null, sessions > 0 ? sessions : null,
                            price);
                } else {
                    db.addPlan(code, name, desc, days > 0 ? days : null, sessions > 0 ? sessions : null, price);
                }
                dialog.dispose();
                loadPlans();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(saveBtn);

        JButton cancelBtn = createDarkButton("Hủy", new Color(100, 100, 120));
        cancelBtn.addActionListener(e -> dialog.dispose());
        buttonPanel.add(cancelBtn);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setContentPane(mainPanel);
        dialog.setVisible(true);
    }

    private JTextField createDarkTextField() {
        JTextField field = new JTextField(20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBackground(new Color(50, 55, 75));
        field.setForeground(TEXT_WHITE);
        field.setCaretColor(TEXT_WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 75, 95), 1),
                new EmptyBorder(8, 10, 8, 10)));
        return field;
    }

    private class ActionButtonRenderer extends JPanel implements TableCellRenderer {
        public ActionButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
            setOpaque(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            removeAll();
            setBackground(isSelected ? new Color(60, 65, 95) : CARD_BG);

            JButton editBtn = createSmallButton("Sửa", ACCENT_YELLOW);
            JButton toggleBtn = createSmallButton("Ẩn", ACCENT_BLUE);
            JButton deleteBtn = createSmallButton("Xóa", ACCENT_RED);

            add(editBtn);
            add(toggleBtn);
            add(deleteBtn);
            return this;
        }
    }

    private JButton createSmallButton(String text, Color bgColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(bgColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(60, 32));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private class ActionButtonEditor extends DefaultCellEditor {
        private JPanel panel;
        private int currentRow;

        public ActionButtonEditor() {
            super(new JCheckBox());
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            panel.setBackground(CARD_BG);

            JButton editBtn = createSmallButton("Sửa", ACCENT_YELLOW);
            editBtn.addActionListener(e -> {
                fireEditingStopped();
                editPlan(currentRow);
            });

            JButton toggleBtn = createSmallButton("Ẩn", ACCENT_BLUE);
            toggleBtn.addActionListener(e -> {
                fireEditingStopped();
                togglePlan(currentRow);
            });

            JButton deleteBtn = createSmallButton("Xóa", ACCENT_RED);
            deleteBtn.addActionListener(e -> {
                fireEditingStopped();
                deletePlan(currentRow);
            });

            panel.add(editBtn);
            panel.add(toggleBtn);
            panel.add(deleteBtn);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {
            currentRow = row;
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "actions";
        }
    }

    private void editPlan(int row) {
        try {
            int id = (Integer) tableModel.getValueAt(row, 0);
            List<PlanInfo> plans = db.getAllPlans();
            for (PlanInfo p : plans) {
                if (p.id == id) {
                    showAddEditDialog(p);
                    return;
                }
            }
        } catch (SQLException e) {
        }
    }

    private void togglePlan(int row) {
        try {
            int id = (Integer) tableModel.getValueAt(row, 0);
            String status = (String) tableModel.getValueAt(row, 7);
            db.togglePlanActive(id, !status.contains("Active"));
            loadPlans();
        } catch (SQLException e) {
        }
    }

    private void deletePlan(int row) {
        int id = (Integer) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 2);
        if (JOptionPane.showConfirmDialog(this, "Xóa gói: " + name + "?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                db.deletePlan(id);
                loadPlans();
            } catch (SQLException e) {
            }
        }
    }
}
