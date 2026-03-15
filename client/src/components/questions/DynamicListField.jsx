import React from "react";

const fieldStyles = {
  wrapper: { marginBottom: 16 },
  label: {
    display: "block",
    fontSize: "0.8rem",
    fontWeight: 600,
    color: "#374151",
    marginBottom: 6,
    textTransform: "uppercase",
    letterSpacing: "0.04em",
  },
  row: { display: "flex", gap: 8, marginBottom: 6 },
  input: {
    flex: 1,
    padding: "7px 10px",
    border: "1px solid #e5e7eb",
    borderRadius: 6,
    fontSize: "0.875rem",
    color: "#374151",
  },
  removeBtn: {
    padding: "6px 10px",
    border: "1px solid #fca5a5",
    borderRadius: 6,
    background: "transparent",
    color: "#dc2626",
    cursor: "pointer",
    fontSize: "0.8rem",
    fontWeight: 600,
  },
  addBtn: {
    padding: "6px 14px",
    border: "1px solid #e5e7eb",
    borderRadius: 6,
    background: "#f9fafb",
    color: "#374151",
    cursor: "pointer",
    fontSize: "0.8rem",
    fontWeight: 600,
    marginTop: 4,
  },
};

export default function DynamicListField({
  label,
  items,
  onChange,
  placeholder = "",
  inputType = "text",
}) {
  function handleItemChange(index, value) {
    const next = items.map((item, i) => (i === index ? value : item));
    onChange(next);
  }

  function handleRemove(index) {
    onChange(items.filter((_, i) => i !== index));
  }

  function handleAdd() {
    onChange([...items, ""]);
  }

  return (
    <div style={fieldStyles.wrapper}>
      <label style={fieldStyles.label}>{label}</label>
      {items.map((item, index) => (
        <div key={index} style={fieldStyles.row}>
          <input
            type={inputType}
            value={item}
            onChange={(e) => handleItemChange(index, e.target.value)}
            placeholder={placeholder}
            style={fieldStyles.input}
          />
          <button
            type="button"
            onClick={() => handleRemove(index)}
            style={fieldStyles.removeBtn}
          >
            Remove
          </button>
        </div>
      ))}
      <button type="button" onClick={handleAdd} style={fieldStyles.addBtn}>
        + Add
      </button>
    </div>
  );
}
