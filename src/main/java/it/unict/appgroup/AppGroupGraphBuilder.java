package it.unict.appgroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.fabric8.kubernetes.api.model.Pod;
import it.unict.service.TelemetryService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class AppGroupGraphBuilder {

    private static final Logger log = LoggerFactory.getLogger(AppGroupGraphBuilder.class);

    @Inject
    @RestClient
    TelemetryService telemetryService;

    public AppGroupGraph buildAppGroupGraph(String appGroupName, Map<String, List<Pod>> podsMap,
            String metricsRangeWidth) {
        AppGroupGraph appGroupGraph = new AppGroupGraph();

        podsMap.forEach((appName, pods) -> {
            double cpuUsage = 0.0;
            try {
                cpuUsage = telemetryService.getAppCpuUsage(appGroupName, appName, metricsRangeWidth).await()
                        .indefinitely();
            } catch (Exception e) {
                log.info(e.getMessage());
            }

            double memoryUsage = 0.0;
            try {
                memoryUsage = telemetryService.getAppMemoryUsage(appGroupName, appName, metricsRangeWidth).await()
                        .indefinitely();
            } catch (Exception e) {
                log.info(e.getMessage());
            }

            Map<String, Double> rps = new HashMap<>();
            Map<String, Double> traffic = new HashMap<>();
            try {
                rps = telemetryService.getAppRequestsPerSecond(appGroupName, appName, metricsRangeWidth).await().indefinitely();
                traffic = telemetryService.getAppTraffic(appGroupName, appName, metricsRangeWidth).await().indefinitely();
            } catch (Exception e) {
                log.info(e.getMessage());
            }

            appGroupGraph.addApp(pods, cpuUsage, memoryUsage, rps, traffic);
        });

        return appGroupGraph;
    }
}