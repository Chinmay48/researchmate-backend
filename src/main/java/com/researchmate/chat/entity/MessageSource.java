package com.researchmate.chat.entity;

import com.researchmate.paperdocument.entity.PaperChunk;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "message_sources",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_message_chunk",
                        columnNames = {"message_id", "chunk_id"}
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id", nullable = false)
    private ChatMessage message;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chunk_id", nullable = false)
    private PaperChunk chunk;

    @Column(name = "retrieval_distance")
    private Double retrievalDistance;
}