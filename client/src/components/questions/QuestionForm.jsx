import React, { useEffect, useState } from "react";
import { MultiSelect } from "@mantine/core";
import { TOPICS, DIFFICULTIES } from "./constants";
import DynamicListField from "./DynamicListField";
import styles from "./QuestionForm.module.css";

const EMPTY_FORM = {
  title: "",
  topics: [],
  difficulty: "",
  prompt: "",
  examples: [],
  constraints: [],
  imageUrls: [],
};

export default function QuestionForm({
  mode,
  initialData,
  onSubmit,
  submitLabel,
  loading,
}) {
  const [form, setForm] = useState(EMPTY_FORM);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (initialData) {
      setForm({
        title: initialData.title || "",
        topics: initialData.topics ?? (initialData.topic ? [initialData.topic] : []),
        difficulty: initialData.difficulty
          ? initialData.difficulty.charAt(0).toUpperCase() + initialData.difficulty.slice(1).toLowerCase()
          : "",
        prompt: initialData.prompt || "",
        examples: initialData.examples || [],
        constraints: initialData.constraints || [],
        imageUrls: initialData.imageUrls || [],
      });
    }
  }, [initialData]);

  function handleChange(e) {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  }

  function handleListChange(field, newItems) {
    setForm((prev) => ({ ...prev, [field]: newItems }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    if (form.topics.length === 0) {
      setError("Please select at least one topic.");
      return;
    }
    setSubmitting(true);
    try {
      const cleaned = {
        ...form,
        examples: form.examples.filter((s) => s.trim() !== ""),
        constraints: form.constraints.filter((s) => s.trim() !== ""),
        imageUrls: form.imageUrls.filter((s) => s.trim() !== ""),
      };
      await onSubmit(cleaned);
    } catch (err) {
      setError(err?.response?.data?.message || "Something went wrong.");
    } finally {
      setSubmitting(false);
    }
  }

  const isDisabled = submitting || loading;

  return (
    <form className={styles.form} onSubmit={handleSubmit}>
      {error && <p className={styles.error}>{error}</p>}

      <div className={styles.fieldGroup}>
        <label className={styles.label} htmlFor="title">
          Title
        </label>
        <input
          id="title"
          name="title"
          className={styles.input}
          value={form.title}
          onChange={handleChange}
          required
        />
      </div>

      <div className={styles.fieldGroup}>
        <MultiSelect
          label={<span className={styles.label}>Topics</span>}
          data={TOPICS}
          value={form.topics}
          onChange={(val) => setForm((prev) => ({ ...prev, topics: val }))}
          searchable
          placeholder={form.topics.length === 0 ? "Select topics…" : ""}
          required
          styles={{
            input: {
              border: "1px solid #e5e7eb",
              borderRadius: 6,
              fontSize: "0.875rem",
              color: "#374151",
            },
          }}
        />
      </div>

      <div className={styles.fieldGroup}>
        <label className={styles.label} htmlFor="difficulty">
          Difficulty
        </label>
        <select
          id="difficulty"
          name="difficulty"
          className={styles.select}
          value={form.difficulty}
          onChange={handleChange}
          required
        >
          <option value="">Select difficulty</option>
          {DIFFICULTIES.map((d) => (
            <option key={d} value={d}>
              {d}
            </option>
          ))}
        </select>
      </div>

      <div className={styles.fieldGroup}>
        <label className={styles.label} htmlFor="prompt">
          Problem Statement
        </label>
        <textarea
          id="prompt"
          name="prompt"
          className={styles.textarea}
          value={form.prompt}
          onChange={handleChange}
          required
        />
      </div>

      <DynamicListField
        label="Examples"
        items={form.examples}
        onChange={(items) => handleListChange("examples", items)}
        placeholder="e.g. Input: [1,2,3] → Output: 6"
      />

      <DynamicListField
        label="Constraints"
        items={form.constraints}
        onChange={(items) => handleListChange("constraints", items)}
        placeholder="e.g. 1 ≤ n ≤ 10^5"
      />

      <DynamicListField
        label="Image URLs"
        items={form.imageUrls}
        onChange={(items) => handleListChange("imageUrls", items)}
        placeholder="https://..."
        inputType="url"
      />

      <div style={{ marginTop: 8 }}>
        <button type="submit" className={styles.submitBtn} disabled={isDisabled}>
          {isDisabled ? "Saving…" : (submitLabel || (mode === "create" ? "Create Question" : "Save Changes"))}
        </button>
      </div>
    </form>
  );
}
