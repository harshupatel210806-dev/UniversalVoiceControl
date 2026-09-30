# Universal Voice Control — Combined V1 + V1.1

આ project V1 અને V1.1 ને એક projectમાં combine કરીને બનાવવામાં આવ્યું છે.

## Included
- V1 basic phone control:
  - Open app
  - Back / Home / Recent Apps
  - Scroll Up / Scroll Down
  - Tap visible text
  - Type text
  - Google Search
- V1.1 improvements:
  - Gujarati / Hindi / English command keywords
  - Better app-name matching and common aliases
  - Gujarati/Hindi variants for common commands
  - Basic multi-step: Open YouTube and search GTA 5
- AccessibilityService based UI actions
- Push-to-talk speech recognition

## Important
આ source project છે; APK નથી.
આ project ને actual Samsung Galaxy A15 પર build/install કરીને સંપૂર્ણ રીતે test કરવામાં આવ્યું નથી.
Background/always-listening voice control, dynamic WhatsApp contact/message automation અને advanced V2/V3 features હજુ આ combined V1+V1.1 projectનો ભાગ નથી.

Android AccessibilityService દરેક third-party app/screen પર 100% automationની guarantee આપતી નથી.
Lock-screen PIN/Pattern/Password bypass કરવામાં આવતું નથી.

## Setup
1. Android build environmentમાં project ખોલો.
2. Microphone permission આપો.
3. Settings → Accessibility → Universal Voice Control → ON કરો.
4. Appમાં Speak Command દબાવીને command બોલો.
