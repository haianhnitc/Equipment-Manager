package com.example.project_mobile.Navbar.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.transition.TransitionManager;

import com.example.project_mobile.R;
import com.google.android.material.transition.MaterialFade;

public class UserGuideFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_guide, container, false);

        // Khởi tạo TextView
        TextView userGuideTextView = view.findViewById(R.id.userGuideTextView);
        userGuideTextView.setText(R.string.user_guide_content);

        return view;
    }
}