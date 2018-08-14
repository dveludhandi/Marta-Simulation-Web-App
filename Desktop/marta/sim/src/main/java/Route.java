import org.json.JSONArray;
import org.json.JSONStringer;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Route {
    private int id;
    private int index;
    private String name;
    private ArrayList<Stop> stops;
    private int[] stopIDs;
    private Map<Integer, Stop> stopMap;

    public Map<Integer, Stop> getStopMap() {
        return stopMap;
    }

    public int[] getStopIDs() {
        return stopIDs;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Route(int id, int index, String name, int ... stopIdList) {
        this.id = id;
        this.index = index;
        this.name = name;
        this.stopMap = new HashMap<>();

        int posVals = 0;
        for (int i = 0; i < stopIdList.length; i++) {
            if (stopIdList[i] > -1) {
                this.stopMap.put(stopIdList[i],SimulationServer.sim.getStops().get(stopIdList[i]));
            } else {
                posVals = i;
                break;
            }
        }
        stopIDs = new int[posVals];
        for (int i = 0; i < posVals; i++) {
            stopIDs[i] = stopIdList[i];
        }

    }

    public String toString() {
        String routeString = "";
        routeString = routeString.concat(Integer.toString(getId()) + ",");
        routeString = routeString.concat(Integer.toString(getIndex()) + ",");
        routeString = routeString.concat(getName());
        return routeString;
    }

    public String toJSON() {
        JSONArray stopsJSONArray = new JSONArray();

        for (int i = 0; i <stopIDs.length;i++) {
            stopsJSONArray.put(stopMap.get(stopIDs[i]).getId());
        }
        return new JSONStringer()
                .object()
                    .key("id").value(id)
                    .key("index").value(index)
                    .key("name").value(name)
                    .key("stops").value(stopsJSONArray.toList())
                .endObject()
                .toString();
    }
}
