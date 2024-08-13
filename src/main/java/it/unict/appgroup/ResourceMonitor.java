package it.unict.appgroup;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ResourceMonitor {
    public void updateParams(AppGroupGraph appGroupGraph) {
        appGroupGraph.getApps().forEach(app -> {
            app.getPods().forEach(pod -> {
                pod.getMetadata().getAnnotations().put("cpu-usage", String.valueOf(app.getCpuUsage()));
                pod.getMetadata().getAnnotations().put("memory-usage", String.valueOf(app.getMemoryUsage()));
            });
        });
    }
}