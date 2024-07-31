package sheba.backend.app.mappers;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sheba.backend.app.DTO.GameDTO;
import sheba.backend.app.DTO.UnitDTO;
import sheba.backend.app.entities.Game;
import sheba.backend.app.entities.Unit;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-07-30T23:31:04+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.7 (Oracle Corporation)"
)
@Component
public class GameMapperImpl implements GameMapper {

    @Autowired
    private UnitMapper unitMapper;

    @Override
    public GameDTO gameToGameDTO(Game game) {
        if ( game == null ) {
            return null;
        }

        GameDTO gameDTO = new GameDTO();

        gameDTO.setUnits( unitListToUnitDTOList( game.getUnits() ) );
        gameDTO.setGameID( game.getGameID() );
        gameDTO.setGameName( game.getGameName() );
        gameDTO.setQRCodePath( game.getQRCodePath() );

        return gameDTO;
    }

    protected List<UnitDTO> unitListToUnitDTOList(List<Unit> list) {
        if ( list == null ) {
            return null;
        }

        List<UnitDTO> list1 = new ArrayList<UnitDTO>( list.size() );
        for ( Unit unit : list ) {
            list1.add( unitMapper.unitToUnitDTO( unit ) );
        }

        return list1;
    }
}
