# HƯỚNG DẪN NHANH - GYM CARD SYSTEM (DEMO MODE)

## 🚀 Chạy ngay (3 bước đơn giản)

### Bước 1: Build
```bash
build_simple.bat
```

### Bước 2: Chạy
```bash
run.bat
```

### Bước 3: Sử dụng
1. Nhấn nút **"Kết nối thẻ"** (góc trên bên phải)
2. Chọn **YES** để tải dữ liệu demo
3. Thử các tính năng!

---

## 📱 Demo các chức năng

### 🔐 Đăng nhập (Tab Hội viên)
- **PIN mặc định**: `1234`
- Sau khi load demo data, thử đăng nhập với PIN này

### 👨‍💼 Quản trị viên (Tab Quản trị viên)

#### 1. Xem thông tin thẻ
- Tab "Quản lý thẻ" → Nhấn "Tải thông tin thẻ"
- Xem đầy đủ thông tin demo

#### 2. Đăng ký hội viên mới  
- Tab "Đăng ký hội viên"
- Điền form (ví dụ):
  ```
  Họ tên: Trần Văn B
  Ngày sinh: 19950615
  SĐT: 0912345678
  Địa chỉ: Hà Nội
  Gói: Gói Tháng
  Hết hạn: 20261231
  ```
- Nhấn "Đăng ký hội viên mới"

#### 3. Mở khóa thẻ (khi bị khóa)
- Tab "Mở khóa thẻ"
- Nhập Admin PIN: `9999`
- Nhấn "Mở khóa thẻ"

### 👥 Hội viên (Tab Hội viên)

#### 1. Xem thông tin cá nhân
- Đăng nhập với PIN: `1234`
- Tab "Thông tin cá nhân" → "Tải thông tin"

#### 2. Check-in/Check-out
- Tab "Check-in/Check-out"
- Nhấn nút **"CHECK-IN"** (xanh lá)
- Sau đó nhấn **"CHECK-OUT"** (đỏ)
- Xem thống kê số ngày tập

#### 3. Thay đổi PIN
- Tab "Thay đổi PIN"
- Nhập:
  - PIN hiện tại: `1234`
  - PIN mới: `5678`
  - Xác nhận: `5678`
- Nhấn "Thay đổi PIN"
- **Lưu ý**: Đăng xuất và đăng nhập lại với PIN mới

#### 4. Thanh toán
- Tab "Thanh toán"
- **Nạp tiền**: Nhập số tiền (VD: 100) → Nhấn "Nạp tiền"
- **Thanh toán**: Chọn dịch vụ → Nhấn "Thanh toán"
- **Xem số dư**: Nhấn "Tải lại số dư"

---

## 🎯 Test Case nhanh

### Test 1: PIN đúng ✅
1. Tab Hội viên → Nhập PIN: `1234` → Đăng nhập
2. **Kết quả**: Đăng nhập thành công

### Test 2: PIN sai (3 lần) ❌→🔓
1. Tab Hội viên → Nhập PIN sai: `0000` (3 lần)
2. **Kết quả**: Thẻ bị khóa
3. Tab Quản trị viên → Mở khóa thẻ → Admin PIN: `9999`
4. **Kết quả**: Thẻ được mở khóa

### Test 3: Check-in flow ✅
1. Đăng nhập
2. Tab Check-in/Check-out → CHECK-IN
3. Xem thông tin → Số ngày tập tăng lên
4. CHECK-OUT → Thời gian ra được ghi nhận

### Test 4: Thanh toán ✅
1. Đăng nhập
2. Tab Thanh toán → Nạp 100k
3. Thanh toán dịch vụ 50k
4. Kiểm tra số dư còn 550k (demo có 500k + 100k - 50k)

---

## 💡 Tips

### Reset dữ liệu
- Ngắt kết nối (nút "Ngắt kết nối")
- Kết nối lại
- Chọn **NO** khi hỏi load demo

### Demo data có gì?
```
Hội viên: Nguyễn Văn Demo
Ngày sinh: 19900101
SĐT: 0987654321
Gói: VIP (50 buổi)
Số dư: 500,000 VNĐ
Số ngày tập: 15
Giao dịch: 5 giao dịch mẫu
```

### Thử nhiều tính năng
1. ✅ Đăng ký hội viên mới
2. ✅ Check-in nhiều lần
3. ✅ Thay đổi PIN
4. ✅ Nạp tiền và thanh toán
5. ✅ Xem lịch sử giao dịch
6. ✅ Khóa và mở khóa thẻ

---

## ⚠️ Lưu ý

- **Không cần thẻ vật lý**: Hệ thống dùng Mock Card (thẻ ảo)
- **Dữ liệu trong RAM**: Dữ liệu sẽ mất khi đóng ứng dụng
- **Phù hợp demo**: Dùng để demo giao diện và test chức năng
- **Tích hợp JCIDE**: Có thể tích hợp với JCIDE sau

---

## 🆘 Troubleshooting

### Lỗi compile
```bash
# Kiểm tra Java
java -version

# Build lại
build_simple.bat
```

### Lỗi khi chạy
```bash
# Chạy trực tiếp
java -cp build\classes gymcard.client.GymCardApp
```

### Màn hình trống
- Đợi vài giây để giao diện load
- Thử resize cửa sổ

---

## 📚 Xem thêm

- **README.md**: Hướng dẫn đầy đủ
- **INSTALLATION_GUIDE.md**: Hướng dẫn cài đặt chi tiết
- **PROJECT_SUMMARY.md**: Tổng quan dự án

---

**Chúc bạn demo thành công! 🎉**
