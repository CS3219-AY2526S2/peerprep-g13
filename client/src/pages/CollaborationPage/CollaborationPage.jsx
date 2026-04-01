import React, { useEffect, useRef, useState } from "react";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import * as Y from "yjs";
import { WebsocketProvider } from "y-websocket";
import { yCollab } from "y-codemirror.next";
import { EditorView, lineNumbers, highlightActiveLine, keymap } from "@codemirror/view";
import { EditorState } from "@codemirror/state";
import { defaultKeymap, historyKeymap, history, indentWithTab } from "@codemirror/commands";
import { questionApi } from "../../api/question";
import { useAuth } from "../../context/ContextProvider";
import DifficultyBadge from "../../components/questions/DifficultyBadge";
import styles from "./CollaborationPage.module.css";

export default function CollaborationPage() {
  const { roomId } = useParams();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const questionId = searchParams.get("questionId");
  const { user } = useAuth(); // kept in state so userRef stays current

  const [question, setQuestion] = useState(null);
  const [connected, setConnected] = useState(false);
  const [roomFull, setRoomFull] = useState(false);
  const [roomUsers, setRoomUsers] = useState([]);
  const [copied, setCopied] = useState(false);

  const editorRef = useRef(null);
  const userRef = useRef(user);
  const copiedTimerRef = useRef(null);
  useEffect(() => { userRef.current = user; }, [user]);
  useEffect(() => () => clearTimeout(copiedTimerRef.current), []);

  useEffect(() => {
    if (!questionId) return;
    questionApi
      .getById(questionId)
      .then((res) => setQuestion(res.data.question))
      .catch((err) => console.error("Failed to load question", questionId, err));
  }, [questionId]);

  useEffect(() => {
    if (!editorRef.current || !roomId) return;

    let view = null;
    let provider = null;
    let ydoc = null;

    // Defer setup so React StrictMode's double-invoke cleanup cancels before anything is created
    const timer = setTimeout(() => {
      ydoc = new Y.Doc();
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
    }, 0);

    return () => {
      clearTimeout(timer);
      view?.destroy();
      provider?.destroy();
      ydoc?.destroy();
    };
  }, [roomId]);

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
