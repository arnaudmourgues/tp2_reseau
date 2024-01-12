package annuaire.clients;

import com.proto.annuaire.Log;
import com.proto.annuaire.LoggingServiceGrpc;
import com.proto.annuaire.TypeCR;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.Scanner;

public class LoggingClient {
    private ManagedChannel channel;

    public LoggingClient() {
        this(3244);
    }

    public LoggingClient(int port) {
        this.channel = ManagedChannelBuilder.forAddress("localhost", port)
                .usePlaintext()
                .build();
    }

    public void run() {
        while(true) {
            //ask for the log
            Scanner scanner = new Scanner(System.in);
            System.out.println("Voulez vous tester le log ?" + "\n");
            String chaine = scanner.nextLine();
            if(chaine.equals("non")) break;
            LoggingServiceGrpc.LoggingServiceBlockingStub rechClient = LoggingServiceGrpc.newBlockingStub(channel);
            Log log = Log.newBuilder()
                    .setHorodatage(java.time.LocalDate.now() + " " + java.time.LocalTime.now())
                    .setRequest("check")
                    .setLogin("monstre")
                    .setPasswd("brother")
                    .setIp("localhost")
                    .setResponse(TypeCR.ACCEPTED_GOOD_VALUES)
                    .setPortNumber(3244)
                    .build();
            rechClient.log(log);
        }
    }
}
