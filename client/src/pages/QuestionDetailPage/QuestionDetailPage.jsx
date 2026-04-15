import React, { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { questionApi } from "../../api/question";
import { useAuth } from "../../context/ContextProvider";
import DifficultyBadge from "../../components/questions/DifficultyBadge";
import styles from "./QuestionDetailPage.module.css";

export default function QuestionDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isQuestionManager } = useAuth();
  
  const [question, setQuestion] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [deleteConfirm, setDeleteConfirm] = useState(false);
  const [deleting, setDeleting] = useState(false);
  
  useEffect(() => {
    async function load() {
      try {
        const res = await questionApi.getById(id);
        setQuestion(res.data.question);
      } catch (err) {
        setError(err?.response?.data?.message || "Failed to load question.");
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [id]);

  async function handleDelete() {
    setDeleting(true);
    try {
      await questionApi.delete(id);
      navigate("/questions");
    } catch (err) {
      setError(err?.response?.data?.message || "Failed to delete question.");
      setDeleting(false);
      setDeleteConfirm(false);
    }
  }

  if (loading) {
    return (
      <div className={styles.page}>
        <div className={styles.inner}>
          <div className={styles.loadingState}>Loading…</div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className={styles.page}>
        <div className={styles.inner}>
          <div className={styles.errorState}>{error}</div>
        </div>
      </div>
    );
  }

  if (!question) return null;

  return (
    <div className={styles.page}>
      <div className={styles.inner}>
        <div className={styles.topBar}>
          <button className={styles.backBtn} onClick={() => navigate("/questions")}>
            ← Back to Questions
          </button>
        </div>

        <div className={styles.card}>
          <div className={styles.titleRow}>
            <h1 className={styles.title}>{question.title}</h1>
            <DifficultyBadge difficulty={question.difficulty} />
            {(question.topics ?? (question.topic ? [question.topic] : [])).map((t) => (
              <span key={t} className={styles.topicChip}>{t}</span>
            ))}
          </div>

          {isQuestionManager && (
            <div className={styles.actions}>
              <Link
                to={`/questions/edit/${question.id}`}
                className={styles.editLink}
              >
                Edit
              </Link>

              {!deleteConfirm ? (
                <button
                  className={styles.deleteBtn}
                  onClick={() => setDeleteConfirm(true)}
                >
                  Delete
                </button>
              ) : (
                <>
                  <span className={styles.confirmText}>Are you sure?</span>
                  <button
                    className={styles.confirmDeleteBtn}
                    onClick={handleDelete}
                    disabled={deleting}
                  >
                    {deleting ? "Deleting…" : "Yes, delete"}
                  </button>
                  <button
                    className={styles.cancelBtn}
                    onClick={() => setDeleteConfirm(false)}
                    disabled={deleting}
                  >
                    Cancel
                  </button>
                </>
              )}
            </div>
          )}

          <hr className={styles.divider} />

          <div>
            <p className={styles.sectionTitle}>Problem</p>
            <p className={styles.prompt}>{question.prompt}</p>
          </div>

          {question.examples && question.examples.length > 0 && (
            <>
              <hr className={styles.divider} />
              <div>
                <p className={styles.sectionTitle}>Examples</p>
                <ol className={styles.orderedList}>
                  {question.examples.map((ex, i) => (
                    <li key={i}>{ex}</li>
                  ))}
                </ol>
              </div>
            </>
          )}

          {question.constraints && question.constraints.length > 0 && (
            <>
              <hr className={styles.divider} />
              <div>
                <p className={styles.sectionTitle}>Constraints</p>
                <ul className={styles.unorderedList}>
                  {question.constraints.map((c, i) => (
                    <li key={i}>{c}</li>
                  ))}
                </ul>
              </div>
            </>
          )}

          {question.imageUrls && question.imageUrls.length > 0 && (
            <>
              <hr className={styles.divider} />
              <div>
                <p className={styles.sectionTitle}>Images</p>
                <div className={styles.imageGrid}>
                  {question.imageUrls.map((url, i) => (
                    <img
                      key={i}
                      src={url}
                      alt={`Question image ${i + 1}`}
                      className={styles.image}
                    />
                  ))}
                </div>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
