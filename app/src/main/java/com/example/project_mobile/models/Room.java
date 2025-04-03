package com.example.project_mobile.models;

import java.util.List;

public class Room {
    private String id;
    private List<Equipment> equipmentList;
    private String status; // e.g., "Available" or "Borrowed"

    // Constructor, getter, và setter

    public Room() {};
    public Room(String id, List<Equipment> equipmentList, String status) {
        this.id = id;
        this.equipmentList = equipmentList;
        this.status = status;
    }

    public String getId() { return id; }
    public List<Equipment> getEquipmentList() { return equipmentList; }
    public String getStatus() { return status; }

    public void setEquipmentList(List<Equipment> equipmentList) {this.equipmentList = equipmentList; }
    public void setStatus(String status) { this.status = status; }
}
