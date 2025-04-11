package com.example.speechmaster

import android.app.Application
import com.example.speechmaster.domain.session.UserSessionManager
import dagger.hilt.android.HiltAndroidApp

import javax.inject.Inject

@HiltAndroidApp
class SpeechMasterApplication : Application() {

//    @Inject
//    lateinit var userRepository: UserRepository
    @Inject
    lateinit var userSessionManager: UserSessionManager

    override fun onCreate() {
        super.onCreate()

        // 应用启动时初始化用户
//        initializeUser()
    }

//    private fun initializeUser() {
//        applicationScope.launch {
//            // 确保有本地用户 - 如果没有会创建默认用户
//            userRepository.ensureLocalUser()
//        }
//    }
}