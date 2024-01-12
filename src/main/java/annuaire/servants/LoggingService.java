package annuaire.servants;

import com.google.protobuf.Empty;
import com.proto.annuaire.Log;
import com.proto.annuaire.LogStored;
import com.proto.annuaire.LoggingServiceGrpc;
import com.proto.annuaire.TypeCR;
import io.grpc.stub.StreamObserver;

import java.io.File;
import java.util.Iterator;
import java.util.UUID;

public class LoggingService extends LoggingServiceGrpc.LoggingServiceImplBase {

    private static LoggingService instance;
    File file;

    public static LoggingService getInstance() {
        if (instance == null) {
            instance = new LoggingService();
        }
        return instance;
    }

    private LoggingService() {
        //if "src/main/java/annuaire/logs/logs.txt" doesn't exist, create it, else open it
        file = new File("src/main/java/annuaire/logs/logs.txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                System.out.println("Error while creating log file");
            }
        }
    }

    @Override
    public void log(Log request, StreamObserver<Empty> responseObserver) {
        //write the log in the file after the last line
        try {
            java.io.FileWriter fw = new java.io.FileWriter(file, true);
            String log = UUID.randomUUID() + ";" +
                    request.getHorodatage() + ";" +
                    request.getRequest() + ";" +
                    request.getLogin() + ";" +
                    request.getPasswd() + ";" +
                    request.getResponse() + ";" +
                    request.getIp() + ";" +
                    request.getPortNumber() + "\n";
            fw.write(log);
            fw.close();
        } catch (Exception e) {
            System.out.println("Error while writing in log file");
        }
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void list(Empty request, StreamObserver<LogStored> responseObserver) {
        //read the file and send each line to the client
        try {
            java.io.FileReader fr = new java.io.FileReader(file);
            java.io.BufferedReader br = new java.io.BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                String[] log = line.split(";");
                Log logToSend = Log.newBuilder()
                        .setHorodatage(log[1])
                        .setRequest(log[2])
                        .setLogin(log[3])
                        .setPasswd(log[4])
                        .setResponse(TypeCR.valueOf(log[5]))
                        .setIp(log[6])
                        .setPortNumber(Integer.parseInt(log[7]))
                        .build();
                LogStored logStored = LogStored.newBuilder()
                        .setLog(logToSend)
                        .setUuid(log[0])
                        .build();
                responseObserver.onNext(logStored);
            }
            br.close();
            fr.close();
        } catch (Exception e) {
            System.out.println("Error while reading log file");
        }
        responseObserver.onCompleted();
    }
}
