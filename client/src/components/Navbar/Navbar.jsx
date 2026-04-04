import React from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/ContextProvider";
import styles from "./Navbar.module.css";

export default function Navbar() {
  const { user, isAdmin, logout } = useAuth();
  const navigate = useNavigate();

  async function handleLogout() {
    await logout();
    navigate("/auth");
  }

  if (!user) return null;

  return (
    <nav className={styles.navbar}>
      <div className={styles.inner}>
        <div className={styles.links}>
          <NavLink
            to="/dashboard"
            className={({ isActive }) =>
              isActive ? `${styles.link} ${styles.active}` : styles.link
            }
          >
            Home
          </NavLink>
          <NavLink
            to="/questions"
            className={({ isActive }) =>
              isActive ? `${styles.link} ${styles.active}` : styles.link
            }
          >
            Questions
          </NavLink>
          <NavLink
            to="/matching"
            className={({ isActive }) =>
              isActive ? `${styles.link} ${styles.active}` : styles.link
            }
          >
            Match
          </NavLink>
          {isAdmin && (
            <NavLink
              to="/admin/users"
              className={({ isActive }) =>
                isActive ? `${styles.link} ${styles.active}` : styles.link
              }
            >
              Manage Users
            </NavLink>
          )}
        </div>
        <button className={styles.logoutBtn} onClick={handleLogout}>
          Logout
        </button>
      </div>
    </nav>
  );
}
