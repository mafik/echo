package eu.mrogalski.saidit;

import android.content.Context;
import android.content.Intent;

public class BroadcastReceiver extends android.content.BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
        // Start only if tutorial has been finished
        if (context.getSharedPreferences(SaidIt.PACKAGE_NAME, Context.MODE_PRIVATE).getBoolean("skip_tutorial", false)) {
            context.startService(new Intent(context, SaidItService.class));
        }
    }
}
