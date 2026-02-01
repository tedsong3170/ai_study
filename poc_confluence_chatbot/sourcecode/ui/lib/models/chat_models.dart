/// 채팅 메시지 모델
class ChatMessage {
  final String role; // 'user' | 'assistant'
  final String content;
  final DateTime timestamp;
  final List<Source>? sources;

  ChatMessage({
    required this.role,
    required this.content,
    DateTime? timestamp,
    this.sources,
  }) : timestamp = timestamp ?? DateTime.now();

  factory ChatMessage.fromJson(Map<String, dynamic> json) {
    return ChatMessage(
      role: json['role'] as String,
      content: json['content'] as String,
      timestamp: json['timestamp'] != null
          ? DateTime.fromMillisecondsSinceEpoch(json['timestamp'] as int)
          : DateTime.now(),
    );
  }

  bool get isUser => role == 'user';
  bool get isAssistant => role == 'assistant';
}

/// 출처 정보 모델
class Source {
  final String pageId;
  final String title;
  final String url;
  final String chunkContent;
  final double relevanceScore;

  Source({
    required this.pageId,
    required this.title,
    required this.url,
    required this.chunkContent,
    required this.relevanceScore,
  });

  factory Source.fromJson(Map<String, dynamic> json) {
    return Source(
      pageId: json['page_id'] as String,
      title: json['title'] as String,
      url: json['url'] as String,
      chunkContent: json['chunk_content'] as String,
      relevanceScore: (json['relevance_score'] as num).toDouble(),
    );
  }
}

/// 채팅 응답 모델
class ChatResponse {
  final String conversationId;
  final String answer;
  final List<Source> sources;

  ChatResponse({
    required this.conversationId,
    required this.answer,
    required this.sources,
  });

  factory ChatResponse.fromJson(Map<String, dynamic> json) {
    return ChatResponse(
      conversationId: json['conversation_id'] as String,
      answer: json['answer'] as String,
      sources: (json['sources'] as List<dynamic>)
          .map((e) => Source.fromJson(e as Map<String, dynamic>))
          .toList(),
    );
  }
}
