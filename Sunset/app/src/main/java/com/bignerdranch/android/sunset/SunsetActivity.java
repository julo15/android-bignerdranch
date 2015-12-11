package com.bignerdranch.android.sunset;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class SunsetActivity extends SingleFragmentActivity {

    @Override
    public Fragment createFragment() {
        return SunsetFragment.newInstance();
    }

}
