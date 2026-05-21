import { FormEvent, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AppHeader } from '../components/AppHeader';
import { ChatRecord, createChat, deleteChat, getChats, logout, renameChat } from '../lib/api';
import { useNotifications } from '../components/NotificationProvider';

export function DashboardPage() {
  const navigate = useNavigate();
  const { notify } = useNotifications();
  const [chats, setChats] = useState<ChatRecord[]>([]);
  const [chatName, setChatName] = useState('');
  const [loadingChats, setLoadingChats] = useState(true);
  const [creatingChat, setCreatingChat] = useState(false);
  const [deletingChatId, setDeletingChatId] = useState<number | null>(null);
  const [errorMessage, setErrorMessage] = useState('');

  async function loadChats() {
    setLoadingChats(true);
    setErrorMessage('');

    try {
      const response = await getChats();
      setChats(response);
    } catch (err) {
      setErrorMessage(err instanceof Error ? err.message : 'Unable to load chats.');
    } finally {
      setLoadingChats(false);
    }
  }

  useEffect(() => {
    void loadChats();
  }, []);

  async function handleCreateChat(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!chatName.trim()) {
      setErrorMessage('Enter a chat name first.');
      return;
    }

    setCreatingChat(true);
    setErrorMessage('');

    try {
      const chat = await createChat(chatName.trim());
      notify('Chat created successfully.', 'success');
      setChatName('');
      await loadChats();
      navigate(`/chat/${chat.id}`);
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Unable to create chat.';
      setErrorMessage(message);
      notify(message, 'error');
    } finally {
      setCreatingChat(false);
    }
  }

  async function handleLogout() {
    await logout();
    window.location.assign('/');
  }

  async function handleDeleteChat(chat: ChatRecord) {
    const confirmed = window.confirm(`Delete chat "${chat.name}" and all its documents?`);

    if (!confirmed) {
      return;
    }

    setDeletingChatId(chat.id);
    setErrorMessage('');

    try {
      await deleteChat(chat.id);
      setChats((current) => current.filter((item) => item.id !== chat.id));
      notify('Chat deleted successfully.', 'success');
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Unable to delete chat.';
      setErrorMessage(message);
      notify(message, 'error');
    } finally {
      setDeletingChatId(null);
    }
  }

  async function handleRenameChat(chat: ChatRecord) {
    const nextName = window.prompt('Enter a new name for this workspace.', chat.name)?.trim();

    if (!nextName || nextName === chat.name) {
      return;
    }

    try {
      const updatedChat = await renameChat(chat.id, nextName);
      setChats((current) => current.map((item) => (item.id === chat.id ? updatedChat : item)));
      notify('Workspace renamed successfully.', 'success');
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Unable to rename workspace.';
      setErrorMessage(message);
      notify(message, 'error');
    }
  }

  function formatDate(value: string) {
    return new Date(value).toLocaleString();
  }

  return (
    <main className="dashboard-shell">
      <AppHeader
        links={[
          { label: 'Home', to: '/' },
          { label: 'Workspace', to: '/dashboard' }
        ]}
        actions={
          <button className="ghost-button" type="button" onClick={handleLogout}>
            Logout
          </button>
        }
      />

      <header className="hero">
        <div>
          <p className="eyebrow">Chat Workspace</p>
          <h1>Your chats</h1>
          <p className="lede">
            Create up to five focused workspaces, each with its own documents and chat context.
          </p>
        </div>

        <div className="hero-actions" />
      </header>

      <section className="dashboard-grid">
        <form className="panel" onSubmit={handleCreateChat}>
          <div className="panel-header">
            <div>
              <p className="card-kicker">New Chat</p>
              <h2>Create a workspace</h2>
            </div>
          </div>

          <label className="field">
            <span>Chat name</span>
            <input
              type="text"
              value={chatName}
              onChange={(event) => setChatName(event.target.value)}
              placeholder="Quarterly invoices"
              maxLength={80}
            />
          </label>

          <button className="primary-button" type="submit" disabled={creatingChat || chats.length >= 5}>
            {creatingChat ? 'Creating...' : chats.length >= 5 ? 'Chat limit reached' : 'Create chat'}
          </button>

          <p className="empty-state">You can create up to 5 chats per account.</p>
        </form>
      </section>

      {errorMessage && (
        <section className="status-strip">
          <p className="status error">{errorMessage}</p>
        </section>
      )}

      <section className="panel">
        <div className="panel-header">
          <div>
            <p className="card-kicker">Chat List</p>
            <h2>Your workspaces</h2>
          </div>
          <button className="ghost-button" type="button" onClick={() => void loadChats()}>
            Refresh
          </button>
        </div>

        {loadingChats ? <p className="empty-state">Loading chats...</p> : null}

        {!loadingChats && chats.length === 0 ? (
          <p className="empty-state">No chats created yet.</p>
        ) : null}

        {!loadingChats && chats.length > 0 ? (
          <div className="document-list">
            {chats.map((chat) => (
              <article className="document-card" key={chat.id}>
                <div>
                  <h3>{chat.name}</h3>
                  <p>
                    {chat.documentCount} {chat.documentCount === 1 ? 'document' : 'documents'} · Updated{' '}
                    {formatDate(chat.updatedAt)}
                  </p>
                </div>

                <div className="row-actions">
                  <Link className="ghost-button" to={`/chat/${chat.id}`}>
                    Open chat
                  </Link>
                  <button className="ghost-button" type="button" onClick={() => void handleRenameChat(chat)}>
                    Rename
                  </button>
                  <button
                    className="ghost-button danger-button"
                    type="button"
                    onClick={() => void handleDeleteChat(chat)}
                    disabled={deletingChatId === chat.id}
                  >
                    {deletingChatId === chat.id ? 'Deleting...' : 'Delete'}
                  </button>
                </div>
              </article>
            ))}
          </div>
        ) : null}
      </section>
    </main>
  );
}
