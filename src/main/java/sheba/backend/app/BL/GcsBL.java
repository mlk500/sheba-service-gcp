package sheba.backend.app.BL;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Service
public class GcsBL {

    private final Storage storage;
    private final String bucketName = "project-files-storage";
    private static final Logger logger = LoggerFactory.getLogger(GcsBL.class);


    @Autowired
    public GcsBL(Storage storage) {
        this.storage = storage;
    }

    public String bucketUpload(MultipartFile file, String folderName) throws IOException {
        System.out.println("GcsBL: Attempting to upload file: " + file.getOriginalFilename() + " to folder: " + folderName);
        String objectName = folderName + "/" + file.getOriginalFilename();
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();
        try (InputStream inputStream = file.getInputStream()) {
            Blob blob = storage.create(blobInfo, inputStream);
            if (blob != null) {
//                System.out.println("GcsBL: File uploaded successfully to GCS. Object name: " + objectName);
                return objectName;
            } else {
                System.out.println("GcsBL: File upload failed. Blob is null.");
                throw new IOException("File upload failed. Blob is null.");
            }
        } catch (Exception e) {
            System.out.println("GcsBL: Failed to upload file to GCS: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to upload file to GCS", e);
        }
    }


    public String bucketUploadBytes(byte[] data, String folderName, String fileName, String contentType) throws IOException {
        String objectName = folderName + "/" + fileName;
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(contentType)
                .build();
        storage.create(blobInfo, data);
        return objectName;
    }

    public byte[] bucketRead(String objectName) {
        Blob blob = storage.get(bucketName, objectName);
        return (blob != null) ? blob.getContent() : null;
    }

    public boolean bucketDelete(String objectName) {
        return storage.delete(bucketName, objectName);
    }

    public boolean deleteFolder(String folderName) {
        if (!folderName.endsWith("/")) {
            folderName += "/";
        }
        return storage.delete(bucketName, folderName);
    }

    public boolean bucketObjectExists(String objectName) {
        Blob blob = storage.get(bucketName, objectName);
        return blob != null && blob.exists();
    }

    public String getBucketName() {
        return this.bucketName;
    }

    public String getPublicUrl(String objectName) {
        return String.format("https://storage.googleapis.com/%s/%s", bucketName, objectName);
    }
}