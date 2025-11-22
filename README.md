# TDTU Student Information Management System

Hệ thống quản lý thông tin sinh viên TDTU - Ứng dụng Android hiện đại được xây dựng với Jetpack Compose và Firebase.

## 📋 Tổng quan

TDTU Student Information Management System là một ứng dụng Android quản lý toàn diện thông tin sinh viên và người dùng, được thiết kế để hỗ trợ các hoạt động quản lý của trường đại học. Hệ thống cung cấp các tính năng quản lý người dùng, quản lý sinh viên, theo dõi chứng chỉ và phân quyền truy cập linh hoạt.

## 🏗️ Kiến trúc & Công nghệ

### Kiến trúc
- **MVVM (Model-View-ViewModel)**: Tách biệt logic nghiệp vụ và UI
- **Repository Pattern**: Quản lý truy cập dữ liệu tập trung
- **Dependency Injection**: Sử dụng Hilt để quản lý dependencies

### Công nghệ sử dụng
- **UI Framework**: Jetpack Compose - Modern Android UI toolkit
- **Backend**: Firebase
  - **Firebase Authentication**: Xác thực người dùng
  - **Cloud Firestore**: Database NoSQL real-time
  - **Firebase Storage**: Lưu trữ file (ảnh đại diện, chứng chỉ)
- **Dependency Injection**: Hilt (Dagger)
- **Asynchronous**: Kotlin Coroutines & Flow
- **Image Loading**: Coil

## ✨ Tính năng chính

### 🔐 Xác thực & Bảo mật
- Đăng nhập/Đăng xuất với Email/Password
- Quản lý phiên đăng nhập
- Theo dõi lịch sử đăng nhập (chỉ Admin)
- Quản lý trạng thái tài khoản (Normal/Locked)

### 👥 Quản lý người dùng
- Xem danh sách người dùng với tìm kiếm và lọc
- Thêm/Sửa/Xóa người dùng (Admin only)
- Quản lý vai trò người dùng (Admin/Manager/Employee)
- Cập nhật ảnh đại diện
- Xem lịch sử đăng nhập của người dùng (Admin only)

### 🎓 Quản lý sinh viên
- Xem danh sách sinh viên với tìm kiếm nâng cao
- Thêm/Sửa/Xóa thông tin sinh viên
- Sắp xếp sinh viên theo nhiều tiêu chí (tên, GPA, năm học, v.v.)
- Quản lý chứng chỉ sinh viên
- Xem thông tin chi tiết sinh viên

### 📜 Quản lý chứng chỉ
- Thêm/Sửa/Xóa chứng chỉ cho sinh viên
- Upload và lưu trữ file chứng chỉ
- Theo dõi ngày cấp và ngày hết hạn

### 📊 Dashboard
- Tổng quan thống kê hệ thống
- Số lượng người dùng, sinh viên
- Truy cập nhanh đến các chức năng chính

### 📥 Nhập/Xuất dữ liệu
- Import sinh viên từ file CSV
- Export danh sách sinh viên ra CSV
- Import/Export chứng chỉ

## 🔑 Hệ thống phân quyền

Hệ thống hỗ trợ 3 cấp độ phân quyền:

### 👑 Admin
- **Toàn quyền truy cập** hệ thống
- Quản lý người dùng (thêm/sửa/xóa)
- Quản lý sinh viên và chứng chỉ
- Xem lịch sử đăng nhập của tất cả người dùng
- Import/Export dữ liệu

### 👔 Manager
- Xem danh sách người dùng (không xem lịch sử đăng nhập)
- Quản lý sinh viên và chứng chỉ (thêm/sửa/xóa)
- Import/Export dữ liệu
- Cập nhật ảnh đại diện cá nhân

### 👤 Employee
- Xem danh sách người dùng (không xem lịch sử đăng nhập)
- Xem danh sách sinh viên (chỉ đọc)
- Cập nhật ảnh đại diện cá nhân

## 📁 Cấu trúc dự án

```
app/src/main/java/com/example/tdtustudentinformationmanagement/
├── data/
│   ├── firebase/          # Cấu hình Firebase
│   ├── model/             # Data models (User, Student, Certificate)
│   └── repository/        # Repository layer (Auth, User, Student, Storage)
├── di/                    # Dependency Injection modules
├── ui/
│   ├── screens/           # UI Screens (Compose)
│   │   ├── dashboard/
│   │   ├── importexport/
│   │   ├── profile/
│   │   ├── students/
│   │   └── users/
│   ├── theme/             # Material Design theme
│   └── viewmodel/         # ViewModels (MVVM)
└── utils/                 # Utility functions (CSV parsing)
```

## 🚀 Bắt đầu

### Yêu cầu hệ thống
- Android Studio Hedgehog | 2023.1.1 hoặc mới hơn
- JDK 17
- Android SDK 24+ (Android 7.0+)
- Firebase project đã được cấu hình

### Cài đặt

1. **Clone repository**
   ```bash
   git clone https://github.com/iannwendy/TDTUStudentManagementApp.git
   cd TDTUStudentManagementApp
   ```

2. **Cấu hình Firebase**
   - Tạo Firebase project tại [Firebase Console](https://console.firebase.google.com/)
   - Tải file `google-services.json` và đặt vào thư mục `app/`
   - Xem hướng dẫn chi tiết trong [docs/FIREBASE_SETUP.md](docs/FIREBASE_SETUP.md)

3. **Sync và Build**
   - Mở project trong Android Studio
   - Sync project với Gradle files
   - Build và chạy ứng dụng

4. **Đăng nhập**
   - Tài khoản Admin mặc định: `admin@tdtu.edu.vn` / `admin123456`
   - Hoặc tạo tài khoản mới thông qua Firebase Console

## 📚 Tài liệu

Các tài liệu chi tiết được lưu trong thư mục [`docs/`](docs/):

- **[FIREBASE_SETUP.md](docs/FIREBASE_SETUP.md)**: Hướng dẫn cài đặt và cấu hình Firebase
- **[FIREBASE_TROUBLESHOOTING.md](docs/FIREBASE_TROUBLESHOOTING.md)**: Xử lý sự cố Firebase
- **[STORAGE_RULES_FIX.md](docs/STORAGE_RULES_FIX.md)**: Cấu hình Security Rules cho Storage
- **[NETWORK_ERROR_FIX.md](docs/NETWORK_ERROR_FIX.md)**: Xử lý lỗi mạng
- **[DEBUG_LOGIN_ISSUES.md](docs/DEBUG_LOGIN_ISSUES.md)**: Debug các vấn đề đăng nhập
- **[TESTING_GUIDE.md](docs/TESTING_GUIDE.md)**: Hướng dẫn testing

## 🗄️ Cấu trúc Database

### Collections

- **users**: Thông tin người dùng hệ thống
- **students**: Thông tin sinh viên
- **certificates**: Chứng chỉ của sinh viên
- **login_history**: Lịch sử đăng nhập

Xem chi tiết cấu trúc database trong [docs/FIREBASE_SETUP.md](docs/FIREBASE_SETUP.md)

## 🔒 Bảo mật

- Xác thực người dùng qua Firebase Authentication
- Security Rules cho Firestore và Storage
- Phân quyền truy cập theo vai trò
- Chỉ Admin mới có thể xem lịch sử đăng nhập

## 🤝 Đóng góp

Mọi đóng góp đều được chào đón! Vui lòng tạo Issue hoặc Pull Request.

## 📝 License

Dự án này thuộc về TDTU (Trường Đại học Tôn Đức Thắng).

## 👨‍💻 Tác giả

**iannwendy** - [GitHub](https://github.com/iannwendy)

---

**Lưu ý**: Đảm bảo đã cấu hình đúng Firebase project và Security Rules trước khi sử dụng ứng dụng trong môi trường production.
