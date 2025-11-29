# 🏋️ HỆ THỐNG QUẢN LÝ THẺ TẬP GYM SỬ DỤNG SMARTCARD

## 🎨 Mô tả

Hệ thống quản lý thẻ tập gym với **giao diện desktop hiện đại** và đầy đủ chức năng:
- **JavaCard Applet**: Chạy trên thẻ thông minh (hoặc JCIDE simulator)
- **Desktop Application**: Ứng dụng Java Swing với **giao diện đẹp mắt, gradient design, modern UI**
- **Mock Card Mode**: Demo không cần phần cứng

## ✨ Giao diện hiện đại v2.0
- 🎨 **Gradient backgrounds** - Headers với hiệu ứng gradient đẹp mắt
- 🔘 **Rounded buttons** - Nút bấm bo tròn, có hover & press effects
- 🎯 **Modern typography** - Font Segoe UI, kích thước hợp lý
- 🌈 **Professional colors** - Color palette hiện đại, dễ nhìn
- 📐 **Better spacing** - Khoảng cách hợp lý, không chật chội
- 💡 **Enhanced UX** - Trải nghiệm người dùng được cải thiện đáng kể

👉 Xem chi tiết: [UI_IMPROVEMENTS.md](UI_IMPROVEMENTS.md)

## ⚡ PHIÊN BẢN DEMO - KHÔNG CẦN THẺ VẬT LÝ

**Lưu ý quan trọng**: Phiên bản này sử dụng **Mock Card** (thẻ ảo) để demo giao diện.
- ✅ Không cần đầu đọc thẻ vật lý
- ✅ Không cần cài đặt driver PC/SC
- ✅ Chạy được ngay trên máy tính bất kỳ
- ✅ Phù hợp để demo giao diện và test UI
- ✅ Có thể tích hợp với JCIDE sau này

## Cấu trúc dự án

```
JavaSmartCard/
├── src/
│   ├── gymcard/
│   │   ├── GymCardApplet.java          # JavaCard Applet (để tích hợp JCIDE sau)
│   │   └── client/
│   │       ├── GymCardApp.java         # Ứng dụng chính
│   │       ├── CardCommunicator.java   # Mock Card (Thẻ ảo - không cần thẻ vật lý)
│   │       ├── MemberInfo.java         # Model thông tin hội viên
│   │       ├── PackageInfo.java        # Model thông tin gói tập
│   │       ├── CheckInInfo.java        # Model thông tin check-in
│   │       ├── TransactionInfo.java    # Model thông tin giao dịch
│   │       └── ui/
│   │           ├── AdminPanel.java     # Giao diện quản trị
│   │           └── UserPanel.java      # Giao diện hội viên
├── build_client.bat                    # Build ứng dụng
├── run.bat                             # Chạy ứng dụng
└── README.md
```

## Chức năng chính

### 1. QUẢN LÝ THÔNG TIN HỘI VIÊN
- ✅ Lưu trữ thông tin cá nhân (họ tên, ngày sinh, SĐT, địa chỉ)
- ✅ Thông tin gói tập (loại gói, thời hạn, ngày đăng ký)
- ✅ Hỗ trợ lưu ảnh thẻ (tối đa 1KB)

### 2. XÁC THỰC VÀ BẢO MẬT
- ✅ Xác thực bằng mã PIN (4 chữ số)
- ✅ Giới hạn số lần nhập sai PIN (3 lần)
- ✅ Mã hóa thông tin cá nhân bằng AES-128
- ✅ Thay đổi mã PIN
- ✅ Mở khóa thẻ bằng mã PIN Admin
- ✅ Cảnh báo số lần nhập còn lại

### 3. CHECK-IN/CHECK-OUT
- ✅ Ghi nhận thời gian vào/ra phòng tập
- ✅ Theo dõi số ngày tập trong tháng
- ✅ Lưu trữ lịch sử check-in

### 4. THANH TOÁN VÀ NẠP TIỀN
- ✅ Nạp tiền vào tài khoản thẻ
- ✅ Thanh toán dịch vụ (HLV riêng, đồ uống, khăn tập)
- ✅ Theo dõi số dư tài khoản
- ✅ Lịch sử giao dịch (10 giao dịch gần nhất)

### 5. QUẢN LÝ GÓI TẬP
- ✅ **Gói Tháng**: Tập không giới hạn trong thời hạn
- ✅ **Gói Theo Buổi**: Số buổi tập cố định
- ✅ **Gói VIP**: Gói cao cấp với đặc quyền
- ✅ Chuyển đổi/nâng cấp gói

## Mã PIN mặc định

| Loại | Mã PIN | Ghi chú |
|------|--------|---------|
| User PIN | 1234 | Đăng nhập hội viên (Mock) |
| Admin PIN | 9999 | Mở khóa thẻ (Mock) |

**Lưu ý Mock Mode**: 
- PIN được lưu trong RAM
- Có thể thay đổi tự do
- Reset khi ngắt kết nối

## Hướng dẫn sử dụng

### YÊU CẦU HỆ THỐNG (Đơn giản)
1. Java Runtime Environment (JRE) 8 trở lên
2. **Không cần** đầu đọc thẻ
3. **Không cần** cài đặt driver
4. **Không cần** thẻ vật lý

### BƯỚC 1: Build ứng dụng

```bash
# Chạy script build
build_client.bat

# Hoặc build thủ công
mkdir build\classes
javac -d build\classes -sourcepath src src\gymcard\client\*.java src\gymcard\client\ui\*.java
cd build\classes
jar cvfe ..\..\dist\GymCardClient.jar gymcard.client.GymCardApp gymcard\
cd ..\..
```

### BƯỚC 2: Chạy ứng dụng

```bash
# Sử dụng script
run.bat

# Hoặc chạy trực tiếp
java -jar dist\GymCardClient.jar
```

### BƯỚC 3: Sử dụng chức năng (Demo Mode)

#### A. QUẢN TRỊ VIÊN

#### A. QUẢN TRỊ VIÊN

**Bước 0**: Kết nối thẻ ảo
- Nhấn nút "Kết nối thẻ" ở góc trên
- Chọn YES để tải dữ liệu demo (hoặc NO để bắt đầu từ đầu)

1. **Đăng ký hội viên mới**n
   - Chọn loại gói tập
   - Nhấn "Đăng ký hội viên mới"

2. **Quản lý thẻ**
   - Xem thông tin chi tiết thẻ
   - Xem lịch sử giao dịch

3. **Nâng cấp gói tập**
4. **Mở khóa thẻ**
   - Chọn tab "Mở khóa thẻ"
   - Nhập mã PIN Admin (`9999`)
   - Nhấn "Mở khóa thẻ"
   - Thẻ ảo được reset về trạng thái ban đầu

#### B. HỘI VIÊN

1. **Đăng nhập**
   - Chọn tab "Hội viên"
   - Nhập mã PIN (mặc định: `1234`)
   - Nhấn "Đăng nhập"
   - ⚠️ **Demo**: Chỉ có 3 lần nhập sai, sau đó thẻ sẽ bị khóa (mock)
   - Chọn tab "Hội viên"
   - Nhập mã PIN (mặc định: `1234`)
   - Nhấn "Đăng nhập"
   - ⚠️ **Lưu ý**: Chỉ có 3 lần nhập sai, sau đó thẻ sẽ bị khóa

2. **Xem thông tin cá nhân**
   - Tab "Thông tin cá nhân"
   - Nhấn "Tải thông tin"

3. **Check-in/Check-out**
   - Tab "Check-in/Check-out"
   - Nhấn "CHECK-IN" khi vào phòng tập
   - Nhấn "CHECK-OUT" khi ra về
   - Xem lịch sử tập luyện

4. **Thay đổi PIN**
   - Tab "Thay đổi PIN"
   - Nhập PIN cũ và PIN mới
   - Nhấn "Thay đổi PIN"

5. **Thanh toán**
   - Tab "Thanh toán"
   - Nạp tiền vào tài khoản
   - Thanh toán dịch vụ
   - Xem số dư
## Cấu trúc dữ liệu (Mock trong bộ nhớ)

### Dữ liệu được lưu trong RAM (simulation)
## Cấu trúc dữ liệu trên thẻ

### Thông tin hội viên
- Họ tên: tối đa 50 ký tự
- Ngày sinh: 8 ký tự (YYYYMMDD)
- Số điện thoại: tối đa 15 ký tự
- Địa chỉ: tối đa 100 ký tự
- Ảnh thẻ: tối đa 1KB

### Thông tin gói tập
- Loại gói: 1 byte (1=Tháng, 2=Buổi, 3=VIP)
- Ngày hết hạn: 8 ký tự (YYYYMMDD)
- Ngày đăng ký: 8 ký tự (YYYYMMDD)
- Số buổi còn lại: 2 bytes

### Giao dịch
## Tính năng Mock Card

### Ưu điểm:
✅ **Không cần phần cứng**: Chạy được ngay không cần thẻ hoặc đầu đọc
✅ **Demo nhanh**: Phù hợp cho presentation và testing UI
✅ **Dữ liệu linh hoạt**: Có thể load demo data hoặc bắt đầu từ đầu
✅ **Tích hợp JCIDE**: Dễ dàng chuyển sang JCIDE sau này
✅ **Development**: Phát triển UI mà không lo phần cứng

### Các method được simulate:
- ✅ connect() - Kết nối thẻ ảo (luôn thành công)
- ✅ verifyPin() - Xác thực PIN với giới hạn 3 lần
- ✅ changePin() - Thay đổi PIN
- ✅ unlockPin() - Mở khóa bằng admin PIN
- ✅ setMemberInfo() / getMemberInfo() - Quản lý thông tin
- ✅ setPackage() / getPackage() - Quản lý gói tập
- ✅ checkIn() / checkOut() - Check-in/out
- ✅ addBalance() / deductBalance() - Thanh toán
- ✅ getTransaction() - Lịch sử giao dịch

### Demo Data:
Khi chọn "Load demo data", hệ thống sẽ tạo:
- Hội viên: Nguyễn Văn Demo
- Gói: VIP (50 buổi)
- Số dư: 500,000 VNĐ
- 15 ngày tập trong tháng
- 5 giao dịch mẫu

## Lệnh APDU (Dành cho tích hợp JCIDE) giao dịch gần nhất
- Mỗi giao dịch: Ngày/Giờ + Số tiền + Loại

## Lệnh APDU

### Authentication (0x2X)
- `0x20`: Verify PIN
- `0x21`: Change PIN
- `0x22`: Unlock PIN (Admin)
- `0x23`: Get PIN tries

### Member Management (0x3X)
- `0x30`: Set member info
- `0x31`: Get member info
- `0x32`: Set member photo
- `0x33`: Get member photo

### Package Management (0x4X)
- `0x40`: Set package
- `0x41`: Get package
## Xử lý sự cố

### Ứng dụng không chạy
```bash
# Kiểm tra Java
java -version

# Build lại
build_client.bat

# Chạy lại
run.bat
```

### Thẻ ảo bị khóa (Mock)
- Sử dụng chức năng "Mở khóa thẻ" trong tab Quản trị viên
- Nhập mã PIN Admin: `9999`
- Thẻ ảo sẽ được reset

### Reset toàn bộ dữ liệu
- Ngắt kết nối
- Kết nối lại
- Chọn NO khi hỏi load demo data

## Tích hợp với JCIDE

Để tích hợp với JCIDE sau này:

1. **Cài đặt JCIDE** và tạo virtual card
2. **Load GymCardApplet.java** vào JCIDE
3. **Compile và deploy** applet trên thẻ ảo JCIDE
4. **Thay thế CardCommunicator** để giao tiếp với JCIDE API
5. **Test** các APDU commands với thẻ ảo JCIDE

## Phát triển thêm

### Để chuyển sang thẻ thật:
- [ ] Thay thế CardCommunicator bằng PC/SC implementation
- [ ] Thêm xử lý APDU commands thực tế
- [ ] Cài đặt applet lên thẻ vật lý
- [ ] Test với đầu đọc thẻ

### Tích hợp JCIDE:
- [ ] Import project vào JCIDE
- [ ] Compile applet trong JCIDE
- [ ] Deploy lên virtual card
- [ ] Cập nhật CardCommunicator để giao tiếp JCIDE API
- [ ] Test end-to-end với simulator

### Cải thiện UI:
- [ ] Thêm animation
- [ ] Thêm biểu đồ thống kê
- [ ] Export báo cáo PDF
- [ ] Multi-language support

## Phát triển thêm (Tương lai)
- `0x9000`: Thành công
- `0x6301`: Yêu cầu xác thực PIN
- `0x6983`: Thẻ bị khóa
- `0x6984`: PIN không đúng
- `0x6A80`: Gói tập đã hết hạn
- `0x6A81`: Số dư không đủ

## Bảo mật

1. **Mã hóa AES-128**: Tất cả thông tin nhạy cảm được mã hóa
2. **PIN Protection**: Giới hạn 3 lần nhập sai
3. **Admin Override**: Chỉ admin mới có thể mở khóa thẻ
4. **Secure Channel**: Giao tiếp an toàn với thẻ

## Xử lý sự cố

### Thẻ bị khóa
- Sử dụng chức năng "Mở khóa thẻ" trong tab Quản trị viên
- Nhập mã PIN Admin: `9999`

### Không kết nối được thẻ
- Kiểm tra đầu đọc thẻ đã được kết nối
- Đảm bảo thẻ đặt đúng vị trí
- Kiểm tra applet đã được cài đặt

### Quên mã PIN
- Liên hệ quản trị viên để reset
- Admin có thể mở khóa và hướng dẫn đặt lại PIN

## Phát triển thêm

Các tính năng có thể mở rộng:
- [ ] Sinh trắc học (vân tay)
- [ ] Tích hợp camera chụp ảnh
- [ ] Báo cáo thống kê chi tiết
- [ ] Kết nối online/cloud
- [ ] Ứng dụng mobile
- [ ] QR Code check-in

## Tác giả

Dự án JavaCard Gym Management System

## Giấy phép

Educational purpose - KMA University

---

**Lưu ý**: Đây là **phiên bản DEMO với Mock Card** - không cần thẻ vật lý. Dữ liệu được lưu trong RAM và sẽ mất khi tắt ứng dụng. Phù hợp để demo giao diện và test chức năng UI. Có thể tích hợp với JCIDE hoặc thẻ vật lý sau này.
