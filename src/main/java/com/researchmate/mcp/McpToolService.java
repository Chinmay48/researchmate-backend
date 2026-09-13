package com.researchmate.mcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.researchmate.exception.ExternalServiceException;
import com.researchmate.mcp.model.AcademicPaper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.execution.ToolExecutionException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class McpToolService {

    private final SyncMcpToolCallbackProvider toolCallbackProvider;
    private final ObjectMapper objectMapper;

    public List<AcademicPaper> searchArxiv(String query) {

        for (ToolCallback toolCallback :
                toolCallbackProvider.getToolCallbacks()) {

            System.out.println(
                    "Discovered MCP tool: "
                            + toolCallback.getToolDefinition().name()
            );

            if (toolCallback.getToolDefinition()
                    .name()
                    .equals("searchArXiv")) {

                try {

                    String rawResponse = toolCallback.call(
                            "{\"query\":\""
                                    + query.replace("\"", "\\\"")
                                    + "\"}"
                    );

                    return parseMcpResponse(rawResponse);

                } catch (ToolExecutionException exception) {

                    System.err.println(
                            "MCP tool execution failed: "
                                    + exception.getMessage()
                    );

                    exception.printStackTrace();

                    String errorMessage = exception.getMessage();

                    if (errorMessage != null
                            && errorMessage.contains("429")) {

                        throw new ExternalServiceException(
                                "The academic paper service is rate-limited. Please try again later.",
                                HttpStatus.TOO_MANY_REQUESTS,
                                exception
                        );
                    }

                    if (errorMessage != null
                            && errorMessage.contains("503")) {

                        throw new ExternalServiceException(
                                "The academic paper service is temporarily unavailable. Please try again later.",
                                HttpStatus.SERVICE_UNAVAILABLE,
                                exception
                        );
                    }

                    throw new ExternalServiceException(
                            "The academic paper service could not process the request.",
                            HttpStatus.BAD_GATEWAY,
                            exception
                    );
                }
            }
        }

        throw new ExternalServiceException(
                "The searchArXiv MCP tool is unavailable.",
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }

    private List<AcademicPaper> parseMcpResponse(
            String rawResponse
    ) {

        try {

            JsonNode root = objectMapper.readTree(rawResponse);

            List<AcademicPaper> papers = new ArrayList<>();

            for (JsonNode item : root) {

                if (!item.has("text")) {
                    continue;
                }

                String text = item.get("text").asText();

                JsonNode paperArray =
                        objectMapper.readTree(text);

                for (JsonNode paperNode : paperArray) {

                    AcademicPaper paper =
                            objectMapper.treeToValue(
                                    paperNode,
                                    AcademicPaper.class
                            );

                    papers.add(paper);
                }
            }

            return papers;

        } catch (JsonProcessingException exception) {

            throw new IllegalStateException(
                    "Unable to parse MCP academic paper response",
                    exception
            );
        }
    }
}