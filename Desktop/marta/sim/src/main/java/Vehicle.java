import org.json.JSONStringer;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Vehicle {
    private int id;
    private int routeId;
    private Route route;
    private int location; // current or most recent stop's id
    private double timeToNextStop = 0;
    private double distanceToNextStop = 0;
    private VehicleType vehicleType;
    private int numberOfRiders;
    private Rider[] riders;
    private OperationStatus operationStatus;
    private int speed;
    private int numLeaving = 0;
    private int numBoarding = 0;

    public double getTimeToNextStop() {
        return timeToNextStop;
    }

    public void setTimeToNextStop(double timeToNextStop) {
        this.timeToNextStop = timeToNextStop;
    }

    public double getDistanceToNextStop() {
        return distanceToNextStop;
    }

    public void setDistanceToNextStop(double distanceToNextStop) {
        this.distanceToNextStop = distanceToNextStop;
    }

    public int getNumLeaving() {
        return numLeaving;
    }

    public void setNumLeaving(int numLeaving) {
        this.numLeaving = numLeaving;
    }

    public int getNumBoarding() {
        return numBoarding;
    }

    public void setNumBoarding(int numBoarding) {
        this.numBoarding = numBoarding;
    }

    private enum VehicleType {
        BUS, TRAIN
    }

    private enum OperationStatus {
        OPERATIONAL, DOWN
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRouteId() {
        return routeId;
    }

    public Route getRoute() { return this.route; }

    public void setRoute(int route) {
        this.routeId = route;
    }

    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public Rider[] getRiders() {
        return riders;
    }

    public void setRiders(Rider[] riders) {
        this.riders = riders;
    }

    OperationStatus getOperationStatus() {
        return operationStatus;
    }

    public void setOperationStatus(OperationStatus operationStatus) {
        this.operationStatus = operationStatus;
    }

    public int getSpeed() {
        return speed;
    }

    public int getNumberOfRiders() {
        return numberOfRiders;
    }

    public void setNumberOfRiders(int numberOfRiders) {
        this.numberOfRiders = numberOfRiders;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public boolean equals(Object object) {
        if (id == ((Vehicle)object).getId()) {
            return true;
        } else {
            return false;
        }
    }

    Vehicle(int id, int route, int location, int numberOfRiders, int capacity, int speed) {
        this.id = id;
        this.routeId = route; // assigned route
        this.route = SimulationServer.sim.getRoutes().get(routeId);
        this.location = location; // current position in route
        this.numberOfRiders = numberOfRiders;
        this.riders = new Rider[capacity]; // a list of size = capacity
        for (int i = 0; i < numberOfRiders; i++) {
            riders[i] = new Rider(); // for now, the rider's don't have identities
        }
        this.operationStatus = OperationStatus.OPERATIONAL; // operational by default
        this.speed = speed; // current speed, units unknown
        this.distanceToNextStop = -1;
        this.timeToNextStop = -1;
    }

    public String toString() {
        String vehicleString = "";
        vehicleString = vehicleString.concat(Integer.toString(getId()) + ",");
        vehicleString = vehicleString.concat(Integer.toString(getRouteId()) + ",");
        vehicleString = vehicleString.concat(Integer.toString(getLocation()) + ",");
        vehicleString = vehicleString.concat(Integer.toString(getNumberOfRiders()) + ",");
        vehicleString = vehicleString.concat(Integer.toString(getRiders().length) + ",");
        vehicleString = vehicleString.concat(Integer.toString(getSpeed()));
        return vehicleString;
    }

    String toJSON() {
            return new JSONStringer()
                    .object()
                    .key("id").value(id)
                    .key("routeId").value(routeId)
                    .key("location").value(location)
                    .key("numberOfRiders").value(numberOfRiders)
                    .key("capacity").value(riders.length)
                    .key("speed").value(speed)
                    .endObject()
                    .toString();
    }

    private void breakDown() {
        this.setOperationStatus(OperationStatus.DOWN);
    }

    public void repair() {
        this.setOperationStatus(OperationStatus.OPERATIONAL);
    }

    Stop getCurrentStop() {
        return route.getStopMap().get(route.getStopIDs()[location]);
    }

    Stop getNextStop() {
        int[] stopIDs = route.getStopIDs();
        Map<Integer, Stop> stopMap = route.getStopMap();
        return stopMap.get(stopIDs[(location + 1) % stopIDs.length]);
    }

    double distanceToNextStop() {
        double dist = Math.sqrt(
                Math.pow((getNextStop().getLatitude() - getCurrentStop().getLatitude()), 2)
                        + Math.pow(getNextStop().getLongitude() - getCurrentStop().getLongitude(), 2));
        return (dist * 70.0d);
    }

    double timeToNextStop() {
        double time = TimeUnit.MINUTES.toMillis((long) (60.0 * distanceToNextStop / (double)speed));
        return time;
    }

    public void updateAtStop() {
        Random rand = new Random();
        int removeRiders = rand.nextInt((3) +1) + 2;
        if (numberOfRiders - removeRiders <= 0){
            this.numLeaving = numberOfRiders;
            numberOfRiders = 0;
        } else {
            numberOfRiders -= removeRiders;
            this.numLeaving = removeRiders;
        }
        int newRiders = rand.nextInt((11));
        if (numberOfRiders + newRiders > riders.length) {
            this.numBoarding = riders.length - numberOfRiders;
            numberOfRiders = riders.length;
        } else {
            numberOfRiders += newRiders;
            this.numBoarding = newRiders;
        };
        if (route.getStopIDs().length != 0) {
            setLocation((location + 1) % route.getStopIDs().length);
        }

    }

}
