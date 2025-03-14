# Springboot RAG

Retrieval Augmented Generation (RAG) is a technique useful to overcome the limitations of large language models that struggle with long-form content, factual accuracy, and context-awareness.

QuestionAnswerAdvisor is a Spring Boot application that provides a service for storing and retrieving vectors. It uses the VectorDB library to store and retrieve vectors.

## Requirements
- JDK17+
- SpringBoot 3.3+
- [Ollama](https://ollama.com/)
- [milvus](https://milvus.io/zh)


## Springboot VectorStore & ETL

VectorStore is a Spring Boot application that provides a service for storing and retrieving vectors. It uses the VectorDB library to store and retrieve vectors.

Document is SpringBoot-AI ETL data model, which is used to store the data of the document. docx, pdf, txt, json types file can be converted to Document object.

![img.png](img.png)


Document fields are as follows:
1. id: Document id
2. text: Document content
3. media: Document media type
4. metadata: Document metadata,that is a map of key-value pairs
5. score: Document score, which is used to simulate the relevance of the document, score default is null, only used in the retrieval process
6. contentFormatter: Document content formatter, Mutable, ephemeral, content to text formatter. Defaults to Document text.

MilvusVectorStore(VectorStore implementation) fields are as follows:
1. doc_id: id of the document 
2. content: content of the document
3. metadata: metadata of the document
4. embedding: document text field embedding data
5. score: score of the document,Mutable, ephemeral, content to text formatter. Defaults to Document text.

## FQA
1. springboot-ai not support rerank search result, so we need to implement it by ourselves.


## References
- [SpringBoot-RAG](https://docs.spring.io/spring-ai/reference/api/retrieval-augmented-generation.html)
- [SpringBoot-ETL](https://docs.spring.io/spring-ai/reference/api/etl-pipeline.html)
- [vector db](https://docs.spring.io/spring-ai/reference/api/vectordbs.html)
