package gymcard.client.ui.tabs;

import gymcard.client.CardCommunicator;
import gymcard.client.ui.BaseTabPanel;
import gymcard.databaseManager.DatabaseManager;
import gymcard.databaseManager.DatabaseManager.ServiceInfo;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/**
 * Tab quản lý dịch vụ (Admin) - Thêm, sửa, xóa dịch vụ trong database
 */
public class ServiceManagementTab extends BaseTabPanel {

    private JTable serviceTable;
    private DefaultTableModel tableModel;
    private DatabaseManager db;

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
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(248, 249, 250));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(248, 249, 250));

        JLabel titleLabel = new JLabel("QUẢN LÝ DỊCH VỤ");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(155, 89, 182));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(new Color(248, 249, 250));

        JButton addBtn = createModernButton("+ Thêm dịch vụ", new Color(155, 89, 182), 13);
        addBtn.setPreferredSize(new Dimension(150, 35));
        addBtn.addActionListener(e -> showAddEditDialog(null));

        JButton refreshBtn = createModernButton("Tải lại", new Color(52, 152, 219), 13);
        refreshBtn.setPreferredSize(new Dimension(100, 35));
        refreshBtn.addActionListener(e -> loadServices());

        buttonPanel.add(addBtn);
        buttonPanel.add(refreshBtn);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = { "ID", "Mã", "Tên dịch vụ", "Mô tả", "Giá (VNĐ)", "Trạng thái", "Hành động" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6;
            }
        };

        serviceTable = new JTable(tableModel);
        serviceTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        serviceTable.setRowHeight(40);
        serviceTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        serviceTable.getTableHeader().setBackground(new Color(155, 89, 182));
        serviceTable.getTableHeader().setForeground(Color.WHITE);

        // Hide ID column
        serviceTable.getColumnModel().getColumn(0).setMinWidth(0);
        serviceTable.getColumnModel().getColumn(0).setMaxWidth(0);
        serviceTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        // Set column widths
        serviceTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        serviceTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        serviceTable.getColumnModel().getColumn(3).setPreferredWidth(250);
        serviceTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        serviceTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        serviceTable.getColumnModel().getColumn(6).setPreferredWidth(150);

        // Action buttons column
        serviceTable.getColumn("Hành động").setCellRenderer(new ActionButtonRenderer());
        serviceTable.getColumn("Hành động").setCellEditor(new ActionButtonEditor());

        JScrollPane scrollPane = new JScrollPane(serviceTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        add(scrollPane, BorderLayout.CENTER);

        // Footer info
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        footerPanel.setBackground(new Color(248, 249, 250));
        JLabel infoLabel = new JLabel("💡 Dịch vụ được lưu trong database. Thay đổi sẽ được cập nhật ngay đến User.");
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        infoLabel.setForeground(new Color(127, 140, 141));
        footerPanel.add(infoLabel);
        add(footerPanel, BorderLayout.SOUTH);
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
                        svc.isActive ? "✅ Active" : "❌ Ẩn",
                        "actions"
                };
                tableModel.addRow(row);
            }
            log("Đã tải " + services.size() + " dịch vụ");
        } catch (SQLException e) {
            log("LỖI tải dịch vụ: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Lỗi tải danh sách dịch vụ: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddEditDialog(ServiceInfo existing) {
        boolean isEdit = existing != null;
        String title = isEdit ? "Sửa dịch vụ" : "Thêm dịch vụ mới";

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setSize(400, 320);
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
        formPanel.add(new JLabel("Mã dịch vụ:"), gbc);
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
        formPanel.add(new JLabel("Tên dịch vụ:"), gbc);
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

        // Price
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Giá (VNĐ):"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JSpinner priceSpinner = new JSpinner(new SpinnerNumberModel(10000.0, 1000.0, 10000000.0, 1000.0));
        priceSpinner.setFont(labelFont);
        if (isEdit)
            priceSpinner.setValue(existing.price);
        formPanel.add(priceSpinner, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton saveBtn = createModernButton(isEdit ? "Lưu thay đổi" : "Thêm dịch vụ", new Color(155, 89, 182), 14);
        saveBtn.setPreferredSize(new Dimension(130, 38));
        saveBtn.addActionListener(e -> {
            try {
                String code = codeField.getText().trim();
                String name = nameField.getText().trim();
                String desc = descField.getText().trim();
                double price = ((Number) priceSpinner.getValue()).doubleValue();

                if (code.isEmpty() || name.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Vui lòng nhập mã và tên dịch vụ!", "Thiếu thông tin",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (price < 1000) {
                    JOptionPane.showMessageDialog(dialog, "Giá phải từ 1000 VNĐ trở lên!", "Lỗi",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (isEdit) {
                    db.updateService(existing.id, code, name, desc, price);
                    log("Đã cập nhật dịch vụ: " + name);
                } else {
                    db.addService(code, name, desc, price);
                    log("Đã thêm dịch vụ mới: " + name);
                }

                dialog.dispose();
                loadServices();
                JOptionPane.showMessageDialog(this,
                        (isEdit ? "Cập nhật" : "Thêm") + " dịch vụ thành công!\nThay đổi đã được áp dụng.",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);

            } catch (SQLException ex) {
                log("LỖI: " + ex.getMessage());
                JOptionPane.showMessageDialog(dialog,
                        "Lỗi lưu dịch vụ: " + ex.getMessage(),
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
            toggleBtn.setBackground(new Color(52, 152, 219));
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
                editService(currentRow);
            });

            toggleBtn = new JButton("Ẩn/Hiện");
            toggleBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            toggleBtn.setBackground(new Color(52, 152, 219));
            toggleBtn.setForeground(Color.WHITE);
            toggleBtn.setFocusPainted(false);
            toggleBtn.setBorderPainted(false);
            toggleBtn.addActionListener(e -> {
                fireEditingStopped();
                toggleService(currentRow);
            });

            deleteBtn = new JButton("Xóa");
            deleteBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            deleteBtn.setBackground(new Color(231, 76, 60));
            deleteBtn.setForeground(Color.WHITE);
            deleteBtn.setFocusPainted(false);
            deleteBtn.setBorderPainted(false);
            deleteBtn.addActionListener(e -> {
                fireEditingStopped();
                deleteService(currentRow);
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
            log("LỖI: " + e.getMessage());
        }
    }

    private void toggleService(int row) {
        try {
            int id = (Integer) tableModel.getValueAt(row, 0);
            String status = (String) tableModel.getValueAt(row, 5);
            boolean currentActive = status.contains("Active");
            boolean newActive = !currentActive;

            db.toggleServiceActive(id, newActive);
            log("Đã " + (newActive ? "hiện" : "ẩn") + " dịch vụ ID=" + id);
            loadServices();
        } catch (SQLException e) {
            log("LỖI: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteService(int row) {
        int id = (Integer) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn XÓA VĨNH VIỄN dịch vụ:\n" + name + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                db.deleteService(id);
                log("Đã xóa dịch vụ: " + name);
                loadServices();
                JOptionPane.showMessageDialog(this, "Đã xóa dịch vụ!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                log("LỖI: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Lỗi xóa: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
