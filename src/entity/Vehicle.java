package entity;

public class Vehicle {
	private String vehicleNumber;
    private String vehicleType;
    private String color;
    private double weightKg;

    public Vehicle(String vehicleNumber, String vehicleType, String color, double weightKg) {
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.color = color;
        this.weightKg = weightKg;
    }

    public String getVehicleNumber() { return vehicleNumber; }
}

