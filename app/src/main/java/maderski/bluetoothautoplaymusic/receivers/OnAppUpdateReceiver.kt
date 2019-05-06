package maderski.bluetoothautoplaymusic.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import maderski.bluetoothautoplaymusic.sharedprefs.BAPMPreferences
import maderski.bluetoothautoplaymusic.workers.OnAppUpdateWorker
import maderski.bluetoothautoplaymusic.workers.OnBootWorker

/**
 * Created by Jason on 6/10/17.
 */

class OnAppUpdateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            if (action != null && action == Intent.ACTION_PACKAGE_REPLACED) {
                val appContext = context.applicationContext
                // Create and add work request to work manager
                val workOnAppUpdateRequest = OneTimeWorkRequestBuilder<OnAppUpdateWorker>()
                        .addTag(OnBootWorker.TAG)
                        .build()
                WorkManager.getInstance().enqueue(workOnAppUpdateRequest)

                Log.d(TAG, "BAPM Service Started")


                // Sync newly separated Home Work checkboxes
                syncHomeWorkCheckboxes(appContext)
            }
        }
    }

    private fun syncHomeWorkCheckboxes(context: Context) {
        val hasRan = BAPMPreferences.getUpdateHomeWorkDaysSync(context)
        if (!hasRan) {
            val daysHomeWorkRan = BAPMPreferences.getHomeDaysToLaunchMaps(context) as MutableSet<String>
            BAPMPreferences.setWorkDaysToLaunchMaps(context, daysHomeWorkRan)
            BAPMPreferences.setUpdateHomeWorkDaysSync(context, true)
            Log.d(TAG, "Work/Home Sync Complete")
        }
    }

    companion object {
        private const val TAG = "OnAppUpdateReceiver"
    }
}
