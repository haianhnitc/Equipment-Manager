package com.example.project_mobile.Navbar.Widget;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.project_mobile.R;
import com.example.project_mobile.models.Equipment;
import com.example.project_mobile.models.History;
import com.example.project_mobile.models.Room;
import com.example.project_mobile.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ClassroomAdapter extends RecyclerView.Adapter<ClassroomAdapter.RoomViewHolder> {
    FirebaseUser currentUser;
    FirebaseFirestore db;
    private List<Room> roomList;
    private Context context;

    public ClassroomAdapter(List<Room> roomList, Context context) {
        this.roomList = roomList;
        this.context = context;
    }

    @Override
    public RoomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_room, parent, false);
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RoomViewHolder holder, int position) {
        Room room = roomList.get(position);
        holder.roomName.setText( context.getString(R.string.room_id) + ": " + room.getId());
        holder.roomStatus.setText(context.getString(R.string.status) + ": " + room.getStatus());

//         Hiển thị dialog khi nhấn vào phòng để xem thiết bị
        holder.borrowButton.setOnClickListener(v -> openClassroomDialog(room));
    }

    private void openClassroomDialog(Room room) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.list_equipment_on_room) + " " + room.getId());

        // Lấy dữ liệu mới nhất từ Firestore
        db.collection("rooms").document(room.getId()).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Room roomData = documentSnapshot.toObject(Room.class);
                if (roomData == null || roomData.getEquipmentList() == null) {
                    Toast.makeText(context, context.getString(R.string.no_equipment), Toast.LENGTH_SHORT).show();
                }

                // Kiểm tra trạng thái phòng
                if ("Đã được mượn".equals(roomData.getStatus())) {
                    Toast.makeText(context, context.getString(R.string.room_is_occupied), Toast.LENGTH_SHORT).show();
                    return;
                }

                // Tạo danh sách checkbox cho các thiết bị
                List<Equipment> equipments = roomData.getEquipmentList();
                String[] equipmentNames = new String[equipments.size()];
                boolean[] checkedItems = new boolean[equipments.size()];

                for (int i = 0; i < equipments.size(); i++) {
                    equipmentNames[i] = equipments.get(i).getEquipmentName() + " ( " + context.getString(R.string.quantity) + ": "
                            + equipments.get(i).getQuantity() + " )";
                }

                builder.setMultiChoiceItems(equipmentNames, checkedItems, (dialog, which, isChecked) -> {
                    checkedItems[which] = isChecked;
                });

                builder.setPositiveButton(context.getString(R.string.borrow), (dialog, which) -> {
                    String borrowTime = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault()).format(new Date());

                    // Cập nhật trạng thái của phòng
                    db.collection("rooms").document(room.getId())
                            .update("status", "Đã được mượn")
                            .addOnSuccessListener(aVoid -> {
                                // Cập nhật trạng thái của các thiết bị trong phòng
                                for (int i = 0; i < equipments.size(); i++) {
                                    if (checkedItems[i]) {
                                        Equipment selectedDevice = equipments.get(i);
                                        selectedDevice.setStatus("Đã được mượn");
                                    }
                                }

                                // Cập nhật lại trạng thái thiết bị trong Firestore
                                db.collection("rooms").document(room.getId())
                                        .update("equipmentList", equipments)
                                        .addOnSuccessListener(aVoid1 -> {
                                            room.setEquipmentList(equipments);
                                            room.setStatus("Đã được mượn");
                                            saveToHistory(room, borrowTime);
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(context, context.getString(R.string.error_update_equipment_status), Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, context.getString(R.string.error_update_room_status), Toast.LENGTH_SHORT).show();
                            });
                });

                builder.setNegativeButton(context.getString(R.string.cancel), null);
                builder.create().show();
            } else {
                Toast.makeText(context, context.getString(R.string.failed_fetch_data_firestore), Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(context, context.getString(R.string.error_renting_room), Toast.LENGTH_SHORT).show();
        });
    }

    private void saveToHistory(Room room, String borrowTime) {
        if(currentUser == null) return;
        String userId = currentUser.getUid();
        db.collection("users").document(currentUser.getUid()).get().addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);

                // Lưu toàn bộ danh sách thiết bị của phòng, không chỉ các thiết bị đã mượn
                Room roomWithAllDevices = new Room(room.getId(), room.getEquipmentList(), "Đã được mượn");
                History history = new History(user.getStudentId(),"room", roomWithAllDevices, null
                        , borrowTime, null);

                // Lưu lịch sử vào Firestore
                db.collection("history").add(history)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(context, context.getString(R.string.room_borrowed_successfully), Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, context.getString(R.string.room_borrowed_failed), Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }


    @Override
    public int getItemCount() {
        return roomList.size();
    }

    public class RoomViewHolder extends RecyclerView.ViewHolder {
        TextView roomName, roomStatus;
        Button borrowButton;

        public RoomViewHolder(View itemView) {
            super(itemView);
            roomName = itemView.findViewById(R.id.room_name);
            roomStatus = itemView.findViewById(R.id.room_status);
            borrowButton = itemView.findViewById(R.id.borrow_button);
        }
    }

    public void filterList(List<Room> filteredList) {
        this.roomList = filteredList;
        notifyDataSetChanged();
    }

}
