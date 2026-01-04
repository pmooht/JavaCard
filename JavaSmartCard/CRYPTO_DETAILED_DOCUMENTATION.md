# TÀI LIỆU CHI TIẾT VỀ MÃ HÓA (CRYPTO LAYER)
## Dự Án JavaCard Gym Membership System

---

## MỤC LỤC

1. [Tổng Quan Kiến Trúc Mã Hóa](#1-tổng-quan-kiến-trúc-mã-hóa)
2. [AESUtils.java - Mã Hóa Đối Xứng Cơ Bản](#2-aesutilsjava---mã-hóa-đối-xứng-cơ-bản)
3. [CryptoUtils.java - Mã Hóa Nâng Cao](#3-cryptoutilsjava---mã-hóa-nâng-cao)
4. [RSAUtils.java - Mã Hóa Bất Đối Xứng](#4-rsautilsjava---mã-hóa-bất-đối-xứng)
5. [RSAKeyUtils.java - Quản Lý Khóa RSA](#5-rsakeyutilsjava---quản-lý-khóa-rsa)
6. [AppKeyStore.java - Lưu Trữ Khóa Ứng Dụng](#6-appkeystorejava---lưu-trữ-khóa-ứng-dụng)
7. [So Sánh Các Thuật Toán](#7-so-sánh-các-thuật-toán)
8. [Ứng Dụng Trong Dự Án](#8-ứng-dụng-trong-dự-án)

---

## 1. TỔNG QUAN KIẾN TRÚC MÃ HÓA

### 1.1 Sơ Đồ Các Lớp Crypto

```
┌─────────────────────────────────────────────────────────────────┐
│                      CRYPTO LAYER                               │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    MÃ HÓA ĐỐI XỨNG                       │   │
│  │  ┌─────────────────┐    ┌─────────────────────────────┐ │   │
│  │  │   AESUtils.java │    │    CryptoUtils.java         │ │   │
│  │  │   (AES-ECB)     │    │    (AES-CBC + PBKDF2)       │ │   │
│  │  │   - Cơ bản      │    │    - Nâng cao               │ │   │
│  │  │   - Nhanh       │    │    - An toàn hơn            │ │   │
│  │  └─────────────────┘    └─────────────────────────────┘ │   │
│  └─────────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                   MÃ HÓA BẤT ĐỐI XỨNG                    │   │
│  │  ┌─────────────────┐    ┌─────────────────────────────┐ │   │
│  │  │   RSAUtils.java │    │    RSAKeyUtils.java         │ │   │
│  │  │   (RSA-2048)    │    │    (Import/Export Keys)     │ │   │
│  │  │   - Encrypt     │    │    - Modulus handling       │ │   │
│  │  │   - Decrypt     │    │    - X.509 encoding         │ │   │
│  │  └─────────────────┘    └─────────────────────────────┘ │   │
│  └─────────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    LƯU TRỮ KHÓA                          │   │
│  │  ┌───────────────────────────────────────────────────┐  │   │
│  │  │              AppKeyStore.java                      │  │   │
│  │  │              (Lưu RSA KeyPair vào file)            │  │   │
│  │  └───────────────────────────────────────────────────┘  │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 Các Thuật Toán Sử Dụng

| Thuật toán | Loại | Key Size | Mục đích trong dự án |
|------------|------|----------|----------------------|
| **AES-128-ECB** | Đối xứng | 128 bits | Master Key mã hóa dữ liệu |
| **AES-128-CBC** | Đối xứng | 128 bits | Mã hóa nâng cao (có IV) |
| **PBKDF2-HMAC-SHA256** | Key Derivation | - | Sinh key từ PIN |
| **RSA-2048** | Bất đối xứng | 2048 bits | Client keypair |
| **RSA-1024** | Bất đối xứng | 1024 bits | JavaCard keypair |

---

## 2. AESUtils.java - MÃ HÓA ĐỐI XỨNG CƠ BẢN

### 2.1 Thông Tin File

| Thuộc tính | Giá trị |
|------------|---------|
| **Đường dẫn** | `src/gymcard/Crypto/AESUtils.java` |
| **Số dòng** | 32 dòng |
| **Package** | `gymcard.Crypto` |
| **Imports** | `java.security.SecureRandom`, `javax.crypto.Cipher`, `javax.crypto.spec.SecretKeySpec` |

### 2.2 Mục Đích

Class utility cung cấp các hàm mã hóa/giải mã AES-128 đơn giản sử dụng chế độ ECB. Được dùng cho các tác vụ mã hóa cơ bản khi không cần IV.

### 2.3 Chi Tiết Từng Hàm

---

#### 🔹 Hàm `generateAESKey()`

```java
public static byte[] generateAESKey() {
    byte[] key = new byte[16];
    new SecureRandom().nextBytes(key);
    return key;
}
```

| Thuộc tính | Mô tả |
|------------|-------|
| **Mục đích** | Sinh khóa AES-128 ngẫu nhiên |
| **Tham số** | Không có |
| **Return** | `byte[16]` - Khóa AES 128-bit |
| **Exception** | Không throw |

**Chi tiết hoạt động:**

1. Tạo mảng byte có độ dài 16 (128 bits)
2. Sử dụng `SecureRandom` để sinh số ngẫu nhiên an toàn về mật mã
3. Điền các byte ngẫu nhiên vào mảng
4. Trả về mảng làm khóa AES

**Ví dụ sử dụng:**
```java
byte[] masterKey = AESUtils.generateAESKey();
// masterKey = [0x4A, 0x7B, 0x2C, ... 16 bytes ngẫu nhiên]
```

**Biến quan trọng:**

| Biến | Kiểu | Giá trị | Ý nghĩa |
|------|------|---------|---------|
| `key` | `byte[]` | `new byte[16]` | Mảng chứa khóa AES |
| `16` | `int` | 16 | Độ dài khóa = 128 bits |

---

#### 🔹 Hàm `encryptAES()`

```java
public static byte[] encryptAES(byte[] key, byte[] plain) throws Exception {
    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
    cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
    return cipher.doFinal(plain);
}
```

| Thuộc tính | Mô tả |
|------------|-------|
| **Mục đích** | Mã hóa dữ liệu bằng AES-ECB |
| **Tham số** | `key`: Khóa 16 bytes; `plain`: Dữ liệu plaintext |
| **Return** | `byte[]` - Dữ liệu đã mã hóa |
| **Exception** | `Exception` nếu key không hợp lệ hoặc lỗi mã hóa |

**Chi tiết hoạt động:**

1. Lấy instance Cipher với thuật toán "AES/ECB/PKCS5Padding"
2. Khởi tạo Cipher ở chế độ ENCRYPT với khóa đã cho
3. Thực hiện mã hóa và trả về ciphertext

**Giải thích transformation string:**
```
"AES/ECB/PKCS5Padding"
  │    │       │
  │    │       └── Padding: Thêm bytes để dữ liệu chia hết cho block size
  │    └────────── Mode: Electronic Codebook (không dùng IV)
  └─────────────── Algorithm: Advanced Encryption Standard
```

**Biến quan trọng:**

| Biến | Kiểu | Ý nghĩa |
|------|------|---------|
| `cipher` | `Cipher` | Đối tượng thực hiện mã hóa |
| `Cipher.ENCRYPT_MODE` | `int` | Hằng số = 1, chế độ mã hóa |
| `SecretKeySpec` | `class` | Wrapper cho key bytes thành Key object |

**Ví dụ sử dụng:**
```java
byte[] key = AESUtils.generateAESKey();
byte[] plaintext = "Hello World".getBytes("UTF-8");
byte[] ciphertext = AESUtils.encryptAES(key, plaintext);
// ciphertext = [0xA3, 0x5F, ... ] (16 bytes do padding)
```

---

#### 🔹 Hàm `decryptAES()`

```java
public static byte[] decryptAES(byte[] key, byte[] cipherData) throws Exception {
    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
    cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"));
    return cipher.doFinal(cipherData);
}
```

| Thuộc tính | Mô tả |
|------------|-------|
| **Mục đích** | Giải mã dữ liệu AES-ECB |
| **Tham số** | `key`: Khóa 16 bytes; `cipherData`: Dữ liệu đã mã hóa |
| **Return** | `byte[]` - Dữ liệu gốc (plaintext) |
| **Exception** | `Exception` nếu key sai hoặc dữ liệu bị corrupt |

**Chi tiết hoạt động:**

1. Lấy instance Cipher với cùng thuật toán
2. Khởi tạo ở chế độ DECRYPT
3. Giải mã và loại bỏ padding, trả về plaintext

**Biến quan trọng:**

| Biến | Kiểu | Ý nghĩa |
|------|------|---------|
| `Cipher.DECRYPT_MODE` | `int` | Hằng số = 2, chế độ giải mã |

**Ví dụ sử dụng:**
```java
byte[] decrypted = AESUtils.decryptAES(key, ciphertext);
String original = new String(decrypted, "UTF-8");
// original = "Hello World"
```

---

### 2.4 Lưu Ý Bảo Mật

⚠️ **ECB Mode Weakness:**
- Chế độ ECB không sử dụng IV (Initialization Vector)
- Cùng plaintext + cùng key → cùng ciphertext
- Có thể bị tấn công pattern analysis
- **Nên dùng**: Cho dữ liệu nhỏ, random, không lặp lại

✅ **Khi nào dùng AESUtils:**
- Mã hóa Master Key (16 bytes ngẫu nhiên)
- Mã hóa dữ liệu nhỏ trên JavaCard (ECB đơn giản hơn cho embedded)

---

## 3. CryptoUtils.java - MÃ HÓA NÂNG CAO

### 3.1 Thông Tin File

| Thuộc tính | Giá trị |
|------------|---------|
| **Đường dẫn** | `src/gymcard/Crypto/CryptoUtils.java` |
| **Số dòng** | 75 dòng |
| **Package** | `gymcard.Crypto` |

### 3.2 Các Hằng Số Quan Trọng

```java
private static final String AES_TRANSFORMATION = "AES/CBC/PKCS5Padding";
private static final int AES_KEY_BITS = 128;
private static final int IV_LEN = 16;
private static final SecureRandom secureRandom = new SecureRandom();
```

| Hằng số | Giá trị | Ý nghĩa |
|---------|---------|---------|
| `AES_TRANSFORMATION` | `"AES/CBC/PKCS5Padding"` | Thuật toán AES với CBC mode |
| `AES_KEY_BITS` | `128` | Độ dài khóa AES (bits) |
| `IV_LEN` | `16` | Độ dài IV = 128 bits |
| `secureRandom` | `SecureRandom` | Generator số ngẫu nhiên dùng chung |

### 3.3 Chi Tiết Từng Hàm

---

#### 🔹 Hàm `deriveKeyFromPin()`

```java
public static SecretKey deriveKeyFromPin(char[] pin, byte[] salt) 
        throws GeneralSecurityException {
    PBEKeySpec spec = new PBEKeySpec(pin, salt, 65536, AES_KEY_BITS);
    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    byte[] keyBytes = factory.generateSecret(spec).getEncoded();
    return new SecretKeySpec(keyBytes, "AES");
}
```

| Thuộc tính | Mô tả |
|------------|-------|
| **Mục đích** | Sinh khóa AES từ PIN sử dụng PBKDF2 |
| **Tham số** | `pin`: Mã PIN dạng char[]; `salt`: Muối 16+ bytes |
| **Return** | `SecretKey` - Khóa AES có thể dùng với Cipher |
| **Exception** | `GeneralSecurityException` |

**Chi tiết thuật toán PBKDF2:**

```
PBKDF2 (Password-Based Key Derivation Function 2)
─────────────────────────────────────────────────

PIN: "123456"  +  Salt: [random 16 bytes]
        │                    │
        └────────┬───────────┘
                 │
                 ▼
    ┌────────────────────────┐
    │  HMAC-SHA256           │ ← 65,536 iterations
    │  (lặp 65,536 lần)      │
    └────────────────────────┘
                 │
                 ▼
        AES Key (16 bytes)
```

**Biến quan trọng:**

| Biến | Kiểu | Giá trị | Ý nghĩa |
|------|------|---------|---------|
| `PBEKeySpec` | `class` | - | Specification cho password-based encryption |
| `65536` | `int` | 65536 | Số lần lặp (iterations) - chống brute-force |
| `AES_KEY_BITS` | `int` | 128 | Độ dài output key |
| `"PBKDF2WithHmacSHA256"` | `String` | - | Thuật toán PBKDF2 với HMAC-SHA256 |

**Tại sao dùng 65,536 iterations?**
- Mỗi lần thử PIN phải tính 65,536 lần HMAC
- Làm chậm brute-force attack
- 6 chữ số = 1,000,000 khả năng × 65,536 = rất lâu

**Ví dụ sử dụng:**
```java
char[] pin = "123456".toCharArray();
byte[] salt = new byte[16];
new SecureRandom().nextBytes(salt);

SecretKey key = CryptoUtils.deriveKeyFromPin(pin, salt);
// key có thể dùng để mã hóa/giải mã
```

---

#### 🔹 Hàm `generateIv()`

```java
public static byte[] generateIv() {
    byte[] iv = new byte[IV_LEN];
    secureRandom.nextBytes(iv);
    return iv;
}
```

| Thuộc tính | Mô tả |
|------------|-------|
| **Mục đích** | Sinh Initialization Vector ngẫu nhiên |
| **Tham số** | Không có |
| **Return** | `byte[16]` - IV 128-bit |

**IV là gì và tại sao cần?**

```
Không có IV (ECB mode):
  Block 1: "Hello" → Encrypt → [A1 B2 C3...]
  Block 2: "Hello" → Encrypt → [A1 B2 C3...] ← GIỐNG NHAU!

Có IV (CBC mode):
  Block 1: "Hello" XOR IV1 → Encrypt → [A1 B2 C3...]
  Block 2: "Hello" XOR [A1 B2...] → Encrypt → [D4 E5 F6...] ← KHÁC!
```

---

#### 🔹 Hàm `aesEncrypt()` (CBC mode)

```java
public static byte[] aesEncrypt(byte[] keyBytes, byte[] ivBytes, byte[] plaintext)
        throws GeneralSecurityException {

    SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
    IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

    Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
    cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

    return cipher.doFinal(plaintext);
}
```

| Thuộc tính | Mô tả |
|------------|-------|
| **Mục đích** | Mã hóa AES-CBC với IV |
| **Tham số** | `keyBytes`: Khóa 16 bytes; `ivBytes`: IV 16 bytes; `plaintext`: Dữ liệu |
| **Return** | `byte[]` - Ciphertext |

**So sánh với AESUtils.encryptAES():**

| Tiêu chí | AESUtils (ECB) | CryptoUtils (CBC) |
|----------|----------------|-------------------|
| IV | Không cần | Bắt buộc |
| An toàn | Thấp hơn | Cao hơn |
| Phức tạp | Đơn giản | Phức tạp hơn |
| Dùng cho | Dữ liệu nhỏ, random | Dữ liệu lớn, có pattern |

**Biến quan trọng:**

| Biến | Kiểu | Ý nghĩa |
|------|------|---------|
| `keySpec` | `SecretKeySpec` | Wrapper cho key bytes |
| `ivSpec` | `IvParameterSpec` | Wrapper cho IV bytes |

---

#### 🔹 Hàm `aesDecrypt()` (CBC mode)

```java
public static byte[] aesDecrypt(byte[] keyBytes, byte[] ivBytes, byte[] ciphertext)
        throws GeneralSecurityException {

    SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
    IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

    Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
    cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

    return cipher.doFinal(ciphertext);
}
```

| Thuộc tính | Mô tả |
|------------|-------|
| **Mục đích** | Giải mã AES-CBC |
| **Tham số** | `keyBytes`, `ivBytes`, `ciphertext` |
| **Return** | `byte[]` - Plaintext |

**Lưu ý:** Phải dùng đúng IV đã dùng khi encrypt!

---

#### 🔹 Hàm `toBase64()` và `fromBase64()`

```java
public static String toBase64(byte[] data) {
    return Base64.getEncoder().encodeToString(data);
}

public static byte[] fromBase64(String s) {
    return Base64.getDecoder().decode(s);
}
```

| Hàm | Mục đích | Input | Output |
|-----|----------|-------|--------|
| `toBase64()` | Encode bytes → String | `byte[]` | `String` |
| `fromBase64()` | Decode String → bytes | `String` | `byte[]` |

**Ví dụ:**
```java
byte[] data = {0x48, 0x65, 0x6C, 0x6C, 0x6F}; // "Hello"
String b64 = CryptoUtils.toBase64(data);
// b64 = "SGVsbG8="

byte[] back = CryptoUtils.fromBase64(b64);
// back = {0x48, 0x65, 0x6C, 0x6C, 0x6F}
```

---

## 4. RSAUtils.java - MÃ HÓA BẤT ĐỐI XỨNG

### 4.1 Thông Tin File

| Thuộc tính | Giá trị |
|------------|---------|
| **Đường dẫn** | `src/gymcard/Crypto/RSAUtils.java` |
| **Số dòng** | 35 dòng |
| **Package** | `gymcard.Crypto` |

### 4.2 Giới Thiệu RSA

```
RSA (Rivest–Shamir–Adleman)
───────────────────────────

┌─────────────┐                    ┌─────────────┐
│ Public Key  │                    │ Private Key │
│ (Công khai) │                    │ (Bí mật)    │
└──────┬──────┘                    └──────┬──────┘
       │                                  │
       ▼                                  ▼
  Dùng để:                           Dùng để:
  • Mã hóa                           • Giải mã
  • Verify signature                 • Ký (Sign)
```

### 4.3 Chi Tiết Từng Hàm

---

#### 🔹 Hàm `generateKeyPair()`

```java
public static KeyPair generateKeyPair() throws Exception {
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(2048);
    return keyGen.generateKeyPair();
}
```

| Thuộc tính | Mô tả |
|------------|-------|
| **Mục đích** | Sinh cặp khóa RSA 2048-bit |
| **Tham số** | Không có |
| **Return** | `KeyPair` chứa PublicKey và PrivateKey |
| **Exception** | `Exception` nếu RSA không được hỗ trợ |

**Biến quan trọng:**

| Biến | Kiểu | Giá trị | Ý nghĩa |
|------|------|---------|---------|
| `keyGen` | `KeyPairGenerator` | - | Factory sinh keypair |
| `2048` | `int` | 2048 | Độ dài key (bits) |

**Thời gian sinh key:**
- RSA-1024: ~100ms
- RSA-2048: ~500ms - 1s
- RSA-4096: ~3-5s

**Ví dụ sử dụng:**
```java
KeyPair kp = RSAUtils.generateKeyPair();
PublicKey publicKey = kp.getPublic();
PrivateKey privateKey = kp.getPrivate();
```

---

#### 🔹 Hàm `rsaEncrypt()`

```java
public static byte[] rsaEncrypt(byte[] data, PublicKey publicKey) throws Exception {
    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    cipher.init(Cipher.ENCRYPT_MODE, publicKey);
    return cipher.doFinal(data);
}
```

| Thuộc tính | Mô tả |
|------------|-------|
| **Mục đích** | Mã hóa dữ liệu bằng RSA public key |
| **Tham số** | `data`: Dữ liệu (max ~245 bytes cho RSA-2048); `publicKey`: Khóa công khai |
| **Return** | `byte[]` - Ciphertext (256 bytes cho RSA-2048) |

**Giải thích transformation:**
```
"RSA/ECB/PKCS1Padding"
  │    │       │
  │    │       └── Padding: PKCS#1 v1.5 (thêm random bytes)
  │    └────────── Mode: ECB (không áp dụng cho RSA)
  └─────────────── Algorithm: RSA
```

**Giới hạn kích thước dữ liệu:**
```
Max plaintext size = Key size (bytes) - Padding overhead
                   = 2048/8 - 11
                   = 256 - 11
                   = 245 bytes
```

**Ví dụ sử dụng:**
```java
byte[] secret = "My Secret Data".getBytes();
byte[] encrypted = RSAUtils.rsaEncrypt(secret, publicKey);
// encrypted.length = 256 bytes (cho RSA-2048)
```

---

#### 🔹 Hàm `rsaDecrypt()`

```java
public static byte[] rsaDecrypt(byte[] data, PrivateKey privateKey) throws Exception {
    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    cipher.init(Cipher.DECRYPT_MODE, privateKey);
    return cipher.doFinal(data);
}
```

| Thuộc tính | Mô tả |
|------------|-------|
| **Mục đích** | Giải mã dữ liệu bằng RSA private key |
| **Tham số** | `data`: Ciphertext 256 bytes; `privateKey`: Khóa bí mật |
| **Return** | `byte[]` - Plaintext gốc |

**Ví dụ sử dụng:**
```java
byte[] decrypted = RSAUtils.rsaDecrypt(encrypted, privateKey);
String original = new String(decrypted);
// original = "My Secret Data"
```

---

## 5. RSAKeyUtils.java - QUẢN LÝ KHÓA RSA

### 5.1 Thông Tin File

| Thuộc tính | Giá trị |
|------------|---------|
| **Đường dẫn** | `src/gymcard/Crypto/RSAKeyUtils.java` |
| **Số dòng** | 40 dòng |
| **Package** | `gymcard.Crypto` |

### 5.2 Mục Đích

Cung cấp các hàm tiện ích để:
- Export/Import RSA public key
- Xử lý modulus (thành phần chính của RSA key)
- Chuyển đổi giữa các format key

### 5.3 Cấu Trúc RSA Public Key

```
RSA Public Key gồm 2 thành phần:
─────────────────────────────────

┌─────────────────────────────────────────────────────────┐
│                   RSA Public Key                         │
│  ┌─────────────────────────┐  ┌──────────────────────┐  │
│  │       Modulus (n)       │  │    Exponent (e)      │  │
│  │     (128 bytes cho      │  │   (thường = 65537)   │  │
│  │       RSA-1024)         │  │   = 0x10001          │  │
│  └─────────────────────────┘  └──────────────────────┘  │
└─────────────────────────────────────────────────────────┘

Công thức mã hóa: ciphertext = plaintext^e mod n
```

### 5.4 Chi Tiết Từng Hàm

---

#### 🔹 Hàm `exportModulus()`

```java
public static byte[] exportModulus(RSAPublicKey pubKey) {
    return pubKey.getModulus().toByteArray();
}
```

| Thuộc tính | Mô tả |
|------------|-------|
| **Mục đích** | Trích xuất modulus từ RSA public key |
| **Tham số** | `pubKey`: RSA public key |
| **Return** | `byte[]` - Modulus bytes (128 bytes cho RSA-1024) |

**Tại sao chỉ export modulus?**
- Exponent (e) thường cố định = 65537
- Chỉ cần lưu modulus là đủ để reconstruct key
- Tiết kiệm không gian lưu trữ

**Ví dụ sử dụng:**
```java
RSAPublicKey pubKey = (RSAPublicKey) keyPair.getPublic();
byte[] modulus = RSAKeyUtils.exportModulus(pubKey);
// Lưu modulus vào database
database.savePublicKey(userId, modulus);
```

---

#### 🔹 Hàm `importFromModulus()`

```java
public static RSAPublicKey importFromModulus(byte[] modulusBytes) throws Exception {
    java.math.BigInteger mod = new java.math.BigInteger(1, modulusBytes);
    java.math.BigInteger exp = java.math.BigInteger.valueOf(65537);
    RSAPublicKeySpec spec = new RSAPublicKeySpec(mod, exp);
    KeyFactory kf = KeyFactory.getInstance("RSA");
    return (RSAPublicKey) kf.generatePublic(spec);
}
```

| Thuộc tính | Mô tả |
|------------|-------|
| **Mục đích** | Tạo RSA public key từ modulus bytes |
| **Tham số** | `modulusBytes`: Modulus đã lưu |
| **Return** | `RSAPublicKey` - Key có thể dùng để verify/encrypt |

**Chi tiết hoạt động:**

```
modulusBytes (từ DB/thẻ)
        │
        ▼
┌───────────────────────┐
│ BigInteger(1, bytes)  │  ← Số 1 = positive number
└───────────────────────┘
        │
        ▼
┌───────────────────────┐
│ RSAPublicKeySpec      │  ← Kết hợp modulus + exponent
│ (modulus, 65537)      │
└───────────────────────┘
        │
        ▼
┌───────────────────────┐
│ KeyFactory.getInstance│
│ ("RSA")               │
└───────────────────────┘
        │
        ▼
    RSAPublicKey
```

**Biến quan trọng:**

| Biến | Kiểu | Giá trị | Ý nghĩa |
|------|------|---------|---------|
| `mod` | `BigInteger` | - | Modulus dạng số nguyên lớn |
| `exp` | `BigInteger` | 65537 | Public exponent chuẩn |
| `spec` | `RSAPublicKeySpec` | - | Specification cho public key |
| `kf` | `KeyFactory` | - | Factory tạo key từ spec |

**Tại sao exponent = 65537?**
- Là số nguyên tố Fermat: 2^16 + 1
- Có ít bit 1 → tính toán nhanh
- Được chọn làm chuẩn công nghiệp

---

#### 🔹 Hàm `encodeRSAPublicKey()`

```java
public static byte[] encodeRSAPublicKey(RSAPublicKey pubKey) {
    return pubKey.getEncoded();
}
```

| Thuộc tính | Mô tả |
|------------|-------|
| **Mục đích** | Encode full public key theo chuẩn X.509 |
| **Tham số** | `pubKey`: RSA public key |
| **Return** | `byte[]` - X.509 encoded key |

**So sánh với exportModulus():**

| Tiêu chí | exportModulus() | encodeRSAPublicKey() |
|----------|-----------------|----------------------|
| Kích thước | ~128 bytes (RSA-1024) | ~162 bytes |
| Chứa gì | Chỉ modulus | Modulus + Exponent + Header |
| Format | Raw bytes | X.509/DER |
| Dùng khi | Lưu DB, gửi qua network | Trao đổi chuẩn |

---

#### 🔹 Hàm `decodeRSAPublicKey()`

```java
public static RSAPublicKey decodeRSAPublicKey(byte[] x509Bytes) throws Exception {
    X509EncodedKeySpec spec = new X509EncodedKeySpec(x509Bytes);
    KeyFactory kf = KeyFactory.getInstance("RSA");
    return (RSAPublicKey) kf.generatePublic(spec);
}
```

| Thuộc tính | Mô tả |
|------------|-------|
| **Mục đích** | Decode X.509 bytes thành RSA public key |
| **Tham số** | `x509Bytes`: Bytes đã encode bằng encodeRSAPublicKey() |
| **Return** | `RSAPublicKey` |

---

## 6. AppKeyStore.java - LƯU TRỮ KHÓA ỨNG DỤNG

### 6.1 Thông Tin File

| Thuộc tính | Giá trị |
|------------|---------|
| **Đường dẫn** | `src/gymcard/Crypto/AppKeyStore.java` |
| **Số dòng** | 38 dòng |
| **Package** | `gymcard.Crypto` |

### 6.2 Hằng Số

```java
private static final String KEY_FILE = "app_rsa.key";
```

| Hằng số | Giá trị | Ý nghĩa |
|---------|---------|---------|
| `KEY_FILE` | `"app_rsa.key"` | Tên file lưu keypair |

### 6.3 Chi Tiết Hàm `loadOrCreateAppKeyPair()`

```java
public static KeyPair loadOrCreateAppKeyPair() throws Exception {
    if (Files.exists(Paths.get(KEY_FILE))) {
        // Load existing keypair
        String content = new String(Files.readAllBytes(Paths.get(KEY_FILE)), "UTF-8");
        String[] parts = content.split("\\|");
        byte[] pubBytes = Base64.getDecoder().decode(parts[0]);
        byte[] privBytes = Base64.getDecoder().decode(parts[1]);

        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey pub = kf.generatePublic(new X509EncodedKeySpec(pubBytes));
        PrivateKey priv = kf.generatePrivate(new PKCS8EncodedKeySpec(privBytes));
        return new KeyPair(pub, priv);
    } else {
        // Create new keypair
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();

        // Save to file
        String pubB64 = Base64.getEncoder().encodeToString(kp.getPublic().getEncoded());
        String privB64 = Base64.getEncoder().encodeToString(kp.getPrivate().getEncoded());
        String content = pubB64 + "|" + privB64;
        Files.write(Paths.get(KEY_FILE), content.getBytes("UTF-8"));

        return kp;
    }
}
```

| Thuộc tính | Mô tả |
|------------|-------|
| **Mục đích** | Load keypair từ file hoặc tạo mới nếu chưa có |
| **Tham số** | Không có |
| **Return** | `KeyPair` - Cặp khóa RSA của ứng dụng |

### 6.4 Luồng Hoạt Động

```
loadOrCreateAppKeyPair()
         │
         ▼
┌────────────────────┐
│ File app_rsa.key   │
│     tồn tại?       │
└─────────┬──────────┘
          │
    ┌─────┴─────┐
    │           │
   YES          NO
    │           │
    ▼           ▼
┌─────────┐  ┌─────────────────┐
│ Đọc file│  │ Sinh keypair mới│
│ → Parse │  │ RSA-2048        │
│ → Decode│  │ → Encode Base64 │
└─────────┘  │ → Ghi file      │
    │        └─────────────────┘
    │                │
    └────────┬───────┘
             │
             ▼
        Return KeyPair
```

### 6.5 Format File Lưu Trữ

```
app_rsa.key
───────────────────────────────────────────────────────────
[Base64 của Public Key (X.509)]|[Base64 của Private Key (PKCS8)]
───────────────────────────────────────────────────────────

Ví dụ:
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...|MIIEvgIBADANBgkqhk...
```

**Biến quan trọng:**

| Biến | Kiểu | Ý nghĩa |
|------|------|---------|
| `X509EncodedKeySpec` | `class` | Format chuẩn cho public key |
| `PKCS8EncodedKeySpec` | `class` | Format chuẩn cho private key |
| `parts[0]` | `String` | Base64 của public key |
| `parts[1]` | `String` | Base64 của private key |

---

## 7. SO SÁNH CÁC THUẬT TOÁN

### 7.1 Bảng So Sánh AES

| Tiêu chí | AES-ECB (AESUtils) | AES-CBC (CryptoUtils) |
|----------|--------------------|-----------------------|
| **IV** | Không cần | Bắt buộc (16 bytes) |
| **Bảo mật** | Thấp hơn | Cao hơn |
| **Tốc độ** | Nhanh hơn | Chậm hơn một chút |
| **Pattern leak** | Có | Không |
| **Parallel** | Có thể | Không thể |
| **Use case** | Dữ liệu random, nhỏ | Dữ liệu có pattern |

### 7.2 Bảng So Sánh RSA vs AES

| Tiêu chí | RSA | AES |
|----------|-----|-----|
| **Loại** | Bất đối xứng | Đối xứng |
| **Key** | Cặp public/private | Một key duy nhất |
| **Tốc độ** | Chậm | Nhanh |
| **Kích thước dữ liệu** | Giới hạn (~245 bytes) | Không giới hạn |
| **Use case** | Key exchange, Signature | Bulk encryption |

### 7.3 Hybrid Encryption (Dùng trong dự án)

```
┌─────────────────────────────────────────────────────────────────┐
│                    HYBRID ENCRYPTION                             │
│                                                                  │
│  Dữ liệu lớn                    Khóa AES                        │
│       │                             │                            │
│       ▼                             ▼                            │
│  ┌─────────┐                   ┌─────────┐                      │
│  │ AES-128 │ ◀───── Key ───── │ RSA     │                      │
│  │ Encrypt │                   │ Encrypt │                      │
│  └─────────┘                   │ Key     │                      │
│       │                        └─────────┘                      │
│       ▼                             │                            │
│  Ciphertext                    Encrypted Key                    │
│  (nhanh, lớn)                  (chậm, nhỏ)                      │
└─────────────────────────────────────────────────────────────────┘
```

---

## 8. ỨNG DỤNG TRONG DỰ ÁN

### 8.1 Luồng Mã Hóa Dữ Liệu Cá Nhân

```
┌─────────────────────────────────────────────────────────────────┐
│                     KHI ĐĂNG KÝ THẺ MỚI                         │
│                                                                  │
│  1. Admin nhập PIN "123456"                                     │
│         │                                                        │
│         ▼                                                        │
│  2. Applet sinh Master Key (AES-128) = AESUtils.generateAESKey()│
│         │                                                        │
│         ▼                                                        │
│  3. Mã hóa Master Key bằng PIN-derived key                      │
│     encryptedMK = AES(MK, deriveFromPIN("123456"))              │
│         │                                                        │
│         ▼                                                        │
│  4. Lưu encryptedMK vào thẻ                                     │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                     KHI LƯU THÔNG TIN                           │
│                                                                  │
│  1. Giải mã Master Key bằng PIN                                 │
│     MK = AES_decrypt(encryptedMK, deriveFromPIN(pin))           │
│         │                                                        │
│         ▼                                                        │
│  2. Mã hóa dữ liệu cá nhân                                      │
│     encName = AES(name, MK)                                     │
│     encDOB = AES(birthDate, MK)                                 │
│     encPhone = AES(phone, MK)                                   │
│         │                                                        │
│         ▼                                                        │
│  3. Lưu encrypted data vào thẻ                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 8.2 Luồng Xác Thực RSA (Chống Clone Thẻ)

```
┌─────────────────────────────────────────────────────────────────┐
│                     KHI ĐĂNG NHẬP                               │
│                                                                  │
│  ┌──────────┐           ┌──────────┐           ┌──────────┐    │
│  │  CLIENT  │           │   THẺ    │           │    DB    │    │
│  └────┬─────┘           └────┬─────┘           └────┬─────┘    │
│       │                      │                      │           │
│       │  1. Sinh challenge   │                      │           │
│       │  (32 bytes random)   │                      │           │
│       │ ────────────────────▶│                      │           │
│       │                      │                      │           │
│       │                      │ 2. Ký bằng           │           │
│       │                      │    Private Key       │           │
│       │                      │    (RSA-1024)        │           │
│       │                      │                      │           │
│       │◀──── signature ──────│                      │           │
│       │     (128 bytes)      │                      │           │
│       │                      │                      │           │
│       │ 3. Lấy stored        │                      │           │
│       │    public key ───────┼─────────────────────▶│           │
│       │                      │                      │           │
│       │◀─── modulus ─────────┼──────────────────────│           │
│       │                      │                      │           │
│       │ 4. importFromModulus()                      │           │
│       │    → RSAPublicKey                           │           │
│       │                      │                      │           │
│       │ 5. Verify signature                         │           │
│       │    Signature.verify(challenge, sig, pubKey) │           │
│       │                      │                      │           │
│       │    ✓ Valid → Thẻ chính chủ                 │           │
│       │    ✗ Invalid → Thẻ giả/clone               │           │
│       │                      │                      │           │
│  └────┴─────┘           └────┴─────┘           └────┴─────┘    │
└─────────────────────────────────────────────────────────────────┘
```

### 8.3 Bảng Tổng Hợp Sử Dụng Crypto

| Chức năng | Class | Hàm | Mục đích |
|-----------|-------|-----|----------|
| Sinh Master Key | AESUtils | `generateAESKey()` | Tạo key bảo vệ data |
| Mã hóa personal data | AESUtils | `encryptAES()` | Bảo mật thông tin |
| Giải mã personal data | AESUtils | `decryptAES()` | Đọc thông tin |
| Sinh key từ PIN | CryptoUtils | `deriveKeyFromPin()` | Bảo vệ Master Key |
| Export public key | RSAKeyUtils | `exportModulus()` | Lưu vào DB |
| Import public key | RSAKeyUtils | `importFromModulus()` | Verify signature |
| Lưu app keypair | AppKeyStore | `loadOrCreateAppKeyPair()` | Client keypair |

---

## PHỤ LỤC: CODE MẪU

### A. Mã Hóa/Giải Mã AES Đơn Giản

```java
import gymcard.Crypto.AESUtils;

// Sinh khóa
byte[] key = AESUtils.generateAESKey();

// Mã hóa
String plaintext = "Nguyễn Văn A - 0987654321";
byte[] encrypted = AESUtils.encryptAES(key, plaintext.getBytes("UTF-8"));

// Giải mã
byte[] decrypted = AESUtils.decryptAES(key, encrypted);
String result = new String(decrypted, "UTF-8");
// result = "Nguyễn Văn A - 0987654321"
```

### B. Sinh Key Từ PIN

```java
import gymcard.Crypto.CryptoUtils;
import java.security.SecureRandom;

// Sinh salt (lưu cùng với encrypted data)
byte[] salt = new byte[16];
new SecureRandom().nextBytes(salt);

// Sinh key từ PIN
char[] pin = "123456".toCharArray();
SecretKey key = CryptoUtils.deriveKeyFromPin(pin, salt);

// Dùng key để mã hóa
byte[] iv = CryptoUtils.generateIv();
byte[] ciphertext = CryptoUtils.aesEncrypt(
    key.getEncoded(), iv, "Secret Data".getBytes()
);

// Giải mã
byte[] plaintext = CryptoUtils.aesDecrypt(
    key.getEncoded(), iv, ciphertext
);
```

### C. Xác Thực RSA Challenge-Response

```java
import gymcard.Crypto.RSAKeyUtils;
import java.security.*;

// 1. Client sinh challenge
byte[] challenge = new byte[32];
new SecureRandom().nextBytes(challenge);

// 2. Gửi challenge đến thẻ, thẻ ký và trả về signature
byte[] signature = cardManager.signChallenge(challenge);

// 3. Lấy stored public key từ DB
byte[] storedModulus = database.getPublicKey(cardId);
RSAPublicKey pubKey = RSAKeyUtils.importFromModulus(storedModulus);

// 4. Verify signature
Signature sig = Signature.getInstance("SHA1withRSA");
sig.initVerify(pubKey);
sig.update(challenge);
boolean valid = sig.verify(signature);

if (valid) {
    System.out.println("Thẻ chính chủ!");
} else {
    System.out.println("CẢNH BÁO: Thẻ giả hoặc clone!");
}
```

---

*Tài liệu được tạo bởi GitHub Copilot*  
*Ngày tạo: Tháng 1/2025*
