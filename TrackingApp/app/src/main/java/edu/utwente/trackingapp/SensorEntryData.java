package edu.utwente.trackingapp;

public class SensorEntryData {
    private double Ax;
    private double Ay;
    private double Az;
    private double Gx;
    private double Gy;
    private double Gz;

    public SensorEntryData() {

    }

    public SensorEntryData(double ax, double ay, double az, double gx, double gy, double gz) {
        Ax = ax;
        Ay = ay;
        Az = az;
        Gx = gx;
        Gy = gy;
        Gz = gz;
    }

    public double getAx() {
        return Ax;
    }

    public void setAx(double ax) {
        Ax = ax;
    }

    public double getAy() {
        return Ay;
    }

    public void setAy(double ay) {
        Ay = ay;
    }

    public double getAz() {
        return Az;
    }

    public void setAz(double az) {
        Az = az;
    }

    public double getGx() {
        return Gx;
    }

    public void setGx(double gx) {
        Gx = gx;
    }

    public double getGy() {
        return Gy;
    }

    public void setGy(double gy) {
        Gy = gy;
    }

    public double getGz() {
        return Gz;
    }

    public void setGz(double gz) {
        Gz = gz;
    }

    public String toString() {
        return Ax + "," + Ay + "," + Az + ","
                + Gx + "," + Gy + "," + Gz;
    }

}
