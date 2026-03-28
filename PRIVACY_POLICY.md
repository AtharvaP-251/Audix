# 🔒 Privacy Policy

**Last Updated: 2026-03-28**

Your privacy is our top priority. Audix is designed to be a private and secure audio enhancement tool. This document explains how we handle your data.

---

## 1. Data Collection & Usage

### A. Notification Access 🔔
To detect the genre of the music you are playing, Audix requires **Notification Access**.
- **What we process**: Track Title, Artist Name, and Album Name.
- **What we do NOT process**: We do not read personal messages, contacts, location, or any other private notifications.
- **Storage**: This data is used only to query the genre classification API and is cached locally on your device to improve performance.

### B. Gemini API Integration 🧠
Audix uses the Google Gemini API to classify the genre of your tracks.
- **Metadata Sharing**: Only the **Song Title** and **Artist Name** are sent to the Gemini API for classification. No user-identifiable information is shared.
- **API Keys**: If you provide your own Gemini API key, it is stored securely on your device using Android SharedPreferences and is only used to make requests to the Gemini API.

### C. No Accounts 👤
Audix does not require or support user accounts. We do not collect your name, email address, or any other personally identifiable information (PII).

---

## 2. Local Processing & Storage

Most of the app's functions happen entirely on your device:
- **Audio Processing**: All EQ transformations and Spatial Audio effects occur locally using the Android `AudioEffect` framework.
- **Local Database**: Song-to-genre mappings are stored in a local SQLite database on your device (using Room). This data never leaves your phone.

---

## 3. Third-Party Services

Audix integrates with the following third-party services:
- **Google Gemini API**: For genre classification.
- **Music Players (e.g., Spotify, YT Music)**: We interact with these apps only to read track metadata via standard Android APIs.

---

## 4. Your Rights

As we do not collect any personal data, there is no data to delete or export from our servers. All your preferences and cached data are stored locally and can be cleared by:
1. Uninstalling the app.
2. Clearing the app's storage in Android Settings.

---

## 5. Contact Us

If you have any questions about this Privacy Policy, please contact us through our official project repository.

---

*Audix Labs — Private, intelligent sound.*
