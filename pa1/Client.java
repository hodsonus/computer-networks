import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client {
    /* TODO - This method has no business being in the client code, will remove
     * in future projects when refactoring.
     */
    public static void main(String[] args) {
        String hostname = "localhost";
        int port = 415;

        Client client = new Client(hostname, port);
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

        try {
            while(true) {

                System.out.print("C> ");
                String request = inFromUser.readLine();

                String response = client.makeRequest(request);

                handleServerResponse(response);
            }
        }
        catch (IOException ioe) {}
    }

    /* TODO - This method has no business being in the client code, will remove
     * in future projects when refactoring.
     */
    public static void handleServerResponse(String serverResponse) {
        // TODO - application specific code goes here
        System.out.println(serverResponse);
    }

    /* Instance Members */

    private String hostname;
    private int port;

    public Client(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    /**
     * @param request - the request to be sent over TCP to the server.
     *
     * @return String - the server response to our request.
     *
     * @throws IOException - such an exception implies that we are out of I/O
     * resources for our input/output buffers.
     *
     * Initiate a TCP connection and send the request paramater to the server
     * specified by the hostname and the port instance members.
     */
    public String makeRequest(String request) throws IOException {

        Socket clientSocket = new Socket(this.hostname, this.port);
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        outToServer.writeBytes(request + '\n');

        String response = inFromServer.readLine();

        clientSocket.close();
        return response;
    }
}