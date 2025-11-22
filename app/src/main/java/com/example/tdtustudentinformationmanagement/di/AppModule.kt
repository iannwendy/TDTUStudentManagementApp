package com.example.tdtustudentinformationmanagement.di

import com.example.tdtustudentinformationmanagement.data.firebase.FirebaseConfig
import com.example.tdtustudentinformationmanagement.data.repository.AuthRepository
import com.example.tdtustudentinformationmanagement.data.repository.StorageRepository
import com.example.tdtustudentinformationmanagement.data.repository.StudentRepository
import com.example.tdtustudentinformationmanagement.data.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideFirebaseConfig(): FirebaseConfig {
        return FirebaseConfig()
    }
    
    @Provides
    @Singleton
    fun provideUserRepository(firebaseConfig: FirebaseConfig): UserRepository {
        return UserRepository(firebaseConfig)
    }
    
    @Provides
    @Singleton
    fun provideStudentRepository(firebaseConfig: FirebaseConfig): StudentRepository {
        return StudentRepository(firebaseConfig)
    }
    
    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseConfig: FirebaseConfig,
        userRepository: UserRepository
    ): AuthRepository {
        return AuthRepository(firebaseConfig, userRepository)
    }

    @Provides
    @Singleton
    fun provideStorageRepository(
        firebaseConfig: FirebaseConfig
    ): StorageRepository {
        return StorageRepository(firebaseConfig)
    }
}
