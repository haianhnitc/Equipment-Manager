package com.example.project_mobile.models;

public class History {
    String id;
    String studentId;
    String type;
    Room room;
    Equipment equipment;
    String borrowTime;
    String returnTime;  // Thêm thời gian trả

    public History() {}

    public History(String studentId,String type, Room room, Equipment equipment, String borrowTime, String returnTime) {
        this.studentId = studentId;
        this.type = type;
        this.room = room;
        this.equipment = equipment;
        this.borrowTime = borrowTime;
        this.returnTime = returnTime;
    }

    public String getId() {
        return id;
    }

    public String getStudentId() {return studentId;}
    public String getType() {
        return type;
    }
    public Room getRoom() {
        return room;
    }
    public Equipment getEquipment() {
        return equipment;
    }
    public String getBorrowTime() {
        return borrowTime;
    }
    public String getReturnTime() {
        return returnTime;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setStudentId(String studentId) {this.studentId = studentId;}
    public void setRoom(Room room) {
        this.room = room;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

    public void setBorrowTime(String borrowTime) {
        this.borrowTime = borrowTime;
    }

    public void setReturnTime(String returnTime) {
        this.returnTime = returnTime;
    }
}
