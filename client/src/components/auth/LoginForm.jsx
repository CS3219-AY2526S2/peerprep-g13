import { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Anchor,
  Button,
  PasswordInput,
  Space,
  Text,
  TextInput,
} from "@mantine/core";
import { useAuth } from "../../context/ContextProvider";

export default function LoginForm({ setIsLoginMode }) {
  const { login } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();
  const [err, setErr] = useState("");

  async function handleLogin(e) {
    e.preventDefault();
    setErr("");

    if (!email || !password) {
      setErr("Please enter both email and password.");
      return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      setErr("Please enter a valid email address.");
      return;
    }

    if (password.length < 8) {
      setErr("Password must be at least 8 characters.");
      return;
    }

    try {
      await login(email, password);
      navigate("/dashboard");
    } catch (e2) {
      setErr(
        e2?.response?.data?.error?.message ||
          e2?.response?.data?.message ||
          "Login failed"
      );
    }
  }

  return (
    <div className="login-form">
      <div className="form-container">
        <form onSubmit={handleLogin}>
          <TextInput
            placeholder="example@gmail.com"
            label="Email Address"
            size="md"
            value={email}
            onChange={(event) => setEmail(event.currentTarget.value)}
          />

          <Space h="lg" />

          <PasswordInput
            placeholder="Password"
            label="Password"
            size="md"
            value={password}
            onChange={(event) => setPassword(event.currentTarget.value)}
          />

          <Space h="xs" />

          <Text ta="right" size="sm">
            <Anchor
              component="button"
              type="button"
              onClick={() => {}}
              style={{ cursor: "not-allowed", color: "gray", fontWeight: 500 }}
            >
              Forgot password?
            </Anchor>
          </Text>

          {err && (
            <>
              <Space h="sm" />
              <Text c="red" size="sm">
                {err}
              </Text>
            </>
          )}

          <Space h="xl" />

          <div className="button-container">
            <Button fullWidth variant="filled" size="md" type="submit" className="button">
              Log In
            </Button>
          </div>
        </form>

        <Space h="lg" />

        <Text ta="center">Don't have an account yet?</Text>
        <Text ta="center">
          <Anchor
            component="button"
            onClick={() => setIsLoginMode(false)}
            className="text"
          >
            Create an Account
          </Anchor>
        </Text>
      </div>
    </div>
  );
}