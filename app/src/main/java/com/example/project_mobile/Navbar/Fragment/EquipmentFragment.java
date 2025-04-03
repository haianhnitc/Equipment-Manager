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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_mobile.Navbar.Widget.EquipmentAdapter;
import com.example.project_mobile.R;
import com.example.project_mobile.models.Equipment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EquipmentFragment extends Fragment {

    private FirebaseFirestore db;
    private RecyclerView equipmentRecyclerView;
    private EquipmentAdapter equipmentAdapter;
    private EditText searchEquipment;
    private List<Equipment> equipmentList = new ArrayList<>();
    private ImageView clearIcon;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_equipment, container, false);

        // Khởi tạo các thành phần
        db = FirebaseFirestore.getInstance();
        searchEquipment = view.findViewById(R.id.search_equipment);
        equipmentRecyclerView = view.findViewById(R.id.equipment_recycler_view);
        equipmentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        clearIcon = view.findViewById(R.id.clear_icon);

        equipmentAdapter = new EquipmentAdapter(getContext(), equipmentList);
        equipmentRecyclerView.setAdapter(equipmentAdapter);

        // Gọi fetchEquipment để lấy dữ liệu
        fetchEquipment();
        
        searchEquipment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                filterEquipment(charSequence.toString());
                if(clearIcon != null) {
                    clearIcon.setVisibility(charSequence.toString().length() > 0 ? View.VISIBLE : View.GONE);
                }
                clearIcon.setOnClickListener(v -> {
                    searchEquipment.setText("");
                });
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void fetchEquipment() {
        db.collection("equipments").addSnapshotListener((querySnapshot, error) -> {
            if (error != null) {
                Toast.makeText(getContext(),
                        getContext().getString(R.string.failed_fetch_data_firestore),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (querySnapshot != null) {
                equipmentList.clear();
                for (QueryDocumentSnapshot document : querySnapshot) {
                    Equipment equipment = document.toObject(Equipment.class);
                    equipmentList.add(equipment);
                }
                equipmentAdapter.notifyDataSetChanged();
            }
        });
    }

    private void filterEquipment(String query) {
        if(equipmentAdapter == null) return;
        List<Equipment> filteredEquipmentList = new ArrayList<>();

        for(Equipment equipment : equipmentList) {
            if(equipment.getEquipmentName().toLowerCase().contains(query.toLowerCase())) {
                filteredEquipmentList.add(equipment);
            }
        }

        equipmentAdapter.filterList(filteredEquipmentList);
    }

    private void pushFirestore() {
        List<Equipment> equipments = createSampleEquipment();
        for (Equipment equipment : equipments) {
            db.collection("equipments")
                    .document(equipment.getEquipmentId())
                    .set(equipment)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(),
                                "Sample data added successfully",
                                Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(),
                                "Failed to add sample data: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private List<Equipment> createSampleEquipment() {
        List<Equipment> equipments = new ArrayList<>();
        equipments.add(new Equipment("DEV001", "Bàn dài", 10, "Available"));
        equipments.add(new Equipment("DEV002", "Ghế dài", 20, "Available"));
        equipments.add(new Equipment("DEV003", "Ghế", 50, "Available"));
        equipments.add(new Equipment("DEV004", "Mic", 20, "Borrowed"));
        equipments.add(new Equipment("DEV005", "Bộ đàm", 15, "Available"));
        equipments.add(new Equipment("DEV006", "Bục", 3, "Available"));
        equipments.add(new Equipment("DEV007", "Máy chiếu", 3, "Borrowed"));
        equipments.add(new Equipment("DEV008", "Loa", 4, "Available"));
        equipments.add(new Equipment("DEV009", "Nam châm", 30, "Available"));
        equipments.add(new Equipment("DEV010", "Khăn trải bàn", 5, "Available"));
        return equipments;
    }
}