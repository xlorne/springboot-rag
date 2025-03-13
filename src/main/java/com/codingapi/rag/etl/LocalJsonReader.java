package com.codingapi.rag.etl;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.JsonReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LocalJsonReader {

    private final Resource resource;

    public LocalJsonReader(@Value("classpath:rag.json") Resource resource) {
        this.resource = resource;
    }

    public List<Document> loadDocument() {
        JsonReader jsonReader = new JsonReader(this.resource,
                jsonMap -> jsonMap,
                "brand", "description");
        return jsonReader.get();
    }
}
