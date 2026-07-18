package com.github.cookiesmartart.monopolybank.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.github.cookiesmartart.monopolybank.data.local.dao.GameSessionDao
import com.github.cookiesmartart.monopolybank.data.local.dao.PlayerDao
import com.github.cookiesmartart.monopolybank.data.local.dao.TransactionDao
import com.github.cookiesmartart.monopolybank.data.local.entity.GameSessionEntity
import com.github.cookiesmartart.monopolybank.data.local.entity.PlayerEntity
import com.github.cookiesmartart.monopolybank.data.local.entity.TransactionEntity

// v1.0.0 shipped at schema version 2, so this one (adding the Free Parking pot columns) is the
// first migration a real installed app goes through — an explicit migration keeps a player's
// in-progress game intact across the update instead of silently wiping it.
private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE game_sessions ADD COLUMN freeParkingPotEnabled INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE game_sessions ADD COLUMN freeParkingPot INTEGER NOT NULL DEFAULT 0")
    }
}

@Database(
    entities = [GameSessionEntity::class, PlayerEntity::class, TransactionEntity::class],
    version = 3,
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
                    .addMigrations(MIGRATION_2_3)
                    // Fallback for anyone on a schema older than the explicit migrations cover
                    // (pre-1.0.0 dev builds) — destructive migration just clears local state then.
                    .fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
    }
}
