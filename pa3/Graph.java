import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Graph {
    private Map<Integer, Node> nodes;
    private static final int INFINITY = Integer.MAX_VALUE;

    public Graph(String fileName) {
        this.nodes = Graph.constructGraph(fileName);
    }

    private static Map<Integer, Node> constructGraph(String fileName) {
        Map<Integer, Node> ret = new HashMap<Integer, Node>();

        // Split the file into a list of lines
        List<String> lines;
        try {
            /* This convoluted line of code retrieves the Path from the
             * fileName, reads all of the bytes from the corresponding
             * file, converts the byte array to a string, splits it on
             * whitespace, and converts the String[] to List<String>. */
            lines = new ArrayList<String>(
                    Arrays.asList((new String(Files.readAllBytes(Paths.get(fileName))).split("\n"))));
        }
        catch (IOException e) {
            throw new RuntimeException("Bad file path.");
        }

        /* Iterate over the lines, creating an edge from each line. Additionally, create
         * a node if it is not present yet. */
        for (String line : lines) {
            
            // Split the line on whitespace.
            String[] decomposedLine = line.split("\\s+");
            if (decomposedLine.length != 3) {
                throw new RuntimeException(String.format("Bad line format, offending line=\"%s\"", line));
            }

            // Parse out the node IDs and weight of the edge.
            int srcID, destID, weight;
            try {
                srcID = Integer.parseInt(decomposedLine[0]);
                destID = Integer.parseInt(decomposedLine[1]);
                weight = Integer.parseInt(decomposedLine[2]);
            }
            catch (NumberFormatException nfe) {
                throw new RuntimeException(String.format("Bad line format, offending line=\"%s\"", line));
            }

            // If the node being referred to does not exist in our map yet, create it.
            if (!ret.containsKey(srcID)) ret.put(srcID, new Node(srcID));
            if (!ret.containsKey(destID)) ret.put(destID, new Node(destID));

            // Get the nodes associated with the src ID and the dest ID.
            Node srcNode = ret.get(srcID);
            Node dstNode = ret.get(destID);

            /* Add the respective edges to the nodes. Duplicate edges should resolve
             * themselves bc of the overriden hashCode() method, equals() method,
             * and the fact that edges are stored as a set, NOT a list. */
            srcNode.addOutgoingEdge(dstNode, weight);
            dstNode.addOutgoingEdge(srcNode, weight);
        }

        return ret;
    }

    public static Map<Node, DistanceTableEntry> djikstraSPF(Graph graph, int sourceID) {
        // Printing intermediate results is enabled by default.
        return djikstraSPF(graph, sourceID, true);
    }

    private static Map<Node, DistanceTableEntry> djikstraSPF(Graph graph, int sourceID, boolean print) {
        /* Mark all nodes unvisited. Create a set of all the unvisited nodes
         * called the unvisited set. */
        Set<Node> unvisited = new HashSet<Node>(graph.nodes.values());

        // Verify that the source node exists in the graph.
        if (!graph.nodes.containsKey(sourceID)) {
            throw new RuntimeException(String.format("No node exists with ID %d, cannot be used as source.", sourceID));
        }

        // Set the initial node to be the node with ID sourceID.
        Node initial = graph.nodes.get(sourceID);

        // Print the header if printing is enabled.
        if (print) Graph.printDistanceTableHeader(graph.nodes.keySet());
        
        /* Assign to every node a tentative distance value: set it to zero for
         * our initial node and to infinity for all other nodes. */
        Map<Node, DistanceTableEntry> distances = new HashMap<Node, DistanceTableEntry>();
        DistanceTableEntry initDist;
        for (Node node : graph.nodes.values()) {
            if (node.equals(initial)) initDist = new DistanceTableEntry(0, initial);
            else initDist = new DistanceTableEntry(INFINITY, null);

            distances.put(node, initDist);
        }

        // Set the initial node as current.
        Node current = initial;
        
        // Set step.
        int step = 0;
        
        while (current != null) {

            // If printing is enabled, print the current (unfinished) distance table.
            if (print) Graph.printDistanceTableRow(step, distances);

            /* For the current node, consider all of its unvisited neighbours and
            * calculate their tentative distances through the current node.
            * Compare the newly calculated tentative distance to the current
            * assigned value and assign the smaller one. */
            for (Edge outgoingEdge : current.getEdges()) {
                Node neighbor = outgoingEdge.getDest();
                int weight = outgoingEdge.getWeight();
                
                if (!unvisited.contains(neighbor)) continue;

                if (distances.get(current).distance + weight < distances.get(neighbor).distance) {
                    distances.put(neighbor, new DistanceTableEntry(distances.get(current).distance + weight, current));
                }
            }
            
            /* When we are done considering all of the unvisited neighbours of the
            * current node, mark the current node as visited and remove it from
            * the unvisited set. A visited node will never be checked again. */
            unvisited.remove(current);

            // Mark that we took a step.
            step++;
            
            /* If the smallest tentative distance among the nodes in the
             * unvisited set is infinity (occurs when there is no connection
             * between the initial node and remaining unvisited nodes), then
             * stop. The algorithm has finished. If there exists an unvisited
             * node with a finite distance, then select the unvisited node that
             *  is marked with the smallest tentative distance, set it as the
             * new "current node", and loop again. */
            current = null;
            for (Node node : unvisited) {
                if (distances.get(node).distance == INFINITY) continue;
                if (current == null) current = node;
                if (distances.get(node).distance < distances.get(current).distance) current = node;
            }
        }

        // If printing is enabled, print the final table.
        if (print) Graph.printDistanceTableRow(step, distances);

        return distances;
    }

    private static void printDistanceTableRow(int step, Map<Node, DistanceTableEntry> distances) {
        // Get the nodes representing the keyset as a list
        List<Node> nodes = new ArrayList<Node>(distances.keySet());
        
        /* Sort the nodes list according to the compareTo method present in
         * the Node class, necessary so that this printout matches the header. */
        nodes.sort(null);

        // Print the current step number
        System.out.print(step);

        // Iterate over the list, printing each entry
        for (Node node : nodes) {
            DistanceTableEntry distanceTableEntry = distances.get(node);

            String dist = distanceTableEntry.distance == INFINITY ? "i" : String.valueOf(distanceTableEntry.distance);
            String parent = distanceTableEntry.parent == null ? "?" : String.valueOf(distanceTableEntry.parent.getID());

            System.out.print(String.format(",%s,%s", dist, parent));
        }

        System.out.println();
    }

    /* An IDSet is required here since we cannot guarantee that the IDs of the
     * nodes are labeled as 1->N for N nodes. */
    private static void printDistanceTableHeader(Set<Integer> IDSet) {
        // Construct a list from the set of IDs so that we may sort it.
        List<Integer> IDList = new ArrayList<Integer>(IDSet);
        // Passing null sorts with the default comparator
        IDList.sort(null);

        System.out.print("Step");
        
        // Iterate over the list, printing each element.
        for (int i = 0; i < IDList.size(); ++i) {
            System.out.print(String.format(",D%d,P%d", IDList.get(i), IDList.get(i)));
        }

        System.out.println();
    }
}