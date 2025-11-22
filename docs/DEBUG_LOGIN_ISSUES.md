# 🔧 Debug Login Issues - Hướng dẫn khắc phục lỗi đăng nhập

## ✅ **Đã thêm các cải tiến:**

### 1. **Connection Test khi mở app:**
- App sẽ tự động test Firebase connection khi mở
- Hiển thị thông báo: "Đang kiểm tra kết nối Firebase..."
- Kết quả: "✅ Kết nối Firebase thành công" hoặc "❌ Không thể kết nối Firebase"

### 2. **Debug Logging chi tiết:**
- Log tất cả bước trong quá trình login
- Kiểm tra Firebase initialization
- Test Auth và Firestore connection
- Log chi tiết lỗi khi login fail

### 3. **Enhanced Error Handling:**
- Kiểm tra user existence trước khi login
- Auto-create admin account nếu cần
- Better error messages bằng tiếng Việt

## 🔍 **Cách debug lỗi mạng:**

### **Bước 1: Kiểm tra Logs**
```bash
# Mở terminal và chạy:
adb logcat -c && adb logcat | grep -E "(DEBUG|FATAL|AndroidRuntime|TDTU)"
```

### **Bước 2: Kiểm tra Firebase Console**
1. Vào [Firebase Console](https://console.firebase.google.com)
2. Chọn project `androidmidterm-ed4b4`
3. Vào **Authentication > Users**
4. Kiểm tra user `admin@tdtu.edu.vn` có tồn tại không

### **Bước 3: Kiểm tra Sign-in Methods**
1. Vào **Authentication > Sign-in method**
2. Đảm bảo **Email/Password** được enable
3. Kiểm tra **Authorized domains**

### **Bước 4: Test Network Connection**
```bash
# Test Firebase endpoints
ping firebase.googleapis.com
ping firestore.googleapis.com
```

### **Bước 5: Kiểm tra google-services.json**
1. Download file mới từ Firebase Console
2. Thay thế file cũ trong `app/google-services.json`
3. Clean và rebuild project

## 🚨 **Các lỗi thường gặp:**

### **Lỗi 1: "Firebase not initialized properly"**
**Nguyên nhân:** Firebase chưa được khởi tạo đúng cách
**Giải pháp:**
```kotlin
// Kiểm tra trong TDTUApplication.kt
if (FirebaseApp.getApps(this).isEmpty()) {
    FirebaseApp.initializeApp(this)
}
```

### **Lỗi 2: "Network error" hoặc "Timeout"**
**Nguyên nhân:** Không kết nối được đến Firebase servers
**Giải pháp:**
1. Kiểm tra internet connection
2. Tắt firewall tạm thời
3. Thử với VPN khác
4. Kiểm tra DNS settings

### **Lỗi 3: "User not found"**
**Nguyên nhân:** User chưa được tạo trong Firebase Auth
**Giải pháp:**
1. Tạo user trong Firebase Console
2. Hoặc để app tự động tạo admin account

### **Lỗi 4: "Invalid email format"**
**Nguyên nhân:** Email không đúng format
**Giải pháp:**
- Sử dụng: `admin@tdtu.edu.vn`
- Không dùng: `admin@tdtu.edu.vn ` (có space)

## 🔧 **Debug Commands:**

### **Clean và Rebuild:**
```bash
./gradlew clean
./gradlew assembleDebug
```

### **Install APK:**
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### **View Logs:**
```bash
adb logcat -c && adb logcat | grep -E "(DEBUG|FATAL|AndroidRuntime|TDTU)"
```

## 📱 **Test App:**

### **Khi mở app:**
1. Sẽ thấy thông báo: "Đang kiểm tra kết nối Firebase..."
2. Sau đó: "✅ Kết nối Firebase thành công" hoặc "❌ Không thể kết nối Firebase"

### **Khi login:**
1. Nhập email: `admin@tdtu.edu.vn`
2. Nhập password: `admin123456`
3. Xem logs để debug lỗi cụ thể

## 🎯 **Expected Logs:**

### **Successful Connection:**
```
🔍 [DEBUG] Testing Firebase connection...
✅ [DEBUG] Firebase app initialized
✅ [DEBUG] Firebase Auth accessible, current user: none
✅ [DEBUG] Firestore connection successful, got 0 documents
```

### **Successful Login:**
```
🔍 [DEBUG] Attempting login for: admin@tdtu.edu.vn
✅ [DEBUG] Firebase initialized, proceeding with login
🔍 [DEBUG] Sign-in methods for admin@tdtu.edu.vn: [password]
✅ [DEBUG] Login successful for: admin@tdtu.edu.vn
```

### **Failed Login:**
```
❌ [DEBUG] Login failed: The email address is badly formatted
❌ [DEBUG] Exception type: FirebaseAuthInvalidCredentialsException
❌ [DEBUG] Full exception: com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
```

## 🚀 **Next Steps:**

1. **Build và test app**
2. **Xem logs để identify lỗi cụ thể**
3. **Kiểm tra Firebase Console settings**
4. **Test với different network conditions**

---

**Lưu ý:** Nếu vẫn gặp lỗi, hãy copy logs và gửi để được hỗ trợ thêm!
