package eu.mrogalski.saidit;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by marek on 15.12.13.
 */
public class FakeService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        Notification note = new Notification( 0, null, System.currentTimeMillis() );
        note.flags |= Notification.FLAG_NO_CLEAR;
        startForeground(42, note);
        stopForeground(true);

        stopSelf();

        return super.onStartCommand(intent, flags, startId);
    }
}
