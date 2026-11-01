package com.researchmate.paperdocument.projection;

import java.util.UUID;

public interface RetrievedChunk {

    UUID getId();

    UUID getPaperId();

    Integer getChunkIndex();

    Integer getPageNumber();

    String getSectionTitle();

    String getContent();

    Double getDistance();
}