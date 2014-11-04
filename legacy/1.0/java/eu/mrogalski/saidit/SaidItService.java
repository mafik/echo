package eu.mrogalski.saidit;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import simplesound.pcm.WavAudioFormat;
import simplesound.pcm.WavFileWriter;
import static eu.mrogalski.saidit.SaidIt.*;

public class SaidItService extends Service {
    static final String TAG = SaidItService.class.getSimpleName();

    static final int SAMPLE_RATE = 44100;

    File wavFile;
    AudioRecord audioRecord; // used only in the audio thread
    WavFileWriter wavFileWriter; // used only in the audio thread
    final AudioMemory audioMemory = new AudioMemory(); // used only in the audio thread
    volatile private int readLimit = Integer.MAX_VALUE; // used to control responsiveness of audio thread

    HandlerThread audioThread;
    Handler audioHandler; // used to post messages to audio thread

    @Override
    public void onCreate() {

        final SharedPreferences preferences = this.getSharedPreferences(PACKAGE_NAME, MODE_PRIVATE);
        if(preferences.getBoolean(AUDIO_MEMORY_ENABLED_KEY, true)) {
            innerStartListening();
        }

    }

    @Override
    synchronized
    public void onDestroy() {
        stopRecording(null);
        innerStopListening();
    }

    @Override
    public IBinder onBind(Intent intent) {
        readLimit = SAMPLE_RATE * 2 / 10;
        return new BackgroundRecorderBinder();
    }

    @Override
    public void onRebind(Intent intent) {
        readLimit = SAMPLE_RATE * 2 / 10;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        readLimit = Integer.MAX_VALUE;
        return true;
    }

    public void enableListening() {
        getSharedPreferences(PACKAGE_NAME, MODE_PRIVATE)
                .edit().putBoolean(AUDIO_MEMORY_ENABLED_KEY, true).commit();

        innerStartListening();
    }

    public void disableListening() {
        getSharedPreferences(PACKAGE_NAME, MODE_PRIVATE)
                .edit().putBoolean(AUDIO_MEMORY_ENABLED_KEY, false).commit();

        innerStopListening();
    }

    int state;

    static final int STATE_READY = 0;
    static final int STATE_LISTENING = 1;
    static final int STATE_RECORDING = 2;

    synchronized
    private void innerStartListening() {
        switch(state) {
            case STATE_READY:
                break;
            case STATE_LISTENING:
            case STATE_RECORDING:
                return;
        }
        state = STATE_LISTENING;

        Log.d(TAG, "STARTING LISTENING");

        Notification note = new Notification( 0, null, System.currentTimeMillis() );
        note.flags |= Notification.FLAG_NO_CLEAR;
        startForeground( 42, note );

        audioThread = new HandlerThread("audioThread", Thread.MAX_PRIORITY);
        audioThread.start();
        audioHandler = new Handler(audioThread.getLooper());
        audioHandler.post(new Runnable() {
            @Override
            public void run() {
                audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        44100 * 5 * 2); // five seconds in bytes

                audioRecord.startRecording();
            }
        });

        audioHandler.post(audioReader);
    }

    synchronized
    private void innerStopListening() {
        switch(state) {
            case STATE_READY:
            case STATE_RECORDING:
                return;
            case STATE_LISTENING:
                break;
        }
        state = STATE_READY;
        Log.d(TAG, "STOPPING LISTENING");

        stopForeground(true);

        audioHandler.post(new Runnable() {
            @Override
            public void run() {
                audioRecord.release();
                audioHandler.removeCallbacks(audioReader);
            }
        });

    }

    synchronized public void startRecording() {
        switch(state) {
            case STATE_READY:
                innerStartListening();
                break;
            case STATE_LISTENING:
                break;
            case STATE_RECORDING:
                return;
        }
        state = STATE_RECORDING;

        audioHandler.post(new Runnable() {
            @Override
            public void run() {
                String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                path += "/out.wav";

                wavFile = new File(path);
                WavAudioFormat format = new WavAudioFormat.Builder().sampleRate(SAMPLE_RATE).build();
                try {
                    wavFileWriter = new WavFileWriter(format, wavFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                audioMemory.read(new AudioMemory.Consumer() {
                    @Override
                    public int consume(byte[] array, int offset, int count) {
                        try {
                            wavFileWriter.write(array, offset, count);
                        } catch (IOException ignored) {}
                        return 0;
                    }
                });
            }
        });

        final Notification notification = buildNotification();
        startForeground(42, notification);
    }

    public interface WavFileReceiver {
        public void fileReady(File file);
    }

    synchronized
    public void stopRecording(final WavFileReceiver wavFileReceiver) {
        switch(state) {
            case STATE_READY:
            case STATE_LISTENING:
                return;
            case STATE_RECORDING:
                break;
        }
        state = STATE_LISTENING;

        audioHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    wavFileWriter.close();
                } catch (IOException e) {
                    Log.e(TAG, "CLOSING ERROR", e);
                }
                if(wavFileReceiver != null) {
                    wavFileReceiver.fileReady(wavFile);
                }
                wavFileWriter = null;
            }
        });

        final SharedPreferences preferences = this.getSharedPreferences(PACKAGE_NAME, MODE_PRIVATE);
        if(!preferences.getBoolean(AUDIO_MEMORY_ENABLED_KEY, true)) {
            innerStopListening();
        }

        stopForeground(true);
    }

    final Runnable audioReader = new Runnable() {
        @Override
        public void run() {
            audioMemory.fill(new AudioMemory.Consumer() {
                @Override
                public int consume(byte[] array, int offset, int count) {
                    final int read = audioRecord.read(array, offset, count);
                    if(read == AudioRecord.ERROR_BAD_VALUE) {
                        Log.e(TAG, "AUDIO RECORD ERROR - BAD VALUE");
                        return 0;
                    }
                    if(read == AudioRecord.ERROR_INVALID_OPERATION) {
                        Log.e(TAG, "AUDIO RECORD ERROR - INVALID OPERATION");
                        return 0;
                    }
                    if(read == AudioRecord.ERROR) {
                        Log.e(TAG, "AUDIO RECORD ERROR - UNKNOWN ERROR");
                        return 0;
                    }
                    if(wavFileWriter != null && read > 0) {
                        try {
                            wavFileWriter.write(array, offset, count);
                        } catch (IOException e) {
                            Log.e(TAG, "WRITING ERROR", e);
                        }
                    }
                    audioHandler.post(audioReader);
                    return read;
                }
            });
        }
    };

    public interface StateCallback {
        public void state(boolean listeningEnabled, boolean recording, int memorized, int totalMemory, int recorded);
    }

    synchronized
    public void getState(final StateCallback stateCallback) {
        final SharedPreferences preferences = this.getSharedPreferences(PACKAGE_NAME, MODE_PRIVATE);
        final boolean listeningEnabled = preferences.getBoolean(AUDIO_MEMORY_ENABLED_KEY, true);
        final boolean recording = (state == STATE_RECORDING);
        audioMemory.observe(new AudioMemory.Observer() {
            @Override
            public void observe(int probablyTaken, int reallyTaken, int total) {
                int recorded = 0;
                if(wavFileWriter != null) {
                    recorded += wavFileWriter.getTotalSampleBytesWritten();
                    recorded += probablyTaken;
                }
                stateCallback.state(listeningEnabled, recording, reallyTaken + probablyTaken, total, recorded);
            }
        });
    }

    class BackgroundRecorderBinder extends Binder {
        public SaidItService getService() {
            return SaidItService.this;
        }
    }

    private Notification buildNotification() {

        Intent intent = new Intent(this, BackgroundRecorderActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setContentTitle("Recording...");
        notificationBuilder.setUsesChronometer(true);
        notificationBuilder.setProgress(100, 50, true);
        notificationBuilder.setSmallIcon(R.drawable.ic_stat_notify_recording);
        notificationBuilder.setTicker("Recording...");
        notificationBuilder.setContentIntent(pendingIntent);
        return notificationBuilder.build();
    }

}
