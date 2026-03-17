import React from "react";
import styles from "./DifficultyBadge.module.css";

export default function DifficultyBadge({ difficulty }) {
  const lower = difficulty?.toLowerCase();
  const cls =
    lower === "easy"
      ? styles.easy
      : lower === "medium"
      ? styles.medium
      : styles.hard;

  return <span className={`${styles.badge} ${cls}`}>{difficulty}</span>;
}
