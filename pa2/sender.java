import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.net.Socket;

public class sender {

    private static String usage = "Usage - java sender [URL] [portNumber] " +
                                  "[MessageFileName]. URL is a valid URL, " +
                                  "port number is a valid integer between 0 " +
                                  "and 65535 (inclusive), and " +
                                  "MessageFileName refers to a valid file " +
                                  "with the last word in the file ending in " +
                                  "a period.";

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println(usage);
            return;
        }

        String URL = args[0];
        String stringPortNumber = args[1]; 
        String MessageFileName = args[2];
        
        Socket networkSocket;
        List<String> messages;

        try {
            // Throws NumberFormatException if it's not a valid integer.
            int portNumber = Integer.parseInt(stringPortNumber);

            /* This convoluted line of code retrieves the Path from the
             * MessageFileName, reads all of the bytes from the corresponding
             * file, converts the byte array to a string, splits it on
             * whitespace, and converts the String[] to List<String>. */
            messages = new ArrayList<String>(Arrays.asList((new String(Files.readAllBytes(Paths.get(MessageFileName))).split("\\s+"))));

            String lastWord = messages.get(messages.size() - 1);
            if (lastWord.charAt(lastWord.length()-1) != '.') {
               System.out.println(usage);
               return;
            }

            // Try to instantiate a socket with the user provided URL and the portNumber.
            // Throws UnknownHostException (subclass of IOException) if it's not a valid hostname or IP address.
            networkSocket = new Socket(URL, portNumber);
        }
        catch (NumberFormatException | IOException | InvalidPathException e) {
            // Expected exceptions, user provided bad input
            e.printStackTrace();
            System.out.println(usage);
            return;
        }

        try {
            senderWorker(networkSocket, messages);
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static void senderWorker(Socket networkSocket, List<String> messages) throws IOException {

        messages.add("-1");

        BufferedReader fromNetwork = new BufferedReader(new InputStreamReader(networkSocket.getInputStream()));
        DataOutputStream toNetwork = new DataOutputStream(networkSocket.getOutputStream());

        List<String> packets = new ArrayList<String>();
        for (int id = 0; id < messages.size(); ++id) {
            String message = messages.get(id);
            int sequenceNo = id % 2;
            int checksum = 0;
            for (char c : message.toCharArray()) checksum += c;
            String packet = String.format("%d %d %d %s", sequenceNo, id, checksum, message);

            packets.add(packet);
        }

        for (int id = 0; id < packets.size(); ++id) {
            String packet = packets.get(id);

            boolean isDrop, isNAK, isCorrupt;
            do {
                toNetwork.writeBytes(packet + "\n");
                String response = fromNetwork.readLine();
                List<String> decomposedResponse = new ArrayList<String>(Arrays.asList(response.split(" ")));
    
                int packetSequenceNo = Integer.parseInt(decomposedResponse.get(0));
                int packetID = Integer.parseInt(decomposedResponse.get(1));
                int packetChecksum = Integer.parseInt(decomposedResponse.get(2));
                String packetMessage = decomposedResponse.get(3);

                int actualChecksum = 0;
                for (char c : packetMessage.toCharArray()) actualChecksum += c;
    
                // Update isDrop, isNAK, isCorrupt
                isDrop = packetSequenceNo == 2 && packetMessage.equals("ACK");
                isNAK = packetMessage.equals("NAK");
                isCorrupt = actualChecksum != packetChecksum;
            } while(isDrop || isNAK || isCorrupt);
        }

        networkSocket.close();
    }
}