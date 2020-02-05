import java.util.HashMap;
import java.util.Map;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    /* Static Members */
    private static Map<Integer, Server> servers = new HashMap<Integer, Server>();

    /**
     * @param port
     * @return Server
     * @throws IOException
     */
    public static Server getServerInstance(int port) throws IOException {
        if (!servers.containsKey(port)) {
            servers.put(port, new Server(port));
        }

        return servers.get(port);
    }


    /**
     * @param args
     */
    /* Main */
    public static void main(String[] args) {
        int port = 0415;
        try {
            Server server = getServerInstance(port);
            server.listen();
        }
        catch (IOException ioe) {}
    }

    /* Instance Members */
    private int port;
    private ServerSocket serverSocket;

    private Server(int port) throws IOException {
        this.port = port;
        serverSocket = new ServerSocket(this.port);
    }

    /**
     * @throws IOException
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
     * @return ServerResponse
     */
    public ClientRequestResult handleRequest(String clientInput) {
        System.out.println("Hello world!");
        return new ClientRequestResult("response", true); // TODO
    }
}

class ClientRequestResult {
    public String response;
    public boolean serverStatus;

    public ClientRequestResult(String response, boolean serverStatus) {
        this.response = response;
        this.serverStatus = serverStatus;
    }
}