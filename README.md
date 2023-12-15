# CSE 389 - Web System Architecture and Programming - Term Project

## Building a Simple Web Server using Java
Authors: Chenze Chen, Aaron Alakkadan, Adebayo Afolabi, Benjamin Canfield

### Features

- [x] **HTTP Get Request**
- [x] **HTTP Head Request**
- [x] **HTTP Post Request**
- [x] **Logging**
- [x] **Multi-Threading**
- [x] **Cached Requests**
- [x] **Authentication**
- [x] **Authorization**

## System Architecture

This project consists of several key files responsible for different functionalities:

- **JHTTP.java**: Initializes the HTTP server and manages incoming client requests.
  
- **RequestProcessor.java**: Processes incoming requests, determining appropriate actions based on the request type (GET, POST, HEAD).
  
- **CacheRequest.java**: Implements a basic caching system utilizing a `HashMap`. It provides methods to store and retrieve data based on keys (usually file names).
  
- **DataFetcher.java**: Demonstrates caching functionality by utilizing a `CacheRequest` instance to manage data storage and retrieval.
  
- **index.html**: The default file served by the server.
  
- **styles.css**: CSS file for `index.html`, enhancing its appearance.
  
- **hello.html**: A welcome page designed for authenticated users, facilitating testing of "GET" and "POST" methods.
  
- **special.html**: A file used for testing web page accessibility, restricted to access by the **admin/admin** credentials only.

## How to Run and Test the Project

1. **Clone the Repository:**

    ```bash
    git clone <repository_url>
    ```

2. **Compile the Java Files:**

    Compile the `JHTTP.java` and `RequestProcessor.java` files.

3. **Start the Server:**

    Start the server by navigating to the project directory in the terminal and executing the following command:

    ```bash
    java JHTTP "<your_project_directory_path>" <port_number>
    ```

    Replace `<your_project_directory_path>` with the path to your project directory and `<port_number>` with the desired port number for the server.

4. **Access the Server:**

    Open a new incognito window in your web browser and enter the following URL:

    ```
    http://localhost:<port_number>/
    ```

    Replace `<port_number>` with the port you specified when starting the server.

5. **Authentication:**

    Upon accessing the URL, you'll be prompted for authentication. Use one of the following username/password combinations:

    - **Admin:** "admin/admin"
    - **User:** "user/user"

6. **Explore the Functionalities:**

    - After successful authentication, you'll be directed to the default `index.html` file.
    
    - To view the hello page containing the username via the "POST" request method, navigate to the URL:

      ```
      http://localhost:<port_number>/post-name/hello
      ```

    - Test the authorization feature by accessing the URL:

      ```
      http://localhost:<port_number>/special.html
      ```
