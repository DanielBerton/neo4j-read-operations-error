/**
 * Created by Daniele Berton on 26/04/2017.
 */

import org.neo4j.graphdb.Node;

public class WeightedNodeResult {
    public final Node node;
    public final double weight;

    public WeightedNodeResult(Node node, double weight) {
        this.weight = weight;
        this.node = node;
    }
}