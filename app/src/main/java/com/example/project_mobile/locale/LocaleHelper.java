package com.example.project_mobile.locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import androidx.appcompat.app.AppCompatDelegate;

import java.util.Locale;

public class LocaleHelper {

    public static void setLocale(Context context) {

        SharedPreferences sharedPreferences = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("DarkMode", false);

        // Áp dụng chế độ sáng/tối
        AppCompatDelegate.setDefaultNightMode(isDarkMode ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        // sửa đổi ngôn ngữ
        String language = sharedPreferences.getString("Language", "vi");
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);

        // Áp dụng cấu hình cho cả context
        context.createConfigurationContext(config);

        // Cập nhật cấu hình toàn hệ thống
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
}
