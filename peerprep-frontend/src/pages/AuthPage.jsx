import React, { useState } from "react";
import { Card, Center, Stack, Title } from "@mantine/core";

const AuthPage = ({ initLoginMode }) => { 
    const [isLoginMode, setIsLoginMode] = useState(initLoginMode);

  return (
    <div className="auth-page">
      <Center style={{ height: "100vh" }}>
        <Card
          withBorder
          p="xl"
          style={{ width: "90%", maxWidth: 600 }}
          shadow="xl"
        >
          <Stack p="xl">
            <img
              src={" "}
              alt="PeerPrep logo"
              className="navbar__logo"
            />
            <Title align="center">
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
}
export default AuthPage;