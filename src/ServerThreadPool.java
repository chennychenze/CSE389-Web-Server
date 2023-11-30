import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerThreadPool {

    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            throw new IllegalArgumentException("Parameter(s): <Port> <Threads>");
        }

        int ServPort = Integer.parseInt(args[0]); // Server port
        int threadPoolSize = Integer.parseInt(args[1]);


        final ServerSocket servSock = new ServerSocket(ServPort);

        final Logger logger = Logger.getLogger("practical");


        for (int i = 0; i < threadPoolSize; i++) {
            Thread thread = new Thread() {
                public void run() {
                    while (true) {
                        try {
                            Socket clntSock = servSock.accept();
                            ServerProtocol.handleClient(clntSock, logger);
                        } catch (IOException ex) {
                            logger.log(Level.WARNING, "Client accept failed", ex);
                        }
                    }
                }
            };
            thread.start();
            logger.info("Created and started Thread = " + thread.getName());
        }
    }
}