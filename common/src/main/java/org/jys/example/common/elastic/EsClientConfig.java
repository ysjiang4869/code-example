package org.jys.example.common.elastic;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.sniff.SniffOnFailureListener;
import org.elasticsearch.client.sniff.Sniffer;
import org.elasticsearch.client.sniff.SnifferBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * @author YueSong Jiang
 * @date 2019/3/28
 * Elastic high-level client and sniffer config example
 */
@Configuration
@Profile("elastic")
public class EsClientConfig {

    @Value("${elasticsearch.nodes.master}")
    private String[] nodes;

    @Bean(destroyMethod = "close")
    public RestHighLevelClient restHighLevelClient() {

        HttpHost[] hosts = new HttpHost[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            hosts[i] = new HttpHost(nodes[i]);
        }
        return new RestHighLevelClient(
                RestClient.builder(hosts));
    }

    @Bean(destroyMethod = "close")
    public Sniffer sniffer(RestHighLevelClient client) {
        SnifferBuilder snifferBuilder = Sniffer.builder(client.getLowLevelClient()).setSniffIntervalMillis(10000);
        snifferBuilder.setSniffAfterFailureDelayMillis(1000);
        Sniffer sniffer = snifferBuilder.build();
        SniffOnFailureListener sniffOnFailureListener = new SniffOnFailureListener();
        sniffOnFailureListener.setSniffer(sniffer);
        return sniffer;
    }
}
