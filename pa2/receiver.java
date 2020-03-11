import java.net.InetAddress;
import java.net.UnknownHostException;

public class receiver {

    private static String usage = "Usage - java receiver [URL] " +
                                  "[portNumber]. URL is a valid URL and " +
                                  "port number is a valid integer between 0 " +
                                  "and 65535 (inclusive).";

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println(usage);
            return;
        }

        String URL = args[0];
        String stringPortNumber = args[1];

        int portNumber;

        try {
            // Try to get the IP address of the provided URL.
            // Throws UnknownHostException if it's not a valid hostname or IP address.
            InetAddress.getByName(URL);

            // Throws NumberFormatException if it's not a valid integer.
            portNumber = Integer.parseInt(stringPortNumber);
        }
        catch (UnknownHostException | NumberFormatException e) {
            e.printStackTrace();
            System.out.println(usage);
            return;
        }

         if (portNumber < 0 || portNumber > 65535) {
            System.out.println(usage);
            return;
         }

         startReceiver(URL, portNumber);
    }

    // TODO - receiver implementation
    private static void startReceiver(String URL, int portNumber) {

    }
}