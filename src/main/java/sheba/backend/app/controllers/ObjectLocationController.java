package sheba.backend.app.controllers;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sheba.backend.app.BL.LocationBL;
import sheba.backend.app.BL.ObjectLocationBL;
import sheba.backend.app.entities.ObjectLocation;
import sheba.backend.app.exceptions.*;
import sheba.backend.app.util.Endpoints;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(Endpoints.OBJECTS_ENDPOINT)
@RequiredArgsConstructor
public class ObjectLocationController {

    private final ObjectLocationBL locationObjectBL;
    private final LocationBL locationBL;

    @PostMapping(value = "/create/{locationId}", consumes = { "multipart/form-data" })
    public ResponseEntity<?> addLocationObject(
            @PathVariable Long locationId,
            @RequestPart("locationObject") ObjectLocation locationObject,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        try {
            ObjectLocation createdLocationObject = locationObjectBL.createLocationObject(locationId, locationObject, images);
            return ResponseEntity.ok(createdLocationObject);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (MediaUploadFailed e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving object images: " + e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing request: " + e.getMessage());
        }
        catch (ObjectNameMustBeUnique e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving object: " + e.getMessage());
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }

    }

    @GetMapping(value = "/getAll/{locationId}")
    public ResponseEntity<?> getAllObjects (@PathVariable Long locationId){
        try{
            return ResponseEntity.ok(locationBL.getObjectsOfLocation(locationId));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<?> deleteObject(@PathVariable long id){
        try {
            locationObjectBL.deleteObject(id);
            return ResponseEntity.status(HttpStatus.OK).body("Object Deleted Successfully");
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping(value="get-objects-for-model")
    public ResponseEntity<List<ObjectLocation>> getAllObjects(){
        return null;
    }

    @PutMapping("/update/{objectId}")
    public ResponseEntity<?> updateObjectLocation(@PathVariable Long objectId,
                                        @RequestPart(value = "object", required = false) ObjectLocation objectLocation,
                                        @RequestPart(value = "images", required = false) List<MultipartFile> media,
                                        @RequestPart(value = "deletedImageIds", required = false) List<Long> toBeDeletedMediaIds) {
        try {
            ObjectLocation updatedObject = locationObjectBL.updateObject(objectId, objectLocation, media, toBeDeletedMediaIds);
            return ResponseEntity.ok(updatedObject);
        }  catch (MediaUploadFailed e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error with media: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error Updating Object: "+ e.getMessage());
        }
    }
}
