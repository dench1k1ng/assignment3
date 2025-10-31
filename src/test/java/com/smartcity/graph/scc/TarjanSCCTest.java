package com.smartcity.graph.scc;

import com.smartcity.common.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

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

        Graph graph = new Graph(3, true);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 0);

        TarjanSCC tarjan = new TarjanSCC(graph, metrics);
        SCCResult result = tarjan.findSCC();

        assertEquals(1, result.getNumComponents());
        assertEquals(3, result.getComponentSize(0));

        assertEquals(result.getComponentId(0), result.getComponentId(1));
        assertEquals(result.getComponentId(1), result.getComponentId(2));

        assertTrue(result.inSameComponent(0, 1));
        assertTrue(result.inSameComponent(1, 2));
        assertTrue(result.inSameComponent(0, 2));
    }

    @Test
    void testDAGNoSCC() {

        Graph graph = new Graph(3, true);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);

        TarjanSCC tarjan = new TarjanSCC(graph, metrics);
        SCCResult result = tarjan.findSCC();

        assertEquals(3, result.getNumComponents());

        assertNotEquals(result.getComponentId(0), result.getComponentId(1));
        assertNotEquals(result.getComponentId(1), result.getComponentId(2));

        assertFalse(result.inSameComponent(0, 1));
        assertFalse(result.inSameComponent(1, 2));
    }

    @Test
    void testMixedSCCAndDAG() {

        Graph graph = new Graph(5, true);

        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 0);

        graph.addEdge(2, 3);

        graph.addEdge(3, 4);

        TarjanSCC tarjan = new TarjanSCC(graph, metrics);
        SCCResult result = tarjan.findSCC();

        assertEquals(3, result.getNumComponents());

        assertTrue(result.inSameComponent(0, 1));
        assertTrue(result.inSameComponent(1, 2));

        assertFalse(result.inSameComponent(3, 4));

        assertFalse(result.inSameComponent(0, 3));
        assertFalse(result.inSameComponent(2, 4));
    }

    @Test
    void testCondensation() {

        Graph graph = new Graph(6, true);

        graph.addEdge(0, 1);
        graph.addEdge(1, 0);

        graph.addEdge(2, 3);
        graph.addEdge(3, 4);
        graph.addEdge(4, 2);

        graph.addEdge(1, 2);
        graph.addEdge(4, 5);

        TarjanSCC tarjan = new TarjanSCC(graph, metrics);
        SCCResult result = tarjan.findSCC();

        assertEquals(3, result.getNumComponents());

        SCCCondensation condensation = new SCCCondensation(graph, result);
        Graph dag = condensation.buildCondensationDAG();

        assertEquals(3, dag.getNumVertices());
        assertEquals(2, dag.getNumEdges());

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
