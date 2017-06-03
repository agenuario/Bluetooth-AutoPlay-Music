package maderski.bluetoothautoplaymusic.BluetoothActions;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;
import android.widget.Toast;

import maderski.bluetoothautoplaymusic.BuildConfig;
import maderski.bluetoothautoplaymusic.Controls.PlayMusicControl;
import maderski.bluetoothautoplaymusic.Controls.RingerControl;
import maderski.bluetoothautoplaymusic.Controls.VolumeControl;
import maderski.bluetoothautoplaymusic.Controls.WakeLockControl.ScreenONLock;
import maderski.bluetoothautoplaymusic.Controls.WifiControl;
import maderski.bluetoothautoplaymusic.Helpers.ReceiverHelper;
import maderski.bluetoothautoplaymusic.Helpers.TimeHelper;
import maderski.bluetoothautoplaymusic.LaunchApp;
import maderski.bluetoothautoplaymusic.Notification;
import maderski.bluetoothautoplaymusic.PackageTools;
import maderski.bluetoothautoplaymusic.Receivers.BTStateChangedReceiver;
import maderski.bluetoothautoplaymusic.SharedPrefs.BAPMDataPreferences;
import maderski.bluetoothautoplaymusic.SharedPrefs.BAPMPreferences;

/**
 * Created by Jason on 6/3/17.
 */

public class BTDisconnectActions {
    private static final String TAG = "BTDisconnectActions";

    private final ScreenONLock mScreenONLock;
    private final Context context;
    private final Notification mNotification;
    private final VolumeControl mVolumeControl;
    private final PlayMusicControl mPlayMusicControl;

    public BTDisconnectActions(Context context){
        this.context = context;
        this.mScreenONLock = ScreenONLock.getInstance();
        this.mNotification = new Notification();
        this.mVolumeControl = new VolumeControl(context);
        this.mPlayMusicControl = new PlayMusicControl(context);
    }

    //Removes mNotification and if set releases wakelock, puts the ringer back to normal,
    //pauses the music
    public void actionsOnBTDisconnect(){
        LaunchApp launchApp = new LaunchApp();
        RingerControl ringerControl = new RingerControl(context);

        removeBAPMNotification();
        pauseMusic();
        turnOffPriorityMode(ringerControl);
        sendAppToBackground(launchApp);
        closeWaze(launchApp);
        setWifiOn(launchApp);
        stopKeepingScreenOn();
        setVolumeBack(ringerControl);

        BAPMDataPreferences.setRanActionsOnBtConnect(context, false);
    }

    private void removeBAPMNotification(){
        boolean canShowNotification = BAPMPreferences.getShowNotification(context);

        if(canShowNotification) {
            mNotification.removeBAPMMessage(context);
        }
    }

    private void pauseMusic(){
        boolean playMusic = BAPMPreferences.getAutoPlayMusic(context);
        if (playMusic) {
            mPlayMusicControl.pause();
        }
    }

    private void sendAppToBackground(LaunchApp launchApp){
        boolean sendToBackground = BAPMPreferences.getSendToBackground(context);
        if (sendToBackground) {
            launchApp.sendEverythingToBackground(context);
        }
    }

    private void turnOffPriorityMode(RingerControl ringerControl){

        boolean priorityMode = BAPMPreferences.getPriorityMode(context);
        if (priorityMode) {
            int currentRinger = BAPMDataPreferences.getCurrentRingerSet(context);
            try {
                switch (currentRinger) {
                    case AudioManager.RINGER_MODE_SILENT:
                        Log.d(TAG, "Phone is on Silent");
                        break;
                    case AudioManager.RINGER_MODE_VIBRATE:
                        ringerControl.vibrateOnly();
                        break;
                    case AudioManager.RINGER_MODE_NORMAL:
                        ringerControl.soundsON();
                        break;
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private void closeWaze(LaunchApp launchApp){
        boolean closeWaze = BAPMPreferences.getCloseWazeOnDisconnect(context)
                && launchApp.checkPkgOnPhone(context, PackageTools.PackageName.WAZE)
                && BAPMPreferences.getMapsChoice(context).equals(PackageTools.PackageName.WAZE);
        if(closeWaze) {
            launchApp.closeWazeOnDisconnect(context);
        }
    }

    private void setWifiOn(LaunchApp launchApp){
        boolean isWifiOffDevice = BAPMDataPreferences.getIsTurnOffWifiDevice(context);
        if(isWifiOffDevice){
            int morningStartTime = BAPMPreferences.getMorningStartTime(context);
            int morningEndTime = BAPMPreferences.getMorningEndTime(context);

            int eveningStartTime = BAPMPreferences.getEveningStartTime(context);
            int eveningEndTime = BAPMPreferences.getEveningEndTime(context);

            TimeHelper timeHelper = new TimeHelper(morningStartTime, morningEndTime, eveningStartTime, eveningEndTime);
            boolean isHomeLocation = timeHelper.getDirectionLocation().equals(LaunchApp.DirectionLocations.HOME);

            boolean canChangeWifiState = !BAPMPreferences.getWifiUseMapTimeSpans(context)
                    || (isHomeLocation && launchApp.canMapsLaunchOnThisDay(context));
            if(canChangeWifiState && !WifiControl.isWifiON(context)) {
                WifiControl.wifiON(context, true);
            }
            BAPMDataPreferences.setIsTurnOffWifiDevice(context, false);
        }
    }

    private void stopKeepingScreenOn(){
        boolean screenON = BAPMPreferences.getKeepScreenON(context);
        if (screenON) {
            mScreenONLock.releaseWakeLock();
        }
    }

    private void setVolumeBack(RingerControl ringerControl){
        boolean volumeMAX = BAPMPreferences.getMaxVolume(context);
        if (volumeMAX) {
            mVolumeControl.setToOriginalVolume(ringerControl);
        }
    }

    public void actionsBTStateOff(){
        // Pause music
        PlayMusicControl playMusicControl = new PlayMusicControl(context);
        playMusicControl.pause();

        // Put music volume back to original volume
        mVolumeControl.setToOriginalVolume(new RingerControl(context));

        if(BuildConfig.DEBUG)
            Toast.makeText(context, "Music Paused", Toast.LENGTH_SHORT).show();

        if(BAPMDataPreferences.getRanActionsOnBtConnect(context)) {
            actionsOnBTDisconnect();
        }
        ReceiverHelper.stopReceiver(context, BTStateChangedReceiver.class);
    }
}
