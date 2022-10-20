package euphoria.psycho.fileserver;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.io.IOException;


public class FileService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    FileServer mFileServer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            mFileServer = new FileServer(this);
            mFileServer.start();
        } catch (
                IOException ignored) {
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
