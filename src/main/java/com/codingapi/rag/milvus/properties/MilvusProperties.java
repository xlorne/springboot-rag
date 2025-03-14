package com.codingapi.rag.milvus.properties;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MilvusProperties {

    private String host = "localhost";
    private int port = 19530;
    private String collectionName = "rag";
    private int dimension = 1024;
    private int documentMaxLength = 1000;

    public String getUri() {
        return String.format("http://%s:%d", host, port);
    }
}
