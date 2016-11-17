package eu.vcmi.vcmi;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by F on 13.11.2016.
 */

public class ServerService extends Service
{
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        NativeMethods.setupCtx(ServerService.this);
        new Thread()
        {
            @Override
            public void run()
            {
                System.loadLibrary("vcmi-server");
                NativeMethods.createServer();
            }
        }.start();
        return START_NOT_STICKY;
    }
}
