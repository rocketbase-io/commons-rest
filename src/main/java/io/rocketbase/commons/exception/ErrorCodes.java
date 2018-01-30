package io.rocketbase.commons.exception;

import lombok.Getter;

public enum ErrorCodes {
    FORM_ERROR(1000);

    @Getter
    private int errorCode;

    ErrorCodes(int errorCode) {
        this.errorCode = errorCode;
    }
}
