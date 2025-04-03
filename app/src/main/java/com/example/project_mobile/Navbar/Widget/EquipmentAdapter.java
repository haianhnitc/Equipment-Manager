package com.example.project_mobile.Navbar.Widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import java.util.List;
import java.util.Locale;

public class EquipmentAdapter extends RecyclerView.Adapter<EquipmentAdapter.EquipmentViewHolder> {
    FirebaseUser currentUser;
    FirebaseFirestore db;
    List<Equipment> equipmentList = new ArrayList<>();
    private Context context;

    public EquipmentAdapter(Context context, List<Equipment> equipmentList) {
        this.context = context;
        this.equipmentList = equipmentList;
    };

    @Override
    public EquipmentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_equipment, parent, false);
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return new EquipmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EquipmentViewHolder holder, int position) {
        Equipment equipment = equipmentList.get(position);
        holder.equipmentName.setText(context.getString(R.string.equipment_name) + ": " + equipment.getEquipmentName());
        holder.equipmentID.setText(context.getString(R.string.equipment_id) + ": " + equipment.getEquipmentId());
        holder.equipmentQuantity.setText(context.getString(R.string.quantity) + ": " + equipment.getQuantity());
        holder.equipmentStatus.setText(context.getString(R.string.status) + ": " + equipment.getStatus());

        holder.borrowButton.setOnClickListener(v -> {
            openDialogBorrowEquipment(equipment);
        });
    }

    private void openDialogBorrowEquipment(Equipment equipment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.borrow) + " " + equipment.getEquipmentName());

        // Tạo layout cho dialog
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 20);

        // Tạo EditText để nhập số lượng
        final EditText quantityInput = new EditText(context);
        quantityInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        quantityInput.setHint(context.getString(R.string.enter_quantity) + " " + equipment.getQuantity() + ")");
        layout.addView(quantityInput);

        builder.setView(layout);

        // Nút xác nhận
        builder.setPositiveButton(context.getString(R.string.borrow), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String borrowTime = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault()).format(new Date());
                String input = quantityInput.getText().toString();
                if (!input.isEmpty()) {
                    int requestedQuantity = Integer.parseInt(input);
                    if (requestedQuantity > 0 && requestedQuantity <= equipment.getQuantity()) {
                        // Cập nhật số lượng trong Firestore
                        int newQuantity = equipment.getQuantity() - requestedQuantity;
                        String newStatus = newQuantity > 0 ? "Sẵn sàng" : "Đã được mượn";

                        db.collection("equipments")
                                .document(equipment.getEquipmentId())
                                .update("quantity", newQuantity, "status", newStatus)
                                .addOnSuccessListener(aVoid -> {
                                    saveToHistory(equipment, requestedQuantity, borrowTime);

                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context,
                                            context.getString(R.string.borrow_equipment_failed),
                                            Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(context,
                                context.getString(R.string.equipment_quantity_required) + equipment.getQuantity() + ")",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // Nút hủy
        builder.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        // Đảm bảo nút Borrow chỉ hoạt động khi input hợp lệ
        final Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setEnabled(false);

        quantityInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                boolean isValid = !input.isEmpty() &&
                        Integer.parseInt(input) > 0 &&
                        Integer.parseInt(input) <= equipment.getQuantity();
                positiveButton.setEnabled(isValid);
            }
        });
    }

    private void saveToHistory(Equipment equipment, int quantity ,String borrowTime) {
        if(currentUser == null) return;

        String userId = currentUser.getUid();
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            User user = documentSnapshot.toObject(User.class);

            Equipment equipmentSave = new Equipment(equipment.getEquipmentId(), equipment.getEquipmentName(),
                    quantity, "Đã được mượn");
            History history = new History(user.getStudentId(), "equipment", null,
                    equipmentSave, borrowTime, null);

            db.collection("history").add(history)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(context,
                                context.getString(R.string.borrow_successfully)+ " " + history.getEquipment().getQuantity() + " "
                                        + equipment.getEquipmentName(),
                                Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, context.getString(R.string.borrow_equipment_failed), Toast.LENGTH_SHORT).show();
                    });
        }).addOnFailureListener(e -> {
           Toast.makeText(context, context.getString(R.string.failed_save_equipment_firestore), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return equipmentList.size();
    }

    public class EquipmentViewHolder extends RecyclerView.ViewHolder {

        TextView equipmentName, equipmentID, equipmentQuantity, equipmentStatus;
        Button borrowButton;
        public EquipmentViewHolder(View itemView) {
            super(itemView);
            equipmentName = itemView.findViewById(R.id.equipment_name);
            equipmentID = itemView.findViewById(R.id.equipment_id);
            equipmentQuantity = itemView.findViewById(R.id.equipment_quantity);
            equipmentStatus = itemView.findViewById(R.id.equipment_status);
            borrowButton = itemView.findViewById(R.id.borrow_equipment_button);

        }

    }

    public void filterList(List<Equipment> filteredList) {
        this.equipmentList = filteredList;
        notifyDataSetChanged();
    }
}
