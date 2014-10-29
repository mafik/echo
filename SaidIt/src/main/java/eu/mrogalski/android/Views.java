package eu.mrogalski.android;

import android.view.View;
import android.view.ViewGroup;

public class Views {
    public static void search(ViewGroup viewGroup, SearchViewCallback callback) {
        final int cnt = viewGroup.getChildCount();
        for(int i = 0; i < cnt; ++i) {
            final View child = viewGroup.getChildAt(i);
            if(child instanceof ViewGroup) {
                search((ViewGroup) child, callback);
            }
            callback.onView(child, viewGroup);
        }

    }

    public static interface SearchViewCallback {
        public void onView(View view, ViewGroup parent);
    }
}
