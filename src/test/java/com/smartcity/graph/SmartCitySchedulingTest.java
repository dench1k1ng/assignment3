package com.smartcity.graph;

import com.smartcity.common.*;
import com.smartcity.graph.scc.*;
import com.smartcity.graph.topo.*;
import com.smartcity.graph.dagsp.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class SmartCitySchedulingTest {

    private Metrics metrics;

    @BeforeEach
    void setUp() {
        metrics = new MetricsImpl();
    }

    @Test
    void testCompleteWorkflowWithSCCs() {

        Graph graph = new Graph(7, true);

        graph.addEdge(0, 1, 2);
        graph.addEdge(1, 2, 3);
        graph.addEdge(2, 0, 1);

        graph.addEdge(3, 4, 2);
        graph.addEdge(4, 3, 1);

        graph.addEdge(2, 3, 4);
        graph.addEdge(4, 5, 3);
        graph.addEdge(5, 6, 2);

        TarjanSCC tarjan = new TarjanSCC(graph, metrics);
        SCCResult sccResult = tarjan.findSCC();

        assertEquals(4, sccResult.getNumComponents());

        SCCCondensation condensation = new SCCCondensation(graph, sccResult);
        Graph condensationDAG = condensation.buildCondensationDAG();

        assertEquals(4, condensationDAG.getNumVertices());
        assertTrue(condensationDAG.getNumEdges() >= 3);

        SCCTopologicalOrder sccTopo = new SCCTopologicalOrder(graph, metrics);
        assertTrue(sccTopo.computeOrder());

        List<Integer> componentOrder = sccTopo.getComponentOrder();
        assertNotNull(componentOrder);
        assertEquals(4, componentOrder.size());

        if (condensationDAG.getNumEdges() > 0) {
            DAGShortestPath shortestPath = new DAGShortestPath(condensationDAG, metrics);

            int sourceComponent = findSourceComponent(condensationDAG);
            if (sourceComponent != -1) {
                PathResult shortestResult = shortestPath.findShortestPaths(sourceComponent);
                assertNotNull(shortestResult);
                assertEquals(sourceComponent, shortestResult.getSource());
            }
        }

        if (condensationDAG.getNumEdges() > 0) {
            DAGLongestPath longestPath = new DAGLongestPath(condensationDAG, metrics);
            PathResult criticalResult = longestPath.findCriticalPath();

            assertNotNull(criticalResult);
            assertTrue(criticalResult.isLongestPath());
        }
    }

    @Test
    void testPureDAGWorkflow() {

        Graph dag = new Graph(6, true);
        dag.addEdge(0, 1, 5);
        dag.addEdge(0, 2, 3);
        dag.addEdge(1, 3, 2);
        dag.addEdge(2, 3, 4);
        dag.addEdge(1, 4, 6);
        dag.addEdge(3, 5, 1);
        dag.addEdge(4, 5, 2);

        TarjanSCC tarjan = new TarjanSCC(dag, metrics);
        SCCResult sccResult = tarjan.findSCC();

        assertEquals(6, sccResult.getNumComponents());

        KahnTopologicalSort kahnSort = new KahnTopologicalSort();
        List<Integer> topoOrder = kahnSort.topologicalSort(dag, metrics);

        assertNotNull(topoOrder);
        assertEquals(6, topoOrder.size());

        DAGShortestPath shortestPath = new DAGShortestPath(dag, metrics);
        PathResult shortestResult = shortestPath.findShortestPaths(0);

        assertNotNull(shortestResult);
        assertEquals(0, shortestResult.getSource());

        assertTrue(shortestResult.isReachable(5));
        List<Integer> pathTo5 = shortestResult.getPath(5);
        assertNotNull(pathTo5);
        assertEquals(0, pathTo5.get(0).intValue());
        assertEquals(5, pathTo5.get(pathTo5.size() - 1).intValue());

        DAGLongestPath longestPath = new DAGLongestPath(dag, metrics);
        PathResult criticalResult = longestPath.findCriticalPath();

        assertNotNull(criticalResult);
        assertTrue(criticalResult.isLongestPath());

        List<Integer> criticalPath = criticalResult.getCriticalPath();
        assertNotNull(criticalPath);
        assertTrue(criticalResult.getCriticalPathLength() > 0);
    }

    @Test
    void testDisconnectedGraphHandling() {

        Graph graph = new Graph(6, true);

        graph.addEdge(0, 1, 3);

        graph.addEdge(2, 3, 2);
        graph.addEdge(3, 4, 1);
        graph.addEdge(4, 2, 4);

        TarjanSCC tarjan = new TarjanSCC(graph, metrics);
        SCCResult sccResult = tarjan.findSCC();

        assertEquals(4, sccResult.getNumComponents());

        boolean foundLargeSCC = false;
        for (int i = 0; i < sccResult.getNumComponents(); i++) {
            if (sccResult.getComponentSize(i) == 3) {
                foundLargeSCC = true;
                break;
            }
        }
        assertTrue(foundLargeSCC);
    }

    @Test
    void testPerformanceWithLargerGraph() {

        int n = 20;
        Graph graph = new Graph(n, true);

        for (int i = 0; i < n - 1; i++) {
            graph.addEdge(i, i + 1, i + 1);
            if (i % 3 == 0 && i > 0) {
                graph.addEdge(i, i - 1, 1);
            }
        }

        long startTime = System.nanoTime();

        TarjanSCC tarjan = new TarjanSCC(graph, metrics);
        SCCResult sccResult = tarjan.findSCC();

        SCCCondensation condensation = new SCCCondensation(graph, sccResult);
        Graph condensationDAG = condensation.buildCondensationDAG();

        if (condensationDAG.getNumEdges() > 0) {
            DAGLongestPath longestPath = new DAGLongestPath(condensationDAG, metrics);
            longestPath.findCriticalPath();
        }

        long endTime = System.nanoTime();
        double durationMs = (endTime - startTime) / 1_000_000.0;

        assertTrue(durationMs < 100, "Performance test should complete in under 100ms");

        assertTrue(sccResult.getNumComponents() > 0);
        assertTrue(sccResult.getNumComponents() <= n);
    }

    private int findSourceComponent(Graph dag) {
        boolean[] hasIncomingEdge = new boolean[dag.getNumVertices()];

        for (int u = 0; u < dag.getNumVertices(); u++) {
            for (Graph.Edge edge : dag.getEdges(u)) {
                hasIncomingEdge[edge.to] = true;
            }
        }

        for (int i = 0; i < dag.getNumVertices(); i++) {
            if (!hasIncomingEdge[i]) {
                return i;
            }
        }

        return -1;
    }
}
