package it.unict.appgroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apps.Deployment;
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

    List<Deployment> deployments = client.apps().deployments().inNamespace(resource.getSpec().getNamespace())
      .withLabel("app-group", resource.getSpec().getName()).list().getItems();

    AppGroupGraph appGroupGraph = appGroupGraphBuilder.buildAppGroupGraph(resource.getSpec().getName(), deployments, resource.getSpec().getMetricsRangeWidth());

    if (resource.getSpec().isResourceMonitorEnabled()) {
      log.info("Updating resource params for app group {}", resource.getSpec().getName());
      resourceMonitor.updateParams(appGroupGraph);
    }

    if (resource.getSpec().isNetworkMonitorEnabled()) {
      log.info("Updating network params for app group {}", resource.getSpec().getName());
      networkMonitor.updateParams(appGroupGraph);
    }

    appGroupGraph.getApps().forEach(app -> {
      Deployment deployment = app.getDeployment();
      client.apps().deployments().inNamespace(deployment.getMetadata().getNamespace())
        .withName(deployment.getMetadata().getName()).patch(deployment);
    });

    return UpdateControl.<AppGroup>noUpdate().rescheduleAfter(resource.getSpec().getRunPeriod(), TimeUnit.SECONDS);
  }
}
