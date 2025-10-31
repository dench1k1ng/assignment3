package com.smartcity.graph.topo;

import com.smartcity.common.Graph;
import com.smartcity.common.Metrics;
import java.util.List;

public interface TopologicalSort {

    List<Integer> topologicalSort(Graph graph, Metrics metrics);

    boolean isDAG(Graph graph);
}
