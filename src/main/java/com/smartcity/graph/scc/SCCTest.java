package com.smartcity.graph.scc;

import com.smartcity.common.*;

public class SCCTest {

    public static void main(String[] args) {

        try {
            System.out.println("=== Testing SCC Implementation ===\n");

            Graph graph = new Graph(6, true);
            graph.addEdge(0, 1, 2);
            graph.addEdge(1, 2, 3);
            graph.addEdge(2, 0, 1);
            graph.addEdge(3, 4, 4);
            graph.addEdge(4, 5, 2);

            System.out.println("Original graph:");
            System.out.println(graph);

            Metrics metrics = new MetricsImpl();
            TarjanSCC tarjan = new TarjanSCC(graph, metrics);
            SCCResult result = tarjan.findSCC();

            System.out.println("SCC Result:");
            System.out.println(result);

            System.out.println(tarjan.getMetricsSummary());

            SCCCondensation condensation = new SCCCondensation(graph, result);
            Graph dag = condensation.buildCondensationDAG();

            System.out.println("Condensation DAG:");
            System.out.println(dag);

            System.out.println(condensation.getCondensationStats());

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
