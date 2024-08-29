package it.unict.appgroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import it.unict.service.TelemetryService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class AppGroupGraphBuilder {

    private static final Logger log = LoggerFactory.getLogger(AppGroupGraphBuilder.class);

    @Inject
    @RestClient
    TelemetryService telemetryService;

    public AppGroupGraph buildAppGroupGraph(String appGroupName, List<Deployment> deployments,
            String metricsRangeWidth) {
        AppGroupGraph appGroupGraph = new AppGroupGraph();

        deployments.forEach(deployment -> {
            String appName = deployment.getMetadata().getLabels().get("app");

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

            double networkBandwidthUsage = 0.0;
            try {
                networkBandwidthUsage = telemetryService.getAppNetworkBandwidthUsage(appGroupName, appName, metricsRangeWidth).await()
                        .indefinitely();
            } catch (Exception e) {
                log.info(e.getMessage());
            }

            double diskBandwidthUsage = 0.0;
            try {
                diskBandwidthUsage = telemetryService.getAppDiskBandwidthUsage(appGroupName, appName, metricsRangeWidth).await()
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

            appGroupGraph.addApp(deployment, cpuUsage, memoryUsage, networkBandwidthUsage, diskBandwidthUsage, rps, traffic);
        });

        return appGroupGraph;
    }
}