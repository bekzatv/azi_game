# Visual assets — Azi 1.1

- `app/src/main/res/drawable-nodpi/deck_shanyrak.png`: new AI-generated production texture, created for this update using the image generation tool; 1024 × 1536 PNG. No external stock-photo dependency.
- Direction: a flat portrait 2:3 playing-card back; midnight indigo, champagne-gold engraving and restrained turquoise; symmetrical Kazakh shanyrak and ram-horn ornament; no letters, ranks, mockup, hands or scene.
- `TableEngraving.kt`: original code-native concentric and radial linework.
- `CourtEmblem` in `KazakhCardView.kt`: code-native vector crown and crossed-staff marks.
- Silver and sunset backs reuse the project's existing Tumar/Syrgha procedural geometry with new palettes.
- Existing assets and author attribution were preserved. The generated artwork is integrated directly into Compose card rendering, not a screenshot-only mockup.

Implementation follows the draw-ui approach of a consistent palette and clear screen hierarchy.
New raster artwork was generated with the imagegen workflow; layout and court symbols remain code-native.
