package com.smartcity.graph.topo;

import com.smartcity.common.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class KahnTopologicalSortTest {

    private Metrics metrics;
    private KahnTopologicalSort kahnSort;

    @BeforeEach
    void setUp() {
        metrics = new MetricsImpl();
        kahnSort = new KahnTopologicalSort();
    }

    @Test
    void testSimpleDAG() {

        Graph dag = new Graph(3, true);
        dag.addEdge(0, 1);
        dag.addEdge(1, 2);

        List<Integer> result = kahnSort.topologicalSort(dag, metrics);

        assertNotNull(result);
        assertEquals(3, result.size());

        assertEquals(0, result.get(0).intValue());

        assertTopoOrder(dag, result);
    }

    @Test
    void testComplexDAG() {

        Graph dag = new Graph(5, true);
        dag.addEdge(0, 1);
        dag.addEdge(0, 2);
        dag.addEdge(1, 3);
        dag.addEdge(2, 3);
        dag.addEdge(3, 4);

        List<Integer> result = kahnSort.topologicalSort(dag, metrics);

        assertNotNull(result);
        assertEquals(5, result.size());

        assertTopoOrder(dag, result);

        assertTrue(result.indexOf(0) < result.indexOf(1));
        assertTrue(result.indexOf(0) < result.indexOf(2));

        assertTrue(result.indexOf(1) < result.indexOf(3));
        assertTrue(result.indexOf(2) < result.indexOf(3));

        assertTrue(result.indexOf(3) < result.indexOf(4));
    }

    @Test
    void testSingleVertex() {
        Graph dag = new Graph(1, true);

        List<Integer> result = kahnSort.topologicalSort(dag, metrics);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(0, result.get(0).intValue());
    }

    @Test
    void testDisconnectedDAG() {

        Graph dag = new Graph(4, true);
        dag.addEdge(0, 1);
        dag.addEdge(2, 3);

        List<Integer> result = kahnSort.topologicalSort(dag, metrics);

        assertNotNull(result);
        assertEquals(4, result.size());

        assertTrue(result.indexOf(0) < result.indexOf(1));
        assertTrue(result.indexOf(2) < result.indexOf(3));
    }

    @Test
    void testCycleDetection() {

        Graph cyclic = new Graph(3, true);
        cyclic.addEdge(0, 1);
        cyclic.addEdge(1, 2);
        cyclic.addEdge(2, 0);

        List<Integer> result = kahnSort.topologicalSort(cyclic, metrics);

        assertNull(result, "Topological sort should return null for cyclic graphs");
        assertTrue(metrics.getCounter("cycle_detected") > 0);
    }

    @Test
    void testDAGCheck() {

        Graph dag = new Graph(3, true);
        dag.addEdge(0, 1);
        dag.addEdge(1, 2);

        assertTrue(kahnSort.isDAG(dag));

        dag.addEdge(2, 0);

        assertFalse(kahnSort.isDAG(dag));
    }

    @Test
    void testMetricsTracking() {
        Graph dag = new Graph(4, true);
        dag.addEdge(0, 1);
        dag.addEdge(1, 2);
        dag.addEdge(2, 3);

        kahnSort.topologicalSort(dag, metrics);

        assertTrue(metrics.getCounter("indegree_calculations") > 0);
        assertTrue(metrics.getCounter("queue_pushes") > 0);
        assertTrue(metrics.getCounter("queue_pops") > 0);
        assertTrue(metrics.getCounter("vertices_processed") > 0);
        assertTrue(metrics.getTime("kahn_topological_sort") > 0);
    }

    @Test
    void testUndirectedGraphThrows() {
        Graph undirected = new Graph(3, false);

        assertThrows(IllegalArgumentException.class, () -> {
            kahnSort.topologicalSort(undirected, metrics);
        });
    }

    private void assertTopoOrder(Graph graph, List<Integer> ordering) {
        for (int u = 0; u < graph.getNumVertices(); u++) {
            for (Graph.Edge edge : graph.getEdges(u)) {
                int v = edge.to;
                int uIndex = ordering.indexOf(u);
                int vIndex = ordering.indexOf(v);

                assertTrue(uIndex < vIndex,
                        String.format("Edge %d->%d violates topological order", u, v));
            }
        }
    }
}
