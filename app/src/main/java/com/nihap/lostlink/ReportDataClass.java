package com.nihap.lostlink;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

public class ReportDataClass {
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

    public String getReportType() { return reportType; }
    public String getItemName() { return itemName; }
    public String getLocation() { return location; }
    public Integer getRadius() { return radius; }

    public GeoPoint getGeoPoint(){return geoPoint;}
    public String getDescription() { return description; }

    public Timestamp getTimeStamp() { return timestamp; }
    public String getImageUrl() { return imageUrl; }
    public String getUserId() { return userId; }
    public String getUserName() {return userName;}
}
