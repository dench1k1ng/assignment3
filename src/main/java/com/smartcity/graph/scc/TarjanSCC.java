package com.smartcity.graph.scc;

import com.smartcity.common.Graph;
import com.smartcity.common.Metrics;
import java.util.*;

public class TarjanSCC {

    private final Graph graph;
    private final Metrics metrics;

    private int[] discoveryTime;
    private int[] lowLink;
    private boolean[] onStack;
    private Stack<Integer> stack;
    private int time;

    private List<List<Integer>> components;
    private int[] componentId;

    public TarjanSCC(Graph graph, Metrics metrics) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("SCC algorithm requires a directed graph");
        }
        this.graph = graph;
        this.metrics = metrics;
    }

    public SCCResult findSCC() {
        metrics.startTiming("tarjan_scc_total");

        int n = graph.getNumVertices();

        discoveryTime = new int[n];
        lowLink = new int[n];
        onStack = new boolean[n];
        stack = new Stack<>();
        components = new ArrayList<>();
        componentId = new int[n];
        time = 0;

        Arrays.fill(discoveryTime, -1);
        Arrays.fill(componentId, -1);

        for (int v = 0; v < n; v++) {
            if (discoveryTime[v] == -1) {
                metrics.incrementCounter("dfs_starts");
                tarjanDFS(v);
            }
        }

        assignComponentIds();

        metrics.stopTiming("tarjan_scc_total");

        return new SCCResult(components, componentId);
    }

    private void tarjanDFS(int u) {

        discoveryTime[u] = lowLink[u] = time++;
        stack.push(u);
        onStack[u] = true;

        metrics.incrementCounter("dfs_visits");

        for (Graph.Edge edge : graph.getEdges(u)) {
            int v = edge.to;
            metrics.incrementCounter("edge_traversals");

            if (discoveryTime[v] == -1) {

                tarjanDFS(v);
                lowLink[u] = Math.min(lowLink[u], lowLink[v]);
            } else if (onStack[v]) {

                lowLink[u] = Math.min(lowLink[u], discoveryTime[v]);
                metrics.incrementCounter("back_edges");
            }
        }

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

    private void assignComponentIds() {
        int compId = components.size() - 1;
        for (List<Integer> component : components) {
            for (int vertex : component) {
                componentId[vertex] = compId;
            }
            compId--;
        }
    }

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
