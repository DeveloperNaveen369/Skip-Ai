# Skip AI 🤖

> **An Android AI assistant that lets you use AI directly on selected text without opening a separate AI chat app.**

Skip AI brings AI assistance directly into the app you're currently using. Select text anywhere in a supported Android application, and Skip AI provides a floating toolbar with useful AI actions.

## 📱 Screenshots



<p align="center">
  <img src="screenshots/home.png" width="220">
  <img src="screenshots/ask-ai.png" width="220">

  <img src="screenshots/summarize.png" width="220">
  <img src="screenshots/translate.png" width="220">
</p>

## ✨ Features

* 🤖 **Ask AI** — Ask questions about selected text
* 📝 **Summarize** — Quickly summarize selected content
* ✨ **Simplify** — Make complex text easier to understand
* 🌐 **Translate** — Translate selected text
* 📌 **Floating AI Toolbar** — Access AI actions without leaving the current app
* 📱 **Cross-App Text Selection** — Detect selected text from supported applications
* ⚡ **Quick AI Access** — No need to copy text and open a separate AI chat application

## 💡 Why Skip AI?

Normally, using AI with text from another application requires:

1. Select the text
2. Copy it
3. Open an AI chat application
4. Paste the text
5. Ask the AI

Skip AI removes these extra steps.

**Select → Choose an AI action → Get the result.**

## 🔧 How It Works

Skip AI uses Android's **AccessibilityService** to detect text-selection events from supported applications.

When text is selected:

```text
User selects text
       ↓
AccessibilityService detects selection
       ↓
Skip AI processes the selected text
       ↓
Floating AI toolbar appears
       ↓
User selects an AI action
       ↓
AI processes the request
       ↓
Result is displayed to the user
```

## 🛠️ Tech Stack

* **Language:** Java
* **Platform:** Android
* **IDE:** Android Studio
* **API:** Android AccessibilityService
* **UI:** XML
* **AI:** AI API integration
* **Architecture:** Android Service + Floating Window

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/Skip-AI.git
```

### 2. Open in Android Studio

Open the cloned project in Android Studio and allow Gradle to sync.

### 3. Configure the AI API

Add your AI API configuration according to the project's configuration.

> **Never commit API keys or other secrets to GitHub.**

### 4. Build and install

Connect an Android device or start an emulator, then run the project from Android Studio.

### 5. Enable Accessibility Service

After installing the application:

**Settings → Accessibility → Skip AI → Enable**

The exact location may vary depending on the Android device manufacturer.

## 📂 Project Structure

```text
Skip-AI/
├── app/
│   └── src/
│       └── main/
│           ├── java/
│           ├── res/
│           └── AndroidManifest.xml
├── screenshots/
├── build.gradle
├── settings.gradle
└── README.md
```

## 🔐 Permissions

Skip AI requires Android accessibility access to detect selected text from other applications and display its floating interface.

Accessibility access is used specifically for the application's core functionality.

## ⚠️ Limitations

* Text-selection behavior may vary between Android applications.
* Some applications may not expose selected text through Android accessibility APIs.
* Accessibility behavior can differ between Android versions and device manufacturers.
* AI features require an active AI API configuration.

## 🔮 Future Improvements

* More AI actions
* Custom AI prompts
* Conversation/history support
* Better support for additional applications
* Customizable floating toolbar
* Voice-based AI interaction
* More translation options

## 🤝 Contributing

Contributions, ideas, and improvements are welcome.

Fork the repository, create a new branch, make your changes, and submit a pull request.

## 📄 License

This project is currently available for learning and development purposes.

---

### Built with ❤️ using Java and Android
