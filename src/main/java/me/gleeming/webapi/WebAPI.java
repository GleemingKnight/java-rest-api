package me.gleeming.webapi;

import com.sun.net.httpserver.HttpServer;
import me.gleeming.webapi.context.Context;

import java.net.InetSocketAddress;

public class WebAPI {
    private HttpServer httpServer;
    public WebAPI(int port) {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(port), 0);

            httpServer.setExecutor(null);
            httpServer.start();
        } catch(Exception ex) { ex.printStackTrace(); }
    }

    public void registerHandler(String route, Object obj) {
        httpServer.createContext(route, new Context(route, obj));
    }
}
