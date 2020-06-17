package RtiObjects;

public class FuelPaid {
    private int vehicleId;
    private int gasPumpId;

    public FuelPaid(int vehicleId, int gasPumpId) {
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
