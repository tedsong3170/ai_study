import 'package:flutter_test/flutter_test.dart';
import 'package:ui/main.dart';

void main() {
  testWidgets('App should render ChatScreen', (WidgetTester tester) async {
    await tester.pumpWidget(const ConfluenceChatbotApp());
    
    // 앱바 타이틀 확인
    expect(find.text('Confluence Chatbot'), findsOneWidget);
    
    // 빈 상태 메시지 확인
    expect(find.text('Confluence 문서에 대해 질문해보세요!'), findsOneWidget);
  });
}
