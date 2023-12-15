import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.logging.*;

public class JHTTP {

    // Logger for logging server events
    private static final Logger logger = Logger.getLogger(JHTTP.class.getCanonicalName());

    // Number of threads in the thread pool
    private static final int NUM_THREADS = 50;

    // Default file to serve if not specified in the request
    private static final String INDEX_FILE = "index.html";

    // Root directory of the server
    private final File rootDirectory;

    // Port on which the server listens for connections
    private final int port;

    // Constructor to initialize the server with a root directory and port
    public JHTTP(File rootDirectory, int port) throws IOException {
        // Check if the provided root directory is a valid directory
        if (!rootDirectory.isDirectory()) {
            throw new IOException(rootDirectory + " does not exist as a directory");
        }

        // Initialize the root directory and port
        this.rootDirectory = rootDirectory;
        this.port = port;
    }

    // Main method to start the server
    public static void main(String[] args) {
        File docRoot;
        try {
            // Get the root directory from the command line arguments
            docRoot = new File(args[0]);
        } catch (ArrayIndexOutOfBoundsException ex) {
            // Display usage information if root directory is not provided
            System.out.println("Usage: java JHTTP docRoot port");
            return;
        }

        int port;
        try {
            // Get the port from the command line arguments
            port = Integer.parseInt(args[1]);

            // Ensure the port is within valid range, otherwise use the default (80)
            if (port < 0 || port > 65535) port = 80;
        } catch (RuntimeException ex) {
            // Use the default port (80) if a valid port is not provided
            port = 80;
        }

        try {
            // Create an instance of JHTTP with the specified root directory and port
            JHTTP webserver = new JHTTP(docRoot, port);

            // Start the server
            webserver.start();
        } catch (IOException ex) {
            // Log a severe error if the server fails to start
            logger.log(Level.SEVERE, "Server could not start", ex);
        }
    }

    // Method to start the server
    public void start() throws IOException {
        // Create a thread pool with a fixed number of threads
        ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS);

        // Use try-with-resources to automatically close the server socket
        try (ServerSocket server = new ServerSocket(port)) {

            // Log server information
            logger.info("Accepting connections on port " + server.getLocalPort());
            logger.info("Document Root: " + rootDirectory);

            // Continuously listen for incoming connections
            while (true) {
                try {
                    // Accept incoming connection requests
                    Socket request = server.accept();

                    // Create a new RequestProcessor runnable and submit it to the thread
                    Runnable r = new RequestProcessor(rootDirectory, INDEX_FILE, request);
                    pool.submit(r);
                } catch (IOException ex) {
                    // Log a warning if an error occurs while accepting a connection
                    logger.log(Level.WARNING, "Error accepting connection", ex);
                }
            }
        }
    }
}