import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';
import '../models/chat_models.dart';

/// 출처 정보 패널
class SourcesPanel extends StatelessWidget {
  final List<Source> sources;

  const SourcesPanel({super.key, required this.sources});

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;

    return Container(
      height: 80,
      padding: const EdgeInsets.symmetric(vertical: 8),
      decoration: BoxDecoration(
        color: colorScheme.surfaceContainerLow,
        border: Border(
          top: BorderSide(
            color: colorScheme.outlineVariant,
            width: 1,
          ),
        ),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: Text(
              '📚 참고 문서',
              style: Theme.of(context).textTheme.labelMedium?.copyWith(
                    color: colorScheme.onSurface.withOpacity(0.6),
                  ),
            ),
          ),
          const SizedBox(height: 4),
          Expanded(
            child: ListView.builder(
              scrollDirection: Axis.horizontal,
              padding: const EdgeInsets.symmetric(horizontal: 12),
              itemCount: sources.length,
              itemBuilder: (context, index) {
                return _SourceChip(source: sources[index]);
              },
            ),
          ),
        ],
      ),
    );
  }
}

class _SourceChip extends StatelessWidget {
  final Source source;

  const _SourceChip({required this.source});

  Future<void> _openUrl() async {
    final uri = Uri.parse(source.url);
    if (await canLaunchUrl(uri)) {
      await launchUrl(uri, mode: LaunchMode.externalApplication);
    }
  }

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 4),
      child: ActionChip(
        avatar: Icon(
          Icons.description_outlined,
          size: 16,
          color: colorScheme.primary,
        ),
        label: Text(
          source.title.length > 20
              ? '${source.title.substring(0, 20)}...'
              : source.title,
          style: TextStyle(
            fontSize: 12,
            color: colorScheme.onSurface,
          ),
        ),
        tooltip: source.title,
        onPressed: _openUrl,
        backgroundColor: colorScheme.surface,
        side: BorderSide(color: colorScheme.outlineVariant),
      ),
    );
  }
}
