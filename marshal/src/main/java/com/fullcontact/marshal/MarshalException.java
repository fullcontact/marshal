package com.fullcontact.marshal;

import java.io.IOException;

/**
 * An exception for use by a Marshal. Other classes can also use this exception to indicate
 * marshaling or demarshaling errors when using Marshals.
 *
 * @author Brandon Vargo
 */
public class MarshalException extends IOException {
    public MarshalException(String message) {
        super(message);
    }

    public MarshalException(String message, Throwable cause) {
        super(message, cause);
    }
}
