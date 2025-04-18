package com.example.project_mobile.Navbar.Fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_mobile.Navbar.Widget.HistoryAdapter;
import com.example.project_mobile.R;
import com.example.project_mobile.models.Equipment;
import com.example.project_mobile.models.History;
import com.example.project_mobile.models.Room;
import com.example.project_mobile.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private HistoryAdapter historyAdapter;
    private List<History> historyList = new ArrayList<>();
    private EditText historySearch;
    private ImageView clearIcon;
    private String studentId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = view.findViewById(R.id.recycler_history);
        historySearch = view.findViewById(R.id.search_history);
        clearIcon = view.findViewById(R.id.clear_icon);
        db = FirebaseFirestore.getInstance();

        historyAdapter = new HistoryAdapter(historyList, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(historyAdapter);


        historySearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterHistory(s.toString());
                if(clearIcon != null) {
                    clearIcon.setVisibility(s.toString().length() > 0 ? View.VISIBLE : View.GONE);
                }
                clearIcon.setOnClickListener(v -> {
                    historySearch.setText("");

                });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        loadHistoryData();
        return view;
    }
    private void loadHistoryData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }

        db.collection("users").document(currentUser.getUid()).get().addOnSuccessListener(documentSnapshot -> {
            User user = documentSnapshot.toObject(User.class);
            studentId = user.getStudentId();

            db.collection("history").orderBy("borrowTime", Query.Direction.DESCENDING).
                    whereEqualTo("studentId", studentId)
                     // Sắp xếp theo borrowTime giảm dần (mới nhất trước)
                    .addSnapshotListener((querySnapshot, error) -> {
                        if (error != null) {
                            Toast.makeText(getContext(), getContext().getString(R.string.failed_fetch_data_firestore), Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (querySnapshot != null) {
                            historyList.clear();
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                History history = document.toObject(History.class);
                                history.setId(document.getId());
                                historyList.add(history);
                            }
                            if (historyAdapter == null) {
                                historyAdapter = new HistoryAdapter(historyList, getContext());
                                recyclerView.setAdapter(historyAdapter);
                            } else {
                                historyAdapter.notifyDataSetChanged();
                            }
                        }
                    });

        }).addOnFailureListener(e-> {
           Toast.makeText(getContext(), getContext().getString(R.string.failed_fetch_data_firestore) + e.getMessage(), Toast.LENGTH_SHORT).show();
        });

    }

    private void filterHistory(String query) {
        List<History> filterHistory = new ArrayList<>();

        for(History history : historyList) {
            Equipment equipment = history.getEquipment();
            Room room = history.getRoom();
            if(room != null) {
                if(room.getId().toLowerCase().contains(query.toString().toLowerCase())) {
                    filterHistory.add(history);
                }
            } else if(equipment != null) {
                if(equipment.getEquipmentName().toLowerCase().contains(query.toString().toLowerCase())) {
                    filterHistory.add(history);
                }
            }

        }

        historyAdapter.filterList(filterHistory);
    }


}
