import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client {
    
    /** 
     * @param args
     */
    public static void main(String[] args) {
        String hostname = "localhost";
        int port = 0415;

        Client client = new Client(hostname, port);
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

        try {
            while(true) {

                String request = inFromUser.readLine();
                String response = client.makeRequest(request);

                System.out.println(response);
            }
        }
        catch (IOException ioe) {}
    }

    private String hostname;
    private int port;

    public Client(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    
    /** 
     * @param request
     * @return String
     * @throws IOException
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