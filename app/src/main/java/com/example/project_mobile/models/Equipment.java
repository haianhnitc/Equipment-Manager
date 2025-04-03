package com.example.project_mobile.models;

import java.util.List;

public class Equipment {
    private String equipmentId;
    private String equipmentName;
    private int quantity;
    private String status; // e.g., "Available" or "Borrowed"

    // Constructor, getter, và setter

    public Equipment() {};
    public Equipment(String equipmentId, String equipmentName, int quantity, String status) {
        this.equipmentId = equipmentId;
        this.equipmentName = equipmentName;
        this.quantity = quantity;
        this.status = status;
    }

    public String getEquipmentId() { return equipmentId; }
    public String getEquipmentName() { return equipmentName; }
    public int getQuantity() { return quantity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
