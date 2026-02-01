import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/chat_models.dart';

/// Chatbot API 서비스
class ChatApiService {
  final String baseUrl;
  final http.Client _client;

  ChatApiService({
    String? baseUrl,
    http.Client? client,
  })  : baseUrl = baseUrl ?? 'http://localhost:8080',
        _client = client ?? http.Client();

  /// 질문 전송 및 답변 수신
  Future<ChatResponse> sendMessage(String question, {String? conversationId}) async {
    final body = {
      'question': question,
      if (conversationId != null) 'conversation_id': conversationId,
    };

    final response = await _client.post(
      Uri.parse('$baseUrl/api/chat'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode(body),
    );

    if (response.statusCode == 200) {
      final json = jsonDecode(response.body) as Map<String, dynamic>;
      return ChatResponse.fromJson(json);
    } else {
      throw Exception('Failed to send message: ${response.statusCode}');
    }
  }

  /// 대화 이력 조회
  Future<List<ChatMessage>> getHistory(String conversationId) async {
    final response = await _client.get(
      Uri.parse('$baseUrl/api/chat/history?conversation_id=$conversationId'),
    );

    if (response.statusCode == 200) {
      final json = jsonDecode(response.body) as Map<String, dynamic>;
      final messages = json['messages'] as List<dynamic>;
      return messages
          .map((e) => ChatMessage.fromJson(e as Map<String, dynamic>))
          .toList();
    } else {
      throw Exception('Failed to get history: ${response.statusCode}');
    }
  }

  /// 헬스체크
  Future<bool> healthCheck() async {
    try {
      final response = await _client.get(Uri.parse('$baseUrl/health'));
      return response.statusCode == 200;
    } catch (e) {
      return false;
    }
  }

  void dispose() {
    _client.close();
  }
}
