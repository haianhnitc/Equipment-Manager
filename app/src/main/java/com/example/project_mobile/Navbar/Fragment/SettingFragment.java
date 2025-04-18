package com.example.project_mobile.Navbar.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.project_mobile.R;
import com.example.project_mobile.locale.DeviceInfoHelper;
import com.example.project_mobile.locale.LocaleHelper;

public class SettingFragment extends Fragment {

    private Switch darkModeSwitch;
    private Spinner languageSpinner;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        // Áp dụng chế độ trước khi inflate layout
        boolean isDarkMode = sharedPreferences.getBoolean("DarkMode", false);
        AppCompatDelegate.setDefaultNightMode(isDarkMode ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        darkModeSwitch = view.findViewById(R.id.darkModeSwitch);
        languageSpinner = view.findViewById(R.id.languageSpinner);

        TextView deviceInfoTextView = view.findViewById(R.id.deviceInfoTextView);
        deviceInfoTextView.setText(DeviceInfoHelper.getDeviceInfo(getContext()));

        // Thiết lập chế độ sáng/tối
        boolean isDarkMode = sharedPreferences.getBoolean("DarkMode", false);
        darkModeSwitch.setChecked(isDarkMode);
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("DarkMode", isChecked);
            editor.apply();
            // Thay đổi chế độ và làm mới activity
            AppCompatDelegate.setDefaultNightMode(isChecked ?
                    AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            requireActivity().recreate();
        });

        // Lấy ngôn ngữ hiện tại
        String currentLanguage = sharedPreferences.getString("Language", "vi");

        switch (currentLanguage) {
            case "vi":
                languageSpinner.setSelection(1);
                break;
            case "zh":
                languageSpinner.setSelection(2);
                break;
            default:
                languageSpinner.setSelection(0);
                break;
        }

        // Xử lý khi người dùng chọn ngôn ngữ mới
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLanguage = parent.getItemAtPosition(position).toString();
                changeLanguage(selectedLanguage, currentLanguage);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return view;
    }

    // Phương thức thay đổi ngôn ngữ
    private void changeLanguage(String language, String currentLanguage) {
        String languageCode;

        switch (language) {
            case "Tiếng Việt":
                languageCode = "vi";
                break;
            case "中文":
                languageCode = "zh";
                break;
            default:
                languageCode = "en";
        }

        // Chỉ thay đổi ngôn ngữ nếu ngôn ngữ mới khác với ngôn ngữ hiện tại
        if (!currentLanguage.equals(languageCode)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("Language", languageCode);
            editor.apply();

            // Áp dụng ngôn ngữ mới và làm mới activity
            LocaleHelper.setLocale(getActivity());
            requireActivity().recreate();

        }
    }
}