package gymcard.client.ui;

import com.toedter.calendar.JCalendar;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;

/**
 * Custom decorator for JCalendar to highlight check-in days
 * - Purple: Check-in done but not checked out yet
 * - Green: Check-in and check-out completed
 */
public class CheckInDayDecorator {

    private final JCalendar calendar;
    private final Map<String, Boolean> checkInDates; // date -> hasCheckedOut
    private final SimpleDateFormat dateFormat;

    // Colors - XANH LA: dang trong phong tap, TIM: hoan thanh buoi tap
    private static final Color COLOR_CHECKING_IN = new Color(200, 230, 201); // Light green - dang check-in
    private static final Color BORDER_CHECKING_IN = new Color(76, 175, 80); // Green
    private static final Color COLOR_COMPLETED = new Color(233, 213, 255); // Light purple - da checkout
    private static final Color BORDER_COMPLETED = new Color(155, 89, 182); // Purple

    public CheckInDayDecorator(JCalendar calendar) {
        this.calendar = calendar;
        this.checkInDates = new HashMap<>();
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    }

    /**
     * Add a check-in date (not checked out yet - purple)
     */
    public void addCheckInDate(String date) {
        checkInDates.put(date, false); // false = not checked out yet
        updateCalendar();
    }

    /**
     * Mark a date as checked out (green)
     */
    public void markDateCheckedOut(String date) {
        if (checkInDates.containsKey(date)) {
            checkInDates.put(date, true); // true = checked out
            updateCalendar();
        }
    }

    /**
     * Add a completed check-in/check-out date (green)
     */
    public void addCompletedDate(String date) {
        checkInDates.put(date, true); // true = completed
        updateCalendar();
    }

    /**
     * Add multiple check-in dates (default: completed)
     */
    public void addCheckInDates(Collection<String> dates) {
        for (String date : dates) {
            checkInDates.put(date, true); // Assume completed for demo data
        }
        updateCalendar();
    }

    /**
     * Clear all check-in dates
     */
    public void clearCheckInDates() {
        checkInDates.clear();
        updateCalendar();
    }

    /**
     * Update calendar to apply decorations
     */
    private void updateCalendar() {
        // Get the day chooser panel
        Component[] components = calendar.getDayChooser().getDayPanel().getComponents();

        for (Component comp : components) {
            if (comp instanceof JButton) {
                JButton dayButton = (JButton) comp;

                // Get the date for this button
                try {
                    String buttonText = dayButton.getText();
                    if (buttonText != null && !buttonText.isEmpty() && !buttonText.equals("")) {
                        int day = Integer.parseInt(buttonText);

                        // Get current month and year from calendar
                        Calendar cal = calendar.getCalendar();
                        cal.set(Calendar.DAY_OF_MONTH, day);
                        String dateStr = dateFormat.format(cal.getTime());

                        // Check if this date has a check-in
                        if (checkInDates.containsKey(dateStr)) {
                            boolean isCompleted = checkInDates.get(dateStr);

                            if (isCompleted) {
                                // Green for completed check-out
                                dayButton.setBackground(COLOR_COMPLETED);
                                dayButton.setBorder(BorderFactory.createLineBorder(BORDER_COMPLETED, 2));
                            } else {
                                // Purple for check-in only (not checked out)
                                dayButton.setBackground(COLOR_CHECKING_IN);
                                dayButton.setBorder(BorderFactory.createLineBorder(BORDER_CHECKING_IN, 2));
                            }
                            dayButton.setOpaque(true);
                            dayButton.setBorderPainted(true);
                        } else {
                            // Reset to default
                            dayButton.setBackground(Color.WHITE);
                            dayButton.setOpaque(true);
                        }
                    }
                } catch (NumberFormatException e) {
                    // Not a day button (might be empty or label)
                }
            }
        }

        calendar.repaint();
    }

    /**
     * Add property change listener to update on month/year change
     */
    public void install() {
        calendar.getDayChooser().addPropertyChangeListener("day", evt -> updateCalendar());
        calendar.getMonthChooser().addPropertyChangeListener("month", evt -> updateCalendar());
        calendar.getYearChooser().addPropertyChangeListener("year", evt -> updateCalendar());
        updateCalendar();
    }
}
