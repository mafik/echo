package eu.mrogalski.saidit;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;

public class SaidItActivity extends Activity {

    static final String TAG = SaidItActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background_recorder);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new SaidItFragment(), "main-fragment")
                    .commit();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
