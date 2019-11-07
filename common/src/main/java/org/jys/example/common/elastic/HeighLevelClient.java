package org.jys.example.common.elastic;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

/**
 * @author YueSong Jiang
 * @date 2019/3/21
 * @description <p> </p>
 */
public class HeighLevelClient {

    private RestHighLevelClient client;
    public HeighLevelClient() {

        client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("192.168.14.8", 9200, "http"),
                        new HttpHost("192.168.14.10", 9200, "http"),
                        new HttpHost(new HttpHost("192.168.14.12", 9200, "http"))));
    }
}
