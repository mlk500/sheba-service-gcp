package sheba.backend.app.mappers;

import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sheba.backend.app.DTO.UnitDTO;
import sheba.backend.app.entities.Unit;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-08-23T12:04:24+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.7 (Oracle Corporation)"
)
@Component
public class UnitMapperImpl implements UnitMapper {

    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private LocationMapper locationMapper;
    @Autowired
    private ObjectLocationMapper objectLocationMapper;

    @Override
    public UnitDTO unitToUnitDTO(Unit unit) {
        if ( unit == null ) {
            return null;
        }

        UnitDTO unitDTO = new UnitDTO();

        unitDTO.setTaskDTO( taskMapper.taskToTaskDTO( unit.getTask() ) );
        unitDTO.setLocationDTO( locationMapper.locationToLocationDTO( unit.getLocation() ) );
        unitDTO.setObjectDTO( objectLocationMapper.objectToObjectDTO( unit.getObject() ) );
        unitDTO.setUnitID( unit.getUnitID() );
        unitDTO.setName( unit.getName() );
        unitDTO.setHint( unit.getHint() );
        unitDTO.setUnitOrder( unit.getUnitOrder() );

        return unitDTO;
    }
}
