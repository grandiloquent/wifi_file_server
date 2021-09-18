package euphoria.psycho.fileserver;

import android.Manifest;
import android.Manifest.permission;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Process;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends Activity {
    static {
        System.loadLibrary("nativelib");
    }

    private TextView mTextView;
    private ImageView mImageView;

    public static String getDeviceIP(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        try {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            InetAddress inetAddress = intToInetAddress(wifiInfo.getIpAddress());
            return inetAddress.getHostAddress();
        } catch (Exception e) {
            Log.e("TAG", e.getMessage());
            return null;
        }
    }

    public static InetAddress intToInetAddress(int hostAddress) {
        byte[] addressBytes = {(byte) (0xff & hostAddress),
                (byte) (0xff & (hostAddress >> 8)),
                (byte) (0xff & (hostAddress >> 16)),
                (byte) (0xff & (hostAddress >> 24))};
        try {
            return InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException e) {
            throw new AssertionError();
        }
    }

    public native static int makeQrCode(String value, byte[] buffer);

    public native static boolean startServer(
            String ip,
            String resourceDirectory, int port);

    private static native void load(AssetManager mgr);

    private Bitmap getBitmap(String localIp) {
        byte[] buffer = new byte[29 * 29];
        makeQrCode(localIp, buffer);
        int border = 0;
        int scale = 20;
        int width = (29 + border * 2) * scale;
        Bitmap qrcode = Bitmap.createBitmap(width, width, Config.ARGB_8888);
        for (int y = 0; y < width; y++) {
            for (int x = 0; x < width; x++) {
                boolean color = buffer[x / scale + y / scale * 29] == 1;
                qrcode.setPixel(x, y, color ? 0xFF000000 : 0x00FFFFFF);
            }
        }
        return qrcode;
    }

    private void initialize() {
        load(getAssets());
        setContentView(R.layout.activity_main);
        mTextView = findViewById(R.id.text_view);
        mImageView = findViewById(R.id.image_view);
        new Thread(() -> {
            String localIp = getDeviceIP(MainActivity.this);
            localIp = "http://" + localIp + ":12345";
            String finalLocalIp = localIp;
            Bitmap qrcode = getBitmap(localIp);
            MainActivity.this.runOnUiThread(() -> {
                mTextView.setText("服务器地址：" + finalLocalIp);
                mImageView.setImageBitmap(qrcode);
            });
        }).start();
        if (VERSION.SDK_INT >= VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                try {
                    Uri uri = Uri.parse("package:" + getPackageName());
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri);
                    startActivity(intent);
                } catch (Exception ex) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivity(intent);
                }
            }
        }
        Intent service = new Intent(this, FileService.class);
        startService(service);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List<String> needPermissions = Arrays.stream(new String[]{
                permission.INTERNET,
                permission.ACCESS_WIFI_STATE,
                permission.READ_EXTERNAL_STORAGE,
        }).filter(permission -> checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                .collect(Collectors.toList());
        if (VERSION.SDK_INT <= 28 && checkSelfPermission(permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            needPermissions.add(permission.WRITE_EXTERNAL_STORAGE);
        } else if (VERSION.SDK_INT >= VERSION_CODES.P && (checkSelfPermission(permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED)) {
            needPermissions.add(permission.FOREGROUND_SERVICE);
        }
        if (needPermissions.size() > 0) {
            requestPermissions(needPermissions.toArray(new String[0]), 1);
            return;
        }
        initialize();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "帮助");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://lucidu.cn"));
        startActivity(Intent.createChooser(intent, "打开"));
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (Arrays.stream(grantResults)
                .filter(g -> g != PackageManager.PERMISSION_GRANTED).count() > 0) {
            Toast.makeText(this, "程序缺少必要程序无法运行", Toast.LENGTH_LONG).show();
            finish();
        } else {
            initialize();
        }

    }

    public static class FileService extends Service implements Runnable {
        public static final String CHANNEL_ID = "FileService_channel_01";
        private String mHost;
        Thread mThread;
        private WakeLock mWakeLock;
        private NotificationManager mNotificationManager;

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onCreate() {
            super.onCreate();
            mHost = getDeviceIP(this);
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
                    .setContentTitle("Wifi文件服务器已启动")
                    .build());
        }

        private Notification.Builder createNotification() {
            Notification.Builder builder;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                builder = new Notification.Builder(this, CHANNEL_ID);
            } else {
                builder = new Notification.Builder(this);
            }
//            PendingIntent pendingIntent = PendingIntent.getActivity(
//                    this, 0, new Intent(this, MusicActivity
//                            .class),
//                    PendingIntent.FLAG_UPDATE_CURRENT
//            );
            builder.setSmallIcon(android.R.drawable.stat_notify_sync);
//                    .addAction(R.drawable.ic_action_play_arrow, "", null)
//                    .setContentIntent(pendingIntent);
            return builder;
        }

        @Override
        public void onDestroy() {
            if (mThread != null) {
                mThread.interrupt();
                mThread = null;
            }
            mWakeLock.release();
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
            startServer(mHost,
                    "", 12345);
        }
    }

}
