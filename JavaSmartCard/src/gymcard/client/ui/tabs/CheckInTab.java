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
 * Tab check-in/check-out với lịch
 */
public class CheckInTab extends BaseTabPanel {

    public CheckInTab(CardCommunicator cardComm) {
        super(cardComm);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(new Color(248, 249, 250));

        JLabel titleLabel = new JLabel("LỊCH TẬP GYM & CHECK-IN");
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

        // Decorator & demo data
        CheckInDayDecorator decorator = new CheckInDayDecorator(calendar);
        Map<String, String[]> checkInTimes = new HashMap<>();
        checkInTimes.put("02/11/2025", new String[] { "08:00:00", "10:30:00" });
        checkInTimes.put("04/11/2025", new String[] { "07:45:00", "09:15:00" });
        checkInTimes.put("06/11/2025", new String[] { "18:30:00", "20:00:00" });
        checkInTimes.put("09/11/2025", new String[] { "08:15:00", "10:45:00" });
        checkInTimes.put("11/11/2025", new String[] { "19:00:00", "21:00:00" });
        checkInTimes.put("16/11/2025", new String[] { "08:30:00", "10:00:00" });
        checkInTimes.put("18/11/2025", new String[] { "07:30:00", "09:30:00" });
        checkInTimes.put("23/11/2025", new String[] { "17:45:00", "19:45:00" });
        checkInTimes.put("25/11/2025", new String[] { "08:00:00", "10:15:00" });
        checkInTimes.put("28/11/2025", new String[] { "18:00:00", "20:30:00" });

        decorator.addCheckInDates(checkInTimes.keySet());
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

        JLabel statsTitle = new JLabel("THỐNG KÊ");
        statsTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statsTitle.setForeground(new Color(52, 73, 94));
        statsPanel.add(statsTitle);

        JLabel countLabel = new JLabel("Số ngày đã tập: 10 ngày");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statsPanel.add(countLabel);

        JLabel lastLabel = new JLabel("<html>Click vào ngày check-in<br>để xem chi tiết giờ vào - ra</html>");
        lastLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lastLabel.setForeground(new Color(127, 140, 141));
        statsPanel.add(lastLabel);

        calendar.getDayChooser().addPropertyChangeListener("day", evt -> {
            java.util.Calendar selectedCal = calendar.getCalendar();
            String selectedDate = new SimpleDateFormat("dd/MM/yyyy").format(selectedCal.getTime());
            if (checkInTimes.containsKey(selectedDate)) {
                String[] times = checkInTimes.get(selectedDate);
                lastLabel.setText(String.format(
                        "<html>Ngày: %s<br>Vào: %s | Ra: %s</html>",
                        selectedDate, times[0], times[1]));
                lastLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                lastLabel.setForeground(new Color(52, 73, 94));
                log("Xem chi tiết: " + selectedDate + " - " + times[0] + " -> " + times[1]);
            } else {
                lastLabel.setText("<html>Click vào ngày check-in<br>để xem chi tiết giờ vào - ra</html>");
                lastLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                lastLabel.setForeground(new Color(127, 140, 141));
            }
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
                    log("Check-in thành công!");

                    CheckInInfo info = cardComm.getLastCheckIn();
                    int count = cardComm.getCheckInCount();

                    countLabel.setText("Số ngày đã tập: " + count + " ngày");
                    lastLabel.setText(String.format(
                            "<html>Lần tập gần nhất:<br>%s<br>Vào: %s | Ra: %s</html>",
                            info.date, info.checkInTime, info.checkOutTime));

                    decorator.addCheckInDate(date);

                    JOptionPane.showMessageDialog(this,
                            "Check-in thành công!\nChúc bạn buổi tập tốt!",
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    log("Check-in thất bại");
                }

            } catch (Exception ex) {
                log("LỖI: " + ex.getMessage());
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
                    log("Check-out thành công!");

                    CheckInInfo info = cardComm.getLastCheckIn();
                    lastLabel.setText(String.format(
                            "<html>Lần tập gần nhất:<br>%s<br>Vào: %s | Ra: %s</html>",
                            info.date, info.checkInTime, info.checkOutTime));

                    JOptionPane.showMessageDialog(this,
                            "Check-out thành công!\nHẹn gặp lại!",
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    log("Check-out thất bại");
                }

            } catch (Exception ex) {
                log("LỖI: " + ex.getMessage());
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
}
