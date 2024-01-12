package annuaire.servers;

import annuaire.servants.LoggingService;
import io.grpc.Server;

import java.io.IOException;

public class LoggingServer {
    private LoggingService loggingService;
    private Server server;

    public LoggingServer() throws IOException {
        this(3244); //port par défaut
    }

    public LoggingServer(int port) throws IOException {
        this.loggingService = LoggingService.getInstance();
        this.server = io.grpc.ServerBuilder.forPort(port).addService(loggingService).build().start();
    }

    public void run() {
        try {
            System.err.println("Serveur Logging RUNNING");
            // Interception Ctrl C et arrêt processus
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (server != null) {
                    server.shutdown();
                }
            }));
            // Boucle infinie
            server.awaitTermination();
        } catch (Exception var3) {
            var3.printStackTrace();
        }
    }


}
