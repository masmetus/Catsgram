package ru.yandex.practicum.catsgram.model.DTO;

public class ErrorResponse {
    private final String error;
    //private final String description;

    public ErrorResponse(String error) {
        this.error = error;
        //this.description = description;
    }

    public String getError() {
        return error;
    }

    /*public String getDescription() {
        return description;
    }*/
}
