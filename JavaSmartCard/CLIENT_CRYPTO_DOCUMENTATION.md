# TÀI LIỆU CHI TIẾT CLIENT VÀ CRYPTO LAYER
## Dự Án JavaCard Gym Membership System

---

## MỤC LỤC

1. [Tầng Client - Data Models](#1-tầng-client---data-models)
2. [CardCommunicator - Giao Tiếp Thẻ](#2-cardcommunicator---giao-tiếp-thẻ)
3. [Tầng Crypto - Mã Hóa](#3-tầng-crypto---mã-hóa)
4. [Luồng Bảo Mật Tổng Quan](#4-luồng-bảo-mật-tổng-quan)

---

## 1. TẦNG CLIENT - DATA MODELS

### 1.1 MemberInfo.java

**📁 Đường dẫn:** `src/gymcard/client/MemberInfo.java`  
**📊 Số dòng:** 28 dòng  
**🎯 Chức năng:** Model chứa thông tin cá nhân hội viên

#### Các Thuộc Tính:

| Thuộc tính | Kiểu | Mô tả | Giới hạn |
|------------|------|-------|----------|
| `name` | String | Họ và tên | Max 64 bytes UTF-8 |
| `birthDate` | String | Ngày sinh (dd/MM/yyyy) | Max 16 bytes |
| `phone` | String | Số điện thoại | Max 16 bytes |
| `address` | String | Địa chỉ | Max 128 bytes |
| `avatarBytes` | byte[] | Ảnh đại diện (JPEG nén) | Max 8KB |

#### Constructor:
```java
public MemberInfo() {
    this.name = "";
    this.birthDate = "";
    this.phone = "";
    this.address = "";
}
```

---

### 1.2 PackageInfo.java

**📁 Đường dẫn:** `src/gymcard/client/PackageInfo.java`  
**📊 Số dòng:** 80 dòng  
**🎯 Chức năng:** Model chứa thông tin gói tập

#### Các Thuộc Tính:

| Thuộc tính | Kiểu | Mô tả |
|------------|------|-------|
| `type` | byte | 0=chưa có, 1=gói ngày, 2=gói theo lượt |
| `expiry` | String | Ngày hết hạn (dd/MM/yyyy) |
| `registration` | String | Ngày đăng ký |
| `remainingSessions` | int | Số buổi còn lại (gói theo lượt) |
| `maxDurationMinutes` | int | Thời lượng tối đa/buổi (phút), 0=không giới hạn |
| `usedMinutesToday` | int | Số phút đã tập hôm nay |

#### Các Hàm Quan Trọng:

| Hàm | Mô tả | Return |
|-----|-------|--------|
| `getPackageTypeName()` | Lấy tên loại gói | "Gói Ngày" / "Gói Theo Lượt" / "Chưa có gói" |
| `hasTimeLimit()` | Kiểm tra gói có giới hạn thời gian | boolean |
| `isOvertime()` | Kiểm tra đã vượt thời gian chưa | boolean |
| `getRemainingMinutes()` | Lấy số phút còn lại trong buổi | int |
| `getMaxDurationText()` | Format thời lượng tối đa | "2 giờ" / "1h30p" / "Không giới hạn" |

---

### 1.3 CheckInInfo.java

**📁 Đường dẫn:** `src/gymcard/client/CheckInInfo.java`  
**📊 Số dòng:** 60 dòng  
**🎯 Chức năng:** Model chứa thông tin check-in hiện tại

#### Các Thuộc Tính:

| Thuộc tính | Kiểu | Mô tả |
|------------|------|-------|
| `date` | String | Ngày tập (dd/MM/yyyy) |
| `checkInTime` | String | Giờ check-in gần nhất (HH:mm:ss) |
| `checkOutTime` | String | Giờ check-out gần nhất |
| `count` | int | Tổng số buổi đã tập |
| `isCheckedIn` | boolean | Đang trong phòng tập? |
| `totalMinutesToday` | int | Tổng phút đã tập hôm nay |

#### Các Hàm Quan Trọng:

| Hàm | Mô tả | Return |
|-----|-------|--------|
| `getTotalTimeText()` | Format thời gian đã tập | "1 giờ 30 phút" |
| `getStatusText()` | Lấy trạng thái hiện tại | "Đang trong phòng tập" / "Đã rời phòng" / "Chưa check-in" |

---

### 1.4 CheckInLogEntry.java

**📁 Đường dẫn:** `src/gymcard/client/CheckInLogEntry.java`  
**📊 Số dòng:** 70 dòng  
**🎯 Chức năng:** Model cho 1 entry trong lịch sử check-in (tối đa 10 entries)

#### Các Thuộc Tính:

| Thuộc tính | Kiểu | Mô tả |
|------------|------|-------|
| `date` | String | Ngày (dd/MM/yyyy) |
| `checkInTime` | String | Giờ check-in đầu tiên (HH:mm) |
| `checkOutTime` | String | Giờ check-out cuối cùng |
| `totalMinutes` | int | Tổng thời gian tập trong ngày |

#### Các Hàm Quan Trọng:

| Hàm | Mô tả | Return |
|-----|-------|--------|
| `serialize()` | Serialize thành String để lưu thẻ | `"date\|inTime\|outTime\|minutes"` |
| `parse(String str)` | Parse từ String đọc từ thẻ | CheckInLogEntry (static) |
| `getTotalTimeText()` | Format thời gian | "1 giờ 30 phút" |

#### Format Lưu Trữ:
```
date|inTime|outTime|minutes
Ví dụ: "29/11/2025|08:00|10:30|150"
```

---

### 1.5 TransactionInfo.java

**📁 Đường dẫn:** `src/gymcard/client/TransactionInfo.java`  
**📊 Số dòng:** 35 dòng  
**🎯 Chức năng:** Model cho giao dịch tài chính

#### Các Thuộc Tính:

| Thuộc tính | Kiểu | Mô tả |
|------------|------|-------|
| `date` | String | Ngày giao dịch |
| `time` | String | Giờ giao dịch |
| `amount` | short | Số tiền (đơn vị 1000 VND) |
| `type` | byte | 1=Nạp tiền, 2=Thanh toán |

#### Các Hàm:

| Hàm | Mô tả |
|-----|-------|
| `getTypeName()` | Trả về "Nạp tiền" hoặc "Thanh toán" |

---

## 2. CARDCOMMUNICATOR - GIAO TIẾP THẺ

**📁 Đường dẫn:** `src/gymcard/client/CardCommunicator.java`  
**📊 Số dòng:** 1280 dòng  
**🎯 Chức năng:** Lớp trung tâm giao tiếp với thẻ JavaCard

### 2.1 Các Hằng Số Giới Hạn

| Hằng số | Giá trị | Mô tả |
|---------|---------|-------|
| `NAME_MAX_LEN` | 64 | Max bytes cho họ tên |
| `DOB_MAX_LEN` | 16 | Max bytes cho ngày sinh |
| `PHONE_MAX_LEN` | 16 | Max bytes cho SĐT |
| `ADDRESS_MAX_LEN` | 128 | Max bytes cho địa chỉ |
| `AVATAR_MAX_LEN` | 8192 | Max bytes cho avatar (8KB) |
| `MAX_BALANCE` | 999,999,999,999 | Số dư tối đa (999 tỷ VND) |
| `MAX_CHECKIN_LOG` | 10 | Số entry lịch sử check-in tối đa |

### 2.2 Các Hàm Kết Nối

| Hàm | Mô tả | Exception |
|-----|-------|-----------|
| `connect()` | Kết nối tới đầu đọc + thẻ | Exception nếu không tìm thấy thẻ |
| `disconnect()` | Ngắt kết nối thẻ | - |
| `isConnected()` | Kiểm tra đã kết nối chưa | - |

### 2.3 Các Hàm Khởi Tạo Thẻ

| Hàm | Mô tả | Tham số | Return |
|-----|-------|---------|--------|
| `initNewCard(String cardId, String userPin)` | Khởi tạo thẻ mới với CardID và PIN | cardId: mã thẻ; userPin: PIN 6 số | CardID đã gán |

#### Chi tiết `initNewCard()`:
```java
/**
 * Khởi tạo thẻ mới:
 * - Sinh CardID tự động (GYM000001, GYM000002, ...)
 * - Gửi APDU INIT_CARD(cardId, pin) xuống thẻ
 * - Trên thẻ: set PIN mới + sinh masterKey từ PIN
 */
public String initNewCard(String cardId, String userPin) throws Exception
```

### 2.4 Các Hàm PIN / Bảo Mật

| Hàm | Mô tả | Return |
|-----|-------|--------|
| `verifyPin(String pin)` | Xác thực PIN với thẻ | true nếu đúng |
| `verifyPinWithCardAuth(String pin)` | Xác thực PIN + RSA challenge-response | AuthResult object |
| `changePin(String oldPin, String newPin)` | Đổi PIN (user tự đổi) | true nếu thành công |
| `checkDefaultPin()` | Kiểm tra PIN mặc định (123456) | true nếu đang dùng PIN mặc định |
| `unlockPin(String adminPass)` | Mở khóa thẻ bằng mật khẩu admin | true nếu thành công |
| `getPinTries()` | Lấy số lần nhập PIN còn lại | int (0-5) |
| `adminResetMemberPin(String adminPass, String newPin)` | Admin đổi PIN cho hội viên | true nếu thành công |

#### Chi tiết `verifyPinWithCardAuth()`:
```java
/**
 * Verify PIN + Xác thực thẻ bằng RSA (nếu có public key trong DB).
 * 
 * Luồng:
 * 1. Verify PIN với thẻ
 * 2. Nếu PIN đúng, đọc CardID từ thẻ
 * 3. Thực hiện RSA challenge-response để xác thực thẻ
 * 
 * @param pin Mã PIN 6 chữ số
 * @return AuthResult chứa kết quả PIN và RSA
 */
public AuthResult verifyPinWithCardAuth(String pin) throws Exception
```

### 2.5 Inner Class AuthResult

```java
public static class AuthResult {
    public boolean pinVerified = false;   // PIN có đúng không
    public boolean rsaVerified = false;   // RSA có verified không
    public boolean rsaSkipped = false;    // RSA bị bỏ qua (thẻ mới)
    public String cardId = null;          // CardID đọc từ thẻ
    public String rsaError = null;        // Lỗi RSA nếu có
    
    public boolean isFullyAuthenticated() {
        return pinVerified && (rsaVerified || rsaSkipped);
    }
}
```

### 2.6 Các Hàm RSA Authentication

| Hàm | Mô tả | Return |
|-----|-------|--------|
| `getCardPublicKey()` | Lấy RSA public key của thẻ | RSAPublicKey object |
| `getCardPublicKeyModulus()` | Lấy modulus dạng byte[] | byte[] |
| `saveCardPublicKeyToDb(String userCode)` | Lưu public key vào DB | userId hoặc 0 nếu update |
| `authenticateCard(String userCode)` | Xác thực thẻ bằng challenge-response | true nếu thẻ chính chủ |

#### Chi tiết `authenticateCard()`:
```java
/**
 * Xác thực thẻ bằng challenge-response RSA.
 * 
 * Luồng:
 * 1. Lấy stored public key từ DB theo userCode
 * 2. Sinh challenge ngẫu nhiên 32 bytes
 * 3. Gửi challenge xuống thẻ để ký
 * 4. Verify signature bằng stored public key
 * 
 * @param userCode Mã người dùng để tra cứu public key trong DB
 * @return true nếu thẻ authentic, false nếu thẻ giả hoặc không khớp
 */
public boolean authenticateCard(String userCode) throws Exception
```

### 2.7 Các Hàm Thông Tin Hội Viên

| Hàm | Mô tả | Tham số |
|-----|-------|---------|
| `setMemberInfo(name, birthDate, phone, address, avatarBytes)` | Ghi thông tin xuống thẻ | Các field thông tin |
| `setMemberInfo(name, birthDate, phone, address)` | Ghi thông tin (không avatar) | Overload |
| `getMemberInfo()` | Đọc thông tin từ thẻ | - |

#### Chi tiết `setMemberInfo()`:
```java
/**
 * Ghi thông tin người dùng xuống thẻ (kèm avatar):
 * - name → FIELD_NAME (encrypted by Master Key)
 * - birthDate → FIELD_DOB (encrypted)
 * - phone → FIELD_PHONE (encrypted)
 * - address → FIELD_ADDRESS (encrypted)
 * - avatarBytes → FIELD_AVATAR (encrypted)
 *
 * avatarBytes là byte[] đã nén sẵn ở UI (JPEG chất lượng thấp + resize).
 */
public boolean setMemberInfo(...) throws Exception
```

### 2.8 Các Hàm Gói Tập

| Hàm | Mô tả | Return |
|-----|-------|--------|
| `setPackage(type, expiry, registration, sessions, maxDuration)` | Set gói tập | boolean |
| `setPackage(type, expiry, registration, sessions)` | Set gói tập (không maxDuration) | boolean |
| `getPackage()` | Đọc gói tập từ thẻ | PackageInfo |

#### Format Lưu Trữ Gói Tập:
```
type|expiry|registration|sessions|maxDuration|usedMinutesToday
Ví dụ: "1|31/12/2026|01/01/2025|0|120|45"
```

### 2.9 Các Hàm Check-in / Check-out

| Hàm | Mô tả | Logic |
|-----|-------|-------|
| `checkIn(String date, String time)` | Check-in | Nếu ngày mới → tạo buổi mới, tăng count |
| `checkOut(String time)` | Check-out | Tính thời gian, cộng dồn, lưu history |
| `getCheckInCount()` | Lấy tổng số buổi | int |
| `getLastCheckIn()` | Lấy thông tin check-in hiện tại | CheckInInfo |
| `getCheckInHistory()` | Lấy lịch sử (max 10 entries) | List<CheckInLogEntry> |

#### Format Lưu Trữ Check-in:
```
currentState;historyEntry1;historyEntry2;...
currentState = date|inTime|outTime|count|isCheckedIn|totalMinutes
historyEntry = date|inTime|outTime|minutes
```

### 2.10 Các Hàm Số Dư

| Hàm | Mô tả | Return |
|-----|-------|--------|
| `getBalance()` | Đọc số dư từ thẻ (8 bytes, AES encrypted) | long (VND) |
| `addBalance(long amount)` | Nạp tiền | true nếu thành công |
| `deductBalance(long amount)` | Trừ tiền | true nếu đủ tiền |

#### Lưu trữ Số Dư:
- **Format**: 8 bytes big-endian (long)
- **Mã hóa**: AES-128 bởi Master Key trên thẻ
- **Giới hạn**: 0 → 999,999,999,999 VND

### 2.11 Các Hàm Dịch Vụ

| Hàm | Mô tả |
|-----|-------|
| `savePurchasedServices(List<String> services)` | Lưu danh sách dịch vụ đã mua lên thẻ |
| `loadPurchasedServices(List<String> targetList)` | Đọc danh sách dịch vụ từ thẻ |

### 2.12 Các Hàm Tiện Ích

| Hàm | Mô tả |
|-----|-------|
| `toUtf8AndLimit(String value, int maxBytes, String fieldName)` | Chuyển UTF-8 và kiểm tra giới hạn |
| `validateBirthDate(String birthDate)` | Validate ngày sinh (dd/MM/yyyy) |
| `validatePhone(String phone)` | Validate SĐT (0xxxxxxxxx) |
| `calculateSessionMinutes(String startTime, String endTime)` | Tính số phút giữa 2 mốc thời gian |
| `resetCardMockState()` | Reset trạng thái in-memory |
| `loadDemoDataLocal()` | Load dữ liệu demo (test) |

---

## 3. TẦNG CRYPTO - MÃ HÓA

### 3.1 AESUtils.java

**📁 Đường dẫn:** `src/gymcard/Crypto/AESUtils.java`  
**📊 Số dòng:** 30 dòng  
**🎯 Chức năng:** Utility class cho mã hóa AES-128 ECB

#### Các Hàm:

| Hàm | Mô tả | Tham số | Return |
|-----|-------|---------|--------|
| `generateAESKey()` | Sinh khóa AES 128-bit ngẫu nhiên | Không | byte[16] |
| `encryptAES(byte[] key, byte[] plain)` | Mã hóa dữ liệu bằng AES-ECB | key: khóa 16 bytes; plain: dữ liệu | byte[] ciphertext |
| `decryptAES(byte[] key, byte[] cipherData)` | Giải mã dữ liệu bằng AES-ECB | key: khóa 16 bytes; cipherData: mã hóa | byte[] plaintext |

#### Chi Tiết Kỹ Thuật:
```java
// Algorithm: AES/ECB/PKCS5Padding
// Key size: 128 bits (16 bytes)
// Padding: PKCS5

public static byte[] encryptAES(byte[] key, byte[] plain) throws Exception {
    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
    cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
    return cipher.doFinal(plain);
}
```

#### Sử Dụng Trong Dự Án:
- **Master Key**: Sinh bởi `generateAESKey()`, lưu trên thẻ
- **Personal Data**: Encrypt bằng Master Key
- **Balance**: Encrypt bằng Master Key (8 bytes long)

---

### 3.2 CryptoUtils.java

**📁 Đường dẫn:** `src/gymcard/Crypto/CryptoUtils.java`  
**📊 Số dòng:** 72 dòng  
**🎯 Chức năng:** Utility class nâng cao với AES-CBC và PBKDF2

#### Các Hàm:

| Hàm | Mô tả | Tham số | Return |
|-----|-------|---------|--------|
| `deriveKeyFromPin(char[] pin, byte[] salt)` | Sinh AES key từ PIN bằng PBKDF2 | pin: mã PIN; salt: 16 bytes | SecretKey |
| `generateIv()` | Sinh IV ngẫu nhiên 16 bytes | Không | byte[16] |
| `aesEncrypt(byte[] keyBytes, byte[] ivBytes, byte[] plaintext)` | Mã hóa AES-CBC | key, IV, plaintext | byte[] ciphertext |
| `aesDecrypt(byte[] keyBytes, byte[] ivBytes, byte[] ciphertext)` | Giải mã AES-CBC | key, IV, ciphertext | byte[] plaintext |
| `toBase64(byte[] data)` | Encode sang Base64 | data | String |
| `fromBase64(String s)` | Decode từ Base64 | s | byte[] |

#### Chi Tiết PBKDF2:
```java
/**
 * Sinh AES key từ PIN + salt bằng PBKDF2.
 * - Algorithm: PBKDF2WithHmacSHA256
 * - Iterations: 65536
 * - Key size: 128 bits
 */
public static SecretKey deriveKeyFromPin(char[] pin, byte[] salt) {
    PBEKeySpec spec = new PBEKeySpec(pin, salt, 65536, 128);
    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    byte[] keyBytes = factory.generateSecret(spec).getEncoded();
    return new SecretKeySpec(keyBytes, "AES");
}
```

#### Chi Tiết AES-CBC:
```java
// Algorithm: AES/CBC/PKCS5Padding
// Key size: 128 bits
// IV size: 128 bits (16 bytes)
// Padding: PKCS5

public static byte[] aesEncrypt(byte[] keyBytes, byte[] ivBytes, byte[] plaintext) {
    SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
    IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
    return cipher.doFinal(plaintext);
}
```

---

### 3.3 RSAUtils.java

**📁 Đường dẫn:** `src/gymcard/Crypto/RSAUtils.java`  
**📊 Số dòng:** 30 dòng  
**🎯 Chức năng:** Utility class cho mã hóa RSA

#### Các Hàm:

| Hàm | Mô tả | Tham số | Return |
|-----|-------|---------|--------|
| `generateKeyPair()` | Sinh cặp khóa RSA 2048-bit | Không | KeyPair |
| `rsaEncrypt(byte[] data, PublicKey publicKey)` | Mã hóa bằng public key | data, publicKey | byte[] ciphertext |
| `rsaDecrypt(byte[] data, PrivateKey privateKey)` | Giải mã bằng private key | data, privateKey | byte[] plaintext |

#### Chi Tiết Kỹ Thuật:
```java
// Algorithm: RSA/ECB/PKCS1Padding
// Key size: 2048 bits (client side)
// Key size: 1024 bits (JavaCard side - do giới hạn)

public static KeyPair generateKeyPair() throws Exception {
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(2048);
    return keyGen.generateKeyPair();
}
```

---

### 3.4 RSAKeyUtils.java

**📁 Đường dẫn:** `src/gymcard/Crypto/RSAKeyUtils.java`  
**📊 Số dòng:** 40 dòng  
**🎯 Chức năng:** Utility class cho export/import RSA keys

#### Các Hàm:

| Hàm | Mô tả | Tham số | Return |
|-----|-------|---------|--------|
| `exportModulus(RSAPublicKey pubKey)` | Export modulus từ public key | pubKey | byte[] modulus |
| `importFromModulus(byte[] modulusBytes)` | Import public key từ modulus | modulusBytes | RSAPublicKey |
| `encodeRSAPublicKey(RSAPublicKey pubKey)` | Encode full key (X.509) | pubKey | byte[] |
| `decodeRSAPublicKey(byte[] x509Bytes)` | Decode full key từ X.509 | x509Bytes | RSAPublicKey |

#### Chi Tiết `importFromModulus()`:
```java
/**
 * Tạo RSAPublicKey từ modulus bytes.
 * Exponent cố định = 65537 (0x10001) - tiêu chuẩn RSA.
 * 
 * Sử dụng khi đọc public key từ JavaCard (chỉ trả về modulus).
 */
public static RSAPublicKey importFromModulus(byte[] modulusBytes) throws Exception {
    BigInteger mod = new BigInteger(1, modulusBytes);
    BigInteger exp = BigInteger.valueOf(65537);
    RSAPublicKeySpec spec = new RSAPublicKeySpec(mod, exp);
    KeyFactory kf = KeyFactory.getInstance("RSA");
    return (RSAPublicKey) kf.generatePublic(spec);
}
```

#### Sử Dụng Trong Dự Án:
- **Export**: Khi đăng ký thẻ mới, export modulus từ thẻ lưu vào DB
- **Import**: Khi xác thực, import modulus từ DB thành RSAPublicKey để verify signature

---

### 3.5 AppKeyStore.java

**📁 Đường dẫn:** `src/gymcard/Crypto/AppKeyStore.java`  
**📊 Số dòng:** 35 dòng  
**🎯 Chức năng:** Quản lý RSA keypair của ứng dụng (lưu file)

#### Các Hàm:

| Hàm | Mô tả | Return |
|-----|-------|--------|
| `loadOrCreateAppKeyPair()` | Load keypair từ file hoặc tạo mới | KeyPair |

#### Chi Tiết:
```java
private static final String KEY_FILE = "app_rsa.key";

/**
 * Load hoặc tạo mới RSA keypair cho ứng dụng.
 * 
 * - Nếu file tồn tại: decode và trả về
 * - Nếu không: sinh mới RSA 2048-bit, lưu file
 * 
 * Format file: publicKeyBase64|privateKeyBase64
 */
public static KeyPair loadOrCreateAppKeyPair() throws Exception {
    if (Files.exists(Paths.get(KEY_FILE))) {
        // Load existing
        String content = new String(Files.readAllBytes(Paths.get(KEY_FILE)));
        String[] parts = content.split("\\|");
        // Decode public and private keys...
    } else {
        // Create new
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        // Save to file...
        return kp;
    }
}
```

---

## 4. LUỒNG BẢO MẬT TỔNG QUAN

### 4.1 Kiến Trúc Master Key

```
┌─────────────────────────────────────────────────────────────────┐
│                        THẺ JAVACARD                             │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  PIN (6 số)          Master Key (AES-128)     RSA Private │  │
│  │       │                    │                       │       │  │
│  │       ▼                    ▼                       ▼       │  │
│  │   ┌────────┐          ┌────────┐             ┌────────┐   │  │
│  │   │OwnerPIN│          │ Encrypt│             │Sign    │   │  │
│  │   │verify()│          │Personal│             │Challeng│   │  │
│  │   └────────┘          │  Data  │             │   e    │   │  │
│  │       │                └────────┘             └────────┘   │  │
│  │       │                    │                       │       │  │
│  │       ▼                    ▼                       ▼       │  │
│  │   Authenticated        Encrypted               Signature  │  │
│  │   (access data)        Fields                  (128 bytes)│  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### 4.2 Luồng Xác Thực Đăng Nhập

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  User nhập  │     │   Client    │     │   JavaCard  │
│     PIN     │────▶│ verifyPin() │────▶│VerifyPIN    │
└─────────────┘     └─────────────┘     └─────────────┘
                           │                    │
                           │◀──────────────────│
                           │    PIN OK/FAIL     │
                           │                    │
                    ┌──────▼──────┐             │
                    │ PIN OK?     │             │
                    └──────┬──────┘             │
                           │ Yes               │
                    ┌──────▼──────┐             │
                    │ Read CardID │◀───────────│
                    │ from Card   │             │
                    └──────┬──────┘             │
                           │                    │
                    ┌──────▼──────┐             │
                    │ Get PubKey  │             │
                    │ from DB     │             │
                    └──────┬──────┘             │
                           │                    │
                    ┌──────▼──────┐             │
                    │ Generate    │             │
                    │ Challenge   │             │
                    │ (32 bytes)  │             │
                    └──────┬──────┘             │
                           │                    │
                    ┌──────▼──────┐     ┌──────▼──────┐
                    │ Send to Card│────▶│ Sign with   │
                    │             │     │ RSA Private │
                    └─────────────┘     └──────┬──────┘
                           │                    │
                    ┌──────▼──────┐◀───────────│
                    │ Verify Sig  │   Signature │
                    │ with PubKey │             │
                    └──────┬──────┘             │
                           │                    │
                    ┌──────▼──────┐             │
                    │ Sig Valid?  │             │
                    └──────┬──────┘             │
                           │                    │
              ┌────────────┼────────────┐       │
              ▼            ▼            ▼       │
         ┌────────┐   ┌────────┐   ┌────────┐  │
         │  OK    │   │ SKIPPED│   │ FAIL   │  │
         │(login) │   │(new)   │   │(clone?)│  │
         └────────┘   └────────┘   └────────┘  │
```

### 4.3 Luồng Mã Hóa Dữ Liệu Cá Nhân

```
┌──────────────────────────────────────────────────────────────┐
│                    CLIENT SIDE                                │
│                                                               │
│  ┌─────────┐    ┌───────────────┐    ┌──────────────────┐   │
│  │ User    │    │ CardComm      │    │  APDU Command    │   │
│  │ Input   │───▶│ setMemberInfo │───▶│  WRITE_FIELD     │   │
│  │ (UTF-8) │    │ ()            │    │  (plaintext)     │   │
│  └─────────┘    └───────────────┘    └────────┬─────────┘   │
└───────────────────────────────────────────────│──────────────┘
                                                │
                                                ▼
┌──────────────────────────────────────────────────────────────┐
│                    JAVACARD APPLET                            │
│                                                               │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────────┐  │
│  │ Receive     │    │ Get Master  │    │ AES Encrypt     │  │
│  │ Plaintext   │───▶│ Key (from   │───▶│ (data +         │  │
│  │ Data        │    │ PIN decrypt)│    │  Master Key)    │  │
│  └─────────────┘    └─────────────┘    └────────┬────────┘  │
│                                                  │           │
│                                         ┌────────▼────────┐  │
│                                         │ Store Encrypted │  │
│                                         │ in EEPROM       │  │
│                                         └─────────────────┘  │
└──────────────────────────────────────────────────────────────┘
```

### 4.4 Bảng Tổng Hợp Thuật Toán

| Thành phần | Thuật toán | Key Size | Mục đích |
|------------|------------|----------|----------|
| PIN Encryption | Derived | 6 digits | Bảo vệ Master Key |
| Master Key | AES-128 | 128 bits | Mã hóa dữ liệu cá nhân |
| Personal Data | AES-ECB | 128 bits | Bảo mật thông tin |
| Card Auth | RSA-1024 | 1024 bits | Chống clone thẻ |
| App Keys | RSA-2048 | 2048 bits | Dự phòng client |
| Key Derivation | PBKDF2 | - | Sinh key từ PIN |

---

## PHỤ LỤC: TỔNG HỢP TẤT CẢ HÀM

### A. CardCommunicator.java (1280 dòng)

| # | Hàm | Loại | Mô tả ngắn |
|---|-----|------|------------|
| 1 | `connect()` | Kết nối | Kết nối thẻ |
| 2 | `disconnect()` | Kết nối | Ngắt kết nối |
| 3 | `isConnected()` | Kết nối | Kiểm tra kết nối |
| 4 | `initNewCard()` | Khởi tạo | Khởi tạo thẻ mới |
| 5 | `verifyPin()` | PIN | Xác thực PIN |
| 6 | `verifyPinWithCardAuth()` | PIN+RSA | Xác thực đầy đủ |
| 7 | `changePin()` | PIN | Đổi PIN (user) |
| 8 | `checkDefaultPin()` | PIN | Kiểm tra PIN mặc định |
| 9 | `unlockPin()` | PIN | Mở khóa thẻ |
| 10 | `getPinTries()` | PIN | Số lần thử còn lại |
| 11 | `adminResetMemberPin()` | PIN | Admin đổi PIN |
| 12 | `getCardPublicKey()` | RSA | Lấy public key |
| 13 | `getCardPublicKeyModulus()` | RSA | Lấy modulus |
| 14 | `saveCardPublicKeyToDb()` | RSA | Lưu key vào DB |
| 15 | `authenticateCard()` | RSA | Challenge-response |
| 16 | `setMemberInfo()` | Data | Ghi thông tin hội viên |
| 17 | `getMemberInfo()` | Data | Đọc thông tin hội viên |
| 18 | `setPackage()` | Data | Set gói tập |
| 19 | `getPackage()` | Data | Đọc gói tập |
| 20 | `checkIn()` | Check-in | Check-in |
| 21 | `checkOut()` | Check-in | Check-out |
| 22 | `getCheckInCount()` | Check-in | Số buổi tập |
| 23 | `getLastCheckIn()` | Check-in | Thông tin check-in |
| 24 | `getCheckInHistory()` | Check-in | Lịch sử check-in |
| 25 | `getBalance()` | Tài chính | Đọc số dư |
| 26 | `addBalance()` | Tài chính | Nạp tiền |
| 27 | `deductBalance()` | Tài chính | Trừ tiền |
| 28 | `savePurchasedServices()` | Dịch vụ | Lưu dịch vụ đã mua |
| 29 | `loadPurchasedServices()` | Dịch vụ | Đọc dịch vụ đã mua |
| 30 | `getTransaction()` | Log | Lấy giao dịch |

### B. Crypto Layer

| File | Hàm | Mô tả |
|------|-----|-------|
| AESUtils | `generateAESKey()` | Sinh AES-128 key |
| AESUtils | `encryptAES()` | Mã hóa AES-ECB |
| AESUtils | `decryptAES()` | Giải mã AES-ECB |
| CryptoUtils | `deriveKeyFromPin()` | PBKDF2 key derivation |
| CryptoUtils | `generateIv()` | Sinh IV 16 bytes |
| CryptoUtils | `aesEncrypt()` | Mã hóa AES-CBC |
| CryptoUtils | `aesDecrypt()` | Giải mã AES-CBC |
| CryptoUtils | `toBase64()` | Encode Base64 |
| CryptoUtils | `fromBase64()` | Decode Base64 |
| RSAUtils | `generateKeyPair()` | Sinh RSA 2048 keypair |
| RSAUtils | `rsaEncrypt()` | Mã hóa RSA |
| RSAUtils | `rsaDecrypt()` | Giải mã RSA |
| RSAKeyUtils | `exportModulus()` | Export modulus |
| RSAKeyUtils | `importFromModulus()` | Import từ modulus |
| RSAKeyUtils | `encodeRSAPublicKey()` | Encode X.509 |
| RSAKeyUtils | `decodeRSAPublicKey()` | Decode X.509 |
| AppKeyStore | `loadOrCreateAppKeyPair()` | Load/tạo keypair app |

---

*Tài liệu được tạo bởi GitHub Copilot*  
*Ngày tạo: Tháng 1/2025*
