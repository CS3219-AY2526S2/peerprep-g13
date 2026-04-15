import React, { useEffect, useRef, useState } from "react";
import { useNavigate, useParams, useLocation } from "react-router-dom";
import * as Y from "yjs";
import { questionApi } from "../../api/question";
import { WebsocketProvider } from "y-websocket";
import { yCollab } from "y-codemirror.next";
import { EditorView, lineNumbers, highlightActiveLine, keymap } from "@codemirror/view";
import { EditorState } from "@codemirror/state";
import { defaultKeymap, historyKeymap, history, indentWithTab } from "@codemirror/commands";
import { useAuth } from "../../context/ContextProvider";
import DifficultyBadge from "../../components/questions/DifficultyBadge";
import styles from "./CollaborationPage.module.css";

// Normalise question fields from any source (matching-service or question-service)
// so the rest of the component always sees `examples` (array) and `topics` (array).
const normaliseQuestion = (q) => {
  if (!q) return null;
  return {
    ...q,
    examples: q.examples ?? q.example ?? [],
    topics: q.topics ?? (q.topic ? [q.topic] : []),
  };
};

export default function CollaborationPage() {
  const { roomId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuth();

  const [question, setQuestion] = useState(
    normaliseQuestion(location.state?.question ?? null)
  );
  const [connected, setConnected] = useState(false);
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
  const knownUsersRef = useRef(null);
  useEffect(() => { userRef.current = user; }, [user]);
  useEffect(() => () => clearTimeout(copiedTimerRef.current), []);

  // Always fetch the full question from question-service on mount.
  // This guarantees complete data (examples, constraints) regardless of
  // what the matching-service payload contained.
  useEffect(() => {
    const qId = location.state?.question?.questionId;
    if (!qId) return;
    questionApi.getById(qId)
      .then(res => setQuestion(normaliseQuestion(res.data.question)))
      .catch(err => console.error("[Collab] Failed to fetch full question:", err));
  }, []);

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

    const timer = setTimeout(() => {
      ydoc = new Y.Doc();

      const roomState = ydoc.getMap("room-state");
      const loadQuestion = async () => {
        const questionId = roomState.get("questionId");
        if (questionId == null) return;
        try {
          const res = await questionApi.getById(questionId);
          setQuestion(normaliseQuestion(res.data.question));
        } catch (err) {
          console.error("[Collab] Failed to load question:", err);
        }
      };
      roomState.observe(loadQuestion);

      let wsUrl = import.meta.env.VITE_COLLAB_SERVICE_WS_URL || "ws://localhost:4000";
      if (!wsUrl.startsWith("wss://") && !wsUrl.includes("localhost") && !wsUrl.includes("127.0.0.1")) {
        wsUrl = wsUrl.replace(/^ws:\/\//, "wss://");
      }
      const token = localStorage.getItem("accessToken");

      provider = new WebsocketProvider(wsUrl, roomId, ydoc, {
         params: { token },
         disableBc: true,
         });
      providerRef.current = provider;

      provider.on("sync", (isSynced) => { if (isSynced) loadQuestion(); });
      loadQuestion();

      const ytext = ydoc.getText("codemirror");

      provider.awareness.on("change", () => {
        const seen = new Map();
        for (const s of provider.awareness.getStates().values()) {
          const u = s?.user;
          if (u?.userId && !seen.has(u.userId)) seen.set(u.userId, u);
        }
        setRoomUsers([...seen.values()]);
        if (provider.wsconnected) setConnected(true);

        const myId = localStorage.getItem("userId") || "anonymous";

        if (knownUsersRef.current === null) {
          // Initialize with only myself, then immediately notify for anyone already present
          const onlyMe = new Map();
          const myEntry = seen.get(myId);
          if (myEntry) onlyMe.set(myId, myEntry);
          knownUsersRef.current = onlyMe;

          for (const [uid, u] of seen) {
            if (uid !== myId) {
              pushNotification(`${u.username} joined`, "join");
            }
          }
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
        if (event?.code === 4004 || event?.code === 4003) {
          provider.shouldConnect = false;
          provider.disconnect();
          const error = event.code === 4004
            ? "Room not found. Please start a new match."
            : "You are not a participant of this room.";
          navigate("/matching", { state: { error } });
          return;
        }
        setConnected(false);
      });

      const handleWsMessage = (event) => {
        try {
          const buf = event.data instanceof ArrayBuffer
            ? new Uint8Array(event.data)
            : new Uint8Array(event.data.buffer ?? event.data);
          if (buf[0] === 2) setPartnerEnded(true);
        } catch (_) { /* ignore malformed */ }
      };
      provider.ws?.addEventListener("message", handleWsMessage);
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
      ws.send(new Uint8Array([2]));
    }
  }

  function confirmEndSession() {
    sendEndSessionMessage();
    setShowEndConfirm(false);
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

  const topics = question?.topics ?? [];

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