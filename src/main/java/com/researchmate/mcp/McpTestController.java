package com.researchmate.mcp;

import com.researchmate.mcp.model.ArxivPaper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class McpTestController {

    private final McpToolService mcpToolService;

    @GetMapping("/api/mcp/test")
    public List<ArxivPaper> testArxivTool(
            @RequestParam String query
    ) {
        return mcpToolService.searchArxiv(query);
    }
}