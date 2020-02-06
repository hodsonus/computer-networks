import java.util.HashMap;
import java.util.Map;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    /* TODO - This method has no business being in the server code, will remove
     * in future projects when refactoring.
     */
    public static void main(String[] args) {
        int port = 415;
        Server server = null;

        try {
            server = getServerInstance(port);
            server.listen();
            server.close();
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

        System.out.println("Listening on port " + this.port + "...");

        while (stillRunning) {
            // throws IOException if server socket is closed or we are out of resources
            Socket socket = serverSocket.accept();
            System.out.println("get connection from " + socket.getInetAddress().toString());

            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(socket.getOutputStream());

            // throws IOException if inFromClient is closed
            String clientInput = inFromClient.readLine();

            // TODO - make this multithreaded
            ClientRequestResult clientRequestResult = this.handleRequest(clientInput);

            String serverOutput = clientRequestResult.response;
            // throws IOException if outToClient is closed
            outToClient.writeBytes(serverOutput + "\n");

            stillRunning = clientRequestResult.stillRunning;
        }
    }

    /**
     * @param clientRequest
     *
     * @return ServerResponse - ADT representing the server response to the client
     * and the state of our server after the response is sent to the client.
     *
     * Handles the client request. Future projects should extract this server class
     * out into an abstract class. Subclasses should override this method.
     */
    public ClientRequestResult handleRequest(String clientRequest) {
        ClientRequestResult crr = new ClientRequestResult();
        crr.stillRunning = true;

        if (clientRequest == null) { // incorrect operation command
            crr.response = "-1";
            return crr;
        }
        
        String[] clientRequestArr = clientRequest.split(" ");
        String op = clientRequestArr[0];
        if (op .equals("add") || op .equals("subtract") || op .equals("multiply")) {
            performArrayOperation(clientRequestArr, crr);
        }
        else if (clientRequestArr[0].equals("bye")) { // exit
            crr.response = "-5";
            crr.stillRunning = false;
        }
        else {  // incorrect operation command
            crr.response = "-1";
        }

        System.out.println("get: " + clientRequest + ", return: " + crr.response);
    
        return crr;
    }

    /* TODO - This method has no business being in the server code, will remove
     * in future projects when refactoring.
     */
    public static void performArrayOperation(String[] arr, ClientRequestResult crr) {
        if (arr.length < 3) { // number of inputs is less than two
            crr.response = "-2";
        }
        else if (arr.length > 5) { // number of inputs is more than four
            crr.response = "-3";
        }
        else { // handle OK operation
            int accum;
            try { // set our accumulator equal to the first operator
                accum = Integer.parseInt(arr[1]);
            }
            catch (NumberFormatException nfe) { // one or more of the inputs contain(s) non-number(s)
                crr.response = "-4";
                return;
            }

            // Operator is the first element in the array
            String op = arr[0];
            for (int i = 2; i < arr.length; ++i) {
                try {
                    if (op.equals("add")) {
                        accum += Integer.parseInt(arr[i]);
                    }
                    else if (op.equals("subtract")) {
                        accum -= Integer.parseInt(arr[i]);
                    }
                    else if (op.equals("multiply")) {
                        accum *= Integer.parseInt(arr[i]);
                    }
                    else throw new IllegalArgumentException();
                }
                catch (NumberFormatException nfe) { // one or more of the inputs contain(s) non-number(s)
                    crr.response = "-4";
                    return;
                }
            }

            crr.response = Integer.toString(accum);
        }
    }

    /** 
     * @return boolean - true if the socket was able to be closed, false otherwise.
     * 
     * Closes the socket member variable.
     */
    public boolean close() {
        try {
            serverSocket.close();
        }
        catch (IOException ioe) {
            return false;
        }

        return true;
    }

    class ClientRequestResult {
        public String response;
        public boolean stillRunning;
        public ClientRequestResult(String response, boolean stillRunning) {
            this.response = response;
            this.stillRunning = stillRunning;
        }
        public ClientRequestResult() {}
    }
}