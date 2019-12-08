package maderski.bluetoothautoplaymusic.helpers

import maderski.bluetoothautoplaymusic.helpers.enums.MapApps
import maderski.bluetoothautoplaymusic.sharedprefs.BAPMPreferences

class PreferencesHelper(private val preferences: BAPMPreferences) {
    val mapAppChosen get() = preferences.getMapsChoice()
    val customLocationName get() = preferences.getCustomLocationName()
    val canLaunchDirections get() = preferences.getCanLaunchDirections()
    val canLaunchDrivingMode get() = preferences.getLaunchMapsDrivingMode() &&
            mapAppName == MapApps.MAPS.packageName
    val isLaunchingWithDirections get() = preferences.getCanLaunchDirections()
    val isUsingTimesToLaunch get() = preferences.getUseTimesToLaunchMaps()

    val morningStartTime get() = preferences.getMorningStartTime()
    val morningEndTime get() = preferences.getMorningEndTime()

    val eveningStartTime get() = preferences.getEveningStartTime()
    val eveningEndTime get() = preferences.getEveningEndTime()

    val customStartTime get() = preferences.getCustomStartTime()
    val customEndTime get() = preferences.getCustomEndTime()

    val isUseLaunchTimeEnabled get() = preferences.getUseTimesToLaunchMaps()

    val musicPlayerPkgName get() = preferences.getPkgSelectedMusicPlayer()

    val daysToLaunchHome get() = preferences.getHomeDaysToLaunchMaps() ?: setOf<String>()
    val daysToLaunchWork get() = preferences.getWorkDaysToLaunchMaps() ?: setOf<String>()
    val daysToLaunchCustom get() = preferences.getCustomDaysToLaunchMaps() ?: setOf<String>()

    val waitTillOffPhone get() = preferences.getWaitTillOffPhone()
    val unlockScreen get() = preferences.getUnlockScreen()
    val mapAppName get() = preferences.getMapsChoice()
    val canShowNotification get() = preferences.getShowNotification()
    val keepScreenON get() = preferences.getKeepScreenON()
    val volumeMAX get() = preferences.getMaxVolume()
    val canAutoPlayMusic get() = preferences.getAutoPlayMusic()
    val isLaunchingMusicPlayer get() = preferences.getLaunchMusicPlayer()
    val isLaunchingMaps get() = preferences.getLaunchGoogleMaps()
    val isUsingWifiMapTimeSpans get() = preferences.getWifiUseMapTimeSpans()
    val priorityMode get() = preferences.getPriorityMode()
}