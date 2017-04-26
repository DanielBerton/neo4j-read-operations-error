import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.kernel.api.exceptions.KernelException;
import org.neo4j.kernel.impl.proc.Procedures;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created by Daniele Berton on 26/04/2017.
 */

public class ReadOperationTest {

    public static final String MONTH = "2015-12";
    public static final String DATE = MONTH + "-01";
    public static final String TYPE = "CHECKIN";

    public static final String NAME = "name";

    public static final String PERSON = "Person";
    public static final String HIPSTER = "Hipster";
    public static final String JOE = "Joe";
    public static final String AGE = "age";
    public static final String JOE_PATTERN = "(joe:" + PERSON + ":" + HIPSTER + " {" + NAME + ":'" + JOE + "'," + AGE + ":42})";


    public static final String PLACE = "Place";
    public static final String PHILZ = "Philz";
    public static final String PHILZ_PATTERN = "(philz:" + PLACE + " {" + NAME + ":'" + PHILZ + "'})";

    public static final String CHECKIN_PATTERN = JOE_PATTERN + "-[checkin:" + TYPE + " {on:'" + DATE + "'}]->" + PHILZ_PATTERN;
    private GraphDatabaseService db;

    @Before
    public void setUp() throws Exception {
        db = new TestGraphDatabaseFactory().newImpermanentDatabase();
        registerProcedure(db, ReadOperationProcedure.class);
    }

    @After
    public void tearDown() {
        db.shutdown();
    }

    @Test
    public void testReadOnly() throws Exception {
        createData();
        testCall(db,"MATCH "+JOE_PATTERN+" WITH joe CALL out(joe, 'CHECKIN','on:2015-*') YIELD node RETURN *",
                (row) -> {
                    Node node = (Node) row.get("node");
                    assertEquals(PHILZ, node.getProperty("name"));
                    assertEquals(true, node.hasLabel(Label.label(PLACE)));
                });
    }

    public static void testCall(GraphDatabaseService db, String call, Consumer<Map<String, Object>> consumer) {
        testCall(db,call,null,consumer);
    }

    public static void testCall(GraphDatabaseService db, String call,Map<String,Object> params, Consumer<Map<String, Object>> consumer) {
        testResult(db, call, params, (res) -> {
            try {
                if (res.hasNext()) {
                    Map<String, Object> row = res.next();
                    consumer.accept(row);
                }
                assertFalse(res.hasNext());
            } catch(Throwable t) {
                t.printStackTrace();
                throw t;
            }
        });
    }

    public static void testResult(GraphDatabaseService db, String call, Map<String,Object> params, Consumer<Result> resultConsumer) {
        try (Transaction tx = db.beginTx()) {
            Map<String, Object> p = (params == null) ? Collections.<String, Object>emptyMap() : params;
            resultConsumer.accept(db.execute(call, p));
            tx.success();
        }
    }

    private  static void registerProcedure(GraphDatabaseService db, Class<?>...procedures) throws KernelException {
        Procedures proceduresService = ((GraphDatabaseAPI) db).getDependencyResolver().resolveDependency(Procedures.class);
        for (Class<?> procedure : procedures) {
            proceduresService.registerProcedure(procedure);
            proceduresService.registerFunction(procedure);
        }
    }

    private void createData() {
        testCall(db, "CREATE "+CHECKIN_PATTERN+" RETURN *",(row)->{
            Node joe = (Node) row.get("joe");
            db.index().forNodes(PERSON).add(joe,"name",joe.getProperty("name"));
            Node philz = (Node) row.get("philz");
            db.index().forNodes(PLACE).add(philz,"name",philz.getProperty("name"));
            db.index().forRelationships(TYPE).add((Relationship) row.get("checkin"),"on",DATE);
        });
    }
}
