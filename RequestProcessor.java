import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.*;
import java.nio.charset.StandardCharsets;

public class RequestProcessor implements Runnable {

    // Logger for logging information and warnings
    private final static Logger logger = Logger.getLogger(
            RequestProcessor.class.getCanonicalName());

    // Instance variables
    private File rootDirectory;
    private String indexFileName = "index.html";
    private Socket connection;

    private String httpCommand;

    private String basicAuthHeader;
    private boolean isAdmin  = false;

    //instance of CacheRequest for managing caching.....
    private CacheRequest cacheRequest;

    // Constructor to initialize instance variables
    public RequestProcessor(File rootDirectory,
                            String indexFileName, Socket connection, CacheRequest cacheRequest) {
        logger.log(Level.WARNING, "first line log");
        // Check if rootDirectory is a directory, not a file
        if (rootDirectory.isFile()) {
            throw new IllegalArgumentException(
                    "rootDirectory must be a directory, not a file");
        }

        try {
            // Get the canonical file representation of the root directory
            rootDirectory = rootDirectory.getCanonicalFile();
        } catch (IOException ex) {
            // Ignore IOException during canonicalization
        }

        // Initialize instance variables
        this.rootDirectory = rootDirectory;

        //set reference to CacheRequest.....
        this.cacheRequest = cacheRequest;

        // Set index file name, if provided
        if (indexFileName != null) this.indexFileName = indexFileName;

        // Set the connection
        this.connection = connection;
        //this.run();
    }
    private void readRequestHeader() {
        try {
            int counter = 0;
            InputStream is = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                logger.log(Level.WARNING, "line is: " + line);
                if(line.equals("")) {
                    break;
                }
                if (counter == 0) {
                    this.httpCommand = line;
                }
                if (line.startsWith("Authorization: Basic ")) {
                    this.basicAuthHeader = line;
                    return;
                }
                counter++;
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error reading request for authentication", e);
        }
        logger.log(Level.WARNING, "Last line in read Header");
        return;
    }
    // Authentication method
    private boolean authenticate(Socket connection, String credentials) {
        try {
            String line = this.basicAuthHeader;
            if (line != null) {
                String base64Credentials = line.substring("Authorization: Basic ".length()).trim();
                byte[] userPass = (Base64.getDecoder().decode(base64Credentials));
                String decodedString = new String(userPass);
                logger.log(Level.WARNING, "Authentication: " + userPass);
                if (decodedString.equals(credentials)) {
                    if(credentials.startsWith("admin:")) {
                        isAdmin = true;
                    }
                    else {
                        isAdmin = false;
                    }
                    logger.log(Level.WARNING, "Passed authentication");
                }
                logger.log(Level.WARNING, "before return equals");
                return decodedString.equals(credentials);
            }
        } finally {
            logger.log(Level.WARNING, "Last line in authenticate");
        }
        return false;
    }
    private void sendAuthenticationRequiredResponse() {
        try {
            OutputStream raw = new BufferedOutputStream(connection.getOutputStream());
            Writer out = new OutputStreamWriter(raw);

            String body = "<HTML>\r\n"
                    + "<HEAD><TITLE>Authentication Required</TITLE>\r\n"
                    + "</HEAD>\r\n"
                    + "<BODY>"
                    + "<H1>HTTP Error 401: Authentication Required</H1>\r\n"
                    + "</BODY></HTML>\r\n";
            //out.write("WWW-Authenticate: Basicrealm=\"localhost\"\r\n");
            String responseCode = "HTTP/1.0 401 Unauthorized\r\n" + "WWW-Authenticate: Basic realm=\"User Visible Realm\"\r\n";
            sendHeader(out, responseCode, "text/html; charset=utf-8", body.length());
            out.write(body);
            out.flush();

            closeWriter(out);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error sending authentication response", e);
        }
    }
    private void sendAuthenticationSuccessResponse() {
        try {
            PrintWriter out = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(connection.getOutputStream())),
                    true);
            out.write("HTTP/1.0 200 OK\r\n");
            out.write("Date: Fri, 31 Dec 1999 23:59:59 GMT\r\n");
            out.write("Server: Apache/0.8.4\r\n");
            out.write("Content-Type: text/html\r\n");
            out.write("Content-Length: 59\r\n");
            out.write("Expires: Sat, 01 Jan 2000 00:59:59 GMT\r\n");
            out.write("Last-modified: Fri, 09 Aug 1996 14:21:40 GMT\r\n");
            out.write("\r\n");
            out.write("<TITLE>Exemple</TITLE>");
            out.write("<P>Hello.</P>");
            out.flush();
            out.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void run() {
        // For security checks
        String root = rootDirectory.getPath();
        Writer out = null;
        // Comment out the following block if you want to use authentication
        // Uncomment the lines below and replace "username:password" with your desired credentials
        logger.log(Level.WARNING, "Run in different thread");
        readRequestHeader();
        if (!authenticate(connection, "admin:admin") && !authenticate(connection, "user:user")) {
            sendAuthenticationRequiredResponse();
            try {
                connection.close(); // Close the connection
            } catch (IOException e) {
                logger.log(Level.WARNING, "Error closing connection", e);
            }
            return;

        }

        try {
            // Set up input and output streams for communication with the client
            OutputStream raw = new BufferedOutputStream(
                    connection.getOutputStream()
            );
            out = new OutputStreamWriter(raw);
            Reader in = new InputStreamReader(
                    new BufferedInputStream(
                            connection.getInputStream()
                    ), "US-ASCII"
            );


            // Convert the request line to a string
            String get = this.httpCommand;

            // Log information about the request
            logger.info(connection.getRemoteSocketAddress() + " " + get);

            // Split the request into tokens
            String[] tokens = get.split("\\s+");
            String method = tokens[0];
            String version = "";
            if (method.equals("GET")) {
                // Process a GET request
                String fileName = tokens[1];
                // Append index file name if the requested file ends with "/"
                if (fileName.endsWith("/")) fileName += indexFileName;
                // Check if the requested file is special.html
                if (fileName.equals("/special.html")) {
                    // Deny access for user:user even if authenticated
                    if (!isAdmin) {
                        sendAuthenticationRequiredResponse();
                        connection.close();
                        return;
                    }
                }

                // Get the content type based on the file extension
                String contentType =
                        URLConnection.getFileNameMap().getContentTypeFor(fileName);

                if (tokens.length > 2) {
                    version = tokens[2];
                }

                // Construct the file object for the requested resource
                File theFile = new File(rootDirectory,
                        fileName.substring(1, fileName.length()));

                // Check if the file is readable and within the document root
                if (theFile.canRead() && theFile.getCanonicalPath().startsWith(root)) {
                    // Read the file content into a byte array
                    byte[] theData = Files.readAllBytes(theFile.toPath());

                    if (version.startsWith("HTTP/")) { // Send a MIME header
                        sendHeader(out, "HTTP/1.0 200 OK", contentType, theData.length);
                    }

                    // Send the file content; it may be binary data, so use raw output stream
                    raw.write(theData);
                    raw.flush();

                    //Cache data for future requests.....
                    cacheRequest.addToCache(fileName, new String(theData, StandardCharsets.UTF_8));

                    //log message related to cache.....
                    logger.info("Cache request served for: " + fileName);
                    
                } else { // File not found
                    String body = new StringBuilder("<HTML>\r\n")
                            .append("<HEAD><TITLE>File Not Found</TITLE>\r\n")
                            .append("</HEAD>\r\n")
                            .append("<BODY>")
                            .append("<H1>HTTP Error 404: File Not Found</H1>\r\n")
                            .append("</BODY></HTML>\r\n").toString();

                    if (version.startsWith("HTTP/")) { // Send a MIME header
                        sendHeader(out, "HTTP/1.0 404 File Not Found",
                                "text/html; charset=utf-8", body.length());
                    }

                    // Send the error response body
                    out.write(body);
                    out.flush();
                }
            } else { // Method is not "GET"
                String body = new StringBuilder("<HTML>\r\n")
                        .append("<HEAD><TITLE>Not Implemented</TITLE>\r\n")
                        .append("</HEAD>\r\n")
                        .append("<BODY>")
                        .append("<H1>HTTP Error 501: Not Implemented</H1>\r\n")
                        .append("</BODY></HTML>\r\n").toString();

                if (version.startsWith("HTTP/")) { // Send a MIME header
                    sendHeader(out, "HTTP/1.0 501 Not Implemented",
                            "text/html; charset=utf-8", body.length());
                }

                // Send the error response body
                out.write(body);
                out.flush();
            }
        } catch (IOException ex) {
            // Log a warning for errors during communication
            logger.log(Level.WARNING,
                    "Error talking to " + connection.getRemoteSocketAddress(), ex);
        } finally {
            try {
                // Close the connection in the finally block
                connection.close();
            } catch (IOException ex) {
                // Ignore IOException during connection close
            }
        }

    }

    // Helper method to close the Writer
    private void closeWriter(Writer writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Error closing Writer", e);
            }
        }
    }

    // Helper method to send the HTTP response header for authentication failure
    private void sendAuthenticationRequiredResponse(Writer out) throws IOException {
        String body = "<HTML>\r\n"
                + "<HEAD><TITLE>Authentication Required</TITLE>\r\n"
                + "</HEAD>\r\n"
                + "<BODY>"
                + "<H1>HTTP Error 401: Authentication Required</H1>\r\n"
                + "</BODY></HTML>\r\n";

        sendHeader(out, "HTTP/1.0 401 Unauthorized", "text/html; charset=utf-8", body.length());
        out.write(body);
        out.flush();
    }

    // Helper method to send the HTTP response header
    private void sendHeader(Writer out, String responseCode,
                            String contentType, int length)
            throws IOException {
        out.write(responseCode + "\r\n");
        Date now = new Date();
        out.write("Date: " + now + "\r\n");
        out.write("Server: JHTTP 2.0\r\n");
        out.write("Content-length: " + length + "\r\n");
        out.write("Content-type: " + contentType + "\r\n\r\n");
        out.flush();
    }
}
