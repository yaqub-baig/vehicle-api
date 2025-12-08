package com.autoadda.apis.vehicle.exception;

import java.io.IOException;

public class FileStorageException extends RuntimeException {
    public FileStorageException(String couldNotInitializeStorageDirectories, IOException e) {
        super(couldNotInitializeStorageDirectories, e);
    }

    public FileStorageException(String message) {
        super(message);
    }
}
