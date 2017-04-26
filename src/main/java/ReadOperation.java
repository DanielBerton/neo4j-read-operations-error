import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.api.LegacyIndexHits;
import org.neo4j.kernel.api.ReadOperations;
import org.neo4j.kernel.api.exceptions.legacyindex.LegacyIndexNotFoundKernelException;
import org.neo4j.kernel.impl.core.ThreadToStatementContextBridge;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Procedure;

import java.util.Map;

/**
 * Created by Daniele Berton on 26/04/2017.
 */

public class ReadOperation {

    public static LegacyIndexHits relationshipQueryIndex(String indexName, Object query, GraphDatabaseService db, Long startNode, Long endNode) throws Exception {
        long startingNode = (startNode == null) ? -1 : startNode;
        long endingNode = (endNode == null) ? -1 : endNode; //FIXME (works only if endNode != -1)
        return getReadOperation(db).relationshipLegacyIndexQuery(indexName, query, startingNode, endingNode);
    }

    private static ReadOperations getReadOperation(GraphDatabaseService db){
        return  ((GraphDatabaseAPI)db)
                .getDependencyResolver()
                .resolveDependency(ThreadToStatementContextBridge.class).get()
                .readOperations();
    }
}
