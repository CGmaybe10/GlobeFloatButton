package com.cgmaybe.globe.activity;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.view.accessibility.AccessibilityManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.TextView;

import com.cgmaybe.globe.R;
import com.cgmaybe.globe.service.MyAccessibilityService;
import com.cgmaybe.globe.util.SettingsCompat;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "moubiao";
    private static final int ACC_REQUEST_CODE = 10000;
    private static final int DEVICE_REQUEST_CODE = 10001;
    private TextView mPopTv, mAccTV, mDeviceTv;
    private Button mPopBT, mAccBT, mDeviceBT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
        initView();
        setListener();
        checkPermission();
    }

    private void initData() {
        startService(new Intent(MainActivity.this, MyAccessibilityService.class));
    }

    private void initView() {
        mPopTv = (TextView) findViewById(R.id.pop_tv);
        mAccTV = (TextView) findViewById(R.id.acc_tv);
        mDeviceTv = (TextView) findViewById(R.id.device_tv);

        mPopBT = (Button) findViewById(R.id.pop_bt);
        mAccBT = (Button) findViewById(R.id.accessibility_bt);
        mDeviceBT = (Button) findViewById(R.id.finish_bt);
    }

    private void setListener() {
        mPopBT.setOnClickListener(this);
        mAccBT.setOnClickListener(this);
        mDeviceBT.setOnClickListener(this);
    }

    private void checkPermission() {
        checkPopPermission();
        checkAccPermission();
        checkDevicePermission();
    }

    private void checkPopPermission() {
        if (SettingsCompat.canDrawOverlays(this)) {
            mPopTv.setText(R.string.pop_permission_grant);
            mPopBT.setEnabled(false);
        } else {
            mPopTv.setText(R.string.pop_permission_no_grant);
            mPopBT.setEnabled(true);
        }
    }

    /**
     * 检查无障碍权限
     */
    private void checkAccPermission() {
        if (mAccTV == null || mAccBT == null) {
            return;
        }
        AccessibilityManager manager = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> list = AccessibilityManagerCompat.getEnabledAccessibilityServiceList(manager,
                AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        for (int i = 0; i < list.size(); i++) {
            Log.d(TAG, "checkPermission: 已经可用的服务列表 = " + list.get(i).getId());
            if ("com.cgmaybe.globe/.service.MyAccessibilityService".equals(list.get(i).getId())) {
                mAccTV.setText(R.string.acc_permission_grant);
                mAccBT.setEnabled(false);
                break;
            } else {
                mAccTV.setText(R.string.acc_permission_not_grant);
                mAccBT.setEnabled(true);
            }
        }
    }

    /**
     * 检查设备管理权限
     */
    private void checkDevicePermission() {
        if (mDeviceTv == null || mDeviceBT == null) {
            return;
        }
        DevicePolicyManager policyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName componentName = new ComponentName(this, AdminReceiver.class);
        if (policyManager.isAdminActive(componentName)) {
            mDeviceTv.setText(R.string.device_permission_grant);
            mDeviceBT.setEnabled(false);
        } else {
            mDeviceTv.setText(R.string.device_permission_not_grant);
            mDeviceBT.setEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPopPermission();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.pop_bt:
                requestPopPermission();
                break;
            case R.id.accessibility_bt:
                requestAccPermission();
                break;
            case R.id.finish_bt:
                requestDevicePermission();
                break;
            default:
                break;
        }
    }

    private void requestPopPermission() {
        SettingsCompat.manageDrawOverlays(this);
    }

    private void requestAccPermission() {
        Intent intentSet = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intentSet);
        startActivityForResult(intentSet, ACC_REQUEST_CODE);
    }

    private void requestDevicePermission() {
        ComponentName componentName = new ComponentName(this, AdminReceiver.class);
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.device_manager_permission_tip));
        startActivityForResult(intent, DEVICE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACC_REQUEST_CODE:
                checkAccPermission();
                break;
            case DEVICE_REQUEST_CODE:
                checkDevicePermission();
                break;
            default:
                break;
        }
    }
}
