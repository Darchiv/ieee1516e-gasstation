package RtiObjects;

import util.FuelEnum;

public class GasPumpOpen {
    private int gasPumpId;
    private FuelEnum fuelType;

    public GasPumpOpen(int gasPumpId, FuelEnum fuelType) {
        this.gasPumpId = gasPumpId;
        this.fuelType = fuelType;
    }

    public int getGasPumpId() {
        return gasPumpId;
    }

    public FuelEnum getFuelType() {
        return fuelType;
    }
}
