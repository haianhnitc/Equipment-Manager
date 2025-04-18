package com.example.project_mobile.models;

import java.util.List;

public class Room {
    private String id;
    private String capacity;
    private List<Equipment> equipmentList;
    private String status;


    public Room() {};
    public Room(String id, List<Equipment> equipmentList, String status, String capacity) {
        this.id = id;
        this.equipmentList = equipmentList;
        this.status = status;
        this.capacity = capacity;
    }

    public String getId() { return id; }
    public String getCapacity() {return capacity;}
    public List<Equipment> getEquipmentList() { return equipmentList; }
    public String getStatus() { return status; }

    public void setEquipmentList(List<Equipment> equipmentList) {this.equipmentList = equipmentList; }
    public void setStatus(String status) { this.status = status; }
}
