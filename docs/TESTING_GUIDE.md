# Hướng dẫn Test App TDTU Student Information Management

## Cài đặt và Chạy App

1. **Build và Install APK:**
   ```bash
   ./gradlew assembleDebug
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

2. **Chạy App:**
   - Mở app từ launcher
   - App sẽ hiển thị màn hình đăng nhập

## Test Cases

### 1. Test Đăng nhập Admin
- **Email:** `admin@tdtu.edu.vn`
- **Password:** `admin123456`
- **Expected:** Đăng nhập thành công, hiển thị MainScreen với role ADMIN

### 2. Test Đăng nhập với tài khoản không tồn tại
- **Email:** `test@example.com`
- **Password:** `wrongpassword`
- **Expected:** Hiển thị lỗi "Login failed"

### 3. Test Đăng nhập với password sai
- **Email:** `admin@tdtu.edu.vn`
- **Password:** `wrongpassword`
- **Expected:** Hiển thị lỗi authentication

### 4. Test Logout
- Sau khi đăng nhập thành công
- Nhấn nút "Logout"
- **Expected:** Quay về màn hình đăng nhập

## Troubleshooting

### App bị crash khi mở
1. **Kiểm tra log:**
   ```bash
   adb logcat | grep -E "(FATAL|AndroidRuntime|TDTU)"
   ```

2. **Các nguyên nhân thường gặp:**
   - Firebase chưa được cấu hình đúng
   - google-services.json không đúng
   - Hilt dependency injection lỗi
   - Permissions không đủ

### Lỗi Firebase Authentication
1. **Kiểm tra Firebase Console:**
   - Authentication > Users có admin account không
   - Sign-in method có Email/Password không

2. **Kiểm tra Security Rules:**
   - Firestore rules cho phép đọc/ghi
   - Storage rules cho phép upload

### Lỗi Network
1. **Kiểm tra kết nối internet**
2. **Kiểm tra Firebase project settings**
3. **Kiểm tra API keys trong google-services.json**

## Cấu trúc Database Test

### Tạo Admin User trong Firestore
1. Mở Firebase Console > Firestore Database
2. Tạo collection `users`
3. Thêm document với ID là UID của admin user từ Authentication
4. Fields:
   ```json
   {
     "name": "Admin",
     "age": 30,
     "phoneNumber": "0123456789",
     "status": "NORMAL",
     "role": "ADMIN",
     "profilePictureUrl": "",
     "createdAt": "timestamp",
     "updatedAt": "timestamp"
   }
   ```

## Debug Tips

1. **Enable Debug Logging:**
   ```kotlin
   FirebaseApp.getInstance().setLogLevel(LogLevel.DEBUG)
   ```

2. **Kiểm tra Firebase Initialization:**
   - Đảm bảo FirebaseApp.initializeApp() được gọi
   - Kiểm tra google-services.json có đúng package name

3. **Test từng component:**
   - Test Firebase Auth riêng
   - Test Firestore connection riêng
   - Test UI components riêng

## Performance Testing

1. **Test với nhiều users:**
   - Tạo nhiều test accounts
   - Test concurrent login

2. **Test với dữ liệu lớn:**
   - Thêm nhiều students
   - Test search và filter

3. **Test offline:**
   - Tắt internet
   - Kiểm tra app behavior

## Security Testing

1. **Test SQL Injection:**
   - Thử input đặc biệt trong search

2. **Test Authentication Bypass:**
   - Thử truy cập protected routes

3. **Test Data Validation:**
   - Input validation
   - File upload validation
