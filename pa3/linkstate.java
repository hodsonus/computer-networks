public class linkstate {

    private static String usage = "java linkstate <network.txt>";

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println(usage);
            return;
        }
        String filename = args[0];

        /* Construct the graph from the filename, all logic is contained within
         * the Graph class. */
        Graph network = new Graph(filename);

        /* For this assignment, we choose to always construct the routing table
         * for the node with ID 1. */
        Graph.djikstraSPF(network, 1);
    }
}