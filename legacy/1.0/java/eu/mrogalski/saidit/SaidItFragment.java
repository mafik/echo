package eu.mrogalski.saidit;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.File;
import java.util.Random;

public class SaidItFragment extends Fragment {

    private static final String TAG = SaidItFragment.class.getSimpleName();


    private RingView circles;
    private Button status;
    private Button listenButton;
    ListenButtonClickListener listenButtonClickListener = new ListenButtonClickListener();
    RecordButtonClickListener recordButtonClickListener = new RecordButtonClickListener();
    private final String KEY_STATE = "recordingState";
    private Handler handler;

    private Runnable fixStatus = new Runnable() {
        @Override
        public void run() {
            if(service != null) {
                float t;
                String pre;
                switch(service.getState()) {
                    case SaidItService.STATE_LISTENING:
                        listeningRings();
                        t = Math.round(service.getBufferedSeconds()*10)/10f;
                        pre = "Buffer: ";
                        break;
                    case SaidItService.STATE_RECORDING:
                        recordingRings();
                        t = Math.round(service.getRecordedSeconds()*10)/10f;
                        pre = "Recorded ";
                        break;
                    default:
                        return;
                }
                circles.invalidate();
                String s = Math.round(t % 60 * 10) / 10f + " seconds";
                int minutes = (int) Math.floor(t/60);
                if(minutes > 0) {
                    if(minutes == 1) {
                        s = minutes + " minute, " + s;
                    } else {
                        s = minutes + " minutes, " + s;
                    }
                }
                listenButton.setText(pre + s);
                ViewCompat.postOnAnimation(circles, this);
            }
        }

        private void recordingRings() {
            float t = service.getRecordedSeconds();
            RingView.Ring r;
            int n = 0;

            if(circles.rings.size() < n+1) circles.rings.add(new RingView.Ring());
            r = circles.rings.get(n);
            r.max = 1;
            r.value = t % 1;
            r.ticks = 1;

            t = (float) Math.floor(t);
            if(t <= 0) return;
            n += 1;

            if(circles.rings.size() < n+1) circles.rings.add(new RingView.Ring());
            r = circles.rings.get(n);

            r.max = 60;
            r.value = t % 60;
            r.ticks = 60;

            t = (float) Math.floor(t/60);
            if(t <= 0) return;
            n += 1;

            if(circles.rings.size() < n+1) circles.rings.add(new RingView.Ring());
            r = circles.rings.get(n);

            r.max = 60;
            r.value = t % 60;
            r.ticks = 60;


        }

        private void listeningRings() {

            float t = service.getBufferedSeconds();
            RingView.Ring r;
            int n = 0;

            if(circles.rings.size() < n+1) circles.rings.add(new RingView.Ring());
            r = circles.rings.get(n);
            r.max = service.getMaxSeconds();
            r.value = t;
            r.ticks = 1;

            t = (float) Math.floor(t);
            if(t <= 0) return;
            n += 1;

            if(circles.rings.size() < n+1) circles.rings.add(new RingView.Ring());
            r = circles.rings.get(n);

            r.max = 60;
            r.value = t % 60;
            r.ticks = 60;

            t = (float) Math.floor(t/60);
            if(t <= 0) return;
            n += 1;

            if(circles.rings.size() < n+1) circles.rings.add(new RingView.Ring());
            r = circles.rings.get(n);

            r.max = 60;
            r.value = t % 60;
            r.ticks = 60;
        }
    };

    public SaidItFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler(getActivity().getMainLooper());
    }

    @Override
    public void onStart() {
        super.onStart();
        final FragmentActivity activity = getActivity();
        Intent intent = new Intent(activity, SaidItService.class);
        activity.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(service != null) {
            outState.putInt(KEY_STATE, service.getState());
        }
    }

    @Override
    public void onStop() {
        getActivity().unbindService(connection);
        super.onStop();
    }

    SaidItService service;
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder binder) {
            Log.d(TAG, "onServiceConnected");
            SaidItService.BackgroundRecorderBinder typedBinder = (SaidItService.BackgroundRecorderBinder) binder;
            service = typedBinder.getService();
            setStatus(service.getState());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "onServiceDisconnected");
            service = null;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_background_recorder, container, false);

        if(rootView == null) return null;

        Typeface tf = Typeface.createFromAsset(getActivity().getAssets(),"LCD.TTF");

        circles = (RingView) rootView.findViewById(R.id.circles);


        listenButton = (Button) rootView.findViewById(R.id.listen_button);
        if(listenButton != null) {
            listenButton.setOnClickListener(listenButtonClickListener);
            listenButton.setTypeface(tf);
        }

        status = (Button) rootView.findViewById(R.id.status);
        if(savedInstanceState != null) {
            final int statusCode = savedInstanceState.getInt(KEY_STATE);
            setStatus(statusCode);
        } else {
            stateReady();
        }
        status.setTypeface(tf);
        status.setOnClickListener(recordButtonClickListener);

        return rootView;
    }

    private void setStatus(int statusCode) {
        switch(statusCode) {
            case SaidItService.STATE_READY:
                stateReady();
                break;
            case SaidItService.STATE_RECORDING:
                stateRecording();
                break;
            case SaidItService.STATE_LISTENING:
                stateListening();
                break;
            default:
                stateReady();
                break;
        }
    }

    void stateReady() {
        handler.removeCallbacks(fixStatus);
        status.setText(R.string.ready);
        listenButton.setVisibility(View.VISIBLE);
        listenButton.setText(R.string.listening_disabled_enable);
        circles.rings.clear();
        circles.invalidate();
    }

    void stateListening() {
        handler.removeCallbacks(fixStatus);
        status.setText(R.string.ready);
        listenButton.setText(R.string.listening_enabled_disable);
        circles.rings.clear();
        ViewCompat.postOnAnimation(circles, fixStatus);
    }

    void stateRecording() {
        handler.removeCallbacks(fixStatus);
        status.setText(R.string.recording);
        circles.rings.clear();
        ViewCompat.postOnAnimation(circles, fixStatus);
    }

    private class ListenButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            final int serviceState = service.getState();
            switch(serviceState) {
                case SaidItService.STATE_READY:
                    showBufferingDialog();
                    break;
                case SaidItService.STATE_LISTENING:
                    stateReady();
                    service.disableListening();
                    break;
                default:
                    break;
            }
        }
    }

    void showBufferingDialog() {
        //DialogFragment dialog = new BufferingDialogFragment();
        //dialog.show(getFragmentManager(), "BufferingDialogFragment");

        service.innerStartListening();
        stateListening();
    }

    private class RecordButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            final int serviceState = service.getState();
            switch(serviceState) {
                case SaidItService.STATE_READY:
                case SaidItService.STATE_LISTENING:
                    service.startRecording();
                    stateRecording();
                    break;
                case SaidItService.STATE_RECORDING:
                    final File outFile = service.stopRecording();
                    NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                    Random generator = new Random();
                    notificationManager.notify(generator.nextInt(), buildNotificationForFile(outFile));
                    stateReady();
                    break;
            }
        }
    }


    private Notification buildNotificationForFile(File outFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(outFile), "audio/wav");
        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 0, intent, 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getActivity());
        notificationBuilder.setContentTitle("Recording saved");
        notificationBuilder.setContentText(outFile.getName());
        notificationBuilder.setSmallIcon(R.drawable.ic_stat_notify_recorded);
        notificationBuilder.setTicker("Recorded " + outFile.getName());
        notificationBuilder.setContentIntent(pendingIntent);
        notificationBuilder.setAutoCancel(true);
        return notificationBuilder.build();
    }
}
