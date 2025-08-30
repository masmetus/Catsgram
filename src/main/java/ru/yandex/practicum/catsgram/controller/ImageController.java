package ru.yandex.practicum.catsgram.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.catsgram.model.Image;
import ru.yandex.practicum.catsgram.model.ImageData;
import ru.yandex.practicum.catsgram.service.ImageService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ImageController {
    ImageService imageService;


    @GetMapping("/posts/{postId}/images")
    public List<Image> getPostImage(@PathVariable("postId") long postId) {
        return imageService.getPostImage(postId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/posts/{postId}/images")
    public List<Image> addPostImage(
            @PathVariable("postId") long postId,
            @RequestParam("image") List<MultipartFile> files) {
        return imageService.saveImages(postId, files);
    }

    @GetMapping(value = "/images/{imageId}")
    public ResponseEntity<byte[]> downLoadImage(@PathVariable("imageId") long imageId) {
        ImageData imageData = imageService.getImageData(imageId);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentDisposition(ContentDisposition.attachment()
                .filename(imageData.getName())
                .build()
        );


        return new ResponseEntity<>(imageData.getData(), httpHeaders, HttpStatus.OK);
    }
}
