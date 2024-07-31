package sheba.backend.app.DTO;

import lombok.Data;

@Data
public class LocationDTO {
    private long locationID;
    private String name;
    private int floor;
    private String QRCodePublicUrl;
    private String locationImagePublicUrl;

}
