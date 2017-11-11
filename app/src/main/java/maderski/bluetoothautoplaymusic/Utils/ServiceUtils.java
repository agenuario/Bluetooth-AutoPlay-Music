package maderski.bluetoothautoplaymusic.Utils;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.List;

import maderski.bluetoothautoplaymusic.R;

/**
 * Created by Jason on 6/6/17.
 */

public class ServiceUtils {
    public static void startService(Context context, Class<?> serviceClass, String tag) {
        Intent intent = new Intent(context, serviceClass);
        intent.addCategory(tag);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            context.startService(intent);
        } else {
            context.startForegroundService(intent);
        }
    }

    public static void stopService(Context context, Class<?> serviceClass, String tag) {
        Intent intent = new Intent(context, serviceClass);
        intent.addCategory(tag);
        context.stopService(intent);
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass){
        ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(serviceClass.getName())){
                return true;
            }
        }
        return false;
    }

    public static void createServiceNotification(int id, String title, String message, Service service) {
        Notification.Builder builder;

        if(Build.VERSION.SDK_INT < 26) {
            builder = new android.app.Notification.Builder(service);
        } else {
            NotificationManager notificationManager = (NotificationManager) service.getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
            String channelId = "BTAPMChannelID";
            CharSequence channelName = "Bluetooth Autoplay Music";

            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_MIN);
            notificationManager.createNotificationChannel(channel);
            builder = new Notification.Builder(service, channelId);
        }

        Notification notification = builder
                .setSmallIcon(R.drawable.ic_notif_icon)
                .setContentTitle(title)
                .setContentText(message)
                .build();

        service.startForeground(id, notification);
    }
}