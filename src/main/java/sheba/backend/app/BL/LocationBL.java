package sheba.backend.app.BL;

import com.google.zxing.WriterException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sheba.backend.app.entities.Location;
import sheba.backend.app.entities.LocationImage;
import sheba.backend.app.entities.ObjectLocation;
import sheba.backend.app.exceptions.LocationIsPartOfUnit;
import sheba.backend.app.exceptions.MediaUploadFailed;
import sheba.backend.app.repositories.LocationImageRepository;
import sheba.backend.app.repositories.LocationRepository;
import sheba.backend.app.repositories.UnitRepository;
import sheba.backend.app.util.QRCodeGenerator;
import sheba.backend.app.util.StoragePath;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class LocationBL {

    private final LocationRepository locationRepository;
    private final LocationImageRepository locationImageRepository;
    private final LocationImageBL locationImageBL;
    private final GcsBL gcsBL;
    private final UnitRepository unitRepository;
    private final ObjectLocationBL objectLocationBL;

    public LocationBL(LocationRepository locationRepository, LocationImageRepository locationImageRepository, LocationImageBL locationImageBL, GcsBL gcsBL, UnitRepository unitRepository, ObjectLocationBL objectLocationBL) {
        this.locationRepository = locationRepository;
        this.locationImageRepository = locationImageRepository;
        this.locationImageBL = locationImageBL;
        this.gcsBL = gcsBL;
        this.unitRepository = unitRepository;
        this.objectLocationBL = objectLocationBL;
    }

    @Transactional
    public Location createLocationWithImage(Location location, MultipartFile imageFile) throws IOException, WriterException {
        System.out.println("LocationBL: Creating location with image. Location name: " + location.getName());
        String[] qrCodeInfo = generateLocationQRCode(location);
        location.setQRCode(qrCodeInfo[0]);
        location.setQRCodePublicUrl(qrCodeInfo[1]);
        Location locationSaved = locationRepository.save(location);
        System.out.println("LocationBL: Location saved. ID: " + locationSaved.getLocationID());

        if (imageFile != null && !imageFile.isEmpty()) {
            System.out.println("LocationBL: Image file present. Filename: " + imageFile.getOriginalFilename() + ", Size: " + imageFile.getSize() + " bytes");
            try {
                LocationImage locationImage = locationImageBL.uploadImageToGCS(imageFile, locationSaved);
                System.out.println("LocationBL: Image uploaded to GCS. GCS Object Name: " + locationImage.getGcsObjectName());
                locationSaved.setLocationImage(locationImage);
                locationSaved.setLocationImagePublicUrl(locationImage.getImageURL());
                locationSaved = locationRepository.save(locationSaved);
                System.out.println("LocationBL: Location updated with image information. Image ID: " + locationImage.getLocationImgID());
            } catch (Exception e) {
                System.out.println("LocationBL: Error uploading image for location: " + e.getMessage());
                e.printStackTrace();
                throw new MediaUploadFailed("Failed to upload image for location", e);
            }
        } else {
            System.out.println("LocationBL: No image file provided for location");
        }
        return locationSaved;
    }

    @Transactional
    public Location createLocation(Location location) throws IOException, WriterException {
        try {
            String[] qrCodeInfo = generateLocationQRCode(location);
            location.setQRCode(qrCodeInfo[0]);
            location.setQRCodePublicUrl(qrCodeInfo[1]);
            return locationRepository.save(location);
        } catch (IOException | WriterException e) {
            throw new MediaUploadFailed("Failed to create location", e);
        }
    }

    @Transactional
    public Location updateLocation(Location location) throws IOException, WriterException {
        Location currLocation = locationRepository.findByLocationID(location.getLocationID());
        if (currLocation != null) {
            try {
                if (currLocation.getQRCode() != null) {
                    gcsBL.bucketDelete(currLocation.getQRCode());
                }

                currLocation.setName(location.getName());
                currLocation.setDescription(location.getDescription());
                currLocation.setFloor(location.getFloor());
                currLocation.getObjectsList().clear();
                currLocation.getObjectsList().addAll(location.getObjectsList());

                String[] qrCodeInfo = generateLocationQRCode(currLocation);
                currLocation.setQRCode(qrCodeInfo[0]);
                currLocation.setQRCodePublicUrl(qrCodeInfo[1]);

                return locationRepository.save(currLocation);
            } catch (IOException | WriterException e) {
                throw new MediaUploadFailed("Failed to update location", e);
            }
        } else {
            throw new EntityNotFoundException("Location not found with ID: " + location.getLocationID());
        }
    }

    @Transactional
    public void deleteLocation(long id) throws LocationIsPartOfUnit, MediaUploadFailed {
        Location location = locationRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Location not found with ID: " + id));
        if(isPartOfAGame(location)){
            throw new LocationIsPartOfUnit("Location is part of an existing game");
        }

        try {
            if(location.getObjectsList()!= null && !location.getObjectsList().isEmpty()){
                for(ObjectLocation obj : location.getObjectsList()){
                    objectLocationBL.deleteObject(obj);
                }
            }
            if (location.getLocationImage() != null) {
                gcsBL.bucketDelete(location.getLocationImage().getGcsObjectName());
            }

            if (location.getQRCode() != null) {
                gcsBL.bucketDelete(location.getQRCode());
            }

            locationRepository.delete(location);
        } catch (MediaUploadFailed e) {
            throw new MediaUploadFailed("Failed to delete location and associated media", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String[] generateLocationQRCode(Location location) throws IOException, WriterException {
        String qrName = "location_" + location.getName() + "_floor" + location.getFloor();
        String qrContent = "ID " + location.getLocationID() + "\n" + "Location Name " + location.getName() + "\n"
                + "Floor " + location.getFloor() + "\n" + "Description " + location.getDescription();

        ByteArrayOutputStream qrStream = new ByteArrayOutputStream();
        QRCodeGenerator.generateQRCode(qrName, qrContent, qrStream, StoragePath.LOC_QR_IMG);
        byte[] qrCodeBytes = qrStream.toByteArray();

        String fileName = qrName + ".png";
        String objectName = gcsBL.bucketUploadBytes(qrCodeBytes, StoragePath.QR_LOCATION, fileName, "image/png");
        String publicUrl = gcsBL.getPublicUrl(objectName);

        return new String[]{objectName, publicUrl};
    }

    public List<Location> getAll() {
        List<Location> locations = locationRepository.findAll();
        for (Location location : locations) {
            if (location.getQRCode() != null) {
                location.setQRCodePublicUrl(gcsBL.getPublicUrl(location.getQRCode()));
            }
            if (location.getLocationImage() != null) {
                location.setLocationImagePublicUrl(location.getLocationImage().getImageURL());
            }
        }
        return locations;
    }

    public Optional<Location> getLocationByID(long id) {
        Optional<Location> locationOpt = locationRepository.findById(id);
        locationOpt.ifPresent(location -> {
            if (location.getQRCode() != null) {
                location.setQRCodePublicUrl(gcsBL.getPublicUrl(location.getQRCode()));
            }
            if (location.getLocationImage() != null) {
                location.setLocationImagePublicUrl(location.getLocationImage().getImageURL());
            }
        });
        return locationOpt;
    }

    public List<ObjectLocation> getObjectsOfLocation(long id) {
        Optional<Location> currLocation = getLocationByID(id);
        return currLocation.orElseThrow(() -> new RuntimeException("Location not found")).getObjectsList();
    }

    private boolean isPartOfAGame(Location checkLocation){
        return unitRepository.findByLocation(checkLocation) != null && !unitRepository.findByLocation(checkLocation).isEmpty();
    }
}