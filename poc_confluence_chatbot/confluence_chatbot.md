# 1. Project Overview
- This poc is to build a chatbot that can answer questions about Confluence documents.

# 2. Purpose
- To build a chatbot that can answer questions about Confluence documents.
- To evaluate the feasibility of using AI to build a chatbot that can answer questions about Confluence documents.

# 3. Architecture
## 3.1 Indexer
- Indexer is a service that indexes Confluence documents and stores them in a vector database.
- It is implemented using Kotlin and Vert.x. 
- It uses Atlassian API to fetch Confluence documents.
- It uses Agent to analyze and save documents to vector database.
- It is batch application.
- It is scheduled to run every time when Confluence documents are updated.

## 3.2 Chatbot
- Chatbot is a service that answers questions about Confluence documents.
- It is implemented using Kotlin and Vert.x.
- It uses Agent to analyze and search documents from vector database.
- It is async application.

## 3.3 Agent
- It is llm agent that uses LangChain and Ollama to support chatbot and indexer.
- It is implemented using Python.

## 3.4 UI
- It is a web application that allows users to ask questions about Confluence documents.
- It is implemented using Flutter.
- macos, android supported.

# 4. Data Flow
## 4.1 Indexer Data Flow
- Confluence documents are fetched using Atlassian API.
- Documents are analyzed using Agent.
- Documents are saved to vector database.

## 4.2 Chatbot Data Flow
- User's question is received.
- Question is analyzed using Agent.
- Question is answered using Agent.

# 5. Skills Stack
## 5.1 Indexer
- Kotlin 1.9
- Vert.x 4.5.24


## 5.2 Chatbot
- Kotlin 1.9
- Vert.x 4.5.24

## 5.3 Agent
- Python 3.12
- LangChain
- Ollama
- pg-vector
- sentence-transformers

## 5.4 UI
- Flutter
- macOS, Android supported
