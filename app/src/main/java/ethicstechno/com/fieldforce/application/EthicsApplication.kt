package ethicstechno.com.fieldforce.application

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.yanzhenjie.album.Album
import com.yanzhenjie.album.AlbumConfig
import java.util.Locale

class EthicsApplication : Application() {

    companion object {
        private var instance: EthicsApplication? = null

        fun getAppContext(): Context {
            return instance!!.applicationContext
        }

        fun getInstance(): EthicsApplication {
            return instance!!
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        FirebaseApp.initializeApp(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        Album.initialize(
            AlbumConfig.newBuilder(this)
                .setAlbumLoader(MediaLoader())
                .setLocale(Locale.getDefault())
                .build()
        )
    }
}