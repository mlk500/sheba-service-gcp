package sheba.backend.app.DTO;

import lombok.Data;

@Data
public class UnitDTO {

    private long unitID;
    private String name;
    private String hint;
    private int unitOrder;
    private TaskDTO taskDTO;
    private LocationDTO locationDTO;
    private ObjectLocationDTO objectDTO;
}
