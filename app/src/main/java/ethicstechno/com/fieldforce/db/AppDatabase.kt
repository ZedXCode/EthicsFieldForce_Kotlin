package ethicstechno.com.fieldforce.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ethicstechno.com.fieldforce.db.dao.AppDao
import ethicstechno.com.fieldforce.models.AppRegistrationResponse
import ethicstechno.com.fieldforce.models.LoginResponse

@Database(
    entities = [AppRegistrationResponse::class, LoginResponse::class],
    version = 2, exportSchema = false
)
@TypeConverters(
)

abstract class AppDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "FieldForce")
                    .addMigrations(MIGRATION_1_2)
                    .allowMainThreadQueries()
                    .build()
            }
            return instance!!
        }

        fun destroyInstance() {
            instance = null
        }

        val MIGRATION_1_2 = object : Migration(1, 2){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE appRegistrationTable ADD COLUMN webHostingServer TEXT NOT NULL DEFAULT ''")
            }

        }
    }
}