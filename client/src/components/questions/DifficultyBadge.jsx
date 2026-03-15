import React from "react";
import styles from "./DifficultyBadge.module.css";

export default function DifficultyBadge({ difficulty }) {
  const cls =
    difficulty === "Easy"
      ? styles.easy
      : difficulty === "Medium"
      ? styles.medium
      : styles.hard;

  return <span className={`${styles.badge} ${cls}`}>{difficulty}</span>;
}
