import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import static spark.Spark.get;
import static spark.Spark.port;

public class Main {
    private static Stack<String> roles = new Stack<>();
    private static Map<String, String> map = new HashMap<>();
    private static int plaetze = 0;
    private static int PORT;

    private static boolean initRoles(String playersFile) {
        try (BufferedReader br = new BufferedReader(new FileReader(playersFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                roles.push(line);
            }
        } catch (IOException e) {
            System.out.println("Error while reading: " + e.getMessage());
            return false;
        }

        if (roles.size() == 0)
            return false;
        Collections.shuffle(roles);
        plaetze = roles.size();
        return true;
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: WerWolf <port> <playersFile>");
            return;
        }

        parsePort(args[0]);
        if (!initRoles(args[1])) {
            System.out.println("Won't start an empty game!");
            return;
        }

        System.out.println("Starting application on port " + PORT);
        port(PORT);

        handleRequests();
    }

    private static void parsePort(String arg) {
        try {
            PORT = Integer.valueOf(arg);
        } catch (NumberFormatException e) {
            // Ignore parameter
            System.out.println("Could not parse port: " + arg);
        }
    }

    private static void handleRequests() {
        get("/", (request, response) -> {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("frei", roles.size());
            attributes.put("von", plaetze);
            return new ModelAndView(attributes, "form.ftl");
        }, new FreeMarkerEngine());

        get("/reg", (request, response) -> {
            if (request.session().isNew()) {
                request.session(true);
                Map<String, Object> attributes = new HashMap<>();
                String role = handleNewUser(request.queryParams("name"));
                attributes.put("role", role);
                attributes.put("description", getDescription(role));
                return new ModelAndView(attributes, "role.ftl");
            } else {
                return new ModelAndView(null, "already.ftl");
            }
        }, new FreeMarkerEngine());


        get("/master", (request, response) -> {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("spieler", printHTMLUsers());
            return new ModelAndView(attributes, "master.ftl");
        }, new FreeMarkerEngine());
    }

    static private String handleNewUser(String name) {
        if (map.containsKey(name))
            return "Name schon vorhanden!";
        if (roles.size() == 0)
            return "Alles voll.. :/";
        String tmp = roles.pop();
        map.put(name, tmp);
        return tmp;
    }

    static private String printHTMLUsers() {
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (String user : map.keySet()) {
            String role = map.get(user);
            String description = getDescription(role);
            sb.append("<tr><th scope=\"row\">");
            sb.append(i++);
            sb.append("</th><td>");
            sb.append(user);
            sb.append("</td><td>");
            sb.append(role);
            sb.append("</td><td>");
            sb.append(description);
            sb.append("</td></tr>");
        }
        return sb.toString();
    }

    private static String getDescription(String role) {
        switch (role.toLowerCase()) {
            case "werwolf":
                return "Ziel ist es alle B&uuml;rger zu t&ouml;ten.";
            case "buerger":
                return "Ziel ist es alle Werw&ouml;lfe zu t&ouml;ten.";
            case "seherin":
                return "In jeder Nacht darf sie die Identit&auml;t eines Mitspielers erfragen.";
            case "maedchen":
                return "es darf blinzeln, w&auml;hrend die W&ouml;lfe nachts am Werk sind.";
            case "hexe":
                return "Ihr stehen zwei Tr&auml;nke zur Verf&uuml;gung, ein Heil- und ein Gifttrank.<br>\n" +
                        "Deren Bedeutung ist zwar selbsterkl&auml;rend, aber dennoch: Mit dem Gifttrank kann sie einmal\n" +
                        "im Spiel einen Mitspieler vergiften, mit dem Heiltrank jemanden vor den Werw&ouml;lfen erretten\n" +
                        "(auch sich selber).";
            case "jaeger":
                return "Sollte er zu Tode kommen, kann er einen letzten Schuss abgeben\n" +
                        "und einen Mitspieler mit ins Verderben reissen.";
            case "amor":
                return "Zu Beginn des Spieles bestimmt er zwei Spieler, die sofort in inniger Liebe zueinander entflammen\n" +
                        "(das kann auch er selbst sein). Stirbt im Laufe des Spiels einer der beiden Liebenden, so auch der andere aus Gram.\n" +
                        "Achtung: Ist einer der beiden Liebenden ein Werwolf und der andere ein B&uuml;rger, so haben sie ein gemeinsames\n" +
                        "neues Ziel: &Uuml;berleben sie als einzige so gewinnen sie allein.";
            case "hauptmann":
                return "Seine Stimme z&auml;hlt doppelt. Stirbt er, benennt er sofort einen Nachfolger.";
            default:
                return "TBD";
        }
    }


}
