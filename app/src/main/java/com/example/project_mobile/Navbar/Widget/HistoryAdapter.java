package com.example.project_mobile.Navbar.Widget;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_mobile.R;
import com.example.project_mobile.models.Equipment;
import com.example.project_mobile.models.History;
import com.example.project_mobile.models.Room;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private FirebaseFirestore db;
    private List<History> historyList;
    private Context context;

    public HistoryAdapter(List<History> historyList, Context context) {
        this.historyList = historyList;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history_card, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        History history = historyList.get(position);

        // Đặt lại văn bản dựa trên dữ liệu gốc
        String borrowTimeText = context.getString(R.string.borrow_time) + ": " + history.getBorrowTime();
        if (history.getReturnTime() != null) {
            borrowTimeText += "\n" + context.getString(R.string.return_time) + ": " + history.getReturnTime();
            holder.returnButton.setVisibility(View.GONE);
        } else {
            holder.returnButton.setVisibility(View.VISIBLE);
        }
        holder.borrowTimeText.setText(borrowTimeText);

        if ("room".equals(history.getType())) {
            holder.infoText.setText(context.getString(R.string.room_id) + ": " + history.getRoom().getId());

            // Hiển thị danh sách thiết bị đã mượn
            List<Equipment> devices = history.getRoom().getEquipmentList();
            holder.devicesContainer.removeAllViews();
            for (Equipment device : devices) {
                if (device.getStatus().equals("Đã được mượn")) {
                    TextView deviceInfo = new TextView(context);
                    deviceInfo.setText(device.getEquipmentName() + " (" + context.getString(R.string.quantity) + ": " + device.getQuantity() + ")");

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.gravity = Gravity.CENTER; // Center the TextView within its parent layout
                    deviceInfo.setLayoutParams(params);
                    deviceInfo.setTextColor(ContextCompat.getColor(context, R.color.puple));

                    holder.devicesContainer.addView(deviceInfo);

                }
            }
            holder.returnButton.setText(context.getString(R.string.return_room));

        } else if ("equipment".equals(history.getType())) {
            holder.infoText.setText(context.getString(R.string.device) + ": " + history.getEquipment().getEquipmentName());
            holder.devicesContainer.setVisibility(View.VISIBLE);

            holder.devicesContainer.removeAllViews();
            TextView equipmentQuantity = new TextView(context);
            equipmentQuantity.setText(context.getString(R.string.quantity) + ": " + history.getEquipment().getQuantity());

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            params.gravity = Gravity.CENTER; // Center the TextView within its parent layout
            equipmentQuantity.setLayoutParams(params);
            equipmentQuantity.setTextColor(ContextCompat.getColor(context, R.color.puple));


            holder.devicesContainer.addView(equipmentQuantity);

            holder.returnButton.setText(context.getString(R.string.return_equipment));

        }

        // Xử lý nút "Trả"
        String finalBorrowTimeText = borrowTimeText;
        holder.returnButton.setOnClickListener(v -> {
            String returnTime = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault()).format(new Date());

            // Cập nhật trạng thái trong Firestore
            db.collection("history").document(history.getId())
                    .update("returnTime", returnTime)
                    .addOnSuccessListener(aVoid -> {
                        // Cập nhật dữ liệu trong historyList
                        history.setReturnTime(returnTime);

                        // Cập nhật trạng thái phòng và thiết bị
                        if(("room").equals(history.getType())) {
                            Room room = history.getRoom();
                            db.collection("rooms").document(room.getId())
                                    .update("status", "Sẵn sàng")
                                    .addOnSuccessListener(aVoid1 -> {
                                        for (Equipment device : room.getEquipmentList()) {
                                            device.setStatus("Sẵn sàng");
                                        }
                                        db.collection("rooms").document(room.getId())
                                                .update("equipmentList", room.getEquipmentList())
                                                .addOnSuccessListener(aVoid2 -> {
                                                    // Cập nhật giao diện
                                                    holder.returnButton.setVisibility(View.GONE);
                                                    holder.borrowTimeText.setText(finalBorrowTimeText + "\n" +
                                                            context.getString(R.string.return_time) + ": " + returnTime);
                                                    Toast.makeText(context, context.getString(R.string.check_out_successful), Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(context, context.getString(R.string.error_update_equipment_status), Toast.LENGTH_SHORT).show();
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, context.getString(R.string.error_update_room_status), Toast.LENGTH_SHORT).show();
                                    });
                        }
                        else if(("equipment").equals(history.getType())) {
                            Equipment equipment = history.getEquipment();
                            db.collection("equipments").document(equipment.getEquipmentId()).get().addOnSuccessListener(documentSnapshot -> {
                                int quantity = 0;
                               Equipment thisEquipment = documentSnapshot.toObject(Equipment.class);
                               quantity = thisEquipment.getQuantity();

                                db.collection("equipments").document(equipment.getEquipmentId()).update("status", "Sẵn sàng",
                                        "quantity", equipment.getQuantity()+quantity).addOnSuccessListener(aVoid2 -> {
                                    holder.returnButton.setVisibility(View.GONE);
                                    holder.borrowTimeText.setText(finalBorrowTimeText + "\n" +
                                            context.getString(R.string.return_time) + ": " + returnTime);
                                    Toast.makeText(context, context.getString(R.string.return_equipment_successful), Toast.LENGTH_SHORT).show();

                                }).addOnFailureListener(e -> {
                                   Toast.makeText(context, context.getString(R.string.error_update_equipment_status), Toast.LENGTH_SHORT).show();
                                });
                            }).addOnFailureListener(e -> {
                               Toast.makeText(context, context.getString(R.string.failed_fetch_data_firestore), Toast.LENGTH_SHORT).show();
                            });

                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, context.getString(R.string.check_out_failed), Toast.LENGTH_SHORT).show();
                    });
        });
    }


    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView infoText, borrowTimeText;
        LinearLayout devicesContainer;
        Button returnButton;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            infoText = itemView.findViewById(R.id.info_text);
            borrowTimeText = itemView.findViewById(R.id.borrow_time);
            devicesContainer = itemView.findViewById(R.id.devices_container);
            returnButton = itemView.findViewById(R.id.return_button);
        }
    }

    public void filterList(List<History> filteredList) {
        this.historyList = filteredList;
        notifyDataSetChanged();
    }
}
