import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.logging.*;

public class JHTTP {

    // Logger for logging server activity
    private static final Logger logger = Logger.getLogger(
            JHTTP.class.getCanonicalName());

    // Number of threads in the thread pool
    private static final int NUM_THREADS = 50;

    // Default index file
    private static final String INDEX_FILE = "index.html";

    // Root directory of the server
    private final File rootDirectory;

    // Port on which the server will listen
    private final int port;

    // instance of CacheRequest for managing caching.....
    private CacheRequest cacheRequest;

    // Constructor for initializing the server with a root directory and port
    public JHTTP(File rootDirectory, int port) throws IOException {

        // Check if the provided root directory exists and is a directory
        if (!rootDirectory.isDirectory()) {
            throw new IOException(rootDirectory
                    + " does not exist as a directory");
        }

        // Initialize instance variables
        this.rootDirectory = rootDirectory;
        this.port = port;

        //creates new instance of CacheRequest.....
        this.cacheRequest = new CacheRequest();
    }

    // Method to start the server
    public void start() throws IOException {
        // Create a thread pool with a fixed number of threads
        ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS);

        // Open a server socket on the specified port
        try (ServerSocket server = new ServerSocket(port)) {

            // Log server information
            logger.info("Accepting connections on port " + server.getLocalPort());
            logger.info("Document Root: " + rootDirectory);

            // Infinite loop to accept incoming connections
            while (true) {
                try {
                    logger.log(Level.WARNING, "BEFORE SERVER statement");
                    // Accept an incoming connection
                    Socket request = server.accept();
                    logger.log(Level.WARNING, "AFTER SERVER statement");
                    // Create a RequestProcessor (a Runnable) for processing the request
                    Runnable r = new RequestProcessor(
                            rootDirectory, INDEX_FILE, request, cacheRequest);
                    logger.log(Level.WARNING, "AFTER REQUEST PROCESSOR method call");
                    // Submit the RequestProcessor to the thread pool for execution
                    pool.submit(r);
                } catch (IOException ex) {
                    // Log a warning if there's an error accepting a connection
                    logger.log(Level.WARNING, "Error accepting connection", ex);
                }
            }
        }
    }

    // Main method, the entry point of the program
    public static void main(String[] args) {

        // Get the document root from command line arguments
        File docroot;
        try {
            docroot = new File(args[0]);
        } catch (ArrayIndexOutOfBoundsException ex) {
            // Display usage message if the document root is not provided
            System.out.println("Usage: java JHTTP docroot port");
            return;
        }

        // Set the port to listen on, default to 80 if not provided or invalid
        int port;
        try {
            port = Integer.parseInt(args[1]);
            if (port < 0 || port > 65535) port = 80;
        } catch (RuntimeException ex) {
            port = 80;
        }

        // Create and start the JHTTP server
        try {
            JHTTP webserver = new JHTTP(docroot, port);
            webserver.start();
        } catch (IOException ex) {
            // Log a severe error message if the server cannot start
            logger.log(Level.SEVERE, "Server could not start", ex);
        }
    }
}