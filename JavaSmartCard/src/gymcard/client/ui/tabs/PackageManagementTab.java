package gymcard.client.ui.tabs;

import gymcard.client.CardCommunicator;
import gymcard.client.ui.BaseTabPanel;
import gymcard.databaseManager.DatabaseManager;
import gymcard.databaseManager.DatabaseManager.PlanInfo;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/**
 * Tab quản lý gói tập (Admin) - Thêm, sửa, xóa gói tập trong database
 */
public class PackageManagementTab extends BaseTabPanel {

    private JTable planTable;
    private DefaultTableModel tableModel;
    private DatabaseManager db;

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
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(248, 249, 250));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(248, 249, 250));

        JLabel titleLabel = new JLabel("QUẢN LÝ GÓI TẬP");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(52, 73, 94));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(new Color(248, 249, 250));

        JButton addBtn = createModernButton("+ Thêm gói mới", new Color(46, 204, 113), 13);
        addBtn.setPreferredSize(new Dimension(150, 35));
        addBtn.addActionListener(e -> showAddEditDialog(null));

        JButton refreshBtn = createModernButton("Tải lại", new Color(52, 152, 219), 13);
        refreshBtn.setPreferredSize(new Dimension(100, 35));
        refreshBtn.addActionListener(e -> loadPlans());

        buttonPanel.add(addBtn);
        buttonPanel.add(refreshBtn);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = { "ID", "Mã", "Tên gói", "Mô tả", "Số ngày", "Số buổi", "Giá (VNĐ)", "Trạng thái",
                "Hành động" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 8; // Chỉ cột hành động
            }
        };

        planTable = new JTable(tableModel);
        planTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        planTable.setRowHeight(40);
        planTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        planTable.getTableHeader().setBackground(new Color(52, 152, 219));
        planTable.getTableHeader().setForeground(Color.WHITE);

        // Hide ID column
        planTable.getColumnModel().getColumn(0).setMinWidth(0);
        planTable.getColumnModel().getColumn(0).setMaxWidth(0);
        planTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        // Set column widths
        planTable.getColumnModel().getColumn(1).setPreferredWidth(80); // Mã
        planTable.getColumnModel().getColumn(2).setPreferredWidth(120); // Tên
        planTable.getColumnModel().getColumn(3).setPreferredWidth(200); // Mô tả
        planTable.getColumnModel().getColumn(4).setPreferredWidth(70); // Số ngày
        planTable.getColumnModel().getColumn(5).setPreferredWidth(70); // Số buổi
        planTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Giá
        planTable.getColumnModel().getColumn(7).setPreferredWidth(80); // Trạng thái
        planTable.getColumnModel().getColumn(8).setPreferredWidth(150); // Hành động

        // Action buttons column
        planTable.getColumn("Hành động").setCellRenderer(new ActionButtonRenderer());
        planTable.getColumn("Hành động").setCellEditor(new ActionButtonEditor());

        JScrollPane scrollPane = new JScrollPane(planTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        add(scrollPane, BorderLayout.CENTER);

        // Footer info
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        footerPanel.setBackground(new Color(248, 249, 250));
        JLabel infoLabel = new JLabel("💡 Gói tập được lưu trong database. Thay đổi sẽ được cập nhật ngay đến User.");
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        infoLabel.setForeground(new Color(127, 140, 141));
        footerPanel.add(infoLabel);
        add(footerPanel, BorderLayout.SOUTH);
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
                        plan.isActive ? "✅ Active" : "❌ Ẩn",
                        "actions"
                };
                tableModel.addRow(row);
            }
            log("Đã tải " + plans.size() + " gói tập");
        } catch (SQLException e) {
            log("LỖI tải gói tập: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Lỗi tải danh sách gói tập: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddEditDialog(PlanInfo existing) {
        boolean isEdit = existing != null;
        String title = isEdit ? "Sửa gói tập" : "Thêm gói tập mới";

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        Font labelFont = new Font("Segoe UI", Font.PLAIN, 13);

        // Code
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Mã gói:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextField codeField = new JTextField(20);
        codeField.setFont(labelFont);
        codeField.setBorder(fieldBorder());
        if (isEdit)
            codeField.setText(existing.code);
        formPanel.add(codeField, gbc);

        // Name
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Tên gói:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextField nameField = new JTextField(20);
        nameField.setFont(labelFont);
        nameField.setBorder(fieldBorder());
        if (isEdit)
            nameField.setText(existing.name);
        formPanel.add(nameField, gbc);

        // Description
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Mô tả:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextField descField = new JTextField(20);
        descField.setFont(labelFont);
        descField.setBorder(fieldBorder());
        if (isEdit)
            descField.setText(existing.description);
        formPanel.add(descField, gbc);

        // Duration days
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Số ngày (gói tháng):"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JSpinner daysSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 365 * 5, 1));
        daysSpinner.setFont(labelFont);
        if (isEdit && existing.durationDays > 0)
            daysSpinner.setValue(existing.durationDays);
        formPanel.add(daysSpinner, gbc);

        // Session count
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Số buổi (gói buổi):"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JSpinner sessionsSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 500, 1));
        sessionsSpinner.setFont(labelFont);
        if (isEdit && existing.sessionCount > 0)
            sessionsSpinner.setValue(existing.sessionCount);
        formPanel.add(sessionsSpinner, gbc);

        // Price
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Giá (VNĐ):"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JSpinner priceSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100000000.0, 10000.0));
        priceSpinner.setFont(labelFont);
        if (isEdit)
            priceSpinner.setValue(existing.price);
        formPanel.add(priceSpinner, gbc);

        // Hint
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        JLabel hintLabel = new JLabel("<html><i>Ghi chú: Nhập số ngày HOẶC số buổi (không cả hai).</i></html>");
        hintLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hintLabel.setForeground(new Color(127, 140, 141));
        formPanel.add(hintLabel, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton saveBtn = createModernButton(isEdit ? "Lưu thay đổi" : "Thêm gói", new Color(46, 204, 113), 14);
        saveBtn.setPreferredSize(new Dimension(130, 38));
        saveBtn.addActionListener(e -> {
            try {
                String code = codeField.getText().trim();
                String name = nameField.getText().trim();
                String desc = descField.getText().trim();
                int days = (Integer) daysSpinner.getValue();
                int sessions = (Integer) sessionsSpinner.getValue();
                double price = ((Number) priceSpinner.getValue()).doubleValue();

                if (code.isEmpty() || name.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Vui lòng nhập mã và tên gói!", "Thiếu thông tin",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (price <= 0) {
                    JOptionPane.showMessageDialog(dialog, "Giá phải lớn hơn 0!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Integer daysValue = days > 0 ? days : null;
                Integer sessionsValue = sessions > 0 ? sessions : null;

                if (isEdit) {
                    db.updatePlan(existing.id, code, name, desc, daysValue, sessionsValue, price);
                    log("Đã cập nhật gói: " + name);
                } else {
                    db.addPlan(code, name, desc, daysValue, sessionsValue, price);
                    log("Đã thêm gói mới: " + name);
                }

                dialog.dispose();
                loadPlans();
                JOptionPane.showMessageDialog(this,
                        (isEdit ? "Cập nhật" : "Thêm") + " gói tập thành công!\nThay đổi đã được áp dụng.",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);

            } catch (SQLException ex) {
                log("LỖI: " + ex.getMessage());
                JOptionPane.showMessageDialog(dialog,
                        "Lỗi lưu gói tập: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelBtn = new JButton("Hủy");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setContentPane(mainPanel);
        dialog.setVisible(true);
    }

    // Custom renderer for action buttons
    private class ActionButtonRenderer extends JPanel implements TableCellRenderer {
        public ActionButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 2));
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            removeAll();
            setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);

            JButton editBtn = new JButton("Sửa");
            editBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            editBtn.setBackground(new Color(241, 196, 15));
            editBtn.setForeground(Color.WHITE);
            editBtn.setFocusPainted(false);
            editBtn.setBorderPainted(false);

            JButton toggleBtn = new JButton("Ẩn/Hiện");
            toggleBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            toggleBtn.setBackground(new Color(155, 89, 182));
            toggleBtn.setForeground(Color.WHITE);
            toggleBtn.setFocusPainted(false);
            toggleBtn.setBorderPainted(false);

            JButton deleteBtn = new JButton("Xóa");
            deleteBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            deleteBtn.setBackground(new Color(231, 76, 60));
            deleteBtn.setForeground(Color.WHITE);
            deleteBtn.setFocusPainted(false);
            deleteBtn.setBorderPainted(false);

            add(editBtn);
            add(toggleBtn);
            add(deleteBtn);

            return this;
        }
    }

    // Custom editor for action buttons
    private class ActionButtonEditor extends DefaultCellEditor {
        private JPanel panel;
        private JButton editBtn, toggleBtn, deleteBtn;
        private int currentRow;

        public ActionButtonEditor() {
            super(new JCheckBox());
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
            panel.setBackground(Color.WHITE);

            editBtn = new JButton("Sửa");
            editBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            editBtn.setBackground(new Color(241, 196, 15));
            editBtn.setForeground(Color.WHITE);
            editBtn.setFocusPainted(false);
            editBtn.setBorderPainted(false);
            editBtn.addActionListener(e -> {
                fireEditingStopped();
                editPlan(currentRow);
            });

            toggleBtn = new JButton("Ẩn/Hiện");
            toggleBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            toggleBtn.setBackground(new Color(155, 89, 182));
            toggleBtn.setForeground(Color.WHITE);
            toggleBtn.setFocusPainted(false);
            toggleBtn.setBorderPainted(false);
            toggleBtn.addActionListener(e -> {
                fireEditingStopped();
                togglePlan(currentRow);
            });

            deleteBtn = new JButton("Xóa");
            deleteBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            deleteBtn.setBackground(new Color(231, 76, 60));
            deleteBtn.setForeground(Color.WHITE);
            deleteBtn.setFocusPainted(false);
            deleteBtn.setBorderPainted(false);
            deleteBtn.addActionListener(e -> {
                fireEditingStopped();
                deletePlan(currentRow);
            });

            panel.add(editBtn);
            panel.add(toggleBtn);
            panel.add(deleteBtn);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
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
            log("LỖI: " + e.getMessage());
        }
    }

    private void togglePlan(int row) {
        try {
            int id = (Integer) tableModel.getValueAt(row, 0);
            String status = (String) tableModel.getValueAt(row, 7);
            boolean currentActive = status.contains("Active");
            boolean newActive = !currentActive;

            db.togglePlanActive(id, newActive);
            log("Đã " + (newActive ? "hiện" : "ẩn") + " gói ID=" + id);
            loadPlans();
        } catch (SQLException e) {
            log("LỖI: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deletePlan(int row) {
        int id = (Integer) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn XÓA VĨNH VIỄN gói tập:\n" + name + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                db.deletePlan(id);
                log("Đã xóa gói: " + name);
                loadPlans();
                JOptionPane.showMessageDialog(this, "Đã xóa gói tập!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                log("LỖI: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Lỗi xóa: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
