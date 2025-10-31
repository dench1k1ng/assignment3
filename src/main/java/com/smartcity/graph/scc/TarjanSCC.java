package com.smartcity.graph.scc;

import com.smartcity.common.Graph;
import com.smartcity.common.Metrics;
import java.util.*;

/**
 * Tarjan's algorithm for finding Strongly Connected Components.
 * Uses DFS with low-link values to identify SCCs in one pass.
 */
public class TarjanSCC {

    private final Graph graph;
    private final Metrics metrics;

    // Tarjan's algorithm state
    private int[] discoveryTime;
    private int[] lowLink;
    private boolean[] onStack;
    private Stack<Integer> stack;
    private int time;

    // Results
    private List<List<Integer>> components;
    private int[] componentId;

    /**
     * Constructor for Tarjan's SCC algorithm.
     * 
     * @param graph   the directed graph to analyze
     * @param metrics metrics tracker for performance measurement
     */
    public TarjanSCC(Graph graph, Metrics metrics) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("SCC algorithm requires a directed graph");
        }
        this.graph = graph;
        this.metrics = metrics;
    }

    /**
     * Find all strongly connected components using Tarjan's algorithm.
     * 
     * @return SCCResult containing all SCCs
     */
    public SCCResult findSCC() {
        metrics.startTiming("tarjan_scc_total");

        int n = graph.getNumVertices();

        // Initialize arrays
        discoveryTime = new int[n];
        lowLink = new int[n];
        onStack = new boolean[n];
        stack = new Stack<>();
        components = new ArrayList<>();
        componentId = new int[n];
        time = 0;

        Arrays.fill(discoveryTime, -1);
        Arrays.fill(componentId, -1);

        // Run DFS from each unvisited vertex
        for (int v = 0; v < n; v++) {
            if (discoveryTime[v] == -1) {
                metrics.incrementCounter("dfs_starts");
                tarjanDFS(v);
            }
        }

        // Assign component IDs (reverse order for topological ordering)
        assignComponentIds();

        metrics.stopTiming("tarjan_scc_total");

        return new SCCResult(components, componentId);
    }

    /**
     * Tarjan's DFS implementation.
     * 
     * @param u current vertex
     */
    private void tarjanDFS(int u) {
        // Initialize discovery time and low-link value
        discoveryTime[u] = lowLink[u] = time++;
        stack.push(u);
        onStack[u] = true;

        metrics.incrementCounter("dfs_visits");

        // Visit all adjacent vertices
        for (Graph.Edge edge : graph.getEdges(u)) {
            int v = edge.to;
            metrics.incrementCounter("edge_traversals");

            if (discoveryTime[v] == -1) {
                // Tree edge - recursively visit
                tarjanDFS(v);
                lowLink[u] = Math.min(lowLink[u], lowLink[v]);
            } else if (onStack[v]) {
                // Back edge - update low-link
                lowLink[u] = Math.min(lowLink[u], discoveryTime[v]);
                metrics.incrementCounter("back_edges");
            }
        }

        // If u is a root node, pop the stack and create SCC
        if (lowLink[u] == discoveryTime[u]) {
            List<Integer> component = new ArrayList<>();
            int v;
            do {
                v = stack.pop();
                onStack[v] = false;
                component.add(v);
                metrics.incrementCounter("scc_pops");
            } while (v != u);

            components.add(component);
            metrics.incrementCounter("scc_found");
        }
    }

    /**
     * Assign component IDs to vertices.
     * Components are numbered in reverse topological order.
     */
    private void assignComponentIds() {
        int compId = components.size() - 1;
        for (List<Integer> component : components) {
            for (int vertex : component) {
                componentId[vertex] = compId;
            }
            compId--;
        }
    }

    /**
     * Get performance metrics summary.
     * 
     * @return formatted metrics string
     */
    public String getMetricsSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Tarjan SCC Metrics ===\n");
        sb.append(String.format("DFS starts: %d\n", metrics.getCounter("dfs_starts")));
        sb.append(String.format("DFS visits: %d\n", metrics.getCounter("dfs_visits")));
        sb.append(String.format("Edge traversals: %d\n", metrics.getCounter("edge_traversals")));
        sb.append(String.format("Back edges: %d\n", metrics.getCounter("back_edges")));
        sb.append(String.format("SCCs found: %d\n", metrics.getCounter("scc_found")));
        sb.append(String.format("Total time: %.3f ms\n",
                metrics.getTime("tarjan_scc_total") / 1_000_000.0));
        return sb.toString();
    }
}
