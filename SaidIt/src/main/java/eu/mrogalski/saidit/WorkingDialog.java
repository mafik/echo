package eu.mrogalski.saidit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class WorkingDialog extends ThemedDialog {
    private int descriptionStringId = R.string.work_default;

    @Override
    public void onSaveInstanceState( Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(getDescriptionKey(), getDescriptionStringId());
    }

    private String getDescriptionKey() {
        return WorkingDialog.class.getName() + "_description_id";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null && savedInstanceState.containsKey(getDescriptionKey())) {
            descriptionStringId = savedInstanceState.getInt(getDescriptionKey());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.progress_dialog, container);

        fixFonts(root);

        setDescriptionOnView(root);

        return root;
    }

    private void setDescriptionOnView(View root) {
        ((TextView) root.findViewById(R.id.progress_description)).setText(getDescriptionStringId());
    }


    public int getDescriptionStringId() {
        return descriptionStringId;
    }

    public void setDescriptionStringId(int descriptionStringId) {
        this.descriptionStringId = descriptionStringId;
        final View root = getView();
        if(root != null) {
            setDescriptionOnView(root);
        }
    }
}
