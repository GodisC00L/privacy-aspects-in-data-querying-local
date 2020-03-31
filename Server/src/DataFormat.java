class DataFormat {
    int carID;
    double timestamp, x, y, velocity, sumToIndex;

    DataFormat(double timestamp, int carID, double x, double y, double velocity) {
        this.timestamp = timestamp;
        this.x = x;
        this.y = y;
        this.velocity = velocity;
        this.carID = carID;
        this.sumToIndex = velocity;
    }

    @Override
    public String toString() {
        return "dataFormat{" +
                "timestamp=" + timestamp +
                ", x=" + x +
                ", y=" + y +
                ", velocity=" + velocity +
                ", carID=" + carID +
                '}';
    }
}