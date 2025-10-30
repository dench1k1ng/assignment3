package com.smartcity.common;

import java.util.List;

public class Graph {
    public static class Edge {
        public final int from;
        public final int to;
        public final double weight;

        public Edge(int from, int to, double weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }

        public Edge(int from, int to) {
            this(from, to, 1.0);
        }

        @Override
        public String toString() {
            return String.format("(%d -> %d, w=%.2f)", from, to, weight);
        }
    }

    private final int numVertices;
    private final List<Edge>[] adjacencyList;
    private final boolean directed;

    @SuppressWarnings("unchecked")
    public Graph(int numVertices, boolean directed) {
        this.numVertices = numVertices;
        this.directed = directed;
        this.adjacencyList = new List[numVertices];

        for (int i = 0; i < numVertices; i++) {
            this.adjacencyList[i] = new java.util.ArrayList<>();
        }
    }

    public void addEdge(int from, int to, double weight) {
        validateVertex(from);
        validateVertex(to);

        adjacencyList[from].add(new Edge(from, to, weight));

        if (!directed) {
            adjacencyList[to].add(new Edge(to, from, weight));
        }
    }

    public void addEdge(int from, int to) {
        addEdge(from, to, 1.0);
    }

    public List<Edge> getEdges(int vertex) {
        validateVertex(vertex);
        return adjacencyList[vertex];
    }

    public int getNumVertices() {
        return numVertices;
    }

    public boolean isDirected() {
        return directed;
    }

    public int getNumEdges() {
        int count = 0;
        for (int i = 0; i < numVertices; i++) {
            count += adjacencyList[i].size();
        }
        return directed ? count : count / 2;
    }

    private void validateVertex(int vertex) {
        if (vertex < 0 || vertex >= numVertices) {
            throw new IllegalArgumentException(
                    String.format("Vertex %d is out of range [0, %d)", vertex, numVertices));
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Graph: %d vertices, %d edges, %s\n",
                numVertices, getNumEdges(), directed ? "directed" : "undirected"));

        for (int i = 0; i < numVertices; i++) {
            sb.append(String.format("Vertex %d: %s\n", i, adjacencyList[i]));
        }

        return sb.toString();
    }
}
