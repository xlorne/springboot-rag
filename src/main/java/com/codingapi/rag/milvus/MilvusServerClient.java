package com.codingapi.rag.milvus;

import com.codingapi.rag.milvus.properties.MilvusProperties;
import com.google.gson.JsonObject;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.DropCollectionReq;
import io.milvus.v2.service.vector.request.*;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.request.data.SparseFloatVec;
import io.milvus.v2.service.vector.request.ranker.BaseRanker;
import io.milvus.v2.service.vector.request.ranker.WeightedRanker;
import io.milvus.v2.service.vector.response.DeleteResp;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.SearchResp;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.Filter;

import java.util.*;
import java.util.stream.Collectors;

public class MilvusServerClient {

    private final MilvusClientV2 milvusClient;
    private final MilvusProperties milvusProperties;
    private final BaseRanker reranker = new WeightedRanker(Arrays.asList(0.9f,0.1f));
    private CreateCollectionReq.CollectionSchema schema;

    public MilvusServerClient(MilvusProperties milvusProperties) {
        this.milvusClient = new MilvusClientV2(ConnectConfig.builder()
                .uri(milvusProperties.getUri())
                .build());
        this.milvusProperties = milvusProperties;

        this.initSchema();

        this.dropCollection();
        this.initCollection();
    }

    private void initSchema() {
        this.schema = milvusClient.createSchema();
        schema.addField(AddFieldReq.builder()
                .fieldName("id")
                .dataType(DataType.Int64)
                .isPrimaryKey(true)
                .autoID(false)
                .build());

        schema.addField(AddFieldReq.builder()
                .fieldName("text")
                .dataType(DataType.VarChar)
                .maxLength(milvusProperties.getDocumentMaxLength())
                .build());

        schema.addField(AddFieldReq.builder()
                .fieldName("dense")
                .dataType(DataType.FloatVector)
                .dimension(milvusProperties.getDimension())
                .build());

        schema.addField(AddFieldReq.builder()
                .fieldName("sparse")
                .dataType(DataType.SparseFloatVector)
                .build());

    }

    private void initCollection() {
        Map<String, Object> denseParams = new HashMap<>();
        denseParams.put("nlist", 128);
        IndexParam indexParamForDenseField = IndexParam.builder()
                .fieldName("dense")
                .indexName("dense_index")
                .indexType(IndexParam.IndexType.IVF_FLAT)
                .metricType(IndexParam.MetricType.IP)
                .extraParams(denseParams)
                .build();

        Map<String, Object> sparseParams = new HashMap<>();
        sparseParams.put("inverted_index_algo", "DAAT_MAXSCORE");
        IndexParam indexParamForSparseField = IndexParam.builder()
                .fieldName("sparse")
                .indexName("sparse_index")
                .indexType(IndexParam.IndexType.SPARSE_INVERTED_INDEX)
                .metricType(IndexParam.MetricType.IP)
                .extraParams(sparseParams)
                .build();

        List<IndexParam> indexParams = new ArrayList<>();
        indexParams.add(indexParamForDenseField);
        indexParams.add(indexParamForSparseField);

        CreateCollectionReq createCollectionReq = CreateCollectionReq.builder()
                .collectionName(milvusProperties.getCollectionName())
                .collectionSchema(schema)
                .indexParams(indexParams)
                .build();
        milvusClient.createCollection(createCollectionReq);
    }

    public void insert(List<JsonObject> data) {
        InsertReq insertReq = InsertReq.builder()
                .collectionName(milvusProperties.getCollectionName())
                .data(data)
                .build();
        InsertResp insertResp = milvusClient.insert(insertReq);
        System.out.println("size:"+insertResp.getPrimaryKeys().size());
    }

    public void dropCollection() {
        DropCollectionReq dropCollectionReq = DropCollectionReq.builder()
                .collectionName(milvusProperties.getCollectionName())
                .build();

        milvusClient.dropCollection(dropCollectionReq);
    }

    public void delete(List<String> ids) {
        DeleteResp deleteResp = milvusClient.delete(DeleteReq.builder()
                .collectionName(milvusProperties.getCollectionName())
                .ids(Arrays.asList(ids.toArray()))
                .build());
    }

    public void delete(Filter.Expression filterExpression) {
        DeleteResp deleteResp = milvusClient.delete(DeleteReq.builder()
                .collectionName(milvusProperties.getCollectionName())
                .filter(filterExpression.toString())
                .build());
    }

    public List<Document> hybridSearch(String query, EmbeddingModel embeddingModel) {
        // Get dense vectors from the query using embedding model
        float[] queryDenseVectors = embeddingModel.embed(query);

        // Get sparse vectors from the query (assuming we have a sparse embedding model)
        float[] querySparseVectors = embeddingModel.embed(query);

        // Convert dense vector to sparse vector format
        // Only keep non-zero values and their indices
        SortedMap<Long, Float> sparseMap = new TreeMap<>();
        for (int i = 0; i < querySparseVectors.length; i++) {
            sparseMap.put((long) i, Math.max(0, querySparseVectors[i]));
        }

        List<AnnSearchReq> searchRequests = new ArrayList<>();
        searchRequests.add(AnnSearchReq.builder()
                .vectorFieldName("dense")
                .vectors(Collections.singletonList(new FloatVec(queryDenseVectors)))
                .metricType(IndexParam.MetricType.IP)
//                .params("{\"nprobe\": 10}")
                .topK(10)
                .build());

        searchRequests.add(AnnSearchReq.builder()
                .vectorFieldName("sparse")
                .vectors(Collections.singletonList(new SparseFloatVec(sparseMap)))
                .metricType(IndexParam.MetricType.IP)
//                .params("{}")
                .topK(10)
                .build());

        HybridSearchReq hybridSearchReq = HybridSearchReq.builder()
                .collectionName(milvusProperties.getCollectionName())
                .searchRequests(searchRequests)
                .ranker(reranker)
                .outFields(Arrays.asList("dense","id","text"))
                .topK(5)
                .consistencyLevel(ConsistencyLevel.BOUNDED)
                .build();


        SearchResp searchResp = milvusClient.hybridSearch(hybridSearchReq);
        return toListDocument(searchResp);
    }


    public List<Document> search(SearchRequest request, EmbeddingModel embeddingModel) {
        float[] queryVectors = embeddingModel.embed(request.getQuery());
        SearchReq searchReq = SearchReq.builder()
                .collectionName(milvusProperties.getCollectionName())
                .data(Collections.singletonList(new FloatVec(queryVectors)))
                .annsField("dense")
                .outputFields(Arrays.asList("dense", "id", "text"))
                .filter(request.getFilterExpression() != null ? request.getFilterExpression().toString() : "")
                .topK(request.getTopK())
                .build();

        SearchResp searchResp = milvusClient.search(searchReq);
        return toListDocument(searchResp);
    }


    private List<Document> toListDocument(SearchResp searchResp) {

        if (searchResp == null || searchResp.getSearchResults() == null || searchResp.getSearchResults().isEmpty()) {
            return List.of();
        }

        return searchResp.getSearchResults()
                .stream()
                .map(result -> {
                    if(result.isEmpty()){
                        return null;
                    }
                    SearchResp.SearchResult searchResult = result.get(0);
                    Map<String,Object> entity =  searchResult.getEntity();
                    return Document.builder()
                            .id(searchResult.getId().toString())
                            .text((String) entity.get("text"))
                            .score(searchResult.getScore().doubleValue())
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

    }

}
