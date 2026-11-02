package com.researchmate.ai.service;

import com.researchmate.sessionpaper.dto.SessionRagChunkResponse;
import com.researchmate.ai.enums.AnswerMode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RagPromptBuilder {

    public String buildPrompt(
            String question,
            AnswerMode answerMode,
            List<SessionRagChunkResponse> chunks) {

        StringBuilder prompt = new StringBuilder();

        prompt.append("""
                You are ResearchMate, an AI research assistant.

                Your responsibility is to provide accurate, clear,
                well-structured and academically useful answers.

                Follow the instructions for the selected answer mode strictly.

                """);

        if (answerMode == AnswerMode.RESEARCH_GROUNDED) {

            prompt.append("""
                    ANSWER MODE: RESEARCH GROUNDED

                    STRICT RULES:
                    1. Answer using ONLY the supplied research context.
                    2. Do not use your general knowledge to fill missing information.
                    3. Do not invent facts, statistics, findings or citations.
                    4. If the supplied context does not contain sufficient
                       information, explicitly say so.
                    5. Cite supporting claims using the exact source ID
                       and page number provided in the context.
                    6. Clearly distinguish between what the research states
                       and what cannot be established from the sources.

                    """);

        } else if (answerMode == AnswerMode.AI_ENHANCED) {

            prompt.append("""
                    ANSWER MODE: AI ENHANCED

                    RULES:
                    1. Use relevant research context wherever available.
                    2. You may use your general knowledge to explain concepts,
                       provide background and fill knowledge gaps.
                    3. Never present general AI knowledge as a finding
                       from a supplied research paper.
                    4. Cite research-supported claims using the exact
                       source ID and page number provided.
                    5. Clearly label general knowledge as
                       "AI-generated explanation".
                    6. If research findings conflict with your general
                       knowledge, explicitly mention the discrepancy.
                    7. Never invent citations or research findings.

                    """);
        }

        prompt.append("""
                RESPONSE FORMATTING:
                - Use clear headings where appropriate.
                - Explain technical concepts in simple language.
                - Use bullet points when they improve readability.
                - Include citations close to the claims they support.
                - Never fabricate source references.

                RESEARCH CONTEXT:

                """);

        for (SessionRagChunkResponse chunk : chunks) {

            prompt.append("Source ID: ")
                    .append(chunk.paperId())
                    .append("\n");

            prompt.append("Chunk ID: ")
                    .append(chunk.chunkId())
                    .append("\n");

            prompt.append("Page Number: ")
                    .append(chunk.pageNumber())
                    .append("\n");

            prompt.append("Content:\n")
                    .append(chunk.content())
                    .append("\n\n");

            prompt.append("--------------------------------\n\n");
        }

        prompt.append("USER QUESTION:\n")
                .append(question)
                .append("\n\n");

        prompt.append("Now answer the user's question according to the selected mode.");

        return prompt.toString();
    }
}