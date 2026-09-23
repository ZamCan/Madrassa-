package com.zamcan.madrassa.domain.common;

/**
 * Provider-independent result returned by domain operations.
 *
 * The domain layer must never depend on Android UI classes,
 * Toasts, Activities, Views, or network SDKs.
 */
public final class OperationResult<T> {

    public enum Status {
        SUCCESS,
        VALIDATION_ERROR,
        NOT_FOUND,
        CONFLICT,
        UNAUTHORIZED,
        FORBIDDEN,
        FAILED
    }

    private final Status status;
    private final T data;
    private final String code;
    private final String message;

    private OperationResult(
            Status status,
            T data,
            String code,
            String message
    ) {
        this.status = status;
        this.data = data;
        this.code = code;
        this.message = message;
    }

    public static <T> OperationResult<T> success(T data) {
        return new OperationResult<>(
                Status.SUCCESS,
                data,
                null,
                null
        );
    }

    public static <T> OperationResult<T> validationError(
            String code,
            String message
    ) {
        return new OperationResult<>(
                Status.VALIDATION_ERROR,
                null,
                code,
                message
        );
    }

    public static <T> OperationResult<T> notFound(
            String code,
            String message
    ) {
        return new OperationResult<>(
                Status.NOT_FOUND,
                null,
                code,
                message
        );
    }

    public static <T> OperationResult<T> conflict(
            String code,
            String message
    ) {
        return new OperationResult<>(
                Status.CONFLICT,
                null,
                code,
                message
        );
    }

    public static <T> OperationResult<T> unauthorized(
            String code,
            String message
    ) {
        return new OperationResult<>(
                Status.UNAUTHORIZED,
                null,
                code,
                message
        );
    }

    public static <T> OperationResult<T> forbidden(
            String code,
            String message
    ) {
        return new OperationResult<>(
                Status.FORBIDDEN,
                null,
                code,
                message
        );
    }

    public static <T> OperationResult<T> failed(
            String code,
            String message
    ) {
        return new OperationResult<>(
                Status.FAILED,
                null,
                code,
                message
        );
    }

    public Status getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }
}
