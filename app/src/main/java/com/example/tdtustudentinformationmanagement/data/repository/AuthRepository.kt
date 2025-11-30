package com.example.tdtustudentinformationmanagement.data.repository

import com.example.tdtustudentinformationmanagement.data.firebase.FirebaseConfig
import com.example.tdtustudentinformationmanagement.data.model.LoginHistory
import com.example.tdtustudentinformationmanagement.data.model.User
import com.example.tdtustudentinformationmanagement.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseConfig: FirebaseConfig,
    private val userRepository: UserRepository
) {
    
    suspend fun signIn(email: String, password: String): Result<FirebaseUser?> {
        return try {
            println("🔍 [DEBUG] Attempting login for: $email")
            
            // Test Firebase connection first
            if (firebaseConfig.auth.app == null) {
                println("❌ [DEBUG] Firebase not initialized properly")
                return Result.failure(Exception("Firebase not initialized properly"))
            }
            println("✅ [DEBUG] Firebase initialized, proceeding with login")

            // Check if user exists first
            try {
                val userMethods = firebaseConfig.auth.fetchSignInMethodsForEmail(email).await()
                println("🔍 [DEBUG] Sign-in methods for $email: ${userMethods.signInMethods}")
            } catch (e: Exception) {
                println("⚠️ [DEBUG] Could not fetch sign-in methods: ${e.message}")
            }
            
            val result = firebaseConfig.auth.signInWithEmailAndPassword(email, password).await()
            println("✅ [DEBUG] Firebase Auth login successful for: $email")
            
            // Check if user account is locked BEFORE ensuring user document
            // This prevents creating a new user document with NORMAL status if user is locked
            result.user?.let { firebaseUser ->
                val userDataResult = userRepository.getUserById(firebaseUser.uid)
                if (userDataResult.isSuccess) {
                    val userData = userDataResult.getOrNull()
                    if (userData != null) {
                        // User document exists, check status
                        if (userData.status == com.example.tdtustudentinformationmanagement.data.model.UserStatus.LOCKED) {
                            // Account is locked, sign out immediately BEFORE returning error
                            println("❌ [DEBUG] Account is locked for: $email - signing out immediately")
                            try {
                                firebaseConfig.auth.signOut()
                                println("✅ [DEBUG] Signed out locked account")
                                // Longer delay to ensure signOut completes and error is processed
                                // before auth state change propagates to MainActivity
                                // This gives LoginViewModel time to process the error
                                delay(300)
                            } catch (signOutError: Exception) {
                                println("⚠️ [DEBUG] Error during signOut: ${signOutError.message}")
                            }
                            return Result.failure(Exception("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên."))
                        }
                    }
                } else {
                    // If we can't fetch user data, log warning but don't block login
                    // (user document might not exist yet, will be created by ensureUserDocument)
                    println("⚠️ [DEBUG] Could not fetch user data for ${firebaseUser.uid}: ${userDataResult.exceptionOrNull()?.message}")
                }
                
                // Only ensure user document if account is not locked
                ensureUserDocument(firebaseUser)
                
                // Add login history only if account is not locked
                val loginHistory = LoginHistory(
                    userId = firebaseUser.uid,
                    deviceInfo = "Android Device", // You can get more detailed device info
                    ipAddress = "Unknown" // You can get IP address if needed
                )
                userRepository.addLoginHistory(loginHistory)
            }
            
            Result.success(result.user)
        } catch (e: Exception) {
            println("❌ [DEBUG] Login failed: ${e.message}")
            println("❌ [DEBUG] Exception type: ${e.javaClass.simpleName}")
            println("❌ [DEBUG] Full exception: $e")
            
            // If login fails and it's the default admin account, try to create it
            if (email == FirebaseConfig.DEFAULT_ADMIN_EMAIL) {
                println("🔍 [DEBUG] Attempting to create admin account...")
                return try {
                    val createResult = firebaseConfig.auth.createUserWithEmailAndPassword(email, password).await()
                    println("✅ [DEBUG] Admin account created successfully")
                    
                    // Create admin user document in Firestore
                    createResult.user?.let { user ->
                        val adminUser = User(
                            id = user.uid,
                            name = "Admin",
                            email = FirebaseConfig.DEFAULT_ADMIN_EMAIL,
                            age = 30,
                            phoneNumber = "0123456789",
                            status = com.example.tdtustudentinformationmanagement.data.model.UserStatus.NORMAL,
                            role = UserRole.ADMIN,
                            profilePictureUrl = "",
                            createdAt = com.google.firebase.Timestamp.now(),
                            updatedAt = com.google.firebase.Timestamp.now()
                        )
                        userRepository.createUser(adminUser)
                        println("✅ [DEBUG] Admin user document created in Firestore")
                        
                        // Check status after creating (should be NORMAL, but check for consistency)
                        val userDataResult = userRepository.getUserById(user.uid)
                        if (userDataResult.isSuccess) {
                            val userData = userDataResult.getOrNull()
                            if (userData != null && userData.status == com.example.tdtustudentinformationmanagement.data.model.UserStatus.LOCKED) {
                                println("❌ [DEBUG] Newly created admin account is locked (unexpected)")
                                firebaseConfig.auth.signOut()
                                return Result.failure(Exception("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên."))
                            }
                        }
                    }
                    
                    Result.success(createResult.user)
                } catch (createException: Exception) {
                    println("❌ [DEBUG] Failed to create admin account: ${createException.message}")
                    Result.failure(createException)
                }
            }
            Result.failure(e)
        }
    }

    private suspend fun ensureUserDocument(firebaseUser: FirebaseUser) {
        try {
            val userDoc = userRepository.getUserById(firebaseUser.uid)
            if (userDoc.isSuccess && userDoc.getOrNull() == null) {
                val defaultRole = if (firebaseUser.email == FirebaseConfig.DEFAULT_ADMIN_EMAIL) {
                    UserRole.ADMIN
                } else {
                    UserRole.EMPLOYEE
                }
                val newUser = User(
                    id = firebaseUser.uid,
                    name = firebaseUser.displayName ?: firebaseUser.email ?: "User",
                    email = firebaseUser.email ?: "",
                    phoneNumber = firebaseUser.phoneNumber ?: "",
                    role = defaultRole
                )
                userRepository.createUser(newUser)
                println("✅ [DEBUG] Created missing user document for ${firebaseUser.email}")
            }
        } catch (e: Exception) {
            println("❌ [DEBUG] Failed to ensure user document: ${e.message}")
        }
    }
    
    suspend fun signOut(): Result<Unit> {
        return try {
            firebaseConfig.auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getCurrentUser(): FirebaseUser? {
        return firebaseConfig.auth.currentUser
    }
    
    suspend fun getCurrentUserData(): Result<User?> {
        return try {
            val currentUser = getCurrentUser()
            if (currentUser != null) {
                userRepository.getUserById(currentUser.uid)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createUserAccount(email: String, password: String, userData: User): Result<FirebaseUser?> {
        return try {
            // Create Firebase Auth user
            val authResult = firebaseConfig.auth.createUserWithEmailAndPassword(email, password).await()
            
            // Create user document in Firestore
            authResult.user?.let { user ->
                val userWithId = userData.copy(id = user.uid)
                userRepository.createUser(userWithId)
            }
            
            Result.success(authResult.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updatePassword(newPassword: String): Result<Unit> {
        return try {
            val currentUser = getCurrentUser()
            if (currentUser != null) {
                currentUser.updatePassword(newPassword).await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("No user logged in"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            firebaseConfig.auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun isUserAdmin(): Boolean {
        val currentUser = getCurrentUser()
        return currentUser?.email == FirebaseConfig.DEFAULT_ADMIN_EMAIL
    }
    
    suspend fun testFirebaseConnection(): Result<Boolean> {
        return try {
            println("🔍 [DEBUG] Testing Firebase connection...")
            
            // Test if Firebase is properly initialized
            if (firebaseConfig.auth.app == null) {
                println("❌ [DEBUG] Firebase app not initialized")
                return Result.failure(Exception("Firebase app not initialized"))
            }
            println("✅ [DEBUG] Firebase app initialized")

            // Test Auth connection
            try {
                val currentUser = firebaseConfig.auth.currentUser
                println("✅ [DEBUG] Firebase Auth accessible, current user: ${currentUser?.email ?: "none"}")
            } catch (e: Exception) {
                println("❌ [DEBUG] Firebase Auth error: ${e.message}")
                return Result.failure(e)
            }

            // Test Firestore connection
            try {
                println("🔍 [DEBUG] Testing Firestore connection...")
                val testDoc = firebaseConfig.firestore.collection("test").limit(1).get().await()
                println("✅ [DEBUG] Firestore connection successful, got ${testDoc.size()} documents")
                Result.success(true)
            } catch (e: Exception) {
                println("❌ [DEBUG] Firestore connection failed: ${e.message}")
                // Don't fail completely if Firestore fails, Auth might still work
                Result.success(true)
            }
        } catch (e: Exception) {
            println("❌ [DEBUG] Firebase connection test failed: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun checkUserRole(): Result<UserRole> {
        return try {
            val userData = getCurrentUserData()
            when {
                userData.isSuccess && userData.getOrNull() != null -> {
                    Result.success(userData.getOrNull()!!.role)
                }
                isUserAdmin() -> {
                    Result.success(UserRole.ADMIN)
                }
                else -> {
                    Result.success(UserRole.EMPLOYEE)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseConfig.auth.addAuthStateListener(listener)
        awaitClose {
            firebaseConfig.auth.removeAuthStateListener(listener)
        }
    }
}
