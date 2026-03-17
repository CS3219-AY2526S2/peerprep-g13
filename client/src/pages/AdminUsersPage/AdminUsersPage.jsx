import React, { useState } from "react";
import { Modal, Button, TextInput, Select, Text, Space } from "@mantine/core";
import { useAuth } from "../../context/ContextProvider";
import { userApi } from "../../api/user";
import styles from "./AdminUsersPage.module.css";

const ROLE_OPTIONS = [
  { value: "user", label: "User" },
  { value: "question-master", label: "Question Master" },
  { value: "admin", label: "Admin" },
];

export default function AdminUsersPage() {
  const { user } = useAuth();

  // ── Find user by username ──
  const [searchUsername, setSearchUsername] = useState("");
  const [searchResults, setSearchResults] = useState(null); // null = not searched yet
  const [searchLoading, setSearchLoading] = useState(false);
  const [searchError, setSearchError] = useState("");

  async function handleSearch(e) {
    e.preventDefault();
    if (!searchUsername.trim()) return;
    setSearchLoading(true);
    setSearchError("");
    setSearchResults(null);
    try {
      // TODO: replace with real API call when endpoint is available
      // e.g. const res = await userApi.findByUsername(searchUsername.trim());
      // setSearchResults(res.data.users);
      throw new Error("API not yet available");
    } catch {
      setSearchError("User search API is not yet available.");
    } finally {
      setSearchLoading(false);
    }
  }

  // ── Promote user ──
  const [targetId, setTargetId] = useState("");
  const [newRole, setNewRole] = useState(null);
  const [adminPassword, setAdminPassword] = useState("");
  const [modalOpen, setModalOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  function handlePromoteClick() {
    setError("");
    setSuccess("");
    if (!targetId || !newRole) {
      setError("Please fill in both Target User ID and New Role.");
      return;
    }
    if (Number(targetId) === user.userId) {
      setError("You cannot change your own role.");
      return;
    }
    setAdminPassword("");
    setModalOpen(true);
  }

  async function handleConfirm() {
    setLoading(true);
    setError("");
    setSuccess("");
    try {
      const res = await userApi.updateRole(targetId, {
        adminId: user.userId,
        password: adminPassword,
        newRole,
      });
      setSuccess(res.data?.data?.message || "Role updated successfully.");
      setModalOpen(false);
      setTargetId("");
      setNewRole(null);
    } catch (err) {
      const msg =
        err.response?.data?.message ||
        err.response?.data?.error ||
        "Failed to update role.";
      setError(msg);
      setModalOpen(false);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className={styles.page}>
      <div className={styles.inner}>
        <div className={styles.pageTitle}>Admin — User Management</div>

        {/* ── Find User ── */}
        <div className={styles.card}>
          <div className={styles.sectionTitle}>Find User</div>
          <form className={styles.searchRow} onSubmit={handleSearch}>
            <input
              className={styles.searchInput}
              type="text"
              placeholder="Search by username…"
              value={searchUsername}
              onChange={(e) => setSearchUsername(e.target.value)}
            />
            <button
              type="submit"
              className={styles.searchBtn}
              disabled={searchLoading || !searchUsername.trim()}
            >
              {searchLoading ? "Searching…" : "Search"}
            </button>
          </form>

          {searchError && (
            <p className={styles.errorText}>{searchError}</p>
          )}

          {searchResults !== null && (
            searchResults.length === 0 ? (
              <p className={styles.emptyText}>No users found.</p>
            ) : (
              <table className={styles.table}>
                <thead>
                  <tr>
                    <th>User ID</th>
                    <th>Username</th>
                    <th>Email</th>
                    <th>Role</th>
                  </tr>
                </thead>
                <tbody>
                  {searchResults.map((u) => (
                    <tr key={u.userId}>
                      <td>{u.userId}</td>
                      <td>{u.username}</td>
                      <td>{u.email}</td>
                      <td>{u.role}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )
          )}
        </div>

        <Space h="xl" />

        {/* ── Promote User ── */}
        <div className={styles.card}>
          <div className={styles.sectionTitle}>Promote User</div>
          <TextInput
            label="Target User ID"
            placeholder="Enter user ID"
            value={targetId}
            onChange={(e) => setTargetId(e.currentTarget.value)}
            type="number"
          />
          <Space h="md" />
          <Select
            label="New Role"
            placeholder="Select a role"
            data={ROLE_OPTIONS}
            value={newRole}
            onChange={setNewRole}
          />
          <Space h="lg" />
          {error && (
            <Text c="red" size="sm" mb="sm">
              {error}
            </Text>
          )}
          {success && (
            <Text c="green" size="sm" mb="sm">
              {success}
            </Text>
          )}
          <Button onClick={handlePromoteClick}>Promote User</Button>
        </div>
      </div>

      <Modal
        opened={modalOpen}
        onClose={() => setModalOpen(false)}
        title="Confirm Role Change"
      >
        <Text size="sm">
          You are about to change user <strong>{targetId}</strong> to role{" "}
          <strong>{ROLE_OPTIONS.find((o) => o.value === newRole)?.label}</strong>.
        </Text>
        <Space h="md" />
        <TextInput
          label="Your Password"
          placeholder="Enter your admin password"
          type="password"
          value={adminPassword}
          onChange={(e) => setAdminPassword(e.currentTarget.value)}
        />
        <Space h="md" />
        <Button onClick={handleConfirm} loading={loading} mr="sm">
          Confirm
        </Button>
        <Button variant="default" onClick={() => setModalOpen(false)}>
          Cancel
        </Button>
      </Modal>
    </div>
  );
}
