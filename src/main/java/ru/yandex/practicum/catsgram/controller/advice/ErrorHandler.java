package ru.yandex.practicum.catsgram.controller.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.DuplicatedDataException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.exception.ParameterNotValidException;
import ru.yandex.practicum.catsgram.model.DTO.ErrorResponse;

@ControllerAdvice
@Slf4j
public class ErrorHandler {

    // Пользователю не нужно всё полотно, достаточно сообщить, что что-то не так. Но ТЗ есть ТЗ.
    // А в лог уже всё полотно
    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handelNotFoundException (final NotFoundException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                e.getMessage()
        );

        return new ResponseEntity<>(errorResponse,HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handelDuplicatedDataException (final DuplicatedDataException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                e.getMessage()
        );

        return new ResponseEntity<>(errorResponse,HttpStatus.CONFLICT);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handelConditionsNotMetException (final ConditionsNotMetException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                e.getMessage()
        );

        return new ResponseEntity<>(errorResponse,HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handelParameterNotValidException (final ParameterNotValidException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                e.getMessage()
        );

        return new ResponseEntity<>(errorResponse,HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handelUnexpectedException (final RuntimeException e) {
        //log.error("Неожиданная ошибка", e);
        ErrorResponse errorResponse = new ErrorResponse(
                e.getMessage()
        );

        return new ResponseEntity<>(errorResponse,HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
