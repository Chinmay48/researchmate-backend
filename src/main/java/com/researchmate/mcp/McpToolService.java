package com.researchmate.mcp;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class McpToolService {

    private final SyncMcpToolCallbackProvider toolCallbackProvider;

    public String searchArxiv(String query) {

        for (ToolCallback toolCallback : toolCallbackProvider.getToolCallbacks()) {

            System.out.println(
                    "Discovered MCP tool: "
                            + toolCallback.getToolDefinition().name()
            );

            if (toolCallback.getToolDefinition().name().equals("searchArXiv")) {

                return toolCallback.call(
                        "{\"query\":\"" + query.replace("\"", "\\\"") + "\"}"
                );
            }
        }

        throw new IllegalStateException("searchArxiv MCP tool not found");
    }
}