package com.smartcity.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;


public class GraphData {


    public static class EdgeData {
        @JsonProperty("u")
        public int from;

        @JsonProperty("v")
        public int to;

        @JsonProperty("w")
        public double weight = 1.0;

        public EdgeData() {
        }

        public EdgeData(int from, int to, double weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }
    }

    @JsonProperty("directed")
    public boolean directed = true;

    @JsonProperty("n")
    public int numVertices;

    @JsonProperty("edges")
    public List<EdgeData> edges;

    @JsonProperty("source")
    public Integer source;

    @JsonProperty("weight_model")
    public String weightModel = "edge";

    public GraphData() {
    }

    /**
     * Convert to Graph object.
     * 
     * @return Graph instance
     */
    public Graph toGraph() {
        Graph graph = new Graph(numVertices, directed);

        if (edges != null) {
            for (EdgeData edge : edges) {
                graph.addEdge(edge.from, edge.to, edge.weight);
            }
        }

        return graph;
    }

    @Override
    public String toString() {
        return String.format("GraphData{vertices=%d, edges=%d, directed=%s, source=%s, weightModel=%s}",
                numVertices,
                edges != null ? edges.size() : 0,
                directed,
                source,
                weightModel);
    }
}
