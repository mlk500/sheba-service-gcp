package sheba.backend.app.DTO;

import lombok.Data;
import sheba.backend.app.entities.Location;

import java.util.List;
@Data
public class ObjectImageModelDTO {
    private long objectID;
    private String name;
    private long locationID;
    private List<String> objectImgsUrls;
}
