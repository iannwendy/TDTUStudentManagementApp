# Hướng dẫn cài đặt Firebase cho TDTU Student Information Management

## Bước 1: Tạo Firebase Project

1. Truy cập [Firebase Console](https://console.firebase.google.com/)
2. Nhấn "Create a project" hoặc "Add project"
3. Nhập tên project: `TDTU Student Management`
4. Bật Google Analytics (khuyến nghị)
5. Chọn Analytics account hoặc tạo mới
6. Nhấn "Create project"

## Bước 2: Thêm Android App

1. Trong Firebase Console, nhấn biểu tượng Android
2. Nhập package name: `com.example.tdtustudentinformationmanagement`
3. App nickname: `TDTU Student Management`
4. Debug signing certificate SHA-1 (tùy chọn): Bỏ trống
5. Nhấn "Register app"

## Bước 3: Tải google-services.json

1. Tải file `google-services.json`
2. Thay thế file mẫu trong thư mục `app/` của project
3. Nhấn "Next" và "Continue to console"

## Bước 4: Cấu hình Firebase Authentication

1. Trong Firebase Console, chọn "Authentication" từ menu bên trái
2. Nhấn "Get started"
3. Chọn tab "Sign-in method"
4. Nhấn "Email/Password"
5. Bật "Email/Password" provider
6. Nhấn "Save"

### Tạo Admin Account
1. Trong Authentication > Users, nhấn "Add user"
2. Email: `admin@tdtu.edu.vn`
3. Password: `admin123456`
4. Nhấn "Add user"

## Bước 5: Cấu hình Firestore Database

1. Chọn "Firestore Database" từ menu bên trái
2. Nhấn "Create database"
3. Chọn "Start in test mode" (có thể thay đổi sau)
4. Chọn location gần nhất (ví dụ: asia-southeast1)
5. Nhấn "Done"

### Cấu hình Security Rules
1. Trong Firestore Database, chọn tab "Rules"
2. Thay thế rules mặc định bằng:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users collection
    match /users/{userId} {
      allow read, write: if request.auth != null;
    }
    
    // Students collection
    match /students/{studentId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && 
        (get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role in ['ADMIN', 'MANAGER']);
    }
    
    // Certificates collection
    match /certificates/{certificateId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && 
        (get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role in ['ADMIN', 'MANAGER']);
    }
    
    // Login history collection
    match /login_history/{historyId} {
      allow read, write: if request.auth != null;
    }
  }
}
```

3. Nhấn "Publish"

### Cấu hình Firestore Indexes (Quan trọng cho query chứng chỉ)

Firestore yêu cầu composite index khi query với nhiều điều kiện. Để tạo index tự động:

1. **Cách 1: Sử dụng Firebase CLI (Khuyến nghị)**
   ```bash
   # Cài đặt Firebase CLI nếu chưa có
   npm install -g firebase-tools
   
   # Đăng nhập Firebase
   firebase login
   
   # Deploy indexes
   firebase deploy --only firestore:indexes
   ```

2. **Cách 2: Tạo index thủ công trong Firebase Console**
   - Vào Firestore Database > Indexes tab
   - Nhấn "Create Index"
   - Collection ID: `certificates`
   - Fields:
     - `studentId` (Ascending)
     - `issueDate` (Descending)
   - Nhấn "Create"

3. **Cách 3: Click vào link trong lỗi** (Khi app báo lỗi)
   - App sẽ hiển thị link tạo index tự động
   - Click vào link và nhấn "Create Index"

**Lưu ý**: File `firestore.indexes.json` đã được tạo sẵn trong project root. Bạn có thể deploy bằng Firebase CLI.

## Bước 6: Cấu hình Firebase Storage

1. Chọn "Storage" từ menu bên trái
2. Nhấn "Get started"
3. Chọn "Start in test mode"
4. Chọn location (giống với Firestore)
5. Nhấn "Done"

### Cấu hình Storage Rules
1. Trong Storage, chọn tab "Rules"
2. Thay thế rules mặc định bằng:

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    // Profile pictures - allow users to upload/update their own profile picture
    // Path format: profile_pictures/{userId}.jpg
    match /profile_pictures/{fileName} {
      allow read, write: if request.auth != null && 
        fileName.matches(request.auth.uid + '\\.jpg$');
    }
    match /certificates/{allPaths=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

3. Nhấn "Publish"

## Bước 7: Tạo Admin User trong Firestore

1. Trong Firestore Database, chọn tab "Data"
2. Nhấn "Start collection"
3. Collection ID: `users`
4. Document ID: `admin` (hoặc để tự động)
5. Thêm các fields:

| Field | Type | Value |
|-------|------|-------|
| name | string | Admin |
| age | number | 30 |
| phoneNumber | string | 0123456789 |
| status | string | NORMAL |
| role | string | ADMIN |
| profilePictureUrl | string | (để trống) |
| createdAt | timestamp | (chọn "now") |
| updatedAt | timestamp | (chọn "now") |

6. Nhấn "Save"

## Bước 8: Test ứng dụng

1. Mở Android Studio
2. Sync project với Gradle
3. Build và chạy ứng dụng
4. Đăng nhập với:
   - Email: `admin@tdtu.edu.vn`
   - Password: `admin123456`

## Lưu ý quan trọng

- **Bảo mật**: Thay đổi security rules trước khi deploy production
- **Backup**: Thường xuyên backup dữ liệu Firestore
- **Monitoring**: Sử dụng Firebase Analytics để theo dõi ứng dụng
- **Testing**: Test kỹ các chức năng authentication và database operations

## Troubleshooting

### Lỗi Authentication
- Kiểm tra email/password đúng
- Đảm bảo Authentication đã được enable
- Kiểm tra SHA-1 fingerprint nếu cần

### Lỗi Firestore
- Kiểm tra security rules
- Đảm bảo user đã đăng nhập
- Kiểm tra network connection

### Lỗi Storage
- Kiểm tra storage rules
- Đảm bảo file size không quá lớn
- Kiểm tra file format được hỗ trợ
