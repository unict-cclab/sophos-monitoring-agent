package it.unict.appgroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.fabric8.kubernetes.api.model.Pod;

public class AppGroupGraph {
    private final List<App> apps;

    AppGroupGraph() {
        apps = new ArrayList<>();
    }

    public List<App> getApps() {
        return apps;
    }

    public void addApp(List<Pod> pods, double cpuUsage, double memoryUsage, Map<String, Double> rps, Map<String, Double> traffic) {
        Set<String> keySet = new HashSet<>(rps.keySet());
        keySet.addAll(traffic.keySet());

        List<String> keyList = new ArrayList<>();
        keyList.addAll(keySet);

        List<Channel> channels = new ArrayList<>();

        for(String key: keyList) {
            double channelRps = 0.0;
            double channelTraffic = 0.0;

            if(rps.containsKey(key)) {
                channelRps = rps.get(key);
            }

            if(traffic.containsKey(key)) {
                channelTraffic = traffic.get(key);
            }

            channels.add(new Channel(key, channelRps, channelTraffic));
        }

        apps.add(new App(pods, cpuUsage, memoryUsage, channels));
    }
}