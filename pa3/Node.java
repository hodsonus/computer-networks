import java.util.HashSet;
import java.util.Set;

public class Node implements Comparable<Node> {
    private Set<Edge> edges;
    private int ID;

    public Node(int ID) {
        this.ID = ID;
        this.edges = new HashSet<Edge>();
    }

    public int getID() {
        return this.ID;
    }

    public boolean addOutgoingEdge(Node dest, int weight) {
        if (dest == null) return false;
        this.edges.add(new Edge(this, dest, weight));
        return true;
    }

    public Set<Edge> getEdges() {
        return this.edges;
    }

    @Override
    public int hashCode() {
        return this.ID;
    }

    @Override
    public boolean equals(Object o)
    {
        // Succeed if it is the same instance and fail if it isn't an instance of Node.
        if (o == this) return true;
        if ( !(o instanceof Node) ) return false;

        // Cast back to Edge to compare attributes.
        Node other = (Node)(o);

        // They're the same node if they share IDs.
        return this.ID == other.getID();
    }

    @Override
    public int compareTo(Node otherNode) {
        return Integer.compare(this.ID, otherNode.ID);
    }
}