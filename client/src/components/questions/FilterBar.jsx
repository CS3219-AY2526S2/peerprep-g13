import React from "react";
import { TOPICS, DIFFICULTIES } from "./constants";
import styles from "./FilterBar.module.css";

export default function FilterBar({ filters, onFilterChange }) {
  function handleChange(e) {
    onFilterChange({ ...filters, [e.target.name]: e.target.value });
  }

  return (
    <div className={styles.filterBar}>
      <select
        name="topic"
        value={filters.topic}
        onChange={handleChange}
        className={styles.select}
      >
        <option value="">All Topics</option>
        {TOPICS.map((t) => (
          <option key={t} value={t}>
            {t}
          </option>
        ))}
      </select>

      <select
        name="difficulty"
        value={filters.difficulty}
        onChange={handleChange}
        className={styles.select}
      >
        <option value="">All Difficulties</option>
        {DIFFICULTIES.map((d) => (
          <option key={d} value={d}>
            {d}
          </option>
        ))}
      </select>
    </div>
  );
}
