package ru.yandex.practicum.catsgram.model;

import lombok.*;


@Data
@EqualsAndHashCode(of = "id")
public class Image {
    Long id;
    long postId;
    String originalFileName;
    String filePath;
}
