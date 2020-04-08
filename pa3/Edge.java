
public class Edge {
    private Node src, dest;
    private int weight;

    public Edge(Node src, Node dest, int weight) {
        this.src = src;
        this.dest = dest;
        this.weight = weight;
    }

    public Node getSrc() {
        return this.src;
    }

    public Node getDest() {
        return this.dest;
    }

    public int getWeight() {
        return this.weight;
    }

    @Override
    public int hashCode() {
        /* Get the hash related for the source and the destination nodes to construct
         * this hash. */
        int srcHash = this.src.hashCode();
        int dstHash = this.dest.hashCode();

        /* Logic implemented here to ensure the hash is
         * the same for edges in the opposite direction. */
        int a, b, c;
        if (srcHash < dstHash) {
            a = srcHash;
            b = dstHash;
        }
        else {
            a = dstHash;
            b = srcHash;
        }
        c = this.weight;

        /* Cantor pairing function applied to three integers. */
        int hash = (a + b) * (a + b + 1) / 2 + a;
        hash = (hash + c) * (hash + c + 1) / 2 + c;

        return hash;
    }

    @Override
    public boolean equals(Object o)
    {
        // Succeed if it is the same instance and fail if it isn't an instance of Edge.
        if (o == this) return true;
        if ( !(o instanceof Edge) ) return false;

        // Cast back to Edge to compare attributes.
        Edge other = (Edge)(o);

        /* Check to see if the the sources and destinations match, OR if the destination
         *  matches the other's source and the source matches the other's destination. */
        boolean nodesMatch = ( other.getSrc().equals(this.src) && other.getDest().equals(this.dest) ) ||
                             ( other.getDest().equals(this.src) && other.getSrc().equals(this.dest) );
        boolean weightMatches = other.getWeight() == this.weight;
       
        /* They're the same edge if they share nodes AND weights, this behavior implements
         * a bidirectional weighted graph. */
        return nodesMatch && weightMatches;
    }
}