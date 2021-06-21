package cz.palda97.lpclient.viewmodel.settings

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.*
import cz.palda97.lpclient.AppInit
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.R
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.model.SharedPreferencesFactory
import cz.palda97.lpclient.model.SharedPreferencesFactory.liveData
import cz.palda97.lpclient.model.repository.EditServerRepository
import cz.palda97.lpclient.model.repository.ExecutionNoveltyRepository
import cz.palda97.lpclient.model.repository.ServerRepository
import cz.palda97.lpclient.model.services.ExecutionMonitorPeriodic
import cz.palda97.lpclient.view.Notifications.NOTIFICATIONS
import cz.palda97.lpclient.view.Notifications.NOTIFICATIONS_CANCEL
import cz.palda97.lpclient.viewmodel.CommonViewModel
import cz.palda97.lpclient.viewmodel.settings.SettingsViewModel.TimeEnum.Companion.toStatus
import cz.palda97.lpclient.viewmodel.MainActivityViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * ViewModel for the [SettingsFragment][cz.palda97.lpclient.view.settings.SettingsFragment].
 */
@SuppressLint("ApplySharedPref")
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = SharedPreferencesFactory.sharedPreferences(application)
    private val serverRepository: ServerRepository = Injector.serverRepository
    private val editServerRepository: EditServerRepository = Injector.editServerRepository

    private val dbScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.IO)

    /**
     * A property for getting and setting the night mode.
     */
    var nightMode: NightModeEnum
        get() = NightModeEnum.fromNightMode(
            sharedPreferences.getInt(AppInit.NIGHT_MODE, AppInit.DEFAULT_NIGHT_MODE)
        )
        set(value) {
            sharedPreferences.edit().putInt(AppInit.NIGHT_MODE, value.nightMode).apply()
            AppCompatDelegate.setDefaultNightMode(value.nightMode)
        }

    private var lastDeletedServer: ServerInstance =
        ServerInstance()

    init {
        l("init")
    }

    var stopRemindingMonitorBackground: Boolean
        get() = sharedPreferences.getBoolean(STOP_REMINDING_MONITOR_BACKGROUND, false)
        set(value) {
            sharedPreferences.edit().putBoolean(STOP_REMINDING_MONITOR_BACKGROUND, value).apply()
        }

    var stopRemindingDoNotKill: Boolean
        get() = sharedPreferences.getBoolean(STOP_REMINDING_DO_NOT_KILL, false)
        set(value) {
            sharedPreferences.edit().putBoolean(STOP_REMINDING_DO_NOT_KILL, value).apply()
        }

    private fun getNotificationSwitchText(context: Context): String {
        fun getString(resId: Int) = context.getString(resId)
        return "${getString(R.string.notifications)} (${getString(R.string.frequency_lower_case)}: $timeValue ${getString(timeUnit.resId)})"
    }

    private val _liveInterval = MutableLiveData<String>().apply {
        value = getNotificationSwitchText(application.applicationContext)
    }
    val liveInterval: LiveData<String>
        get() = _liveInterval

    private val _liveNotificationInfoDialog = MutableLiveData<Boolean>()
    val liveNotificationInfoDialog: LiveData<Boolean>
        get() = _liveNotificationInfoDialog
    fun resetNotificationInfoDialog() {
        _liveNotificationInfoDialog.value = false
    }

    private val _liveDoNotKillDialog = MutableLiveData<Boolean>()
    val liveDoNotKillDialog: LiveData<Boolean>
        get() = _liveDoNotKillDialog
    fun resetDoNotKillDialog() {
        _liveDoNotKillDialog.value = false
    }

    val automaticRefreshRate: String =
        "${MainActivityViewModel.AUTOMATIC_REFRESH_RATE / 1000} ${application.applicationContext.getString(R.string.seconds)}"

    fun enqueueMonitor() {
        _liveTimeButtonEnable.value = false
        if (!stopRemindingDoNotKill) {
            _liveDoNotKillDialog.value = true
        }
        if (!stopRemindingMonitorBackground) {
            _liveNotificationInfoDialog.value = true
        }
        ExecutionMonitorPeriodic.enqueue(getApplication(), timeValue, timeUnit.unit)
        _liveInterval.value = getNotificationSwitchText(getApplication())
    }

    /**
     * A property for turning notifications on and off and getting this settings.
     */
    var notifications: Boolean
        get() = sharedPreferences.getBoolean(NOTIFICATIONS, false)
        set(value) {
            sharedPreferences.edit().putBoolean(NOTIFICATIONS, value).commit()
            when(value) {
                true -> {
                    val currentTime = System.currentTimeMillis()
                    sharedPreferences.edit()
                        .putLong(ExecutionNoveltyRepository.NOTIFICATION_SINCE, currentTime)
                        .putBoolean(ExecutionNoveltyRepository.SHOULD_RESET_RECENT_EXECUTIONS, true)
                        .commit()
                    enqueueMonitor()
                }
                false -> ExecutionMonitorPeriodic.cancel(getApplication())
            }
        }

    val liveNotificationCancel = sharedPreferences
        .liveData(NOTIFICATIONS_CANCEL, false) { key, defaultValue ->
            getBoolean(key, defaultValue)
        }
    fun resetNotificationCancel() {
        sharedPreferences.edit().putBoolean(NOTIFICATIONS_CANCEL, false).commit()
    }

    var timeValue: Long
        get() = sharedPreferences.getLong(TIME_VALUE, 15)
        set(value) {
            sharedPreferences.edit().putLong(TIME_VALUE, value).commit()
            _liveTimeButtonEnable.value = isTimeDifferent
        }

    enum class TimeEnum(val unit: TimeUnit, val resId: Int) {
        SECONDS(TimeUnit.SECONDS, R.string.seconds),
        MINUTES(TimeUnit.MINUTES, R.string.minutes),
        HOURS(TimeUnit.HOURS, R.string.hours);
        companion object {
            val String?.toStatus
                get() = if (this == null) {
                    MINUTES
                } else {
                    try {
                        valueOf(this)
                    } catch (e: IllegalArgumentException) {
                        MINUTES
                    }
                }
        }
    }

    var timeUnit: TimeEnum
        get() = sharedPreferences.getString(TIME_UNIT, null).toStatus
        set(value) {
            sharedPreferences.edit().putString(TIME_UNIT, value.name).commit()
            _liveTimeButtonEnable.value = isTimeDifferent
        }

    private val isTimeDifferent: Boolean
        get() = sharedPreferences.getLong(ExecutionMonitorPeriodic.LAST_INTERVAL, 0) != timeUnit.unit.toSeconds(timeValue)

    private val _liveTimeButtonEnable = MutableLiveData<Boolean>().apply {
        value = isTimeDifferent
    }
    val liveTimeButtonEnable: LiveData<Boolean>
        get() = _liveTimeButtonEnable

    private fun forceSaveServer(serverInstance: ServerInstance) {
        dbScope.launch {
            saveServerAndUpdate(serverInstance)
        }
    }

    /**
     * Delete this server instance from database.
     * (Also, store it locally for the UNDO option)
     */
    fun deleteServer(serverInstance: ServerInstance) {
        lastDeletedServer = serverInstance
        dbScope.launch {
            serverRepository.deleteServer(serverInstance)
        }
    }

    /**
     * UNDO the last deletion of a server instance.
     */
    fun undoLastDeleteServer() {
        forceSaveServer(lastDeletedServer)
    }

    /** @see ServerRepository.liveServers */
    val liveServers: LiveData<MailPackage<List<ServerInstance>>>
        get() = serverRepository.liveServers

    /** @see ServerRepository.activeLiveServers */
    val activeLiveServers: LiveData<MailPackage<List<ServerInstance>>>
        get() = serverRepository.activeLiveServers

    /**
     * Prepare [EditServerRepository] for this server instance.
     */
    fun editServer(serverInstance: ServerInstance) {
        editServerRepository.serverToEdit = serverInstance
        editServerRepository.tmpServer = serverInstance
    }

    /**
     * Prepare [EditServerRepository] for a new server instance.
     */
    fun addServer() {
        editServer(ServerInstance())
    }

    /**
     * Change the active property of this [ServerInstance].
     */
    fun activeChange(server: ServerInstance) {
        server.active = !server.active
        forceSaveServer(server)
    }

    companion object {
        private val l = Injector.generateLogFunction(this)

        /**
         * Gets an instance of [SettingsViewModel] tied to the owner's lifecycle.
         */
        fun getInstance(owner: ViewModelStoreOwner) = ViewModelProvider(owner).get(SettingsViewModel::class.java)

        private const val TIME_VALUE = "TIME_VALUE"
        private const val TIME_UNIT = "TIME_UNIT"

        private const val STOP_REMINDING_MONITOR_BACKGROUND = "STOP_REMINDING_MONITOR_BACKGROUND"
        private const val STOP_REMINDING_DO_NOT_KILL = "STOP_REMINDING_DO_NOT_KILL"

        suspend fun saveServerAndUpdate(server: ServerInstance) {
            Injector.serverRepository.insertServer(server)
            if (server.active) {
                val currentTime = System.currentTimeMillis()
                SharedPreferencesFactory.sharedPreferences(Injector.context).edit()
                    .putLong(ExecutionNoveltyRepository.NOTIFICATION_SINCE, currentTime)
                    .commit()
                CoroutineScope(Dispatchers.IO).launch { CommonViewModel.updateAndNotify(server) }
            }
        }
    }
}