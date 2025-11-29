# MOCK CARD - THẺ ẢO CHO DEMO

## 🎯 Mục đích

Mock Card là một implementation giả lập (simulation) của SmartCard, cho phép:
- ✅ Demo giao diện mà không cần thẻ vật lý
- ✅ Test các chức năng UI một cách nhanh chóng
- ✅ Phát triển và debug ứng dụng dễ dàng
- ✅ Presentation và trình bày dự án
- ✅ Chuẩn bị trước khi tích hợp với JCIDE hoặc thẻ thật

## 🔧 Implementation

### Class: CardCommunicator (Mock Version)

```java
Location: src/gymcard/client/CardCommunicator.java
Type: Pure Java (không cần javax.smartcardio)
Storage: RAM-based (in-memory)
```

### Dữ liệu được simulate:

```
✓ Kết nối thẻ (luôn thành công)
✓ Xác thực PIN (với giới hạn 3 lần)
✓ Thông tin hội viên (name, birthDate, phone, address)
✓ Thông tin gói tập (type, expiry, sessions)
✓ Check-in/Check-out history
✓ Số dư và giao dịch
✓ Đếm số ngày tập
```

## 📊 So sánh với thẻ thật

| Tính năng | Mock Card | Thẻ Vật lý | JCIDE |
|-----------|-----------|------------|-------|
| Cần phần cứng | ❌ Không | ✅ Cần | ❌ Không |
| Lưu trữ dữ liệu | RAM | EEPROM | Simulator |
| Tốc độ | Nhanh | Trung bình | Nhanh |
| Bảo mật | Demo only | Cao | Trung bình |
| Dùng để | Demo UI | Production | Testing |
| Chi phí | $0 | $10-50 | $0 |

## 🎮 Các method được implement

### 1. Connection Management
```java
connect()           // Simulate kết nối (delay 500ms)
disconnect()        // Ngắt kết nối
isConnected()       // Kiểm tra trạng thái
```

### 2. Authentication
```java
verifyPin(pin)          // Xác thực PIN, giảm tries
changePin(old, new)     // Đổi PIN
unlockPin(adminPin)     // Mở khóa với admin PIN
getPinTries()           // Lấy số lần còn lại
```

### 3. Member Management
```java
setMemberInfo(...)      // Lưu thông tin (vào RAM)
getMemberInfo()         // Đọc thông tin
```

### 4. Package Management
```java
setPackage(...)         // Thiết lập gói
getPackage()            // Đọc thông tin gói
```

### 5. Check-in/out
```java
checkIn(date, time)     // Check-in, tăng counter
checkOut(time)          // Check-out
getCheckInCount()       // Đếm số lần
getLastCheckIn()        // Lần check-in gần nhất
```

### 6. Payment
```java
getBalance()            // Xem số dư
addBalance(amount)      // Nạp tiền
deductBalance(amount)   // Trừ tiền
getTransaction(index)   // Xem giao dịch
```

### 7. Demo Utilities
```java
resetCard()             // Reset về mặc định
loadDemoData()          // Load dữ liệu mẫu
```

## 🎨 Demo Data

Khi gọi `loadDemoData()`, hệ thống tạo:

```java
Member:
  Name: "Nguyễn Văn Demo"
  Birth: "19900101"
  Phone: "0987654321"
  Address: "123 Đường ABC, Quận XYZ, Hà Nội"

Package:
  Type: 3 (VIP)
  Expiry: "20261231"
  Registration: "20250101"
  Sessions: 50

Check-in:
  Date: "20251129"
  Time In: "080000"
  Time Out: "100000"
  Count: 15

Balance:
  Amount: 500 (nghìn VNĐ)

Transactions:
  5 giao dịch mẫu (add/deduct)
```

## 💾 Lifecycle dữ liệu

```
[Start App]
    ↓
[Connect Card] ← Tạo Mock Card instance
    ↓
[Load Demo?]
    ↓ YES                    ↓ NO
[loadDemoData()]    [Empty data]
    ↓                        ↓
[Use App] ← Dữ liệu trong RAM
    ↓
[Disconnect] ← Dữ liệu vẫn trong RAM
    ↓
[Connect Again] ← Dữ liệu còn đó
    ↓
[Close App] ← MẤT dữ liệu
```

## 🔄 Chuyển đổi sang thẻ thật

Để chuyển từ Mock Card sang thẻ vật lý:

### 1. Replace CardCommunicator
```java
// Thay thế implementation
import javax.smartcardio.*;

public class CardCommunicator {
    private Card card;
    private CardChannel channel;
    // ... PC/SC implementation
}
```

### 2. Add APDU commands
```java
private ResponseAPDU sendCommand(byte ins, byte[] data) {
    CommandAPDU cmd = new CommandAPDU(0x80, ins, 0x00, 0x00, data);
    return channel.transmit(cmd);
}
```

### 3. Update methods
```java
public boolean verifyPin(String pin) throws CardException {
    byte[] pinBytes = pinToBytes(pin);
    ResponseAPDU resp = sendCommand(INS_VERIFY_PIN, pinBytes);
    return resp.getSW() == 0x9000;
}
```

## 🔧 Tích hợp JCIDE

Để tích hợp với JCIDE:

### 1. Cài đặt JCIDE
- Download JCIDE từ website
- Tạo project và import GymCardApplet.java

### 2. Compile Applet
- Build applet trong JCIDE
- Deploy lên virtual card

### 3. Update CardCommunicator
```java
// Sử dụng JCIDE API thay vì Mock
import com.licel.jcardsim.base.Simulator;

public class CardCommunicator {
    private Simulator simulator;
    // ... JCIDE implementation
}
```

## 📝 Testing với Mock Card

### Scenario 1: PIN Flow
```
1. Connect card
2. verifyPin("0000") → false, tries = 2
3. verifyPin("0000") → false, tries = 1  
4. verifyPin("0000") → false, tries = 0 (BLOCKED)
5. unlockPin("9999") → true, tries = 3 (RESET)
6. verifyPin("1234") → true (SUCCESS)
```

### Scenario 2: Balance Flow
```
1. Connect & auth
2. getBalance() → 0
3. addBalance(100) → balance = 100
4. deductBalance(30) → balance = 70
5. deductBalance(100) → false (insufficient)
```

### Scenario 3: Check-in Flow
```
1. Connect & auth
2. checkIn("20251129", "080000")
3. getCheckInCount() → 1
4. checkOut("100000")
5. getLastCheckIn() → full info
```

## 🎯 Ưu điểm Mock Card

### Cho Developer:
- ✅ Phát triển UI nhanh chóng
- ✅ Không cần đợi phần cứng
- ✅ Debug dễ dàng (println)
- ✅ Có thể test offline

### Cho Demo:
- ✅ Setup nhanh (không cần cài đặt gì)
- ✅ Reliable (không lo lỗi phần cứng)
- ✅ Có thể preset data
- ✅ Trình bày mượt mà

### Cho Testing:
- ✅ Unit test dễ dàng
- ✅ Integration test nhanh
- ✅ Không cần môi trường đặc biệt
- ✅ CI/CD friendly

## ⚠️ Giới hạn

### Mock Card KHÔNG có:
- ❌ Bảo mật thực sự (không có crypto chip)
- ❌ Persistence (dữ liệu mất khi tắt)
- ❌ Xác thực phần cứng
- ❌ Giới hạn bộ nhớ thực tế

### Không nên dùng Mock Card cho:
- ❌ Production environment
- ❌ Security testing
- ❌ Performance testing
- ❌ Hardware integration testing

## 🚀 Roadmap

### Phase 1: Mock Card (DONE) ✅
- Pure Java implementation
- Full UI demo
- No hardware needed

### Phase 2: JCIDE Integration
- [ ] Setup JCIDE project
- [ ] Compile applet in JCIDE
- [ ] Connect to virtual card
- [ ] Test APDU commands

### Phase 3: Physical Card
- [ ] Get ACR122U reader
- [ ] Get JCOP card
- [ ] Install applet
- [ ] Full system test

## 📚 Tài liệu tham khảo

- `CardCommunicator.java` - Mock implementation
- `GymCardApp.java` - UI integration
- `README.md` - System overview
- `QUICK_START.md` - Demo guide

---

**Mock Card - Making SmartCard development accessible to everyone! 🎉**
