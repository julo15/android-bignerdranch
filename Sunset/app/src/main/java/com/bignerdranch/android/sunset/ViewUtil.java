package com.bignerdranch.android.sunset;

import android.view.View;

/**
 * Created by julianlo on 12/11/15.
 */
public class ViewUtil {

    public static <T> T findView(View view, int id) {
        return (T)view.findViewById(id);
    }
}
