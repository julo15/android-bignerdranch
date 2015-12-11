package com.bignerdranch.android.sunset;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by julianlo on 12/11/15.
 */
public class SunsetFragment extends Fragment {

    private View mSceneView;
    private View mSunView;
    private View mSkyView;

    private int mBlueSkyColor;
    private int mSunsetSkyColor;
    private int mNightSkyColor;

    private boolean mSunIsUp = true;

    private ObjectAnimator mSunWidthAnimator;
    private ObjectAnimator mSunHeightAnimator;

    public static SunsetFragment newInstance() {
        return new SunsetFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sunset, container, false);

        mSceneView = view;
        mSunView = ViewUtil.findView(view, R.id.sun);
        mSkyView = ViewUtil.findView(view, R.id.sky);

        Resources resources = getResources();
        mBlueSkyColor = resources.getColor(R.color.blue_sky);
        mSunsetSkyColor = resources.getColor(R.color.sunset_sky);
        mNightSkyColor = resources.getColor(R.color.night_sky);

        mSceneView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAnimation(mSunIsUp);
                mSunIsUp = !mSunIsUp;
            }
        });

        initSunAnimation();
        mSunWidthAnimator.start();
        mSunHeightAnimator.start();

        return view;
    }

    private void initSunAnimation() {
        final float SMALL_SUN_SCALE = 1.0f;
        final float BIG_SUN_SCALE = 1.1f;
        final long DURATION = 2000;

        mSunWidthAnimator = ObjectAnimator
                .ofFloat(mSunView, "scaleX", SMALL_SUN_SCALE, BIG_SUN_SCALE)
                .setDuration(DURATION);
        mSunWidthAnimator.setRepeatCount(ValueAnimator.INFINITE);

        mSunHeightAnimator = ObjectAnimator
                .ofFloat(mSunView, "scaleY", SMALL_SUN_SCALE, BIG_SUN_SCALE)
                .setDuration(DURATION);
        mSunHeightAnimator.setRepeatCount(ValueAnimator.INFINITE);
    }

    private void startAnimation(boolean doSunset) {
        final float sunYUp = mSunView.getTop();
        final float sunYDown = mSkyView.getHeight();
        float sunYStart = doSunset ? sunYUp : sunYDown;
        float sunYEnd = doSunset ? sunYDown : sunYUp;
        int sunsetSkyStart = doSunset ? mBlueSkyColor : mSunsetSkyColor;
        int sunsetSkyEnd = doSunset ? mSunsetSkyColor : mBlueSkyColor;
        int nightSkyStart = doSunset ? mSunsetSkyColor : mNightSkyColor;
        int nightSkyEnd = doSunset ? mNightSkyColor : mSunsetSkyColor;

        if (doSunset) {
            mSunWidthAnimator.cancel();
            mSunHeightAnimator.cancel();
            mSunView.setScaleX(1.0f);
            mSunView.setScaleY(1.0f);
        }

        ObjectAnimator heightAnimator = ObjectAnimator
                .ofFloat(mSunView, "y", sunYStart, sunYEnd)
                .setDuration(3000);
        heightAnimator.setInterpolator(doSunset ? new AccelerateInterpolator() : new DecelerateInterpolator());

        ObjectAnimator sunsetSkyAnimator = ObjectAnimator
                .ofInt(mSkyView, "backgroundColor", sunsetSkyStart, sunsetSkyEnd)
                .setDuration(3000);
        sunsetSkyAnimator.setEvaluator(new ArgbEvaluator());

        ObjectAnimator nightSkyAnimator = ObjectAnimator
                .ofInt(mSkyView, "backgroundColor", nightSkyStart, nightSkyEnd)
                .setDuration(1500);
        nightSkyAnimator.setEvaluator(new ArgbEvaluator());

        AnimatorSet animatorSet = new AnimatorSet();
        AnimatorSet.Builder builder = animatorSet
                .play(heightAnimator)
                .with(sunsetSkyAnimator);
        if (doSunset) {
            builder.before(nightSkyAnimator);
        } else {
            builder
                    .after(nightSkyAnimator)
                    .before(mSunWidthAnimator)
                    .before(mSunHeightAnimator);
        }

        animatorSet.start();
    }
}
