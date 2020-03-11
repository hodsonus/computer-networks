public class network {

    private static String usage = "Usage - java network [portNumber]. Port " +
                                  "number is a valid integer between 0 and " +
                                  "65535 (inclusive).";

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println(usage);
            return;
        }

        int portNumber;
        String stringPortNumber = args[0];

        try {
            // Throws NumberFormatException if it's not a valid integer.
            portNumber = Integer.parseInt(stringPortNumber);
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
            System.out.println(usage);
            return;
        }

         if (portNumber < 0 || portNumber > 65535) {
            System.out.println(usage);
            return;
         }

         startNetwork(portNumber);
    }

    // TODO - network implementation
    private static void startNetwork(int portNumber) {

    }
}