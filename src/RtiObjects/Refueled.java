package RtiObjects;

public class Refueled {
    private int vehicleId;
    private int gasPumpId;

    public Refueled(int vehicleId, int gasPumpId) {
        this.vehicleId = vehicleId;
        this.gasPumpId = gasPumpId;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public int getGasPumpId() {
        return gasPumpId;
    }
}
