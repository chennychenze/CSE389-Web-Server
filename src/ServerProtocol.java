import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerProtocol implements Runnable {
    private static final int BUFSIZE = 32;
    private Socket clntSock;
    private Logger logger;

    public ServerProtocol(Socket clntSock, Logger logger) {
        this.clntSock = clntSock;
        this.logger = logger;
    }

    public static void handleClient(Socket clntSock, Logger logger) {
        try {

            InputStream in = clntSock.getInputStream();
            OutputStream out = clntSock.getOutputStream();

            int recvMsgSize;
            int totalBytesEchoed = 0;
            byte[] echoBuffer = new byte[BUFSIZE];

            while ((recvMsgSize = in.read(echoBuffer)) != -1) {
                out.write(echoBuffer, 0, recvMsgSize);
                totalBytesEchoed += recvMsgSize;
            }

            logger.info("Client " + clntSock.getRemoteSocketAddress() + ", echoed "
                    + totalBytesEchoed + " bytes.");

        } catch (IOException ex) {
            logger.log(Level.WARNING, "Exception in echo protocol", ex);
        } finally {
            try {
                clntSock.close();
            } catch (IOException e) {
            }
        }
    }

    public void run() {
        handleClient(clntSock, logger);
    }
}