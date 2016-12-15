package com.cgmaybe.globe.service;

import android.accessibilityservice.AccessibilityService;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import com.cgmaybe.globe.R;
import com.cgmaybe.globe.activity.AdminReceiver;
import com.cgmaybe.globe.widget.GlobeFloatButton;


/**
 * Created by moubiao on 2016/12/13.
 */

public class MyAccessibilityService extends AccessibilityService {

    @Override
    public void onCreate() {
        super.onCreate();
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View view = inflater.inflate(R.layout.float_button, null);
        GlobeFloatButton globeFloatButton = (GlobeFloatButton) view.findViewById(R.id.float_bt);

        final GestureDetector gestureDetector = new GestureDetector(getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
            }

            @Override
            public void onLongPress(MotionEvent e) {
                lockScreen();
                super.onLongPress(e);
            }
        });

        globeFloatButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
    }

    private void lockScreen() {
        DevicePolicyManager policyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName componentName = new ComponentName(this, AdminReceiver.class);
        if (policyManager.isAdminActive(componentName)) {
            policyManager.lockNow();
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInterrupt() {

    }
}
