package me.gleeming.webapi.context;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.Getter;
import lombok.SneakyThrows;
import me.gleeming.webapi.route.Route;
import org.bson.Document;

import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Context implements HttpHandler {
    private final String route;
    private final Object obj;

    private final List<RouteHandler> routes = new ArrayList<>();
    public Context(String route, Object obj) {
        this.route = route;
        this.obj = obj;

        for(Method method : obj.getClass().getMethods()) {
            Route routeAnnotation = method.getAnnotation(Route.class);

            if(routeAnnotation != null) {
                String name = (routeAnnotation.name().startsWith("/") ? routeAnnotation.name().substring(1) : routeAnnotation.name());
                String authentication = routeAnnotation.authentication();
                Route.Type type = routeAnnotation.type();

                routes.add(new RouteHandler(name, authentication, type, method));
            }
        }
    }

    @SneakyThrows
    public void handle(HttpExchange he) {
        String route = he.getRequestURI().toString().substring(this.route.length() + 1);
        List<String> arguments = new ArrayList<>(Arrays.asList(route.split("/")));

        for(RouteHandler handler : routes) {
            if(handler.getArguments().size() == arguments.size()) {
                int currentArgument = 0;
                boolean matches = true;
                for(RouteHandler.Argument argument : handler.getArguments()) {
                    if(argument instanceof RouteHandler.Argument.KnownArgument && !arguments.get(currentArgument).equals(argument.getArgument())) matches = false;

                    currentArgument++;
                }

                if(matches) {
                    if(!handler.getAuthentication().equals("")) if(he.getRequestHeaders().getFirst("authentication") == null || !he.getRequestHeaders().getFirst("authentication").equals(handler.getAuthentication())) return;

                    List<String> unknownArguments = new ArrayList<>();
                    currentArgument = 0;
                    for(RouteHandler.Argument argument : handler.getArguments()) {
                        if(argument instanceof RouteHandler.Argument.UnknownArgument) unknownArguments.add(arguments.get(currentArgument));

                        currentArgument++;
                    }

                    if(handler.getType() == Route.Type.GET) {
                        Headers headers = he.getResponseHeaders();
                        headers.add("Access-Control-Allow-Origin", "*");
                        headers.add("Content-type", "application/json");

                        String response = ((Document) handler.getMethod().invoke(obj, unknownArguments)).toJson();
                        he.sendResponseHeaders(200, response.length());

                        OutputStream os = he.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                    } else if(handler.getType() == Route.Type.POST) handler.getMethod().invoke(obj, unknownArguments);

                    return;
                }
            }
        }

        he.close();
    }

    public static class RouteHandler {
        @Getter private final String route;
        @Getter private final String authentication;

        @Getter private final Route.Type type;
        @Getter private final Method method;

        @Getter private final List<Argument> arguments = new ArrayList<>();
        public RouteHandler(String route, String authentication, Route.Type type, Method method) {
            this.route = route;
            this.authentication = authentication;
            this.type = type;
            this.method = method;

            for(String s : route.split("/")) {
                if(s.startsWith("<") && s.endsWith(">")) arguments.add(new Argument.UnknownArgument(s.substring(1, s.length() - 2)));
                else arguments.add(new Argument.KnownArgument(s));
            }
        }

        public static abstract class Argument {
            @Getter private final String argument;
            public Argument(String argument) { this.argument = argument; }

            public static class KnownArgument extends Argument { public KnownArgument(String argument) { super(argument); }}
            public static class UnknownArgument extends Argument { public UnknownArgument(String lookingFor) { super(lookingFor); }}
        }
    }
}
