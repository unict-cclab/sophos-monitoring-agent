package it.unict.appgroup;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.api.model.Pod;

public class App {

    private final List<Pod> pods;

    private final double cpuUsage;

    private final double memoryUsage;

    private final List<Channel> channels;

    public App(List<Pod> pods, double cpuUsage, double memoryUsage, List<Channel> channels) {
        this.pods = pods;
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
        this.channels = channels;
    }

    public List<Pod> getPods() {
        return pods;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public double getMemoryUsage() {
        return memoryUsage;
    }

    public Map<String, Double> getRequestsPerSecond() {
        return channels.stream()
                .collect(Collectors.toMap(Channel::getPeerAppName, Channel::getRequestsPerSecond, (v1, v2) -> v1));
    }

    public Map<String, Double> getTraffic() {
        return channels.stream()
                .collect(Collectors.toMap(Channel::getPeerAppName, Channel::getTraffic, (v1, v2) -> v1));
    }
}