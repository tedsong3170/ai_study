import 'package:flutter/foundation.dart';
import '../models/chat_models.dart';
import '../services/chat_api_service.dart';

/// 채팅 상태 관리 Provider
class ChatProvider extends ChangeNotifier {
  final ChatApiService _apiService;
  
  List<ChatMessage> _messages = [];
  String? _conversationId;
  bool _isLoading = false;
  String? _error;
  List<Source>? _lastSources;

  ChatProvider({ChatApiService? apiService})
      : _apiService = apiService ?? ChatApiService();

  // Getters
  List<ChatMessage> get messages => _messages;
  String? get conversationId => _conversationId;
  bool get isLoading => _isLoading;
  String? get error => _error;
  List<Source>? get lastSources => _lastSources;
  bool get hasMessages => _messages.isNotEmpty;

  /// 메시지 전송
  Future<void> sendMessage(String question) async {
    if (question.trim().isEmpty) return;

    debugPrint('📤 Sending message: $question');

    // 사용자 메시지 추가
    final userMessage = ChatMessage(
      role: 'user',
      content: question,
    );
    _messages.add(userMessage);
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      // API 호출
      debugPrint('🔗 Calling API...');
      final response = await _apiService.sendMessage(
        question,
        conversationId: _conversationId,
      );
      debugPrint('✅ Response received: ${response.answer.substring(0, response.answer.length.clamp(0, 50))}...');

      // 대화 ID 저장
      _conversationId = response.conversationId;

      // 어시스턴트 응답 추가
      final assistantMessage = ChatMessage(
        role: 'assistant',
        content: response.answer,
        sources: response.sources,
      );
      _messages.add(assistantMessage);
      _lastSources = response.sources;

    } catch (e, stackTrace) {
      debugPrint('❌ Error: $e');
      debugPrint('❌ StackTrace: $stackTrace');
      _error = e.toString();
      // 에러 발생 시 사용자 메시지는 유지하되 에러 메시지 추가
      final errorMessage = ChatMessage(
        role: 'assistant',
        content: '⚠️ 오류가 발생했습니다: ${e.toString()}',
      );
      _messages.add(errorMessage);
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  /// 새 대화 시작
  void startNewConversation() {
    _messages.clear();
    _conversationId = null;
    _error = null;
    _lastSources = null;
    notifyListeners();
  }

  /// 대화 이력 로드
  Future<void> loadHistory(String conversationId) async {
    _isLoading = true;
    notifyListeners();

    try {
      final history = await _apiService.getHistory(conversationId);
      _messages = history;
      _conversationId = conversationId;
      _error = null;
    } catch (e) {
      _error = e.toString();
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  @override
  void dispose() {
    _apiService.dispose();
    super.dispose();
  }
}
