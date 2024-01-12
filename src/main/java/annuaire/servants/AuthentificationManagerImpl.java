package annuaire.servants;

import annuaire.bd.ListeAuth;
import com.proto.annuaire.*;
import io.grpc.*;
import io.grpc.stub.StreamObserver;

import java.util.Objects;
import java.util.logging.Logger;

public class AuthentificationManagerImpl extends AuthentificationServiceManagerGrpc.AuthentificationServiceManagerImplBase implements ServerInterceptor {
    private ListeAuth listeAuth;
    private LoggingServiceGrpc.LoggingServiceBlockingStub rechClient;
    private String host; // contiendra l'adresse IP du client
    private int port; // contiendra le port du client
    private static final Logger logger = Logger.getLogger(AuthentificationClientSimpleImpl.class.getName());


    public AuthentificationManagerImpl(ListeAuth listeAuth, int portLog) {
        this.listeAuth = listeAuth;
        this.rechClient = LoggingServiceGrpc.newBlockingStub(
                ManagedChannelBuilder.forAddress("localhost", portLog)
                        .usePlaintext()
                        .build());
    }

    @Override
    public void check(Identifiant request,
                       StreamObserver<AUDResponse> responseObserver) {
        if (listeAuth.tester(request.getLogin(), request.getPasswd())) {
            responseObserver.onNext(AUDResponse.newBuilder().setCr(TypeCR.ACCEPTED_GOOD_VALUES).build());
        } else {
            responseObserver.onNext(AUDResponse.newBuilder().setCr(TypeCR.DENY_BAD_VALUES).build());
        }
        Log log = constructLog(request, "check");
        rechClient.log(log);
        responseObserver.onCompleted();
    }

    @Override
    public void add(Identifiant request,
                    StreamObserver<AUDResponse> responseObserver) {
        if (listeAuth.creer(request.getLogin(), request.getPasswd())) {
            responseObserver.onNext(AUDResponse.newBuilder().setCr(TypeCR.DONE).build());
        } else {
            responseObserver.onNext(AUDResponse.newBuilder().setCr(TypeCR.DENY_DUPLICATED).build());
        }
        Log log = constructLog(request, "add");
        rechClient.log(log);
        responseObserver.onCompleted();
    }

    @Override
    public void delete(Identifiant request, StreamObserver<AUDResponse> responseObserver) {
        if (listeAuth.supprimer(request.getLogin(), request.getPasswd())) {
            responseObserver.onNext(AUDResponse.newBuilder().setCr(TypeCR.DONE).build());
        } else {
            responseObserver.onNext(AUDResponse.newBuilder().setCr(TypeCR.DENY_DUPLICATED).build());
        }
        Log log = constructLog(request, "delete");
        rechClient.log(log);
        responseObserver.onCompleted();
    }

    @Override
    public void update(Identifiant request, StreamObserver<AUDResponse> responseObserver) {
        if (listeAuth.mettreAJour(request.getLogin(), request.getPasswd())) {
            responseObserver.onNext(AUDResponse.newBuilder().setCr(TypeCR.DONE).build());
        } else {
            responseObserver.onNext(AUDResponse.newBuilder().setCr(TypeCR.DENY_NOT_FOUND).build());
        }
        Log log = constructLog(request, "update");
        rechClient.log(log);
        responseObserver.onCompleted();
    }

    protected void getRemoteAddr(String inetSocketString) {
        // The substring is simply host:port, even if host is IPv6 as it fails to use []. Can't use
        // standard parsing because the string isn't following any standard.
        host = inetSocketString.substring(0, inetSocketString.lastIndexOf(':'));
        port = Integer.parseInt(inetSocketString.substring(inetSocketString.lastIndexOf(':') + 1));
    }
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> serverCall,
            Metadata metadata,
            ServerCallHandler<ReqT, RespT> serverCallHandler){
        // on récupère les attributs de la requête
        Attributes attributes = serverCall.getAttributes();
        // on récupère l'attribut contenant l'adresse IP et le port du client
        String clientIpPort = Objects.requireNonNull(attributes.get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR)).toString();
        // on appelle la méthode qui va extraire l'IP et le port du client
        this.getRemoteAddr(Objects.requireNonNull(attributes.get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR)).toString());
        logger.info("Client IP: " + host + " Port: " + port);
        // Puis on appelle la méthode qui va appeler la bonne méthode gRPC
        return serverCallHandler.startCall(serverCall, metadata);
    }

    private Log constructLog(Identifiant request, String requestType){
        return Log.newBuilder()
                .setHorodatage(java.time.LocalDate.now() + " " + java.time.LocalTime.now())
                .setRequest(requestType)
                .setLogin(request.getLogin())
                .setPasswd(request.getPasswd())
                .setIp(host)
                .setResponse(TypeCR.ACCEPTED_GOOD_VALUES)
                .setPortNumber(port)
                .build();
    }
}
