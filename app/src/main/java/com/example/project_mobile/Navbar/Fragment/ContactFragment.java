package com.example.project_mobile.Navbar.Fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.project_mobile.R;

public class ContactFragment extends Fragment {

    public ContactFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);

        Button sendRequestButton = view.findViewById(R.id.send_request_button);
        Button callSupportButton = view.findViewById(R.id.call_support_button);

        sendRequestButton.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:" + getString(R.string.contact_email)));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Support Request from Device Manager");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Dear Support Team,\n\n" +
                    "I need assistance with...\n\nRegards,\n[Your Name]");

            try {
                startActivity(Intent.createChooser(emailIntent, "Send email"));
            } catch (Exception e) {
                Toast.makeText(getContext(), R.string.request_failed, Toast.LENGTH_SHORT).show();
            }
        });

        callSupportButton.setOnClickListener(v -> {
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse("tel:" + getString(R.string.contact_phone)));
            try {
                startActivity(dialIntent);
            } catch (Exception e) {
                Toast.makeText(getContext(), R.string.dial_failed, Toast.LENGTH_SHORT).show();
            }
        });



        return view;
    }
}