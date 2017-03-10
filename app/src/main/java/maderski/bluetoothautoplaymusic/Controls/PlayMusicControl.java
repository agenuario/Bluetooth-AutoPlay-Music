package maderski.bluetoothautoplaymusic.Controls;

import android.content.Context;
import android.media.AudioManager;
import android.os.CountDownTimer;
import android.util.Log;

import maderski.bluetoothautoplaymusic.Analytics.FirebaseHelper;
import maderski.bluetoothautoplaymusic.BuildConfig;
import maderski.bluetoothautoplaymusic.LaunchApp;
import maderski.bluetoothautoplaymusic.PackageTools;
import maderski.bluetoothautoplaymusic.SharedPrefs.BAPMPreferences;

/**
 * Created by Jason on 12/8/15.
 */
public class PlayMusicControl {

    private static final String TAG = PlayMusicControl.class.getName();

    private PlayerControls playerControls;
    private FirebaseHelper mFirebaseHelper;

    private static CountDownTimer mCountDownTimer;

    private Context mContext;

    public PlayMusicControl(Context context){
        mContext = context;
        setPlayerControls();
        mFirebaseHelper = new FirebaseHelper(context);
    }

    private void setPlayerControls(){
        String pkgName = BAPMPreferences.getPkgSelectedMusicPlayer(mContext);
        Log.d(TAG, "PLAYER: " + pkgName);
        switch (pkgName) {
            case PackageTools.PackageName.SPOTIFY:
                playerControls = new Spotify(mContext);
                break;
            case PackageTools.PackageName.BEYONDPOD:
                playerControls = new BeyondPod(mContext);
                break;
            case PackageTools.PackageName.APPLEMUSIC:
                playerControls = new AppleMusicAndroid(mContext);
                break;
            default:
                playerControls = new OtherMusicPlayer(mContext);
                break;
        }
    }

    public void pause(){ playerControls.pause(); }

    public void play(){
        Log.d(TAG, "Tried to play");
        playerControls.play();
    }

    public static void cancelCheckIfPlaying(){
        if(mCountDownTimer != null){
            mCountDownTimer.cancel();
            Log.d(TAG, "Music Play Check CANCELLED");
            mCountDownTimer = null;
        }
    }

    public synchronized void checkIfPlaying(final Context context, final int seconds){
        long milliseconds = seconds * 1000;
        mCountDownTimer = new CountDownTimer(milliseconds, 1000) {
            AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            @Override
            public void onTick(long l) {
                if(BuildConfig.DEBUG){
                    Log.d(TAG, "millsUntilFinished: " + Long.toString(l));
                    Log.d(TAG, "isMusicPlaying: " + Boolean.toString(audioManager.isMusicActive()));
                }

                if(audioManager.isMusicActive()){
                    Log.d(TAG, "Music is playing");
                }else{
                    play();
                }
            }

            @Override
            public void onFinish() {
                if(!audioManager.isMusicActive()){
                    String selectedMusicPlayer = BAPMPreferences.getPkgSelectedMusicPlayer(context);
                    if(selectedMusicPlayer.equals(PackageTools.PackageName.PANDORA)){
                        LaunchApp launchApp = new LaunchApp();
                        launchApp.launchPackage(context, PackageTools.PackageName.PANDORA);
                        Log.d(TAG, "PANDORA LAUNCHED");
                    }else {
                        Log.d(TAG, "Final attempt to play");
                        playerControls.play_keyEvent();
                    }
                }
                mFirebaseHelper.musicAutoPlay(audioManager.isMusicActive());
                mCountDownTimer = null;
            }
        }.start();
    }
}
