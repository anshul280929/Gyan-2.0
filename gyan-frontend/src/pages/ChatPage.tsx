import { ChangeEvent, FormEvent, useEffect, useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useParams } from 'react-router-dom';
import { AppHeader } from '../components/AppHeader';
import {
  askQuestion,
  ChatRecord,
  ChatMessageRecord,
  deleteChat,
  deleteDocument,
  DocumentRecord,
  downloadDocument,
  getChat,
  getChatDocuments,
  getChatMessages,
  logout,
  previewDocument,
  renameChat,
  uploadDocument
} from '../lib/api';
import { useNotifications } from '../components/NotificationProvider';

function renderInlineFormatting(text: string) {
  const parts = text.split(/(\*\*[^*]+\*\*)/g).filter(Boolean);

  return parts.map((part, index) => {
    const boldMatch = part.match(/^\*\*([^*]+)\*\*$/);

    if (boldMatch) {
      return (
        <strong key={`strong-${index}`} className="answer-strong">
          {boldMatch[1]}
        </strong>
      );
    }

    return <span key={`text-${index}`}>{part}</span>;
  });
}

function renderAnswer(answer: string) {
  const blocks = answer
    .split(/\n\s*\n/)
    .map((block) => block.trim())
    .filter(Boolean);

  return blocks.map((block, index) => {
    const lines = block
      .split('\n')
      .map((line) => line.trim())
      .filter(Boolean);

    const isBulletList = lines.every((line) => /^[-*]\s+/.test(line));
    const isNumberedList = lines.every((line) => /^\d+\.\s+/.test(line));

    if (isBulletList) {
      return (
        <ul className="answer-list" key={`bullet-${index}`}>
          {lines.map((line, lineIndex) => (
            <li key={`bullet-item-${index}-${lineIndex}`}>
              {renderInlineFormatting(line.replace(/^[-*]\s+/, ''))}
            </li>
          ))}
        </ul>
      );
    }

    if (isNumberedList) {
      return (
        <ol className="answer-list" key={`number-${index}`}>
          {lines.map((line, lineIndex) => (
            <li key={`number-item-${index}-${lineIndex}`}>
              {renderInlineFormatting(line.replace(/^\d+\.\s+/, ''))}
            </li>
          ))}
        </ol>
      );
    }

    return (
      <p className="answer-paragraph" key={`paragraph-${index}`}>
        {renderInlineFormatting(lines.join(' '))}
      </p>
    );
  });
}

export function ChatPage() {
  const navigate = useNavigate();
  const { notify } = useNotifications();
  const params = useParams();
  const chatId = Number(params['chatId']);
  const [chat, setChat] = useState<ChatRecord | null>(null);
  const [messages, setMessages] = useState<ChatMessageRecord[]>([]);
  const [documents, setDocuments] = useState<DocumentRecord[]>([]);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [question, setQuestion] = useState('');
  const [answer, setAnswer] = useState('');
  const [loadingChat, setLoadingChat] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [asking, setAsking] = useState(false);
  const [viewingId, setViewingId] = useState<number | null>(null);
  const [deletingDocumentId, setDeletingDocumentId] = useState<number | null>(null);
  const [deletingChat, setDeletingChat] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [previewFileName, setPreviewFileName] = useState('');
  const [previewContentType, setPreviewContentType] = useState('');
  const previousStatusesRef = useRef<Map<number, string | null>>(new Map());
  const hasLoadedStatusesRef = useRef(false);

  useEffect(() => {
    if (!Number.isFinite(chatId)) {
      navigate('/dashboard', { replace: true });
      return;
    }

    async function loadWorkspace() {
      setLoadingChat(true);
      setErrorMessage('');

      try {
        const [chatResponse, documentsResponse, messagesResponse] = await Promise.all([
          getChat(chatId),
          getChatDocuments(chatId),
          getChatMessages(chatId)
        ]);
        setChat(chatResponse);
        setDocuments(documentsResponse);
        setMessages(messagesResponse);
      } catch (err) {
        setErrorMessage(err instanceof Error ? err.message : 'Unable to load chat.');
      } finally {
        setLoadingChat(false);
      }
    }

    void loadWorkspace();
  }, [chatId, navigate]);

  function handleFileChange(event: ChangeEvent<HTMLInputElement>) {
    setSelectedFile(event.target.files?.[0] ?? null);
  }

  async function refreshDocuments() {
    const response = await getChatDocuments(chatId);
    setDocuments(response);
  }

  async function refreshMessages() {
    const response = await getChatMessages(chatId);
    setMessages(response);
  }

  useEffect(() => {
    if (!documents.some((document) => document.processingStatus === 'UPLOADED' || document.processingStatus === 'PROCESSING')) {
      return;
    }

    const intervalId = window.setInterval(() => {
      void refreshDocuments();
    }, 4000);

    return () => window.clearInterval(intervalId);
  }, [documents]);

  useEffect(() => {
    return () => {
      if (previewUrl) {
        window.URL.revokeObjectURL(previewUrl);
      }
    };
  }, [previewUrl]);

  useEffect(() => {
    if (loadingChat) {
      return;
    }

    const previousStatuses = previousStatusesRef.current;

    if (!hasLoadedStatusesRef.current) {
      hasLoadedStatusesRef.current = true;
      previousStatusesRef.current = new Map(documents.map((document) => [document.id, document.processingStatus]));
      return;
    }

    for (const document of documents) {
      const previousStatus = previousStatuses.get(document.id);
      const nextStatus = document.processingStatus;

      if (previousStatus && previousStatus !== nextStatus) {
        if (nextStatus === 'PROCESSING') {
          notify(`${document.fileName} is now being processed.`, 'info');
        } else if (nextStatus === 'READY') {
          notify(`${document.fileName} is ready for chat and search.`, 'success');
        } else if (nextStatus === 'FAILED') {
          notify(
            document.processingError
              ? `${document.fileName} failed to process: ${document.processingError}`
              : `${document.fileName} failed to process.`,
            'error'
          );
        }
      }
    }

    previousStatusesRef.current = new Map(documents.map((document) => [document.id, document.processingStatus]));
  }, [documents, loadingChat, notify]);

  async function handleUpload(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!selectedFile) {
      setErrorMessage('Select a file before uploading.');
      return;
    }

    setUploading(true);
    setErrorMessage('');

    try {
      await uploadDocument(chatId, selectedFile);
      setSelectedFile(null);
      notify('Document uploaded. Processing has started in the background.', 'info');
      await refreshDocuments();
      setChat((current) =>
        current
          ? { ...current, documentCount: current.documentCount + 1, updatedAt: new Date().toISOString() }
          : current
      );
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Upload failed.';
      setErrorMessage(message);
      notify(message, 'error');
    } finally {
      setUploading(false);
    }
  }

  async function handleAsk(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!question.trim()) {
      setErrorMessage('Enter a question first.');
      return;
    }

    setAsking(true);
    setErrorMessage('');

    try {
      const response = await askQuestion(chatId, question.trim());
      setAnswer(response.answer);
      await refreshMessages();
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Unable to get an answer.';
      setErrorMessage(message);
      notify(message, 'error');
    } finally {
      setAsking(false);
    }
  }

  async function handleDownload(documentId: number) {
    setViewingId(documentId);
    setErrorMessage('');

    try {
      await downloadDocument(chatId, documentId);
      notify('Download started.', 'success');
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Unable to download document.';
      setErrorMessage(message);
      notify(message, 'error');
    } finally {
      setViewingId(null);
    }
  }

  async function handlePreview(document: DocumentRecord) {
    setViewingId(document.id);
    setErrorMessage('');

    try {
      if (previewUrl) {
        window.URL.revokeObjectURL(previewUrl);
      }

      const preview = await previewDocument(chatId, document.id);
      setPreviewUrl(preview.url);
      setPreviewContentType(preview.contentType);
      setPreviewFileName(document.fileName);
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Unable to preview document.';
      setErrorMessage(message);
      notify(message, 'error');
    } finally {
      setViewingId(null);
    }
  }

  async function handleDeleteDocument(document: DocumentRecord) {
    const confirmed = window.confirm(`Delete "${document.fileName}" from this chat?`);

    if (!confirmed) {
      return;
    }

    setDeletingDocumentId(document.id);
    setErrorMessage('');

    try {
      await deleteDocument(chatId, document.id);
      setDocuments((current) => current.filter((item) => item.id !== document.id));
      setChat((current) =>
        current
          ? {
              ...current,
              documentCount: Math.max(0, current.documentCount - 1),
              updatedAt: new Date().toISOString()
            }
          : current
      );
      notify('Document deleted successfully.', 'success');
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Unable to delete document.';
      setErrorMessage(message);
      notify(message, 'error');
    } finally {
      setDeletingDocumentId(null);
    }
  }

  async function handleDeleteChat() {
    if (!chat) {
      return;
    }

    const confirmed = window.confirm(`Delete chat "${chat.name}" and all its documents?`);

    if (!confirmed) {
      return;
    }

    setDeletingChat(true);
    setErrorMessage('');

    try {
      await deleteChat(chat.id);
      notify('Chat deleted successfully.', 'success');
      navigate('/dashboard', { replace: true });
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Unable to delete chat.';
      setErrorMessage(message);
      notify(message, 'error');
      setDeletingChat(false);
    }
  }

  async function handleRenameChat() {
    if (!chat) {
      return;
    }

    const nextName = window.prompt('Enter a new name for this workspace.', chat.name)?.trim();

    if (!nextName || nextName === chat.name) {
      return;
    }

    try {
      const updatedChat = await renameChat(chat.id, nextName);
      setChat(updatedChat);
      notify('Workspace renamed successfully.', 'success');
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Unable to rename workspace.';
      setErrorMessage(message);
      notify(message, 'error');
    }
  }

  async function handleLogout() {
    await logout();
    window.location.assign('/');
  }

  function formatFileSize(size: number) {
    if (size < 1024) {
      return `${size} B`;
    }

    if (size < 1024 * 1024) {
      return `${(size / 1024).toFixed(1)} KB`;
    }

    return `${(size / (1024 * 1024)).toFixed(1)} MB`;
  }

  if (!Number.isFinite(chatId)) {
    return null;
  }

  return (
    <main className="dashboard-shell">
      <AppHeader
        links={[
          { label: 'Home', to: '/' },
          { label: 'Workspace', to: '/dashboard' },
          { label: 'Chat', to: `/chat/${chatId}` }
        ]}
        currentLabel={chat?.name ?? 'Chat'}
        actions={
          <>
            <Link className="ghost-button" to="/dashboard">
              Back to workspace
            </Link>
            <button className="ghost-button" type="button" onClick={handleLogout}>
              Logout
            </button>
          </>
        }
      />

      <header className="hero">
        <div>
          <p className="eyebrow">Document Chat</p>
          <h1>{chat?.name ?? 'Loading chat...'}</h1>
          <p className="lede">
            Upload documents and ask questions inside this dedicated workspace.
          </p>
        </div>

        <div className="hero-actions">
          <button className="ghost-button" type="button" onClick={() => void handleRenameChat()} disabled={deletingChat}>
            Rename workspace
          </button>
          <button className="ghost-button danger-button" type="button" onClick={handleDeleteChat} disabled={deletingChat}>
            {deletingChat ? 'Deleting chat...' : 'Delete chat'}
          </button>
        </div>
      </header>

      {errorMessage ? (
        <section className="status-strip">
          <p className="status error">{errorMessage}</p>
        </section>
      ) : null}

      <section className="dashboard-grid dashboard-chat-grid">
        <form className="panel" onSubmit={handleUpload}>
          <div className="panel-header">
            <div>
              <p className="card-kicker">Upload</p>
              <h2>Add documents to this chat</h2>
            </div>
          </div>

          <label className="upload-dropzone">
            <input type="file" onChange={handleFileChange} />
            <span>{selectedFile ? selectedFile.name : 'Choose a file to upload'}</span>
          </label>

          <button className="primary-button" type="submit" disabled={uploading || loadingChat}>
            {uploading ? 'Uploading...' : 'Upload document'}
          </button>
        </form>

        <form className="panel chat-panel" onSubmit={handleAsk}>
          <div className="panel-header">
            <div>
              <p className="card-kicker">Chat</p>
              <h2>Ask about this chat's documents</h2>
            </div>
          </div>

          <label className="field">
            <span>Question</span>
            <textarea
              rows={6}
              value={question}
              onChange={(event) => setQuestion(event.target.value)}
              placeholder="Ask something about the uploaded documents"
            />
          </label>

          <button className="primary-button secondary-accent" type="submit" disabled={asking || loadingChat}>
            {asking ? 'Thinking...' : 'Ask'}
          </button>

          {answer ? (
            <section className="answer-card">
              <p className="card-kicker">Answer</p>
              <div className="answer-content">{renderAnswer(answer)}</div>
            </section>
          ) : null}
        </form>
      </section>

      <section className="panel">
        <div className="panel-header">
          <div>
            <p className="card-kicker">History</p>
            <h2>Conversation</h2>
          </div>
          <button className="ghost-button" type="button" onClick={() => void refreshMessages()}>
            Refresh
          </button>
        </div>

        {messages.length === 0 ? (
          <p className="empty-state">No messages yet. Ask your first question to start the chat history.</p>
        ) : (
          <div className="message-list">
            {messages.map((message) => (
              <article className={`message-card ${message.role === 'USER' ? 'user-message' : 'assistant-message'}`} key={message.id}>
                <p className="card-kicker">{message.role === 'USER' ? 'You' : 'Gyan'}</p>
                {message.role === 'ASSISTANT' ? (
                  <div className="answer-content">{renderAnswer(message.content)}</div>
                ) : (
                  <p className="answer-paragraph">{message.content}</p>
                )}
              </article>
            ))}
          </div>
        )}
      </section>

      <section className="panel">
        <div className="panel-header">
          <div>
            <p className="card-kicker">Documents</p>
            <h2>Files in this chat</h2>
          </div>
          <button className="ghost-button" type="button" onClick={() => void refreshDocuments()}>
            Refresh
          </button>
        </div>

        {loadingChat ? <p className="empty-state">Loading chat workspace...</p> : null}

        {!loadingChat && documents.length === 0 ? (
          <p className="empty-state">No documents uploaded to this chat yet.</p>
        ) : null}

        {!loadingChat && documents.length > 0 ? (
          <div className="document-list">
            {documents.map((document) => (
              <article className="document-card" key={document.id}>
                <div>
                  <h3>{document.fileName}</h3>
                  <p>{formatFileSize(document.fileSize)}</p>
                  <p className={`document-status status-${(document.processingStatus ?? 'unknown').toLowerCase()}`}>
                    {document.processingStatus === 'FAILED'
                      ? `Failed${document.processingError ? `: ${document.processingError}` : ''}`
                      : document.processingStatus ?? 'Unknown'}
                  </p>
                  {document.processingMessage ? <p>{document.processingMessage}</p> : null}
                </div>

                <div className="row-actions">
                  <button
                    className="ghost-button"
                    type="button"
                    onClick={() => void handlePreview(document)}
                    disabled={viewingId === document.id || deletingDocumentId === document.id}
                  >
                    {viewingId === document.id ? 'Opening...' : 'Preview'}
                  </button>
                  <button
                    className="ghost-button"
                    type="button"
                    onClick={() => void handleDownload(document.id)}
                    disabled={viewingId === document.id || deletingDocumentId === document.id}
                  >
                    {viewingId === document.id ? 'Downloading...' : 'Download'}
                  </button>
                  <button
                    className="ghost-button danger-button"
                    type="button"
                    onClick={() => void handleDeleteDocument(document)}
                    disabled={deletingDocumentId === document.id || viewingId === document.id}
                  >
                    {deletingDocumentId === document.id ? 'Deleting...' : 'Delete'}
                  </button>
                </div>
              </article>
            ))}
          </div>
        ) : null}
      </section>

      {previewUrl ? (
        <section className="preview-overlay" role="dialog" aria-modal="true">
          <div className="preview-panel">
            <div className="panel-header">
              <div>
                <p className="card-kicker">Preview</p>
                <h2>{previewFileName}</h2>
              </div>
              <button
                className="ghost-button"
                type="button"
                onClick={() => {
                  window.URL.revokeObjectURL(previewUrl);
                  setPreviewUrl(null);
                  setPreviewContentType('');
                  setPreviewFileName('');
                }}
              >
                Close
              </button>
            </div>

            {previewContentType.startsWith('text/') ? (
              <iframe className="preview-frame" src={previewUrl} title={previewFileName} />
            ) : previewContentType.includes('pdf') ? (
              <iframe className="preview-frame" src={previewUrl} title={previewFileName} />
            ) : (
              <div className="preview-fallback">
                <p className="empty-state">
                  This file type may not render inline in the browser. You can still download it from the document list.
                </p>
                <iframe className="preview-frame" src={previewUrl} title={previewFileName} />
              </div>
            )}
          </div>
        </section>
      ) : null}
    </main>
  );
}
