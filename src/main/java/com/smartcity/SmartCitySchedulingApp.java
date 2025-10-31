package com.smartcity;

import com.smartcity.common.*;
import com.smartcity.graph.scc.*;
import com.smartcity.graph.topo.*;
import com.smartcity.graph.dagsp.*;

import java.util.*;

public class SmartCitySchedulingApp {

    public static void main(String[] args) {
        System.out.println("=== Smart City/Smart Campus Scheduling System ===\n");

        demonstrateSimpleDAG();
        System.out.println("\n" + "=".repeat(60) + "\n");

        demonstrateMixedGraph();
        System.out.println("\n" + "=".repeat(60) + "\n");

        demonstrateComplexWorkflow();
    }

    private static void demonstrateSimpleDAG() {
        System.out.println("1. SIMPLE DAG SCHEDULING EXAMPLE");
        System.out.println("Tasks: A->B, A->C, B->D, C->D");
        System.out.println("Weights represent task durations (minutes)\n");

        Graph graph = new Graph(4, true);
        graph.addEdge(0, 1, 10);
        graph.addEdge(0, 2, 15);
        graph.addEdge(1, 3, 20);
        graph.addEdge(2, 3, 25);

        System.out.println("Graph structure:");
        System.out.println("A(0) --(10)--> B(1) --(20)--> D(3)");
        System.out.println("A(0) --(15)--> C(2) --(25)--> D(3)");

        MetricsImpl metrics = new MetricsImpl();
        TarjanSCC sccDetector = new TarjanSCC(graph, metrics);
        SCCResult sccResult = sccDetector.findSCC();

        System.out.println("\nSCC Analysis:");
        System.out.println("Number of SCCs: " + sccResult.getNumComponents());
        boolean isDAG = sccResult.getNumComponents() == graph.getNumVertices();
        System.out.println("Is DAG: " + isDAG);

        if (isDAG) {

            KahnTopologicalSort topoSort = new KahnTopologicalSort();
            List<Integer> order = topoSort.topologicalSort(graph, metrics);

            System.out.println("Topological Order: " + formatVertexNames(order));

            DAGShortestPath shortestPath = new DAGShortestPath(graph, metrics);
            DAGLongestPath longestPath = new DAGLongestPath(graph, metrics);

            PathResult shortestResult = shortestPath.findShortestPaths(0);
            PathResult longestResult = longestPath.findLongestPaths(0);

            System.out.println("\nProject Scheduling Analysis:");
            System.out.println("Minimum time to complete all tasks: " + longestResult.getDistance(3) + " minutes");
            System.out.println("Critical path: " + formatPath(longestResult.getPath(3)));

            System.out.println("\nAll shortest times from A:");
            for (int v = 0; v < 4; v++) {
                double dist = shortestResult.getDistance(v);
                if (dist != Double.POSITIVE_INFINITY) {
                    System.out.println("  To " + getVertexName(v) + ": " + dist + " minutes");
                }
            }
        }
    }

    private static void demonstrateMixedGraph() {
        System.out.println("2. MIXED GRAPH WITH CYCLES EXAMPLE");
        System.out.println("Tasks with circular dependencies that need resolution\n");

        Graph graph = new Graph(5, true);
        graph.addEdge(0, 1, 10);
        graph.addEdge(1, 2, 15);
        graph.addEdge(2, 1, 5);
        graph.addEdge(1, 3, 20);
        graph.addEdge(3, 4, 25);
        graph.addEdge(0, 4, 50);

        System.out.println("Graph structure (with cycle B<->C):");
        System.out.println("A(0) --(10)--> B(1) --(15)--> C(2)");
        System.out.println("               ^               |");
        System.out.println("               +-------(5)-----+");
        System.out.println("B(1) --(20)--> D(3) --(25)--> E(4)");
        System.out.println("A(0) ----------(50)---------> E(4)");

        MetricsImpl metrics = new MetricsImpl();
        TarjanSCC sccDetector = new TarjanSCC(graph, metrics);
        SCCResult sccResult = sccDetector.findSCC();

        System.out.println("\nSCC Analysis:");
        System.out.println("Number of SCCs: " + sccResult.getNumComponents());
        boolean isDAG = sccResult.getNumComponents() == graph.getNumVertices();
        System.out.println("Is DAG: " + isDAG);

        System.out.println("SCCs found:");
        for (int i = 0; i < sccResult.getNumComponents(); i++) {
            List<Integer> component = sccResult.getComponents().get(i);
            System.out.println("  SCC " + i + ": " + formatVertexNames(component));
        }

        SCCCondensation condensation = new SCCCondensation(graph, sccResult);
        Graph condensedGraph = condensation.buildCondensationDAG();

        System.out.println("\nCondensed DAG:");
        System.out.println("Condensed graph has " + condensedGraph.getNumVertices() + " super-nodes");

        MetricsImpl topoMetrics = new MetricsImpl();
        SCCTopologicalOrder sccTopo = new SCCTopologicalOrder(graph, topoMetrics);
        sccTopo.computeOrder();
        List<Integer> condensedOrder = sccTopo.getComponentOrder();

        System.out.println("Processing order of SCCs: " + condensedOrder);

        MetricsImpl condensedMetrics = new MetricsImpl();
        DAGShortestPath shortestPath = new DAGShortestPath(condensedGraph, condensedMetrics);
        PathResult result = shortestPath.findShortestPaths(0);

        System.out.println("\nShortest paths between super-nodes:");
        for (int v = 0; v < condensedGraph.getNumVertices(); v++) {
            double dist = result.getDistance(v);
            if (dist != Double.POSITIVE_INFINITY && v != 0) {
                System.out.println("  To SCC " + v + ": " + dist + " units");
            }
        }
    }

    private static void demonstrateComplexWorkflow() {
        System.out.println("3. COMPLEX WORKFLOW WITH PERFORMANCE METRICS");
        System.out.println("Large task dependency graph analysis\n");

        Graph graph = new Graph(8, true);

        graph.addEdge(0, 1, 10);
        graph.addEdge(1, 2, 15);
        graph.addEdge(2, 0, 20);

        graph.addEdge(3, 4, 25);

        graph.addEdge(5, 6, 30);
        graph.addEdge(6, 7, 35);
        graph.addEdge(5, 7, 40);

        graph.addEdge(1, 3, 12);
        graph.addEdge(4, 6, 18);

        System.out.println("Complex graph with 8 tasks and multiple dependencies");

        MetricsImpl metrics = new MetricsImpl();

        long startTime = System.nanoTime();

        TarjanSCC sccDetector = new TarjanSCC(graph, metrics);
        SCCResult sccResult = sccDetector.findSCC();

        long sccTime = System.nanoTime();

        SCCCondensation condensation = new SCCCondensation(graph, sccResult);
        Graph condensedGraph = condensation.buildCondensationDAG();

        long condensationTime = System.nanoTime();

        MetricsImpl topoMetrics = new MetricsImpl();
        SCCTopologicalOrder sccTopo = new SCCTopologicalOrder(graph, topoMetrics);
        sccTopo.computeOrder();
        List<Integer> topoOrder = sccTopo.getComponentOrder();

        long topoTime = System.nanoTime();

        MetricsImpl pathMetrics = new MetricsImpl();
        DAGShortestPath shortestPath = new DAGShortestPath(condensedGraph, pathMetrics);
        PathResult pathResult = shortestPath.findShortestPaths(0);

        long pathTime = System.nanoTime();

        System.out.println("Analysis Results:");
        System.out.println("Original graph: " + graph.getNumVertices() + " vertices, " +
                graph.getNumEdges() + " edges");
        System.out.println("SCCs found: " + sccResult.getNumComponents());
        System.out.println("Condensed graph: " + condensedGraph.getNumVertices() + " vertices");
        System.out.println("Is schedulable (DAG after condensation): " +
                (condensedGraph.getNumVertices() <= graph.getNumVertices()));

        System.out.println("\nPerformance Metrics:");
        System.out.println("SCC Detection: " + String.format("%.3f ms", (sccTime - startTime) / 1_000_000.0));
        System.out
                .println("Graph Condensation: " + String.format("%.3f ms", (condensationTime - sccTime) / 1_000_000.0));
        System.out
                .println("Topological Sort: " + String.format("%.3f ms", (topoTime - condensationTime) / 1_000_000.0));
        System.out.println("Shortest Paths: " + String.format("%.3f ms", (pathTime - topoTime) / 1_000_000.0));
        System.out.println("Total time: " + String.format("%.3f ms", (pathTime - startTime) / 1_000_000.0));

        System.out.println("Metrics summary: " + metrics.getSummary());

        System.out.println("\nTopological order of SCCs: " + topoOrder);

        System.out.println("\nSample shortest paths in condensed graph:");
        for (int target = 1; target < condensedGraph.getNumVertices(); target++) {
            double dist = pathResult.getDistance(target);
            if (dist != Double.POSITIVE_INFINITY) {
                List<Integer> path = pathResult.getPath(target);
                System.out.println("  To SCC " + target + ": distance=" + dist + ", path=" + path);
            }
        }
    }

    private static String formatVertexNames(List<Integer> vertices) {
        if (vertices == null || vertices.isEmpty())
            return "[]";

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vertices.size(); i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(getVertexName(vertices.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    private static String formatPath(List<Integer> path) {
        if (path == null || path.isEmpty())
            return "No path";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            if (i > 0)
                sb.append(" -> ");
            sb.append(getVertexName(path.get(i)));
        }
        return sb.toString();
    }

    private static String getVertexName(int vertex) {
        return String.valueOf((char) ('A' + vertex));
    }
}
