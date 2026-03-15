import React, { useState } from "react";
import { Card, Center, Stack, Title } from "@mantine/core";
import LoginForm from "../../components/auth/LoginForm";
import RegisterForm from "../../components/auth/RegisterForm";

const AuthPage = ({ initLoginMode = true }) => {
  const [isLoginMode, setIsLoginMode] = useState(initLoginMode);

  return (
    <div className="auth-page">
      <Center style={{ minHeight: "100vh" }}>
        <Card
          withBorder
          p="xl"
          style={{ width: "90%", maxWidth: 600 }}
          shadow="xl"
        >
          <Stack p="xl">
            <Title ta="center">
              Welcome to <span className="peerprep">PeerPrep</span>
            </Title>

            {isLoginMode ? (
              <LoginForm setIsLoginMode={setIsLoginMode} />
            ) : (
              <RegisterForm setIsLoginMode={setIsLoginMode} />
            )}
          </Stack>
        </Card>
      </Center>
    </div>
  );
};

export default AuthPage;
