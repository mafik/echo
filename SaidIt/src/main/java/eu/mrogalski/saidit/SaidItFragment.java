package eu.mrogalski.saidit;

import android.app.Activity;
import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;

import eu.mrogalski.android.TimeFormat;
import eu.mrogalski.android.Views;

public class SaidItFragment extends Fragment {

    private static final String TAG = SaidItFragment.class.getSimpleName();

    private Button record_pause_button;
    private Button listenButton;

    ListenButtonClickListener listenButtonClickListener = new ListenButtonClickListener();
    RecordButtonClickListener recordButtonClickListener = new RecordButtonClickListener();

    private boolean isListening = true;
    private boolean isRecording = false;

    private LinearLayout ready_section;
    private Button recordLastFiveMinutesButton;
    private Button recordMaxButton;
    private Button recordLastMinuteButton;

    private TextView history_limit;
    private TextView history_size;
    private TextView history_size_title;

    private LinearLayout rec_section;
    private TextView rec_indicator;
    private TextView rec_time;

    private ImageButton rate_on_google_play;
    private ImageView heart;

    @Override
    public void onStart() {
        super.onStart();
        final Activity activity = getActivity(); assert activity != null;
        activity.bindService(new Intent(activity, SaidItService.class), echoConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        final Activity activity = getActivity(); assert activity != null;
        activity.unbindService(echoConnection);
    }

    class ActivityResult {
        final int requestCode;
        final int resultCode;
        final Intent data;

        ActivityResult(int requestCode, int resultCode, Intent data) {
            this.requestCode = requestCode;
            this.resultCode = resultCode;
            this.data = data;
        }
    }

    private Runnable updater = new Runnable() {
        @Override
        public void run() {
            final View view = getView();
            if(view == null) return;
            if(echo == null) return;
            echo.getState(serviceStateCallback);
        }
    };

    SaidItService echo;
    private ServiceConnection echoConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder binder) {
            SaidItService.BackgroundRecorderBinder typedBinder = (SaidItService.BackgroundRecorderBinder) binder;
            echo = typedBinder.getService();
            ViewCompat.postOnAnimation(getView(), updater);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            echo = null;
        }
    };

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_background_recorder, container, false);

        if(rootView == null) return null;

        final Activity activity = getActivity();
        final AssetManager assets = activity.getAssets();
        final Typeface robotoCondensedBold = Typeface.createFromAsset(assets,"RobotoCondensedBold.ttf");
        final Typeface robotoCondensedRegular = Typeface.createFromAsset(assets, "RobotoCondensed-Regular.ttf");
        final float density = activity.getResources().getDisplayMetrics().density;

        Views.search((ViewGroup) rootView, new Views.SearchViewCallback() {
            @Override
            public void onView(View view, ViewGroup parent) {

                if (view instanceof Button) {
                    final Button button = (Button) view;
                    button.setTypeface(robotoCondensedBold);
                    final int shadowColor;
                    if (Build.VERSION.SDK_INT >= 16)
                        shadowColor = button.getShadowColor();
                    else
                        shadowColor = 0x80000000;
                    button.setShadowLayer(0.01f, 0, density * 2, shadowColor);
                } else if (view instanceof TextView) {

                    final TextView textView = (TextView) view;
                    textView.setTypeface(robotoCondensedRegular);
                }
            }
        });

        history_limit = (TextView) rootView.findViewById(R.id.history_limit);
        history_size = (TextView) rootView.findViewById(R.id.history_size);
        history_size_title = (TextView) rootView.findViewById(R.id.history_size_title);

        history_limit.setTypeface(robotoCondensedBold);
        history_size.setTypeface(robotoCondensedBold);

        listenButton = (Button) rootView.findViewById(R.id.listen_button);
        if(listenButton != null) {
            listenButton.setOnClickListener(listenButtonClickListener);
        }

        if(Build.VERSION.SDK_INT >= 19) {
            final int statusBarHeight = getStatusBarHeight();
            listenButton.setPadding(listenButton.getPaddingLeft(), listenButton.getPaddingTop() + statusBarHeight, listenButton.getPaddingRight(), listenButton.getPaddingBottom());
            final ViewGroup.LayoutParams layoutParams = listenButton.getLayoutParams();
            layoutParams.height += statusBarHeight;
            listenButton.setLayoutParams(layoutParams);
        }


        record_pause_button = (Button) rootView.findViewById(R.id.rec_stop_button);
        record_pause_button.setOnClickListener(recordButtonClickListener);

        recordLastMinuteButton = (Button) rootView.findViewById(R.id.record_last_minute);
        recordLastMinuteButton.setOnClickListener(recordButtonClickListener);
        recordLastMinuteButton.setOnLongClickListener(recordButtonClickListener);

        recordLastFiveMinutesButton = (Button) rootView.findViewById(R.id.record_last_5_minutes);
        recordLastFiveMinutesButton.setOnClickListener(recordButtonClickListener);
        recordLastFiveMinutesButton.setOnLongClickListener(recordButtonClickListener);

        recordMaxButton = (Button) rootView.findViewById(R.id.record_last_max);
        recordMaxButton.setOnClickListener(recordButtonClickListener);
        recordMaxButton.setOnLongClickListener(recordButtonClickListener);

        ready_section = (LinearLayout) rootView.findViewById(R.id.ready_section);
        rec_section = (LinearLayout) rootView.findViewById(R.id.rec_section);
        rec_indicator = (TextView) rootView.findViewById(R.id.rec_indicator);
        rec_time = (TextView) rootView.findViewById(R.id.rec_time);

        rate_on_google_play = (ImageButton) rootView.findViewById(R.id.rate_on_google_play);

        final Animation pulse = AnimationUtils.loadAnimation(activity, R.anim.pulse);
        heart = (ImageView) rootView.findViewById(R.id.heart);
        heart.startAnimation(pulse);

        rate_on_google_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + activity.getPackageName())));
            }
        });

        heart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                heart.animate().scaleX(10).scaleY(10).alpha(0).setDuration(2000).start();
                Handler handler = new Handler(activity.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + activity.getPackageName())));
                    }
                }, 1000);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        heart.setAlpha(0f);
                        heart.setScaleX(1);
                        heart.setScaleY(1);
                        heart.animate().alpha(1).start();

                    }
                }, 3000);
            }
        });

        rootView.findViewById(R.id.settings_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(activity, SettingsActivity.class));
            }
        });

        serviceStateCallback.state(isListening, isRecording, 0, 0, 0);

        return rootView;
    }

    private SaidItService.StateCallback serviceStateCallback = new SaidItService.StateCallback() {
        @Override
        public void state(final boolean listeningEnabled, final boolean recording, final float memorized, final float totalMemory, final float recorded) {
            final Activity activity = getActivity();
            if(activity == null) return;
            final Resources resources = activity.getResources();
            if((isRecording != recording) || (isListening  != listeningEnabled)) {
                if(recording != isRecording) {
                    isRecording = recording;
                    if (recording) {
                        rec_section.setVisibility(View.VISIBLE);
                    } else {
                        rec_section.setVisibility(View.GONE);
                    }
                }

                if(listeningEnabled != isListening) {
                    isListening = listeningEnabled;
                    if (listeningEnabled) {
                        listenButton.setText(R.string.listening_enabled_disable);
                        listenButton.setBackgroundResource(R.drawable.top_green_button);
                        listenButton.setShadowLayer(0.01f, 0, resources.getDimensionPixelOffset(R.dimen.shadow_offset), resources.getColor(R.color.dark_green));
                    } else {
                        listenButton.setText(R.string.listening_disabled_enable);
                        listenButton.setBackgroundResource(R.drawable.top_gray_button);
                        listenButton.setShadowLayer(0.01f, 0, resources.getDimensionPixelOffset(R.dimen.shadow_offset), 0xff666666);
                    }

                    if(Build.VERSION.SDK_INT >= 19) {
                        final int statusBarHeight = getStatusBarHeight();
                        listenButton.setPadding(listenButton.getPaddingLeft(), listenButton.getPaddingTop() + statusBarHeight, listenButton.getPaddingRight(), listenButton.getPaddingBottom());
                    }
                    listenButton.setGravity(Gravity.CENTER);
                }

                if (listeningEnabled && !recording) {
                    ready_section.setVisibility(View.VISIBLE);
                } else {
                    ready_section.setVisibility(View.GONE);
                }
            }

            TimeFormat.naturalLanguage(resources, totalMemory, timeFormatResult);

            if(!history_limit.getText().equals(timeFormatResult.text)) {
                history_limit.setText(timeFormatResult.text);
            }

            TimeFormat.naturalLanguage(resources, memorized, timeFormatResult);

            if(!history_size.getText().equals(timeFormatResult.text)) {
                history_size_title.setText(resources.getQuantityText(R.plurals.history_size_title, timeFormatResult.count));
                history_size.setText(timeFormatResult.text);
                recordMaxButton.setText(TimeFormat.shortTimer(memorized));
            }

            TimeFormat.naturalLanguage(resources, recorded, timeFormatResult);

            if(!rec_time.getText().equals(timeFormatResult.text)) {
                rec_indicator.setText(resources.getQuantityText(R.plurals.recorded, timeFormatResult.count));
                rec_time.setText(timeFormatResult.text);
            }

            ViewCompat.postOnAnimation(history_size, updater);
        }
    };

    final TimeFormat.Result timeFormatResult = new TimeFormat.Result();




    private class ListenButtonClickListener implements View.OnClickListener {

        final WorkingDialog dialog = new WorkingDialog() {
            @Override
            public void onStart() {
                super.onStart();
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        echo.enableListening();
                        echo.getState(new SaidItService.StateCallback() {
                            @Override
                            public void state(boolean listeningEnabled, boolean recording, float memorized, float totalMemory, float recorded) {
                                dismiss();
                            }
                        });
                    }
                });
            }
        };

        public ListenButtonClickListener() {
            dialog.setDescriptionStringId(R.string.work_preparing_memory);
        }

        @Override
        public void onClick(View v) {
            echo.getState(new SaidItService.StateCallback() {
                @Override
                public void state(final boolean listeningEnabled, boolean recording, float memorized, float totalMemory, float recorded) {
                    if (listeningEnabled) {
                        echo.disableListening();
                    } else {
                        dialog.show(getFragmentManager(), "Preparing memory");
                    }
                }
            });
        }
    }

    private class RecordButtonClickListener implements View.OnClickListener, View.OnLongClickListener {

        @Override
        public void onClick(final View v) {
            record(v, false);
        }

        @Override
        public boolean onLongClick(final View v) {
            record(v, true);
            return true;
        }

        public void record(final View button, final boolean keepRecording) {
            echo.getState(new SaidItService.StateCallback() {
                @Override
                public void state(final boolean listeningEnabled, final boolean recording, float memorized, float totalMemory, float recorded) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (recording) {
                                echo.stopRecording(new PromptFileReceiver(getActivity()));
                            } else {
                                final float seconds = getPrependedSeconds(button);
                                if (keepRecording) {
                                    echo.startRecording(seconds);
                                } else {
                                    echo.dumpRecording(seconds, new PromptFileReceiver(getActivity()));
                                }
                            }
                        }
                    });
                }
            });
        }

        float getPrependedSeconds(View button) {
            switch (button.getId()) {
                case R.id.record_last_minute: return 60;
                case R.id.record_last_5_minutes: return 60 * 5;
                case R.id.record_last_max: return 60 * 60 * 24 * 365;
            }
            return 0;
        }
    }


    static Notification buildNotificationForFile(Context context, File outFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(outFile), "audio/wav");
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder.setContentTitle(context.getString(R.string.recording_saved));
        notificationBuilder.setContentText(outFile.getName());
        notificationBuilder.setSmallIcon(R.drawable.ic_stat_notify_recorded);
        notificationBuilder.setTicker(outFile.getName());
        notificationBuilder.setContentIntent(pendingIntent);
        notificationBuilder.setAutoCancel(true);
        return notificationBuilder.build();
    }

    static class NotifyFileReceiver implements SaidItService.WavFileReceiver {

        private Context context;

        public NotifyFileReceiver(Context context) {
            this.context = context;
        }

        @Override
        public void fileReady(final File file, float runtime) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(43, buildNotificationForFile(context, file));
        }
    }

    static class PromptFileReceiver implements SaidItService.WavFileReceiver {

        private Activity activity;

        public PromptFileReceiver(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void fileReady(final File file, float runtime) {
            new RecordingDoneDialog()
                    .setFile(file)
                    .setRuntime(runtime)
                    .show(activity.getFragmentManager(), "Recording Done");
        }
    }
}
