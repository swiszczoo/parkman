package cz.swisz.parkman.backend;

import java.io.IOException;

public class FetchException extends IOException {
    public FetchException(Throwable cause) {
        super(cause);
    }
}
