package com.smartcity.graph.scc;

import com.smartcity.common.Graph;
import java.util.HashSet;
import java.util.Set;

public class SCCCondensation {

    private final Graph originalGraph;
    private final SCCResult sccResult;

    public SCCCondensation(Graph originalGraph, SCCResult sccResult) {
        this.originalGraph = originalGraph;
        this.sccResult = sccResult;
    }

    public Graph buildCondensationDAG() {
        int numComponents = sccResult.getNumComponents();
        Graph condensationDAG = new Graph(numComponents, true);

        Set<String> addedEdges = new HashSet<>();

        for (int u = 0; u < originalGraph.getNumVertices(); u++) {
            int compU = sccResult.getComponentId(u);

            for (Graph.Edge edge : originalGraph.getEdges(u)) {
                int v = edge.to;
                int compV = sccResult.getComponentId(v);

                if (compU != compV) {
                    String edgeKey = compU + "->" + compV;
                    if (!addedEdges.contains(edgeKey)) {
                        condensationDAG.addEdge(compU, compV, edge.weight);
                        addedEdges.add(edgeKey);
                    }
                }
            }
        }

        return condensationDAG;
    }

    public int[] getVertexToComponentMapping() {
        int[] mapping = new int[originalGraph.getNumVertices()];
        for (int i = 0; i < mapping.length; i++) {
            mapping[i] = sccResult.getComponentId(i);
        }
        return mapping;
    }

    public String getCondensationStats() {
        Graph dag = buildCondensationDAG();

        StringBuilder sb = new StringBuilder();
        sb.append("=== Condensation Statistics ===\n");
        sb.append(String.format("Original graph: %d vertices, %d edges\n",
                originalGraph.getNumVertices(), originalGraph.getNumEdges()));
        sb.append(String.format("Condensed DAG: %d components, %d edges\n",
                dag.getNumVertices(), dag.getNumEdges()));
        sb.append(String.format("Compression ratio: %.2f%%\n",
                100.0 * dag.getNumVertices() / originalGraph.getNumVertices()));

        sb.append("\nComponent sizes:\n");
        for (int i = 0; i < sccResult.getNumComponents(); i++) {
            sb.append(String.format("  Component %d: %d vertices\n",
                    i, sccResult.getComponentSize(i)));
        }

        return sb.toString();
    }
}
