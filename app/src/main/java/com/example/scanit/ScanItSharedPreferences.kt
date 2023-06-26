import android.content.Context
import android.content.SharedPreferences

class ScanItSharedPreferences private constructor(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        SHARED_PREFERENCES_FILE_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val SHARED_PREFERENCES_FILE_NAME = "scanit_shared_preferences"
        private const val LOGIN_STATUS_KEY = "login_status"
        private const val USERNAME_KEY = "username"
        private const val PASSWORD_KEY = "password"

        @Volatile
        private var instance: ScanItSharedPreferences? = null

        fun getInstance(context: Context): ScanItSharedPreferences {
            return instance ?: synchronized(this) {
                instance ?: ScanItSharedPreferences(context).also { instance = it }
            }
        }
    }

    fun setLoginStatus(loginStatus: Boolean) {
        sharedPreferences.edit().putBoolean(LOGIN_STATUS_KEY, loginStatus).apply()
    }

    fun getLoginStatus(): Boolean {
        return sharedPreferences.getBoolean(LOGIN_STATUS_KEY, false)
    }

    fun setUsername(username: String) {
        sharedPreferences.edit().putString(USERNAME_KEY, username).apply()
    }

    fun getUsername(): String {
        return sharedPreferences.getString(USERNAME_KEY, "") ?: ""
    }

    fun setPassword(password: String) {
        sharedPreferences.edit().putString(PASSWORD_KEY, password).apply()
    }

    fun getPassword(): String {
        return sharedPreferences.getString(PASSWORD_KEY, "") ?: ""
    }
}
