import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class ServerThread {

    public static void main(String[] args) throws IOException {

        if (args.length != 1) {
            throw new IllegalArgumentException("Parameter(s): <Port>");
        }

        int servPort = Integer.parseInt(args[0]); // Server port


        ServerSocket servSock = new ServerSocket(servPort);

        Logger logger = Logger.getLogger("practical");
        System.out.print("starting server with port: " + servPort);

        while (true) {
            Socket clntSock = servSock.accept();

            Thread thread = new Thread(new ServerProtocol(clntSock, logger));
            thread.start();
            logger.info("Created and started Thread " + thread.getName());
        }
    }
}