package sheba.backend.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sheba.backend.app.BL.GcsBL;

import java.io.IOException;

@RestController
@RequestMapping("/gcs-bucket")
public class GCSController {

    private final GcsBL gcsBL;

    @Autowired
    public GCSController(GcsBL gcsBL) {
        this.gcsBL = gcsBL;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam("folderPath") String folderPath) {
        try {
            String objectName = gcsBL.bucketUpload(file, folderPath);
            return ResponseEntity.ok("File uploaded successfully to bucket: " + gcsBL.getBucketName() + ", object: " + objectName);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file: " + e.getMessage());
        }
    }

    @GetMapping("/{folderPath}/{fileName}")
    public ResponseEntity<byte[]> getMedia(
            @PathVariable String folderPath,
            @PathVariable String fileName) {
        String objectName = folderPath + "/" + fileName;
        byte[] mediaBytes = gcsBL.bucketRead(objectName);
        if (mediaBytes != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM) // General binary data
                    .body(mediaBytes);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{folderPath}/{fileName}")
    public ResponseEntity<String> deleteMedia(
            @PathVariable String folderPath,
            @PathVariable String fileName) {
        String objectName = folderPath + "/" + fileName;
        boolean deleted = gcsBL.bucketDelete(objectName);
        if (deleted) {
            return ResponseEntity.ok("Media deleted successfully from bucket: " + gcsBL.getBucketName());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Media not found or could not be deleted");
        }
    }
}
