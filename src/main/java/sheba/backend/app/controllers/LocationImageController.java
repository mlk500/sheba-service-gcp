package sheba.backend.app.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sheba.backend.app.BL.LocationBL;
import sheba.backend.app.BL.LocationImageBL;
import sheba.backend.app.entities.Location;
import sheba.backend.app.entities.LocationImage;
import sheba.backend.app.exceptions.LocationMissingInLocationImage;
import sheba.backend.app.exceptions.MediaUploadFailed;
import sheba.backend.app.util.Endpoints;

import java.io.IOException;

@RestController
@RequestMapping(Endpoints.LOCATION_IMAGE_ENDPOINT)
public class LocationImageController {
    private final LocationImageBL locationImageBL;
    private final LocationBL locationBL;

    public LocationImageController(LocationImageBL locationImageBL, LocationBL locationBL) {
        this.locationImageBL = locationImageBL;
        this.locationBL = locationBL;
    }

    @PostMapping("uploadImage/{locationID}")
    public ResponseEntity<?> uploadImage(@PathVariable long locationID, @RequestParam("image") MultipartFile file) {
        try {
            Location location = locationBL.getLocationByID(locationID)
                    .orElseThrow(() -> new LocationMissingInLocationImage("Location not found"));
            LocationImage uploadedImage = locationImageBL.uploadImageToGCS(file, location);
            return ResponseEntity.status(HttpStatus.OK).body(uploadedImage);
        } catch (LocationMissingInLocationImage e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Location Image must belong to an Existing Location");
        } catch (MediaUploadFailed | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload image: " + e.getMessage());
        }
    }

//    @GetMapping("getImage/{fileName}")
//    public ResponseEntity<?> downloadImage(@PathVariable String fileName) {
//        try {
//            byte[] imageData = locationImageBL.downloadImageFromGCS(fileName);
//            if (imageData != null) {
//                return ResponseEntity.status(HttpStatus.OK)
//                        .contentType(MediaType.valueOf("image/jpeg"))
//                        .body(imageData);
//            }
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
//        } catch (MediaUploadFailed e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Failed to download image: " + e.getMessage());
//        }
//    }
}