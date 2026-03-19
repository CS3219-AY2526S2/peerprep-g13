import { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  TextInput,
  PasswordInput,
  Space,
  Text,
  Anchor,
  Button,
} from "@mantine/core";
import { useAuth } from "../../context/ContextProvider";

const validatePassword = (password) => {
  if (password.length < 8) {
    return "Password must be at least 8 characters.";
  }
  if (!/[A-Z]/.test(password)) {
    return "Password must contain at least one uppercase letter.";
  }
  if (!/[a-z]/.test(password)) {
    return "Password must contain at least one lowercase letter.";
  }
  if (!/[0-9]/.test(password)) {
    return "Password must contain at least one digit.";
  }
  if (!/[!@#$%^&*()]/.test(password)) {
    return "Password must contain at least one special character: ! @ # $ % ^ & * ( )";
  }
  return null;
};

export default function RegisterForm({ setIsLoginMode }) {
  const navigate = useNavigate();
  const { register } = useAuth();

  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [err, setErr] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleRegister(e) {
    e.preventDefault();
    setErr("");

    if (!username || !email || !password) {
      setErr("Please fill in username, email, and password.");
      return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      setErr("Please enter a valid email address.");
      return;
    }

    const passwordError = validatePassword(password);
    if (passwordError) {
      setErr(passwordError);
      return;
    }

    setLoading(true);
    try {
      await register(username, email, password);
      navigate("/dashboard");
    } catch (e2) {
      const msg =
        e2?.response?.data?.error?.message ||
        e2?.response?.data?.message ||
        "Registration failed";
      setErr(msg);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="register-form">
      <div className="form-container">
        <form onSubmit={handleRegister}>
          <TextInput
            placeholder="username"
            label="Username"
            size="md"
            value={username}
            onChange={(event) => setUsername(event.currentTarget.value)}
          />

          <Space h="md" />

          <TextInput
            placeholder="example@gmail.com"
            label="Email Address"
            size="md"
            value={email}
            onChange={(event) => setEmail(event.currentTarget.value)}
          />

          <Space h="md" />

          <PasswordInput
            placeholder="Password"
            label="Password"
            size="md"
            value={password}
            onChange={(event) => setPassword(event.currentTarget.value)}
          />

          <Space h="xs" />
          <div style={{ display: "flex", flexDirection: "column", gap: 2 }}>
            {[
              { label: "At least 8 characters", met: password.length >= 8 },
              { label: "One uppercase letter (A–Z)", met: /[A-Z]/.test(password) },
              { label: "One lowercase letter (a–z)", met: /[a-z]/.test(password) },
              { label: "One digit (0–9)", met: /[0-9]/.test(password) },
              { label: "One special character: ! @ # $ % ^ & * ( )", met: /[!@#$%^&*()]/.test(password) },
            ].map(({ label, met }) => (
              <Text key={label} size="xs" c={password.length === 0 ? "dimmed" : met ? "green" : "red"}>
                {met ? "✓" : "·"} {label}
              </Text>
            ))}
          </div>

          {err && (
            <>
              <Space h="sm" />
              <Text c="red" size="sm">
                {err}
              </Text>
            </>
          )}

          <Space h="lg" />

          <Button fullWidth type="submit" loading={loading}>
            Create Account
          </Button>
        </form>

        <Space h="lg" />

        <Text ta="center">Already have an account?</Text>
        <Text ta="center">
          <Anchor
            component="button"
            onClick={() => setIsLoginMode(true)}
          >
            Log In
          </Anchor>
        </Text>
      </div>
    </div>
  );
}