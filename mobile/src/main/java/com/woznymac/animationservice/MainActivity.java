package com.woznymac.animationservice;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity implements Window.OnRestrictedCaptionAreaChangedListener {

    /** ID of stack where fullscreen activities are normally launched into. */
    public static final int FULLSCREEN_WORKSPACE_STACK_ID = 1;
    /** ID of stack where freeform/resized activities are normally launched into. */
    public static final int FREEFORM_WORKSPACE_STACK_ID = FULLSCREEN_WORKSPACE_STACK_ID + 1;

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getWindow().setRestrictedCaptionAreaListener(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchFreeFormActivity();
            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        View parentLayout = findViewById(android.R.id.content);
        switch (item.getItemId()) {
            case R.id.freeform_mode_checking:
                if (isFreefromModeEnabled()) {
                    Snackbar.make(parentLayout, "Freeform mode enabled", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                else {
                    Snackbar.make(parentLayout, "Freeform mode disabled", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isFreefromModeEnabled() {
        Log.d(TAG, "FEATURE_FREEFORM_WINDOW_MANAGEMENT = " + getPackageManager().hasSystemFeature(PackageManager.FEATURE_FREEFORM_WINDOW_MANAGEMENT));
        Log.d(TAG, "enable_freeform_support = " + Settings.Global.getInt(getContentResolver(), "enable_freeform_support", 0));

        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_FREEFORM_WINDOW_MANAGEMENT) ||
                Settings.Global.getInt(getContentResolver(), "enable_freeform_support", 0) != 0;
    }


    private ActivityOptions getActivityOptions(int applicationType) {
        Log.d(TAG, "on enter: getActivityOptions");
        ActivityOptions options = ActivityOptions.makeBasic();
        try {
            Method method = ActivityOptions.class.getDeclaredMethod("setLaunchStackId", int.class);
            method.setAccessible(true);
            method.invoke(options, applicationType);
            Log.d(TAG, "setLaunchStackId = " + applicationType);
        } catch (Exception e) {
            Log.e(TAG, "setLaunchStackId error");
            e.printStackTrace();
        }
        return options;
    }

    private void launchFreeFormActivity() {
        Log.d(TAG, "on enter: launchFreeFormActivity");
        Intent intent =
                new Intent(this, MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT |
                                Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        ActivityOptions activityOptions = getActivityOptions(FREEFORM_WORKSPACE_STACK_ID);

        Rect bounds = new Rect(0, 100, 720, 640);

        activityOptions.setLaunchBounds(bounds);

        Bundle bundle = activityOptions.toBundle();

        Log.d(TAG, bundle.toString());
        Log.d(TAG, intent.toString());
        try {
            startActivity(intent, bundle);
            Log.d(TAG, "startActivity");
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "startActivity");
            e.printStackTrace();
        }
        finish();
    }

    @Override
    public void onRestrictedCaptionAreaChanged(Rect rect) {
        Log.d(TAG, "onRestrictedCaptionAreaChanged: " + rect);
    }
}
