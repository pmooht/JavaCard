package database.models;

import java.util.Date;

/**
 * Model cho hội viên gym
 */
public class Member {
    
    private int memberId;
    private String cardId;
    private String fullName;
    private String birthDate;
    private String phone;
    private String address;
    private byte[] photo;
    private String createdDate;
    private String status;
    
    // Constructors
    public Member() {
    }
    
    public Member(String cardId, String fullName, String birthDate, 
                  String phone, String address) {
        this.cardId = cardId;
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.phone = phone;
        this.address = address;
        this.status = "active";
    }
    
    // Getters and Setters
    public int getMemberId() {
        return memberId;
    }
    
    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }
    
    public String getCardId() {
        return cardId;
    }
    
    public void setCardId(String cardId) {
        this.cardId = cardId;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getBirthDate() {
        return birthDate;
    }
    
    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public byte[] getPhoto() {
        return photo;
    }
    
    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }
    
    public String getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return "Member{" +
                "memberId=" + memberId +
                ", cardId='" + cardId + '\'' +
                ", fullName='" + fullName + '\'' +
                ", phone='" + phone + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}