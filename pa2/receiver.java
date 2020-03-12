import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class receiver {

    private static String usage = "Usage - java receiver [URL] " + "[portNumber]. URL is a valid URL and "
            + "port number is a valid integer between 0 " + "and 65535 (inclusive).";

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println(usage);
            return;
        }

        String URL = args[0];
        String stringPortNumber = args[1];

        Socket networkSocket;

        try {
            // Throws NumberFormatException if it's not a valid integer.
            int portNumber = Integer.parseInt(stringPortNumber);

            // Try to instantiate a socket with the user provided URL and the portNumber.
            // Throws UnknownHostException (subclass of IOException) if it's not a valid
            // hostname or IP address.
            networkSocket = new Socket(URL, portNumber);
        } catch (NumberFormatException | IOException e) {
            e.printStackTrace();
            System.out.println(usage);
            return;
        }

        try {
            receiverWorker(networkSocket);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static void receiverWorker(Socket networkSocket) throws IOException {
        BufferedReader fromNetwork = new BufferedReader(new InputStreamReader(networkSocket.getInputStream()));
        DataOutputStream toNetwork = new DataOutputStream(networkSocket.getOutputStream());

        int expectedSequenceNo = 0;
        List<String> reconstructedMessage = new ArrayList<String>();
        int packetsReceived = 0;

        while (true) {
            String packet = fromNetwork.readLine();
            List<String> decomposedPacket = new ArrayList<String>(Arrays.asList(packet.split(" ")));

            if (decomposedPacket.get( decomposedPacket.size()-1 ).equals("-1")) {
                break;
            }

            int packetSequenceNo = Integer.parseInt(decomposedPacket.get(0));
            int packetChecksum = Integer.parseInt(decomposedPacket.get(2));
            String packetMessage = decomposedPacket.get(3);

            // Generate the actual checksum from the packetMessage to detect corruption 
            int actualChecksum = 0;
            for (char c : packetMessage.toCharArray()) actualChecksum += c;

            int newPacketSequenceNo;
            boolean updateExpectedSequenceNo = false;
            boolean resetReconstructedMessage = false;

            if (actualChecksum == packetChecksum) {
                // If the packet was not corrupt

                if (expectedSequenceNo == packetSequenceNo) {
                    // If it was the expected sequence number

                    // Deliver data
                    reconstructedMessage.add(packetMessage);

                    // Update flags for updates after status print
                    resetReconstructedMessage = packetMessage.endsWith(".");
                    updateExpectedSequenceNo = true;

                    // Send back the expected sequence number
                    newPacketSequenceNo = expectedSequenceNo;
                }
                else {
                    // If it was not the expected sequenceNo, then send back the packet's sequence number.
                    newPacketSequenceNo = packetSequenceNo;
                }
                
                // If it was an unexpected sequence number then we will still send
                // an ACK but not update the expectedSequenceNo or deliver the data.
            }
            else {
                // Else the packet was corrupt and the newMessage should be an ACK with the unexpected sequence number
                if (packetSequenceNo == 0) newPacketSequenceNo = 1;
                else newPacketSequenceNo = 0;
            }

            System.out.println(String.format("Waiting %d, %d, %s, ACK%d", expectedSequenceNo, ++packetsReceived, packet, newPacketSequenceNo));

            if (updateExpectedSequenceNo) {
                // Flip the expectedSequenceNo
                if (expectedSequenceNo == 0) expectedSequenceNo = 1;
                else expectedSequenceNo = 0;
            }

            if (resetReconstructedMessage) {
                /* If it was the last message in this sequence, then
                * print to the console and reset the
                * reconstructedMessage list. */
                System.out.println(String.join(" ", reconstructedMessage));
                reconstructedMessage = new ArrayList<String>();
            }

            // The same packetSequenceNo will be echoed, the checksum for an ACK should be 0, along with the newMessage (either ACK or NAK)
            String ackPacket = String.format("%d 0 ACK", newPacketSequenceNo);
            toNetwork.writeBytes(ackPacket + "\n");
        }

        networkSocket.close();
    }
}