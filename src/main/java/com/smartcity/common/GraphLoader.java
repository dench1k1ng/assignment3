package com.smartcity.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

public class GraphLoader {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static GraphData loadGraphData(String filePath) throws IOException {
        return objectMapper.readValue(new File(filePath), GraphData.class);
    }

    public static Graph loadGraph(String filePath) throws IOException {
        GraphData data = loadGraphData(filePath);
        return data.toGraph();
    }

    public static void saveGraphData(GraphData graphData, String filePath) throws IOException {
        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(new File(filePath), graphData);
    }
}
