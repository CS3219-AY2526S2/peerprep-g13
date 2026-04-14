import React, { useEffect, useRef, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import * as Y from "yjs";
import { WebsocketProvider } from "y-websocket";
import { yCollab } from "y-codemirror.next";
import { EditorView, lineNumbers, highlightActiveLine, keymap } from "@codemirror/view";
import { EditorState } from "@codemirror/state";
import { defaultKeymap, historyKeymap, history, indentWithTab } from "@codemirror/commands";
import { useAuth } from "../../context/ContextProvider";
import DifficultyBadge from "../../components/questions/DifficultyBadge";
import styles from "./CollaborationPage.module.css";

export default function CollaborationPage() {
  const { roomId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuth(); // kept in state so userRef stays current
  
  const [question, setQuestion] = useState(null);
  const [connected, setConnected] = useState(false);
  const [roomFull, setRoomFull] = useState(false);
  const [roomUsers, setRoomUsers] = useState([]);
  const [copied, setCopied] = useState(false);
  const [showEndConfirm, setShowEndConfirm] = useState(false);
  const [partnerEnded, setPartnerEnded] = useState(false);
  const [notifications, setNotifications] = useState([]);

  const editorRef = useRef(null);
  const userRef = useRef(user);
  const copiedTimerRef = useRef(null);
  const providerRef = useRef(null);
  const viewRef = useRef(null);
  const ydocRef = useRef(null);
  const knownUsersRef = useRef(null); // null = not yet initialized
  useEffect(() => { userRef.current = user; }, [user]);
  useEffect(() => () => clearTimeout(copiedTimerRef.current), []);

  function pushNotification(message, type) {
    const id = Date.now() + Math.random();
    setNotifications((prev) => [...prev, { id, message, type }]);
    setTimeout(() => {
      setNotifications((prev) => prev.filter((n) => n.id !== id));
    }, 3000);
  }

  useEffect(() => {
    if (!editorRef.current || !roomId) return;

    let view = null;
    let provider = null;
    let ydoc = null;

    // Defer setup so React StrictMode's double-invoke cleanup cancels before anything is created
    const timer = setTimeout(() => {
      ydoc = new Y.Doc();

      const ymeta = ydoc.getMap("meta");
      if (!ymeta.has("question") && location.state?.question) {
        ymeta.set("question", location.state.question);
      }

      const updateQuestion = () => {
        const question = ymeta.get("question");
        if (question) setQuestion(question);
      };

      updateQuestion();
      ymeta.observe(updateQuestion);

      let wsUrl =
        import.meta.env.VITE_COLLAB_SERVICE_WS_URL || "ws://localhost:4000";
      // Enforce encrypted WebSocket in non-local environments
      if (!wsUrl.startsWith("wss://") && !wsUrl.includes("localhost") && !wsUrl.includes("127.0.0.1")) {
        wsUrl = wsUrl.replace(/^ws:\/\//, "wss://");
      }
      const token = localStorage.getItem("accessToken");

      provider = new WebsocketProvider(wsUrl, roomId, ydoc, {
        params: { token },
      });
      providerRef.current = provider;

      const ytext = ydoc.getText("codemirror");

      // Register listener before setLocalStateField so the first change event is captured
      provider.awareness.on("change", () => {
        // Deduplicate by userId — same user with multiple tabs = 1 person
        const seen = new Map();
        for (const s of provider.awareness.getStates().values()) {
          const u = s?.user;
          if (u?.userId && !seen.has(u.userId)) seen.set(u.userId, u);
        }
        setRoomUsers([...seen.values()]);
        if (provider.wsconnected) setConnected(true);

        const myId = localStorage.getItem("userId") || "anonymous";

        if (knownUsersRef.current === null) {
          // First fire — record initial state without showing notifications
          knownUsersRef.current = new Map(seen);
        } else {
          const prev = knownUsersRef.current;
          for (const [uid, u] of seen) {
            if (uid !== myId && !prev.has(uid)) {
              pushNotification(`${u.username} joined`, "join");
            }
          }
          for (const [uid, u] of prev) {
            if (uid !== myId && !seen.has(uid)) {
              pushNotification(`${u.username} left`, "leave");
            }
          }
          knownUsersRef.current = new Map(seen);
        }
      });

      const userId = localStorage.getItem("userId") || "anonymous";
      const username = userRef.current?.username || userId;
      provider.awareness.setLocalStateField("user", { userId, username });

      provider.on("status", ({ status }) => {
        if (status === "connected") setConnected(true);
        else if (status === "disconnected") setConnected(false);
      });

      provider.on("connection-close", (event) => {
        if (event?.code === 4003) setRoomFull(true);
        setConnected(false);
      });

      // Listen for MESSAGE_END_SESSION (type byte = 2) from the partner
      const handleWsMessage = (event) => {
        try {
          const buf = event.data instanceof ArrayBuffer
            ? new Uint8Array(event.data)
            : new Uint8Array(event.data.buffer ?? event.data);
          if (buf[0] === 2) setPartnerEnded(true);
        } catch (_) { /* ignore malformed */ }
      };
      provider.ws?.addEventListener("message", handleWsMessage);
      // Re-attach after reconnects (y-websocket recreates provider.ws on reconnect)
      provider.on("status", ({ status }) => {
        if (status === "connected") {
          provider.ws?.addEventListener("message", handleWsMessage);
        }
      });

      view = new EditorView({
        state: EditorState.create({
          extensions: [
            lineNumbers(),
            history(),
            highlightActiveLine(),
            keymap.of([...defaultKeymap, ...historyKeymap, indentWithTab]),
            yCollab(ytext, provider.awareness),
            EditorView.lineWrapping,
            EditorView.theme({
              "&": { height: "100%", fontSize: "13px", background: "#1e1e1e", color: "#d4d4d4" },
              ".cm-scroller": { overflow: "auto", fontFamily: "'Fira Mono', 'Consolas', monospace" },
              ".cm-content": { padding: "8px 0", caretColor: "#d4d4d4" },
              ".cm-gutters": { background: "#1e1e1e", borderRight: "1px solid #333", color: "#555" },
              ".cm-activeLine": { background: "#2a2a2a" },
              ".cm-activeLineGutter": { background: "#2a2a2a" },
              ".cm-cursor": { borderLeftColor: "#d4d4d4" },
              ".cm-selectionBackground": { background: "#264f78 !important" },
            }),
          ],
        }),
        parent: editorRef.current,
      });
      viewRef.current = view;
      ydocRef.current = ydoc;
    }, 0);

    return () => {
      clearTimeout(timer);
      viewRef.current = null;
      providerRef.current = null;
      ydocRef.current = null;
      view?.destroy();
      provider?.destroy();
      ydoc?.destroy();
    };
  }, [roomId]);

  function sendEndSessionMessage() {
    const ws = providerRef.current?.ws;
    if (ws?.readyState === WebSocket.OPEN) {
      // MESSAGE_END_SESSION = 2, encoded as a single varint byte
      ws.send(new Uint8Array([2]));
    }
  }

  function confirmEndSession() {
    sendEndSessionMessage();
    setShowEndConfirm(false);
    // Small delay so the message reaches the partner before we disconnect
    setTimeout(() => {
      viewRef.current?.destroy();
      providerRef.current?.destroy();
      ydocRef.current?.destroy();
      navigate("/questions");
    }, 300);
  }

  function copyRoomLink() {
    navigator.clipboard.writeText(window.location.href).then(() => {
      setCopied(true);
      clearTimeout(copiedTimerRef.current);
      copiedTimerRef.current = setTimeout(() => setCopied(false), 2000);
    }).catch((err) => console.error("Failed to copy link", err));
  }

  const topics = question?.topics ?? (question?.topic ? [question.topic] : []);

  if (roomFull) {
    return (
      <div className={styles.page}>
        <div className={styles.fullScreen}>
          <p className={styles.roomFullTitle}>Room is full</p>
          <p className={styles.roomFullSub}>
            This room already has 2 participants. Ask your partner to share a new room link.
          </p>
          <button className={styles.copyBtn} onClick={() => navigate("/questions")}>
            Back to Questions
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <div className={styles.toastContainer}>
        {notifications.map((n) => (
          <div
            key={n.id}
            className={`${styles.toast} ${n.type === "join" ? styles.toastJoin : styles.toastLeave}`}
          >
            {n.type === "join" ? "●" : "○"} {n.message}
          </div>
        ))}
      </div>

      {showEndConfirm && (
        <div className={styles.modalOverlay}>
          <div className={styles.modal}>
            <p className={styles.modalTitle}>End Session?</p>
            <p className={styles.modalSub}>Your partner will be notified that you have left.</p>
            <div className={styles.modalActions}>
              <button className={styles.cancelBtn} onClick={() => setShowEndConfirm(false)}>
                Cancel
              </button>
              <button className={styles.endBtn} onClick={confirmEndSession}>
                End Session
              </button>
            </div>
          </div>
        </div>
      )}

      {partnerEnded && (
        <div className={styles.modalOverlay}>
          <div className={styles.modal}>
            <p className={styles.modalTitle}>Session Ended</p>
            <p className={styles.modalSub}>Your partner has ended the session.</p>
            <div className={styles.modalActions}>
              <button className={styles.endBtn} onClick={() => navigate("/questions")}>
                Back to Questions
              </button>
            </div>
          </div>
        </div>
      )}

      <div className={styles.header}>
        <button className={styles.backBtn} onClick={() => navigate(-1)}>
          ← Back
        </button>

        <div className={styles.roomInfo}>
          <span className={styles.roomId}>Room: {roomId}</span>
          <span className={connected ? styles.statusOnline : styles.statusOffline}>
            {connected ? "Connected" : "Connecting…"}
          </span>
          <div className={styles.userChips}>
            {roomUsers.map((u) => (
              <span key={u.userId} className={styles.userChip}>
                <span className={styles.userAvatar}>{u.username[0].toUpperCase()}</span>
                {u.username}
              </span>
            ))}
          </div>
        </div>

        <div className={styles.controls}>
          <button className={styles.copyBtn} onClick={copyRoomLink}>
            {copied ? "Copied!" : "Copy Link"}
          </button>
          <button className={styles.endSessionBtn} onClick={() => setShowEndConfirm(true)}>
            End Session
          </button>
        </div>
      </div>

      <div className={styles.body}>
        <div className={styles.questionPanel}>
          {question ? (
            <>
              <div className={styles.titleRow}>
                <h2 className={styles.title}>{question.title}</h2>
                <DifficultyBadge difficulty={question.difficulty} />
              </div>

              {topics.length > 0 && (
                <div className={styles.topicRow}>
                  {topics.map((t) => (
                    <span key={t} className={styles.topicChip}>
                      {t}
                    </span>
                  ))}
                </div>
              )}

              <hr className={styles.divider} />

              <p className={styles.sectionLabel}>Problem</p>
              <p className={styles.prompt}>{question.prompt}</p>

              {question.examples?.length > 0 && (
                <>
                  <hr className={styles.divider} />
                  <p className={styles.sectionLabel}>Examples</p>
                  <ol className={styles.list}>
                    {question.examples.map((ex, i) => (
                      <li key={i}>{ex}</li>
                    ))}
                  </ol>
                </>
              )}

              {question.constraints?.length > 0 && (
                <>
                  <hr className={styles.divider} />
                  <p className={styles.sectionLabel}>Constraints</p>
                  <ul className={styles.list}>
                    {question.constraints.map((c, i) => (
                      <li key={i}>{c}</li>
                    ))}
                  </ul>
                </>
              )}
            </>
          ) : (
            <div className={styles.noQuestion}>
              <p className={styles.noQuestionTitle}>No question loaded</p>
              <p className={styles.noQuestionSub}>
                Share the room link with your partner to start collaborating.
              </p>
            </div>
          )}
        </div>

        <div className={styles.editorPanel}>
          <div ref={editorRef} className={styles.editor} />
        </div>
      </div>
    </div>
  );
}
