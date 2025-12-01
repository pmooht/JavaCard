package gymcard.client;

import java.util.List;
import javax.smartcardio.*;

public class TestCardConnection {
    public static void main(String[] args) {
        try {
            System.out.println("=== KIỂM TRA KẾT NỐI THẺ ===\n");
            
            TerminalFactory factory = TerminalFactory.getDefault();
            List<CardTerminal> terminals = factory.terminals().list();
            
            if (terminals.isEmpty()) {
                System.out.println("❌ KHÔNG tìm thấy đầu đọc thẻ nào!");
                System.out.println("\nĐể JCIDE hoạt động như card reader:");
                System.out.println("1. Mở JCIDE");
                System.out.println("2. Tools > Options > Global Platform");
                System.out.println("3. Chọn 'Enable remote APDU Server'");
                System.out.println("4. Restart JCIDE");
                return;
            }
            
            System.out.println("✓ Tìm thấy " + terminals.size() + " đầu đọc thẻ:\n");
            
            for (int i = 0; i < terminals.size(); i++) {
                CardTerminal terminal = terminals.get(i);
                System.out.println((i+1) + ". Reader: " + terminal.getName());
                
                if (terminal.isCardPresent()) {
                    System.out.println("   ✓ Có thẻ");
                    
                    try {
                        Card card = terminal.connect("*");
                        System.out.println("   ✓ Kết nối thành công");
                        System.out.println("   Protocol: " + card.getProtocol());
                        System.out.println("   ATR: " + bytesToHex(card.getATR().getBytes()));
                        
                        // Thử SELECT applet
                        CardChannel channel = card.getBasicChannel();
                        byte[] AID = {(byte)0x11, (byte)0x22, (byte)0x33, (byte)0x44, (byte)0x55, (byte)0x00};
                        CommandAPDU select = new CommandAPDU(0x00, 0xA4, 0x04, 0x00, AID);
                        
                        System.out.println("   Thử SELECT applet AID: " + bytesToHex(AID));
                        ResponseAPDU response = channel.transmit(select);
                        
                        if (response.getSW() == 0x9000) {
                            System.out.println("   ✓✓✓ SELECT applet THÀNH CÔNG!");
                        } else {
                            System.out.println("   ❌ SELECT applet THẤT BẠI");
                            System.out.println("   SW: " + String.format("%04X", response.getSW()));
                            if (response.getSW() == 0x6A82) {
                                System.out.println("   Lỗi: File/Application not found");
                                System.out.println("   => Kiểm tra AID trong JCIDE có đúng 11 22 33 44 55 00 không?");
                            }
                        }
                        
                        card.disconnect(false);
                    } catch (Exception e) {
                        System.out.println("   ❌ Lỗi: " + e.getMessage());
                    }
                } else {
                    System.out.println("   ❌ Không có thẻ");
                }
                System.out.println();
            }
            
        } catch (Exception e) {
            System.out.println("❌ LỖI: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
}
