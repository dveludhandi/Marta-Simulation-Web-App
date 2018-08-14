import org.json.*;

import java.io.*;
import java.util.Timer;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class Simulation {
    private ArrayList<Vehicle> vehicles = new ArrayList<>();
    private ArrayList<Route> routes = new ArrayList<>();
    private ArrayList<Stop> stops = new ArrayList<>();
    private double time;
    private VariableSpeedClock clock;
    private double speed;
    private boolean on;
    private boolean initialized;
    private DBConnect db;

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public ArrayList<Vehicle> getVehicles() {
        return vehicles;
    }

    public void setVehicles(ArrayList<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public ArrayList<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(ArrayList<Route> routes) {
        this.routes = routes;
    }

    public ArrayList<Stop> getStops() {
        return stops;
    }

    public void setStops(ArrayList<Stop> stops) {
        this.stops = stops;
    }

    public double getTime() {return this.time;}

    public void setTime(double time) {this.time = time;}

    private class SimulationUpdater extends TimerTask {

        @Override
        public void run() {
            if (on == true) {
                SimulationServer.sim.update();
                SimulationServer.sim.setTime(SimulationServer.sim.clock.getTime());
            }
        }
    }

    private void init() throws FileNotFoundException {
        BufferedReader br = null;
        String line;
        String cvsSplitBy = ",";

        ClassLoader classLoader = SimulationServer.class.getClassLoader();
        String rawPath = classLoader.getResource("data/AllData.csv").getFile();
        String correctedPath = rawPath.replaceAll("%20", " ");
        File file = new File(correctedPath);

        try {
            br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine()) != null) {

                String[] text = line.trim().split(cvsSplitBy);
                switch (text[0]) {
                    case "stop":
                        getStops().add(Integer.parseInt(text[1]),
                                new Stop(Integer.parseInt(text[1]),
                                        text[2],
                                        Integer.parseInt(text[3]),
                                        Double.parseDouble(text[4]),
                                        Double.parseDouble(text[5])));
                        break;
                    case "route":
                        int[] stops = new int[9];
                         for (int i = 0; i < 9; i++) {
                            stops[i] = Integer.parseInt(text[i + 4]);
                        }
                        getRoutes().add(Integer.parseInt(text[1]),
                                new Route(Integer.parseInt(text[1]),
                                        Integer.parseInt(text[2]),
                                        text[3],
                                        stops));
                        break;
                    case "bus":
                        try {
                            getVehicles().add(Integer.parseInt(text[1]),
                                    new Vehicle(Integer.parseInt(text[1]),
                                            Integer.parseInt(text[2]),
                                            Integer.parseInt(text[3]),
                                            Integer.parseInt(text[4]),
                                            Integer.parseInt(text[5]),
                                            Integer.parseInt(text[6])));
                        } catch (IndexOutOfBoundsException e) {
                            for (int i = getVehicles().size() - 1; i < Integer.parseInt(text[1]) - 1; i++) {
                                getVehicles().add(null);
                            }
                            getVehicles().add(Integer.parseInt(text[1]),
                                    new Vehicle(Integer.parseInt(text[1]),
                                            Integer.parseInt(text[2]),
                                            Integer.parseInt(text[3]),
                                            Integer.parseInt(text[4]),
                                            Integer.parseInt(text[5]),
                                            Integer.parseInt(text[6])));
                        }
                }
            }

        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    String processDataRequest(String msg) {
        JSONArray outputArray = new JSONArray();
        JSONArray json = new JSONArray();
        switch (msg) {
            case "stops":
                for (Stop stop : stops) {
                    outputArray.put(stop.toJSON());
                }
                break;

            case "routes":
                for (Route route : routes) {
                    outputArray.put(route.toJSON());
                }
                break;

            case "vehicles":
                for (Vehicle vehicle : vehicles) {
                    if (vehicle != null) {
                        outputArray.put(vehicle.toJSON());
                    }
                }
                break;

            default:
                break;
        }
        JSONObject type_ = new JSONObject();
        type_.put("type", msg);
        json.put(type_);
        JSONObject data = new JSONObject();
        data.put("data", outputArray);
        json.put(data);
        return json.toString();
    }

    String processBusInfoRequest(String label,String msg) {
        JSONArray json = new JSONArray();

        int vehicleID = Integer.parseInt(msg);

        Vehicle vehicle = this.getVehicles().get(vehicleID);
        JSONObject obj = new JSONObject();
        obj.put("lastStop", vehicle.getCurrentStop().getId());
        obj.put("nextStop", vehicle.getNextStop().getId());
        obj.put("timeToNext", TimeUnit.MILLISECONDS.toMinutes((long)vehicle.getTimeToNextStop()));
        obj.put("distToNext", Math.round(vehicle.getDistanceToNextStop()*100)/100.0d);
        obj.put("numLeaving", vehicle.getNumLeaving());
        obj.put("numBoarding", vehicle.getNumBoarding());
        obj.put("curPassengers", vehicle.getNumberOfRiders());
        obj.put("busID", vehicle.getId());

        JSONObject type_ = new JSONObject();
        type_.put("type", label);
        json.put(type_);
        JSONObject data = new JSONObject();
        data.put("data", obj);
        json.put(data);
        return json.toString();
    }

    String processStatusRequest(String label, String msg) {
        JSONArray json = new JSONArray();
        JSONObject obj = new JSONObject();

        obj.put("on", on);
        obj.put("speed", speed);


        JSONObject type_ = new JSONObject();
        type_.put("type", label);
        json.put(type_);
        JSONObject data = new JSONObject();
        data.put("data", obj);
        json.put(data);
        return json.toString();
    }

    public boolean isNumeric(String s) {
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");
    }

    String processAlteringState(String label, String msg) {
        JSONArray json = new JSONArray();
        JSONObject obj = new JSONObject();
        String response;

        if (isNumeric(msg)) {
            // set simulation speed
            response = "speedChange";
        } else {
            if (on == false) {
                this.clock = new VariableSpeedClock(speed);
                SimulationServer.sim.setTime(this.clock.getTime());
                Timer timer = new Timer();
                timer.schedule(new SimulationUpdater(),0 ,TimeUnit.SECONDS.toMillis(10));
                on = true;
                response = "simPlay";
            } else {
                System.out.println("Pausing");
                on = false;
                response = "simPaused";
            }

        }
        obj.put("response", response);

        JSONObject type_ = new JSONObject();
        type_.put("type", "alterState");
        json.put(type_);
        JSONObject data = new JSONObject();
        data.put("data", obj);
        json.put(data);
        return json.toString();
    }

    String processInitRequest() {
        JSONArray json = new JSONArray();
        JSONObject obj = new JSONObject();
        obj.put("isInit", initialized);

        JSONObject type_ = new JSONObject();
        type_.put("type", "isInit");
        json.put(type_);
        JSONObject data = new JSONObject();
        data.put("data", obj);
        json.put(data);
        return json.toString();
    }

    String processInitialization(String msg) {
        JSONArray json = new JSONArray();
        JSONObject obj = new JSONObject();
        if (msg.equals("startNew")) {
            try {
                init();
                db.setupDB(this);
                initialized = true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            try {
                init();
                db.initialize(this);
                initialized = true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        obj.put("none", "");
        JSONObject type_ = new JSONObject();
        type_.put("type", "initialize");
        json.put(type_);
        JSONObject data = new JSONObject();
        data.put("data", obj);
        json.put(data);
        return json.toString();
    }

    Vehicle getNearestVehicle() {
        double shortestTime = Double.MAX_VALUE;
        Vehicle nearest = null;
        for (Vehicle vehicle : vehicles) {
            if (vehicle != null) {
                if (vehicle.getTimeToNextStop() < shortestTime) {
                    shortestTime = vehicle.getTimeToNextStop();
                    nearest = vehicle;
                }
            }
        }
        return nearest;
    }

    public String processNearestBus() {
        JSONObject obj = new JSONObject();
        JSONArray json = new JSONArray();

        Vehicle nearest = getNearestVehicle();
        if (nearest != null) {
            obj.put("id", nearest.getId());
            obj.put("timeToNext", TimeUnit.MILLISECONDS.toMinutes((long) nearest.getTimeToNextStop()));
        }


        JSONObject type_ = new JSONObject();
        type_.put("type", "nearestBusRequest");
        json.put(type_);
        JSONObject data = new JSONObject();
        data.put("data", obj);
        json.put(data);
        return json.toString();
    }

    String processPushNext() {
        Vehicle nearest = getNearestVehicle();
        for (Vehicle vehicle : vehicles) {
            if (vehicle != null) {
                if (!vehicle.equals(nearest)) {
                    vehicle.setTimeToNextStop(vehicle.getTimeToNextStop() - nearest.getTimeToNextStop());
                    updateSingleVehicle(vehicle.getId());
                }
            }
        }
        nearest.setTimeToNextStop(0);
        nearest.setDistanceToNextStop(0);
        updateSingleVehicle(nearest.getId());

        JSONObject obj = new JSONObject();
        JSONArray json = new JSONArray();

        obj.put("none", "none");

        JSONObject type_ = new JSONObject();
        type_.put("type", "pushNext");
        json.put(type_);
        JSONObject data = new JSONObject();
        data.put("data", obj);
        json.put(data);
        return json.toString();
    }

    String processRequest(String request) {


        JSONObject req = new JSONObject(request);
        String label = req.getString("label");
        String msg = req.getString("msg");
        if (label.equals("data")) {
            return(processDataRequest(msg));

        } else if (label.equals("busInfo")) {
            return(processBusInfoRequest(label,msg));

        } else if (label.equals("statusRequest")) {
            return(processStatusRequest(label,msg));

        } else if (label.equals("alterState")) {
            return processAlteringState(label, msg);
        } else if (label.equals("isInit")) {
            return processInitRequest();
        } else if (label.equals("initialize")) {
            return processInitialization(msg);
        } else if (label.equals("nearestBusRequest")){
            return processNearestBus();
        } else if (label.equals("pushNext")) {
            return(processPushNext());
        }
        return null;
    }


    private void updateSingleVehicle(int id) {
        Vehicle curVehicle = this.getVehicles().get(id);


        if (curVehicle.getTimeToNextStop() == -1) {
            db.updateVehicleInDB(curVehicle);
            curVehicle.setDistanceToNextStop(curVehicle.distanceToNextStop());
            curVehicle.setTimeToNextStop(curVehicle.timeToNextStop() / this.clock.getSpeed());

        } else if (curVehicle.getTimeToNextStop() <= 0) {
            curVehicle.updateAtStop();
            db.updateVehicleInDB(curVehicle);

            //updating current stop
            curVehicle.setDistanceToNextStop(curVehicle.distanceToNextStop());
            curVehicle.setTimeToNextStop(curVehicle.timeToNextStop() / this.clock.getSpeed());
        } else {
            // updating time to next stop
            double elapsedTime = (clock.getTime() - this.getTime());
            curVehicle.setTimeToNextStop((curVehicle.getTimeToNextStop() - elapsedTime) / this.clock.getSpeed());

            // Updating distance to next stop
            double distance = ((double) curVehicle.getSpeed()) / (60 * 60)
                    * (double)TimeUnit.MILLISECONDS.toSeconds((long)(curVehicle.getTimeToNextStop() * this.clock.getSpeed()));
            curVehicle.setDistanceToNextStop(distance);

            if (curVehicle.getTimeToNextStop() <= 0.03) {
                curVehicle.setTimeToNextStop(0);
            }
            if (curVehicle.getDistanceToNextStop() <= 0.03) {
                curVehicle.setDistanceToNextStop(0);
            }

        }
    }

    private void updateAllVehicles() {
        ArrayList<Vehicle> vehicles = SimulationServer.sim.getVehicles();
        for (int i = 0; i < vehicles.size(); i++) {
            if (vehicles.get(i) != null) {
                updateSingleVehicle(i);
            }
        }
    }

    void update() {
        updateAllVehicles();
    }

    void start() {
        db = new DBConnect();
        on = true;
        speed = 1.0;
        this.clock = new VariableSpeedClock(speed);

        SimulationServer.sim.setTime(this.clock.getTime());

        Timer timer = new Timer();
        timer.schedule(new SimulationUpdater(),0 ,TimeUnit.SECONDS.toMillis(10));
    }
}
class VariableSpeedClock {

    public double getSpeed() {
        return speed;
    }

    private double speed;
    private long startTime;

    public VariableSpeedClock(double speed) {
        this(speed, System.currentTimeMillis());
    }

    public VariableSpeedClock(double speed, long startTime) {
        this.speed = speed;
        this.startTime = startTime;
    }

    public long getTime () {
        return (long) ((System.currentTimeMillis() - this.startTime) * this.speed + this.startTime);
    }
}