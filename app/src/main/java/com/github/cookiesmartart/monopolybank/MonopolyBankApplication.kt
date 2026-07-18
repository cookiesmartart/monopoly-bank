package com.github.cookiesmartart.monopolybank

import android.app.Application
import com.github.cookiesmartart.monopolybank.data.local.MonopolyDatabase
import com.github.cookiesmartart.monopolybank.data.repository.BankRepository
import com.github.cookiesmartart.monopolybank.data.settings.SettingsRepository

class MonopolyBankApplication : Application() {
    val repository: BankRepository by lazy {
        BankRepository(MonopolyDatabase.getInstance(this))
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(this)
    }
}
