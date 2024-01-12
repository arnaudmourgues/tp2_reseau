package annuaire.clients;

import com.google.protobuf.Empty;
import com.proto.annuaire.AuthentificationServiceManagerGrpc;
import com.proto.annuaire.Identifiant;
import com.proto.annuaire.LogStored;
import com.proto.annuaire.LoggingServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.io.IOException;
import java.util.Iterator;
import java.util.Scanner;

public class ManagerSimple {
    private ManagedChannel channelServer;
    private ManagedChannel channelLog;

    public ManagerSimple() {
        this(28415, 3244);
    }

    public ManagerSimple(int port, int portLog) {
        this.channelServer = ManagedChannelBuilder.forAddress("localhost", port)
                .usePlaintext()
                .build();
        this.channelLog = ManagedChannelBuilder.forAddress("localhost", portLog)
                .usePlaintext()
                .build();
    }

    public void run() throws IOException {
        AuthentificationServiceManagerGrpc.AuthentificationServiceManagerBlockingStub
                RechClient = AuthentificationServiceManagerGrpc.newBlockingStub(channelServer);

        LoggingServiceGrpc.LoggingServiceBlockingStub
                logClient = LoggingServiceGrpc.newBlockingStub(channelLog);

        while (true) {
            int choix = getOperation();
            if (choix == 5) {

                Iterator<LogStored> logs = logClient.list(Empty.newBuilder().build());
                while (logs.hasNext()) {
                    LogStored log = logs.next();
                    System.out.println(log);
                }
            } else {


                String chaine = getIds();
                if (chaine.equals("FIN") || choix == 0) break;
                if (!chaine.matches("[a-zA-Z0-9]{1,2047} [a-zA-Z0-9]{1,2047}")) {
                    System.out.println("Veuillez entrer un couple login/mot de passe de la forme <<login password>>.");
                } else {
                    String[] ids = chaine.split(" ");
                    String login = ids[0];
                    String password = ids[1];

                    Identifiant reqOne = Identifiant.newBuilder().setLogin(login).setPasswd(password).build();

                    System.out.println("Réponse : ");

                    switch (choix) {
                        case 1 -> System.out.println(RechClient.add(reqOne));
                        case 2 -> System.out.println(RechClient.check(reqOne));
                        case 3 -> System.out.println(RechClient.update(reqOne));
                        case 4 -> System.out.println(RechClient.delete(reqOne));
                        default -> System.out.println("Commande inconnue");
                    }
                }
            }
        }
    }

    private String getIds() throws IOException {
        String chaine = "";
        // Scanner sur System.in
        Scanner scanner = new Scanner(System.in);
        System.out.println("Tapez votre couple login/mot de passe comme suit : <<login password>>." + "\n");
        chaine = scanner.nextLine();
        return chaine;
    }

    private int getOperation() throws IOException {
        String chaine = "";
        // Scanner sur System.in
        Scanner scanner = new Scanner(System.in);
        System.out.println("+---------------------------------+");
        System.out.println("| Tapez votre demande :           |");
        System.out.println("| 1 - creer une paire             |");
        System.out.println("| 2 - tester une paire            |");
        System.out.println("| 3 - mettre à jour une paire     |");
        System.out.println("| 4 - supprimer une paire         |");
        System.out.println("| 5 - lister les logs             |");
        System.out.println("| 0 - arreter                     |");
        System.out.println("+---------------------------------+");
        chaine = scanner.nextLine();
        try {
            return Integer.parseInt(chaine);
        } catch (Exception e) {
            System.out.println("ERROR bad_request");
            return getOperation();
        }
    }
}
