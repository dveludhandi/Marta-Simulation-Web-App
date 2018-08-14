import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;


import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;



public class SimulationServer {
    static Simulation sim = new Simulation();

    static class WebsocketServer extends WebSocketServer {

        private static int TCP_PORT = 4444;

        private Set<WebSocket> conns;

        public WebsocketServer() {

            super(new InetSocketAddress(TCP_PORT));
            conns = new HashSet<>();
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            conns.add(conn);
            System.out.println("New connection from " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            conns.remove(conn);
            System.out.println("Closed connection to " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
//            System.out.println("Message from client: " + message);
            String response = SimulationServer.sim.processRequest(message);
            conn.send(response);
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            ex.printStackTrace();
            if (conn != null) {
                conns.remove(conn);
                // do some thing if required
            }
//            System.out.println("ERROR: Time between server boot up is too soon.");
            System.exit(0);
        }

        @Override
        public void onStart() {}

    }


    private static void attemptConnection() {
        new WebsocketServer().start();
    }

    public static void main(String[] args) {


        System.out.println("Welcome to the Team 8 MARTA System Simulation.");
        sim.start();
        attemptConnection();
        System.out.println("Listening on Port 4444");
    }
}
