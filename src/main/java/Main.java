import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;

import static spark.Spark.*;

/**
 * Created by bene on 11/18/16.
 */
public class Main {
    static private Stack<String> roles = new Stack<String>();
    static private Map<String, String> map = new HashMap<>();
    static private int plaetze = 0;

    public static void main(String[] args) {
        roles.push("Seherin");
        roles.push("Amor");
        roles.push("Jaeger");
        roles.push("Buerger");
        roles.push("Buerger");
        roles.push("Buerger");
        roles.push("Buerger");
        roles.push("Buerger");
        roles.push("Werwolf");
        roles.push("Werwolf");
        roles.push("Werwolf");
        Collections.shuffle(roles);
        plaetze = roles.size();

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
                attributes.put("role", handleNewUser(request.queryParams("name")));
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
        get("/master", (request, response) -> {
            String res = "";
            for (String s : map.keySet()) {
                res += s + " <==> " + map.get(s) + "<br>";
            }
            return res;
        });

        get("/lol", (request, response) -> {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("asdf", request.queryParams("name"));
            return new ModelAndView(attributes, "index.ftl");
        }, new FreeMarkerEngine());

    }

    static private String handleNewUser(String name) {
        if (map.containsKey(name))
            return "Name schon vorhanden!";
        if(roles.size() == 0)
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
        return "TODO";
    }


}
