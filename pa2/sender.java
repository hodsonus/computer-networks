import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.net.InetAddress;

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
        
        int portNumber;
        List<String> messages;

        try {
            // Try to get the IP address of the provided URL.
            // Throws UnknownHostException (subclass of IOException) if it's not a valid hostname or IP address.
            InetAddress.getByName(URL);

            // Throws NumberFormatException if it's not a valid integer.
            portNumber = Integer.parseInt(stringPortNumber);
            
            /* This convoluted line of code retrieves the Path from the
             * MessageFileName, reads all of the bytes from the corresponding
             * file, converts the byte array to a string, splits it on
             * whitespace, and converts the String[] to List<String>. */
            messages = Arrays.asList((new String(Files.readAllBytes(Paths.get(MessageFileName))).split("\\s+")));
        }
        catch (NumberFormatException | IOException | InvalidPathException e) {
            // Expected exceptions, user provided bad input
            e.printStackTrace();
            System.out.println(usage);
            return;
        }

         if (portNumber < 0 || portNumber > 65535) {
            System.out.println(usage);
            return;
         }

         String lastWord = messages.get(messages.size() - 1);
         if (lastWord.charAt(lastWord.length()-1) != '.') {
            System.out.println(usage);
            return;
         }

         startSender(URL, portNumber, messages);
    }

    // TODO - sender implementation
    private static void startSender(String URL, int portNumber, List<String> messages) {
        for (String message : messages) {
            System.out.println(message);
        }
    }
}