package com.smartcity.graph.scc;

import com.smartcity.common.Graph;
import java.util.HashSet;
import java.util.Set;

/**
 * Builds the condensation graph (DAG) from SCC result.
 * Each SCC becomes a single vertex in the condensation graph.
 */
public class SCCCondensation {

    private final Graph originalGraph;
    private final SCCResult sccResult;

    /**
     * Constructor for SCC condensation.
     * 
     * @param originalGraph the original graph
     * @param sccResult     the SCC computation result
     */
    public SCCCondensation(Graph originalGraph, SCCResult sccResult) {
        this.originalGraph = originalGraph;
        this.sccResult = sccResult;
    }

    /**
     * Build the condensation DAG.
     * 
     * @return condensation graph where each vertex represents an SCC
     */
    public Graph buildCondensationDAG() {
        int numComponents = sccResult.getNumComponents();
        Graph condensationDAG = new Graph(numComponents, true);

        // Use set to avoid duplicate edges between components
        Set<String> addedEdges = new HashSet<>();

        // For each vertex in original graph
        for (int u = 0; u < originalGraph.getNumVertices(); u++) {
            int compU = sccResult.getComponentId(u);

            // Check all outgoing edges
            for (Graph.Edge edge : originalGraph.getEdges(u)) {
                int v = edge.to;
                int compV = sccResult.getComponentId(v);

                // If edge goes between different components, add to condensation
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

    /**
     * Get a mapping of original vertices to their SCC representatives.
     * 
     * @return array where index is original vertex, value is SCC component ID
     */
    public int[] getVertexToComponentMapping() {
        int[] mapping = new int[originalGraph.getNumVertices()];
        for (int i = 0; i < mapping.length; i++) {
            mapping[i] = sccResult.getComponentId(i);
        }
        return mapping;
    }

    /**
     * Get statistics about the condensation.
     * 
     * @return formatted statistics string
     */
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

        // Component size distribution
        sb.append("\nComponent sizes:\n");
        for (int i = 0; i < sccResult.getNumComponents(); i++) {
            sb.append(String.format("  Component %d: %d vertices\n",
                    i, sccResult.getComponentSize(i)));
        }

        return sb.toString();
    }
}
