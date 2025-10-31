package com.smartcity.graph.dagsp;

import com.smartcity.common.Graph;
import com.smartcity.common.Metrics;
import com.smartcity.graph.topo.KahnTopologicalSort;
import java.util.Arrays;
import java.util.List;

/**
 * Single-source shortest paths algorithm for DAGs.
 * Uses topological ordering and dynamic programming.
 */
public class DAGShortestPath {

    private final Graph graph;
    private final Metrics metrics;

    /**
     * Constructor for DAG shortest path algorithm.
     * 
     * @param graph   the DAG
     * @param metrics metrics tracker
     */
    public DAGShortestPath(Graph graph, Metrics metrics) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Algorithm requires a directed graph");
        }
        this.graph = graph;
        this.metrics = metrics;
    }

    /**
     * Compute single-source shortest paths.
     * 
     * @param source the source vertex
     * @return PathResult containing distances and paths
     */
    public PathResult findShortestPaths(int source) {
        metrics.startTiming("dag_shortest_paths");

        if (source < 0 || source >= graph.getNumVertices()) {
            throw new IllegalArgumentException("Invalid source vertex: " + source);
        }

        int n = graph.getNumVertices();

        // Get topological ordering
        KahnTopologicalSort topoSort = new KahnTopologicalSort();
        List<Integer> topoOrder = topoSort.topologicalSort(graph, metrics);

        if (topoOrder == null) {
            throw new IllegalArgumentException("Graph contains cycles - not a DAG");
        }

        // Initialize distances and predecessors
        double[] distances = new double[n];
        int[] predecessors = new int[n];

        Arrays.fill(distances, Double.POSITIVE_INFINITY);
        Arrays.fill(predecessors, -1);

        distances[source] = 0.0;

        // Process vertices in topological order
        for (int u : topoOrder) {
            if (Double.isInfinite(distances[u])) {
                continue; // Skip unreachable vertices
            }

            metrics.incrementCounter("vertex_relaxations");

            // Relax all outgoing edges
            for (Graph.Edge edge : graph.getEdges(u)) {
                int v = edge.to;
                double newDistance = distances[u] + edge.weight;

                metrics.incrementCounter("edge_relaxations");

                if (newDistance < distances[v]) {
                    distances[v] = newDistance;
                    predecessors[v] = u;
                    metrics.incrementCounter("distance_updates");
                }
            }
        }

        metrics.stopTiming("dag_shortest_paths");

        return new PathResult(distances, predecessors, source, false);
    }

    /**
     * Get performance metrics summary.
     * 
     * @return formatted metrics string
     */
    public String getMetricsSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== DAG Shortest Path Metrics ===\n");
        sb.append(String.format("Vertex relaxations: %d\n",
                metrics.getCounter("vertex_relaxations")));
        sb.append(String.format("Edge relaxations: %d\n",
                metrics.getCounter("edge_relaxations")));
        sb.append(String.format("Distance updates: %d\n",
                metrics.getCounter("distance_updates")));
        sb.append(String.format("Total time: %.3f ms\n",
                metrics.getTime("dag_shortest_paths") / 1_000_000.0));

        return sb.toString();
    }
}
