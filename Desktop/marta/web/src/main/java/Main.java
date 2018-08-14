import io.javalin.Javalin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

public class Main {

    private static HashMap<String, String> user = new HashMap<String, String>() {{
        put("username", "user");
        put("password", "pass");
    }};

    public static void main(String[] args) {

        Javalin app = Javalin.create()
                .port(7000)
                .enableStaticFiles("public")
                .start();

        app.post("/login", ctx -> {
            //TODO: Implement different redirects given user type
            if (user.get("username").equals(ctx.formParam("username"))
                    && user.get("password").equals(ctx.formParam("password"))) {
                ctx.redirect("/app/rider/mainRider.html");
            } else if("manager".equals(ctx.formParam("username"))
                    && "pass".equals(ctx.formParam("password"))) {
                ctx.redirect("/app/simManager/mainSimManager.html");

            } else {
                ctx.redirect("/login/loginfailed.html");
            }
        });

    }

}