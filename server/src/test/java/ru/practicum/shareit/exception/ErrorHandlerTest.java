package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorHandlerTest {

    private final ErrorHandler errorHandler = new ErrorHandler();

    @Test
    void testErrorHandlerAndErrorResponse() {
        // 1. ConditionsNotMetException (400)
        ConditionsNotMetException ex1 = new ConditionsNotMetException("Неверные условия");
        ErrorResponse resp1 = errorHandler.handleConditionsNotMetException(ex1);
        assertEquals("Неверные условия", resp1.getError()); // Здесь проверяется и геттер ErrorResponse

        // 2. NotFoundException (404)
        NotFoundException ex2 = new NotFoundException("Не найдено");
        ErrorResponse resp2 = errorHandler.handleNotFoundException(ex2);
        assertEquals("Не найдено", resp2.getError());

        // 3. ConflictException (409)
        ConflictException ex3 = new ConflictException("Конфликт");
        ErrorResponse resp3 = errorHandler.handleConflictException(ex3);
        assertEquals("Конфликт", resp3.getError());

        // 4. Throwable (500)
        Throwable ex4 = new Throwable("Системная ошибка");
        ErrorResponse resp4 = errorHandler.handleThrowable(ex4);
        assertEquals("Произошла непредвиденная ошибка.", resp4.getError());
    }
}
