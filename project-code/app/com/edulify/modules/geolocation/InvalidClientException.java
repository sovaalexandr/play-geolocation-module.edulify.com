package com.edulify.modules.geolocation;

/**
 * Created by sovaalexandr
 */
public class InvalidClientException extends Exception {
  private static final String MESSAGE_TEMPLATE = "There is no default %s client";

  public InvalidClientException(String message) {
    super(String.format(MESSAGE_TEMPLATE, message));
  }
}
