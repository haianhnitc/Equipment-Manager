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

import com.example.project_mobile.Navbar.Widget.ClassroomAdapter;
import com.example.project_mobile.R;
import com.example.project_mobile.models.Equipment;
import com.example.project_mobile.models.Room;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ClassroomFragment extends Fragment {

    private FirebaseFirestore db;
    private RecyclerView classroomRecycleView;
    private ClassroomAdapter classroomAdapter;
    private List<Room> roomList = new ArrayList<>();
    private EditText searchRoom;
    private ImageView clearIcon;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_classroom, container, false);

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();
        classroomRecycleView = view.findViewById(R.id.classroom_recycler_view);
        searchRoom = view.findViewById(R.id.search_classroom);
        clearIcon = view.findViewById(R.id.clear_room);

        classroomRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));
        fetchRoom();

        searchRoom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterRoomList(charSequence.toString());
                if(clearIcon != null) {
                    clearIcon.setVisibility(charSequence.toString().length() > 0 ? View.VISIBLE : View.GONE);
                }
                clearIcon.setOnClickListener(v -> {
                    searchRoom.setText("");
                });
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        return view;
    }

    private void fetchRoom() {
        db.collection("rooms").addSnapshotListener((querySnapshot, error) -> {
            if (error != null) {
                Toast.makeText(getContext(), getContext().getString(R.string.failed_fetch_data_firestore), Toast.LENGTH_SHORT).show();
                return;
            }
            if (querySnapshot != null) {
                roomList.clear();
                for (QueryDocumentSnapshot document : querySnapshot) {
                    Room room = document.toObject(Room.class);
                    roomList.add(room);
                }
                if (classroomAdapter == null) {
                    classroomAdapter = new ClassroomAdapter(roomList, getContext());
                    classroomRecycleView.setAdapter(classroomAdapter);
                } else {
                    classroomAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void filterRoomList(String query) {

        if (classroomAdapter == null) {
            return;
        }
        List<Room> filteredList = new ArrayList<>();
        for (Room room : roomList) {
            if (room.getId().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(room);
            }
        }

        classroomAdapter.filterList(filteredList);
    }

//    private void pushSampleRoomsToFirestore() {
//        List<Room> sampleRooms = createSampleRooms();
//        for (Room room : sampleRooms) {
//            db.collection("rooms").document(room.getId()).set(room)
//                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "DocumentSnapshot successfully written!"))
//                    .addOnFailureListener(e -> Log.w("Firestore", "Error writing document", e));
//        }
//    }
//private List<Room> createSampleRooms() {
//    List<Room> roomList = new ArrayList<>();
//
//    // Phòng A2101 (Phòng học nhỏ)
//    List<Equipment> devicesInA2101 = new ArrayList<>();
//    devicesInA2101.add(new Equipment("DKDH_A2101", "Điều khiển điều hòa", 1, "Sẵn sàng"));
//    devicesInA2101.add(new Equipment("DKMC_A2101", "Điều khiển máy chiếu", 1, "Sẵn sàng"));
//    devicesInA2101.add(new Equipment("Mic_A2101", "Mic", 2, "Sẵn sàng"));
//    devicesInA2101.add(new Equipment("BL_A2101", "Bút lông", 2, "Sẵn sàng"));
//    Room roomA2101 = new Room("A2101", devicesInA2101, "Sẵn sàng", "(30 -> 40 người)");
//    roomList.add(roomA2101);
//
//    // Phòng C202 (Phòng học trung bình)
//    List<Equipment> devicesInC202 = new ArrayList<>();
//    devicesInC202.add(new Equipment("DKDH_C202", "Điều khiển điều hòa", 1, "Sẵn sàng"));
//    devicesInC202.add(new Equipment("DKMC_C202", "Điều khiển máy chiếu", 1, "Sẵn sàng"));
//    devicesInC202.add(new Equipment("Mic_C202", "Mic", 2, "Sẵn sàng"));
//    devicesInC202.add(new Equipment("BL_C202", "Bút lông", 2, "Sẵn sàng"));
//    Room roomC202 = new Room("C202", devicesInC202, "Sẵn sàng", "(40 -> 50 người)");
//    roomList.add(roomC202);
//
//    // Phòng B103 (Phòng học nhỏ)
//    List<Equipment> devicesInB103 = new ArrayList<>();
//    devicesInB103.add(new Equipment("DKDH_B103", "Điều khiển điều hòa", 1, "Sẵn sàng"));
//    devicesInB103.add(new Equipment("DKMC_B103", "Điều khiển máy chiếu", 1, "Sẵn sàng"));
//    devicesInB103.add(new Equipment("Mic_B103", "Mic", 2, "Sẵn sàng"));
//    devicesInB103.add(new Equipment("BL_B103", "Bút lông", 2, "Sẵn sàng"));
//    Room roomB103 = new Room("B103", devicesInB103, "Sẵn sàng", "(25 -> 35 người)");
//    roomList.add(roomB103);
//
//    // Phòng C304 (Phòng học trung bình)
//    List<Equipment> devicesInC304 = new ArrayList<>();
//    devicesInC304.add(new Equipment("DKDH_C304", "Điều khiển điều hòa", 1, "Sẵn sàng"));
//    devicesInC304.add(new Equipment("DKMC_C304", "Điều khiển máy chiếu", 1, "Sẵn sàng"));
//    devicesInC304.add(new Equipment("Mic_C304", "Mic", 2, "Sẵn sàng"));
//    devicesInC304.add(new Equipment("BL_C304", "Bút lông", 2, "Sẵn sàng"));
//    Room roomC304 = new Room("C304", devicesInC304, "Sẵn sàng", "(40 -> 50 người)");
//    roomList.add(roomC304);
//
//    // Phòng B105 (Phòng học nhỏ)
//    List<Equipment> devicesInB105 = new ArrayList<>();
//    devicesInB105.add(new Equipment("DKDH_B105", "Điều khiển điều hòa", 1, "Sẵn sàng"));
//    devicesInB105.add(new Equipment("DKMC_B105", "Điều khiển máy chiếu", 1, "Sẵn sàng"));
//    devicesInB105.add(new Equipment("Mic_B105", "Mic", 2, "Sẵn sàng"));
//    devicesInB105.add(new Equipment("BL_B105", "Bút lông", 2, "Sẵn sàng"));
//    Room roomB105 = new Room("B105", devicesInB105, "Sẵn sàng", "(20 -> 30 người)");
//    roomList.add(roomB105);
//
//    // Phòng A2610 (Phòng học lớn)
//    List<Equipment> devicesInA2610 = new ArrayList<>();
//    devicesInA2610.add(new Equipment("DKDH_A2610", "Điều khiển điều hòa", 2, "Sẵn sàng"));
//    devicesInA2610.add(new Equipment("DKMC_A2610", "Điều khiển máy chiếu", 1, "Sẵn sàng"));
//    devicesInA2610.add(new Equipment("Mic_A2610", "Mic", 3, "Sẵn sàng"));
//    devicesInA2610.add(new Equipment("BL_A2610", "Bút lông", 4, "Sẵn sàng"));
//    Room roomA2610 = new Room("A2610", devicesInA2610, "Sẵn sàng", "(60 -> 80 người)");
//    roomList.add(roomA2610);
//
//    // Phòng A2614 (Phòng học lớn)
//    List<Equipment> devicesInA2614 = new ArrayList<>();
//    devicesInA2614.add(new Equipment("DKDH_A2614", "Điều khiển điều hòa", 2, "Sẵn sàng"));
//    devicesInA2614.add(new Equipment("DKMC_A2614", "Điều khiển máy chiếu", 1, "Sẵn sàng"));
//    devicesInA2614.add(new Equipment("Mic_A2614", "Mic", 3, "Sẵn sàng"));
//    devicesInA2614.add(new Equipment("BL_A2614", "Bút lông", 4, "Sẵn sàng"));
//    Room roomA2614 = new Room("A2614", devicesInA2614, "Sẵn sàng", "(60 -> 80 người)");
//    roomList.add(roomA2614);
//
//    // Phòng D101 (Phòng học nhỏ)
//    List<Equipment> devicesInD101 = new ArrayList<>();
//    devicesInD101.add(new Equipment("DKDH_D101", "Điều khiển điều hòa", 1, "Sẵn sàng"));
//    devicesInD101.add(new Equipment("DKMC_D101", "Điều khiển máy chiếu", 1, "Sẵn sàng"));
//    devicesInD101.add(new Equipment("Mic_D101", "Mic", 2, "Sẵn sàng"));
//    devicesInD101.add(new Equipment("BL_D101", "Bút lông", 2, "Sẵn sàng"));
//    Room roomD101 = new Room("D101", devicesInD101, "Sẵn sàng", "(25 -> 35 người)");
//    roomList.add(roomD101);
//
//    // Phòng D205 (Phòng học trung bình)
//    List<Equipment> devicesInD205 = new ArrayList<>();
//    devicesInD205.add(new Equipment("DKDH_D205", "Điều khiển điều hòa", 1, "Sẵn sàng"));
//    devicesInD205.add(new Equipment("DKMC_D205", "Điều khiển máy chiếu", 1, "Sẵn sàng"));
//    devicesInD205.add(new Equipment("Mic_D205", "Mic", 2, "Sẵn sàng"));
//    devicesInD205.add(new Equipment("BL_D205", "Bút lông", 2, "Sẵn sàng"));
//    Room roomD205 = new Room("D205", devicesInD205, "Sẵn sàng", "(40 -> 50 người)");
//    roomList.add(roomD205);
//
//    // Sửa lỗi trùng ID: Phòng D405 (Phòng học trung bình)
//    List<Equipment> devicesInD405 = new ArrayList<>();
//    devicesInD405.add(new Equipment("DKDH_D405", "Điều khiển điều hòa", 1, "Sẵn sàng"));
//    devicesInD405.add(new Equipment("DKMC_D405", "Điều khiển máy chiếu", 1, "Sẵn sàng"));
//    devicesInD405.add(new Equipment("Mic_D405", "Mic", 2, "Sẵn sàng"));
//    devicesInD405.add(new Equipment("BL_D405", "Bút lông", 2, "Sẵn sàng"));
//    Room roomD405 = new Room("D405", devicesInD405, "Sẵn sàng", "(40 -> 50 người)");
//    roomList.add(roomD405);
//
//    // Phòng A3102 (Phòng học nhỏ)
//    List<Equipment> devicesInA3102 = new ArrayList<>();
//    devicesInA3102.add(new Equipment("DKDH_A3102", "Điều khiển điều hòa", 1, "Sẵn sàng"));
//    devicesInA3102.add(new Equipment("DKMC_A3102", "Điều khiển máy chiếu", 1, "Sẵn sàng"));
//    devicesInA3102.add(new Equipment("Mic_A3102", "Mic", 2, "Sẵn sàng"));
//    devicesInA3102.add(new Equipment("BL_A3102", "Bút lông", 2, "Sẵn sàng"));
//    Room roomA3102 = new Room("A3102", devicesInA3102, "Sẵn sàng", "(20 -> 30 người)");
//    roomList.add(roomA3102);
//
//    // Phòng B204 (Phòng học trung bình)
//    List<Equipment> devicesInB204 = new ArrayList<>();
//    devicesInB204.add(new Equipment("DKDH_B204", "Điều khiển điều hòa", 1, "Sẵn sàng"));
//    devicesInB204.add(new Equipment("DKMC_B204", "Điều khiển máy chiếu", 1, "Sẵn sàng"));
//    devicesInB204.add(new Equipment("Mic_B204", "Mic", 2, "Sẵn sàng"));
//    devicesInB204.add(new Equipment("BL_B204", "Bút lông", 2, "Sẵn sàng"));
//    Room roomB204 = new Room("B204", devicesInB204, "Sẵn sàng", "(35 -> 45 người)");
//    roomList.add(roomB204);
//
//    // Phòng C405 (Phòng học lớn)
//    List<Equipment> devicesInC405 = new ArrayList<>();
//    devicesInC405.add(new Equipment("DKDH_C405", "Điều khiển điều hòa", 2, "Sẵn sàng"));
//    devicesInC405.add(new Equipment("DKMC_C405", "Điều khiển máy chiếu", 1, "Sẵn sàng"));
//    devicesInC405.add(new Equipment("Mic_C405", "Mic", 3, "Sẵn sàng"));
//    devicesInC405.add(new Equipment("BL_C405", "Bút lông", 4, "Sẵn sàng"));
//    Room roomC405 = new Room("C405", devicesInC405, "Sẵn sàng", "(50 -> 70 người)");
//    roomList.add(roomC405);
//
//    // Phòng D306 (Phòng học nhỏ)
//    List<Equipment> devicesInD306 = new ArrayList<>();
//    devicesInD306.add(new Equipment("DKDH_D306", "Điều khiển điều hòa", 1, "Sẵn sàng"));
//    devicesInD306.add(new Equipment("DKMC_D306", "Điều khiển máy chiếu", 1, "Sẵn sàng"));
//    devicesInD306.add(new Equipment("Mic_D306", "Mic", 2, "Sẵn sàng"));
//    devicesInD306.add(new Equipment("BL_D306", "Bút lông", 2, "Sẵn sàng"));
//    Room roomD306 = new Room("D306", devicesInD306, "Sẵn sàng", "(25 -> 35 người)");
//    roomList.add(roomD306);
//
//    // Phòng E101 (Phòng học nhỏ)
//    List<Equipment> devicesInE101 = new ArrayList<>();
//    devicesInE101.add(new Equipment("DKDH_E101", "Điều khiển điều hòa", 1, "Sẵn sàng"));
//    devicesInE101.add(new Equipment("DKMC_E101", "Điều khiển máy chiếu", 1, "Sẵn sàng"));
//    devicesInE101.add(new Equipment("Mic_E101", "Mic", 2, "Sẵn sàng"));
//    devicesInE101.add(new Equipment("BL_E101", "Bút lông", 2, "Sẵn sàng"));
//    Room roomE101 = new Room("E101", devicesInE101, "Sẵn sàng", "(20 -> 30 người)");
//    roomList.add(roomE101);
//
//    // Phòng E305 (Phòng học trung bình)
//    List<Equipment> devicesInE305 = new ArrayList<>();
//    devicesInE305.add(new Equipment("DKDH_E305", "Điều khiển điều hòa", 1, "Sẵn sàng"));
//    devicesInE305.add(new Equipment("DKMC_E305", "Điều khiển máy chiếu", 1, "Sẵn sàng"));
//    devicesInE305.add(new Equipment("Mic_E305", "Mic", 2, "Sẵn sàng"));
//    devicesInE305.add(new Equipment("BL_E305", "Bút lông", 2, "Sẵn sàng"));
//    Room roomE305 = new Room("E305", devicesInE305, "Sẵn sàng", "(40 -> 50 người)");
//    roomList.add(roomE305);
//
//    // Phòng HOITHAOB101 (Phòng hội thảo lớn - Liên hệ)
//    List<Equipment> devicesInHOITHAOB101 = new ArrayList<>();
//    devicesInHOITHAOB101.add(new Equipment("DKDH_HOITHAOB101", "Điều khiển điều hòa", 3, "Sẵn sàng"));
//    devicesInHOITHAOB101.add(new Equipment("DKMC_HOITHAOB101", "Điều khiển máy chiếu", 2, "Sẵn sàng"));
//    devicesInHOITHAOB101.add(new Equipment("Mic_HOITHAOB101", "Mic", 5, "Sẵn sàng"));
//    devicesInHOITHAOB101.add(new Equipment("BL_HOITHAOB101", "Bút lông", 6, "Sẵn sàng"));
//    devicesInHOITHAOB101.add(new Equipment("Loa_HOITHAOB101", "Loa hội thảo", 2, "Sẵn sàng"));
//    Room roomHOITHAOB101 = new Room("HOITHAOB101", devicesInHOITHAOB101, "Liên hệ", "(100 -> 150 người)");
//    roomList.add(roomHOITHAOB101);
//
//    // Phòng HOITHAOB102 (Phòng hội thảo lớn - Liên hệ)
//    List<Equipment> devicesInHOITHAOB102 = new ArrayList<>();
//    devicesInHOITHAOB102.add(new Equipment("DKDH_HOITHAOB102", "Điều khiển điều hòa", 3, "Sẵn sàng"));
//    devicesInHOITHAOB102.add(new Equipment("DKMC_HOITHAOB102", "Điều khiển máy chiếu", 2, "Sẵn sàng"));
//    devicesInHOITHAOB102.add(new Equipment("Mic_HOITHAOB102", "Mic", 5, "Sẵn sàng"));
//    devicesInHOITHAOB102.add(new Equipment("BL_HOITHAOB102", "Bút lông", 6, "Sẵn sàng"));
//    devicesInHOITHAOB102.add(new Equipment("Loa_HOITHAOB102", "Loa hội thảo", 2, "Sẵn sàng"));
//    Room roomHOITHAOB102 = new Room("HOITHAOB102", devicesInHOITHAOB102, "Liên hệ", "(100 -> 150 người)");
//    roomList.add(roomHOITHAOB102);
//
//    // Phòng HOITRUONGA2 (Phòng hội trường lớn - Liên hệ)
//    List<Equipment> devicesInHOITRUONGA2 = new ArrayList<>();
//    devicesInHOITRUONGA2.add(new Equipment("DKDH_HOITRUONGA2", "Điều khiển điều hòa", 4, "Sẵn sàng"));
//    devicesInHOITRUONGA2.add(new Equipment("DKMC_HOITRUONGA2", "Điều khiển máy chiếu", 2, "Sẵn sàng"));
//    devicesInHOITRUONGA2.add(new Equipment("Mic_HOITRUONGA2", "Mic", 6, "Sẵn sàng"));
//    devicesInHOITRUONGA2.add(new Equipment("BL_HOITRUONGA2", "Bút lông", 8, "Sẵn sàng"));
//    devicesInHOITRUONGA2.add(new Equipment("Loa_HOITRUONGA2", "Loa hội trường", 4, "Sẵn sàng"));
//    devicesInHOITRUONGA2.add(new Equipment("MC_HOITRUONGA2", "Máy chiếu phụ", 1, "Sẵn sàng"));
//    Room roomHOITRUONGA2 = new Room("HOITRUONGA2", devicesInHOITRUONGA2, "Liên hệ", "(200 -> 300 người)");
//    roomList.add(roomHOITRUONGA2);
//
//    return roomList;
//}

}

