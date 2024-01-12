package annuaire.servers;

import annuaire.servants.AuthentificationClientSimpleImpl;
import annuaire.bd.ListeAuth;
import annuaire.servants.AuthentificationManagerImpl;
import io.grpc.Server;
import io.grpc.ServerInterceptors;

import java.io.IOException;

public class ServerASManager {

    private AuthentificationManagerImpl servant;
    private Server server;

    public ServerASManager() throws IOException {
        this(28415, 3244); //port par défaut
    }

    public ServerASManager(int port, int portLog) throws IOException {
        ListeAuth listeAuth = new ListeAuth();
        servant = new AuthentificationManagerImpl(listeAuth, portLog);
        server = io.grpc.ServerBuilder.forPort(port).addService(ServerInterceptors.intercept(servant, servant)).build().start();
    }

    public void run() {
        try {
            System.err.println("Serveur Authentification RUNNING");
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
