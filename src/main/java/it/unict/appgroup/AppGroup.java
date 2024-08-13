package it.unict.appgroup;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Version("v1alpha1")
@Group("unict.it")
public class AppGroup extends CustomResource<AppGroupSpec, AppGroupStatus> implements Namespaced {
}
