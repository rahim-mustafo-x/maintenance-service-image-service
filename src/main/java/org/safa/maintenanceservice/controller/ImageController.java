package org.safa.maintenanceservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.safa.maintenanceservice.model.ImageType;
import org.safa.maintenanceservice.model.dto.ApiResponse;
import org.safa.maintenanceservice.model.dto.ImageByteResponse;
import org.safa.maintenanceservice.model.exceptions.NotFoundException;
import org.safa.maintenanceservice.model.dto.ImageResponse;
import org.safa.maintenanceservice.model.exceptions.Unauthorized;
import org.safa.maintenanceservice.service.ImageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/v1/image")
@RequiredArgsConstructor
public class ImageController {
    private final ImageService imageService;

    /**
     * When on the parts of setting a profile image it is optional to set one.
     * <pre>
     *  (------------)
     *  (    /\   () )
     *  (   /  \/\   )
     *  (  /    \ \  )
     *  (____________)
     * </pre>
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImageResponse>> saveImage(
            @RequestPart("image") MultipartFile image,
            @RequestParam long ownerId,
            @RequestParam ImageType imageType,
            HttpServletRequest request) {
        try {
            String accessToken = request.getHeader(HttpHeaders.AUTHORIZATION);
            ImageResponse saved = imageService.save(image, imageType, ownerId, accessToken);

            if (saved == null) {   // service returns null when the file could not be written
                return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Failed to store image file");
            }
            return buildResponse(HttpStatus.CREATED, saved, null);

        } catch (NotFoundException e) {
            return buildResponse(HttpStatus.NOT_FOUND, null, e.getMessage());
        } catch (Unauthorized e) {
            return buildResponse(HttpStatus.UNAUTHORIZED, null, e.getMessage());
        } catch (IOException e) {
            return buildResponse(HttpStatus.EXPECTATION_FAILED, null, e.getMessage());
        } catch (Exception e) {
            return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, e.getMessage());
        }
    }

    private ResponseEntity<ApiResponse<ImageResponse>> buildResponse(
            HttpStatus status, ImageResponse data, String message) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.<ImageResponse>builder()
                        .code(status.value())
                        .data(data)
                        .message(message)
                        .build());
    }
    @GetMapping("/{imageId}")
    public ResponseEntity<byte[]> getImage(@PathVariable UUID imageId) {
        try {
            ImageByteResponse imageAsByteArray = imageService.getImageAsByteArray(imageId);
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.parseMediaType(imageAsByteArray.contentType()))
                    .body(imageAsByteArray.data());
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.MULTIPART_FORM_DATA).body(null);
        }
    }

    @GetMapping("/data/{imageId}")
    public ResponseEntity<ApiResponse<ImageResponse>> getImageData(@PathVariable UUID imageId) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ApiResponse.<ImageResponse>builder()
                            .code(HttpStatus.OK.value())
                            .data(imageService.getImageResponse(imageId))
                            .build());
        }catch (NotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(ApiResponse.<ImageResponse>builder()
                    .code(HttpStatus.NOT_FOUND.value())
                    .message(e.getMessage())
                    .build());
        }
    }
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> updateImage(
            @RequestPart("image") MultipartFile image, @RequestParam UUID imageId, @RequestParam long ownerId, HttpServletRequest request) {
        try {
            String accessToken = request.getHeader(HttpHeaders.AUTHORIZATION);
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ApiResponse.builder()
                            .code(HttpStatus.ACCEPTED.value())
                            .data(imageService.updateImage(image, imageId, ownerId, accessToken))
                            .build());
        }catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.MULTIPART_FORM_DATA).body(ApiResponse.builder()
                    .code(HttpStatus.NOT_FOUND.value())
                    .message(e.getMessage())
                    .build());
        }catch(IOException e){
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).contentType(MediaType.APPLICATION_JSON)
                    .body(ApiResponse.<ImageResponse>builder()
                            .code(HttpStatus.EXPECTATION_FAILED.value())
                            .data(null)
                            .message(e.getMessage())
                            .build());
        }catch(Unauthorized e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).contentType(MediaType.APPLICATION_JSON)
                    .body(ApiResponse.<ImageResponse>builder()
                            .code(HttpStatus.UNAUTHORIZED.value())
                            .data(null)
                            .message(e.getMessage())
                            .build());
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(ApiResponse.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message(e.getMessage())
                    .build());
        }
    }
    @DeleteMapping("/{imageId}")
    public ResponseEntity<ApiResponse<?>> deleteImage(@PathVariable UUID imageId) {
        try {
            if (imageService.deleteImage(imageId)){
                return ResponseEntity.status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(ApiResponse.builder()
                                .code(HttpStatus.OK.value())
                                .data(true)
                                .build());
            }else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(ApiResponse.builder()
                                .code(HttpStatus.NOT_FOUND.value())
                                .data(false)
                                .build());
            }
        }catch (NotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ApiResponse.builder()
                            .code(HttpStatus.NOT_FOUND.value())
                            .message(e.getMessage())
                            .build());
        }
    }
}
