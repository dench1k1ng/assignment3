package com.smartcity.graph.topo;

import com.smartcity.common.Graph;
import com.smartcity.common.Metrics;
import com.smartcity.graph.scc.*;
import java.util.*;

/**
 * Combined SCC detection and topological ordering.
 * Finds SCCs, builds condensation DAG, and computes topological order.
 */
public class SCCTopologicalOrder {
    
    private final Graph originalGraph;
    private final Metrics metrics;
    
    // Results
    private SCCResult sccResult;
    private Graph condensationDAG;
    private List<Integer> componentOrder;
    private List<Integer> originalVertexOrder;
    
    /**
     * Constructor for integrated SCC + topological ordering.
     * @param graph the original directed graph
     * @param metrics metrics tracker
     */
    public SCCTopologicalOrder(Graph graph, Metrics metrics) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Graph must be directed");
        }
        this.originalGraph = graph;
        this.metrics = metrics;
    }
    
    /**
     * Perform complete SCC detection and topological ordering.
     * @return true if successful, false if condensation DAG has cycles
     */
    public boolean computeOrder() {
        metrics.startTiming("scc_topo_total");
        
        // Step 1: Find SCCs using Tarjan's algorithm
        TarjanSCC tarjan = new TarjanSCC(originalGraph, metrics);
        sccResult = tarjan.findSCC();
        
        // Step 2: Build condensation DAG
        SCCCondensation condensation = new SCCCondensation(originalGraph, sccResult);
        condensationDAG = condensation.buildCondensationDAG();
        
        // Step 3: Topologically sort the condensation DAG
        KahnTopologicalSort kahnSort = new KahnTopologicalSort();
        componentOrder = kahnSort.topologicalSort(condensationDAG, metrics);
        
        if (componentOrder == null) {
            metrics.stopTiming("scc_topo_total");
            return false; // Condensation DAG has cycles (shouldn't happen)
        }
        
        // Step 4: Derive original vertex order
        originalVertexOrder = deriveOriginalVertexOrder();
        
        metrics.stopTiming("scc_topo_total");
        return true;
    }
    
    /**
     * Derive topological order of original vertices from component order.
     * @return list of original vertices in topological order
     */
    private List<Integer> deriveOriginalVertexOrder() {
        List<Integer> vertexOrder = new ArrayList<>();
        
        for (int componentId : componentOrder) {
            // Add all vertices from this component
            List<Integer> component = sccResult.getComponents().get(componentId);
            vertexOrder.addAll(component);
        }
        
        return vertexOrder;
    }
    
    /**
     * Get the SCC result.
     * @return SCC computation result
     */
    public SCCResult getSCCResult() {
        return sccResult;
    }
    
    /**
     * Get the condensation DAG.
     * @return condensation DAG
     */
    public Graph getCondensationDAG() {
        return condensationDAG;
    }
    
    /**
     * Get topological order of components.
     * @return list of component IDs in topological order
     */
    public List<Integer> getComponentOrder() {
        return componentOrder;
    }
    
    /**
     * Get topological order of original vertices.
     * @return list of original vertices in topological order
     */
    public List<Integer> getOriginalVertexOrder() {
        return originalVertexOrder;
    }
    
    /**
     * Get comprehensive summary of the computation.
     * @return formatted summary string
     */
    public String getSummary() {
        if (sccResult == null || condensationDAG == null || componentOrder == null) {
            return "Computation not performed yet. Call computeOrder() first.";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== SCC + Topological Order Summary ===\n");
        
        // Original graph info
        sb.append(String.format("Original graph: %d vertices, %d edges\n", 
            originalGraph.getNumVertices(), originalGraph.getNumEdges()));
        
        // SCC info
        sb.append(String.format("SCCs found: %d\n", sccResult.getNumComponents()));
        sb.append(String.format("Condensation DAG: %d vertices, %d edges\n", 
            condensationDAG.getNumVertices(), condensationDAG.getNumEdges()));
        
        // Component order
        sb.append(String.format("Component topological order: %s\n", componentOrder));
        sb.append(String.format("Original vertex order: %s\n", originalVertexOrder));
        
        // Performance
        sb.append(String.format("Total computation time: %.3f ms\n", 
            metrics.getTime("scc_topo_total") / 1_000_000.0));
        
        return sb.toString();
    }
}
