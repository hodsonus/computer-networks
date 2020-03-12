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

            // Check to see if the last message in the list of messages ends with a period (as it should).
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
            // Start the sender worker
            senderWorker(networkSocket, messages);
        }
        catch (IOException ioe) {
            // Handle unexpected exceptions.
            ioe.printStackTrace();
        }
    }

    private static void senderWorker(Socket networkSocket, List<String> messages) throws IOException {

        // Add the terminating message to our messages list
        // Do this before the packets are created such that
        // a valid packet is created for this message.
        messages.add("-1");

        // Create our input and output buffers
        BufferedReader fromNetwork = new BufferedReader(new InputStreamReader(networkSocket.getInputStream()));
        DataOutputStream toNetwork = new DataOutputStream(networkSocket.getOutputStream());

        // Generate the list of packets from the list of messages
        List<String> packets = new ArrayList<String>();
        for (int id = 0; id < messages.size(); ++id) {
            // Get the message from the messages list
            String message = messages.get(id);
            // Even indices have 0 as the sequence number while odd have 1
            int sequenceNo = id % 2;
            // Calculate the checksum (the)
            int checksum = 0;
            for (char c : message.toCharArray()) checksum += c;
            String packet = String.format("%d %d %d %s", sequenceNo, id, checksum, message);

            packets.add(packet);
        }

        int packetsSent = 0;

        // Iterate over the list of packets, sending them one-by-one
        for (int id = 0; id < packets.size(); ++id) {
            
            String packet = packets.get(id);
            List<String> deconstructedPacket = Arrays.asList(packet.split(" "));
            int expectedSequenceNo = Integer.parseInt(deconstructedPacket.get(0));
            String actionTaken = String.format("send Packet%d", expectedSequenceNo);

            boolean isDrop, isCorrupt, isUnexpectedSequenceNo;
            do {
                // Send the packet over the network
                toNetwork.writeBytes(packet + "\n");

                if (packet.endsWith("-1")) {
                    // If the packet was the terminating packet then close the socket and exit.
                    networkSocket.close();
                    return;
                }

                // Get the response from the network and decompose it
                String response = fromNetwork.readLine();
                List<String> decomposedResponse = new ArrayList<String>(Arrays.asList(response.split(" ")));
    
                // Grab the attributes off of the decomposed response
                int packetSequenceNo = Integer.parseInt(decomposedResponse.get(0));
                int packetChecksum = Integer.parseInt(decomposedResponse.get(1));
                String packetMessage = decomposedResponse.get(2);

                // Calculate the actual checksum to check for corruption
    
                // Update isDrop, isCorrupt, isExpectedSequenceNo
                isDrop = packetSequenceNo == 2 && packetMessage.equals("ACK");
                isCorrupt = packetChecksum != 0;
                isUnexpectedSequenceNo = packetSequenceNo != expectedSequenceNo;

                String receiverResponse;
                if (isDrop) {
                    receiverResponse = "DROP";
                }
                else {
                    receiverResponse = String.format("ACK%d", packetSequenceNo);
                }

                System.out.println(String.format("Waiting ACK%d, %d, %s, %s", expectedSequenceNo, ++packetsSent, receiverResponse, actionTaken));

                if (isDrop || isCorrupt || isUnexpectedSequenceNo) {

                    if (id == packets.size()-2) {
                        actionTaken = "no more packets to send";
                    }
                    else {
                        actionTaken = String.format("resend Packet%d", expectedSequenceNo);
                    }
                }

                // Repeat as long as we had a dropped, corrupt, or unexpected sequence number.
            } while(isDrop || isCorrupt || isUnexpectedSequenceNo);
        }
    }
}