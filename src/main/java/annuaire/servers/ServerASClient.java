package annuaire.servers;

import annuaire.bd.ListeAuth;
import annuaire.servants.AuthentificationClientSimpleImpl;
import io.grpc.Server;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;

import java.io.IOException;

public class ServerASClient {

    private AuthentificationClientSimpleImpl servant;
    private Server server;

    public ServerASClient() throws IOException {
        this(28414); //port par défaut
    }

    public ServerASClient(int port) throws IOException {
        ListeAuth listeAuth = new ListeAuth();
        servant = new AuthentificationClientSimpleImpl(listeAuth);
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
