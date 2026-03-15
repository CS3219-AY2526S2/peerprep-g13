import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { questionApi } from "../../api/question";
import QuestionForm from "../../components/questions/QuestionForm";
import styles from "./EditQuestionPage.module.css";

export default function EditQuestionPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [question, setQuestion] = useState(null);
  const [fetchLoading, setFetchLoading] = useState(true);
  const [loadError, setLoadError] = useState("");

  useEffect(() => {
    async function load() {
      try {
        const res = await questionApi.getById(id);
        setQuestion(res.data.question);
      } catch (err) {
        setLoadError(err?.response?.data?.message || "Failed to load question.");
      } finally {
        setFetchLoading(false);
      }
    }
    load();
  }, [id]);

  async function handleEdit(formData) {
    await questionApi.update(id, formData);
    navigate(`/questions/${id}`);
  }

  if (fetchLoading) {
    return (
      <div className={styles.page}>
        <div className={styles.inner}>
          <div className={styles.loadingState}>Loading…</div>
        </div>
      </div>
    );
  }

  if (loadError) {
    return (
      <div className={styles.page}>
        <div className={styles.inner}>
          <div className={styles.errorState}>{loadError}</div>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <div className={styles.inner}>
        <h1 className={styles.pageTitle}>Edit Question</h1>
        <div className={styles.card}>
          {question !== null && (
            <QuestionForm
              mode="edit"
              initialData={question}
              onSubmit={handleEdit}
            />
          )}
        </div>
      </div>
    </div>
  );
}
