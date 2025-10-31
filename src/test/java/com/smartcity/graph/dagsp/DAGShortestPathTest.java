package com.smartcity.graph.dagsp;

import com.smartcity.common.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class DAGShortestPathTest {

    private Metrics metrics;

    @BeforeEach
    void setUp() {
        metrics = new MetricsImpl();
    }

    @Test
    void testSimpleShortestPath() {

        Graph dag = new Graph(4, true);
        dag.addEdge(0, 1, 5);
        dag.addEdge(0, 2, 3);
        dag.addEdge(1, 3, 2);
        dag.addEdge(2, 3, 4);

        DAGShortestPath shortestPath = new DAGShortestPath(dag, metrics);
        PathResult result = shortestPath.findShortestPaths(0);

        assertEquals(0.0, result.getDistance(0), 0.001);
        assertEquals(5.0, result.getDistance(1), 0.001);
        assertEquals(3.0, result.getDistance(2), 0.001);
        assertEquals(7.0, result.getDistance(3), 0.001);

        assertEquals(List.of(0), result.getPath(0));
        assertEquals(List.of(0, 1), result.getPath(1));
        assertEquals(List.of(0, 2), result.getPath(2));
        assertEquals(List.of(0, 1, 3), result.getPath(3));

        assertTrue(result.isReachable(0));
        assertTrue(result.isReachable(1));
        assertTrue(result.isReachable(2));
        assertTrue(result.isReachable(3));
    }

    @Test
    void testSimpleLongestPath() {

        Graph dag = new Graph(4, true);
        dag.addEdge(0, 1, 5);
        dag.addEdge(0, 2, 3);
        dag.addEdge(1, 3, 2);
        dag.addEdge(2, 3, 4);

        DAGLongestPath longestPath = new DAGLongestPath(dag, metrics);
        PathResult result = longestPath.findLongestPaths(0);

        assertEquals(0.0, result.getDistance(0), 0.001);
        assertEquals(5.0, result.getDistance(1), 0.001);
        assertEquals(3.0, result.getDistance(2), 0.001);
        assertEquals(7.0, result.getDistance(3), 0.001);

        assertTrue(result.isLongestPath());
        List<Integer> criticalPath = result.getCriticalPath();
        assertNotNull(criticalPath);
        assertEquals(7.0, result.getCriticalPathLength(), 0.001);
    }

    @Test
    void testUnreachableVertices() {

        Graph dag = new Graph(4, true);
        dag.addEdge(0, 1, 3);
        dag.addEdge(2, 3, 2);

        DAGShortestPath shortestPath = new DAGShortestPath(dag, metrics);
        PathResult result = shortestPath.findShortestPaths(0);

        assertTrue(result.isReachable(0));
        assertTrue(result.isReachable(1));
        assertFalse(result.isReachable(2));
        assertFalse(result.isReachable(3));

        assertEquals(0.0, result.getDistance(0), 0.001);
        assertEquals(3.0, result.getDistance(1), 0.001);
        assertTrue(Double.isInfinite(result.getDistance(2)));
        assertTrue(Double.isInfinite(result.getDistance(3)));

        assertNotNull(result.getPath(0));
        assertNotNull(result.getPath(1));
        assertNull(result.getPath(2));
        assertNull(result.getPath(3));
    }

    @Test
    void testSingleVertex() {
        Graph dag = new Graph(1, true);

        DAGShortestPath shortestPath = new DAGShortestPath(dag, metrics);
        PathResult result = shortestPath.findShortestPaths(0);

        assertEquals(0.0, result.getDistance(0), 0.001);
        assertEquals(List.of(0), result.getPath(0));
        assertTrue(result.isReachable(0));
    }

    @Test
    void testCriticalPathFinding() {

        Graph dag = new Graph(6, true);
        dag.addEdge(0, 1, 3);
        dag.addEdge(0, 2, 2);
        dag.addEdge(1, 3, 4);
        dag.addEdge(1, 4, 1);
        dag.addEdge(2, 3, 1);
        dag.addEdge(2, 4, 5);
        dag.addEdge(3, 5, 2);
        dag.addEdge(4, 5, 3);

        DAGLongestPath longestPath = new DAGLongestPath(dag, metrics);
        PathResult criticalResult = longestPath.findCriticalPath();

        assertNotNull(criticalResult);
        assertTrue(criticalResult.isLongestPath());

        List<Integer> criticalPath = criticalResult.getCriticalPath();
        assertNotNull(criticalPath);

        assertEquals(10.0, criticalResult.getCriticalPathLength(), 0.001);
    }

    @Test
    void testInvalidSource() {
        Graph dag = new Graph(3, true);
        DAGShortestPath shortestPath = new DAGShortestPath(dag, metrics);

        assertThrows(IllegalArgumentException.class, () -> {
            shortestPath.findShortestPaths(-1);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            shortestPath.findShortestPaths(3);
        });
    }

    @Test
    void testCyclicGraphThrows() {

        Graph cyclic = new Graph(3, true);
        cyclic.addEdge(0, 1);
        cyclic.addEdge(1, 2);
        cyclic.addEdge(2, 0);

        DAGShortestPath shortestPath = new DAGShortestPath(cyclic, metrics);

        assertThrows(IllegalArgumentException.class, () -> {
            shortestPath.findShortestPaths(0);
        });
    }

    @Test
    void testUndirectedGraphThrows() {
        Graph undirected = new Graph(3, false);

        assertThrows(IllegalArgumentException.class, () -> {
            new DAGShortestPath(undirected, metrics);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new DAGLongestPath(undirected, metrics);
        });
    }

    @Test
    void testMetricsTracking() {
        Graph dag = new Graph(4, true);
        dag.addEdge(0, 1, 1);
        dag.addEdge(1, 2, 1);
        dag.addEdge(2, 3, 1);

        DAGShortestPath shortestPath = new DAGShortestPath(dag, metrics);
        shortestPath.findShortestPaths(0);

        assertTrue(metrics.getCounter("vertex_relaxations") > 0);
        assertTrue(metrics.getCounter("edge_relaxations") > 0);
        assertTrue(metrics.getTime("dag_shortest_paths") > 0);
    }

    @Test
    void testPathResultMethods() {
        Graph dag = new Graph(3, true);
        dag.addEdge(0, 1, 5);
        dag.addEdge(1, 2, 3);

        DAGLongestPath longestPath = new DAGLongestPath(dag, metrics);
        PathResult result = longestPath.findLongestPaths(0);

        assertEquals(0, result.getSource());
        assertTrue(result.isLongestPath());

        assertEquals(2, result.getCriticalPathTarget());
        assertEquals(List.of(0, 1, 2), result.getCriticalPath());
        assertEquals(8.0, result.getCriticalPathLength(), 0.001);
    }
}
