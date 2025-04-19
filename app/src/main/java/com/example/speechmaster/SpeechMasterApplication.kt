package com.example.speechmaster

import android.app.Application
import com.example.speechmaster.domain.session.UserSessionManager
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

import javax.inject.Inject

@HiltAndroidApp
class SpeechMasterApplication : Application() {

    @Inject
    lateinit var userSessionManager: UserSessionManager

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

    }


}