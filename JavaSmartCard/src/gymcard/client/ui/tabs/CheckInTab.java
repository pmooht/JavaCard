package gymcard.client.ui.tabs;

import gymcard.client.CardCommunicator;
import gymcard.client.CheckInInfo;
import gymcard.client.ui.BaseTabPanel;
import gymcard.client.ui.CheckInDayDecorator;
import com.toedter.calendar.JCalendar;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Tab check-in/check-out voi lich
 */
public class CheckInTab extends BaseTabPanel {

    // Fields for labels that need to be updated
    private JLabel countLabel;
    private JLabel lastLabel;
    private CheckInDayDecorator decorator;
    private String currentCheckInDate; // Track today's check-in

    public CheckInTab(CardCommunicator cardComm) {
        super(cardComm);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(new Color(248, 249, 250));

        JLabel titleLabel = new JLabel("LICH TAP GYM & CHECK-IN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(52, 73, 94));
        titleLabel.setHorizontalAlignment(SwingConstants.LEFT);
        titleLabel.setBorder(new EmptyBorder(0, 5, 5, 5));
        add(titleLabel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 0));
        mainPanel.setBackground(new Color(248, 249, 250));

        // LEFT: Calendar
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(10, 10, 10, 10)));
        leftPanel.setPreferredSize(new Dimension(750, 0));

        JCalendar calendar = new JCalendar();
        calendar.setBackground(Color.WHITE);
        calendar.setWeekOfYearVisible(false);
        calendar.getDayChooser().setFont(new Font("Segoe UI", Font.PLAIN, 13));
        calendar.getMonthChooser().getComboBox().setFont(new Font("Segoe UI", Font.BOLD, 14));
        calendar.getYearChooser().setFont(new Font("Segoe UI", Font.BOLD, 14));

        leftPanel.add(calendar, BorderLayout.CENTER);

        // Decorator
        decorator = new CheckInDayDecorator(calendar);
        decorator.install();

        mainPanel.add(leftPanel, BorderLayout.CENTER);

        // RIGHT: Stats + buttons
        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        rightPanel.setBackground(new Color(248, 249, 250));
        rightPanel.setPreferredSize(new Dimension(260, 0));

        JPanel statsPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        statsPanel.setBackground(new Color(240, 248, 255));
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(52, 152, 219), 2, true),
                new EmptyBorder(15, 15, 15, 15)));

        JLabel statsTitle = new JLabel("THONG KE");
        statsTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statsTitle.setForeground(new Color(52, 73, 94));
        statsPanel.add(statsTitle);

        countLabel = new JLabel("So ngay da tap: 0 ngay");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statsPanel.add(countLabel);

        // Label hiển thị trạng thái và thời gian
        lastLabel = new JLabel("<html>Trạng thái: Chưa check-in<br>Thời gian hôm nay: 0 phút</html>");
        lastLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lastLabel.setForeground(new Color(127, 140, 141));
        statsPanel.add(lastLabel);

        calendar.getDayChooser().addPropertyChangeListener("day", evt -> {
            java.util.Calendar selectedCal = calendar.getCalendar();
            String selectedDate = new SimpleDateFormat("dd/MM/yyyy").format(selectedCal.getTime());
            // Hien thi ngay duoc chon
            lastLabel.setText("<html>Ngay duoc chon:<br>" + selectedDate + "</html>");
            lastLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lastLabel.setForeground(new Color(52, 73, 94));
            log("Chon ngay: " + selectedDate);
        });

        rightPanel.add(statsPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        buttonPanel.setBackground(new Color(248, 249, 250));
        buttonPanel.setBorder(new EmptyBorder(5, 0, 0, 0));

        JButton checkInBtn = createModernButton("CHECK-IN", new Color(46, 204, 113), 16);
        checkInBtn.setPreferredSize(new Dimension(0, 60));
        checkInBtn.addActionListener(e -> {
            try {
                String date = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
                String time = new SimpleDateFormat("HH:mm:ss").format(new Date());

                if (cardComm.checkIn(date, time)) {
                    log("Check-in thanh cong!");

                    CheckInInfo info = cardComm.getLastCheckIn();
                    int count = cardComm.getCheckInCount();

                    countLabel.setText("Số buổi đã tập: " + count + " buổi");
                    lastLabel.setText(String.format(
                            "<html>Trạng thái: <b style='color:green'>Đang trong phòng tập</b><br>Vào lúc: %s<br>Thời gian hôm nay: %s</html>",
                            info.checkInTime, info.getTotalTimeText()));

                    decorator.addCheckInDate(date); // Green - dang trong phong tap
                    currentCheckInDate = date; // Luu lai de biet khi checkout

                    JOptionPane.showMessageDialog(this,
                            "Check-in thành công!\nChúc bạn buổi tập tốt!",
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    log("Check-in that bai");
                }

            } catch (Exception ex) {
                log("LOI: " + ex.getMessage());
                JOptionPane.showMessageDialog(this,
                        "Lỗi check-in: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton checkOutBtn = createModernButton("CHECK-OUT", new Color(231, 76, 60), 16);
        checkOutBtn.setPreferredSize(new Dimension(0, 60));
        checkOutBtn.addActionListener(e -> {
            try {
                String time = new SimpleDateFormat("HH:mm:ss").format(new Date());

                if (cardComm.checkOut(time)) {
                    log("Check-out thanh cong!");

                    CheckInInfo info = cardComm.getLastCheckIn();
                    lastLabel.setText(String.format(
                            "<html>Trạng thái: <b style='color:#8e44ad'>Đã rời phòng tập</b><br>Vào: %s | Ra: %s<br>Thời gian hôm nay: %s</html>",
                            info.checkInTime, info.checkOutTime, info.getTotalTimeText()));

                    // Chuyen sang mau tim khi checkout xong (hoan thanh buoi tap)
                    if (currentCheckInDate != null) {
                        decorator.markDateCheckedOut(currentCheckInDate);
                        currentCheckInDate = null;
                    }

                    // Cap nhat so buoi tap
                    int count = cardComm.getCheckInCount();
                    countLabel.setText("Số buổi đã tập: " + count + " buổi");

                    JOptionPane.showMessageDialog(this,
                            "Check-out thành công!\nTổng thời gian hôm nay: " + info.getTotalTimeText(),
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    log("Check-out that bai");
                }

            } catch (Exception ex) {
                log("LOI: " + ex.getMessage());
                JOptionPane.showMessageDialog(this,
                        "Lỗi check-out: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(checkInBtn);
        buttonPanel.add(checkOutBtn);

        rightPanel.add(buttonPanel, BorderLayout.CENTER);

        mainPanel.add(leftPanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);

        add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * Public method to refresh check-in data (called by UserPanel on tab change)
     */
    public void refreshData() {
        try {
            int count = cardComm.getCheckInCount();
            countLabel.setText("Số buổi đã tập: " + count + " buổi");

            CheckInInfo info = cardComm.getLastCheckIn();
            if (info != null && !info.date.isEmpty()) {
                // Hiển thị trạng thái và thời gian
                String today = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
                if (info.date.equals(today)) {
                    if (info.isCheckedIn) {
                        lastLabel.setText(String.format(
                                "<html>Trạng thái: <b style='color:green'>Đang trong phòng tập</b><br>Vào lúc: %s<br>Thời gian hôm nay: %s</html>",
                                info.checkInTime, info.getTotalTimeText()));
                        decorator.addCheckInDate(info.date);
                        currentCheckInDate = info.date;
                    } else {
                        lastLabel.setText(String.format(
                                "<html>Trạng thái: <b style='color:#8e44ad'>Đã rời phòng tập</b><br>Vào: %s | Ra: %s<br>Thời gian hôm nay: %s</html>",
                                info.checkInTime, info.checkOutTime, info.getTotalTimeText()));
                        decorator.addCompletedDate(info.date);
                    }
                } else {
                    lastLabel.setText(String.format(
                            "<html>Lần tập gần nhất:<br>%s<br>Vào: %s | Ra: %s</html>",
                            info.date, info.checkInTime, info.checkOutTime));
                }
                lastLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                lastLabel.setForeground(new Color(52, 73, 94));
            }
            log("Đã tải thông tin check-in từ thẻ");

        } catch (Exception ex) {
            log("LỖI tải check-in: " + ex.getMessage());
        }
    }
}
