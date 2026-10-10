package com.gramcare.web.client;

/** Thrown by the *Client classes when a backend microservice cannot be reached or returns an error. */
public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String serviceName, Throwable cause) {
        super("Could not reach " + serviceName + ": " + cause.getMessage(), cause);
    }
    public ServiceUnavailableException(String message) {
        super(message);
    }
}
