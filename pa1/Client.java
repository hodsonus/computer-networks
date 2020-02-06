import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        if (args.length < 2) return;
        String hostname = args[0];
        int port;
        try {
            port = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException nfe) { return; }

        try {

            boolean exit = false;
            Socket clientSocket = new Socket(hostname, port);

            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            System.out.println("receive: " + inFromServer.readLine());

            while (!exit) {

                String request = inFromUser.readLine();

                outToServer.writeBytes(request + '\n');

                String response = inFromServer.readLine();

                exit = handleServerResponse(response);
            }

            clientSocket.close();
        }
        catch (IOException ioe) {}
    }

    public static boolean handleServerResponse(String serverResponse) {

        String consoleStr = serverResponse;
        boolean exit = false;

        // map error codes to appropriate meanings
        if ("-1".equals(consoleStr)) { // -1: incorrect operation command.
            consoleStr = "incorrect operation command.";
        }
        else if ("-2".equals(consoleStr)) { // -2: number of inputs is less than two.
            consoleStr = "number of inputs is less than two.";
        }
        else if ("-3".equals(consoleStr)) { // -3: number of inputs is more than four.
            consoleStr = "number of inputs is more than four.";
        }
        else if ("-4".equals(consoleStr)) { // -4: one or more of the inputs contain(s) non-number(s).
            consoleStr = "one or more of the inputs contain(s) non-number(s).";
        }
        else if ("-5".equals(consoleStr)) { // -5: exit.
            consoleStr = "exit.";
            exit = true;
        }

        System.out.println("receive: " + consoleStr);

        return exit;
    }
}