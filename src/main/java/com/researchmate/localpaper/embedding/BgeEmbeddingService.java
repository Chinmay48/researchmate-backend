package com.researchmate.localpaper.embedding;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import org.springframework.stereotype.Service;

import java.nio.LongBuffer;
import java.nio.file.Path;
import java.util.Map;

@Service
public class BgeEmbeddingService {

    private static final String MODEL_PATH =
            "models/bge-small-en-v1.5/model.onnx";

    private static final String TOKENIZER_PATH =
            "models/bge-small-en-v1.5/tokenizer.json";

    private static final int EMBEDDING_DIMENSION = 384;

    private final OrtEnvironment environment;
    private final OrtSession session;
    private final HuggingFaceTokenizer tokenizer;

    public BgeEmbeddingService() {
        try {
            environment = OrtEnvironment.getEnvironment();

            session = environment.createSession(
                    Path.of(MODEL_PATH).toAbsolutePath().toString(),
                    new OrtSession.SessionOptions()
            );

            tokenizer = HuggingFaceTokenizer.builder()
                    .optTokenizerPath(Path.of(TOKENIZER_PATH))
                    .optTruncation(true)
                    .optMaxLength(512)
                    .build();

            System.out.println("BGE embedding model loaded successfully.");
            System.out.println("ONNX inputs: " + session.getInputNames());
            System.out.println("ONNX outputs: " + session.getOutputNames());

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to initialize BGE embedding model", e
            );
        }
    }

    public float[] generateEmbedding(String text) {

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text cannot be empty");
        }

        try {
            Encoding encoding = tokenizer.encode(text);

            long[] inputIds = encoding.getIds();
            long[] attentionMask = encoding.getAttentionMask();
            long[] tokenTypeIds = encoding.getTypeIds();

            long[] shape = {
                    1,
                    inputIds.length
            };

            try (
                    OnnxTensor inputIdsTensor =
                            OnnxTensor.createTensor(
                                    environment,
                                    LongBuffer.wrap(inputIds),
                                    shape
                            );

                    OnnxTensor attentionMaskTensor =
                            OnnxTensor.createTensor(
                                    environment,
                                    LongBuffer.wrap(attentionMask),
                                    shape
                            );

                    OnnxTensor tokenTypeIdsTensor =
                            OnnxTensor.createTensor(
                                    environment,
                                    LongBuffer.wrap(tokenTypeIds),
                                    shape
                            );

                    OrtSession.Result result = session.run(
                            Map.of(
                                    "input_ids", inputIdsTensor,
                                    "attention_mask", attentionMaskTensor,
                                    "token_type_ids", tokenTypeIdsTensor
                            )
                    )
            ) {

                float[][][] lastHiddenState =
                        (float[][][]) result.get(0).getValue();

                /*
                 * BGE-small-en-v1.5:
                 *
                 * output shape:
                 * [batch][sequence_length][384]
                 *
                 * BGE uses CLS pooling followed by
                 * L2 normalization.
                 */
                float[] embedding =
                        lastHiddenState[0][0];

                if (embedding.length != EMBEDDING_DIMENSION) {
                    throw new IllegalStateException(
                            "Unexpected embedding dimension: "
                                    + embedding.length
                    );
                }

                return normalize(embedding);
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to generate BGE embedding", e
            );
        }
    }

    private float[] normalize(float[] vector) {

        double magnitude = 0.0;

        for (float value : vector) {
            magnitude += value * value;
        }

        magnitude = Math.sqrt(magnitude);

        if (magnitude == 0.0) {
            throw new IllegalStateException(
                    "Embedding vector has zero magnitude"
            );
        }

        float[] normalized = new float[vector.length];

        for (int i = 0; i < vector.length; i++) {
            normalized[i] =
                    (float) (vector[i] / magnitude);
        }

        return normalized;
    }
}