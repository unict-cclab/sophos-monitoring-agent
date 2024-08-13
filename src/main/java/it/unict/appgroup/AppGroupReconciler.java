package it.unict.appgroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppGroupReconciler implements Reconciler<AppGroup> {

  private static final Logger log = LoggerFactory.getLogger(AppGroupReconciler.class);

  private final KubernetesClient client;

  @Inject
  AppGroupGraphBuilder appGroupGraphBuilder;

  @Inject
  ResourceMonitor resourceMonitor;

  @Inject
  NetworkMonitor networkMonitor;

  public AppGroupReconciler(KubernetesClient client) {
    this.client = client;
  }

  @Override
  public UpdateControl<AppGroup> reconcile(AppGroup resource, Context context) {
    log.info("Reconciling app group {}", resource.getSpec().getName());

    List<Pod> pods = client.pods().inNamespace(resource.getSpec().getNamespace())
    .withLabel("app-group", resource.getSpec().getName()).list().getItems();

    Map<String, List<Pod>> podsMap = new HashMap<>();

    pods.forEach(pod -> {
      String appName = pod.getMetadata().getLabels().get("app");
      if (!podsMap.containsKey(appName)) {
        podsMap.put(appName, new ArrayList<>());
      }
      podsMap.get(appName).add(pod);
    });

    AppGroupGraph appGroupGraph = appGroupGraphBuilder.buildAppGroupGraph(resource.getSpec().getName(), podsMap,
        resource.getSpec().getMetricsRangeWidth());

    if (resource.getSpec().isResourceMonitorEnabled()) {
      log.info("Updating resource params for app group {}", resource.getSpec().getName());
      resourceMonitor.updateParams(appGroupGraph);
    }

    if (resource.getSpec().isNetworkMonitorEnabled()) {
      log.info("Updating network params for app group {}", resource.getSpec().getName());
      networkMonitor.updateParams(appGroupGraph);
    }

    appGroupGraph.getApps().forEach(app -> {
      app.getPods().forEach(pod -> {
        client.pods().inNamespace(pod.getMetadata().getNamespace())
          .withName(pod.getMetadata().getName()).patch(pod);
      });
    });

    return UpdateControl.<AppGroup>noUpdate().rescheduleAfter(resource.getSpec().getRunPeriod(), TimeUnit.SECONDS);
  }
}
