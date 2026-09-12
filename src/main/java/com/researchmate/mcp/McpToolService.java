package com.researchmate.mcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.researchmate.mcp.model.ArxivPaper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class McpToolService {

    private final SyncMcpToolCallbackProvider toolCallbackProvider;
    private final ObjectMapper objectMapper;

    public List<ArxivPaper> searchArxiv(String query) {

        for (ToolCallback toolCallback : toolCallbackProvider.getToolCallbacks()) {

            System.out.println(
                    "Discovered MCP tool: "
                            + toolCallback.getToolDefinition().name()
            );

            if (toolCallback.getToolDefinition().name().equals("searchArXiv")) {

                String rawResponse = toolCallback.call(
                        "{\"query\":\"" + query.replace("\"", "\\\"") + "\"}"
                );

                return parseMcpResponse(rawResponse);
            }
        }

        throw new IllegalStateException("searchArXiv MCP tool not found");
    }

    private List<ArxivPaper> parseMcpResponse(String rawResponse) {

        try {
            JsonNode root = objectMapper.readTree(rawResponse);

            List<ArxivPaper> papers = new ArrayList<>();

            for (JsonNode item : root) {

                if (!item.has("text")) {
                    continue;
                }

                String text = item.get("text").asText();

                JsonNode paperArray = objectMapper.readTree(text);

                for (JsonNode paperNode : paperArray) {
                    ArxivPaper paper = objectMapper.treeToValue(
                            paperNode,
                            ArxivPaper.class
                    );

                    papers.add(paper);
                }
            }

            return papers;

        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Unable to parse MCP arXiv response",
                    exception
            );
        }
    }
}