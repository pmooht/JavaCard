import ui.MainFrame;

import javax.swing.*;

/**
 * Entry point của ứng dụng Gym Management System
 */
public class Main {
    
    public static void main(String[] args) {
        // Set look and feel
        try {
            // Try to use system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Alternative: Use Nimbus look and feel
            /*
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            */
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Set default font size
        setUIFont(new javax.swing.plaf.FontUIResource("Arial", java.awt.Font.PLAIN, 13));
        
        // Run application in Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
    
    /**
     * Set font cho toàn bộ UI components
     */
    private static void setUIFont(javax.swing.plaf.FontUIResource font) {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, font);
            }
        }
    }
}