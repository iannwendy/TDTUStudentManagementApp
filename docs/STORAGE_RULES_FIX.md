# Sửa lỗi quyền upload ảnh đại diện

## Vấn đề
Lỗi "User does not have permission to access this object" khi upload ảnh đại diện cho cả 3 roles (ADMIN, MANAGER, EMPLOYEE).

## Nguyên nhân
Firebase Storage security rules hiện tại khớp pattern `profile_pictures/{userId}`, nhưng code upload file với đường dẫn `profile_pictures/{userId}.jpg`. Pattern không khớp nên bị từ chối quyền.

## Giải pháp

### Bước 1: Mở Firebase Console
1. Truy cập [Firebase Console](https://console.firebase.google.com/)
2. Chọn project của bạn
3. Vào **Storage** > **Rules**

### Bước 2: Cập nhật Storage Rules
Thay thế rules hiện tại bằng:

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

### Bước 3: Publish Rules
1. Nhấn nút **Publish** để lưu rules mới
2. Đợi vài giây để rules được áp dụng

### Bước 4: Test lại
1. Đăng nhập với bất kỳ role nào (ADMIN, MANAGER, hoặc EMPLOYEE)
2. Thử upload ảnh đại diện
3. Lỗi quyền sẽ được giải quyết

## Giải thích
- Rule mới sử dụng pattern `{fileName}` để khớp với tên file đầy đủ (bao gồm `.jpg`)
- Sử dụng regex `fileName.matches(request.auth.uid + '\\.jpg$')` để đảm bảo:
  - File name phải bắt đầu bằng `userId` của user đang đăng nhập
  - File name phải kết thúc bằng `.jpg`
  - Chỉ user đó mới có quyền upload/update ảnh đại diện của chính họ

## Lưu ý
- Rules này cho phép tất cả 3 roles (ADMIN, MANAGER, EMPLOYEE) upload ảnh đại diện của chính họ
- Mỗi user chỉ có thể upload/update ảnh đại diện của chính mình (dựa trên `request.auth.uid`)
- Rules sẽ tự động áp dụng sau khi publish

