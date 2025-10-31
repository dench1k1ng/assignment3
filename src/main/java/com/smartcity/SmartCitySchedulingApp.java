package com.smartcity;

import com.smartcity.common.*;
import com.smartcity.graph.scc.*;
import com.smartcity.graph.topo.*;
import com.smartcity.graph.dagsp.*;

import java.util.*;

/**
 * Main application demonstrating Smart City/Smart Campus Scheduling with
 * Strongly Connected Components (SCC) detection, Topological Sort, and DAG
 * Shortest Paths.
 */
public class SmartCitySchedulingApp {

    public static void main(String[] args) {
        System.out.println("=== Smart City/Smart Campus Scheduling System ===\n");

        // Demonstrate complete workflow
        demonstrateSimpleDAG();
        System.out.println("\n" + "=".repeat(60) + "\n");

        demonstrateMixedGraph();
        System.out.println("\n" + "=".repeat(60) + "\n");

        demonstrateComplexWorkflow();
    }

    /**
     * Demonstrates scheduling on a pure DAG (no cycles)
     */
    private static void demonstrateSimpleDAG() {
        System.out.println("1. SIMPLE DAG SCHEDULING EXAMPLE");
        System.out.println("Tasks: A->B, A->C, B->D, C->D");
        System.out.println("Weights represent task durations (minutes)\n");

        // Create a simple task dependency graph
        Graph graph = new Graph(4, true); // 4 vertices, directed
        graph.addEdge(0, 1, 10); // A->B (10 minutes)
        graph.addEdge(0, 2, 15); // A->C (15 minutes)
        graph.addEdge(1, 3, 20); // B->D (20 minutes)
        graph.addEdge(2, 3, 25); // C->D (25 minutes)

        System.out.println("Graph structure:");
        System.out.println("A(0) --(10)--> B(1) --(20)--> D(3)");
        System.out.println("A(0) --(15)--> C(2) --(25)--> D(3)");

        // Check if it's already a DAG (no cycles)
        MetricsImpl metrics = new MetricsImpl();
        TarjanSCC sccDetector = new TarjanSCC(graph, metrics);
        SCCResult sccResult = sccDetector.findSCC();

        System.out.println("\nSCC Analysis:");
        System.out.println("Number of SCCs: " + sccResult.getNumComponents());
        boolean isDAG = sccResult.getNumComponents() == graph.getNumVertices();
        System.out.println("Is DAG: " + isDAG);

        if (isDAG) {
            // Direct topological sort
            KahnTopologicalSort topoSort = new KahnTopologicalSort();
            List<Integer> order = topoSort.topologicalSort(graph, metrics);

            System.out.println("Topological Order: " + formatVertexNames(order));

            // Find shortest/longest paths (critical path)
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

    /**
     * Demonstrates handling a graph with cycles (requires SCC condensation)
     */
    private static void demonstrateMixedGraph() {
        System.out.println("2. MIXED GRAPH WITH CYCLES EXAMPLE");
        System.out.println("Tasks with circular dependencies that need resolution\n");

        // Create a graph with cycles
        Graph graph = new Graph(5, true); // 5 vertices, directed
        graph.addEdge(0, 1, 10); // A->B
        graph.addEdge(1, 2, 15); // B->C
        graph.addEdge(2, 1, 5); // C->B (cycle!)
        graph.addEdge(1, 3, 20); // B->D
        graph.addEdge(3, 4, 25); // D->E
        graph.addEdge(0, 4, 50); // A->E (alternative path)

        System.out.println("Graph structure (with cycle B<->C):");
        System.out.println("A(0) --(10)--> B(1) --(15)--> C(2)");
        System.out.println("               ^               |");
        System.out.println("               +-------(5)-----+");
        System.out.println("B(1) --(20)--> D(3) --(25)--> E(4)");
        System.out.println("A(0) ----------(50)---------> E(4)");

        // Detect SCCs
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

        // Create condensed DAG
        SCCCondensation condensation = new SCCCondensation(graph, sccResult);
        Graph condensedGraph = condensation.buildCondensationDAG();

        System.out.println("\nCondensed DAG:");
        System.out.println("Condensed graph has " + condensedGraph.getNumVertices() + " super-nodes");

        // Get topological order of condensed graph
        MetricsImpl topoMetrics = new MetricsImpl();
        SCCTopologicalOrder sccTopo = new SCCTopologicalOrder(graph, topoMetrics);
        sccTopo.computeOrder();
        List<Integer> condensedOrder = sccTopo.getComponentOrder();

        System.out.println("Processing order of SCCs: " + condensedOrder);

        // Find shortest paths on condensed graph
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

    /**
     * Demonstrates a complex workflow with metrics
     */
    private static void demonstrateComplexWorkflow() {
        System.out.println("3. COMPLEX WORKFLOW WITH PERFORMANCE METRICS");
        System.out.println("Large task dependency graph analysis\n");

        // Create a larger graph with multiple components
        Graph graph = new Graph(8, true); // 8 vertices, directed

        // Component 1: Tasks A, B, C with cycle
        graph.addEdge(0, 1, 10); // A->B
        graph.addEdge(1, 2, 15); // B->C
        graph.addEdge(2, 0, 20); // C->A (cycle)

        // Component 2: Tasks D, E (simple chain)
        graph.addEdge(3, 4, 25); // D->E

        // Component 3: Tasks F, G, H with dependencies
        graph.addEdge(5, 6, 30); // F->G
        graph.addEdge(6, 7, 35); // G->H
        graph.addEdge(5, 7, 40); // F->H (alternative)

        // Cross-component dependencies
        graph.addEdge(1, 3, 12); // B->D
        graph.addEdge(4, 6, 18); // E->G

        System.out.println("Complex graph with 8 tasks and multiple dependencies");

        // Full analysis with metrics
        MetricsImpl metrics = new MetricsImpl();

        long startTime = System.nanoTime();

        // SCC Detection
        TarjanSCC sccDetector = new TarjanSCC(graph, metrics);
        SCCResult sccResult = sccDetector.findSCC();

        long sccTime = System.nanoTime();

        // Condensation
        SCCCondensation condensation = new SCCCondensation(graph, sccResult);
        Graph condensedGraph = condensation.buildCondensationDAG();

        long condensationTime = System.nanoTime();

        // Topological ordering
        MetricsImpl topoMetrics = new MetricsImpl();
        SCCTopologicalOrder sccTopo = new SCCTopologicalOrder(graph, topoMetrics);
        sccTopo.computeOrder();
        List<Integer> topoOrder = sccTopo.getComponentOrder();

        long topoTime = System.nanoTime();

        // Shortest paths
        MetricsImpl pathMetrics = new MetricsImpl();
        DAGShortestPath shortestPath = new DAGShortestPath(condensedGraph, pathMetrics);
        PathResult pathResult = shortestPath.findShortestPaths(0);

        long pathTime = System.nanoTime();

        // Display results
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

        // Show some path information
        System.out.println("\nSample shortest paths in condensed graph:");
        for (int target = 1; target < condensedGraph.getNumVertices(); target++) {
            double dist = pathResult.getDistance(target);
            if (dist != Double.POSITIVE_INFINITY) {
                List<Integer> path = pathResult.getPath(target);
                System.out.println("  To SCC " + target + ": distance=" + dist + ", path=" + path);
            }
        }
    }

    /**
     * Utility method to format vertex names
     */
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

    /**
     * Utility method to format a path
     */
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

    /**
     * Convert vertex number to readable name
     */
    private static String getVertexName(int vertex) {
        return String.valueOf((char) ('A' + vertex));
    }
}
