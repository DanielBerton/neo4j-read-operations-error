import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.kernel.api.LegacyIndexHits;
import org.neo4j.procedure.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by Mattia on 26/04/2017.
 */

public class ReadOperationProcedure {

    @Context
    public GraphDatabaseService db;

    @Procedure(mode = Mode.READ)
    @Description("out(node,'TYPE','prop:value*') YIELD node - lucene query on relationship index with the given type name for *outgoing* relationship of the given node, *returns end-nodes*")
    public Stream<WeightedNodeResult> out(@Name("from") Node from, @Name("type") String type, @Name("query") String query) throws Exception {
        if (!db.index().existsForRelationships(type)) return Stream.empty();
        LegacyIndexHits legacyIndexHits = ReadOperation.relationshipQueryIndex(type, query, db, from.getId(), null);
        List<WeightedNodeResult> results = new ArrayList<>(legacyIndexHits.size());
        while (legacyIndexHits.hasNext()) {
            results.add(new WeightedNodeResult(db.getNodeById(legacyIndexHits.next()), legacyIndexHits.currentScore()));
        }

        return results.stream();
    }
}
