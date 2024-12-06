package ethicstechno.com.fieldforce.db.dao

import ethicstechno.com.fieldforce.models.LoginResponse
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ethicstechno.com.fieldforce.models.AppRegistrationResponse

@Dao
interface AppDao {
    //App Registrations operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAppRegistration(appRegistrationResponse: AppRegistrationResponse)

    @Query("Select * from appRegistrationTable")
    fun getAppRegistration() : AppRegistrationResponse

    @Query("Delete from appRegistrationTable")
    fun deleteAppRegistration()

    //Login operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLogin(appRegistrationResponse: LoginResponse)

    @Query("Select * from loginTable")
    fun getLoginData() : LoginResponse

    @Query("Delete from loginTable")
    fun deleteLogin()

    @Query("UPDATE loginTable SET todayClockInDone = :isPunchIn WHERE userId = :loginUserId")
    fun updatePunchInFlag(isPunchIn: Boolean, loginUserId: Int)

    @Query("UPDATE loginTable SET attendanceId = :attendanceId WHERE userId = :loginUserId")
    fun updateAttendanceId(attendanceId: Int, loginUserId: Int)

    @Query("UPDATE loginTable SET todayClockOutDone = :isPunchOut WHERE userId = :loginUserId")
    fun updatePunchOutFlag(isPunchOut: Boolean, loginUserId: Int)

    @Query("UPDATE loginTable SET userPhoto = :userPhoto WHERE userId = :loginUserId")
    fun updateProfilePath(userPhoto:String, loginUserId: Int)

    @Query("UPDATE loginTable SET userName = :username WHERE userId = :loginUserId")
    fun updateUsername(username:String, loginUserId: Int)


}