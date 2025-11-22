package com.example.tdtustudentinformationmanagement.data.firebase

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseConfig @Inject constructor() {
    
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    
    companion object {
        // Collection names
        const val USERS_COLLECTION = "users"
        const val STUDENTS_COLLECTION = "students"
        const val CERTIFICATES_COLLECTION = "certificates"
        const val LOGIN_HISTORY_COLLECTION = "login_history"
        
        // Default admin user
        const val DEFAULT_ADMIN_EMAIL = "admin@tdtu.edu.vn"
        const val DEFAULT_ADMIN_PASSWORD = "admin123456"
    }
}
