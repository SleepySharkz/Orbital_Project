package com.mindmesh.backend.exception;

public class EmailNotFoundException extends RuntimeException {
  public EmailNotFoundException() {
    super("Wrong Email!");
  }
}
