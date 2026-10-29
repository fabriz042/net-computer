package com.netcomp.back_main.common.embedding;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

@Service
public class BedrockEmbeddingService {

    private static final String MODEL_ID = "cohere.embed-multilingual-v3";

    private final BedrockRuntimeClient bedrockRuntimeClient;
    private final JsonMapper jsonMapper;

    public BedrockEmbeddingService(BedrockRuntimeClient bedrockRuntimeClient, JsonMapper jsonMapper) {
        this.bedrockRuntimeClient = bedrockRuntimeClient;
        this.jsonMapper = jsonMapper;
    }

    public List<List<Float>> generateEmbeddings(List<String> texts, String inputType) {
        String requestBody = jsonMapper.writeValueAsString(Map.of(
                "texts", texts,
                "input_type", inputType,
                "truncate", "END"
        ));

        InvokeModelResponse response = bedrockRuntimeClient.invokeModel(InvokeModelRequest.builder()
                .modelId(MODEL_ID)
                .contentType("application/json")
                .accept("application/json")
                .body(SdkBytes.fromUtf8String(requestBody))
                .build());

        CohereEmbeddingResponse embeddingResponse = jsonMapper.readValue(
                response.body().asUtf8String(),
                CohereEmbeddingResponse.class
        );
        return embeddingResponse.embeddings();
    }

    private record CohereEmbeddingResponse(List<List<Float>> embeddings) {
    }
}
