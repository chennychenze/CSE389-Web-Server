import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.*;

public class RequestProcessor implements Runnable {

    // Logger for logging events in RequestProcessor
    private final static Logger logger = Logger.getLogger(RequestProcessor.class.getCanonicalName());
    private static String submittedName = "";
    // Root directory for serving files
    private final File rootDirectory;
    // Socket connection for handling the request
    private final Socket connection;
    // Default index file name
    private String indexFileName = "index.html";

    // Constructor
    public RequestProcessor(File rootDirectory, String indexFileName, Socket connection) throws IOException {
        // Check if the rootDirectory is a directory, not a file
        if (rootDirectory.isFile()) {
            throw new IllegalArgumentException("rootDirectory must be a directory, not a file");
        }

        try {
            // Get the canonical file of the root directory
            this.rootDirectory = rootDirectory.getCanonicalFile();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error getting canonical file", ex);
            throw ex;
        }

        // Set the index file name (default is "index.html")
        if (indexFileName != null) this.indexFileName = indexFileName;

        // Set the socket connection
        this.connection = connection;
    }

    @Override
    public void run() {
        try {
            handleRequest();
        } catch (IOException ex) {
            System.out.println("Error handling client request: " + ex.getMessage());
        } finally {
            try {
                connection.close();
            } catch (IOException ex) {
                // Ignore exception on close
            }
        }
    }

    // Method to handle incoming requests
    private void handleRequest() throws IOException {
        InputStream in = connection.getInputStream();
        OutputStream out = connection.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String requestLine = reader.readLine();
        StringTokenizer tokens = new StringTokenizer(requestLine);
        String method = tokens.nextToken();
        String path = tokens.nextToken();
        String version = tokens.nextToken();

        // Handling GET and HEAD methods
        if (method.equalsIgnoreCase("GET") || method.equalsIgnoreCase("HEAD")) {
            if (path.equals("/post-name/hello")) {
                // Generating HTML content based on a submitted name
                String htmlContent = "<html><head><title>Hello Page</title></head><body>"
                        + "<h1>Welcome to the Hello Page!</h1>"
                        + "<p>Hello, " + submittedName + "!</p>"
                        + "</body></html>";

                // Constructing HTTP response headers
                String header = "HTTP/1.1 200 OK\r\n" +
                        "Content-Length: " + htmlContent.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                        "Content-Type: text/html\r\n\r\n";

                // Sending HTTP response
                out.write(header.getBytes(StandardCharsets.UTF_8));
                out.write(htmlContent.getBytes(StandardCharsets.UTF_8));
            }

            if (path.endsWith("/")) {
                path += indexFileName;
            }

            // Resolving the requested file path
            File file = new File(rootDirectory, path.substring(1)).getCanonicalFile();

            if (file.isFile() && file.exists()) {
                // Retrieving file content and type
                String contentType = URLConnection.getFileNameMap().getContentTypeFor(file.getName());
                byte[] content = readFileInBytes(file);

                if (method.equalsIgnoreCase("GET")) {
                    // Sending HTTP response for GET method
                    String header = "HTTP/1.1 200 OK\r\n" +
                            "Content-Length: " + content.length + "\r\n" +
                            "Content-Type: " + contentType + "\r\n\r\n";
                    out.write(header.getBytes(StandardCharsets.UTF_8));
                    out.write(content);
                } else {
                    // Sending HTTP response for HEAD method
                    String header = "HTTP/1.1 200 OK\r\n" +
                            "Content-Length: " + content.length + "\r\n" +
                            "Content-Type: " + contentType + "\r\n\r\n";
                    out.write(header.getBytes(StandardCharsets.UTF_8));
                }
            } else {
                // Handling file not found error
                String error = "HTTP/1.1 404 Not Found\r\n\r\n";
                out.write(error.getBytes(StandardCharsets.UTF_8));
            }
        } else if (method.equalsIgnoreCase("POST")) {
            // Handling POST requests
            if (path.startsWith("/post-name/hello")) {
                String name = ""; // Declare the name variable

                // Processing the POST request data
                StringBuilder requestData = new StringBuilder();
                int contentLength = 0;

                // Reading request headers to get the Content-Length
                String line;
                while (!(line = reader.readLine()).isEmpty()) {
                    if (line.toLowerCase().startsWith("content-length:")) {
                        contentLength = Integer.parseInt(line.substring(16).trim());
                    }
                }

                // Reading request body (form data)
                char[] body = new char[contentLength];
                int bytesRead = reader.read(body);
                if (bytesRead == contentLength) {
                    requestData.append(body);
                }

                // Parsing received form data to extract the name
                String[] formData = requestData.toString().split("&");
                for (String data : formData) {
                    String[] keyValue = data.split("=");
                    if (keyValue.length == 2 && keyValue[0].equals("name")) {
                        name = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                        break;
                    }
                }

                submittedName = name; // Storing the submitted name

                // Redirecting the client to /post-name/hello
                String redirectHeader = "HTTP/1.1 303 See Other\r\n" +
                        "Location: /post-name/hello\r\n\r\n";

                out.write(redirectHeader.getBytes(StandardCharsets.UTF_8));
                out.flush();

                logger.info("Received name: " + name);
            }
        } else {
            // Handling unimplemented methods
            String error = "HTTP/1.1 501 Not Implemented\r\n\r\n";
            out.write(error.getBytes(StandardCharsets.UTF_8));
        }

        out.flush();
    }

    // Method to read file content in bytes
    private byte[] readFileInBytes(File file) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputStream.toByteArray();
        }
    }
}
