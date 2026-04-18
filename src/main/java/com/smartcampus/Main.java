package com.smartcampus;

import com.smartcampus.resource.DiscoveryResource;
import com.smartcampus.resource.RoomResource;
import com.smartcampus.resource.SensorReadingResource;
import com.smartcampus.resource.SensorResource;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import java.net.URI;

public class Main {

    public static final String BASE_URI = "http://0.0.0.0:8080/api/v1/";

    public static void main(String[] args) throws Exception {
        ResourceConfig config = new ResourceConfig();
        config.register(DiscoveryResource.class);
        config.register(RoomResource.class);
        config.register(SensorResource.class);
        config.register(SensorReadingResource.class);

        HttpServer server = GrizzlyHttpServerFactory
                .createHttpServer(URI.create(BASE_URI), config);

        System.out.println("=== Smart Campus API is running ===");
        System.out.println("Try: http://localhost:8080/api/v1/");
        System.out.println("Press ENTER to stop...");
        System.in.read();
        server.stop();
    }
}