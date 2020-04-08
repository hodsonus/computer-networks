public class DistanceTableEntry {

    public int distance;
    public Node parent;

    public DistanceTableEntry(int distance, Node parent) {
        this.distance = distance;
        this.parent = parent;
    }
}