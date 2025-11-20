package smartcard;

import javax.smartcardio.*;
import java.util.List;

/**
 * Quản lý kết nối và giao tiếp với SmartCard
 */
public class CardManager {
    
    private static CardManager instance;
    private CardTerminal terminal;
    private Card card;
    private CardChannel channel;
    private boolean isConnected;
    
    // AID của applet
    private static final byte[] APPLET_AID = {
        (byte)0xA0, 0x00, 0x00, 0x00, 0x62, 0x03, 0x01, 0x0C, 0x06, 0x01
    };
    
    private CardManager() {
        isConnected = false;
        initTerminal();
    }
    
    public static CardManager getInstance() {
        if (instance == null) {
            instance = new CardManager();
        }
        return instance;
    }
    
    /**
     * Khởi tạo đầu đọc thẻ
     */
    private void initTerminal() {
        try {
            TerminalFactory factory = TerminalFactory.getDefault();
            List<CardTerminal> terminals = factory.terminals().list();
            
            if (!terminals.isEmpty()) {
                terminal = terminals.get(0);
                System.out.println("Đầu đọc thẻ: " + terminal.getName());
            } else {
                System.out.println("Không tìm thấy đầu đọc thẻ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Kiểm tra có thẻ không
     */
    public boolean isCardPresent() {
        try {
            return terminal != null && terminal.isCardPresent();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Kết nối với thẻ
     */
    public boolean connect() {
        try {
            if (terminal == null || !terminal.isCardPresent()) {
                System.out.println("Không có thẻ trong đầu đọc");
                return false;
            }
            
            card = terminal.connect("*");
            channel = card.getBasicChannel();
            
            // Select applet
            ResponseAPDU response = selectApplet();
            if (response != null && response.getSW() == 0x9000) {
                isConnected = true;
                System.out.println("Kết nối thẻ thành công");
                return true;
            } else {
                System.out.println("Không thể chọn applet");
                return false;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            isConnected = false;
            return false;
        }
    }
    
    /**
     * Ngắt kết nối thẻ
     */
    public void disconnect() {
        try {
            if (card != null) {
                card.disconnect(false);
                isConnected = false;
                System.out.println("Ngắt kết nối thẻ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Select applet
     */
    private ResponseAPDU selectApplet() {
        try {
            CommandAPDU selectCmd = new CommandAPDU(
                (byte)0x00, (byte)0xA4, (byte)0x04, (byte)0x00, APPLET_AID
            );
            return channel.transmit(selectCmd);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Gửi APDU command
     */
    public ResponseAPDU sendCommand(byte cla, byte ins, byte p1, byte p2, byte[] data) {
        if (!isConnected) {
            System.out.println("Chưa kết nối với thẻ");
            return null;
        }
        
        try {
            CommandAPDU command;
            if (data == null || data.length == 0) {
                command = new CommandAPDU(cla, ins, p1, p2);
            } else {
                command = new CommandAPDU(cla, ins, p1, p2, data);
            }
            
            ResponseAPDU response = channel.transmit(command);
            return response;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Gửi APDU command với length expected
     */
    public ResponseAPDU sendCommand(byte cla, byte ins, byte p1, byte p2, byte[] data, int le) {
        if (!isConnected) {
            System.out.println("Chưa kết nối với thẻ");
            return null;
        }
        
        try {
            CommandAPDU command;
            if (data == null || data.length == 0) {
                command = new CommandAPDU(cla, ins, p1, p2, le);
            } else {
                command = new CommandAPDU(cla, ins, p1, p2, data, le);
            }
            
            ResponseAPDU response = channel.transmit(command);
            return response;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public boolean isConnected() {
        return isConnected;
    }
    
    public String getTerminalName() {
        return terminal != null ? terminal.getName() : "Không có";
    }
    
    /**
     * Đọc ATR (Answer To Reset)
     */
    public String getATR() {
        if (card != null) {
            ATR atr = card.getATR();
            byte[] atrBytes = atr.getBytes();
            StringBuilder sb = new StringBuilder();
            for (byte b : atrBytes) {
                sb.append(String.format("%02X ", b));
            }
            return sb.toString();
        }
        return "";
    }
}