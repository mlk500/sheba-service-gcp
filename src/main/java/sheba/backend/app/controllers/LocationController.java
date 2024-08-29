package sheba.backend.app.controllers;

import com.google.zxing.WriterException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sheba.backend.app.BL.LocationBL;
import sheba.backend.app.BL.LocationImageBL;
import sheba.backend.app.entities.Location;
import sheba.backend.app.exceptions.LocationIsPartOfUnit;
import sheba.backend.app.exceptions.MediaUploadFailed;
import sheba.backend.app.util.Endpoints;

import java.io.IOException;

@RestController
@RequestMapping(Endpoints.LOCATION_ENDPOINT)
@RequiredArgsConstructor
public class LocationController {
    private final LocationBL locationBL;
    private final LocationImageBL locationImageBL;


    @PostMapping(value = "create", consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> addLocation(@RequestPart("location") Location location, @RequestPart(value = "image", required = false) MultipartFile image) {
        try {
            Location createdLocation;
            if (image != null && !image.isEmpty()) {
                createdLocation = locationBL.createLocationWithImage(location, image);
            } else {
                createdLocation = locationBL.createLocation(location);
            }
            System.out.println("Controller: Location created successfully. ID: " + createdLocation.getLocationID());
            return ResponseEntity.ok(createdLocation);
        } catch (Exception e) {
            System.out.println("Controller: Error creating location: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating location: " + e.getMessage());
        }
    }

    @GetMapping("getAll")
    public ResponseEntity<?> getAll(){
        return ResponseEntity.ok(locationBL.getAll());
    }

    @PutMapping("update/{locationId}")
    public ResponseEntity<?> updateLocation(@PathVariable Long locationId, @RequestPart(value="location", required = true) Location location, @RequestPart(value = "image", required = false) MultipartFile image, @RequestParam(value = "deleteImage", defaultValue = "false") boolean deleteImage) {
        try {
            Location updatedLocation = locationBL.updateLocation(locationId, location, image, deleteImage);
            return new ResponseEntity<>(updatedLocation, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>("Location not found with ID: " + location.getLocationID(), HttpStatus.NOT_FOUND);
        } catch (IOException | WriterException e) {
            return new ResponseEntity<>("Error generating QR code: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> deleteLocation(@PathVariable long id) {
        try {
            locationBL.deleteLocation(id);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            System.out.println("in msg1 "+ e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (LocationIsPartOfUnit e) {
            System.out.println("in msg2 "+ e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            System.out.println("in msg3 "+ e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
