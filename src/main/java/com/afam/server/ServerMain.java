package com.afam.server;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

/**
 * ServerMain – avvio del server REST AFAM su Grizzly + Jersey.
 * Espone le risorse JAX-RS del package com.afam.server su localhost:8080/api.
 */
public class ServerMain {

    // ── Campi ──────────────────
    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://localhost:8080/api/";

    // ── Metodi ──────────────────
    public static HttpServer startServer() {
        // create a resource config that scans for JAX-RS resources and providers
        final ResourceConfig rc = new ResourceConfig().packages("com.afam.server");

        // create and start a new instance of grizzly http server
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    /** Punto di ingresso dell'applicazione. */
    public static void main(String[] args) {
        try {
            final HttpServer server = startServer();
            System.out.println(String.format("Jersey app started with WADL available at "
                    + "%sapplication.wadl\nRunning... Close process to stop.", BASE_URI));
            
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Stopping server...");
                server.shutdownNow();
            }));

            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
