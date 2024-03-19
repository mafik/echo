package eu.mrogalski.saidit;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import eu.mrogalski.android.Views;

public class ThemedDialog extends DialogFragment {
    static final String TAG = ThemedDialog.class.getName();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().getDecorView().setBackgroundDrawable(null);
        //set dialog width to 90% of screen width
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        return dialog;
    }

    protected void fixFonts(View root) {
        final Activity activity = getActivity();
        final Resources resources = activity.getResources();

        final AssetManager assets = activity.getAssets();

        final Typeface robotoCondensedBold = Typeface.createFromAsset(assets,"RobotoCondensedBold.ttf");
        final Typeface robotoCondensedRegular = Typeface.createFromAsset(assets, "RobotoCondensed-Regular.ttf");

        final float density = resources.getDisplayMetrics().density;

        Views.search((ViewGroup) root, new Views.SearchViewCallback() {
            @Override
            public void onView(View view, ViewGroup parent) {
                if (view instanceof Button) {
                    final Button button = (Button) view;
                    button.setTypeface(robotoCondensedRegular);
                } else if (view instanceof TextView) {
                    final String tag = (String) view.getTag();
                    final TextView textView = (TextView) view;
                    if (tag != null) {
                        if (tag.equals("titleBar")) {
                            textView.setTypeface(robotoCondensedBold);
                            textView.setShadowLayer(0.01f, 0, density * 2, resources.getColor(getShadowColorId()));
                        } else if (tag.equals("bold")) {
                            textView.setTypeface(robotoCondensedBold);
                        }
                    } else {
                        textView.setTypeface(robotoCondensedRegular);
                    }
                }
            }
        });
    }

    int getShadowColorId() {
        return R.color.dark_green;
    }
}
