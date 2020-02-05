import java.util.HashMap;
import java.util.Map;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        int port = 0415;
        try {
            Server server = getServerInstance(port);
            server.listen();
        }
        catch (IOException ioe) {}
    }

    /* Static Members */

    private static Map<Integer, Server> servers = new HashMap<Integer, Server>();

    /**
     * @param port - the desired port number.
     *
     * @return Server - the server for the desired port number.
     *
     * @throws IOException - such an exception implies that the port resource
     * is busy with another service not belonging to an instance of this class.
     *
     * Implements singleton behavior for a particular port number. Will return
     * a new server for new port numbers and the same server for the same port.
     */
    public static Server getServerInstance(int port) throws IOException {
        if (!servers.containsKey(port)) {
            servers.put(port, new Server(port));
        }

        return servers.get(port);
    }

    /* Instance Members */

    private int port;
    private ServerSocket serverSocket;

    private Server(int port) throws IOException {
        this.port = port;
        serverSocket = new ServerSocket(this.port);
    }

    /**
     * @throws IOException - such an exception implies that we are dealing with
     * a closed socket (the socket member) or we are out of resources for
     * input/output buffers.
     *
     * Implements the listening behavior of a server. Will make calls to
     * the handleRequest method to perform custom desired behavior for client
     * requests.
     */
    public void listen() throws IOException {

        boolean stillRunning = true;

        while (stillRunning) {
            // throws IOException if server socket is closed or we are out of resources
            Socket socket = serverSocket.accept();
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(socket.getOutputStream());

            // throws IOException if inFromClient is closed
            String clientInput = inFromClient.readLine();

            // TODO - make this multithreaded
            ClientRequestResult clientRequestResult = this.handleRequest(clientInput);

            String serverOutput = clientRequestResult.response;
            // throws IOException if outToClient is closed
            outToClient.writeBytes(serverOutput);

            stillRunning = clientRequestResult.serverStatus;
        }
    }

    /**
     * @param clientInput
     *
     * @return ServerResponse - ADT representing the server response to the client
     * and the state of our server after the response is sent to the client.
     *
     * Handles the client request. Future projects should extract this server class
     * out into an abstract class. Subclasses should override this method.
     */
    public ClientRequestResult handleRequest(String clientInput) {
        System.out.println("Hello world!");
        return new ClientRequestResult("response", true); // TODO
    }

    class ClientRequestResult {
        public String response;
        public boolean serverStatus;
        public ClientRequestResult(String response, boolean serverStatus) {
            this.response = response;
            this.serverStatus = serverStatus;
        }
    }
}