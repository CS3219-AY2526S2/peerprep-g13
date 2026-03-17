import React from "react";
import { useAuth } from "../../context/ContextProvider";
import styles from "./DashboardPage.module.css";

function formatDate(value) {
  if (!value) return "N/A";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleDateString("en-US", {
    year: "numeric",
    month: "short",
    day: "numeric",
  });
}

function formatDateTime(value) {
  if (!value) return "N/A";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleString("en-US", {
    year: "numeric",
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

/** Minimal SVG donut ring */
function SolvedRing({ solved, total, size = 120, stroke = 10 }) {
  const r = (size - stroke) / 2;
  const circ = 2 * Math.PI * r;
  const pct = total > 0 ? Math.min(solved / total, 1) : 0;
  const dash = pct * circ;

  return (
    <div className={styles.ringWrapper} style={{ width: size, height: size }}>
      <svg width={size} height={size}>
        {/* track */}
        <circle
          cx={size / 2}
          cy={size / 2}
          r={r}
          fill="none"
          stroke="#f0f0f0"
          strokeWidth={stroke}
        />
        {/* filled arc */}
        <circle
          cx={size / 2}
          cy={size / 2}
          r={r}
          fill="none"
          stroke="#00b8a3"
          strokeWidth={stroke}
          strokeLinecap="round"
          strokeDasharray={`${dash} ${circ}`}
          style={{ transition: "stroke-dasharray 0.6s ease" }}
        />
      </svg>
      <div className={styles.ringCenter}>
        <span className={styles.ringCount}>{solved}</span>
        <span className={styles.ringTotal}>/ {total}</span>
      </div>
    </div>
  );
}

export default function DashboardPage() {
  const { user, dashboard, loading } = useAuth();

  if (loading) {
    return <div className={styles.loadingState}>Loading dashboard…</div>;
  }

  if (!user) {
    return <div className={styles.loadingState}>No user data found.</div>;
  }

  const history = dashboard?.history || [];
  const metrics = dashboard?.metrics || {};

  const displayName = user.name || user.displayName || "Unknown User";
  const initials = displayName
    .split(" ")
    .map((w) => w[0])
    .slice(0, 2)
    .join("")
    .toUpperCase();
  const preferredTopics = user.preferredTopic || [];
  const totalSolved = metrics.totalSolved ?? history.length;

  // Placeholder totals — replace with real data when available
  const totalQuestions = metrics.totalQuestions ?? 150;

  const roleClass =
    user.role === "ADMIN"
      ? styles.admin
      : user.role === "QUESTION_MASTER"
        ? styles.question_master
        : "";

  return (
    <div className={styles.page}>
      <div className={styles.inner}>
        <p className={styles.pageTitle}>My Profile</p>

        <div className={styles.grid}>
          {/* ── Left column ── */}
          <div className={styles.left}>
            {/* Profile card */}
            <div className={`${styles.card} ${styles.profileCard}`}>
              <div className={styles.avatarWrapper}>
                {user.avatarUrl ? (
                  <img
                    src={user.avatarUrl}
                    alt={displayName}
                    className={styles.avatar}
                    style={{ objectFit: "cover" }}
                  />
                ) : (
                  <div className={styles.avatar}>{initials}</div>
                )}
              </div>

              <p className={styles.displayName}>{displayName}</p>
              <p className={styles.username}>@{user.username || "unknown"}</p>

              <span className={`${styles.roleBadge} ${roleClass}`}>
                {user.role || "user"}
              </span>

              <hr className={styles.divider} />

              <div className={styles.profileMeta}>
                <div className={styles.metaRow}>
                  <span className={styles.metaLabel}>Language</span>
                  <span className={styles.metaValue}>
                    {user.preferredLanguage || "Not set"}
                  </span>
                </div>

                <div className={styles.metaRow}>
                  <span className={styles.metaLabel}>Topics</span>
                  {preferredTopics.length > 0 ? (
                    <div className={styles.topicsList}>
                      {preferredTopics.map((t) => (
                        <span key={t} className={styles.topicChip}>
                          {t}
                        </span>
                      ))}
                    </div>
                  ) : (
                    <span className={styles.metaValue}>Not set</span>
                  )}
                </div>
              </div>
            </div>

            {/* Account info card */}
            <div className={`${styles.card} ${styles.infoCard}`}>
              <p className={styles.cardTitle}>Account Info</p>
              <div className={styles.infoRow}>
                <span className={styles.infoKey}>User ID</span>
                <span className={styles.infoVal} title={user.userId}>
                  {user.userId ?? "N/A"}
                </span>
              </div>
              <div className={styles.infoRow}>
                <span className={styles.infoKey}>Joined</span>
                <span className={styles.infoVal}>
                  {formatDate(user.createdAt)}
                </span>
              </div>
              <div className={styles.infoRow}>
                <span className={styles.infoKey}>Last updated</span>
                <span className={styles.infoVal}>
                  {formatDate(user.updatedAt)}
                </span>
              </div>
              <div className={styles.infoRow}>
                <span className={styles.infoKey}>Status</span>
                <span className={styles.infoVal}>
                  {user.isActive !== false ? "Active" : "Inactive"}
                </span>
              </div>
            </div>
          </div>

          {/* ── Right column ── */}
          <div className={styles.right}>
            {/* Solved widget */}
            <div className={`${styles.card} ${styles.solvedWidget}`}>
              <SolvedRing solved={totalSolved} total={totalQuestions} />
              <div className={styles.ringLegend}>
                <p className={styles.cardTitle} style={{ marginBottom: 12 }}>
                  Problems Solved
                </p>
                <div className={styles.legendItem}>
                  <span
                    className={styles.legendDot}
                    style={{ background: "#00b8a3" }}
                  />
                  <span className={styles.legendText}>Solved</span>
                  <span className={styles.legendCount}>{totalSolved}</span>
                </div>
                <div className={styles.legendItem}>
                  <span
                    className={styles.legendDot}
                    style={{
                      background: "#f0f0f0",
                      border: "1px solid #d1d5db",
                    }}
                  />
                  <span className={styles.legendText}>Remaining</span>
                  <span className={styles.legendCount}>
                    {Math.max(totalQuestions - totalSolved, 0)}
                  </span>
                </div>
              </div>
            </div>

            {/* Quick-stat cards */}
            <div className={styles.statsRow}>
              <div className={`${styles.card} ${styles.statCard}`}>
                <div className={`${styles.statNumber} ${styles.green}`}>
                  {totalSolved}
                </div>
                <div className={styles.statLabel}>Total Solved</div>
              </div>
              <div className={`${styles.card} ${styles.statCard}`}>
                <div className={styles.statNumber}>{history.length}</div>
                <div className={styles.statLabel}>Submissions</div>
              </div>
              <div className={`${styles.card} ${styles.statCard}`}>
                <div className={styles.statNumber}>
                  {history.length > 0
                    ? `${Math.round((totalSolved / totalQuestions) * 100)}%`
                    : "0%"}
                </div>
                <div className={styles.statLabel}>Completion</div>
              </div>
            </div>

            {/* Solving history */}
            <div className={`${styles.card} ${styles.historyCard}`}>
              <p className={styles.cardTitle}>Recent Submissions</p>

              {history.length === 0 ? (
                <div className={styles.emptyState}>
                  No submissions yet — start solving!
                </div>
              ) : (
                <table className={styles.table}>
                  <thead>
                    <tr>
                      <th>#</th>
                      <th>Question ID</th>
                      <th>Status</th>
                      <th>Finished</th>
                    </tr>
                  </thead>
                  <tbody>
                    {[...history]
                      .sort(
                        (a, b) =>
                          new Date(b.finishedDate) - new Date(a.finishedDate)
                      )
                      .map((item, index) => (
                        <tr key={`${item.questionId}-${index}`}>
                          <td style={{ color: "#9ca3af", width: 32 }}>
                            {index + 1}
                          </td>
                          <td>
                            <span className={styles.qId}>
                              {item.questionId}
                            </span>
                          </td>
                          <td>
                            <span className={styles.statusPill}>Solved</span>
                          </td>
                          <td>{formatDateTime(item.finishedDate)}</td>
                        </tr>
                      ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
