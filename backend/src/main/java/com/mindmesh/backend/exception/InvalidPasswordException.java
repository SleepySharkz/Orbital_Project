package com.mindmesh.backend.exception;

public class InvalidPasswordException extends RuntimeException {
  public InvalidPasswordException() {
    super("Invalid Password!");
  }
}
