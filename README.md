# ResearchMate

> An AI-powered academic research workspace for discovering papers,
> organizing research, and asking questions grounded in selected
> academic literature.

ResearchMate combines academic paper discovery, asynchronous PDF
processing, vector-based retrieval, and conversational AI. Users create
research projects, organize work into sessions, discover papers, select
papers independently for each chat, and receive AI-generated answers
with references to retrieved paper chunks.

## Table of Contents

-   [Overview](#overview)
-   [Features](#features)
-   [Application Workflow](#application-workflow)
-   [Architecture](#architecture)
-   [Technology Stack](#technology-stack)
-   [Paper Discovery and Search](#paper-discovery-and-search)
-   [PDF Processing Pipeline](#pdf-processing-pipeline)
-   [Embedding and Vector Retrieval](#embedding-and-vector-retrieval)
-   [Chat-Specific RAG](#chat-specific-rag)
-   [Database Design](#database-design)
-   [API Overview](#api-overview)
-   [Frontend Structure](#frontend-structure)
-   [Security and Reliability](#security-and-reliability)
-   [Getting Started](#getting-started)
-   [Configuration](#configuration)
-   [Future Improvements](#future-improvements)
-   [Screenshots](#screenshots)

## Overview

ResearchMate is a full-stack academic research assistant. It maintains a
local collection of approximately **200,000 academic paper metadata
records**, sourced from the Hugging Face dataset
[`TabMaven/arxiv-raw`](https://huggingface.co/datasets/TabMaven/arxiv-raw/tree/main).
The downloaded metadata is filtered to focus on areas including
Information Technology, Artificial Intelligence, Machine Learning,
Cybersecurity, Internet of Things (IoT), and Blockchain.

Users can search the collection using keyword, semantic, or hybrid
search. Selected papers can be processed asynchronously: PDFs are
downloaded from approved arXiv hosts, text is extracted page by page,
divided into overlapping chunks, embedded using BGE-small-en-v1.5, and
stored in PostgreSQL with pgvector. Chat-specific retrieval supplies
relevant chunks to Gemini for answer generation.

### Core concepts

1.  **Research project** --- a broader research topic or area.
2.  **Research session** --- a paper-discovery activity within a
    project.
3.  **Chat** --- a conversation within a session with its own
    independently selected papers.

This separation allows different chats in the same session to work with
different paper collections.

## Features

-   User registration and login with JWT-based authentication.
-   Create, view, update, and delete research projects.
-   Create and manage research sessions.
-   Search academic metadata using keyword, semantic, and hybrid
    strategies.
-   Filter keyword results by category and publication date.
-   Sort results by relevance, newest, or oldest.
-   Retrieve up to 20 papers for a research session.
-   Process paper PDFs asynchronously.
-   Extract PDF text page by page and preserve page numbers.
-   Generate 384-dimensional BGE embeddings using ONNX Runtime.
-   Store chunk embeddings in PostgreSQL with pgvector.
-   Create multiple chats within a research session.
-   Maintain paper selection independently for each chat.
-   Generate Gemini answers in `RESEARCH_GROUNDED` and `AI_ENHANCED`
    modes.
-   Associate assistant messages with the retrieved source chunks.

## Application Workflow

``` mermaid
flowchart TD
    A[Register or log in] --> B[Create research project]
    B --> C[Create research session]
    C --> D[Discover academic papers]
    D --> E{Search strategy}
    E --> F[Keyword]
    E --> G[Semantic]
    E --> H[Hybrid]
    F --> I[Ranked results]
    G --> I
    H --> I
    I --> J[Explore papers]
    J --> K[Create chat]
    K --> L[Select papers for this chat]
    L --> M[Schedule missing documents]
    M --> N[Download and process PDFs]
    N --> O[Extract, chunk, embed, persist]
    O --> P[Ask a question]
    P --> Q[Retrieve chunks from selected papers]
    Q --> R[Build RAG prompt]
    R --> S[Generate answer with Gemini]
    S --> T[Persist answer and source references]
    T --> U[Display answer and sources]
```

**Selection distinction:** `SessionPaper` represents session-level paper
results/selection; `ChatPaper` represents papers selected for an
individual chat; `MessageSource` represents chunks used for a particular
assistant response. These are different relationships.

## Architecture

``` mermaid
flowchart TB
    subgraph Frontend["Frontend - React and TypeScript"]
      UI[Pages and components]
      CTX[Auth and theme contexts]
      AX[Axios API client]
      UI --> CTX
      UI --> AX
    end
    subgraph Backend["Backend - Spring Boot"]
      SEC[Spring Security and JWT]
      CTRL[REST controllers]
      SVC[Application services]
      SEARCH[Paper search services]
      WORKER[Document worker]
      RAG[RAG retrieval and prompt builder]
      GEM[Gemini service]
      SEC --> CTRL --> SVC
      SVC --> SEARCH
      SVC --> WORKER
      SVC --> RAG --> GEM
    end
    subgraph Data["Data and model runtime"]
      PG[(PostgreSQL)]
      VEC[(pgvector)]
      ONNX[BGE ONNX model]
      TMP[Temporary PDF files]
    end
    AX --> SEC
    SEARCH --> PG
    SEARCH --> VEC
    WORKER --> PG
    WORKER --> ONNX
    WORKER --> TMP
    RAG --> PG
    RAG --> VEC
    GEM --> API[Google Gemini API]
```

### Backend module responsibilities

  -----------------------------------------------------------------------
  Module                              Responsibility
  ----------------------------------- -----------------------------------
  `auth`                              Registration and login

  `security`                          JWT authentication and request
                                      security

  `user`                              User entity and persistence

  `project`                           Research project management

  `session`                           Research session and workspace
                                      management

  `localpaper`                        Metadata import, paper embeddings,
                                      and search

  `sessionpaper`                      Session paper retrieval, selection,
                                      and RAG retrieval

  `paperdocument`                     PDF download, extraction, chunking,
                                      persistence, and worker

  `chat`                              Chat management, history, selected
                                      papers, and message sources

  `ai`                                Prompt construction and Gemini
                                      answer generation

  `paper`                             Research-paper functionality

  `mcp`                               Academic paper provider/model
                                      abstractions and related
                                      integration code
  -----------------------------------------------------------------------

## Technology Stack

### Frontend

  Technology        Purpose
  ----------------- --------------------------------------
  React 19          User interface
  TypeScript 6      Static typing
  Vite 8            Development server and build tooling
  Tailwind CSS v4   Styling
  React Router v7   Client-side routing
  Axios             HTTP communication
  Framer Motion     UI animations
  Lucide React      Icons
  React Toastify    Toast notifications

### Backend

  Technology                     Purpose
  ------------------------------ --------------------------------------
  Java 21                        Backend language
  Spring Boot 4.1.0              Application framework
  Spring Security                Request security
  JWT                            Token-based authentication
  Spring Data JPA / Hibernate    ORM and persistence
  PostgreSQL 18.3                Relational database
  pgvector 0.8.6                 Vector storage and similarity search
  Apache PDFBox                  PDF text extraction
  ONNX Runtime                   Local model inference
  DJL Hugging Face Tokenizers    Tokenization
  Google GenAI Java SDK 1.79.0   Gemini integration
  Gemini 2.5 Flash               Answer generation

### Embedding model

-   Model: `BAAI/bge-small-en-v1.5`
-   Dimension: 384
-   Runtime: ONNX Runtime
-   Tokenizer: Hugging Face tokenizer JSON
-   Maximum tokenized sequence length: 512
-   Pooling in the supplied implementation: first-token/CLS
    representation (`lastHiddenState[0][0]`)
-   Normalization: L2 normalization

## Paper Discovery and Search

### Metadata ingestion

The local metadata collection is based on the Hugging Face dataset
[`TabMaven/arxiv-raw`](https://huggingface.co/datasets/TabMaven/arxiv-raw/tree/main).
Metadata is downloaded and filtered for the technical subject areas
listed in the Overview. Approximately 200,000 records are maintained in
`local_papers`.

The importer classes are `LocalPaperImportRunner` and
`LocalPaperImportService`.

### Keyword search

Keyword search uses PostgreSQL full-text search:

-   `websearch_to_tsquery('english', :query)` parses the search query.
-   `search_vector @@ query` matches terms.
-   `ts_rank(..., 32)` calculates relevance.
-   Title relevance receives an additional multiplier of `2.0`.
-   `ts_headline()` generates highlighted title and abstract fragments.

The search supports category and publication-date filters, plus
relevance/newest/oldest sorting. The provided schema confirms a
`tsvector` column named `search_vector`; the actual index definitions
were not supplied, so this README does not assume a GIN index.

### Semantic search

The query is embedded using BGE-small-en-v1.5. PostgreSQL compares it to
stored paper vectors using pgvector's cosine-distance operator:

``` sql
embedding <=> CAST(:queryEmbedding AS vector)
```

Similarity is calculated as:

``` text
similarityScore = 1.0 - cosineDistance
```

### Hybrid search

Hybrid search retrieves up to 1,000 semantic candidates and up to 1,000
keyword candidates, combines their paper IDs with `UNION`, ranks each
candidate list, and applies Reciprocal Rank Fusion.

The implementation uses RRF constant **60**, with semantic rank weighted
at **60%** and keyword rank weighted at **40%**:

\[ `\text{HybridScore}`{=tex}(d)=
0.4`\left`{=tex}(`\frac{60}{60+\text{keywordRank}(d)}`{=tex}`\right`{=tex}) +
0.6`\left`{=tex}(`\frac{60}{60+\text{semanticRank}(d)}`{=tex}`\right`{=tex})
\]

A missing rank contributes zero. The session retrieval service requests
the first **20** results and persists them as `SessionPaper` records.

  Search parameter                         Value
  -------------------------------------- -------
  Semantic candidates in hybrid search     1,000
  Keyword candidates in hybrid search      1,000
  RRF constant                                60
  Semantic weight                            0.6
  Keyword weight                             0.4
  Papers retrieved per session                20

The two candidate lists may overlap, so their union can contain fewer
than 2,000 unique papers.

## PDF Processing Pipeline

Paper documents are processed asynchronously by
`PaperDocumentWorkerService`.

### Processing lifecycle

``` mermaid
stateDiagram-v2
    [*] --> PENDING
    PENDING --> PROCESSING: Atomic claim succeeds
    PROCESSING --> COMPLETED: Processing succeeds
    PROCESSING --> PENDING: Failure with attempts remaining
    PROCESSING --> FAILED: Maximum attempts reached
    PROCESSING --> PENDING: Stale job recovered
    PROCESSING --> FAILED: Stale job recovered at attempt limit
```

### Scheduling and polling

`PaperDocumentSchedulingService.scheduleIfNeeded(paperId)` verifies that
the paper exists and inserts a document record if one is absent. Its
native insert uses `ON CONFLICT (paper_id) DO NOTHING`, avoiding
duplicate document records for the same paper.

The worker is enabled through `researchmate.documents.worker-enabled`.
It polls at a configurable fixed delay, default **5,000 ms**. Each poll
attempts stale-job recovery and then selects the oldest pending
document.

### Atomic claiming and concurrency

The repository claims a document with a conditional update:

``` sql
UPDATE paper_documents
SET status = 'PROCESSING',
    attempt_count = attempt_count + 1,
    started_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP,
    last_error = NULL
WHERE id = :documentId
  AND status = 'PENDING';
```

The claim succeeds only when one row is updated. Because the status
condition is part of the database update, competing claim attempts
cannot both successfully transition the same row from `PENDING` to
`PROCESSING`. The worker processes the document only when the repository
returns `1`.

This confirms an **atomic conditional UPDATE** claim mechanism. The
supplied worker code processes one pending document per scheduled
invocation. The number of scheduler threads or application instances is
not specified, so no multi-worker throughput claim is made.

### Failure handling and stale recovery

-   Maximum attempts default to **3**.
-   A processing failure returns the document to `PENDING` while
    attempts remain.
-   Once the attempt limit is reached, the document becomes `FAILED`.
-   A document is considered stale when it remains `PROCESSING` for more
    than **15 minutes**.
-   Stale jobs are returned to `PENDING` if attempts remain, otherwise
    marked `FAILED`.
-   Recovery clears `started_at` and records a diagnostic message in
    `last_error`.
-   Error messages stored from normal failures are capped at 1,000
    characters.

### PDF download safeguards

`PdfDownloadService` downloads PDFs into a configured temporary
directory.

  -----------------------------------------------------------------------
  Safeguard                           Implementation
  ----------------------------------- -----------------------------------
  Allowed hosts                       `arxiv.org`, `export.arxiv.org`

  Protocol                            HTTPS only

  Connection timeout                  10 seconds

  Read timeout                        30 seconds

  Maximum redirects                   3

  Maximum file size                   Configurable

  Signature validation                Checks `%PDF-`

  Temporary-file cleanup              Deletes failed downloads and
                                      removes files after processing
  -----------------------------------------------------------------------

The downloader also validates the URL, rejects user-info and unsupported
ports, checks resolved IP addresses against non-public address ranges,
validates HTTP status and response content type, and enforces the size
limit both from the content-length header and while streaming.

These controls reduce risk from untrusted PDF URLs; they should not be
described as eliminating every possible SSRF or network-security risk.

### PDF extraction

`PdfTextExtractionService` uses Apache PDFBox to extract one page at a
time.

-   Preserves the source page number.
-   Uses positional text sorting.
-   Removes NULL characters.
-   Rejects empty PDFs.
-   Enforces a configurable maximum page count, default **500**.
-   Fails if no extractable text is found.

The supplied code does not show OCR for scanned/image-only PDFs.

### Chunking

`PaperChunkingService` divides each extracted page into chunks.

  Parameter                                                Value
  ------------------- ------------------------------------------
  Chunk size                                    1,000 characters
  Overlap                                         150 characters
  Boundary handling               Prefers a nearby word boundary
  Page metadata                          Preserved on each chunk
  Chunk index           Sequential within the processed document

Chunking occurs within each page. The supplied extraction/chunking code
does not show section-title detection, even though the chunk schema
includes an optional `section_title` field.

### Persistence

Chunks are stored in `paper_chunks` and associated with a
`paper_documents` record. The table includes chunk content, index, page
number, optional section title, and embedding. Downloaded PDFs are
temporary and are deleted by the worker after processing.

## Embedding and Vector Retrieval

`BgeEmbeddingService` loads:

``` text
models/bge-small-en-v1.5/model.onnx
models/bge-small-en-v1.5/tokenizer.json
```

The service tokenizes text with truncation and a maximum length of 512,
creates ONNX tensors for `input_ids`, `attention_mask`, and
`token_type_ids`, and runs the model.

The supplied implementation: 1. Reads the last hidden-state output. 2.
Uses the first token representation (`lastHiddenState[0][0]`). 3.
Verifies the output dimension is 384. 4. Applies L2 normalization. 5.
Rejects a zero-magnitude vector.

The same `generateEmbedding()` method is used for query text. No
separate query prefix or instruction prefix is present in the supplied
code.

Chunk retrieval validates the query, selected paper IDs, and `topK`
(1--50), generates the query vector, and searches chunks using cosine
distance:

``` sql
pc.embedding <=> CAST(:queryEmbedding AS vector)
```

Chat-restricted retrieval additionally filters by selected paper IDs and
requires document status `COMPLETED` and a non-null chunk embedding.

The vector index definition was not supplied; this README therefore does
not claim that HNSW or IVFFlat is configured.

## Chat-Specific RAG

Paper selection is maintained independently for each chat. For example,
one chat may select papers 1, 2, and 3, while another chat in the same
session selects papers 4 and 5.

The `chat_papers` table represents chat-level selection. Before
retrieval, the application checks document readiness and schedules
missing documents where necessary. If no papers are selected, retrieval
returns an informative validation error.

``` mermaid
sequenceDiagram
    actor User
    participant API as Spring Boot API
    participant Chat as Chat services
    participant RAG as Retrieval service
    participant DB as PostgreSQL and pgvector
    participant Gemini as Gemini API

    User->>API: Submit chatId and question
    API->>Chat: Validate chat/session access
    Chat->>DB: Load selected paper IDs
    Chat->>RAG: Retrieve within selected papers
    RAG->>DB: Check processing readiness
    RAG->>DB: Similarity search for chunks
    DB-->>RAG: Ranked chunks
    RAG-->>Chat: Retrieved context
    Chat->>Chat: Save user message and build prompt
    Chat->>Gemini: Generate response
    Gemini-->>Chat: Answer
    Chat->>DB: Save assistant message and source chunks
    API-->>User: Answer and source citations
```

### Message source traceability

`MessageSource` connects an assistant message to the exact chunks used
for that response. This is distinct from `ChatPaper`, which stores the
papers selected for the chat. A selected paper does not have to
contribute a chunk to every individual answer.

## Answer Generation

ResearchMate integrates Gemini through
`com.google.genai:google-genai:1.79.0`.

Known configuration: - Model: `gemini-2.5-flash` - API key environment
variable: `GEMINI_KEY` - Prompt construction: `RagPromptBuilder` -
Answer modes: `RESEARCH_GROUNDED` and `AI_ENHANCED`

The answer service: 1. Validates chat access within the session. 2.
Retrieves chunks restricted to the chat's selected papers. 3. Saves the
user message. 4. Builds a prompt from the question, answer mode, and
retrieved chunks. 5. Calls Gemini. 6. Saves the assistant response and
source-chunk associations. 7. Returns the answer and source citation
metadata.

Source metadata can include paper ID, chunk ID, page number, section
title, and retrieval distance. This provides traceability to retrieved
excerpts; it does not by itself guarantee that every generated statement
is factually supported by those excerpts.

## Database Design

ResearchMate uses PostgreSQL for relational data and pgvector for vector
columns and similarity queries.

### Main tables

  -----------------------------------------------------------------------
  Table                               Responsibility
  ----------------------------------- -----------------------------------
  `users`                             User accounts

  `research_projects`                 User-owned projects

  `research_sessions`                 Research activities within projects

  `local_papers`                      Imported academic metadata and
                                      full-text search vector

  `paper_embeddings`                  Paper-level embeddings for semantic
                                      paper discovery

  `session_papers`                    Session paper results, ranking
                                      scores, and selection state

  `paper_documents`                   PDF processing status, attempts,
                                      and timestamps

  `paper_chunks`                      Extracted chunks and chunk
                                      embeddings

  `chats`                             Conversations within sessions

  `chat_papers`                       Papers selected for an individual
                                      chat

  `chat_messages`                     User and assistant messages

  `message_sources`                   Chunk references associated with
                                      assistant messages

  `research_papers`                   Additional research-paper table
                                      present in the supplied schema
  -----------------------------------------------------------------------

### Entity relationship diagram

``` mermaid
erDiagram
    USERS ||--o{ RESEARCH_PROJECTS : owns
    RESEARCH_PROJECTS ||--o{ RESEARCH_SESSIONS : contains
    RESEARCH_SESSIONS ||--o{ SESSION_PAPERS : has
    LOCAL_PAPERS ||--o{ SESSION_PAPERS : appears_in
    LOCAL_PAPERS ||--o| PAPER_DOCUMENTS : processed_as
    PAPER_DOCUMENTS ||--o{ PAPER_CHUNKS : contains
    RESEARCH_SESSIONS ||--o{ CHATS : contains
    CHATS ||--o{ CHAT_PAPERS : selects
    LOCAL_PAPERS ||--o{ CHAT_PAPERS : selected_for
    CHATS ||--o{ CHAT_MESSAGES : contains
    CHAT_MESSAGES ||--o{ MESSAGE_SOURCES : cites
    PAPER_CHUNKS ||--o{ MESSAGE_SOURCES : referenced_by
    LOCAL_PAPERS ||--o{ PAPER_EMBEDDINGS : embedded_as
```

This is a conceptual overview of the main relationships, not a
substitute for the complete database DDL. The supplied schema output
lists columns and types but does not include all foreign-key
definitions, indexes, or uniqueness constraints.

### Important distinctions

-   `local_papers` contains paper metadata.
-   `paper_embeddings` contains paper-level vectors for semantic
    discovery.
-   `paper_chunks` contains chunk-level vectors for RAG.
-   `session_papers` represents session-level paper results/selection.
-   `chat_papers` represents chat-specific paper selection.
-   `message_sources` represents chunks used for an individual assistant
    response.

### Schema management

The project uses Hibernate/JPA for ORM and schema management. Flyway and
Liquibase are not used.

## API Overview

The following endpoints are based on the implementation details
available for this project. Check controller mappings and DTOs in the
current source before treating this list as an exhaustive API
specification.

### Authentication

  Method   Endpoint               Purpose
  -------- ---------------------- ---------------------
  POST     `/api/auth/register`   Register a user
  POST     `/api/auth/login`      Authenticate a user

### Projects

  Method   Endpoint                      Purpose
  -------- ----------------------------- ---------------------
  POST     `/api/projects`               Create a project
  GET      `/api/projects`               List projects
  GET      `/api/projects/{projectId}`   Get project details
  PUT      `/api/projects/{projectId}`   Update a project
  DELETE   `/api/projects/{projectId}`   Delete a project

### Sessions and workspace

  ------------------------------------------------------------------------------------------------------------
  Method                  Endpoint                                                     Purpose
  ----------------------- ------------------------------------------------------------ -----------------------
  GET                     `/api/projects/{projectId}/sessions/{sessionId}/workspace`   Retrieve session
                                                                                       workspace

  PUT                     `/api/sessions/{sessionId}/papers/selection`                 Update session-level
                                                                                       paper selection

  POST                    `/api/sessions/{sessionId}/rag/retrieve`                     Retrieve relevant
                                                                                       chunks for a
                                                                                       session-level RAG query

  POST                    `/api/sessions/{sessionId}/rag/answer`                       Generate an answer
                                                                                       using a chat in the
                                                                                       session
  ------------------------------------------------------------------------------------------------------------

### Chats

  ----------------------------------------------------------------------------------------
  Method                  Endpoint                                 Purpose
  ----------------------- ---------------------------------------- -----------------------
  POST                    `/api/sessions/{sessionId}/chats`        Create a chat

  GET                     `/api/sessions/{sessionId}/chats`        List chats in a session

  GET                     `/api/chats/{chatId}/messages`           Retrieve chat messages

  PUT                     `/api/chats/{chatId}/papers/selection`   Replace selected papers
                                                                   for a chat

  GET                     `/api/chats/{chatId}/papers/selection`   Retrieve selected
                                                                   papers for a chat
  ----------------------------------------------------------------------------------------

### Chat paper selection request

``` json
{
  "paperIds": ["paper-uuid-1", "paper-uuid-2"]
}
```

An empty array clears the chat's paper selection.

### RAG answer request

``` json
{
  "chatId": "chat-uuid",
  "question": "What are the main contributions of these papers?",
  "answerMode": "RESEARCH_GROUNDED",
  "topK": 5
}
```

The answer request accepts a non-empty question of up to 2,000
characters. `topK` defaults to 5 and is constrained to 1--20 at the
request DTO level. The lower-level chunk retrieval service accepts
1--50.

The response contains `answer`, `answerMode`, and `sources`. A source
can include `paperId`, `chunkId`, `pageNumber`, `sectionTitle`, and
`distance`.

## Frontend Structure

Known routes:

  Route                                        Page
  -------------------------------------------- --------------------------
  `/`                                          Landing page
  `/login`                                     Login
  `/register`                                  Registration
  `/dashboard`                                 Dashboard
  `/research/new`                              Create research project
  `/research`                                  Research/project listing
  `/research/:projectId`                       Project workspace
  `/research/:projectId/sessions/:sessionId`   Session workspace

The frontend is organized as follows:

  -----------------------------------------------------------------------
  Directory                           Responsibility
  ----------------------------------- -----------------------------------
  `components/auth`                   Protected and public-only route
                                      guards

  `components/chat`                   Chat input, messages, sidebar,
                                      sources, and new-chat dialog

  `components/landing`                Landing page sections

  `components/layout`                 Shared navigation

  `components/session`                Session header and paper explorer

  `context`                           Authentication and theme contexts

  `lib`                               Axios client and toast helpers

  `pages`                             Route-level pages

  `services`                          Authentication, chat, and research
                                      API functions

  `types`                             Shared TypeScript types
  -----------------------------------------------------------------------

`src/lib/api.ts` centralizes Axios communication and includes a JWT
interceptor. Authentication and theme state are managed through React
context.

## Security and Reliability

Implementation-level safeguards include:

-   JWT-based authentication and Spring Security.
-   Protected frontend routes.
-   Chat/session access validation before answer generation.
-   Validation that chat-selected papers belong to the relevant session.
-   Atomic conditional updates for document claiming.
-   Processing attempt limits and stale-job recovery.
-   PDF HTTPS enforcement and host allowlisting.
-   URL and resolved-address validation.
-   PDF response, signature, redirect, timeout, and size checks.
-   Temporary PDF cleanup.
-   Validation of empty queries, paper selections, and retrieval limits.
-   Global backend exception handling.

These measures are not a claim that the application has undergone a
formal security audit or penetration test.

## Getting Started

ResearchMate is currently a local-development project. It is not
deployed and does not use Docker or Docker Compose.

### Prerequisites

-   Java 21
-   Node.js and npm
-   PostgreSQL with a compatible pgvector extension
-   Google Gemini API key
-   BGE-small-en-v1.5 ONNX model and tokenizer files

### 1. Clone the repositories

``` bash
git clone <frontend-repository-url>
git clone <backend-repository-url>
```

Replace the placeholders with the actual repository URLs if the
repositories are published.

### 2. Prepare PostgreSQL

Create a database:

``` sql
CREATE DATABASE researchmate;
```

Connect to it and enable pgvector:

``` sql
CREATE EXTENSION IF NOT EXISTS vector;
```

Configure the backend datasource to use this database.

### 3. Add the embedding model files

Place the model files at the paths expected by `BgeEmbeddingService`:

``` text
models/
└── bge-small-en-v1.5/
    ├── model.onnx
    └── tokenizer.json
```

The supplied service loads these paths relative to the backend
application's working directory.

### 4. Configure the backend

Set the Gemini key, datasource credentials, and document-processing
properties. See [Configuration](#configuration).

### 5. Start the backend

Use the build tool and wrapper configured in the backend repository. The
known backend setup uses port `8080`.

### 6. Start the frontend

From the frontend directory:

``` bash
npm install
npm run dev
```

Configure the frontend API base URL to point to the running backend.

> Exact backend build commands, frontend scripts, database credentials,
> CORS settings, and Hibernate schema-generation properties were not
> supplied. They are intentionally not guessed here; use the actual
> repository configuration files.

## Configuration

Known settings include:

  ---------------------------------------------------------------------------------
  Setting                                       Purpose
  --------------------------------------------- -----------------------------------
  `GEMINI_KEY`                                  Google Gemini API key

  `researchmate.documents.worker-enabled`       Enables/disables the document
                                                worker

  `researchmate.documents.poll-delay-ms`        Polling delay; default 5,000 ms

  `researchmate.documents.max-attempts`         Maximum attempts; default 3

  `researchmate.documents.temp-directory`       Temporary PDF directory

  `researchmate.documents.max-pdf-size-bytes`   Maximum PDF size

  `researchmate.documents.max-pages`            Maximum page count; default 500
  ---------------------------------------------------------------------------------

Example properties:

``` properties
# Provide GEMINI_KEY through the environment.

researchmate.documents.worker-enabled=true
researchmate.documents.poll-delay-ms=5000
researchmate.documents.max-attempts=3
researchmate.documents.temp-directory=<temporary-directory>
researchmate.documents.max-pdf-size-bytes=<maximum-size-in-bytes>
researchmate.documents.max-pages=500
```

Do not commit API keys, database passwords, JWT secrets, or other
credentials to source control.

## Current Project Status and Future Improvements

The following are potential improvements, not existing capabilities:

-   Automated unit, integration, and end-to-end tests.
-   CI workflows for builds, tests, and static analysis.
-   OpenAPI/Swagger documentation.
-   Docker Compose for reproducible local development.
-   Frontend and backend deployment.
-   Documentation of PostgreSQL indexes for full-text search, metadata
    filters, and vector retrieval.
-   Verification or addition of an HNSW/IVFFlat vector index, based on
    query plans and workload.
-   Explicit scheduler/thread-pool configuration and worker throughput
    monitoring.
-   OCR support for scanned PDFs, if required.
-   Section-title extraction and section-aware chunking.
-   Monitoring for queue depth, processing duration, retries, and
    failures.
-   Documentation of the exact dataset revision and reproducible
    ingestion steps.

## Screenshots

Add screenshots of the actual application to the repository and replace
the placeholders below with the corresponding relative image paths.

### Landing page

```{=html}
<!-- Example: ![ResearchMate landing page](docs/images/landing-page.png) -->
```
`[Add landing page screenshot]`

### Dashboard

```{=html}
<!-- Example: ![ResearchMate dashboard](docs/images/dashboard.png) -->
```
`[Add dashboard screenshot]`

### Project workspace

```{=html}
<!-- Example: ![Research project workspace](docs/images/project-workspace.png) -->
```
`[Add project workspace screenshot]`

### Paper discovery

```{=html}
<!-- Example: ![Paper discovery](docs/images/paper-discovery.png) -->
```
`[Add paper discovery and search results screenshot]`

### Chat workspace

```{=html}
<!-- Example: ![Research chat](docs/images/chat-workspace.png) -->
```
`[Add chat workspace screenshot]`

### Answer sources

```{=html}
<!-- Example: ![Answer sources](docs/images/answer-sources.png) -->
```
`[Add assistant answer with source references screenshot]`

## License

Add the project's chosen license before publishing or distributing the
repository.
