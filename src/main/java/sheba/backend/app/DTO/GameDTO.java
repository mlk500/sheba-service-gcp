package sheba.backend.app.DTO;

import lombok.Data;
import sheba.backend.app.entities.GameImage;
import sheba.backend.app.entities.Unit;

import java.util.List;
@Data
public class GameDTO {

    private long gameID;
    private String gameName;
    private String description;
    private String QRCodeURL;
    private List<UnitDTO> units;

}
