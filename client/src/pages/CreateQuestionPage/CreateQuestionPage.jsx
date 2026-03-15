import React from "react";
import { useNavigate } from "react-router-dom";
import { questionApi } from "../../api/question";
import QuestionForm from "../../components/questions/QuestionForm";
import styles from "./CreateQuestionPage.module.css";

export default function CreateQuestionPage() {
  const navigate = useNavigate();

  async function handleCreate(formData) {
    await questionApi.create(formData);
    navigate("/questions");
  }

  return (
    <div className={styles.page}>
      <div className={styles.inner}>
        <h1 className={styles.pageTitle}>New Question</h1>
        <div className={styles.card}>
          <QuestionForm mode="create" onSubmit={handleCreate} />
        </div>
      </div>
    </div>
  );
}
