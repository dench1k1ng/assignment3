package com.smartcity.graph.dagsp;

import com.smartcity.common.*;

public class DAGShortestPathTest {

    public static void main(String[] args) {
        System.out.println("=== Testing DAG Shortest & Longest Paths ===\n");

        testSimpleDAG();

        System.out.println("\n" + "=".repeat(60) + "\n");

        testComplexDAG();
    }

    private static void testSimpleDAG() {
        System.out.println("Test 1: Simple DAG");

        Graph dag = new Graph(4, true);
        dag.addEdge(0, 1, 5);
        dag.addEdge(0, 2, 3);
        dag.addEdge(1, 3, 2);
        dag.addEdge(2, 3, 4);

        System.out.println("DAG:");
        System.out.println(dag);

        Metrics metrics = new MetricsImpl();

        DAGShortestPath shortestPath = new DAGShortestPath(dag, metrics);
        PathResult shortestResult = shortestPath.findShortestPaths(0);

        System.out.println("Shortest Paths from vertex 0:");
        System.out.println(shortestResult);
        System.out.println(shortestPath.getMetricsSummary());

        metrics.reset();

        DAGLongestPath longestPath = new DAGLongestPath(dag, metrics);
        PathResult longestResult = longestPath.findLongestPaths(0);

        System.out.println("Longest Paths from vertex 0:");
        System.out.println(longestResult);
        System.out.println(longestPath.getMetricsSummary());
    }

    private static void testComplexDAG() {
        System.out.println("Test 2: Complex DAG with Critical Path");

        Graph dag = new Graph(6, true);
        dag.addEdge(0, 1, 3);
        dag.addEdge(0, 2, 2);
        dag.addEdge(1, 3, 4);
        dag.addEdge(1, 4, 1);
        dag.addEdge(2, 3, 1);
        dag.addEdge(2, 4, 5);
        dag.addEdge(3, 5, 2);
        dag.addEdge(4, 5, 3);

        System.out.println("Complex DAG:");
        System.out.println(dag);

        Metrics metrics = new MetricsImpl();

        DAGLongestPath longestPath = new DAGLongestPath(dag, metrics);
        PathResult criticalPathResult = longestPath.findCriticalPath();

        System.out.println("Critical Path Analysis:");
        if (criticalPathResult != null) {
            System.out.println(criticalPathResult);
            System.out.println("Critical Path: " + criticalPathResult.getCriticalPath());
            System.out.println("Critical Path Length: " + criticalPathResult.getCriticalPathLength());
        } else {
            System.out.println("No critical path found!");
        }

        System.out.println(longestPath.getMetricsSummary());

        metrics.reset();
        DAGShortestPath shortestPath = new DAGShortestPath(dag, metrics);
        PathResult shortestResult = shortestPath.findShortestPaths(criticalPathResult.getSource());

        System.out.println("Shortest paths from critical path source (" +
                criticalPathResult.getSource() + "):");
        System.out.println(shortestResult);
    }
}
