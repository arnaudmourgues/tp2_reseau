package annuaire;

import annuaire.clients.ClientSimple;
import annuaire.clients.LoggingClient;
import annuaire.clients.ManagerSimple;
import annuaire.servers.LoggingServer;
import annuaire.servers.ServerASClient;
import annuaire.servers.ServerASManager;

import java.io.IOException;

public class MainGrpc {
    public static void main(String[] args) throws IOException {
        ServerASManager sas = new ServerASManager();
        ServerASClient sac = new ServerASClient();
        LoggingServer ls = new LoggingServer();

        //launch servers in background
        new Thread(sas::run).start();
        new Thread(sac::run).start();
        new Thread(ls::run).start();

        ClientSimple cs = new ClientSimple();
        ManagerSimple ms = new ManagerSimple();
        LoggingClient lc = new LoggingClient();

        System.out.println("-------------- ClientSimple running --------------");
        //cs.run();
        System.out.println("-------------- ManagerSimple running --------------");
        ms.run();
        System.out.println("-------------- LoggingClientTest running --------------");
        //lc.run();
    }
}
