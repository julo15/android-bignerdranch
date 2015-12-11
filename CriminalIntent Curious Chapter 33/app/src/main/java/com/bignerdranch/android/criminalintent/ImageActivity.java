package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by julianlo on 12/11/15.
 */
public class ImageActivity extends AppCompatActivity {

    private static final String EXTRA_FILE_PATH = "com.bignerdranch.android.criminalintent.file_path";

    public static Intent newIntent(Context context, String filePath) {
        Intent intent = new Intent(context, ImageActivity.class);
        intent.putExtra(EXTRA_FILE_PATH, filePath);

        return intent;
    }

    public static void startWithTransition(Activity activity, Intent intent, View sourceView) {
        ViewCompat.setTransitionName(sourceView, "image");
        ActivityOptionsCompat options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(activity, sourceView, "image");
        activity.startActivity(intent, options.toBundle());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        ImageView imageView = (ImageView)findViewById(R.id.image_view);

        String filePath = getIntent().getStringExtra(EXTRA_FILE_PATH);
        Bitmap bitmap = PictureUtils.getScaledBitmap(filePath, 500, 500);
        //imageView.setImageBitmap(bitmap);

        imageView.setImageResource(android.R.drawable.ic_delete);
    }
}
