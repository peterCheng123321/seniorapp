# Product Requirements Document: Senior Voice Assistant

## Executive Summary
A voice-enabled Android application designed specifically for elderly users, featuring pre-trained dialect models and essential daily assistance features like medication reminders and emergency contacts.

## Product Overview

### Vision
To create a simple, immediately useful voice assistant that works out-of-the-box for Chinese seniors, focusing on practical daily needs over complex personalization.

### Target Users
- **Primary:** Chinese seniors (65+) who speak regional dialects
- **Secondary:** Families wanting simple voice assistance for elderly relatives

### Key Problems Solved
1. Standard voice assistants don't understand Chinese dialects
2. Seniors need medication reminders and emergency features
3. Existing apps are too complex for elderly users

### Monetization
- **One-time purchase: $2.99**
- No subscriptions, no ads, no data collection
- Sustainable through volume sales in Chinese market

## Core Features

### 1. Multi-Dialect Recognition (No Setup Required)
**Purpose:** Instant speech understanding for Chinese dialects

**Pre-trained Models:**
- Standard Mandarin (普通话)
- Cantonese (粤语) 
- Shanghainese (上海话)
- Sichuanese (四川话)
- Auto-detection or simple region selection

**Technical Details:**
- Whisper + Paraformer engines
- Local processing, no internet required
- ~200MB total model size

### 2. Essential Daily Features
**Medication Reminders:**
- Voice-set reminders: "提醒我下午2点吃药"
- Simple repeat scheduling
- Large notification alerts

**Emergency Contacts:**
- Voice-activated calls: "打电话给女儿"
- Pre-configured family contacts
- One-tap emergency button

**Basic Voice Commands:**
- Time/weather queries
- Simple note-taking
- Timer/alarm functions

### 3. Minimal UI Design
- Large buttons and text
- High contrast colors
- Maximum 3 buttons per screen
- Voice-first interaction (UI is secondary)

## Technical Architecture

### Dependencies
- whisper.cpp (JNI integration)
- sherpa-ncnn (Paraformer)
- Silero VAD
- Android Compose UI

### Permissions
- RECORD_AUDIO (essential)
- SET_ALARM/REMINDER (for medication)
- CALL_PHONE (for emergency contacts)
- ACCESS_NOTIFICATION_POLICY (for alerts)

### Storage
- Cache: Pre-trained dialect models (~200MB)
- Internal: User contacts, medication schedule
- No external storage needed

## User Flow

1. **First Launch**
   - Single permission request
   - Select region/dialect (optional - auto-detect works)
   - Add emergency contact (optional)

2. **Daily Usage**
   - Voice command → immediate result
   - No setup, no calibration
   - Large text feedback

3. **Key Interactions**
   - "提醒我吃药" → sets medication reminder
   - "打电话给儿子" → calls emergency contact
   - "几点了" → shows time in large text

## MVP Limitations (Acceptable for v1.0)

1. **4 Dialect Limit:** Only covers major Chinese dialects (expandable later)
2. **Basic Commands:** Limited to essential functions (medication, calls, time)
3. **Simple UI:** Functional but not fancy (seniors prefer this)
4. **Offline Only:** No cloud features (privacy advantage)

## Development Timeline

### Phase 1 - MVP (3 months)
- ✅ Multi-dialect recognition
- [ ] Medication reminder system
- [ ] Emergency contact calling
- [ ] Basic voice commands
- [ ] App store release ($2.99)

### Phase 2 - Expansion (6 months)
- [ ] More dialects (Taiwanese, Hokkien)
- [ ] Advanced scheduling
- [ ] Family sharing features
- [ ] Usage analytics

### Phase 3 - Premium (12 months)
- [ ] Optional calibration for accuracy boost
- [ ] Cloud backup (premium feature)
- [ ] Smart home integration
- [ ] Caregiver dashboard

## Success Metrics
- First-day retention: >70% (no setup abandonment)
- Daily active usage: >40% (practical daily value)
- User satisfaction: >4.2/5 (simple expectations)
- Revenue target: 10K downloads × $2.99 = $30K in first year

## Competitive Advantages
1. **Zero Setup:** Works immediately, no calibration required
2. **Dialect-First:** Designed for Chinese seniors specifically  
3. **Offline Privacy:** No data leaves device
4. **One-Time Purchase:** No subscription fatigue
5. **Essential Features:** Medication + emergency (real daily value) 