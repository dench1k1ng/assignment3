package com.smartcity.graph.dagsp;

import java.util.List;

public class PathResult {

    private final double[] distances;
    private final int[] predecessors;
    private final int source;
    private final boolean isLongestPath;

    public PathResult(double[] distances, int[] predecessors, int source, boolean isLongestPath) {
        this.distances = distances.clone();
        this.predecessors = predecessors.clone();
        this.source = source;
        this.isLongestPath = isLongestPath;
    }

    public double getDistance(int vertex) {
        return distances[vertex];
    }

    public double[] getDistances() {
        return distances.clone();
    }

    public boolean isReachable(int vertex) {
        return !Double.isInfinite(distances[vertex]);
    }

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

    public int getSource() {
        return source;
    }

    public boolean isLongestPath() {
        return isLongestPath;
    }

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

    public List<Integer> getCriticalPath() {
        int target = getCriticalPathTarget();
        return target != -1 ? getPath(target) : null;
    }

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
