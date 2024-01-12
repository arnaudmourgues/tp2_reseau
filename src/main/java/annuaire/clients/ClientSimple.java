package annuaire.clients;

import com.google.protobuf.Empty;
import com.proto.annuaire.AUDResponse;
import com.proto.annuaire.AuthentificationServiceClientGrpc;
import com.proto.annuaire.GestionRessourcesServiceGrpc;
import com.proto.annuaire.Identifiant;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.io.IOException;
import java.util.Iterator;
import java.util.Scanner;

public class ClientSimple {
    private ManagedChannel channel;

    public ClientSimple() {
        this(28414);
    }

    public ClientSimple(int port) {
        this.channel = ManagedChannelBuilder.forAddress("localhost", port)
                .usePlaintext()
                .build();
    }

    public void run() {
        AuthentificationServiceClientGrpc.AuthentificationServiceClientBlockingStub rechClient = AuthentificationServiceClientGrpc.newBlockingStub(channel);

        while (true) {
            String chaine = getIds();
            if (chaine.equals("FIN")) break;
            if (!chaine.matches("[a-zA-Z0-9]{1,2047} [a-zA-Z0-9]{1,2047}")) {
                System.out.println("Mauvais format de couple login/mot de passe.");
            }else{
                String[] ids = chaine.split(" ");
                String login = ids[0];
                String password = ids[1];

                Identifiant reqOne = Identifiant.newBuilder().setLogin(login).setPasswd(password).build();

                System.out.println("Réponse : ");
                System.out.println(rechClient.check(reqOne));
            }
        }
    }

    private String getIds() {
        String chaine = "";
        // Scanner sur System.in
        Scanner scanner = new Scanner(System.in);
        System.out.println("Tapez votre couple login/mot de passe comme suit : <<login password>>." + "\n");
        chaine = scanner.nextLine();
        return chaine;
    }
}
