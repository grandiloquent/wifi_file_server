package euphoria.psycho.fileserver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Process;

import java.io.IOException;


public class FileService extends Service implements Runnable {
    public static final String CHANNEL_ID = "FileService_channel_01";
    Thread mThread;
    private WakeLock mWakeLock;
    private NotificationManager mNotificationManager;
    private BroadcastReceiver mBroadcastReceiver = new
            BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    stopForeground(true);
                    stopSelf();
                }
            };
    FileServer mFileServer;

    private Notification.Builder createNotification() {
        Notification.Builder builder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, new Intent("euphoria.psycho.fileserver.SHUTDOWN"),
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setSmallIcon(android.R.drawable.stat_notify_sync)
                .setOngoing(true)
                .addAction(0, "关闭", pendingIntent);
        return builder;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction("euphoria.psycho.fileserver.SHUTDOWN");
        registerReceiver(mBroadcastReceiver, filter);
        final PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        mWakeLock.setReferenceCounted(false);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel;
            channel = new NotificationChannel(CHANNEL_ID, "YT", NotificationManager.IMPORTANCE_LOW);
            mNotificationManager.createNotificationChannel(channel);
        }
        startForeground(hashCode(), createNotification()
                .setContentTitle("Wifi文件服务器正在运行")
                .build());
    }

    @Override
    public void onDestroy() {
        if (mThread != null) {
            mThread.interrupt();
            mThread = null;
        }
        mWakeLock.release();
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mThread == null) {
            mThread = new Thread(this);
            mThread.start();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        try {
            mFileServer = new FileServer(this);
            mFileServer.start();
        } catch (
                IOException ignored) {
        }
    }
}
