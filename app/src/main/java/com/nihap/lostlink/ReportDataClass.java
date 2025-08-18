package com.nihap.lostlink;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;
import java.io.Serializable;

public class ReportDataClass implements Serializable {
    private String reportType;
    private String itemName;
    private String location;
    private Integer radius;

    private GeoPoint geoPoint;
    private String description;
    private String imageUrl;
    private String userId;
    private String userName;
    private Timestamp timestamp;

    public ReportDataClass() {}

    // Getters
    public String getReportType() { return reportType; }
    public String getItemName() { return itemName; }
    public String getLocation() { return location; }
    public Integer getRadius() { return radius; }
    public GeoPoint getGeoPoint(){return geoPoint;}
    public String getDescription() { return description; }
    public Timestamp getTimestamp() { return timestamp; }
    public String getImageUrl() { return imageUrl; }
    public String getUserId() { return userId; }
    public String getUserName() {return userName;}

    // Setters (required for Firebase)
    public void setReportType(String reportType) { this.reportType = reportType; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setLocation(String location) { this.location = location; }
    public void setRadius(Integer radius) { this.radius = radius; }
    public void setGeoPoint(GeoPoint geoPoint) { this.geoPoint = geoPoint; }
    public void setDescription(String description) { this.description = description; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
}
