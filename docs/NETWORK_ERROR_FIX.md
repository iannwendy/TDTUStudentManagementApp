# Khắc phục lỗi mạng Firebase - Step by Step

## 🚨 Lỗi hiện tại: "Lỗi kết nối mạng. Vui lòng kiểm tra internet và thử lại."

## 🔍 Nguyên nhân có thể:

### 1. **Firebase Project chưa được cấu hình đúng**
- Authentication chưa được enable
- Sign-in method chưa được thiết lập
- Project ID không đúng

### 2. **google-services.json không đúng**
- File không đúng project
- Package name không khớp
- API keys không hợp lệ

### 3. **Network/Firewall issues**
- Internet connection không ổn định
- Firewall chặn Firebase requests
- Proxy settings

## 🛠️ Cách khắc phục từng bước:

### **Bước 1: Kiểm tra Firebase Console**

1. **Mở Firebase Console:**
   - Truy cập: https://console.firebase.google.com/
   - Đăng nhập với Google account

2. **Kiểm tra Project:**
   - Project ID: `androidmidterm-ed4b4`
   - Project Number: `6792916557`
   - Storage Bucket: `androidmidterm-ed4b4.firebasestorage.app`

3. **Enable Authentication:**
   ```
   Firebase Console > Authentication > Get Started
   > Sign-in method > Email/Password > Enable
   ```

4. **Tạo Admin User:**
   ```
   Authentication > Users > Add User
   Email: admin@tdtu.edu.vn
   Password: admin123456
   ```

### **Bước 2: Kiểm tra google-services.json**

1. **Download file mới:**
   ```
   Firebase Console > Project Settings > General
   > Your apps > Android app > Download google-services.json
   ```

2. **Thay thế file cũ:**
   ```bash
   # Backup file cũ
   cp app/google-services.json app/google-services.json.backup
   
   # Copy file mới
   cp ~/Downloads/google-services.json app/google-services.json
   ```

3. **Kiểm tra nội dung file:**
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

### **Bước 3: Kiểm tra Network**

1. **Test Internet connection:**
   ```bash
   ping google.com
   ping firebase.google.com
   ```

2. **Kiểm tra Firewall:**
   - Tắt Windows Firewall tạm thời
   - Kiểm tra Antivirus settings
   - Disable VPN nếu có

3. **Test Firebase endpoints:**
   ```bash
   curl -I https://firebase.googleapis.com
   curl -I https://identitytoolkit.googleapis.com
   ```

### **Bước 4: Clean và Rebuild**

1. **Clean project:**
   ```bash
   ./gradlew clean
   ```

2. **Rebuild:**
   ```bash
   ./gradlew assembleDebug
   ```

3. **Reinstall app:**
   ```bash
   adb uninstall com.example.tdtustudentinformationmanagement
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

### **Bước 5: Debug với Logs**

1. **Enable Firebase Debug Logging:**
   ```kotlin
   // Trong TDTUApplication.kt
   FirebaseApp.getInstance().setLogLevel(LogLevel.DEBUG)
   ```

2. **Kiểm tra Logcat:**
   ```bash
   # Android Studio > Logcat
   # Filter: Firebase, Auth, TDTU
   ```

3. **Test Firebase Connection:**
   - App sẽ test connection trước khi đăng nhập
   - Nếu fail, sẽ hiển thị "Không thể kết nối đến Firebase"

## 🔧 Advanced Troubleshooting:

### **1. Kiểm tra SHA-1 Fingerprint**

```bash
# Debug keystore
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android

# Thêm SHA-1 vào Firebase Console:
# Project Settings > General > Your apps > Android app > Add fingerprint
```

### **2. Kiểm tra Firebase Rules**

```javascript
// Firestore Rules
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

### **3. Test với Firebase Test Lab**

```bash
# Upload APK to Firebase Test Lab
gcloud firebase test android run --app app-debug.apk --device model=Pixel2,version=28
```

## 📱 Test Cases:

### **Test 1: Basic Connection**
```
1. Mở app
2. Nhập email: admin@tdtu.edu.vn
3. Nhập password: admin123456
4. Nhấn Sign In
5. Expected: Test connection trước, sau đó đăng nhập
```

### **Test 2: Network Error**
```
1. Tắt internet
2. Thử đăng nhập
3. Expected: "Không thể kết nối đến Firebase"
```

### **Test 3: Invalid Credentials**
```
1. Nhập email sai: test@example.com
2. Nhập password: wrongpassword
3. Expected: "Tài khoản không tồn tại"
```

## 🚀 Quick Fix Commands:

```bash
# 1. Clean và rebuild
./gradlew clean && ./gradlew assembleDebug

# 2. Reinstall app
adb uninstall com.example.tdtustudentinformationmanagement
adb install app/build/outputs/apk/debug/app-debug.apk

# 3. Clear app data
adb shell pm clear com.example.tdtustudentinformationmanagement

# 4. Check logs
adb logcat | grep -E "(Firebase|Auth|TDTU)"
```

## 📞 Nếu vẫn không được:

1. **Kiểm tra Firebase Console logs**
2. **Kiểm tra Android Studio Logcat**
3. **Test với Firebase Test Lab**
4. **Liên hệ Firebase Support**

## ✅ Checklist hoàn thành:

- [ ] Firebase Console đã được cấu hình
- [ ] Authentication đã được enable
- [ ] Email/Password sign-in đã được enable
- [ ] Admin user đã được tạo
- [ ] google-services.json đã được cập nhật
- [ ] Package name khớp với Firebase
- [ ] SHA-1 fingerprint đã được thêm
- [ ] Network connection ổn định
- [ ] App đã được clean và rebuild
- [ ] Test với các scenarios khác nhau
