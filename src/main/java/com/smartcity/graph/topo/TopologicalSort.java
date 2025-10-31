package com.smartcity.graph.topo;

import com.smartcity.common.Graph;
import com.smartcity.common.Metrics;
import java.util.List;

/**
 * Interface for topological sorting algorithms.
 */
public interface TopologicalSort {

    /**
     * Compute topological ordering of a DAG.
     * 
     * @param graph   the DAG to sort
     * @param metrics metrics tracker for performance measurement
     * @return topological ordering as list of vertices, or null if graph has cycles
     */
    List<Integer> topologicalSort(Graph graph, Metrics metrics);

    /**
     * Check if the given graph is a DAG (no cycles).
     * 
     * @param graph the graph to check
     * @return true if DAG, false if has cycles
     */
    boolean isDAG(Graph graph);
}
