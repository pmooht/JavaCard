# TÀI LIỆU CHI TIẾT TẦNG GIAO DIỆN (UI LAYER)
## Dự Án JavaCard Gym Membership System

---

## MỤC LỤC

1. [Tổng Quan Kiến Trúc UI](#1-tổng-quan-kiến-trúc-ui)
2. [Sơ Đồ Phân Cấp Panel](#2-sơ-đồ-phân-cấp-panel)
3. [Chi Tiết Từng File UI](#3-chi-tiết-từng-file-ui)
   - 3.1 [GymCardApp.java](#31-gymcardappjava---lớp-chính)
   - 3.2 [WelcomePanel.java](#32-welcomepaneljava---màn-hình-chào-mừng)
   - 3.3 [AdminPanel.java](#33-adminpaneljava---panel-quản-trị)
   - 3.4 [UserPanel.java](#34-userpaneljava---panel-người-dùng)
   - 3.5 [SidebarPanel.java](#35-sidebarpaneljava---thanh-menu-bên)
   - 3.6 [BaseTabPanel.java](#36-basetabpaneljava---lớp-cơ-sở-tab)
4. [Chi Tiết Các Tab](#4-chi-tiết-các-tab)
   - 4.1 [LoginPanel.java](#41-loginpaneljava)
   - 4.2 [RegistrationTab.java](#42-registrationtabjava)
   - 4.3 [InfoTab.java](#43-infotabjava)
   - 4.4 [CheckInTab.java](#44-checkintabjava)
   - 4.5 [TopUpTab.java](#45-topuptabjava)
   - 4.6 [PackageTab.java](#46-packagetabjava)
   - 4.7 [ServicesTab.java](#47-servicestablejava)
   - 4.8 [StatisticsTab.java](#48-statisticstabjava)
   - 4.9 [ChangePinTab.java](#49-changepintabjava)
   - 4.10 [PackageManagementTab.java](#410-packagemanagementtabjava)
   - 4.11 [ServiceManagementTab.java](#411-servicemanagementtabjava)
   - 4.12 [PinManagementTab.java](#412-pinmanagementtabjava)
5. [Utility Classes](#5-utility-classes)
   - 5.1 [CheckInDayDecorator.java](#51-checkindaydecoratorjava)
6. [Quy Ước Thiết Kế](#6-quy-ước-thiết-kế)
7. [Hướng Dẫn Chuyển Sang DOCX](#7-hướng-dẫn-chuyển-sang-docx)

---

## 1. TỔNG QUAN KIẾN TRÚC UI

### 1.1 Framework Sử Dụng
- **Java Swing**: Framework GUI chính
- **CardLayout**: Quản lý chuyển đổi giữa các panel
- **Custom Painting**: Giao diện hiện đại với Graphics2D
- **JCalendar**: Thư viện lịch cho chức năng check-in

### 1.2 Mô Hình Điều Hướng
```
GymCardApp (JFrame)
    └── mainPanel (CardLayout)
        ├── "welcome" → WelcomePanel
        ├── "admin" → AdminPanel
        │       ├── LoginPanel
        │       └── Sidebar + Tabs (Registration, PackageMgmt, ServiceMgmt, PinMgmt)
        └── "user" → UserPanel
                ├── LoginPanel
                └── Sidebar + Tabs (Info, CheckIn, TopUp, Package, Services, Statistics, ChangePin)
```

### 1.3 Nguyên Tắc Thiết Kế
| Nguyên tắc | Mô tả |
|------------|-------|
| **Dark Theme** | Admin sử dụng giao diện tối (#1E2332) |
| **Light Theme** | User sử dụng giao diện sáng (#F8FAFB) |
| **Rounded Corners** | Các card/button có góc bo tròn (10-16px) |
| **Gradient Header** | Header sử dụng gradient màu |
| **Icon-based UX** | Sử dụng emoji làm icon |

---

## 2. SƠ ĐỒ PHÂN CẤP PANEL

```
┌─────────────────────────────────────────────────────────────────┐
│                        GymCardApp (JFrame)                       │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                   StatusBar (Kết nối thẻ)                  │  │
│  └───────────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                  mainPanel (CardLayout)                    │  │
│  │  ┌─────────────────────────────────────────────────────┐  │  │
│  │  │              WelcomePanel (welcome)                  │  │  │
│  │  │    ┌─────────────┐        ┌─────────────┐           │  │  │
│  │  │    │ Admin Card  │        │  User Card  │           │  │  │
│  │  │    └─────────────┘        └─────────────┘           │  │  │
│  │  └─────────────────────────────────────────────────────┘  │  │
│  │  ┌─────────────────────────────────────────────────────┐  │  │
│  │  │                AdminPanel (admin)                    │  │  │
│  │  │  ┌─────────┐ ┌───────────────────────────────────┐  │  │  │
│  │  │  │ Sidebar │ │          Content Area             │  │  │  │
│  │  │  │         │ │  ┌─────────────────────────────┐  │  │  │  │
│  │  │  │ • Home  │ │  │       LoginPanel            │  │  │  │  │
│  │  │  │ • Đăng  │ │  └─────────────────────────────┘  │  │  │  │
│  │  │  │   ký    │ │  ┌─────────────────────────────┐  │  │  │  │
│  │  │  │ • Gói   │ │  │    RegistrationTab          │  │  │  │  │
│  │  │  │ • Dịch  │ │  │    PackageManagementTab     │  │  │  │  │
│  │  │  │   vụ    │ │  │    ServiceManagementTab     │  │  │  │  │
│  │  │  │ • PIN   │ │  │    PinManagementTab         │  │  │  │  │
│  │  │  └─────────┘ └───────────────────────────────────┘  │  │  │
│  │  └─────────────────────────────────────────────────────┘  │  │
│  │  ┌─────────────────────────────────────────────────────┐  │  │
│  │  │                 UserPanel (user)                     │  │  │
│  │  │  ┌─────────┐ ┌───────────────────────────────────┐  │  │  │
│  │  │  │ Sidebar │ │          Content Area             │  │  │  │
│  │  │  │         │ │  ┌─────────────────────────────┐  │  │  │  │
│  │  │  │ • Home  │ │  │       LoginPanel            │  │  │  │  │
│  │  │  │ • Thông │ │  └─────────────────────────────┘  │  │  │  │
│  │  │  │   tin   │ │  ┌─────────────────────────────┐  │  │  │  │
│  │  │  │ • Check │ │  │    InfoTab                  │  │  │  │  │
│  │  │  │ • Nạp   │ │  │    CheckInTab               │  │  │  │  │
│  │  │  │ • Gói   │ │  │    TopUpTab                 │  │  │  │  │
│  │  │  │ • Dịch  │ │  │    PackageTab               │  │  │  │  │
│  │  │  │   vụ    │ │  │    ServicesTab              │  │  │  │  │
│  │  │  │ • Thống │ │  │    StatisticsTab            │  │  │  │  │
│  │  │  │   kê    │ │  │    ChangePinTab             │  │  │  │  │
│  │  │  │ • Đổi   │ │  └─────────────────────────────┘  │  │  │  │
│  │  │  │   PIN   │ │                                   │  │  │  │
│  │  │  └─────────┘ └───────────────────────────────────┘  │  │  │
│  │  └─────────────────────────────────────────────────────┘  │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 3. CHI TIẾT TỪNG FILE UI

### 3.1 GymCardApp.java - Lớp Chính

**📁 Đường dẫn:** `src/gymcard/client/GymCardApp.java`  
**📊 Số dòng:** 397 dòng  
**🎯 Chức năng:** Điểm khởi đầu ứng dụng, quản lý navigation tổng thể

#### Các Hàm Quan Trọng:

| Hàm | Mô tả | Tham số | Ghi chú |
|-----|-------|---------|---------|
| `main(String[] args)` | Entry point của ứng dụng | args: tham số dòng lệnh | Thiết lập Look&Feel, tạo JFrame |
| `initUI()` | Khởi tạo giao diện chính | Không | Tạo StatusBar, MainPanel với CardLayout |
| `createStatusBar()` | Tạo thanh trạng thái | Không | Hiển thị trạng thái kết nối thẻ |
| `toggleConnection()` | Bật/tắt kết nối thẻ | Không | Gọi `cardComm.connect()` hoặc `disconnect()` |
| `showWelcomePanel()` | Hiển thị màn hình chào | Không | Chuyển CardLayout sang "welcome" |
| `showAdminPanel()` | Hiển thị Admin panel | Không | Chuyển sang "admin", yêu cầu đăng nhập |
| `showUserPanel()` | Hiển thị User panel | Không | Chuyển sang "user", yêu cầu đăng nhập |
| `showForceChangePinDialog()` | Hiển thị dialog đổi PIN bắt buộc | Không | Khi phát hiện PIN mặc định (123456) |
| `isCardKnown(String cardId)` | Kiểm tra thẻ đã đăng ký chưa | cardId: ID thẻ | Đọc từ `known_cards.txt` |
| `markCardAsKnown(String cardId)` | Ghi nhận thẻ đã đăng ký | cardId: ID thẻ | Ghi vào `known_cards.txt` |

#### Flow Xử Lý Chính:
```
main() → GymCardApp() → initUI()
                           │
                           ├── createStatusBar()
                           ├── createWelcomePanel()
                           ├── createAdminPanel()
                           └── createUserPanel()
                           
User chọn Admin/User → showAdminPanel()/showUserPanel()
                           │
                           └── LoginPanel.doLogin() → verifyPinWithCardAuth()
                                                           │
                                                           ├── PIN sai → Thông báo lỗi
                                                           ├── PIN đúng, RSA OK → onLogin()
                                                           └── PIN đúng, RSA thất bại → CHẶN
```

---

### 3.2 WelcomePanel.java - Màn Hình Chào Mừng

**📁 Đường dẫn:** `src/gymcard/client/ui/WelcomePanel.java`  
**📊 Số dòng:** 338 dòng  
**🎯 Chức năng:** Màn hình đầu tiên hiển thị, cho phép chọn vai trò

#### Các Hàm Quan Trọng:

| Hàm | Mô tả | Tham số |
|-----|-------|---------|
| `initUI()` | Khởi tạo giao diện welcome | Không |
| `createHeader()` | Tạo header với logo | Không |
| `createRoleCard(String title, String desc, String icon, Color color, ActionListener action)` | Tạo card chọn vai trò | title, desc: nội dung; icon: emoji; color: màu chủ đạo; action: xử lý click |
| `updateConnectionStatus(boolean connected, String cardId)` | Cập nhật trạng thái kết nối | connected: đã kết nối; cardId: ID thẻ |
| `createConnectionStatusPanel()` | Tạo panel trạng thái kết nối | Không |

#### Giao Diện:
- **Header**: Logo GYM SMARTCARD với gradient xanh
- **2 Card chọn vai trò**:
  - 🔧 **Admin Card**: Màu tím (#9B59B6)
  - 👤 **User Card**: Màu xanh dương (#3498DB)
- **Footer**: Trạng thái kết nối thẻ

---

### 3.3 AdminPanel.java - Panel Quản Trị

**📁 Đường dẫn:** `src/gymcard/client/ui/AdminPanel.java`  
**📊 Số dòng:** 537 dòng  
**🎯 Chức năng:** Giao diện quản trị viên với dark theme

#### Các Hàm Quan Trọng:

| Hàm | Mô tả | Ghi chú |
|-----|-------|---------|
| `initUI()` | Khởi tạo layout (Sidebar + Content) | BorderLayout |
| `createSidebar()` | Tạo sidebar menu bên trái | Chứa các menu item |
| `createHomePanel()` | Tạo dashboard trang chủ | 4 stat cards |
| `createDashboardCard(String title, String value, String icon, Color color)` | Tạo card thống kê | Hiển thị số liệu tóm tắt |
| `showPanel(String panelName)` | Chuyển đến panel cụ thể | Sử dụng CardLayout |
| `onLogin()` | Callback sau đăng nhập thành công | Load dữ liệu, hiển thị Home |
| `logout()` | Đăng xuất | Quay về WelcomePanel |

#### Inner Class:
```java
private class SidebarMenuItem {
    String name;      // Tên hiển thị
    String icon;      // Emoji icon
    String panelKey;  // Key trong CardLayout
}
```

#### Menu Items:
| Icon | Tên | Panel Key | Tab tương ứng |
|------|-----|-----------|---------------|
| 🏠 | Trang chủ | home | Dashboard |
| 📝 | Đăng ký | registration | RegistrationTab |
| 📦 | Gói tập | packages | PackageManagementTab |
| 🛠 | Dịch vụ | services | ServiceManagementTab |
| 🔐 | Quản lý PIN | pin | PinManagementTab |
| 🔒 | Đổi PIN Admin | changepin | ChangePinTab |
| 🚪 | Đăng xuất | - | logout() |

---

### 3.4 UserPanel.java - Panel Người Dùng

**📁 Đường dẫn:** `src/gymcard/client/ui/UserPanel.java`  
**📊 Số dòng:** 355 dòng  
**🎯 Chức năng:** Giao diện người dùng (hội viên) với light theme

#### Các Hàm Quan Trọng:

| Hàm | Mô tả | Ghi chú |
|-----|-------|---------|
| `initUI()` | Khởi tạo layout với sidebar | Light theme |
| `createHeader()` | Tạo header với thông tin user | Hiển thị avatar, tên |
| `createHomePanel()` | Tạo dashboard với quick actions | 4 quick action cards |
| `createQuickCard(String title, String desc, String icon, Color color, String panelKey)` | Tạo card action nhanh | Click để chuyển tab |
| `showPanel(String panelName)` | Chuyển đến panel | CardLayout |
| `refreshCurrentPanel()` | Refresh tab hiện tại | Gọi refreshData() của tab |
| `onLogin()` | Callback sau đăng nhập | Load thông tin cá nhân |

#### Menu Items:
| Icon | Tên | Panel Key |
|------|-----|-----------|
| 🏠 | Trang chủ | home |
| 📋 | Thông tin | info |
| ✅ | Check-in | checkin |
| 💰 | Nạp tiền | topup |
| 📦 | Gói tập | package |
| 🛍 | Dịch vụ | services |
| 📊 | Thống kê | statistics |
| 🔑 | Đổi PIN | changepin |
| 🚪 | Đăng xuất | - |

---

### 3.5 SidebarPanel.java - Thanh Menu Bên

**📁 Đường dẫn:** `src/gymcard/client/ui/SidebarPanel.java`  
**📊 Số dòng:** ~130 dòng  
**🎯 Chức năng:** Component sidebar tái sử dụng cho Admin/User

#### Các Hàm Quan Trọng:

| Hàm | Mô tả | Tham số |
|-----|-------|---------|
| `addItem(String icon, String text, Runnable action)` | Thêm menu item | icon: emoji; text: nhãn; action: callback |
| `addSeparator()` | Thêm đường phân cách | Không |
| `selectItem(int index)` | Chọn item theo index | index: vị trí trong list |

#### Inner Class - SidebarItem:
```java
class SidebarItem extends JPanel {
    private boolean selected;
    private String icon;
    private String text;
    private Runnable action;
    
    // Hover effect với màu khác
    // Selected state với background highlight
}
```

#### Styling:
- **Width**: 200px cố định
- **Item Height**: 45px
- **Selected Background**: 
  - Admin (dark): rgba(255,255,255,0.1)
  - User (light): rgba(0,0,0,0.05)
- **Hover Effect**: Màu sáng hơn khi hover

---

### 3.6 BaseTabPanel.java - Lớp Cơ Sở Tab

**📁 Đường dẫn:** `src/gymcard/client/ui/BaseTabPanel.java`  
**📊 Số dòng:** 152 dòng  
**🎯 Chức năng:** Abstract class cung cấp utilities cho tất cả tab

#### Các Hàm Tiện Ích:

| Hàm | Mô tả | Return |
|-----|-------|--------|
| `createModernButton(String text, Color bgColor, int fontSize)` | Tạo button hiện đại với bo tròn | JButton |
| `fieldBorder()` | Tạo border cho input field | Border |
| `log(String message)` | Ghi log ra console | void |
| `createStatCard(String title, String value, String icon, Color color)` | Tạo card thống kê | JPanel |

#### Các Hàm Validate:

| Hàm | Mô tả | Exception |
|-----|-------|-----------|
| `validateName(String name)` | Kiểm tra họ tên hợp lệ | Exception nếu trống/ngắn |
| `validateBirthDate(String dob)` | Kiểm tra ngày sinh (dd/MM/yyyy) | Exception nếu sai format |
| `validatePhone(String phone)` | Kiểm tra SĐT (0xxxxxxxxx) | Exception nếu sai format |

#### Class Diagram:
```
            BaseTabPanel (abstract)
                   │
    ┌──────────────┼──────────────┐
    │              │              │
InfoTab      CheckInTab      TopUpTab
PackageTab   ServicesTab     StatisticsTab
ChangePinTab RegistrationTab PackageManagementTab
ServiceManagementTab         PinManagementTab
                             LoginPanel
```

---

## 4. CHI TIẾT CÁC TAB

### 4.1 LoginPanel.java

**📁 Đường dẫn:** `src/gymcard/client/ui/tabs/LoginPanel.java`  
**📊 Số dòng:** 188 dòng  
**🎯 Chức năng:** Panel đăng nhập bằng PIN + RSA authentication

#### Các Hàm Quan Trọng:

| Hàm | Mô tả | Logic |
|-----|-------|-------|
| `initUI()` | Tạo form đăng nhập | PIN field 6 số, nút đăng nhập |
| `doLogin()` | Xử lý đăng nhập | Xác thực PIN + RSA challenge-response |
| `checkTries()` | Kiểm tra số lần thử còn lại | Hiển thị cảnh báo nếu gần hết |
| `reset()` | Reset form | Xóa PIN, trạng thái |

#### Flow Xác Thực:
```
doLogin()
    │
    ├── Kiểm tra thẻ kết nối
    │
    ├── Validate PIN (6 chữ số)
    │
    ├── Kiểm tra số lần thử (getPinTries)
    │       └── tries == 0 → Thẻ bị khóa!
    │
    └── cardComm.verifyPinWithCardAuth(pin)
            │
            ├── pinVerified = false → "Sai PIN!"
            │
            ├── rsaVerified = true → Đăng nhập OK ✓
            │
            ├── rsaSkipped = true → Đăng nhập OK (thẻ mới)
            │
            └── rsaVerified = false → "Thẻ không hợp lệ!" ✗
                (Có thể thẻ clone)
```

#### UI Elements:
- 🔐 Icon khóa
- Title: "ĐĂNG NHẬP BẰNG PIN"
- JPasswordField (6 ký tự, echoChar='●')
- Nút "Đăng nhập" (màu xanh)
- Nút "Kiểm tra số lần thử" (màu tím)
- Label hiển thị số lần thử còn lại (màu đỏ)

---

### 4.2 RegistrationTab.java

**📁 Đường dẫn:** `src/gymcard/client/ui/tabs/RegistrationTab.java`  
**📊 Số dòng:** 509 dòng  
**🎯 Chức năng:** Form đăng ký hội viên mới (Admin only)

#### Các Hàm Quan Trọng:

| Hàm | Mô tả | Ghi chú |
|-----|-------|---------|
| `initUI()` | Tạo form đăng ký | Dark theme, nhiều fields |
| `createFormCard()` | Tạo card chứa form | Border tím |
| `registerMember()` | Xử lý đăng ký | Gọi cardComm.initNewCard() + setMemberInfo() |
| `compressAvatarToCardSize(byte[] original)` | Nén ảnh avatar | Giảm xuống ~1KB để lưu vào thẻ |
| `clearForm()` | Xóa dữ liệu form | Reset tất cả field |
| `chooseAvatar()` | Chọn ảnh từ file | JFileChooser |

#### Form Fields:
| Field | Label | Validation |
|-------|-------|------------|
| avatarLabel | Ảnh đại diện | JPEG, nén xuống 1KB |
| nameField | Họ và tên * | Không trống, ≥2 ký tự |
| dobField | Ngày sinh * | Format dd/MM/yyyy |
| phoneField | Số điện thoại * | 0xxxxxxxxx (10-11 số) |
| genderCombo | Giới tính | Nam/Nữ/Khác |
| addressField | Địa chỉ | Optional |
| pinField | Mã PIN * | Đúng 6 chữ số |
| confirmPinField | Xác nhận PIN * | Khớp với PIN |

#### Flow Đăng Ký:
```
registerMember()
    │
    ├── Validate tất cả fields
    │
    ├── Nén avatar (nếu có)
    │
    ├── cardComm.initNewCard(pin)
    │       └── Khởi tạo Master Key, encrypt bằng PIN
    │
    └── cardComm.setMemberInfo(name, dob, phone, gender, address, avatar)
            └── Encrypt personal data bằng Master Key
            └── Lưu vào thẻ
```

---

### 4.3 InfoTab.java

**📁 Đường dẫn:** `src/gymcard/client/ui/tabs/InfoTab.java`  
**📊 Số dòng:** 770 dòng  
**🎯 Chức năng:** Hiển thị và chỉnh sửa thông tin cá nhân

#### Các Hàm Quan Trọng:

| Hàm | Mô tả |
|-----|-------|
| `initUI()` | Tạo giao diện hiển thị info |
| `createMainCard()` | Tạo card thông tin chính |
| `createProfileSection()` | Tạo section avatar + tên |
| `createInfoSection()` | Tạo các field thông tin |
| `createFieldCard(String label, String icon, JLabel valueLabel)` | Tạo card cho từng field |
| `showEditDialog()` | Hiển thị dialog chỉnh sửa |
| `setAvatarToLabel(JLabel label, byte[] avatarBytes)` | Set ảnh avatar vào label |
| `chooseAndCompressAvatar()` | Chọn và nén ảnh mới |
| `refreshData()` | Tải lại thông tin từ thẻ |

#### Thông Tin Hiển Thị:
| Icon | Field | Source |
|------|-------|--------|
| 👤 | Họ và tên | cardComm.getMemberInfo().name |
| 📅 | Ngày sinh | cardComm.getMemberInfo().birthDate |
| 📱 | Số điện thoại | cardComm.getMemberInfo().phone |
| ⚧ | Giới tính | cardComm.getMemberInfo().gender |
| 📍 | Địa chỉ | cardComm.getMemberInfo().address |
| 📸 | Avatar | cardComm.getAvatar() |

#### Dialog Edit:
- Form chỉnh sửa các thông tin
- Có thể đổi avatar
- Nút "Lưu lên thẻ" → `cardComm.setMemberInfo(...)`

---

### 4.4 CheckInTab.java

**📁 Đường dẫn:** `src/gymcard/client/ui/tabs/CheckInTab.java`  
**📊 Số dòng:** 238 dòng  
**🎯 Chức năng:** Check-in/Check-out với lịch đánh dấu

#### Các Hàm Quan Trọng:

| Hàm | Mô tả |
|-----|-------|
| `initUI()` | Tạo UI với JCalendar |
| `refreshData()` | Load trạng thái check-in từ thẻ |
| `checkInBtn.actionPerformed` | Xử lý check-in |
| `checkOutBtn.actionPerformed` | Xử lý check-out |

#### UI Components:
- **JCalendar**: Lịch hiển thị các ngày đã tập
- **CheckInDayDecorator**: Tô màu ngày đã check-in
- **Status Label**: Hiển thị trạng thái hiện tại
- **Check-in Button**: Màu xanh lá
- **Check-out Button**: Màu đỏ

#### Màu Sắc Ngày:
| Trạng thái | Màu | Mô tả |
|------------|-----|-------|
| Đang tập | Xanh lá (#C8E6C9) | Check-in nhưng chưa check-out |
| Hoàn thành | Tím (#E9D5FF) | Đã check-in và check-out |

---

### 4.5 TopUpTab.java

**📁 Đường dẫn:** `src/gymcard/client/ui/tabs/TopUpTab.java`  
**📊 Số dòng:** 568 dòng  
**🎯 Chức năng:** Nạp tiền vào thẻ

#### Các Hàm Quan Trọng:

| Hàm | Mô tả |
|-----|-------|
| `initUI()` | Tạo UI nạp tiền |
| `createHeaderCard()` | Card hiển thị số dư + hình ảnh thẻ |
| `createCardVisual()` | Thiết kế visual thẻ 3D |
| `createQuickAmountSection()` | Nút chọn nhanh 50k/100k/200k/500k |
| `createAmountInputSection()` | Input nhập số tiền tùy ý |
| `createSubmitButton()` | Nút xác nhận nạp |
| `refreshData()` | Tải số dư từ thẻ |

#### Quick Amount Buttons:
| Button | Giá trị |
|--------|---------|
| 50k | 50,000 VND |
| 100k | 100,000 VND (mặc định) |
| 200k | 200,000 VND |
| 500k | 500,000 VND |

#### Flow Nạp Tiền:
```
Nhập số tiền → Submit → cardComm.addBalance(amount)
                              │
                              └── Cập nhật số dư trên thẻ
```

---

### 4.6 PackageTab.java

**📁 Đường dẫn:** `src/gymcard/client/ui/tabs/PackageTab.java`  
**📊 Số dòng:** 474 dòng  
**🎯 Chức năng:** Xem và mua gói tập

#### Các Hàm Quan Trọng:

| Hàm | Mô tả |
|-----|-------|
| `initUI()` | Tạo UI danh sách gói tập |
| `createDBPackageCard(PlanInfo plan, String icon, Color color, JLabel currentLabel)` | Tạo card cho 1 gói tập |
| `showPurchaseDialog(PlanInfo plan, Color color, JLabel currentLabel)` | Dialog xác nhận mua |
| `loadBtn.actionPerformed` | Tải gói hiện tại từ thẻ |

#### Thông Tin Gói Tập Từ DB:
| Field | Mô tả |
|-------|-------|
| name | Tên gói |
| description | Mô tả |
| durationDays | Số ngày |
| sessionCount | Số buổi (nếu có) |
| maxDurationMinutes | Thời lượng tối đa/buổi |
| price | Giá (VND) |

#### Flow Mua Gói:
```
Chọn gói → showPurchaseDialog()
                │
                ├── Kiểm tra số dư >= price
                │
                ├── cardComm.deductBalance(price)
                │
                └── cardComm.setPackage(type, newExpiry, today, sessions)
```

---

### 4.7 ServicesTab.java

**📁 Đường dẫn:** `src/gymcard/client/ui/tabs/ServicesTab.java`  
**📊 Số dòng:** 425 dòng  
**🎯 Chức năng:** Mua các dịch vụ bổ sung

#### Các Hàm Quan Trọng:

| Hàm | Mô tả |
|-----|-------|
| `initUI()` | Tạo UI dạng grid cards |
| `createHeader()` | Header với nút refresh |
| `loadServices()` | Load dịch vụ từ DB |
| `createServiceCard(ServiceInfo svc, String icon, Color iconColor, Color iconBgColor, String category)` | Tạo card dịch vụ |
| `purchaseService(String svcName, long svcPrice)` | Xử lý mua dịch vụ |
| `refreshData()` | Public method để refresh từ bên ngoài |

#### Phân Loại Dịch Vụ:
| Category | Icon | Màu | Điều kiện |
|----------|------|-----|-----------|
| ĐỒ UỐNG | 🥤 | Vàng (#FBB727) | code/name chứa "drink", "nước" |
| DINH DƯỠNG | 🥛 | Xanh dương (#3B82F6) | chứa "protein", "shake" |
| TIỆN ÍCH | 🔐 | Xanh lá (#22C55E) | chứa "locker", "tủ" |
| CAO CẤP | 🏋 | Đỏ (#EF4444) | chứa "pt", "trainer" |
| DỊCH VỤ | ✂ | Tím (#A855F7) | Mặc định |

---

### 4.8 StatisticsTab.java

**📁 Đường dẫn:** `src/gymcard/client/ui/tabs/StatisticsTab.java`  
**📊 Số dòng:** 752 dòng  
**🎯 Chức năng:** Dashboard thống kê hoạt động

#### Các Hàm Quan Trọng:

| Hàm | Mô tả |
|-----|-------|
| `initUI()` | Tạo dashboard |
| `createHeader()` | Header với last update + refresh |
| `createStatCardsRow()` | Row 3 stat cards (Gói tập, Số dư, Dịch vụ) |
| `createPackageCard()` | Card thông tin gói tập hiện tại |
| `createBalanceCard()` | Card hiển thị số dư |
| `createServicesCard()` | Card số dịch vụ đã mua |
| `createActivityChartPanel()` | Biểu đồ cột 7 ngày |
| `createActivityLogPanel()` | Log hoạt động gần đây |
| `refreshData()` | Tải tất cả dữ liệu từ thẻ |

#### Stat Cards:
| Card | Màu | Data Source |
|------|-----|-------------|
| Gói tập | Xanh dương | cardComm.getPackage() |
| Số dư | Xanh lá | cardComm.getBalance() |
| Dịch vụ | Hồng | purchasedServices.size() |

#### Activity Chart:
- Biểu đồ cột 7 ngày trong tuần
- Tô màu ngày có check-in
- Hiển thị tổng buổi + thời gian trung bình

---

### 4.9 ChangePinTab.java

**📁 Đường dẫn:** `src/gymcard/client/ui/tabs/ChangePinTab.java`  
**📊 Số dòng:** 178 dòng  
**🎯 Chức năng:** Đổi mã PIN (User tự đổi)

#### Các Hàm Quan Trọng:

| Hàm | Mô tả |
|-----|-------|
| `initUI()` | Tạo form đổi PIN |
| `changeBtn.actionPerformed` | Xử lý đổi PIN |

#### Form Fields:
| Field | Label | Validation |
|-------|-------|------------|
| oldPinField | Mã PIN hiện tại | 6 chữ số |
| newPinField | Mã PIN mới | 6 chữ số |
| confirmPinField | Xác nhận PIN mới | Khớp với PIN mới |

#### Flow:
```
Nhập PIN cũ + PIN mới → cardComm.changePin(oldPin, newPin)
                              │
                              ├── Verify PIN cũ
                              ├── Decrypt Master Key bằng PIN cũ
                              ├── Re-encrypt Master Key bằng PIN mới
                              └── Update PIN trên thẻ
```

---

### 4.10 PackageManagementTab.java

**📁 Đường dẫn:** `src/gymcard/client/ui/tabs/PackageManagementTab.java`  
**📊 Số dòng:** 670 dòng  
**🎯 Chức năng:** Quản lý gói tập (Admin)

#### Các Hàm Quan Trọng:

| Hàm | Mô tả |
|-----|-------|
| `initUI()` | Tạo UI với bảng + thống kê |
| `createHeader()` | Header với nút Thêm/Refresh |
| `createStatsPanel()` | 3 stat cards (Tổng, Active, Giá max) |
| `createTablePanel()` | Bảng danh sách gói tập |
| `loadPlans()` | Load từ DatabaseManager |
| `showAddEditDialog(PlanInfo existing)` | Dialog thêm/sửa |
| `editPlan(int row)` | Sửa gói tập |
| `togglePlan(int row)` | Ẩn/hiện gói tập |
| `deletePlan(int row)` | Xóa gói tập |

#### Table Columns:
| Column | Field |
|--------|-------|
| ID (ẩn) | id |
| MÃ GÓI | code |
| TÊN GÓI | name |
| MÔ TẢ | description |
| NGÀY | durationDays |
| BUỔI | sessionCount |
| THỜI LƯỢNG | maxDurationMinutes |
| GIÁ | price |
| TRẠNG THÁI | isActive |
| THAO TÁC | Edit/Toggle/Delete buttons |

---

### 4.11 ServiceManagementTab.java

**📁 Đường dẫn:** `src/gymcard/client/ui/tabs/ServiceManagementTab.java`  
**📊 Số dòng:** 514 dòng  
**🎯 Chức năng:** Quản lý dịch vụ (Admin)

#### Các Hàm Quan Trọng:

| Hàm | Mô tả |
|-----|-------|
| `initUI()` | Tạo UI dark theme |
| `createHeader()` | Header với Thêm/Refresh |
| `createTablePanel()` | Bảng danh sách dịch vụ |
| `loadServices()` | Load từ DB |
| `showAddEditDialog(ServiceInfo existing)` | Dialog thêm/sửa |
| `editService(int row)` | Sửa dịch vụ |
| `toggleService(int row)` | Ẩn/hiện dịch vụ |
| `deleteService(int row)` | Xóa dịch vụ |

#### Table Columns:
| Column | Field |
|--------|-------|
| ID (ẩn) | id |
| ID DỊCH VỤ | code |
| TÊN DỊCH VỤ | name |
| MÔ TẢ CHI TIẾT | description |
| GIÁ (VND) | price |
| TRẠNG THÁI | isActive |
| HÀNH ĐỘNG | Edit/Toggle/Delete |

---

### 4.12 PinManagementTab.java

**📁 Đường dẫn:** `src/gymcard/client/ui/tabs/PinManagementTab.java`  
**📊 Số dòng:** 457 dòng  
**🎯 Chức năng:** Quản lý PIN hội viên (Admin)

#### Các Hàm Quan Trọng:

| Hàm | Mô tả |
|-----|-------|
| `initUI()` | Tạo UI 2 cột |
| `createChangePinCard()` | Card đổi PIN khi quên |
| `createUnlockCard()` | Card mở khóa thẻ |
| `createPinBoxes(JPasswordField[] fields)` | Tạo 6 ô nhập PIN |
| `getPinFromFields(JPasswordField[] fields)` | Lấy PIN từ 6 ô |
| `clearPinFields(JPasswordField[] fields)` | Xóa 6 ô |
| `createDarkPasswordField(String placeholder)` | Tạo password field dark |
| `createDarkButton(String text, Color color)` | Tạo button dark |

#### 2 Chức Năng Chính:

**1. Đổi PIN (khi hội viên quên):**
- Yêu cầu mật khẩu Admin
- Nhập PIN mới + Xác nhận
- Gọi `cardComm.adminResetMemberPin(adminPass, newPin)`

**2. Mở khóa thẻ:**
- Khi thẻ bị khóa do nhập sai PIN 5 lần
- Yêu cầu mật khẩu Admin
- Gọi `cardComm.unlockPin(adminPass)`
- Reset bộ đếm lỗi

---

## 5. UTILITY CLASSES

### 5.1 CheckInDayDecorator.java

**📁 Đường dẫn:** `src/gymcard/client/ui/CheckInDayDecorator.java`  
**📊 Số dòng:** 126 dòng  
**🎯 Chức năng:** Tô màu các ngày đã check-in trên JCalendar

#### Các Hàm Quan Trọng:

| Hàm | Mô tả |
|-----|-------|
| `CheckInDayDecorator(JCalendar calendar)` | Constructor |
| `addCheckInDate(String date)` | Thêm ngày check-in (chưa checkout) |
| `markDateCheckedOut(String date)` | Đánh dấu đã checkout |
| `addCompletedDate(String date)` | Thêm ngày hoàn thành |
| `addCheckInDates(Collection<String> dates)` | Thêm nhiều ngày |
| `clearCheckInDates()` | Xóa tất cả |
| `updateCalendar()` | Cập nhật màu trên lịch |
| `install()` | Cài đặt listeners |

#### Màu Sắc:
| Trạng thái | Background | Border |
|------------|------------|--------|
| Đang tập (chưa checkout) | #C8E6C9 (xanh lá nhạt) | #4CAF50 |
| Hoàn thành (đã checkout) | #E9D5FF (tím nhạt) | #9B59B6 |

---

## 6. QUY ƯỚC THIẾT KẾ

### 6.1 Color Palette

**Admin Panel (Dark Theme):**
```
BG_DARK         = #1E2332
CARD_BG         = #282D42
INPUT_BG        = #373C55
TEXT_WHITE      = #E6E6F0
TEXT_GRAY       = #8C91A5
ACCENT_BLUE     = #3498DB
ACCENT_GREEN    = #2ECC71
ACCENT_YELLOW   = #F1C40F
ACCENT_RED      = #E74C3C
```

**User Panel (Light Theme):**
```
BG_LIGHT        = #F8FAFC
CARD_BG         = #FFFFFF
TEXT_DARK       = #1E293B
TEXT_GRAY       = #64748B
PRIMARY_BLUE    = #3B82F6
PRIMARY_GREEN   = #22C55E
PRIMARY_PINK    = #EC4899
BORDER_COLOR    = #E2E8F0
```

### 6.2 Typography
- **Font Family**: Segoe UI
- **Title**: Bold, 22-24px
- **Subtitle**: Plain, 13px
- **Body**: Plain, 13-14px
- **Button**: Bold, 12-14px
- **Small/Caption**: 10-11px

### 6.3 Spacing
- **Panel Padding**: 20-25px
- **Card Padding**: 15-20px
- **Component Gap**: 10-15px
- **Button Height**: 35-45px
- **Input Height**: 45-55px

### 6.4 Border Radius
- **Cards**: 12-16px
- **Buttons**: 8-10px
- **Inputs**: 8-12px
- **Small elements**: 4-6px

---

## 7. HƯỚNG DẪN CHUYỂN SANG DOCX

### Cách 1: Sử dụng Pandoc (Khuyến nghị)
```bash
# Cài đặt Pandoc từ: https://pandoc.org/installing.html
# Chạy lệnh:
pandoc UI_LAYER_DOCUMENTATION.md -o UI_LAYER_DOCUMENTATION.docx
```

### Cách 2: Sử dụng VS Code Extension
1. Cài extension **"Markdown All in One"**
2. Mở file `.md`
3. Ctrl+Shift+P → "Markdown: Export to DOCX"

### Cách 3: Sử dụng Online Converter
1. Truy cập https://cloudconvert.com/md-to-docx
2. Upload file `UI_LAYER_DOCUMENTATION.md`
3. Tải file `.docx` về

### Cách 4: Copy vào Microsoft Word
1. Mở file `.md` trong VS Code
2. Ctrl+Shift+V để xem Preview
3. Copy nội dung từ Preview
4. Paste vào Word
5. Format lại nếu cần

---

## PHỤ LỤC

### A. Danh Sách Tất Cả Files UI

| # | File | Dòng | Chức năng |
|---|------|------|-----------|
| 1 | GymCardApp.java | 397 | Main entry, navigation |
| 2 | WelcomePanel.java | 338 | Màn hình chào mừng |
| 3 | AdminPanel.java | 537 | Panel quản trị |
| 4 | UserPanel.java | 355 | Panel người dùng |
| 5 | SidebarPanel.java | ~130 | Component sidebar |
| 6 | BaseTabPanel.java | 152 | Base class cho tabs |
| 7 | LoginPanel.java | 188 | Đăng nhập PIN+RSA |
| 8 | RegistrationTab.java | 509 | Đăng ký hội viên |
| 9 | InfoTab.java | 770 | Thông tin cá nhân |
| 10 | CheckInTab.java | 238 | Check-in/out |
| 11 | TopUpTab.java | 568 | Nạp tiền |
| 12 | PackageTab.java | 474 | Xem/mua gói tập |
| 13 | ServicesTab.java | 425 | Dịch vụ bổ sung |
| 14 | StatisticsTab.java | 752 | Thống kê |
| 15 | ChangePinTab.java | 178 | Đổi PIN (User) |
| 16 | PackageManagementTab.java | 670 | Quản lý gói (Admin) |
| 17 | ServiceManagementTab.java | 514 | Quản lý dịch vụ (Admin) |
| 18 | PinManagementTab.java | 457 | Quản lý PIN (Admin) |
| 19 | CheckInDayDecorator.java | 126 | Decorator lịch |
| **TỔNG** | | **~7,778** | |

---

*Tài liệu được tạo bởi GitHub Copilot*  
*Ngày tạo: Tháng 1/2025*
