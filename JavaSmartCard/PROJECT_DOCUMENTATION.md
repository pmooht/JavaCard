# 🏋️ GYM CARD SYSTEM - TÀI LIỆU DỰ ÁN

> **Hệ thống Quản lý Thẻ Phòng Gym sử dụng JavaCard (SmartCard) với bảo mật AES-128 và RSA-1024**

---

## 📋 MỤC LỤC

1. [Tổng quan dự án](#1-tổng-quan-dự-án)
2. [Kiến trúc hệ thống](#2-kiến-trúc-hệ-thống)
3. [Cơ chế bảo mật](#3-cơ-chế-bảo-mật)
4. [Luồng hoạt động chi tiết](#4-luồng-hoạt-động-chi-tiết)
5. [Cấu trúc Database](#5-cấu-trúc-database)
6. [APDU Commands](#6-apdu-commands)
7. [Giao diện người dùng](#7-giao-diện-người-dùng)
8. [Cấu trúc mã nguồn](#8-cấu-trúc-mã-nguồn)

---

## 1. TỔNG QUAN DỰ ÁN

### 1.1 Mô tả
Đây là hệ thống quản lý thẻ phòng gym hoàn chỉnh, sử dụng công nghệ **JavaCard (SmartCard)** kết hợp với:
- **AES-128**: Mã hóa dữ liệu cá nhân trên thẻ
- **RSA-1024**: Xác thực thẻ (chống clone)
- **SQLite**: Lưu trữ dữ liệu phía server

### 1.2 Tính năng chính

| Vai trò | Tính năng |
|---------|-----------|
| **Admin** | Đăng ký hội viên mới, quản lý gói tập, quản lý dịch vụ, mở khóa thẻ, reset PIN |
| **User** | Check-in/out, xem thông tin cá nhân, nạp tiền, mua gói tập, mua dịch vụ, đổi PIN |

### 1.3 Công nghệ sử dụng
- **JavaCard 2.2.2+**: Applet chạy trên SmartCard
- **Java Swing**: Giao diện desktop
- **SQLite**: Database nhẹ
- **javax.smartcardio**: Giao tiếp với đầu đọc thẻ

---

## 2. KIẾN TRÚC HỆ THỐNG

### 2.1 Sơ đồ tổng quan

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          GymCardApp (Main UI)                           │
│                         ┌──────────────────┐                            │
│    ┌────────────────────┤   WelcomePanel   ├────────────────────┐       │
│    │                    └──────────────────┘                    │       │
│    ▼                                                            ▼       │
│ ┌──────────────┐                                      ┌──────────────┐  │
│ │  AdminPanel  │                                      │  UserPanel   │  │
│ │ ─────────────│                                      │ ─────────────│  │
│ │• Registration│                                      │• InfoTab     │  │
│ │• PackageMgmt │                                      │• CheckInTab  │  │
│ │• ServiceMgmt │                                      │• TopUpTab    │  │
│ │• PinMgmt     │                                      │• PackageTab  │  │
│ └──────┬───────┘                                      │• ServicesTab │  │
│        │                                              │• StatisticsTab│ │
│        │                                              │• ChangePinTab│  │
│        │                                              └──────┬───────┘  │
│        └────────────────────┬───────────────────────────────┘           │
└─────────────────────────────┼───────────────────────────────────────────┘
                              │
                              ▼
              ┌────────────────────────────────┐
              │       CardCommunicator         │  ← Lớp trung gian
              │  (Giao tiếp App ↔ SmartCard)   │
              └──────────────┬─────────────────┘
                             │
              ┌──────────────┴─────────────────┐
              ▼                                ▼
    ┌──────────────────┐             ┌──────────────────┐
    │   CardManager    │             │  DatabaseManager │
    │   (APDU Layer)   │             │    (SQLite)      │
    └────────┬─────────┘             └──────────────────┘
             │                                
             ▼ (APDU Commands)
    ┌──────────────────────────────────────────────────┐
    │              JavaCard Applet                     │
    │            (ProjectCuoiKy.java)                  │
    │  ┌────────────────────────────────────────────┐  │
    │  │ • AES-128 Encryption (Master Key)          │  │
    │  │ • RSA-1024 Key Pair                        │  │
    │  │ • PIN Management (User + Admin)            │  │
    │  │ • Personal Data Storage (Encrypted)        │  │
    │  │ • Balance, Check-in, Services              │  │
    │  └────────────────────────────────────────────┘  │
    └──────────────────────────────────────────────────┘
```

### 2.2 Các thành phần chính

| Thành phần | File | Mô tả |
|------------|------|-------|
| **Main App** | `GymCardApp.java` | Entry point, quản lý navigation |
| **Card Communicator** | `CardCommunicator.java` | Lớp trung gian giao tiếp với thẻ |
| **Card Manager** | `CardManager.java` | Gửi/nhận APDU commands |
| **Database Manager** | `DatabaseManager.java` | Quản lý SQLite database |
| **Applet** | `ProjectCuoiKy.java` | Chạy trên JavaCard |
| **Crypto Utils** | `AESUtils.java`, `RSAUtils.java` | Tiện ích mã hóa |

---

## 3. CƠ CHẾ BẢO MẬT

### 3.1 Master Key Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    MASTER KEY ARCHITECTURE                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   [Init Card]                                                    │
│        │                                                         │
│        ▼                                                         │
│   ┌────────────────┐                                            │
│   │ Generate Random│──▶ MK (16 bytes AES Key)                   │
│   │  Master Key    │                                            │
│   └────────────────┘                                            │
│        │                                                         │
│        ├─────────┐                                               │
│        ▼         ▼                                               │
│   ┌─────────┐ ┌─────────┐                                       │
│   │User PIN │ │Admin PIN│                                       │
│   │+Salt    │ │+Salt    │                                       │
│   └────┬────┘ └────┬────┘                                       │
│        │           │                                             │
│        ▼           ▼                                             │
│   SHA-256      SHA-256                                          │
│        │           │                                             │
│        ▼           ▼                                             │
│   [PIN Key]   [Admin Key]  (16 bytes mỗi cái)                   │
│        │           │                                             │
│        ▼           ▼                                             │
│   ┌─────────────────────────────────────┐                       │
│   │      AES Encrypt(MK)                │                       │
│   └─────────────────────────────────────┘                       │
│        │           │                                             │
│        ▼           ▼                                             │
│   encMK_User   encMK_Admin  ← Lưu trong EEPROM                  │
│                                                                  │
│   + SHA-256(MK || Salt) → mkHash ← Để verify khi unlock         │
└─────────────────────────────────────────────────────────────────┘
```

**Giải thích:**
1. Khi khởi tạo thẻ, **Master Key (MK)** 16 bytes được sinh ngẫu nhiên
2. MK được mã hóa bằng **2 keys khác nhau**:
   - `encMK_User`: MK mã hóa bằng key derive từ User PIN
   - `encMK_Admin`: MK mã hóa bằng key derive từ Admin PIN
3. Hash của MK (`mkHash`) được lưu để verify khi unlock
4. Khi user nhập PIN đúng → Decrypt encMK_User → Lấy lại MK

### 3.2 RSA Private Key Protection

```
┌───────────────────────────────────────────────────────────────────┐
│                 RSA PRIVATE KEY PROTECTION                         │
├───────────────────────────────────────────────────────────────────┤
│                                                                    │
│   [Init Card] → Generate RSA-1024 KeyPair                         │
│                                                                    │
│   Public Key:                                                      │
│   ├─ Modulus (128 bytes) → cardPublicKey (Plaintext, export OK)   │
│   └─ Exponent = 65537 (F4)                                        │
│                                                                    │
│   Private Key:                                                     │
│   ├─ Modulus (128 bytes) → AES(MK) → encPrivMod (EEPROM)         │
│   └─ Exponent (128 bytes) → AES(MK) → encPrivExp (EEPROM)        │
│                                                                    │
│   Khi cần ký (signChallenge):                                     │
│   1. Decrypt encPrivMod/encPrivExp → RAM                          │
│   2. Nạp vào RSAPrivateKey object                                 │
│   3. Ký challenge                                                  │
│   4. CLEAR RAM ngay lập tức (security!)                           │
└───────────────────────────────────────────────────────────────────┘
```

**Mục đích RSA Authentication:**
- Ngăn chặn **thẻ clone** (thẻ giả có thể copy dữ liệu nhưng KHÔNG có private key)
- Private key được sinh trên thẻ, mã hóa bằng MK, **KHÔNG BAO GIỜ** rời khỏi thẻ

### 3.3 Dữ liệu được mã hóa trên thẻ

| Field | Max Size | Mã hóa AES? | Mô tả |
|-------|----------|-------------|-------|
| `Name` | 64 bytes | ✅ Có | Họ và tên |
| `DOB` | 16 bytes | ✅ Có | Ngày sinh |
| `Phone` | 16 bytes | ✅ Có | Số điện thoại |
| `Address` | 128 bytes | ✅ Có | Địa chỉ |
| `CardID` | 32 bytes | ✅ Có | Mã thẻ |
| `Avatar` | 8KB | ✅ Có | Ảnh đại diện |
| `Balance` | 16 bytes | ✅ Có | Số dư tài khoản |
| `Check-in` | 352 bytes | ❌ Không | Lịch sử check-in |
| `Services` | 256 bytes | ❌ Không | Dịch vụ đã mua |

### 3.4 PIN Security

| Thông số | Giá trị |
|----------|---------|
| Độ dài PIN | 6 chữ số |
| Số lần thử tối đa | 3 lần |
| PIN mặc định | `000000` |
| Hành động khi hết lượt | Thẻ bị KHÓA |
| Mở khóa | Admin dùng Admin PIN |

---

## 4. LUỒNG HOẠT ĐỘNG CHI TIẾT

### 4.1 Luồng 1: Khởi tạo thẻ mới (Admin)

```
┌─────────────────────────────────────────────────────────────────────┐
│                      INIT NEW CARD FLOW                              │
└─────────────────────────────────────────────────────────────────────┘

 Admin nhập thông tin    CardCommunicator         CardManager           Applet
       │                       │                       │                   │
       │ 1.Click "Đăng ký"     │                       │                   │
       │──────────────────────▶│                       │                   │
       │                       │ 2.initNewCard(cardId, │                   │
       │                       │   "000000")           │                   │
       │                       │──────────────────────▶│                   │
       │                       │                       │ 3.APDU:           │
       │                       │                       │ INS_INIT_CARD     │
       │                       │                       │──────────────────▶│
       │                       │                       │                   │
       │                       │                       │   4. Applet:      │
       │                       │                       │   - Save CardID   │
       │                       │                       │   - Gen MasterKey │
       │                       │                       │   - Hash MK→mkHash│
       │                       │                       │   - Encrypt MK    │
       │                       │                       │     với UserPIN   │
       │                       │                       │     và AdminPIN   │
       │                       │                       │   - Gen RSA Pair  │
       │                       │                       │   - Encrypt PrivKey│
       │                       │                       │   - Set isDefaultPin=true
       │                       │                       │◀─────────9000────│
       │                       │ 5.setMemberInfo()     │                   │
       │                       │──────────────────────▶│                   │
       │                       │                       │ WRITE_PERSONAL    │
       │                       │                       │──────────────────▶│
       │                       │                       │   Encrypt + Save  │
       │                       │                       │◀─────────9000────│
       │                       │                       │                   │
       │                       │ 6.savePublicKeyToDB   │                   │
       │                       │─────────────────────────────▶ DatabaseManager
       │                       │   (Lưu public key để │        │
       │                       │    xác thực RSA sau)  │        │
       │◀──────────────────────│                       │        │
       │  "Đăng ký thành công"  │                       │        │
```

**Chi tiết các bước:**

1. Admin điền form đăng ký (tên, SĐT, ngày sinh, địa chỉ, ảnh)
2. Hệ thống tự sinh CardID dạng `GYM000001`, `GYM000002`...
3. Gửi lệnh `INS_INIT_CARD` xuống thẻ với:
   - CardID
   - User PIN mặc định: `000000`
   - Admin PIN: `123456`
4. Applet thực hiện:
   - Sinh Master Key ngẫu nhiên
   - Mã hóa MK bằng cả 2 PIN
   - Sinh RSA key pair
   - Mã hóa Private Key bằng MK
5. Ghi thông tin cá nhân (đã mã hóa AES)
6. Lưu Public Key vào SQLite database

### 4.2 Luồng 2: Đăng nhập hội viên (User)

```
┌─────────────────────────────────────────────────────────────────────┐
│                      USER LOGIN FLOW                                 │
└─────────────────────────────────────────────────────────────────────┘

  GymCardApp                CardCommunicator              Applet
      │                           │                          │
      │ 1. Click "Hội viên"       │                          │
      │───────────────────────────│                          │
      │                           │                          │
      │ 2. Lấy Card Fingerprint   │                          │
      │   (Public Key Modulus)    │                          │
      │───────────────────────────│                          │
      │                           │ INS_GET_CARD_PUB         │
      │                           │─────────────────────────▶│
      │                           │◀────────modulus──────────│
      │                           │                          │
      │ 3. Kiểm tra isKnownCard   │                          │
      │   (so sánh fingerprint    │                          │
      │    với known_cards.txt)   │                          │
      │                           │                          │
      ├───────────────────────────┤                          │
      │ Nếu THẺ MỚI:              │                          │
      ├───────────────────────────┤                          │
      │ 4. Thử PIN mặc định       │                          │
      │    "000000"               │                          │
      │───────────────────────────│                          │
      │                           │ verifyPinWithCardAuth()  │
      │                           │─────────────────────────▶│
      │                           │◀───────success───────────│
      │                           │                          │
      │ 5. Check isDefaultPin     │ INS_CHECK_DEFAULT_PIN    │
      │                           │─────────────────────────▶│
      │                           │◀───────true──────────────│
      │                           │                          │
      │ 6. Bắt buộc đổi PIN       │                          │
      │   (showForceChangePinDialog)                         │
      │                           │                          │
      ├───────────────────────────┤                          │
      │ Nếu THẺ ĐÃ BIẾT:          │                          │
      ├───────────────────────────┤                          │
      │ 7. Hiện dialog nhập PIN   │                          │
      │───────────────────────────│                          │
      │                           │                          │
      │ 8. verifyPinWithCardAuth  │                          │
      │───────────────────────────│                          │
      │                           │ a. INS_VERIFY_PIN        │
      │                           │─────────────────────────▶│
      │                           │   - Derive key từ PIN    │
      │                           │   - Decrypt encMK_User   │
      │                           │   - Verify hash          │
      │                           │   - Load MK vào RAM      │
      │                           │◀───────9000──────────────│
      │                           │                          │
      │                           │ b. RSA Challenge-Response│
      │                           │   - Gen random challenge │
      │                           │ INS_SIGN_CHALLENGE       │
      │                           │─────────────────────────▶│
      │                           │   - Decrypt private key  │
      │                           │   - Sign challenge       │
      │                           │   - Clear RAM            │
      │                           │◀───────signature─────────│
      │                           │                          │
      │                           │ c. Verify signature      │
      │                           │   với public key từ DB   │
      │                           │                          │
      │◀──────────────────────────│                          │
      │  AuthResult {             │                          │
      │    pinVerified: true,     │                          │
      │    rsaVerified: true      │                          │
      │  }                        │                          │
      │                           │                          │
      │ 9. Vào UserPanel          │                          │
```

**Logic xác thực 2 lớp:**
1. **PIN Verification**: Đảm bảo người dùng biết PIN
2. **RSA Challenge-Response**: Đảm bảo thẻ là chính chủ (không phải clone)

### 4.3 Luồng 3: Xác thực RSA (Challenge-Response)

```
┌─────────────────────────────────────────────────────────────────────┐
│                RSA CARD AUTHENTICATION                               │
└─────────────────────────────────────────────────────────────────────┘

   Client App                Database              SmartCard
       │                        │                      │
       │ 1. Lấy stored          │                      │
       │    public key          │                      │
       │───────────────────────▶│                      │
       │◀───────modulus─────────│                      │
       │                        │                      │
       │ 2. Tạo challenge       │                      │
       │    (32 bytes random)   │                      │
       │                        │                      │
       │ 3. Gửi challenge       │                      │
       │    để thẻ ký           │                      │
       │───────────────────────────────────────────────▶│
       │                        │    INS_SIGN_CHALLENGE │
       │                        │                      │
       │                        │   Thẻ: Decrypt PrivKey│
       │                        │   từ EEPROM → RAM     │
       │                        │   Ký challenge        │
       │                        │   Clear PrivKey RAM   │
       │                        │                      │
       │◀──────────────────────────────signature────────│
       │                        │                      │
       │ 4. Verify signature    │                      │
       │    bằng stored pubKey  │                      │
       │                        │                      │
       │ 5. Nếu khớp → Thẻ      │                      │
       │    là CHÍNH CHỦ        │                      │
       │    Nếu sai → THẺ GIẢ   │                      │
```

### 4.4 Luồng 4: Check-in/Check-out

```
┌─────────────────────────────────────────────────────────────────────┐
│                    CHECK-IN/OUT FLOW                                 │
└─────────────────────────────────────────────────────────────────────┘

   User                  CheckInTab              CardCommunicator
     │                       │                         │
     │ 1. Click CHECK-IN     │                         │
     │──────────────────────▶│                         │
     │                       │ 2. checkIn(date, time)  │
     │                       │────────────────────────▶│
     │                       │                         │
     │                       │   - Đọc lastCheckIn từ thẻ
     │                       │   - Kiểm tra đã check-in chưa
     │                       │   - Tính toán buổi tập mới/cũ
     │                       │   - Ghi FIELD_CHECKIN   │
     │                       │                         │
     │                       │◀────────success─────────│
     │◀──────────────────────│                         │
     │   "Check-in OK"       │                         │
     │   Calendar đánh dấu   │                         │
     │   màu xanh            │                         │
     │                       │                         │
     │ 3. Click CHECK-OUT    │                         │
     │──────────────────────▶│                         │
     │                       │ 4. checkOut(time)       │
     │                       │────────────────────────▶│
     │                       │   - Tính thời gian buổi tập
     │                       │   - Cộng dồn totalMinutesToday
     │                       │   - Ghi FIELD_CHECKIN   │
     │                       │   - Thêm vào history    │
     │                       │◀────────success─────────│
     │◀──────────────────────│                         │
     │   "Check-out OK"      │                         │
     │   Hiển thị tổng       │                         │
     │   thời gian tập       │                         │
```

**Tính năng Check-in:**
- Hỗ trợ ra vào **nhiều lần trong ngày** (cộng dồn thời gian)
- Tự động tính số buổi tập
- Hiển thị trên lịch (màu xanh = đang tập, màu tím = đã checkout)

### 4.5 Luồng 5: Đổi PIN

```
┌─────────────────────────────────────────────────────────────────────┐
│                      CHANGE PIN FLOW                                 │
└─────────────────────────────────────────────────────────────────────┘

                                                   Applet
   User                  CardCommunicator              │
     │                         │                       │
     │ 1. Nhập oldPIN, newPIN  │                       │
     │────────────────────────▶│                       │
     │                         │ INS_CHANGE_PIN        │
     │                         │ (oldPIN || newPIN)    │
     │                         │──────────────────────▶│
     │                         │                       │
     │                         │  a. Verify oldPIN     │
     │                         │     - Derive key      │
     │                         │     - Decrypt encMK_User
     │                         │     - Verify hash     │
     │                         │                       │
     │                         │  b. Re-encrypt MK     │
     │                         │     với newPIN        │
     │                         │     - Derive new key  │
     │                         │     - Encrypt MK      │
     │                         │     - Save encMK_User │
     │                         │                       │
     │                         │  c. isDefaultPin=false│
     │                         │                       │
     │                         │◀───────9000───────────│
     │                         │                       │
     │                         │ Update public key     │
     │                         │ trong DB              │
     │                         │                       │
     │◀────────────────────────│                       │
     │   "Đổi PIN thành công"   │                       │
```

---

## 5. CẤU TRÚC DATABASE

### 5.1 Schema SQLite

```sql
-- Bảng users: Lưu public key của từng thẻ
CREATE TABLE users (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_code TEXT UNIQUE NOT NULL,        -- CardID hoặc SĐT
  card_public_key TEXT NOT NULL,          -- RSA public key (Base64)
  status TEXT NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME
);

-- Bảng membership_plans: Các gói tập
CREATE TABLE membership_plans (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  code TEXT UNIQUE NOT NULL,              -- BASIC, STANDARD, PREMIUM...
  name TEXT NOT NULL,
  description TEXT,
  duration_days INTEGER,                  -- Thời hạn (ngày)
  session_count INTEGER,                  -- Số buổi (gói theo lượt)
  max_duration_minutes INTEGER DEFAULT 0, -- Giới hạn phút/buổi (0=unlimited)
  price REAL NOT NULL,
  is_active INTEGER NOT NULL DEFAULT 1,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Bảng services: Dịch vụ bổ sung
CREATE TABLE services (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  code TEXT UNIQUE NOT NULL,
  name TEXT NOT NULL,
  description TEXT,
  price REAL NOT NULL,
  service_type TEXT DEFAULT 'CONSUMABLE', -- CONSUMABLE / SESSION
  is_active INTEGER NOT NULL DEFAULT 1,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Bảng user_memberships: User đang dùng gói nào
CREATE TABLE user_memberships (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL,
  plan_id INTEGER NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE,
  remaining_sessions INTEGER,
  status TEXT NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (plan_id) REFERENCES membership_plans(id)
);

-- Bảng checkin_logs: Lịch sử vào/ra
CREATE TABLE checkin_logs (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL,
  checkin_time DATETIME NOT NULL,
  checkout_time DATETIME,
  device_id TEXT,
  note TEXT,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Bảng transactions: Nạp tiền, mua gói, dịch vụ
CREATE TABLE transactions (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL,
  type TEXT NOT NULL,                     -- TOP_UP, BUY_PACKAGE, BUY_SERVICE
  amount REAL NOT NULL,
  description TEXT,
  related_plan_id INTEGER,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (related_plan_id) REFERENCES membership_plans(id)
);
```

### 5.2 Gói tập mặc định

| Code | Tên | Thời hạn | Giới hạn/buổi | Giá |
|------|-----|----------|---------------|-----|
| BASIC | Gói Basic | 30 ngày | 2 giờ | 300,000đ |
| STANDARD | Gói Standard | 30 ngày | 4 giờ | 500,000đ |
| PREMIUM | Gói Premium | 30 ngày | Không giới hạn | 800,000đ |
| SESSIONS_20 | Gói 20 Buổi | 90 ngày | 3 giờ | 600,000đ |

---

## 6. APDU COMMANDS

### 6.1 Danh sách lệnh

| INS Code | Tên lệnh | Mô tả | Auth cần? |
|----------|----------|-------|-----------|
| `0x10` | `INS_INIT_CARD` | Khởi tạo thẻ mới | Không |
| `0x20` | `INS_VERIFY_PIN` | Xác thực PIN | Không |
| `0x21` | `INS_CHANGE_PIN` | Đổi PIN | Có |
| `0x22` | `INS_UNLOCK` | Admin mở khóa thẻ | Admin PIN |
| `0x23` | `INS_ADMIN_SET_PIN` | Admin reset PIN | Admin PIN |
| `0x24` | `INS_CHECK_DEFAULT_PIN` | Kiểm tra PIN mặc định | Có |
| `0x30` | `INS_WRITE_PERSONAL` | Ghi dữ liệu cá nhân | Có |
| `0x31` | `INS_READ_PERSONAL` | Đọc dữ liệu cá nhân | Có |
| `0x32` | `INS_GET_TRIES` | Lấy số lần thử PIN còn | Không |
| `0x40` | `INS_GET_CARD_PUB` | Lấy public key | Không |
| `0x41` | `INS_SIGN_CHALLENGE` | Ký challenge RSA | Có |
| `0x50` | `INS_AVATAR_BEGIN` | Bắt đầu ghi avatar | Có |
| `0x51` | `INS_AVATAR_CHUNK` | Ghi chunk avatar | Có |
| `0x52` | `INS_AVATAR_END` | Kết thúc ghi avatar | Có |
| `0x53` | `INS_AVATAR_READ_CHUNK` | Đọc chunk avatar | Có |
| `0x55` | `INS_GET_MEM` | Lấy thông tin memory | Không |

### 6.2 Field IDs

| Field ID | Tên | Kích thước |
|----------|-----|------------|
| `0x00` | FIELD_NAME | 64 bytes |
| `0x01` | FIELD_DOB | 16 bytes |
| `0x02` | FIELD_PHONE | 16 bytes |
| `0x03` | FIELD_ADDRESS | 128 bytes |
| `0x04` | FIELD_PACKAGE | 32 bytes |
| `0x05` | FIELD_CARDID | 32 bytes |
| `0x06` | FIELD_AVATAR | 8192 bytes |
| `0x07` | FIELD_CHECKIN | 352 bytes |
| `0x08` | FIELD_BALANCE | 16 bytes |
| `0x09` | FIELD_SERVICES | 256 bytes |

### 6.3 Status Words

| SW | Ý nghĩa |
|----|---------|
| `0x9000` | Thành công |
| `0x63CX` | PIN sai, còn X lần thử |
| `0x6983` | Thẻ bị khóa |
| `0x6982` | Chưa xác thực PIN |
| `0x6A86` | P1P2 không đúng |
| `0x6700` | Độ dài sai |

---

## 7. GIAO DIỆN NGƯỜI DÙNG

### 7.1 Welcome Screen
- Logo + tên hệ thống
- Nút **Kết nối thẻ**
- Card **Quản trị viên** → AdminPanel
- Card **Hội viên** → UserPanel (yêu cầu kết nối + PIN)

### 7.2 Admin Panel

| Tab | Chức năng |
|-----|-----------|
| 🏠 Trang chủ | Dashboard, thống kê nhanh |
| 📝 Đăng ký hội viên | Form nhập thông tin + khởi tạo thẻ |
| 📦 Quản lý gói tập | CRUD membership plans |
| 🛒 Quản lý dịch vụ | CRUD services |
| 🔐 Đổi PIN & Mở khóa | Admin reset PIN, unlock thẻ |

### 7.3 User Panel

| Tab | Chức năng |
|-----|-----------|
| 🏠 Trang chủ | Quick actions, thông tin nhanh |
| 👤 Thông tin cá nhân | Xem/sửa thông tin, avatar |
| 📦 Gói tập | Xem gói hiện tại, gia hạn |
| ✅ Check-in/out | Ghi nhận vào/ra, xem lịch |
| 💰 Nạp tiền | Nạp tiền vào thẻ |
| 🛒 Dịch vụ thêm | Mua dịch vụ bổ sung |
| 📊 Thống kê | Lịch sử tập luyện |
| 🔑 Đổi mã PIN | User tự đổi PIN |

---

## 8. CẤU TRÚC MÃ NGUỒN

```
src/
└── gymcard/
    ├── Applet/
    │   └── ProjectCuoiKy.java      # JavaCard Applet (chạy trên thẻ)
    │
    ├── CardManager/
    │   ├── CardIdGenerator.java    # Sinh CardID tự động
    │   └── CardManager.java        # Giao tiếp APDU với thẻ
    │
    ├── client/
    │   ├── CardCommunicator.java   # Lớp trung gian (business logic)
    │   ├── GymCardApp.java         # Main entry point
    │   ├── MemberInfo.java         # DTO thông tin hội viên
    │   ├── PackageInfo.java        # DTO thông tin gói tập
    │   ├── CheckInInfo.java        # DTO thông tin check-in
    │   ├── CheckInLogEntry.java    # DTO log check-in
    │   ├── TransactionInfo.java    # DTO giao dịch
    │   │
    │   └── ui/
    │       ├── WelcomePanel.java   # Màn hình chào
    │       ├── AdminPanel.java     # Panel admin
    │       ├── UserPanel.java      # Panel user
    │       ├── SidebarPanel.java   # Sidebar navigation
    │       ├── BaseTabPanel.java   # Base class cho tabs
    │       ├── CheckInDayDecorator.java  # Decorator cho lịch
    │       │
    │       └── tabs/
    │           ├── RegistrationTab.java      # Tab đăng ký
    │           ├── PackageManagementTab.java # Tab quản lý gói
    │           ├── ServiceManagementTab.java # Tab quản lý dịch vụ
    │           ├── PinManagementTab.java     # Tab quản lý PIN
    │           ├── InfoTab.java              # Tab thông tin cá nhân
    │           ├── CheckInTab.java           # Tab check-in
    │           ├── TopUpTab.java             # Tab nạp tiền
    │           ├── PackageTab.java           # Tab gói tập
    │           ├── ServicesTab.java          # Tab dịch vụ
    │           ├── StatisticsTab.java        # Tab thống kê
    │           ├── ChangePinTab.java         # Tab đổi PIN
    │           └── LoginPanel.java           # Panel đăng nhập
    │
    ├── Crypto/
    │   ├── AESUtils.java           # Tiện ích AES
    │   ├── RSAUtils.java           # Tiện ích RSA
    │   ├── RSAKeyUtils.java        # Import/Export RSA key
    │   ├── CryptoUtils.java        # Các hàm crypto chung
    │   └── AppKeyStore.java        # Quản lý keystore
    │
    └── databaseManager/
        └── DatabaseManager.java    # Quản lý SQLite database
```

---

## 9. HƯỚNG DẪN CHẠY DỰ ÁN

### 9.1 Yêu cầu
- JDK 8 trở lên
- JavaCard Development Kit
- Đầu đọc SmartCard (PC/SC compatible)
- SmartCard hỗ trợ JavaCard 2.2.2+

### 9.2 Các bước

1. **Build Applet** và nạp lên thẻ:
   ```bash
   # Sử dụng build.bat hoặc build.xml
   ./build.bat
   ```

2. **Chạy ứng dụng client**:
   ```bash
   ./run.bat
   ```

3. **Luồng sử dụng**:
   - Bật app → Kết nối thẻ
   - Admin: Đăng ký hội viên mới (thẻ trắng)
   - User: Cắm thẻ đã đăng ký → Nhập PIN → Sử dụng

---

## 10. LƯU Ý BẢO MẬT

| Điểm | Chi tiết |
|------|----------|
| ✅ Master Key | Sinh ngẫu nhiên trên thẻ, không bao giờ rời thẻ |
| ✅ Private Key | Mã hóa bằng MK, giải mã chỉ khi cần ký |
| ✅ RAM Cleanup | Private key được xóa khỏi RAM ngay sau khi ký |
| ✅ PIN Lock | 3 lần sai → Thẻ bị khóa |
| ✅ First Login | Bắt buộc đổi PIN mặc định |
| ✅ RSA Auth | Chống clone thẻ hiệu quả |
| ✅ AES-128 | Mọi dữ liệu nhạy cảm đều được mã hóa |

---

> **Tác giả**: Dự án Cuối Kỳ - JavaCard  
> **Ngày tạo tài liệu**: 04/01/2026
