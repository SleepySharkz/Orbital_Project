package com.mindmesh.backend.exception;

public class InvalidCredentials extends RuntimeException {
  public InvalidCredentials() {
    super("Incorrect Credentials!");
  }
}
