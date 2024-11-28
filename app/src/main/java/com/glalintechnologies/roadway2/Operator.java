package com.glalintechnologies.roadway2;

import java.util.List;

public class Operator {
    private String contactNumber;
    private String description;
    private String emergencyContact;
    private boolean isActive;
    private String licensePlate;
    private String operatorName;
    private String serviceType;
    private String vehicleModel;
    private String vehicleType;
    private Location locations;

    // Default constructor required for Firebase
    public Operator() {}

    public Operator(String contactNumber, String description, String emergencyContact, boolean isActive,
                    String licensePlate, String operatorName, String serviceType, String vehicleModel,
                    String vehicleType, Location locations) {
        this.contactNumber = contactNumber;
        this.description = description;
        this.emergencyContact = emergencyContact;
        this.isActive = isActive;
        this.licensePlate = licensePlate;
        this.operatorName = operatorName;
        this.serviceType = serviceType;
        this.vehicleModel = vehicleModel;
        this.vehicleType = vehicleType;
        this.locations = locations;
    }

    // Getters and Setters
    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public Location getLocations() {
        return locations;
    }

    public void setLocations(Location locations) {
        this.locations = locations;
    }

    // Nested Location Class
    public static class Location {
        private List<Double> l; // "l" field containing latitude and longitude

        public Location() {}

        public Location(List<Double> l) {
            this.l = l;
        }

        public List<Double> getL() {
            return l;
        }

        public void setL(List<Double> l) {
            this.l = l;
        }

        public double getLatitude() {
            return l != null && l.size() > 0 ? l.get(0) : 0.0;
        }

        public double getLongitude() {
            return l != null && l.size() > 1 ? l.get(1) : 0.0;
        }
    }
}
