package com.example.project_mobile.locale;

import android.content.Context;
import android.os.Build;

import com.example.project_mobile.R;

public class DeviceInfoHelper {

    public static String getDeviceInfo(Context context) {
        return context.getString(R.string.device_id) + ": " + Build.ID + "\n" +
                context.getString(R.string.device_model) + ": " + Build.MODEL + "\n" +
                context.getString(R.string.manufacturer) + ": " + Build.MANUFACTURER + "\n" +
                context.getString(R.string.brand) + ": " + Build.BRAND + "\n" +
                context.getString(R.string.android_version) + ": " + Build.VERSION.RELEASE + "\n" +
                context.getString(R.string.sdk_version) + ": " + Build.VERSION.SDK_INT;
    }
}
