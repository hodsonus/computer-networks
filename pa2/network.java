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

        List<String> deconstructedReceiverPacket, deconstructedSenderPacket;
        String packetForReceiver, packetForSender;
        Random generator = new Random();
        double prob;

        while (true) {
            // get message from the fromSender buffer
            deconstructedSenderPacket = new ArrayList<String>(Arrays.asList(fromSender.readLine().split(" ")));

            if (deconstructedSenderPacket.get( deconstructedSenderPacket.size()-1 ).equals("-1")) {
                // If the message is -1, put it in the toReceiver buffer and terminate
                packetForReceiver = String.join(" ", deconstructedSenderPacket);
                toReceiver.writeBytes(packetForReceiver + "\n");

                System.out.println("Received: TERMINATE");
                break;
            }

            String packetSequenceNo = deconstructedSenderPacket.get(0);
            String packetID = deconstructedSenderPacket.get(1);

            // Choose pass/drop/corrupt
            prob = generator.nextDouble();
            if (prob <= 0.25) {
                // Drop case, DROP packets go to the sender
                packetForSender = "2 0 ACK";
                toSender.writeBytes(packetForSender + "\n");

                System.out.println(String.format("Received: Packet%s, %s, DROP", packetSequenceNo, packetID));
                continue;
            }
            else if (prob <= 0.5) {
                // Corrupt case
                int checksum = Integer.parseInt(deconstructedSenderPacket.get(2));
                ++checksum;
                deconstructedSenderPacket.set(2, Integer.toString(checksum));

                System.out.println(String.format("Received: Packet%s, %s, CORRUPT", packetSequenceNo, packetID));
            }
            else {
                // The pass case is for prob > 0.5, where we do nothing
                System.out.println(String.format("Received: Packet%s, %s, PASS", packetSequenceNo, packetID));
            }

            // Generate the packetForReceiver from the senderPacket (potentially modified in the above statement).
            packetForReceiver = String.join(" ", deconstructedSenderPacket);

            // put it in the toReceiver buffer
            toReceiver.writeBytes(packetForReceiver + "\n");



            // get message from the fromReceiver buffer
            deconstructedReceiverPacket = new ArrayList<String>(Arrays.asList(fromReceiver.readLine().split(" ")));

            packetSequenceNo = deconstructedReceiverPacket.get(0);
            String packetMessage = deconstructedReceiverPacket.get(2);

            // choose pass/drop/corrupt
            prob = generator.nextDouble();
            if (prob <= 0.25) {
                // Drop case
                deconstructedReceiverPacket.set(0, "2"); //  Drop has sequence number 2
                deconstructedReceiverPacket.set(1, "0"); //  ACK has checksum 0
                deconstructedReceiverPacket.set(2, "ACK"); //  Drop uses ACK2 as a message

                System.out.println(String.format("Received: %s%s, %s", packetMessage, packetSequenceNo, "DROP"));
            }
            else if (prob <= 0.5) {
                // Corrupt case - we increment the checksum instead of flipping bits here
                int checksum = Integer.parseInt(deconstructedReceiverPacket.get(1));
                ++checksum;
                deconstructedReceiverPacket.set(1, Integer.toString(checksum));

                System.out.println(String.format("Received: %s%s, %s", packetMessage, packetSequenceNo, "CORRUPT"));
            }
            else {
                // The pass case is for prob > 0.5, where we do nothing
                System.out.println(String.format("Received: %s%s, %s", packetMessage, packetSequenceNo, "PASS"));
            }

            packetForSender = String.join(" ", deconstructedReceiverPacket);
            // put it in the toSender buffer
            toSender.writeBytes(packetForSender + "\n");
        }

        // Close the sockets
        senderSocket.close();
        receiverSocket.close();
        serverSocket.close();
    }
}