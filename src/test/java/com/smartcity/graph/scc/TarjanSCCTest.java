package com.smartcity.graph.scc;

import com.smartcity.common.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests for Strongly Connected Components algorithm.
 */
public class TarjanSCCTest {

    private Metrics metrics;

    @BeforeEach
    void setUp() {
        metrics = new MetricsImpl();
    }

    @Test
    void testSingleVertexSCC() {
        Graph graph = new Graph(1, true);
        TarjanSCC tarjan = new TarjanSCC(graph, metrics);

        SCCResult result = tarjan.findSCC();

        assertEquals(1, result.getNumComponents());
        assertEquals(1, result.getComponentSize(0));
        assertEquals(0, result.getComponentId(0));
    }

    @Test
    void testSimpleCycle() {
        // Create cycle: 0 -> 1 -> 2 -> 0
        Graph graph = new Graph(3, true);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 0);

        TarjanSCC tarjan = new TarjanSCC(graph, metrics);
        SCCResult result = tarjan.findSCC();

        assertEquals(1, result.getNumComponents());
        assertEquals(3, result.getComponentSize(0));

        // All vertices should be in same component
        assertEquals(result.getComponentId(0), result.getComponentId(1));
        assertEquals(result.getComponentId(1), result.getComponentId(2));

        assertTrue(result.inSameComponent(0, 1));
        assertTrue(result.inSameComponent(1, 2));
        assertTrue(result.inSameComponent(0, 2));
    }

    @Test
    void testDAGNoSCC() {
        // Create DAG: 0 -> 1 -> 2
        Graph graph = new Graph(3, true);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);

        TarjanSCC tarjan = new TarjanSCC(graph, metrics);
        SCCResult result = tarjan.findSCC();

        assertEquals(3, result.getNumComponents());

        // Each vertex should be in its own component
        assertNotEquals(result.getComponentId(0), result.getComponentId(1));
        assertNotEquals(result.getComponentId(1), result.getComponentId(2));

        assertFalse(result.inSameComponent(0, 1));
        assertFalse(result.inSameComponent(1, 2));
    }

    @Test
    void testMixedSCCAndDAG() {
        // Create: SCC {0,1,2} connected to DAG {3 -> 4}
        Graph graph = new Graph(5, true);
        // SCC part
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 0);
        // Connection to DAG
        graph.addEdge(2, 3);
        // DAG part
        graph.addEdge(3, 4);

        TarjanSCC tarjan = new TarjanSCC(graph, metrics);
        SCCResult result = tarjan.findSCC();

        assertEquals(3, result.getNumComponents());

        // Vertices 0,1,2 should be in same SCC
        assertTrue(result.inSameComponent(0, 1));
        assertTrue(result.inSameComponent(1, 2));

        // Vertices 3,4 should be in different components
        assertFalse(result.inSameComponent(3, 4));

        // SCC should not include DAG vertices
        assertFalse(result.inSameComponent(0, 3));
        assertFalse(result.inSameComponent(2, 4));
    }

    @Test
    void testCondensation() {
        // Create graph with multiple SCCs
        Graph graph = new Graph(6, true);
        // First SCC: {0,1}
        graph.addEdge(0, 1);
        graph.addEdge(1, 0);
        // Second SCC: {2,3,4}
        graph.addEdge(2, 3);
        graph.addEdge(3, 4);
        graph.addEdge(4, 2);
        // Isolated vertex: {5}
        // Connections between SCCs
        graph.addEdge(1, 2);
        graph.addEdge(4, 5);

        TarjanSCC tarjan = new TarjanSCC(graph, metrics);
        SCCResult result = tarjan.findSCC();

        assertEquals(3, result.getNumComponents());

        // Test condensation
        SCCCondensation condensation = new SCCCondensation(graph, result);
        Graph dag = condensation.buildCondensationDAG();

        assertEquals(3, dag.getNumVertices());
        assertEquals(2, dag.getNumEdges()); // Two edges between components

        // Verify it's a DAG (no self-loops in condensation)
        for (int i = 0; i < dag.getNumVertices(); i++) {
            for (Graph.Edge edge : dag.getEdges(i)) {
                assertNotEquals(i, edge.to, "Condensation should not have self-loops");
            }
        }
    }

    @Test
    void testMetricsTracking() {
        Graph graph = new Graph(4, true);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 0);
        graph.addEdge(3, 1);

        TarjanSCC tarjan = new TarjanSCC(graph, metrics);
        tarjan.findSCC();

        // Verify metrics were tracked
        assertTrue(metrics.getCounter("dfs_visits") > 0);
        assertTrue(metrics.getCounter("edge_traversals") > 0);
        assertTrue(metrics.getCounter("scc_found") > 0);
        assertTrue(metrics.getTime("tarjan_scc_total") > 0);
    }

    @Test
    void testUndirectedGraphThrows() {
        Graph undirectedGraph = new Graph(3, false);

        assertThrows(IllegalArgumentException.class, () -> {
            new TarjanSCC(undirectedGraph, metrics);
        });
    }
}
