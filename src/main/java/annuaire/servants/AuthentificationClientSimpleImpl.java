package annuaire.servants;

import annuaire.bd.ListeAuth;
import com.proto.annuaire.AuthentificationServiceClientGrpc;
import com.proto.annuaire.Log;
import com.proto.annuaire.LoggingServiceGrpc;
import com.proto.annuaire.TypeCR;
import io.grpc.*;

import java.util.Objects;
import java.util.logging.Logger;

public class AuthentificationClientSimpleImpl extends AuthentificationServiceClientGrpc.AuthentificationServiceClientImplBase implements ServerInterceptor {
    private ListeAuth listeAuth;
    private LoggingServiceGrpc.LoggingServiceBlockingStub rechClient;
    private String host; // contiendra l'adresse IP du client
    private int port; // contiendra le port du client
    private static final Logger logger = Logger.getLogger(AuthentificationClientSimpleImpl.class.getName());


    public AuthentificationClientSimpleImpl(ListeAuth listeAuth) {
        this(listeAuth, 3244);
    }

    public AuthentificationClientSimpleImpl(ListeAuth listeAuth, int channelPort) {
        this.listeAuth = listeAuth;
        this.rechClient = LoggingServiceGrpc.newBlockingStub(
                ManagedChannelBuilder.forAddress("localhost", channelPort)
                        .usePlaintext()
                        .build());
    }

    @Override
    public void check(com.proto.annuaire.Identifiant request,
                      io.grpc.stub.StreamObserver<com.proto.annuaire.AUDResponse> responseObserver) {
        Log log = Log.newBuilder()
                .setHorodatage(java.time.LocalDate.now() + " " + java.time.LocalTime.now())
                .setRequest("check")
                .setLogin(request.getLogin())
                .setPasswd(request.getPasswd())
                .setIp(host)
                .setResponse(TypeCR.ACCEPTED_GOOD_VALUES)
                .setPortNumber(port)
                .build();
        if (listeAuth.tester(request.getLogin(), request.getPasswd())) {
            responseObserver.onNext(com.proto.annuaire.AUDResponse.newBuilder().setCr(TypeCR.ACCEPTED_GOOD_VALUES).build());
            //add to the log the response
            log = log.toBuilder().setResponse(TypeCR.ACCEPTED_GOOD_VALUES).build();
        } else {
            responseObserver.onNext(com.proto.annuaire.AUDResponse.newBuilder().setCr(TypeCR.DENY_BAD_VALUES).build());
            //add to the log the response
            log = log.toBuilder().setResponse(TypeCR.DENY_BAD_VALUES).build();
        }
        rechClient.log(log);
        responseObserver.onCompleted();
    }

    /** Méthode permettant de récupérer l'adresse IP et le port du client
     * @param inetSocketString : adresse IP et port du client sous la forme /IP:port
     */
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
        logger.info("Client ip : " + host + " et port : " + port);
        // Puis on appelle la méthode qui va appeler la bonne méthode gRPC
        return serverCallHandler.startCall(serverCall, metadata);
    }
}
