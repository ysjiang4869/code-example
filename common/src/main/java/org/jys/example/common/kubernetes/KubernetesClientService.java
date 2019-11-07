package org.jys.example.common.kubernetes;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

/**
 * @author YueSong Jiang
 * @date 2019/3/13
 * @description <p> </p>
 */
public class KubernetesClientService {

    private KubernetesClient kubernetesClient;

    public KubernetesClientService() {
        Config config = new ConfigBuilder().withCaCertFile("/var/run/secrets/kubernetes.io/serviceaccount/ca.crt").build();
        kubernetesClient = new DefaultKubernetesClient(config);
    }

    public void addPort(int port) {
        kubernetesClient.services().inNamespace("default").withName("viid-mda").edit()
                .editSpec().addNewPort().withNewNodePort(port).withPort(port).withProtocol("TCP")
                .withName("port-" + port).endPort().endSpec().done();
    }
}
