package com.smartcity.graph.topo;

import com.smartcity.common.*;

public class TopologicalSortTest {

    public static void main(String[] args) {
        System.out.println("=== Testing Topological Sort Implementation ===\n");

        testGraphWithSCCs();

        System.out.println("\n" + "=".repeat(50) + "\n");

        testPureDAG();
    }

    private static void testGraphWithSCCs() {
        System.out.println("Test 1: Graph with SCCs");

        Graph graph = new Graph(6, true);
        graph.addEdge(0, 1, 2);
        graph.addEdge(1, 2, 3);
        graph.addEdge(2, 0, 1);
        graph.addEdge(3, 4, 4);
        graph.addEdge(4, 5, 2);
        graph.addEdge(2, 3, 5);

        System.out.println("Original graph:");
        System.out.println(graph);

        Metrics metrics = new MetricsImpl();
        SCCTopologicalOrder sccTopo = new SCCTopologicalOrder(graph, metrics);

        if (sccTopo.computeOrder()) {
            System.out.println(sccTopo.getSummary());

            System.out.println("Condensation DAG:");
            System.out.println(sccTopo.getCondensationDAG());
        } else {
            System.out.println("ERROR: Failed to compute topological order!");
        }
    }

    private static void testPureDAG() {
        System.out.println("Test 2: Pure DAG");

        Graph dag = new Graph(5, true);
        dag.addEdge(0, 1, 1);
        dag.addEdge(0, 2, 2);
        dag.addEdge(1, 3, 1);
        dag.addEdge(2, 3, 1);
        dag.addEdge(3, 4, 1);

        System.out.println("Pure DAG:");
        System.out.println(dag);

        Metrics metrics = new MetricsImpl();
        KahnTopologicalSort kahnSort = new KahnTopologicalSort();

        java.util.List<Integer> topoOrder = kahnSort.topologicalSort(dag, metrics);

        if (topoOrder != null) {
            System.out.println("Topological order: " + topoOrder);
            System.out.println(kahnSort.getMetricsSummary(metrics));
        } else {
            System.out.println("ERROR: Graph contains cycles!");
        }

        System.out.println("\nUsing integrated SCC + Topo approach:");
        Metrics metrics2 = new MetricsImpl();
        SCCTopologicalOrder sccTopo = new SCCTopologicalOrder(dag, metrics2);

        if (sccTopo.computeOrder()) {
            System.out.println(sccTopo.getSummary());
        }
    }
}
