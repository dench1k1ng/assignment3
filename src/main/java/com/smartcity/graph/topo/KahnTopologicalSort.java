package com.smartcity.graph.topo;

import com.smartcity.common.Graph;
import com.smartcity.common.Metrics;
import java.util.*;

public class KahnTopologicalSort implements TopologicalSort {

    @Override
    public List<Integer> topologicalSort(Graph graph, Metrics metrics) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Topological sort requires a directed graph");
        }

        metrics.startTiming("kahn_topological_sort");

        int n = graph.getNumVertices();
        List<Integer> result = new ArrayList<>();

        int[] inDegree = calculateInDegrees(graph, metrics);

        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            if (inDegree[i] == 0) {
                queue.offer(i);
                metrics.incrementCounter("queue_pushes");
            }
        }

        while (!queue.isEmpty()) {
            int u = queue.poll();
            result.add(u);
            metrics.incrementCounter("queue_pops");
            metrics.incrementCounter("vertices_processed");

            for (Graph.Edge edge : graph.getEdges(u)) {
                int v = edge.to;
                inDegree[v]--;
                metrics.incrementCounter("edge_removals");

                if (inDegree[v] == 0) {
                    queue.offer(v);
                    metrics.incrementCounter("queue_pushes");
                }
            }
        }

        metrics.stopTiming("kahn_topological_sort");

        if (result.size() != n) {
            metrics.incrementCounter("cycle_detected");
            return null;
        }

        return result;
    }

    @Override
    public boolean isDAG(Graph graph) {
        Metrics tempMetrics = new com.smartcity.common.MetricsImpl();
        List<Integer> topoOrder = topologicalSort(graph, tempMetrics);
        return topoOrder != null;
    }

    private int[] calculateInDegrees(Graph graph, Metrics metrics) {
        int n = graph.getNumVertices();
        int[] inDegree = new int[n];

        for (int u = 0; u < n; u++) {
            for (Graph.Edge edge : graph.getEdges(u)) {
                inDegree[edge.to]++;
                metrics.incrementCounter("indegree_calculations");
            }
        }

        return inDegree;
    }

    public String getMetricsSummary(Metrics metrics) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Kahn's Topological Sort Metrics ===\n");
        sb.append(String.format("In-degree calculations: %d\n",
                metrics.getCounter("indegree_calculations")));
        sb.append(String.format("Queue pushes: %d\n",
                metrics.getCounter("queue_pushes")));
        sb.append(String.format("Queue pops: %d\n",
                metrics.getCounter("queue_pops")));
        sb.append(String.format("Edge removals: %d\n",
                metrics.getCounter("edge_removals")));
        sb.append(String.format("Vertices processed: %d\n",
                metrics.getCounter("vertices_processed")));

        if (metrics.getCounter("cycle_detected") > 0) {
            sb.append("Cycle detected: YES\n");
        } else {
            sb.append("Cycle detected: NO\n");
        }

        sb.append(String.format("Total time: %.3f ms\n",
                metrics.getTime("kahn_topological_sort") / 1_000_000.0));

        return sb.toString();
    }
}
