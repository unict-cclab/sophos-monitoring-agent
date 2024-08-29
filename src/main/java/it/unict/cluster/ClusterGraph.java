package it.unict.cluster;

import io.fabric8.kubernetes.api.model.Node;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClusterGraph {

    private final List<ClusterNode> clusterNodes;

    ClusterGraph() {
        clusterNodes = new ArrayList<>();
    }

    public List<ClusterNode> getClusterNodes() {
        return clusterNodes;
    }

    public void addClusterNode(Node node, String clusterNodeName, double cpuUsage, double memoryUsage, double networkBandwidthUsage, double diskBandwidthUsage, Map<String, Double> latencies) {
        List<Link> links = latencies.entrySet().stream().map((entry) -> new Link(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        clusterNodes.add(new ClusterNode(node, clusterNodeName, cpuUsage, memoryUsage, networkBandwidthUsage, diskBandwidthUsage, links));
    }
}