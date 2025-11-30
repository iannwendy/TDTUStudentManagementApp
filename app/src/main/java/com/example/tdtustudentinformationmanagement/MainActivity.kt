package com.example.tdtustudentinformationmanagement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.example.tdtustudentinformationmanagement.data.repository.AuthRepository
import com.example.tdtustudentinformationmanagement.ui.screens.LoginScreen
import com.example.tdtustudentinformationmanagement.ui.screens.MainScreen
import com.example.tdtustudentinformationmanagement.ui.theme.TDTUStudentInformationManagementTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TDTUStudentInformationManagementTheme {
                AppContent(authRepository)
            }
        }
    }
}

@Composable
fun AppContent(authRepository: AuthRepository) {
    val coroutineScope = rememberCoroutineScope()
    
    // Observe auth state changes using Firebase Auth State Listener
    val authState = remember {
        authRepository.observeAuthState()
            .stateIn(
                scope = coroutineScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = authRepository.getCurrentUser()
            )
    }
    
    val currentUser by authState.collectAsState()
    val isLoggedIn = currentUser != null
    
    if (isLoggedIn) {
        MainScreen(
            onLogout = {
                // onLogout will be called after logout completes in MainScreen
                // Auth state will automatically update via the listener
            }
        )
    } else {
        LoginScreen(
            onLoginSuccess = {
                // Login success - auth state will automatically update via the listener
                // But status check happens in AuthRepository before returning success
                // If account is locked, signOut is called and auth state will be null
            }
        )
    }
}