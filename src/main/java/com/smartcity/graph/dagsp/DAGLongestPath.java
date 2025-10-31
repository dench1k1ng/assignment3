package com.smartcity.graph.dagsp;

import com.smartcity.common.Graph;
import com.smartcity.common.Metrics;
import com.smartcity.graph.topo.KahnTopologicalSort;
import java.util.Arrays;
import java.util.List;

public class DAGLongestPath {

    private final Graph graph;
    private final Metrics metrics;

    public DAGLongestPath(Graph graph, Metrics metrics) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Algorithm requires a directed graph");
        }
        this.graph = graph;
        this.metrics = metrics;
    }

    public PathResult findLongestPaths(int source) {
        metrics.startTiming("dag_longest_paths");

        if (source < 0 || source >= graph.getNumVertices()) {
            throw new IllegalArgumentException("Invalid source vertex: " + source);
        }

        int n = graph.getNumVertices();

        KahnTopologicalSort topoSort = new KahnTopologicalSort();
        List<Integer> topoOrder = topoSort.topologicalSort(graph, metrics);

        if (topoOrder == null) {
            throw new IllegalArgumentException("Graph contains cycles - not a DAG");
        }

        double[] distances = new double[n];
        int[] predecessors = new int[n];

        Arrays.fill(distances, Double.NEGATIVE_INFINITY);
        Arrays.fill(predecessors, -1);

        distances[source] = 0.0;

        for (int u : topoOrder) {
            if (Double.isInfinite(distances[u])) {
                continue;
            }

            metrics.incrementCounter("vertex_relaxations");

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

    public PathResult findCriticalPath() {
        metrics.startTiming("dag_critical_path");

        PathResult bestResult = null;
        double maxLength = Double.NEGATIVE_INFINITY;

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
