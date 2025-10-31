package com.smartcity.graph.dagsp;

import com.smartcity.common.Graph;
import com.smartcity.common.Metrics;
import com.smartcity.graph.topo.KahnTopologicalSort;
import java.util.Arrays;
import java.util.List;

/**
 * Single-source longest paths algorithm for DAGs.
 * Uses topological ordering and dynamic programming with max relaxation.
 */
public class DAGLongestPath {
    
    private final Graph graph;
    private final Metrics metrics;
    
    /**
     * Constructor for DAG longest path algorithm.
     * @param graph the DAG
     * @param metrics metrics tracker
     */
    public DAGLongestPath(Graph graph, Metrics metrics) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Algorithm requires a directed graph");
        }
        this.graph = graph;
        this.metrics = metrics;
    }
    
    /**
     * Compute single-source longest paths.
     * @param source the source vertex
     * @return PathResult containing distances and paths
     */
    public PathResult findLongestPaths(int source) {
        metrics.startTiming("dag_longest_paths");
        
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
        
        Arrays.fill(distances, Double.NEGATIVE_INFINITY);
        Arrays.fill(predecessors, -1);
        
        distances[source] = 0.0;
        
        // Process vertices in topological order
        for (int u : topoOrder) {
            if (Double.isInfinite(distances[u])) {
                continue; // Skip unreachable vertices
            }
            
            metrics.incrementCounter("vertex_relaxations");
            
            // Relax all outgoing edges (maximize instead of minimize)
            for (Graph.Edge edge : graph.getEdges(u)) {
                int v = edge.to;
                double newDistance = distances[u] + edge.weight;
                
                metrics.incrementCounter("edge_relaxations");
                
                if (newDistance > distances[v]) {
                    distances[v] = newDistance;
                    predecessors[v] = u;
                    metrics.incrementCounter("distance_updates");
                }
            }
        }
        
        metrics.stopTiming("dag_longest_paths");
        
        return new PathResult(distances, predecessors, source, true);
    }
    
    /**
     * Find the critical path (longest path) in the entire DAG.
     * This tries all possible source vertices and returns the longest path found.
     * @return PathResult for the critical path
     */
    public PathResult findCriticalPath() {
        metrics.startTiming("dag_critical_path");
        
        PathResult bestResult = null;
        double maxLength = Double.NEGATIVE_INFINITY;
        
        // Try each vertex as a potential start of critical path
        for (int source = 0; source < graph.getNumVertices(); source++) {
            PathResult result = findLongestPaths(source);
            double criticalLength = result.getCriticalPathLength();
            
            if (criticalLength > maxLength) {
                maxLength = criticalLength;
                bestResult = result;
            }
        }
        
        metrics.stopTiming("dag_critical_path");
        
        return bestResult;
    }
    
    /**
     * Get performance metrics summary.
     * @return formatted metrics string
     */
    public String getMetricsSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== DAG Longest Path Metrics ===\n");
        sb.append(String.format("Vertex relaxations: %d\n", 
            metrics.getCounter("vertex_relaxations")));
        sb.append(String.format("Edge relaxations: %d\n", 
            metrics.getCounter("edge_relaxations")));
        sb.append(String.format("Distance updates: %d\n", 
            metrics.getCounter("distance_updates")));
        
        if (metrics.getTime("dag_longest_paths") > 0) {
            sb.append(String.format("Longest paths time: %.3f ms\n", 
                metrics.getTime("dag_longest_paths") / 1_000_000.0));
        }
        
        if (metrics.getTime("dag_critical_path") > 0) {
            sb.append(String.format("Critical path time: %.3f ms\n", 
                metrics.getTime("dag_critical_path") / 1_000_000.0));
        }
        
        return sb.toString();
    }
}
