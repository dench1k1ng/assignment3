package com.smartcity.graph.scc;

import java.util.List;

public class SCCResult {

    private final List<List<Integer>> components;
    private final int[] componentId;
    private final int numComponents;

    public SCCResult(List<List<Integer>> components, int[] componentId) {
        this.components = components;
        this.componentId = componentId.clone();
        this.numComponents = components.size();
    }

    public List<List<Integer>> getComponents() {
        return components;
    }

    public int getComponentId(int vertex) {
        return componentId[vertex];
    }

    public int getNumComponents() {
        return numComponents;
    }

    public int getComponentSize(int componentId) {
        if (componentId < 0 || componentId >= components.size()) {
            throw new IllegalArgumentException("Invalid component ID: " + componentId);
        }
        return components.get(componentId).size();
    }

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
