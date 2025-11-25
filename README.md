# ✉️ AI Email Writer – Smart Gmail Assistant

A Chrome Extension + Web App that generates **AI-powered email replies** directly inside Gmail.  
Built using **React**, **Spring Boot**, and **OpenAI integration**.

---

## 🚀 Overview

AI Email Writer helps users compose professional, polite, or creative replies in seconds.  
The project includes:

- 🧩 **Chrome Extension:** Adds an “AI Reply” button in Gmail’s Reply and Compose windows  
- 💻 **React Web App:** Standalone interface to try and preview the feature  
- ⚙️ **Spring Boot Backend:** API service that generates email responses using AI models  


---

## 🧠 Tech Stack

| Layer | Technology |
|-------|-------------|
| Frontend | React.js, HTML, CSS |
| Extension | Chrome Manifest V3, JavaScript |
| Backend | Spring Boot, WebClient |
| AI | OpenAI / Gemini API |
| Hosting | Vercel (frontend), Render (backend) |

---

## 🏗️ Folder Structure

ai-email-writer/
┣ 📁 backend/ → Spring Boot API
┣ 📁 frontend/ → React web app
┗ 📁 chrome-extension/ → Gmail integration


---

## ⚙️ Setup (Local)

### Backend
```bash
cd backend
mvn spring-boot:run


cd frontend
npm install
npm start

Extension
  - Go to chrome://extensions/
  - Enable Developer Mode
  - Click Load unpacked
  - Select the chrome-extension folder
