package com.researchmate.mcp;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class McpTestController {

    private final McpToolService mcpToolService;

    @GetMapping("/api/mcp/test")
    public String testArxivTool(
            @RequestParam String query
    ) {
        return mcpToolService.searchArxiv(query);
    }
}