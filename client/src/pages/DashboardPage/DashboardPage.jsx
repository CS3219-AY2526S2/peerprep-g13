import React, { useState } from "react";
import { MultiSelect } from "@mantine/core";
import { useAuth } from "../../context/ContextProvider";
import { userApi } from "../../api/user";
import { TOPICS } from "../../components/questions/constants";
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
        <circle cx={size / 2} cy={size / 2} r={r} fill="none" stroke="#f0f0f0" strokeWidth={stroke} />
        <circle
          cx={size / 2} cy={size / 2} r={r} fill="none"
          stroke="#00b8a3" strokeWidth={stroke} strokeLinecap="round"
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
  const { user, dashboard, loading, returnDashboard } = useAuth();
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState({});
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState("");
  const [saveSuccess, setSaveSuccess] = useState("");

  const [changingPassword, setChangingPassword] = useState(false);
  const [passwordForm, setPasswordForm] = useState({ currentPassword: "", newPassword: "", confirmPassword: "" });
  const [passwordSaving, setPasswordSaving] = useState(false);
  const [passwordError, setPasswordError] = useState("");
  const [passwordSuccess, setPasswordSuccess] = useState("");

  if (loading) return <div className={styles.loadingState}>Loading dashboard…</div>;
  if (!user) return <div className={styles.loadingState}>No user data found.</div>;

  const history = dashboard?.history || [];
  const metrics = dashboard?.metrics || {};
  const displayName = user.name || user.username || "Unknown User";
  const initials = displayName.split(" ").map((w) => w[0]).slice(0, 2).join("").toUpperCase();
  const preferredTopics = user.preferredTopic || [];
  const totalSolved = metrics.totalSolved ?? history.length;
  const totalQuestions = metrics.totalQuestions ?? 150;

  const roleClass =
    user.role === "admin"
      ? styles.admin
      : user.role === "question_master"
        ? styles.question_master
        : "";

  function openEdit() {
    setSaveError("");
    setSaveSuccess("");
    setForm({
      name: user.name || "",
      username: user.username || "",
      avatarUrl: user.avatarUrl || "",
      preferredLanguage: user.preferredLanguage || "",
      preferredTopic: user.preferredTopic || [],
    });
    setChangingPassword(false);
    setEditing(true);
  }

  function openChangePassword() {
    setPasswordError("");
    setPasswordSuccess("");
    setPasswordForm({ currentPassword: "", newPassword: "", confirmPassword: "" });
    setEditing(false);
    setChangingPassword(true);
  }

  async function handlePasswordChange(e) {
    e.preventDefault();
    setPasswordError("");
    setPasswordSuccess("");

    if (!passwordForm.currentPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
      setPasswordError("Please fill in all password fields.");
      return;
    }
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setPasswordError("New passwords do not match.");
      return;
    }
    if (passwordForm.newPassword.length < 8) {
      setPasswordError("New password must be at least 8 characters.");
      return;
    }
    if (!/[A-Z]/.test(passwordForm.newPassword)) {
      setPasswordError("New password must contain at least one uppercase letter.");
      return;
    }
    if (!/[a-z]/.test(passwordForm.newPassword)) {
      setPasswordError("New password must contain at least one lowercase letter.");
      return;
    }
    if (!/[0-9]/.test(passwordForm.newPassword)) {
      setPasswordError("New password must contain at least one digit.");
      return;
    }
    if (!/[!@#$%^&*()]/.test(passwordForm.newPassword)) {
      setPasswordError("New password must contain at least one special character: ! @ # $ % ^ & * ( )");
      return;
    }

    setPasswordSaving(true);
    try {
      await userApi.changePassword({
        currentPassword: passwordForm.currentPassword,
        newPassword: passwordForm.newPassword,
      });
      setPasswordSuccess("Password updated successfully.");
      setPasswordForm({
        currentPassword: "",
        newPassword: "",
        confirmPassword: "",
      });
      setChangingPassword(false);
    } catch (err) {
      setPasswordError(err?.response?.data?.message || "Failed to update password.");
    } finally {
      setPasswordSaving(false);
    }
  }

  async function handleSave(e) {
    e.preventDefault();
    setSaving(true);
    setSaveError("");
    setSaveSuccess("");
    try {
      await userApi.updateDashboard(user.userId, form);
      setSaveSuccess("Profile updated.");
      setEditing(false);
      await returnDashboard();
    } catch (err) {
      setSaveError(err?.response?.data?.message || "Failed to save.");
    } finally {
      setSaving(false);
    }
  }

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
                  <img src={user.avatarUrl} alt={displayName} className={styles.avatar} style={{ objectFit: "cover" }} />
                ) : (
                  <div className={styles.avatar}>{initials}</div>
                )}
              </div>

              <p className={styles.displayName}>{displayName}</p>
              <p className={styles.username}>@{user.username || "unknown"}</p>
              <span className={`${styles.roleBadge} ${roleClass}`}>{user.role || "user"}</span>

              <hr className={styles.divider} />

              <div className={styles.profileMeta}>
                <div className={styles.metaRow}>
                  <span className={styles.metaLabel}>Language</span>
                  <span className={styles.metaValue}>{user.preferredLanguage || "Not set"}</span>
                </div>
                <div className={styles.metaRow}>
                  <span className={styles.metaLabel}>Topics</span>
                  {preferredTopics.length > 0 ? (
                    <div className={styles.topicsList}>
                      {preferredTopics.map((t) => (
                        <span key={t} className={styles.topicChip}>{t}</span>
                      ))}
                    </div>
                  ) : (
                    <span className={styles.metaValue}>Not set</span>
                  )}
                </div>
              </div>

              <hr className={styles.divider} />
              {saveSuccess && <p className={styles.saveSuccess}>{saveSuccess}</p>}
              {passwordSuccess && <p className={styles.saveSuccess}>{passwordSuccess}</p>}
              <button className={styles.editBtn} onClick={openEdit}>Edit Profile</button>
              <button className={styles.editBtn} style={{ marginTop: 8 }} onClick={openChangePassword}>Change Password</button>
            </div>

            {/* Edit form card */}
            {editing && (
              <div className={styles.card}>
                <p className={styles.cardTitle}>Edit Profile</p>
                <form onSubmit={handleSave}>
                  <div className={styles.fieldGroup}>
                    <label className={styles.fieldLabel}>Display Name</label>
                    <input
                      className={styles.fieldInput}
                      value={form.name}
                      onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))}
                    />
                  </div>
                  <div className={styles.fieldGroup}>
                    <label className={styles.fieldLabel}>Username</label>
                    <input
                      className={styles.fieldInput}
                      value={form.username}
                      onChange={(e) => setForm((f) => ({ ...f, username: e.target.value }))}
                    />
                  </div>
                  <div className={styles.fieldGroup}>
                    <label className={styles.fieldLabel}>Avatar URL</label>
                    <input
                      className={styles.fieldInput}
                      value={form.avatarUrl}
                      onChange={(e) => setForm((f) => ({ ...f, avatarUrl: e.target.value }))}
                    />
                  </div>
                  <div className={styles.fieldGroup}>
                    <label className={styles.fieldLabel}>Preferred Language</label>
                    <input
                      className={styles.fieldInput}
                      value={form.preferredLanguage}
                      onChange={(e) => setForm((f) => ({ ...f, preferredLanguage: e.target.value }))}
                    />
                  </div>
                  <div className={styles.fieldGroup}>
                    <MultiSelect
                      label={<span className={styles.fieldLabel}>Preferred Topics</span>}
                      data={TOPICS}
                      value={form.preferredTopic}
                      onChange={(val) => setForm((f) => ({ ...f, preferredTopic: val }))}
                      searchable
                      placeholder={form.preferredTopic.length === 0 ? "Select topics…" : ""}
                    />
                  </div>
                  {saveError && <p className={styles.saveError}>{saveError}</p>}
                  <div className={styles.formActions}>
                    <button type="submit" className={styles.saveBtn} disabled={saving}>
                      {saving ? "Saving…" : "Save"}
                    </button>
                    <button type="button" className={styles.cancelBtn} onClick={() => setEditing(false)} disabled={saving}>
                      Cancel
                    </button>
                  </div>
                </form>
              </div>
            )}

            {/* Change password card */}
            {changingPassword && (
              <div className={styles.card}>
                <p className={styles.cardTitle}>Change Password</p>
                <form onSubmit={handlePasswordChange}>
                  <div className={styles.fieldGroup}>
                    <label className={styles.fieldLabel}>Current Password</label>
                    <input
                      type="password"
                      className={styles.fieldInput}
                      value={passwordForm.currentPassword}
                      onChange={(e) => setPasswordForm((f) => ({ ...f, currentPassword: e.target.value }))}
                    />
                  </div>
                  <div className={styles.fieldGroup}>
                    <label className={styles.fieldLabel}>New Password</label>
                    <input
                      type="password"
                      className={styles.fieldInput}
                      value={passwordForm.newPassword}
                      onChange={(e) => setPasswordForm((f) => ({ ...f, newPassword: e.target.value }))}
                    />
                    <div style={{ marginTop: 6, display: "flex", flexDirection: "column", gap: 2 }}>
                      {[
                        { label: "At least 8 characters", met: passwordForm.newPassword.length >= 8 },
                        { label: "One uppercase letter (A–Z)", met: /[A-Z]/.test(passwordForm.newPassword) },
                        { label: "One lowercase letter (a–z)", met: /[a-z]/.test(passwordForm.newPassword) },
                        { label: "One digit (0–9)", met: /[0-9]/.test(passwordForm.newPassword) },
                        { label: "One special character: ! @ # $ % ^ & * ( )", met: /[!@#$%^&*()]/.test(passwordForm.newPassword) },
                      ].map(({ label, met }) => (
                        <span key={label} style={{ fontSize: "0.75rem", color: passwordForm.newPassword.length === 0 ? "#9ca3af" : met ? "#00876c" : "#dc2626" }}>
                          {met ? "✓" : "·"} {label}
                        </span>
                      ))}
                    </div>
                  </div>
                  <div className={styles.fieldGroup}>
                    <label className={styles.fieldLabel}>Retype New Password</label>
                    <input
                      type="password"
                      className={styles.fieldInput}
                      value={passwordForm.confirmPassword}
                      onChange={(e) => setPasswordForm((f) => ({ ...f, confirmPassword: e.target.value }))}
                    />
                  </div>
                  {passwordError && <p className={styles.saveError}>{passwordError}</p>}
                  <div className={styles.formActions}>
                    <button type="submit" className={styles.saveBtn} disabled={passwordSaving}>
                      {passwordSaving ? "Saving…" : "Update Password"}
                    </button>
                    <button type="button" className={styles.cancelBtn} onClick={() => setChangingPassword(false)} disabled={passwordSaving}>
                      Cancel
                    </button>
                  </div>
                </form>
              </div>
            )}

            {/* Account info card */}
            <div className={`${styles.card} ${styles.infoCard}`}>
              <p className={styles.cardTitle}>Account Info</p>
              <div className={styles.infoRow}>
                <span className={styles.infoKey}>User ID</span>
                <span className={styles.infoVal}>{user.userId ?? "N/A"}</span>
              </div>
              <div className={styles.infoRow}>
                <span className={styles.infoKey}>Joined</span>
                <span className={styles.infoVal}>{formatDate(user.createdAt)}</span>
              </div>
              <div className={styles.infoRow}>
                <span className={styles.infoKey}>Last updated</span>
                <span className={styles.infoVal}>{formatDate(user.updatedAt)}</span>
              </div>
              <div className={styles.infoRow}>
                <span className={styles.infoKey}>Status</span>
                <span className={styles.infoVal}>{user.isActive !== false ? "Active" : "Inactive"}</span>
              </div>
            </div>
          </div>

          {/* ── Right column ── */}
          <div className={styles.right}>
            <div className={`${styles.card} ${styles.solvedWidget}`}>
              <SolvedRing solved={totalSolved} total={totalQuestions} />
              <div className={styles.ringLegend}>
                <p className={styles.cardTitle} style={{ marginBottom: 12 }}>Problems Solved</p>
                <div className={styles.legendItem}>
                  <span className={styles.legendDot} style={{ background: "#00b8a3" }} />
                  <span className={styles.legendText}>Solved</span>
                  <span className={styles.legendCount}>{totalSolved}</span>
                </div>
                <div className={styles.legendItem}>
                  <span className={styles.legendDot} style={{ background: "#f0f0f0", border: "1px solid #d1d5db" }} />
                  <span className={styles.legendText}>Remaining</span>
                  <span className={styles.legendCount}>{Math.max(totalQuestions - totalSolved, 0)}</span>
                </div>
              </div>
            </div>

            <div className={styles.statsRow}>
              <div className={`${styles.card} ${styles.statCard}`}>
                <div className={`${styles.statNumber} ${styles.green}`}>{totalSolved}</div>
                <div className={styles.statLabel}>Total Solved</div>
              </div>
              <div className={`${styles.card} ${styles.statCard}`}>
                <div className={styles.statNumber}>{history.length}</div>
                <div className={styles.statLabel}>Submissions</div>
              </div>
              <div className={`${styles.card} ${styles.statCard}`}>
                <div className={styles.statNumber}>
                  {history.length > 0 ? `${Math.round((totalSolved / totalQuestions) * 100)}%` : "0%"}
                </div>
                <div className={styles.statLabel}>Completion</div>
              </div>
            </div>

            <div className={`${styles.card} ${styles.historyCard}`}>
              <p className={styles.cardTitle}>Recent Submissions</p>
              {history.length === 0 ? (
                <div className={styles.emptyState}>No submissions yet — start solving!</div>
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
                      .sort((a, b) => new Date(b.finishedDate) - new Date(a.finishedDate))
                      .map((item, index) => (
                        <tr key={`${item.questionId}-${index}`}>
                          <td style={{ color: "#9ca3af", width: 32 }}>{index + 1}</td>
                          <td><span className={styles.qId}>{item.questionId}</span></td>
                          <td><span className={styles.statusPill}>Solved</span></td>
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
