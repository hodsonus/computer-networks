import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class network {

    private static String usage = "Usage - java network [portNumber]. Port "
            + "number is a valid integer between 0 and " + "65535 (inclusive).";

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println(usage);
            return;
        }

        String stringPortNumber = args[0];

        ServerSocket serverSocket;

        try {
            // Throws NumberFormatException if it's not a valid integer.
            int portNumber = Integer.parseInt(stringPortNumber);

            // Throws an IllegalArgumentException if the port number is not valid.
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
            System.out.println(usage);
            return;
        }

        try {
            networkWorker(serverSocket);
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static void networkWorker(ServerSocket serverSocket) throws IOException {
        // Setup connection with the receiver and then the sender
        Socket receiverSocket = serverSocket.accept();
        Socket senderSocket = serverSocket.accept();

        // Setup buffers for the sender and receiver
        BufferedReader fromReceiver = new BufferedReader(new InputStreamReader(receiverSocket.getInputStream()));
        DataOutputStream toReceiver = new DataOutputStream(receiverSocket.getOutputStream());
        BufferedReader fromSender = new BufferedReader(new InputStreamReader(senderSocket.getInputStream()));
        DataOutputStream toSender = new DataOutputStream(senderSocket.getOutputStream());

        List<String> receiverPacket, senderPacket;
        String packetForReceiver, packetForSender;
        Random generator = new Random();
        double prob;

        while (true) {
            // get message from the fromSender buffer
            senderPacket = new ArrayList<String>(Arrays.asList(fromSender.readLine().split(" ")));

            // If the message is -1, put it in the toReceiver buffer and terminate
            if (senderPacket.get( senderPacket.size()-1 ).equals("-1")) {
                packetForReceiver = String.join(" ", senderPacket);

                System.out.println(String.format("Received: %s, %s", packetForReceiver, "TERMINATE"));

                toReceiver.writeBytes(packetForReceiver + "\n");

                break;
            }

            String originalMessage = senderPacket.get(3);
            String ID = senderPacket.get(1);

            // Choose pass/drop/corrupt
            prob = generator.nextDouble();
            if (prob <= 0.25) {
                // Drop case
                senderPacket.set(0, "2"); //  Drop has sequence number 2
                // Leave the ID at position 1 unchanged
                senderPacket.set(2, "0"); //  ACK has checksum 0
                senderPacket.set(3, "ACK"); //  Drop has sequence number 2

                // DROP packets go to the sender
                packetForSender = String.join(" ", senderPacket);
                toSender.writeBytes(packetForSender + "\n");

                System.out.println(String.format("Received: %s, %s, %s", originalMessage, ID, "DROP"));

                continue;
            }
            else if (prob <= 0.5) {
                // Corrupt case
                int checksum = Integer.parseInt(senderPacket.get(2));
                ++checksum;
                senderPacket.set(2, Integer.toString(checksum));

                System.out.println(String.format("Received: %s, %s, %s", originalMessage, ID, "CORRUPT"));
            }
            else {
                // The pass case is for prob > 0.5, where we do nothing
                System.out.println(String.format("Received: %s, %s, %s", originalMessage, ID, "PASS"));
            }

            // Generate the packetForReceiver from the senderPacket (potentially modified in the above statement).
            packetForReceiver = String.join(" ", senderPacket);

            // put it in the toReceiver buffer
            toReceiver.writeBytes(packetForReceiver + "\n");



            // get message from the fromReceiver buffer
            receiverPacket = new ArrayList<String>(Arrays.asList(fromReceiver.readLine().split(" ")));

            originalMessage = receiverPacket.get(3);
            String sequenceNumber = receiverPacket.get(0);

            // choose pass/drop/corrupt
            prob = generator.nextDouble();
            if (prob <= 0.25) {
                // Drop case
                receiverPacket.set(0, "2"); //  Drop has sequence number 2
                // Leave the ID at position 1 unchanged
                receiverPacket.set(2, "0"); //  ACK has checksum 0
                receiverPacket.set(3, "ACK"); //  Drop has sequence number 2

                System.out.println(String.format("Received: %s%s, %s", originalMessage, sequenceNumber, "DROP"));
            }
            else if (prob <= 0.5) {
                // Corrupt case - we increment the checksum instead of flipping bits here
                int checksum = Integer.parseInt(receiverPacket.get(2));
                ++checksum;
                receiverPacket.set(2, Integer.toString(checksum));

                System.out.println(String.format("Received: %s%s, %s", originalMessage, sequenceNumber, "CORRUPT"));
            }
            else {
                // The pass case is for prob > 0.5, where we do nothing
                System.out.println(String.format("Received: %s%s, %s", originalMessage, sequenceNumber, "PASS"));
            }

            packetForSender = String.join(" ", receiverPacket);
            // put it in the toSender buffer
            toSender.writeBytes(packetForSender + "\n");
        }

        // Close the sockets
        senderSocket.close();
        receiverSocket.close();
        serverSocket.close();
    }
}