# TDTU Student Information Management System

Hệ thống quản lý thông tin sinh viên TDTU sử dụng Firebase Firestore và Android Jetpack Compose.

## Tính năng chính

### Quản lý người dùng
- Đăng nhập hệ thống
- Thay đổi ảnh đại diện
- Xem danh sách người dùng
- Thêm/sửa/xóa người dùng
- Quản lý trạng thái tài khoản (Normal/Locked)
- Xem lịch sử đăng nhập

### Quản lý sinh viên
- Xem danh sách sinh viên
- Thêm/sửa/xóa sinh viên
- Sắp xếp sinh viên theo nhiều tiêu chí
- Tìm kiếm sinh viên
- Quản lý chứng chỉ sinh viên

### Phân quyền người dùng
- **Admin**: Toàn quyền truy cập
- **Manager**: Quản lý sinh viên và xem thông tin người dùng
- **Employee**: Chỉ xem thông tin

## Cài đặt Firebase

### Bước 1: Tạo Firebase Project
1. Truy cập [Firebase Console](https://console.firebase.google.com/)
2. Tạo project mới với tên "TDTU Student Management"
3. Bật Google Analytics (tùy chọn)

### Bước 2: Thêm Android App
1. Trong Firebase Console, chọn "Add app" > Android
2. Nhập package name: `com.example.tdtustudentinformationmanagement`
3. Tải file `google-services.json` và thay thế file mẫu trong thư mục `app/`

### Bước 3: Cấu hình Firebase Services
1. **Authentication**:
   - Vào Authentication > Sign-in method
   - Bật Email/Password
   - Tạo admin account: `admin@tdtu.edu.vn` với password `admin123456`

2. **Firestore Database**:
   - Vào Firestore Database
   - Tạo database ở chế độ test mode
   - Cấu hình security rules:

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

3. **Storage**:
   - Vào Storage
   - Tạo bucket mặc định
   - Cấu hình security rules:

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

### Bước 4: Cấu trúc Database

#### Users Collection
```json
{
  "id": "user_id",
  "name": "Tên người dùng",
  "age": 25,
  "phoneNumber": "0123456789",
  "status": "NORMAL",
  "role": "ADMIN",
  "profilePictureUrl": "url_to_image",
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

#### Students Collection
```json
{
  "id": "student_id",
  "studentId": "SV001",
  "name": "Tên sinh viên",
  "dateOfBirth": "timestamp",
  "gender": "MALE",
  "address": "Địa chỉ",
  "phoneNumber": "0123456789",
  "email": "email@example.com",
  "major": "Công nghệ thông tin",
  "yearOfStudy": 3,
  "gpa": 3.5,
  "status": "ACTIVE",
  "profilePictureUrl": "url_to_image",
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

#### Certificates Collection
```json
{
  "id": "certificate_id",
  "studentId": "student_id",
  "name": "Tên chứng chỉ",
  "issuingOrganization": "Tổ chức cấp",
  "issueDate": "timestamp",
  "expiryDate": "timestamp",
  "certificateUrl": "url_to_certificate",
  "description": "Mô tả",
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

## Chạy ứng dụng

1. Sync project với Gradle
2. Build và chạy ứng dụng
3. Đăng nhập với admin account: `admin@tdtu.edu.vn` / `admin123456`

## Lưu ý quan trọng

- File `google-services.json` mẫu chỉ để tham khảo, cần thay thế bằng file thật từ Firebase Console
- Đảm bảo package name trong Firebase Console khớp với package name trong ứng dụng
- Cấu hình security rules phù hợp với yêu cầu bảo mật của hệ thống
- Test kỹ các chức năng authentication và database operations trước khi deploy
