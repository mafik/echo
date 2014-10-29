package eu.mrogalski.saidit;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.net.URLConnection;

import eu.mrogalski.StringFormat;
import eu.mrogalski.android.TimeFormat;

public class RecordingDoneDialog extends ThemedDialog {

    private static final String KEY_RUNTIME = "runtime";
    private static final String KEY_FILE = "file";

    private File file;
    private float runtime;
    private final TimeFormat.Result timeFormatResult = new TimeFormat.Result();

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloat(KEY_RUNTIME, runtime);
        outState.putString(KEY_FILE, file.getPath());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            if(savedInstanceState.containsKey(KEY_FILE))
                file = new File(savedInstanceState.getString(KEY_FILE));
            if(savedInstanceState.containsKey(KEY_RUNTIME))
                runtime = savedInstanceState.getFloat(KEY_RUNTIME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.recording_done_dialog, container);
        assert root != null;

        fixFonts(root);

        final Activity activity = getActivity();
        assert activity != null;
        final Resources resources = activity.getResources();
        TimeFormat.naturalLanguage(resources, runtime, timeFormatResult);

        ((TextView) root.findViewById(R.id.recording_done_filename)).setText(file.getName());
        ((TextView) root.findViewById(R.id.recording_done_dirname)).setText(file.getParent());
        ((TextView) root.findViewById(R.id.recording_done_runtime)).setText(timeFormatResult.text);
        ((TextView) root.findViewById(R.id.recording_done_size)).setText(StringFormat.shortFileSize(file.length()));

        root.findViewById(R.id.recording_done_open_dir).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.fromFile(file.getParentFile());
                intent.setData(uri);
                startActivity(Intent.createChooser(intent, "Open folder"));
            }
        });

        root.findViewById(R.id.recording_done_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                shareIntent.setType(URLConnection.guessContentTypeFromName(file.getAbsolutePath()));
                startActivity(Intent.createChooser(shareIntent, "Send to"));
            }
        });

        root.findViewById(R.id.recording_done_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), "audio/*");
                startActivity(intent);
            }
        });

        return root;
    }

    public RecordingDoneDialog setFile(File file) {
        this.file = file;
        return this;
    }

    public RecordingDoneDialog setRuntime(float runtime) {
        this.runtime = runtime;
        return this;
    }
}
