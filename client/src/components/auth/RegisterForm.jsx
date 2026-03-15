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

export default function RegisterForm({ setIsLoginMode }) {
  const navigate = useNavigate();
  const { register } = useAuth();

  const [name, setName] = useState("");
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [err, setErr] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleRegister(e) {
    e.preventDefault();
    setErr("");

    if (!name || !username || !email || !password) {
      setErr("Please fill in name, username, email, and password.");
      return;
    }

    setLoading(true);
    try {
      await register(name, username, email, password);
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
            placeholder="your name"
            label="Name"
            size="md"
            value={name}
            onChange={(event) => setName(event.currentTarget.value)}
          />

          <Space h="md" />

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