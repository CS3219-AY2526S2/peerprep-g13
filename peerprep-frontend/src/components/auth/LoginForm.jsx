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
import { useAuth } from "../auth/AuthContext";

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
      alert("Please enter both email and password.");
      return;
    }

    try {
      await login(email, password);
      navigate("/questions");
    } catch (e2) {
      setErr(e2?.response?.data?.error?.message || "Login failed");
    }
  };

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
          <Text align="right" size="sm">
            <Anchor
              onClick={() => navigate("/forgot-password")}
              style={{ cursor: "not-allowed", color: "gray", fontWeight: 500}}
            >
              Forgot password?
            </Anchor>
          </Text>

          {/* Show error */}
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
            <Button
              fullWidth
              variant="filled"
              size="md"
              onClick={handleLogin}
              className="button"
            >
              Log In
            </Button>
          </div>
        </form>
        <Space h="lg" />
        <Text align="center">Don't have an account yet? </Text>
        <Text align="center">
          <Anchor
            onClick={() => {
              setIsLoginMode(false);
              navigate("/register");
            }}
            className="text"
            component="button"
            // style={{ cursor: "not-allowed", color: "gray" }}
            disabled
          >
            Create an Account
          </Anchor>
        </Text>
      </div>
    </div>
  );
}
