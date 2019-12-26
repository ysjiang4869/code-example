package org.jys.example.common.elastic;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * @author YueSong Jiang
 * @date 2019/3/21
 * Elastic transport client config and use example
 * TODO add more example
 */
public class TransportClientExample {

    private TransportClient client;

    public TransportClientExample() throws UnknownHostException {
        Settings settings = Settings.builder()
                .put("cluster.name", "elasticsearch-cluster").build();
        client = new PreBuiltTransportClient(settings)
                .addTransportAddress(new TransportAddress(InetAddress.getByName("192.168.14.8"), 9300))
                .addTransportAddress(new TransportAddress(InetAddress.getByName("192.168.14.10"), 9300))
                .addTransportAddress(new TransportAddress(InetAddress.getByName("192.168.14.12"), 9300));
    }

    public void BulkIndex() {
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        try {
            bulkRequest.add(client.prepareIndex("index_name", "_doc", "doc_id")
                    .setSource(jsonBuilder()
                            .startObject()
                            .field("recordid", 1)
                            .field("personid", 2)
                            .endObject()
                    )
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        BulkResponse bulkResponse = bulkRequest.get();
        if (bulkResponse.hasFailures()) {
            // process failures by iterating through each bulk response item
            System.out.println(bulkResponse.buildFailureMessage());
        }
    }
}
