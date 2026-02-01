# Confluence Chatbot - 인터페이스 설계

## 1. 개요

본 문서는 Confluence Chatbot 시스템의 컴포넌트 간 인터페이스와 UI 설계를 정의합니다.

---

## 2. 서비스 간 인터페이스

### 2.1 인터페이스 다이어그램

```mermaid
graph LR
    subgraph UI["UI (Flutter)"]
        A1[ChatScreen]
    end
    
    subgraph Chatbot["Chatbot Service"]
        B1[ChatController]
        B2[AgentClient]
    end
    
    subgraph Agent["Agent Service"]
        C1[QueryAPI]
        C2[EmbedAPI]
        C3[RAGChain]
    end
    
    subgraph Indexer["Indexer Service"]
        D1[IndexerService]
        D2[ConfluenceClient]
        D3[AgentClient]
    end
    
    A1 -->|"IChatService"| B1
    B1 -->|"IAgentClient"| B2
    B2 -->|"HTTP"| C1
    
    D1 -->|"IConfluenceClient"| D2
    D1 -->|"IAgentClient"| D3
    D3 -->|"HTTP"| C2
```

---

## 3. Kotlin 인터페이스 정의

### 3.1 Chatbot Service

#### IAgentClient

Agent 서비스와의 통신을 담당하는 인터페이스

```kotlin
// chatbot/src/main/kotlin/interfaces/IAgentClient.kt

interface IAgentClient {
    /**
     * 질문에 대한 RAG 기반 답변을 요청합니다.
     * chatHistory를 전달하면 이전 대화를 참조하여 답변합니다.
     */
    suspend fun query(request: QueryRequest): QueryResponse
    
    /**
     * Agent 서비스 헬스체크
     */
    suspend fun healthCheck(): HealthStatus
}

data class QueryRequest(
    val question: String,
    val topK: Int = 5,
    val spaceKey: String? = null,
    val chatHistory: List<ChatMessage> = emptyList()  // Conversation Memory
)

data class ChatMessage(
    val role: String,  // "user" | "assistant"
    val content: String
)

data class QueryResponse(
    val answer: String,
    val sources: List<Source>,
    val processingTimeMs: Long
)

data class Source(
    val pageId: String,
    val title: String,
    val url: String,
    val chunkContent: String,
    val relevanceScore: Double
)

data class HealthStatus(
    val status: String,
    val dependencies: Map<String, String>
)
```

#### IConversationStore

대화 이력 관리 인터페이스

```kotlin
// chatbot/src/main/kotlin/interfaces/IConversationStore.kt

interface IConversationStore {
    /**
     * 새 대화 세션을 생성합니다.
     */
    suspend fun createConversation(): String
    
    /**
     * 메시지를 저장합니다.
     */
    suspend fun addMessage(
        conversationId: String, 
        role: MessageRole, 
        content: String,
        sources: List<Source>? = null
    )
    
    /**
     * 대화 이력을 조회합니다.
     */
    suspend fun getMessages(
        conversationId: String, 
        limit: Int = 20
    ): List<Message>
}

enum class MessageRole {
    USER, ASSISTANT
}

data class Message(
    val id: String,
    val role: MessageRole,
    val content: String,
    val sources: List<Source>?,
    val createdAt: Instant
)
```

---

### 3.2 Indexer Service

#### IConfluenceClient

Confluence API 통신 인터페이스

```kotlin
// indexer/src/main/kotlin/interfaces/IConfluenceClient.kt

interface IConfluenceClient {
    /**
     * Space 내 모든 페이지 ID를 조회합니다.
     */
    suspend fun getPageIds(spaceKey: String): List<String>
    
    /**
     * 페이지 상세 정보를 조회합니다.
     */
    suspend fun getPage(pageId: String): ConfluencePage
    
    /**
     * 마지막 색인 이후 변경된 페이지를 조회합니다.
     */
    suspend fun getUpdatedPages(
        spaceKey: String, 
        since: Instant
    ): List<String>
}

data class ConfluencePage(
    val id: String,
    val spaceKey: String,
    val title: String,
    val content: String,  // HTML 제거된 텍스트
    val url: String,
    val lastModified: Instant
)
```

#### IDocumentParser

문서 파싱 인터페이스

```kotlin
// indexer/src/main/kotlin/interfaces/IDocumentParser.kt

interface IDocumentParser {
    /**
     * Confluence HTML 콘텐츠를 텍스트로 변환합니다.
     */
    fun parseHtml(html: String): String
    
    /**
     * 텍스트를 청크로 분할합니다.
     */
    fun splitIntoChunks(
        text: String, 
        maxChunkSize: Int = 500,
        overlap: Int = 50
    ): List<String>
}
```

#### IAgentClient (Indexer용)

```kotlin
// indexer/src/main/kotlin/interfaces/IAgentClient.kt

interface IAgentClient {
    /**
     * 문서를 임베딩하여 저장합니다.
     */
    suspend fun embed(request: EmbedRequest): EmbedResponse
    
    /**
     * 페이지의 임베딩을 삭제합니다.
     */
    suspend fun deleteEmbedding(pageId: String): DeleteResponse
}

data class EmbedRequest(
    val pageId: String,
    val spaceKey: String,
    val title: String,
    val content: String,
    val pageUrl: String
)

data class EmbedResponse(
    val status: String,
    val pageId: String,
    val chunksCreated: Int
)

data class DeleteResponse(
    val status: String,
    val pageId: String,
    val chunksDeleted: Int
)
```

---

## 4. Python 인터페이스 정의

### 4.1 Agent Service

#### 서비스 인터페이스

```python
# agent/app/services/interfaces.py

from abc import ABC, abstractmethod
from typing import List, Optional
from dataclasses import dataclass

@dataclass
class EmbeddingResult:
    embedding: List[float]
    token_count: int

@dataclass
class SearchResult:
    page_id: str
    title: str
    content: str
    url: str
    score: float

@dataclass
class GenerationResult:
    answer: str
    sources: List[SearchResult]
    processing_time_ms: int


class IEmbeddingService(ABC):
    """텍스트 임베딩 서비스"""
    
    @abstractmethod
    def embed(self, text: str) -> List[float]:
        """텍스트를 벡터로 변환"""
        pass
    
    @abstractmethod
    def embed_batch(self, texts: List[str]) -> List[List[float]]:
        """여러 텍스트를 벡터로 변환"""
        pass


class IVectorStore(ABC):
    """벡터 저장소 서비스"""
    
    @abstractmethod
    def store(
        self, 
        page_id: str, 
        space_key: str,
        title: str,
        chunks: List[str],
        embeddings: List[List[float]],
        page_url: str
    ) -> int:
        """청크와 임베딩을 저장하고 저장된 수 반환"""
        pass
    
    @abstractmethod
    def search(
        self, 
        query_embedding: List[float], 
        top_k: int = 5,
        space_key: Optional[str] = None
    ) -> List[SearchResult]:
        """유사도 기반 검색"""
        pass
    
    @abstractmethod
    def delete(self, page_id: str) -> int:
        """페이지의 모든 청크 삭제"""
        pass


class ILLMService(ABC):
    """LLM 서비스 - Conversation Memory 지원"""
    
    @abstractmethod
    def generate(
        self, 
        prompt: str, 
        context: List[str],
        chat_history: List[dict] = None,  # Conversation Memory
        max_tokens: int = 1024
    ) -> str:
        """
        컨텍스트 및 대화 이력 기반 답변 생성
        
        Args:
            prompt: 사용자 질문
            context: RAG로 검색된 문서 청크들
            chat_history: 이전 대화 이력 [{"role": "user"|"assistant", "content": "..."}]
            max_tokens: 최대 토큰 수
        """
        pass


@dataclass
class ChatHistoryMessage:
    """Conversation Memory를 위한 대화 메시지"""
    role: str  # "user" | "assistant"
    content: str
```

---

## 5. UI 설계

### 5.1 화면 구성

```
┌─────────────────────────────────────────────────────┐
│                    Confluence Chatbot                │
├─────────────────────────────────────────────────────┤
│                                                      │
│  ┌────────────────────────────────────────────────┐ │
│  │ 🤖 안녕하세요! Confluence 문서에 대해          │ │
│  │    질문해주세요.                               │ │
│  └────────────────────────────────────────────────┘ │
│                                                      │
│  ┌────────────────────────────────────────────────┐ │
│  │ 👤 휴가 신청 절차가 어떻게 되나요?             │ │
│  └────────────────────────────────────────────────┘ │
│                                                      │
│  ┌────────────────────────────────────────────────┐ │
│  │ 🤖 휴가 신청 절차는 다음과 같습니다:           │ │
│  │                                                 │ │
│  │ 1. 사내 포털에 접속합니다                      │ │
│  │ 2. 휴가 신청 메뉴를 선택합니다                 │ │
│  │ 3. 휴가 종류와 기간을 입력합니다               │ │
│  │ ...                                             │ │
│  │                                                 │ │
│  │ 📎 출처:                                        │ │
│  │ • 휴가 정책 가이드 ↗                           │ │
│  │ • 인사 규정 ↗                                  │ │
│  └────────────────────────────────────────────────┘ │
│                                                      │
├─────────────────────────────────────────────────────┤
│  ┌──────────────────────────────────┐  ┌─────────┐ │
│  │ 질문을 입력하세요...              │  │  전송   │ │
│  └──────────────────────────────────┘  └─────────┘ │
└─────────────────────────────────────────────────────┘
```

### 5.2 위젯 구조

```mermaid
graph TB
    App[MaterialApp]
    App --> ChatScreen
    
    ChatScreen --> AppBar[AppBar]
    ChatScreen --> MessageList[ListView]
    ChatScreen --> InputArea[InputRow]
    
    MessageList --> UserBubble[MessageBubble - User]
    MessageList --> AssistantBubble[MessageBubble - Assistant]
    
    AssistantBubble --> MarkdownBody[MarkdownBody]
    AssistantBubble --> SourceLinks[SourceLinks]
    
    InputArea --> TextField[TextField]
    InputArea --> SendButton[IconButton]
```

### 5.3 상태 관리

```dart
// ui/lib/models/chat_state.dart

enum ChatStatus {
  idle,
  loading,
  error,
}

class ChatState {
  final String? conversationId;
  final List<Message> messages;
  final ChatStatus status;
  final String? errorMessage;
  
  ChatState({
    this.conversationId,
    this.messages = const [],
    this.status = ChatStatus.idle,
    this.errorMessage,
  });
  
  ChatState copyWith({
    String? conversationId,
    List<Message>? messages,
    ChatStatus? status,
    String? errorMessage,
  }) {
    return ChatState(
      conversationId: conversationId ?? this.conversationId,
      messages: messages ?? this.messages,
      status: status ?? this.status,
      errorMessage: errorMessage ?? this.errorMessage,
    );
  }
}
```

### 5.4 API 클라이언트

```dart
// ui/lib/services/chat_api.dart

abstract class IChatApi {
  Future<ChatResponse> sendMessage(String question, String? conversationId);
  Future<List<Message>> getHistory(String conversationId, {int limit = 20});
}

class ChatApi implements IChatApi {
  final String baseUrl;
  final http.Client client;
  
  ChatApi({
    required this.baseUrl,
    http.Client? client,
  }) : client = client ?? http.Client();
  
  @override
  Future<ChatResponse> sendMessage(String question, String? conversationId) async {
    final response = await client.post(
      Uri.parse('$baseUrl/api/chat'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'question': question,
        if (conversationId != null) 'conversation_id': conversationId,
      }),
    );
    
    if (response.statusCode == 200) {
      return ChatResponse.fromJson(jsonDecode(response.body));
    } else {
      throw ChatApiException(response.statusCode, response.body);
    }
  }
  
  @override
  Future<List<Message>> getHistory(String conversationId, {int limit = 20}) async {
    // 구현...
  }
}
```

---

## 6. 설정 파일 인터페이스

### 6.1 환경 변수

```bash
# .env.example

# Confluence 설정
CONFLUENCE_BASE_URL=https://your-domain.atlassian.net
CONFLUENCE_API_TOKEN=your-api-token
CONFLUENCE_USER_EMAIL=your-email@example.com
CONFLUENCE_SPACE_KEY=DEV

# 데이터베이스 설정
DATABASE_URL=postgresql://postgres:postgres@localhost:5432/confluence_chatbot

# Ollama 설정
OLLAMA_BASE_URL=http://localhost:11434
OLLAMA_MODEL=gpt-oss:20b

# 서비스 포트
CHATBOT_PORT=8080
INDEXER_PORT=8081
AGENT_PORT=8000
```

### 6.2 애플리케이션 설정

```yaml
# chatbot/src/main/resources/application.yaml

server:
  port: ${CHATBOT_PORT:8080}

agent:
  base-url: ${AGENT_BASE_URL:http://localhost:8000}
  timeout-ms: 30000

conversation:
  max-history: 50
  cleanup-interval-hours: 24
```

```yaml
# agent/config.yaml

database:
  url: ${DATABASE_URL}
  pool_size: 5

ollama:
  base_url: ${OLLAMA_BASE_URL}
  model: ${OLLAMA_MODEL}
  timeout: 60

embedding:
  model: all-MiniLM-L6-v2
  dimension: 384
  
chunking:
  max_size: 500
  overlap: 50
```

---

## 변경 이력

| 버전 | 날짜 | 작성자 | 변경 내용 |
|-----|------|-------|----------|
| 0.1 | 2026-01-19 | Antigravity | 초안 작성 |
