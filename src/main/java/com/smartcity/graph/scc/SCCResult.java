package com.smartcity.graph.scc;

import java.util.List;

/**
 * Result of Strongly Connected Components computation.
 * Contains the list of SCCs and their sizes.
 */
public class SCCResult {

    private final List<List<Integer>> components;
    private final int[] componentId;
    private final int numComponents;

    /**
     * Constructor for SCC result.
     * 
     * @param components  List of SCCs, each SCC is a list of vertices
     * @param componentId Array mapping vertex to its component ID
     */
    public SCCResult(List<List<Integer>> components, int[] componentId) {
        this.components = components;
        this.componentId = componentId.clone();
        this.numComponents = components.size();
    }

    /**
     * Get all strongly connected components.
     * 
     * @return List of SCCs
     */
    public List<List<Integer>> getComponents() {
        return components;
    }

    /**
     * Get the component ID for a vertex.
     * 
     * @param vertex the vertex
     * @return component ID
     */
    public int getComponentId(int vertex) {
        return componentId[vertex];
    }

    /**
     * Get the number of strongly connected components.
     * 
     * @return number of SCCs
     */
    public int getNumComponents() {
        return numComponents;
    }

    /**
     * Get the size of a specific component.
     * 
     * @param componentId the component ID
     * @return size of the component
     */
    public int getComponentSize(int componentId) {
        if (componentId < 0 || componentId >= components.size()) {
            throw new IllegalArgumentException("Invalid component ID: " + componentId);
        }
        return components.get(componentId).size();
    }

    /**
     * Check if two vertices are in the same SCC.
     * 
     * @param u first vertex
     * @param v second vertex
     * @return true if in same SCC
     */
    public boolean inSameComponent(int u, int v) {
        return componentId[u] == componentId[v];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SCC Result: %d components\n", numComponents));

        for (int i = 0; i < components.size(); i++) {
            sb.append(String.format("Component %d (size %d): %s\n",
                    i, components.get(i).size(), components.get(i)));
        }

        return sb.toString();
    }
}
