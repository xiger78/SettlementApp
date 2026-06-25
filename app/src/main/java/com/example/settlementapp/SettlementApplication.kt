package com.example.settlementapp

import android.app.Application
import com.example.settlementapp.data.AppDatabase
import com.example.settlementapp.data.SettlementRepository

class SettlementApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    val repository: SettlementRepository by lazy {
        SettlementRepository(database.meetingDao(), database.participantDao())
    }
}
