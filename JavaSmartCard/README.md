# 🏋️ Hệ Thống Quản Lý Thẻ Tập Gym

## 📱 Giới Thiệu

Ứng dụng quản lý thẻ tập gym với giao diện desktop hiện đại, sử dụng JavaCard và Mock Card để demo không cần phần cứng.

### ✨ Tính Năng Chính

**Admin Panel (3 Tabs):**
- 📝 Đăng ký hội viên (với PIN, avatar placeholder)
- 🔐 Đổi PIN & mở khóa thẻ
- 📊 Quản lý thông tin thẻ

**User Panel (5 Tabs):**
- 👤 Thông tin cá nhân (avatar + chi tiết)
- 📦 Quản lý gói tập (3 loại gói)
- 📅 Check-in/out (lịch tháng + thống kê)
- 🔐 Đổi PIN
- 💰 Thanh toán & nạp tiền

### 🎨 Design Highlights
- Modern UI với gradient backgrounds
- Rounded buttons (15-20px radius)
- Segoe UI font family
- Color scheme: Green, Blue, Red, Purple
- Responsive hover effects

## 🚀 Hướng Dẫn Sử Dụng

### Yêu Cầu
- Java JDK 8+
- Windows với PowerShell

### Build & Run

**Cách 1: Sử dụng batch files**
```bash
.\build.bat    # Build project
.\run.bat      # Run application
```

**Cách 2: Manual**
```powershell
# Build
javac -d build\classes -sourcepath src -encoding UTF-8 src\gymcard\client\*.java src\gymcard\client\ui\*.java

# Run
java -cp build\classes gymcard.client.GymCardApp
```

### Mã PIN Mặc Định
- **User PIN**: 1234
- **Admin PIN**: 9999

## 📖 Hướng Dẫn Test

### Admin
1. Kết nối thẻ (chọn YES để load demo data)
2. Đăng ký hội viên mới:
   - Nhập thông tin đầy đủ
   - Đặt PIN 4 chữ số
   - Xác nhận PIN
3. Đổi PIN (yêu cầu admin PIN: 9999)
4. Mở khóa thẻ (khi nhập sai PIN 3 lần)
5. Xem thông tin & lịch sử giao dịch

### User
1. Đăng nhập với PIN (mặc định: 1234)
2. Xem thông tin cá nhân (tab 1)
3. Xem gói tập hiện tại & mua gói mới (tab 2)
4. Check-in/out và xem lịch tập (tab 3)
5. Đổi PIN cá nhân (tab 4)
6. Nạp tiền & thanh toán dịch vụ (tab 5)

## 🗂️ Cấu Trúc Project

```
JavaSmartCard/
├── src/gymcard/client/
│   ├── GymCardApp.java           # Main application
│   ├── CardCommunicator.java     # Mock card (RAM-based)
│   ├── MemberInfo.java           # Member model
│   ├── PackageInfo.java          # Package model
│   ├── CheckInInfo.java          # Check-in model
│   ├── TransactionInfo.java      # Transaction model
│   └── ui/
│       ├── AdminPanel.java       # Admin interface (3 tabs)
│       └── UserPanel.java        # User interface (5 tabs)
├── build/                        # Compiled classes
├── build.bat                     # Build script
├── run.bat                       # Run script
└── README.md                     # This file
```

## 🎯 Chức Năng Chi Tiết

### 1. Quản Lý Thành Viên
- Đăng ký thành viên với thông tin đầy đủ
- Lưu trữ avatar (placeholder - tính năng tương lai)
- Thiết lập PIN ban đầu
- Validation đầy đủ (PIN 4 số, thông tin bắt buộc)

### 2. Xác Thực & Bảo Mật
- PIN 4 chữ số
- Giới hạn 3 lần nhập sai
- Admin có thể reset PIN
- Mở khóa thẻ với admin PIN

### 3. Check-in/Check-out
- Ghi nhận giờ vào/ra
- Lịch tháng với đánh dấu ngày tập
- Thống kê số ngày tập
- Lịch sử check-in

### 4. Quản Lý Gói Tập
**3 loại gói:**
- 🏋️ **Gói Tháng**: 300k/tháng (tập không giới hạn)
- 💪 **Gói Buổi**: 500k/20 buổi (linh hoạt thời gian)
- ⭐ **Gói VIP**: 2000k/3 tháng (đầy đủ tiện ích)

**Logic mua gói:**
- Xem gói hiện tại
- Chọn gói mới → Chuyển sang tab thanh toán
- Sau khi thanh toán → Kích hoạt gói

### 5. Thanh Toán
- Hiển thị số dư tài khoản
- Nạp tiền (10k-10000k)
- Thanh toán dịch vụ:
  - 🏋️ HLV riêng: 200k
  - 💧 Nước uống: 20k
  - 🧖 Khăn tập: 10k
  - 🥤 Protein shake: 50k
  - 🍎 Dinh dưỡng: 100k
- Lịch sử 10 giao dịch gần nhất

## 🎨 Design System

### Date & Time Format
- **Date Format**: `dd/MM/yyyy` (định dạng Việt Nam)
- **Time Format**: `HH:mm:ss` (24 giờ với giây)
- **Examples**:
  - Date: `30/11/2025`
  - Time: `14:30:45`

### Calendar Component
- **Library**: JCalendar 1.4
- **Features**:
  - Interactive month/year selection
  - Check-in days highlighted with green background
  - Automatic refresh on check-in/check-out
  - Clean, professional appearance

### Colors
- **Green** (#2ecc71): Success, Check-in
- **Blue** (#3498db): Info, Primary
- **Red** (#e74c3c): Warning, Check-out
- **Purple** (#9b59b6): Premium, VIP

### Typography
- **Font**: Segoe UI
- **Sizes**: 12-28px
- **Weights**: Regular, Bold

### Components
- Border radius: 15-20px
- Button height: 40-60px
- Padding: 20-30px
- Shadow: rgba(0,0,0,0.1)

## 🔮 Tính Năng Tương Lai

### Priority High
- [ ] Upload và lưu ảnh avatar thực
- [ ] Tích hợp backend cho mua gói tập
- [ ] Payment gateway integration

### Priority Medium
- [ ] Navigation lịch (prev/next month)
- [ ] Export data to Excel/PDF
- [ ] Email notifications

### Priority Low
- [ ] Statistics charts
- [ ] QR code check-in
- [ ] Mobile app companion

## 📝 Notes

- **Mock Mode**: Dữ liệu lưu trong RAM, reset khi ngắt kết nối
- **Production**: Cần tích hợp với JavaCard thật hoặc database
- **UI**: Đã hoàn thành 100% redesign v2.0

## 📞 Support

Gặp vấn đề? Kiểm tra:
1. Java version (phải >= 8)
2. Encoding UTF-8
3. Build thành công chưa
4. Console output để debug

---

**Version:** 2.0.0  
**Last Updated:** November 30, 2025  
**Status:** ✅ Production Ready (UI Demo)
