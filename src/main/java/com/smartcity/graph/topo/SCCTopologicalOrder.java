package com.smartcity.graph.topo;

import com.smartcity.common.Graph;
import com.smartcity.common.Metrics;
import com.smartcity.graph.scc.*;
import java.util.*;

public class SCCTopologicalOrder {

    private final Graph originalGraph;
    private final Metrics metrics;

    private SCCResult sccResult;
    private Graph condensationDAG;
    private List<Integer> componentOrder;
    private List<Integer> originalVertexOrder;

    public SCCTopologicalOrder(Graph graph, Metrics metrics) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Graph must be directed");
        }
        this.originalGraph = graph;
        this.metrics = metrics;
    }

    public boolean computeOrder() {
        metrics.startTiming("scc_topo_total");

        TarjanSCC tarjan = new TarjanSCC(originalGraph, metrics);
        sccResult = tarjan.findSCC();

        SCCCondensation condensation = new SCCCondensation(originalGraph, sccResult);
        condensationDAG = condensation.buildCondensationDAG();

        KahnTopologicalSort kahnSort = new KahnTopologicalSort();
        componentOrder = kahnSort.topologicalSort(condensationDAG, metrics);

        if (componentOrder == null) {
            metrics.stopTiming("scc_topo_total");
            return false;
        }

        originalVertexOrder = deriveOriginalVertexOrder();

        metrics.stopTiming("scc_topo_total");
        return true;
    }

    private List<Integer> deriveOriginalVertexOrder() {
        List<Integer> vertexOrder = new ArrayList<>();

        for (int componentId : componentOrder) {

            List<Integer> component = sccResult.getComponents().get(componentId);
            vertexOrder.addAll(component);
        }

        return vertexOrder;
    }

    public SCCResult getSCCResult() {
        return sccResult;
    }

    public Graph getCondensationDAG() {
        return condensationDAG;
    }

    public List<Integer> getComponentOrder() {
        return componentOrder;
    }

    public List<Integer> getOriginalVertexOrder() {
        return originalVertexOrder;
    }

    public String getSummary() {
        if (sccResult == null || condensationDAG == null || componentOrder == null) {
            return "Computation not performed yet. Call computeOrder() first.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== SCC + Topological Order Summary ===\n");

        sb.append(String.format("Original graph: %d vertices, %d edges\n",
                originalGraph.getNumVertices(), originalGraph.getNumEdges()));

        sb.append(String.format("SCCs found: %d\n", sccResult.getNumComponents()));
        sb.append(String.format("Condensation DAG: %d vertices, %d edges\n",
                condensationDAG.getNumVertices(), condensationDAG.getNumEdges()));

        sb.append(String.format("Component topological order: %s\n", componentOrder));
        sb.append(String.format("Original vertex order: %s\n", originalVertexOrder));

        sb.append(String.format("Total computation time: %.3f ms\n",
                metrics.getTime("scc_topo_total") / 1_000_000.0));

        return sb.toString();
    }
}
