import java.sql.*;

public class DBConnect {

    private static Connection conn;
    private static String url = "jdbc:sqlite:martadata.sqlite";

    public static void createVehicleTable() {
        try {
            conn = DriverManager.getConnection(url, "root", "");

            String query = "create table Vehicle (ID int primary key, Route int, " +
            "Location int, Riders   int, Capacity int, Speed int);";
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.execute();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }

    }

    public static void addVehicle(Vehicle vehicle) {
        try {
            conn = DriverManager.getConnection(url, "root", "");

            String query = " insert into Vehicle (ID, Route, Location, Riders, Capacity, Speed)"
                    + " values (?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setInt(1, vehicle.getId());
            preparedStmt.setInt(2, vehicle.getRouteId());
            preparedStmt.setInt(3, vehicle.getLocation());
            preparedStmt.setInt(4, vehicle.getNumberOfRiders());
            preparedStmt.setInt(5, vehicle.getRiders().length);
            preparedStmt.setInt(6, vehicle.getSpeed());
            preparedStmt.execute();


        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }

    }

    public static void updateVehiclesFromDB(Simulation sim) {
        try {
            conn = DriverManager.getConnection(url, "root", "");

            String query = "SELECT * FROM Vehicle";
            PreparedStatement ps = conn.prepareStatement(query);

            ResultSet result = ps.executeQuery();
            while(result.next()) {
                int id = result.getInt("ID");
                int route = result.getInt("Route");
                int loc = result.getInt("Location");
                int riders = result.getInt("Riders");
                int capacity = result.getInt("Capacity");
                int speed = result.getInt("Speed");

                Vehicle curVehicle = new Vehicle(id, route, loc, riders, capacity, speed);
                sim.getVehicles().set(id,curVehicle);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public static void updateVehicleInDB(Vehicle vehicle) {
        try {
            conn = DriverManager.getConnection(url, "root", "");

            String query = "UPDATE Vehicle SET Route = ?, Location = ?, Riders = ?, Capacity = ?, Speed = ?"
                    + "WHERE ID = ?";

            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, vehicle.getRouteId());
            ps.setInt(2, vehicle.getLocation());
            ps.setInt(3, vehicle.getNumberOfRiders());
            ps.setInt(4, vehicle.getRiders().length);
            ps.setInt(5, vehicle.getSpeed());
            ps.setInt(6, vehicle.getId());
            ps.execute();
            ps.close();


        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public static void initialize(Simulation simulation) {
        try {
            conn = DriverManager.getConnection(url, "root", "");
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet res = meta.getTables(null, null, "Vehicle",
                    new String[] {"TABLE"});

            boolean vehicleExists = false;
            while (res.next()) {
                if (res.getString("TABLE_NAME").toLowerCase().equals("vehicle")) {
                    vehicleExists = true;
                }
            }

            if (!vehicleExists) {
                createVehicleTable();
                for (Vehicle vehicle : simulation.getVehicles()) {
                    if(vehicle != null) {
                        addVehicle(vehicle);
                    }
                }
            } else {
                updateVehiclesFromDB(simulation);

            }


        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }

    }

    public static void setupDB(Simulation simulation) {
        try {
            conn = DriverManager.getConnection(url, "root", "");

            DatabaseMetaData meta = conn.getMetaData();
            ResultSet res = meta.getTables(null, null, "Vehicle",
                    new String[] {"TABLE"});

            boolean vehicleExists = false;
            while (res.next()) {
                if (res.getString("TABLE_NAME").toLowerCase().equals("vehicle")) {
                    vehicleExists = true;
                }
            }

            if (vehicleExists) {
                String query = "drop table Vehicle";
                PreparedStatement ps = conn.prepareStatement(query);
                ps.execute();
            }

            createVehicleTable();
            for (Vehicle vehicle : simulation.getVehicles()) {
                if(vehicle != null) {
                    addVehicle(vehicle);
                }
            }


        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }

    }

}
