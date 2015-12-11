package com.bignerdranch.android.draganddraw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by julianlo on 12/10/15.
 */
public class BoxDrawingView extends View {
    private static final String TAG = "BoxDrawingView";

    private static final String STATE_ARG_VIEW_SAVED_INSTANCE_STATE = "view_saved_instance_state";
    private static final String STATE_ARG_ORIGIN_ARRAY = "origin_array";
    private static final String STATE_ARG_CURRENT_ARRAY = "current_array";

    private Box mCurrentBox;
    private List<Box> mBoxen = new ArrayList<>();
    private Paint mBoxPaint;
    private Paint mBackgroundPaint;

    // Used when creating the view in code
    public BoxDrawingView(Context context) {
        this(context, null);
    }

    // Used when inflating the view from XML
    public BoxDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Paint the boxes a nice semitransparent red (ARBG)
        mBoxPaint = new Paint();
        mBoxPaint.setColor(0x22ff0000);

        // Paint the background off-white
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(0xfff8efe0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        PointF current = new PointF(event.getX(), event.getY());
        String action = "";

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                action = "ACTION_DOWN";
                // Reset drawing state
                mCurrentBox = new Box(current);
                mBoxen.add(mCurrentBox);
                break;

            case MotionEvent.ACTION_MOVE:
                action = "ACTION_MOVE";
                if (mCurrentBox != null) {
                    mCurrentBox.setCurrent(current);
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_UP:
                action = "ACTION_UP";
                mCurrentBox = null;
                break;

            case MotionEvent.ACTION_CANCEL:
                action = "ACTION_CANCEL";
                mCurrentBox = null;
                break;
        }

        Log.i(TAG, action + " at x=" + current.x + ", y=" + current.y);

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Fill the background
        canvas.drawPaint(mBackgroundPaint);

        for (Box box : mBoxen) {
            float left = Math.min(box.getOrigin().x, box.getCurrent().x);
            float right = Math.max(box.getOrigin().x, box.getCurrent().x);
            float top = Math.min(box.getOrigin().y, box.getCurrent().y);
            float bottom = Math.max(box.getOrigin().y, box.getCurrent().y);

            canvas.drawRect(left, top, right, bottom, mBoxPaint);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(STATE_ARG_VIEW_SAVED_INSTANCE_STATE, super.onSaveInstanceState());

        ArrayList<PointF> origins = new ArrayList<>(mBoxen.size());
        ArrayList<PointF> currents = new ArrayList<>(mBoxen.size());

        for (Box box : mBoxen) {
            origins.add(box.getOrigin());
            currents.add(box.getCurrent());
        }

        bundle.putParcelableArrayList(STATE_ARG_ORIGIN_ARRAY, origins);
        bundle.putParcelableArrayList(STATE_ARG_CURRENT_ARRAY, currents);

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle)state;

        ArrayList<PointF> origins = bundle.getParcelableArrayList(STATE_ARG_ORIGIN_ARRAY);
        ArrayList<PointF> currents = bundle.getParcelableArrayList(STATE_ARG_CURRENT_ARRAY);

        for (int i = 0; i < origins.size(); i++) {
            Box box = new Box(origins.get(i));
            box.setCurrent(currents.get(i));
            mBoxen.add(box);
        }

        super.onRestoreInstanceState(bundle.getParcelable(STATE_ARG_VIEW_SAVED_INSTANCE_STATE));
    }
}
