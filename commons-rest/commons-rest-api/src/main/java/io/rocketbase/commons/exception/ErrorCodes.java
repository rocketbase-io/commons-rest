package io.rocketbase.commons.exception;

import lombok.Getter;

public enum ErrorCodes {
    FORM_ERROR(1000);

    @Getter
    private int status;

    ErrorCodes(int status) {
        this.status = status;
    }
}
