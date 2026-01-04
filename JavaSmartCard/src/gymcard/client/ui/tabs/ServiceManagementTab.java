package gymcard.client.ui.tabs;

import gymcard.client.CardCommunicator;
import gymcard.client.ui.BaseTabPanel;
import gymcard.databaseManager.DatabaseManager;
import gymcard.databaseManager.DatabaseManager.ServiceInfo;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;

/**
 * Tab quản lý dịch vụ (Admin) - Dark theme design
 */
public class ServiceManagementTab extends BaseTabPanel {

    private JTable serviceTable;
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

    public ServiceManagementTab(CardCommunicator cardComm) {
        super(cardComm);
        try {
            db = DatabaseManager.getInstance();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        initUI();
        loadServices();
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

        mainPanel.add(Box.createVerticalStrut(25));

        // Table
        JPanel tablePanel = createTablePanel();
        tablePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(tablePanel);

        mainPanel.add(Box.createVerticalStrut(15));

        // Footer
        JLabel footerLabel = new JLabel("Dịch vụ được lưu trong database. Thay đổi sẽ được cập nhật ngay đến User.");
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

        JLabel titleLabel = new JLabel("Quản lý Dịch vụ");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_WHITE);
        leftPanel.add(titleLabel);

        leftPanel.add(Box.createVerticalStrut(5));

        JLabel subtitleLabel = new JLabel(
                "Quản lý danh sách các gói dịch vụ, định giá và trạng thái hoạt động trong hệ thống.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(TEXT_GRAY);
        leftPanel.add(subtitleLabel);

        header.add(leftPanel, BorderLayout.WEST);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton refreshBtn = createDarkButton("Tải lại", new Color(60, 65, 85));
        refreshBtn.addActionListener(e -> loadServices());
        buttonPanel.add(refreshBtn);

        JButton addBtn = createDarkButton("+ Thêm dịch vụ", ACCENT_GREEN);
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

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        String[] columns = { "ID", "ID DỊCH VỤ", "TÊN DỊCH VỤ", "MÔ TẢ CHI TIẾT", "GIÁ (VND)", "TRẠNG THÁI",
                "HÀNH ĐỘNG" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6;
            }
        };

        serviceTable = new JTable(tableModel);
        serviceTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        serviceTable.setRowHeight(50);
        serviceTable.setBackground(CARD_BG);
        serviceTable.setForeground(TEXT_WHITE);
        serviceTable.setGridColor(new Color(60, 65, 85));
        serviceTable.setSelectionBackground(new Color(60, 65, 95));
        serviceTable.setSelectionForeground(TEXT_WHITE);
        serviceTable.setShowGrid(true);

        // Header styling
        JTableHeader header = serviceTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setBackground(new Color(35, 40, 55));
        header.setForeground(TEXT_GRAY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 65, 85)));

        // Hide ID column
        serviceTable.getColumnModel().getColumn(0).setMinWidth(0);
        serviceTable.getColumnModel().getColumn(0).setMaxWidth(0);
        serviceTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        // Set column widths
        serviceTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        serviceTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        serviceTable.getColumnModel().getColumn(3).setPreferredWidth(220);
        serviceTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        serviceTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        serviceTable.getColumnModel().getColumn(6).setPreferredWidth(150);

        // Custom renderers
        serviceTable.setDefaultRenderer(Object.class, new DarkTableCellRenderer());
        serviceTable.getColumn("HÀNH ĐỘNG").setCellRenderer(new ActionButtonRenderer());
        serviceTable.getColumn("HÀNH ĐỘNG").setCellEditor(new ActionButtonEditor());

        JScrollPane scrollPane = new JScrollPane(serviceTable);
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

            // ID column - plain text with accent color
            if (column == 1 && value != null) {
                setText(value.toString());
                setForeground(TEXT_WHITE);
            }

            // Status column styling
            if (column == 5 && value != null) {
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

    private void loadServices() {
        try {
            tableModel.setRowCount(0);
            List<ServiceInfo> services = db.getAllServices();
            for (ServiceInfo svc : services) {
                Object[] row = {
                        svc.id,
                        svc.code,
                        svc.name,
                        svc.description,
                        String.format("%,.0f", svc.price),
                        svc.isActive ? "Active" : "Inactive",
                        "actions"
                };
                tableModel.addRow(row);
            }
            log("Da tai " + services.size() + " dich vu");
        } catch (SQLException e) {
            log("LOI tai dich vu: " + e.getMessage());
        }
    }

    private void showAddEditDialog(ServiceInfo existing) {
        boolean isEdit = existing != null;
        String title = isEdit ? "Sửa dịch vụ" : "Thêm dịch vụ mới";

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(CARD_BG);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Code
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        JLabel codeLbl = new JLabel("Mã dịch vụ:");
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
        JLabel nameLbl = new JLabel("Tên dịch vụ:");
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
        JSpinner priceSpinner = new JSpinner(new SpinnerNumberModel(10000.0, 1000.0, 10000000.0, 1000.0));
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
                double price = ((Number) priceSpinner.getValue()).doubleValue();

                if (code.isEmpty() || name.isEmpty() || price < 1000) {
                    JOptionPane.showMessageDialog(dialog, "Vui lòng điền đủ thông tin!", "Lỗi",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (isEdit) {
                    db.updateService(existing.id, code, name, desc, price);
                } else {
                    db.addService(code, name, desc, price);
                }
                dialog.dispose();
                loadServices();
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
        btn.setPreferredSize(new Dimension(60, 30));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private class ActionButtonRenderer extends JPanel implements TableCellRenderer {
        public ActionButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 8));
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

    private class ActionButtonEditor extends DefaultCellEditor {
        private JPanel panel;
        private int currentRow;

        public ActionButtonEditor() {
            super(new JCheckBox());
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 8));
            panel.setBackground(CARD_BG);

            JButton editBtn = createSmallButton("Sửa", ACCENT_YELLOW);
            editBtn.addActionListener(e -> {
                fireEditingStopped();
                editService(currentRow);
            });

            JButton toggleBtn = createSmallButton("Ẩn", ACCENT_BLUE);
            toggleBtn.addActionListener(e -> {
                fireEditingStopped();
                toggleService(currentRow);
            });

            JButton deleteBtn = createSmallButton("Xóa", ACCENT_RED);
            deleteBtn.addActionListener(e -> {
                fireEditingStopped();
                deleteService(currentRow);
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

    private void editService(int row) {
        try {
            int id = (Integer) tableModel.getValueAt(row, 0);
            List<ServiceInfo> services = db.getAllServices();
            for (ServiceInfo s : services) {
                if (s.id == id) {
                    showAddEditDialog(s);
                    return;
                }
            }
        } catch (SQLException e) {
        }
    }

    private void toggleService(int row) {
        try {
            int id = (Integer) tableModel.getValueAt(row, 0);
            String status = (String) tableModel.getValueAt(row, 5);
            db.toggleServiceActive(id, !status.contains("Active"));
            loadServices();
        } catch (SQLException e) {
        }
    }

    private void deleteService(int row) {
        int id = (Integer) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 2);
        if (JOptionPane.showConfirmDialog(this, "Xóa dịch vụ: " + name + "?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                db.deleteService(id);
                loadServices();
            } catch (SQLException e) {
            }
        }
    }
}
