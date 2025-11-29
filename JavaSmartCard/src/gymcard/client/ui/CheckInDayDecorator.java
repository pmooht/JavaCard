package gymcard.client.ui;

import com.toedter.calendar.JCalendar;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;

/**
 * Custom decorator for JCalendar to highlight check-in days
 */
public class CheckInDayDecorator {
    
    private final JCalendar calendar;
    private final Set<String> checkInDates; // dd/MM/yyyy format
    private final SimpleDateFormat dateFormat;
    
    public CheckInDayDecorator(JCalendar calendar) {
        this.calendar = calendar;
        this.checkInDates = new HashSet<>();
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    }
    
    /**
     * Add a check-in date to highlight
     */
    public void addCheckInDate(String date) {
        checkInDates.add(date);
        updateCalendar();
    }
    
    /**
     * Add multiple check-in dates
     */
    public void addCheckInDates(Collection<String> dates) {
        checkInDates.addAll(dates);
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
                        if (checkInDates.contains(dateStr)) {
                            // Highlight with green background
                            dayButton.setBackground(new Color(200, 230, 201)); // Light green
                            dayButton.setOpaque(true);
                            dayButton.setBorderPainted(true);
                            dayButton.setBorder(BorderFactory.createLineBorder(new Color(76, 175, 80), 2));
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
