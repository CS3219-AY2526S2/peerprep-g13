import React, { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { questionApi } from "../../api/question";
import { useAuth } from "../../context/ContextProvider";
import DifficultyBadge from "../../components/questions/DifficultyBadge";
import FilterBar from "../../components/questions/FilterBar";
import styles from "./QuestionsPage.module.css";

const PAGE_SIZE = 10;

export default function QuestionsPage() {
  const { isQuestionManager } = useAuth();
  const [questions, setQuestions] = useState([]);
  const [filters, setFilters] = useState({ topic: "", difficulty: "" });
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const fetchQuestions = useCallback(async (activeFilters) => {
    setLoading(true);
    setError("");
    try {
      let res;
      const hasFilter = activeFilters.topic || activeFilters.difficulty;
      if (hasFilter) {
        const body = {};
        if (activeFilters.topic) body.topic = activeFilters.topic;
        if (activeFilters.difficulty) body.difficulty = activeFilters.difficulty;
        res = await questionApi.filter(body);
      } else {
        res = await questionApi.list();
      }
      setQuestions(res.data.questions || []);
      setPage(1);
    } catch (err) {
      setError(err?.response?.data?.message || "Failed to load questions.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchQuestions(filters);
  }, []);

  function handleFilterChange(newFilters) {
    setFilters(newFilters);
    setPage(1);
    fetchQuestions(newFilters);
  }

  function handleSearchChange(e) {
    setSearch(e.target.value);
    setPage(1);
  }

  const filtered = questions.filter((q) =>
    q.title.toLowerCase().includes(search.toLowerCase())
  );
  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const paginated = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  return (
    <div className={styles.page}>
      <div className={styles.inner}>
        <div className={styles.header}>
          <h1 className={styles.pageTitle}>Questions</h1>
          {isQuestionManager && (
            <Link to="/questions/create" className={styles.newBtn}>
              + New Question
            </Link>
          )}
        </div>

        <div className={styles.card}>
          <FilterBar filters={filters} onFilterChange={handleFilterChange} />

          <div className={styles.searchRow}>
            <input
              className={styles.searchInput}
              type="text"
              placeholder="Search by title…"
              value={search}
              onChange={handleSearchChange}
            />
          </div>

          {loading ? (
            <div className={styles.loadingState}>Loading…</div>
          ) : error ? (
            <div className={styles.errorState}>{error}</div>
          ) : filtered.length === 0 ? (
            <div className={styles.emptyState}>No questions found.</div>
          ) : (
            <>
              <table className={styles.table}>
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Title</th>
                    <th>Topic</th>
                    <th>Difficulty</th>
                  </tr>
                </thead>
                <tbody>
                  {paginated.map((q, index) => (
                    <tr key={q.id}>
                      <td>{(page - 1) * PAGE_SIZE + index + 1}</td>
                      <td>
                        <Link to={`/questions/${q.id}`} className={styles.qLink}>
                          {q.title}
                        </Link>
                      </td>
                      <td>
                        {(q.topics ?? (q.topic ? [q.topic] : [])).map((t) => (
                          <span key={t} className={styles.topicChip}>{t}</span>
                        ))}
                      </td>
                      <td>
                        <DifficultyBadge difficulty={q.difficulty} />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>

              <div className={styles.pagination}>
                <button
                  className={styles.pageBtn}
                  onClick={() => setPage((p) => p - 1)}
                  disabled={page === 1}
                >
                  ‹
                </button>
                {Array.from({ length: totalPages }, (_, i) => i + 1).map((p) => (
                  <button
                    key={p}
                    className={`${styles.pageBtn} ${p === page ? styles.pageBtnActive : ""}`}
                    onClick={() => setPage(p)}
                  >
                    {p}
                  </button>
                ))}
                <button
                  className={styles.pageBtn}
                  onClick={() => setPage((p) => p + 1)}
                  disabled={page === totalPages}
                >
                  ›
                </button>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
