package com.example.tdtustudentinformationmanagement.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import com.example.tdtustudentinformationmanagement.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tdtustudentinformationmanagement.data.model.User
import com.example.tdtustudentinformationmanagement.data.model.UserRole
import com.example.tdtustudentinformationmanagement.ui.screens.dashboard.DashboardScreen
import com.example.tdtustudentinformationmanagement.ui.screens.importexport.ImportExportScreen
import com.example.tdtustudentinformationmanagement.ui.screens.profile.ProfileScreen
import com.example.tdtustudentinformationmanagement.ui.screens.students.StudentsScreen
import com.example.tdtustudentinformationmanagement.ui.screens.users.UsersScreen
import com.example.tdtustudentinformationmanagement.ui.viewmodel.ImportExportViewModel
import com.example.tdtustudentinformationmanagement.ui.viewmodel.MainViewModel
import com.example.tdtustudentinformationmanagement.ui.viewmodel.StudentsViewModel
import com.example.tdtustudentinformationmanagement.ui.viewmodel.UsersViewModel

enum class MainSection(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    DASHBOARD("Dashboard", Icons.Outlined.Dashboard),
    USERS("Người dùng", Icons.Outlined.Group),
    STUDENTS("Sinh viên", Icons.Outlined.School),
    IMPORT_EXPORT("Nhập/Xuất", Icons.AutoMirrored.Outlined.ListAlt),
    PROFILE("Hồ sơ", Icons.Outlined.AccountCircle)
}

private fun availableSections(role: UserRole?): List<MainSection> {
    return when (role) {
        UserRole.ADMIN -> listOf(
            MainSection.DASHBOARD,
            MainSection.USERS,
            MainSection.STUDENTS,
            MainSection.IMPORT_EXPORT,
            MainSection.PROFILE
        )
        UserRole.MANAGER -> listOf(
            MainSection.DASHBOARD,
            MainSection.USERS,
            MainSection.STUDENTS,
            MainSection.IMPORT_EXPORT,
            MainSection.PROFILE
        )
        UserRole.EMPLOYEE -> listOf(
            MainSection.DASHBOARD,
            MainSection.USERS,
            MainSection.STUDENTS,
            MainSection.PROFILE
        )
        else -> listOf(MainSection.DASHBOARD)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit,
    mainViewModel: MainViewModel = hiltViewModel(),
    usersViewModel: UsersViewModel = hiltViewModel(),
    studentsViewModel: StudentsViewModel = hiltViewModel(),
    importExportViewModel: ImportExportViewModel = hiltViewModel()
) {
    val currentUser by mainViewModel.currentUser.collectAsState()
    val userRole by mainViewModel.userRole.collectAsState()
    val dashboardState by mainViewModel.dashboardState.collectAsState()
    val profilePictureState by mainViewModel.profilePictureState.collectAsState()
    val usersState by usersViewModel.uiState.collectAsState()
    val studentsState by studentsViewModel.uiState.collectAsState()
    val importExportState by importExportViewModel.uiState.collectAsState()
    val isLoggingOut by mainViewModel.isLoggingOut.collectAsState()

    val sections = remember(userRole) { availableSections(userRole) }
    var selectedSection by rememberSaveable(userRole) { 
        mutableStateOf(availableSections(userRole).firstOrNull() ?: MainSection.DASHBOARD) 
    }
    val configuration = LocalConfiguration.current
    val isLargeScreen = configuration.screenWidthDp >= 900
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Reload all data when user changes (e.g., switching accounts)
    // This triggers when currentUser.id changes, indicating a new user
    LaunchedEffect(currentUser?.id) {
        if (currentUser != null) {
            // Wait for userRole to be loaded (it's loaded by MainViewModel's auth observer)
            // Use first() to wait for non-null userRole or timeout
            withTimeoutOrNull(3000) {
                mainViewModel.userRole.first { it != null }
            }
            // Now reload all ViewModels' data after user and role are loaded
            mainViewModel.refreshDashboard()
            usersViewModel.loadUsers()
            studentsViewModel.loadStudents()
        }
    }
    
    // Reset selected section and reload ViewModels when user role changes
    LaunchedEffect(userRole) {
        if (userRole != null && currentUser != null) {
            // Reset selected section based on new role
            val newSections = availableSections(userRole)
            if (newSections.isNotEmpty()) {
                // If current section is not available for new role, switch to first available
                if (selectedSection !in newSections) {
                    selectedSection = newSections.first()
                }
            } else {
                // If no sections available, default to dashboard
                selectedSection = MainSection.DASHBOARD
            }
            
            // Reload all ViewModels when role is loaded/changed
            usersViewModel.loadUsers()
            studentsViewModel.loadStudents()
        }
    }

    // Load data when navigating to a section (lazy loading)
    LaunchedEffect(selectedSection) {
        when (selectedSection) {
            MainSection.DASHBOARD -> {
                mainViewModel.refreshDashboard()
                studentsViewModel.loadStudents()
                usersViewModel.loadUsers()
            }
            MainSection.USERS -> {
                usersViewModel.loadUsers()
            }
            MainSection.STUDENTS -> {
                studentsViewModel.loadStudents()
            }
            MainSection.IMPORT_EXPORT -> {
                studentsViewModel.loadStudents()
            }
            MainSection.PROFILE -> {
                // Profile doesn't need additional loading
            }
        }
    }

    LaunchedEffect(profilePictureState.successMessage, profilePictureState.errorMessage) {
        profilePictureState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            mainViewModel.refreshDashboard()
        }
        profilePictureState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    LaunchedEffect(usersState.successMessage, usersState.errorMessage) {
        usersState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            usersViewModel.clearMessages()
            mainViewModel.refreshDashboard()
        }
        usersState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            usersViewModel.clearMessages()
        }
    }

    LaunchedEffect(studentsState.successMessage, studentsState.errorMessage) {
        studentsState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            studentsViewModel.clearMessages()
            mainViewModel.refreshDashboard()
        }
        studentsState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            studentsViewModel.clearMessages()
        }
    }

    LaunchedEffect(importExportState.message, importExportState.errorMessage) {
        importExportState.message?.let {
            snackbarHostState.showSnackbar(it)
            importExportViewModel.clearMessage()
            studentsViewModel.loadStudents()
        }
        importExportState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            importExportViewModel.clearMessage()
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
            if (bytes != null) {
                mainViewModel.updateProfilePicture(bytes, mimeType)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_2),
                                contentDescription = "TDTU Logo",
                                modifier = Modifier.size(40.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Column {
                            Text(
                                text = "TDTU Student Management",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            AnimatedVisibility(
                                visible = currentUser != null,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = currentUser?.name ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                mainViewModel.logout()
                                onLogout()
                            }
                        },
                        enabled = !isLoggingOut
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.ExitToApp, contentDescription = "Đăng xuất")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!isLargeScreen) {
                NavigationBar {
                    sections.forEach { section ->
                        NavigationBarItem(
                            selected = selectedSection == section,
                            onClick = { selectedSection = section },
                            icon = { Icon(section.icon, contentDescription = section.title) },
                            label = { Text(section.title) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLargeScreen) {
                NavigationRail(
                    header = {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(16.dp)
                                .height(32.dp)
                        )
                    }
                ) {
                    sections.forEach { section ->
                        NavigationRailItem(
                            selected = selectedSection == section,
                            onClick = { selectedSection = section },
                            icon = { Icon(section.icon, contentDescription = section.title) },
                            label = { Text(section.title) }
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                when (selectedSection) {
                    MainSection.DASHBOARD -> DashboardScreen(
                        user = currentUser,
                        userRole = userRole,
                        dashboardState = dashboardState,
                        studentsState = studentsState,
                        usersState = usersState,
                        onRefresh = {
                            mainViewModel.refreshDashboard()
                            studentsViewModel.loadStudents()
                            usersViewModel.loadUsers()
                        },
                        onNavigateToStudents = { selectedSection = MainSection.STUDENTS },
                        onNavigateToUsers = { selectedSection = MainSection.USERS }
                    )

                    MainSection.USERS -> UsersScreen(
                        currentUser = currentUser,
                        uiState = usersState,
                        canManageUsers = userRole == UserRole.ADMIN,
                        canViewLoginHistory = userRole == UserRole.ADMIN,
                        onAddUser = { adminPassword, email, password, user ->
                            val adminEmail = currentUser?.email
                            if (!adminEmail.isNullOrBlank()) {
                                usersViewModel.addUser(adminEmail, adminPassword, email, password, user)
                            }
                        },
                        onUpdateUser = { user ->
                            usersViewModel.updateUser(user.id, user)
                        },
                        onDeleteUser = { user ->
                            usersViewModel.deleteUser(user.id)
                        },
                        onUpdateStatus = { user, status ->
                            usersViewModel.updateStatus(user.id, status)
                        },
                        onUpdateRole = { user, role ->
                            usersViewModel.updateRole(user.id, role)
                        },
                        onRefresh = { usersViewModel.loadUsers() },
                        onShowHistory = { usersViewModel.fetchLoginHistory(it) },
                        onHideHistory = { usersViewModel.clearHistory() }
                    )

                    MainSection.STUDENTS -> StudentsScreen(
                        uiState = studentsState,
                        canManageStudents = userRole == UserRole.ADMIN || userRole == UserRole.MANAGER,
                        onCreateStudent = { studentsViewModel.createStudent(it) },
                        onUpdateStudent = { studentsViewModel.updateStudent(it.id, it) },
                        onDeleteStudent = { studentsViewModel.deleteStudent(it.id) },
                        onSelectStudent = { studentsViewModel.selectStudent(it) },
                        onClearSelection = { studentsViewModel.clearSelection() },
                        onCreateCertificate = { studentsViewModel.createCertificate(it) },
                        onUpdateCertificate = { studentsViewModel.updateCertificate(it.id, it) },
                        onDeleteCertificate = { studentsViewModel.deleteCertificate(it.id) },
                        onRefresh = { studentsViewModel.loadStudents() }
                    )

                    MainSection.IMPORT_EXPORT -> ImportExportScreen(
                        uiState = importExportState,
                        students = studentsState.students,
                        onImportStudents = { uri -> importExportViewModel.importStudents(context, uri) },
                        onExportStudents = { uri -> importExportViewModel.exportStudents(context, uri) },
                        onImportCertificates = { studentId, uri ->
                            importExportViewModel.importCertificates(context, studentId, uri)
                        },
                        onExportCertificates = { studentId, uri ->
                            importExportViewModel.exportCertificates(context, studentId, uri)
                        }
                    )

                    MainSection.PROFILE -> ProfileScreen(
                        user = currentUser,
                        userRole = userRole,
                        profilePictureState = profilePictureState,
                        onChangePhoto = { imagePicker.launch("image/*") },
                        onUpdateUser = { name, email, phoneNumber, age ->
                            mainViewModel.updateUserInfo(name, email, phoneNumber, age)
                        }
                    )
                }
            }
        }
    }
}
