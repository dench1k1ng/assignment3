package com.smartcity.graph.dagsp;

import java.util.List;

/**
 * Result of shortest/longest path computation on a DAG.
 * Contains distances and reconstructed paths.
 */
public class PathResult {

    private final double[] distances;
    private final int[] predecessors;
    private final int source;
    private final boolean isLongestPath;

    /**
     * Constructor for path result.
     * 
     * @param distances     array of distances from source
     * @param predecessors  array of predecessors for path reconstruction
     * @param source        the source vertex
     * @param isLongestPath true if this represents longest paths, false for
     *                      shortest
     */
    public PathResult(double[] distances, int[] predecessors, int source, boolean isLongestPath) {
        this.distances = distances.clone();
        this.predecessors = predecessors.clone();
        this.source = source;
        this.isLongestPath = isLongestPath;
    }

    /**
     * Get distance to a vertex.
     * 
     * @param vertex the target vertex
     * @return distance from source to vertex
     */
    public double getDistance(int vertex) {
        return distances[vertex];
    }

    /**
     * Get all distances.
     * 
     * @return array of distances
     */
    public double[] getDistances() {
        return distances.clone();
    }

    /**
     * Check if a vertex is reachable from source.
     * 
     * @param vertex the target vertex
     * @return true if reachable
     */
    public boolean isReachable(int vertex) {
        return !Double.isInfinite(distances[vertex]);
    }

    /**
     * Reconstruct path from source to target.
     * 
     * @param target the target vertex
     * @return path as list of vertices, or null if not reachable
     */
    public List<Integer> getPath(int target) {
        if (!isReachable(target)) {
            return null;
        }

        List<Integer> path = new java.util.ArrayList<>();
        int current = target;

        while (current != -1) {
            path.add(current);
            current = predecessors[current];
        }

        java.util.Collections.reverse(path);
        return path;
    }

    /**
     * Get the source vertex.
     * 
     * @return source vertex
     */
    public int getSource() {
        return source;
    }

    /**
     * Check if this represents longest paths.
     * 
     * @return true if longest paths, false if shortest
     */
    public boolean isLongestPath() {
        return isLongestPath;
    }

    /**
     * Find the critical path (longest path ending at any vertex).
     * 
     * @return target vertex of the critical path
     */
    public int getCriticalPathTarget() {
        if (!isLongestPath) {
            throw new IllegalStateException("Critical path only available for longest path results");
        }

        int criticalTarget = -1;
        double maxDistance = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < distances.length; i++) {
            if (isReachable(i) && distances[i] > maxDistance) {
                maxDistance = distances[i];
                criticalTarget = i;
            }
        }

        return criticalTarget;
    }

    /**
     * Get the critical path (longest path in the DAG).
     * 
     * @return critical path as list of vertices
     */
    public List<Integer> getCriticalPath() {
        int target = getCriticalPathTarget();
        return target != -1 ? getPath(target) : null;
    }

    /**
     * Get the critical path length.
     * 
     * @return length of the critical path
     */
    public double getCriticalPathLength() {
        int target = getCriticalPathTarget();
        return target != -1 ? distances[target] : 0.0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("=== %s Path Result (source: %d) ===\n",
                isLongestPath ? "Longest" : "Shortest", source));

        for (int i = 0; i < distances.length; i++) {
            if (isReachable(i)) {
                sb.append(String.format("Vertex %d: distance = %.2f, path = %s\n",
                        i, distances[i], getPath(i)));
            } else {
                sb.append(String.format("Vertex %d: unreachable\n", i));
            }
        }

        if (isLongestPath) {
            List<Integer> criticalPath = getCriticalPath();
            if (criticalPath != null) {
                sb.append(String.format("Critical path: %s (length: %.2f)\n",
                        criticalPath, getCriticalPathLength()));
            }
        }

        return sb.toString();
    }
}
