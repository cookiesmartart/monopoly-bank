package com.github.cookiesmartart.monopolybank.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.github.cookiesmartart.monopolybank.data.local.dao.GameSessionDao
import com.github.cookiesmartart.monopolybank.data.local.dao.PlayerDao
import com.github.cookiesmartart.monopolybank.data.local.dao.TransactionDao
import com.github.cookiesmartart.monopolybank.data.local.entity.GameSessionEntity
import com.github.cookiesmartart.monopolybank.data.local.entity.PlayerEntity
import com.github.cookiesmartart.monopolybank.data.local.entity.TransactionEntity

@Database(
    entities = [GameSessionEntity::class, PlayerEntity::class, TransactionEntity::class],
    version = 2,
    exportSchema = true
)
abstract class MonopolyDatabase : RoomDatabase() {
    abstract fun gameSessionDao(): GameSessionDao
    abstract fun playerDao(): PlayerDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var instance: MonopolyDatabase? = null

        fun getInstance(context: Context): MonopolyDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MonopolyDatabase::class.java,
                    "monopoly-bank.db"
                )
                    // No release has shipped yet, so there's no real user data to preserve across
                    // schema changes — destructive migration just clears local state on upgrade.
                    .fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
    }
}
