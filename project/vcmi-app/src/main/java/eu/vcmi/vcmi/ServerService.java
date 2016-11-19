package eu.vcmi.vcmi;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

import eu.vcmi.vcmi.util.Log;

/**
 * Created by F on 13.11.2016.
 */

public class ServerService extends Service
{
    public static final int CLIENT_MESSAGE_CLIENT_REGISTERED = 1;
    public static final String INTENT_ACTION_KILL_SERVER = "ServerService.Action.Kill";
    final Messenger mMessenger = new Messenger(new IncomingClientMessageHandler());
    private Messenger mClient;

    @Override
    public IBinder onBind(Intent intent)
    {
        return mMessenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        NativeMethods.setupCtx(ServerService.this);
        System.loadLibrary("vcmi-server");
        if (INTENT_ACTION_KILL_SERVER.equals(intent.getAction()))
        {
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.i(this, "destroyed");
        // we need to kill the process to ensure all server data is cleaned up; this isn't a good solution (as we mess with system's
        // memory management stuff), but clearing all native data manually would be a pain and we can't force close the server "gracefully", because
        // even after onDestroy call, the system can postpone actually finishing the process -- this would bread CVCMIServer initialization
        System.exit(0);
    }

    private static class ServerStartThread extends Thread
    {
        @Override
        public void run()
        {
            NativeMethods.createServer();
        }
    }

    private class IncomingClientMessageHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case CLIENT_MESSAGE_CLIENT_REGISTERED:
                    mClient = msg.replyTo;
                    NativeMethods.setupMsg(mClient);
                    new ServerStartThread().start();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
