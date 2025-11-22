# Troubleshooting Firebase Authentication Issues

## 🔧 Đã sửa các vấn đề:

### 1. **Icon Password Visibility**
- **Trước:** Icon 3 chấm (MoreVert) không phù hợp
- **Sau:** Icon Check/Close để toggle password visibility
- **Cải thiện:** UX tốt hơn, người dùng hiểu rõ chức năng

### 2. **Error Message Localization**
- **Trước:** Error message bằng tiếng Anh khó hiểu
- **Sau:** Error message bằng tiếng Việt rõ ràng
- **Các loại lỗi được xử lý:**
  - Network timeout → "Lỗi kết nối mạng. Vui lòng kiểm tra internet và thử lại."
  - Invalid email → "Email không hợp lệ. Vui lòng kiểm tra lại."
  - User not found → "Tài khoản không tồn tại. Vui lòng kiểm tra email."
  - Wrong password → "Mật khẩu không đúng. Vui lòng thử lại."
  - Too many requests → "Quá nhiều lần thử đăng nhập. Vui lòng đợi và thử lại sau."

### 3. **Auto-create Admin Account**
- **Tính năng mới:** Tự động tạo admin account nếu chưa tồn tại
- **Logic:** Nếu đăng nhập admin@tdtu.edu.vn thất bại, sẽ tự động tạo account
- **Lợi ích:** Không cần tạo account thủ công trong Firebase Console

## 🚨 Các lỗi thường gặp và cách khắc phục:

### 1. **Network Error (Lỗi kết nối mạng)**
```
Error: "A network error (such as timeout, interrupted connection or unreachable host) has occurred"
```

**Nguyên nhân:**
- Firebase project chưa được cấu hình đúng
- google-services.json không đúng hoặc thiếu
- Internet connection không ổn định
- Firebase services chưa được enable

**Cách khắc phục:**
1. Kiểm tra Firebase Console:
   - Project có tồn tại không
   - Authentication có được enable không
   - Sign-in method có Email/Password không

2. Kiểm tra google-services.json:
   - File có trong thư mục app/ không
   - Package name có đúng không
   - API keys có hợp lệ không

3. Kiểm tra network:
   - Internet connection
   - Firewall settings
   - Proxy settings

### 2. **Invalid Email Error**
```
Error: "The email address is badly formatted"
```

**Nguyên nhân:**
- Email không đúng format
- Email có ký tự đặc biệt

**Cách khắc phục:**
- Kiểm tra format email: user@domain.com
- Không có khoảng trắng
- Không có ký tự đặc biệt

### 3. **User Not Found Error**
```
Error: "There is no user record corresponding to this identifier"
```

**Nguyên nhân:**
- Email chưa được đăng ký trong Firebase
- Email bị xóa khỏi Firebase

**Cách khắc phục:**
- Tạo user trong Firebase Console
- Hoặc sử dụng tính năng auto-create admin account

### 4. **Wrong Password Error**
```
Error: "The password is invalid or the user does not have a password"
```

**Nguyên nhân:**
- Password không đúng
- User chưa set password

**Cách khắc phục:**
- Kiểm tra password
- Reset password trong Firebase Console

### 5. **Too Many Requests Error**
```
Error: "Too many unsuccessful login attempts"
```

**Nguyên nhân:**
- Quá nhiều lần thử đăng nhập sai
- Firebase rate limiting

**Cách khắc phục:**
- Đợi 15-30 phút
- Kiểm tra lại email/password
- Clear app data nếu cần

## 🔍 Debug Steps:

### 1. **Kiểm tra Firebase Console**
```
1. Mở Firebase Console
2. Chọn project
3. Authentication > Users
4. Kiểm tra có admin@tdtu.edu.vn không
5. Nếu không có, tạo mới
```

### 2. **Kiểm tra google-services.json**
```json
{
  "project_info": {
    "project_number": "6792916557",
    "project_id": "androidmidterm-ed4b4"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "1:6792916557:android:26ac34f837a7c15b86f604",
        "android_client_info": {
          "package_name": "com.example.tdtustudentinformationmanagement"
        }
      }
    }
  ]
}
```

### 3. **Kiểm tra Logs**
```bash
# Android Studio Logcat
adb logcat | grep -E "(Firebase|Auth|TDTU)"

# Hoặc filter theo tag
adb logcat FirebaseAuth:V *:S
```

### 4. **Test Firebase Connection**
```kotlin
// Thêm vào AuthRepository để test
suspend fun testFirebaseConnection(): Boolean {
    return try {
        firebaseConfig.auth.signInAnonymously().await()
        true
    } catch (e: Exception) {
        false
    }
}
```

## 📱 Testing Steps:

### 1. **Test với Admin Account**
```
Email: admin@tdtu.edu.vn
Password: admin123456
Expected: Đăng nhập thành công hoặc tự động tạo account
```

### 2. **Test với Account không tồn tại**
```
Email: test@example.com
Password: wrongpassword
Expected: "Tài khoản không tồn tại"
```

### 3. **Test với Password sai**
```
Email: admin@tdtu.edu.vn
Password: wrongpassword
Expected: "Mật khẩu không đúng"
```

### 4. **Test với Email không hợp lệ**
```
Email: invalid-email
Password: admin123456
Expected: "Email không hợp lệ"
```

## 🛠️ Advanced Troubleshooting:

### 1. **Enable Firebase Debug Logging**
```kotlin
// Trong Application class
FirebaseApp.getInstance().setLogLevel(LogLevel.DEBUG)
```

### 2. **Check Firebase Rules**
```javascript
// Firestore Rules
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null;
    }
  }
}
```

### 3. **Verify SHA-1 Fingerprint**
```bash
# Debug keystore
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android

# Release keystore
keytool -list -v -keystore your-release-key.keystore -alias your-key-alias
```

## ✅ Checklist trước khi deploy:

- [ ] Firebase project đã được tạo
- [ ] Authentication đã được enable
- [ ] Email/Password sign-in method đã được enable
- [ ] google-services.json đã được thêm vào project
- [ ] Package name khớp với Firebase Console
- [ ] SHA-1 fingerprint đã được thêm vào Firebase Console
- [ ] Admin account đã được tạo hoặc auto-create hoạt động
- [ ] Error messages đã được localize
- [ ] App đã được test với các scenarios khác nhau

## 📞 Support:

Nếu vẫn gặp vấn đề, hãy:
1. Kiểm tra Firebase Console logs
2. Kiểm tra Android Studio Logcat
3. Test với Firebase Test Lab
4. Liên hệ Firebase Support
