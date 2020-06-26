package com.example.forvenders.base

import android.app.Application

class AppClass : Application() {

    override fun onCreate() {
        super.onCreate()
        PreferanceRepository.init(this)
    }
}