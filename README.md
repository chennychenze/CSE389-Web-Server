# CSE 389 - Web System Architecture and Programming - Term Project
Building a Simple Web Server using Java by Chenze Chen, Aaron Alakkadan, Adebayo Afolabi, Benjamin Canfield

## How to Run and Test the Project

This project demonstrates various HTTP request methods using a Java server. Follow these steps to run and test the functionalities locally:

### Steps:

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
