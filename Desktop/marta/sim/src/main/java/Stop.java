import org.json.JSONStringer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;

public class Stop {
    private int id;
    private String name;
    private PriorityQueue<Rider> riders;
    private double latitude;
    private double longitude;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PriorityQueue<Rider> getRiders() {
        return riders;
    }

    public void setRiders(PriorityQueue<Rider> riders) {
        this.riders = riders;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Stop(int id, String name, int riders, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.riders = new PriorityQueue<>();
        for (int i = 0; i < riders; i++) {
            this.riders.add(new Rider());
        }
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String toString(){
        String stopString = "";
        stopString = stopString.concat(Integer.toString(getId()) + ",");
        stopString = stopString.concat(getName() + ",");
        stopString = stopString.concat(Integer.toString(getRiders().size()) + ",");
        stopString = stopString.concat(Double.toString(latitude) + ",");
        stopString = stopString.concat(Double.toString(longitude));
        return stopString;
    }

    String toJSON() {
        return new JSONStringer()
                .object()
                    .key("id").value(id)
                    .key("name").value(name)
                    .key("riders").value(riders.size())
                    .key("latitude").value(latitude)
                    .key("longitude").value(longitude)
                .endObject()
                .toString();
    }

    Rider nextInLine() {

        return riders.poll();
    }
}
