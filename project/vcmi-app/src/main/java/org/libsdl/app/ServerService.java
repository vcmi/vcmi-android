package org.libsdl.app;

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
        new Thread()
        {
            @Override
            public void run()
            {
                System.loadLibrary("vcmi-server");
//                System.loadLibrary("vcmiaibattle");
//                System.loadLibrary("vcmiaivcai");
//                NativeMethods.registerVCAI();
//                NativeMethods.registerBattleAI();
                VCMIJavaHelpers.setupCtx(ServerService.this);
                createServer();
            }
        }.start();
        return START_NOT_STICKY;
    }

    public native void createServer();
}
