# TDTU Student Information Management System

A modern Android application built with Jetpack Compose and Firebase for managing student and user information at TDTU.

## Overview

TDTU Student Information Management System is a comprehensive Android application for managing student and user information, designed to support university management operations. The system provides user management, student management, certificate tracking, and flexible access control features.

## Architecture & Technology

### Architecture
- **MVVM (Model-View-ViewModel)**: Separation of business logic and UI
- **Repository Pattern**: Centralized data access management
- **Dependency Injection**: Using Hilt for dependency management

### Technologies Used
- **UI Framework**: Jetpack Compose - Modern Android UI toolkit
- **Backend**: Firebase
  - **Firebase Authentication**: User authentication
  - **Cloud Firestore**: Real-time NoSQL database
  - **Firebase Storage**: File storage (profile pictures, certificates)
- **Dependency Injection**: Hilt (Dagger)
- **Asynchronous**: Kotlin Coroutines & Flow
- **Image Loading**: Coil

## Key Features

### Authentication & Security
- Login/Logout with Email/Password
- Session management
- Login history tracking (Admin only)
- Account status management (Normal/Locked)

### User Management
- View user list with search and filter
- Add/Edit/Delete users (Admin only)
- Manage user roles (Admin/Manager/Employee)
- Update profile picture
- View user login history (Admin only)

### Student Management
- View student list with advanced search
- Add/Edit/Delete student information
- Sort students by multiple criteria (name, GPA, year of study, etc.)
- Manage student certificates
- View detailed student information

### Certificate Management
- Add/Edit/Delete certificates for students
- Upload and store certificate files
- Track issue date and expiry date

### Dashboard
- System statistics overview
- User and student counts
- Quick access to main features

### Data Import/Export
- Import students from CSV file
- Export student list to CSV
- Import/Export certificates

## Access Control System

The system supports 3 levels of access control:

### Admin
- **Full system access**
- User management (add/edit/delete)
- Student and certificate management
- View login history of all users
- Import/Export data

### Manager
- View user list (cannot view login history)
- Manage students and certificates (add/edit/delete)
- Import/Export data
- Update personal profile picture

### Employee
- View user list (cannot view login history)
- View student list (read-only)
- Update personal profile picture

## Project Structure

```
app/src/main/java/com/example/tdtustudentinformationmanagement/
├── data/
│   ├── firebase/          # Firebase configuration
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

## Getting Started

### System Requirements
- Android Studio Hedgehog | 2023.1.1 or newer
- JDK 17
- Android SDK 24+ (Android 7.0+)
- Configured Firebase project

### Installation

1. **Clone repository**
   ```bash
   git clone https://github.com/iannwendy/TDTUStudentManagementApp.git
   cd TDTUStudentManagementApp
   ```

2. **Configure Firebase**
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Download `google-services.json` file and place it in the `app/` folder
   - See detailed instructions in [docs/FIREBASE_SETUP.md](docs/FIREBASE_SETUP.md)

3. **Sync and Build**
   - Open the project in Android Studio
   - Sync project with Gradle files
   - Build and run the application

4. **Login**
   - Default Admin account: `admin@tdtu.edu.vn` / `admin123456`
   - Or create a new account through Firebase Console

## Documentation

Detailed documentation is stored in the [`docs/`](docs/) folder:

- **[FIREBASE_SETUP.md](docs/FIREBASE_SETUP.md)**: Firebase installation and configuration guide
- **[FIREBASE_TROUBLESHOOTING.md](docs/FIREBASE_TROUBLESHOOTING.md)**: Firebase troubleshooting
- **[STORAGE_RULES_FIX.md](docs/STORAGE_RULES_FIX.md)**: Storage Security Rules configuration
- **[NETWORK_ERROR_FIX.md](docs/NETWORK_ERROR_FIX.md)**: Network error handling
- **[DEBUG_LOGIN_ISSUES.md](docs/DEBUG_LOGIN_ISSUES.md)**: Debug login issues
- **[TESTING_GUIDE.md](docs/TESTING_GUIDE.md)**: Testing guide

## Database Structure

### Collections

- **users**: System user information
- **students**: Student information
- **certificates**: Student certificates
- **login_history**: Login history

See detailed database structure in [docs/FIREBASE_SETUP.md](docs/FIREBASE_SETUP.md)

## Security

- User authentication via Firebase Authentication
- Security Rules for Firestore and Storage
- Role-based access control
- Only Admin can view login history

## Contributing

Contributions are welcome! Please create an Issue or Pull Request.

## License

This project belongs to TDTU (Ton Duc Thang University).

## Author

**iannwendy** - [GitHub](https://github.com/iannwendy)

---

**Note**: Ensure that Firebase project and Security Rules are properly configured before using the application in a production environment.
