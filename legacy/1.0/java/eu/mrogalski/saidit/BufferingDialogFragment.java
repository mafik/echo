package eu.mrogalski.saidit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

/**
 * Created by marek on 14.12.13.
 */
public class BufferingDialogFragment extends DialogFragment {

    private SaidItService service;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setTitle("Memory for audio")
                .setSingleChoiceItems(R.array.memory_sizes, service.getMemorySize(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        return builder.show();
    }

    @Override
    public void onAttach(Activity activity) {
        FragmentActivity fra = (FragmentActivity) activity;
        SaidItFragment brf = (SaidItFragment) fra.getSupportFragmentManager().findFragmentById(R.id.container);
        service = brf.service;
        super.onAttach(activity);
    }
}
