package sheba.backend.app.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sheba.backend.app.DTO.GameDTO;
import sheba.backend.app.entities.Game;

@Mapper(componentModel = "spring", uses = {UnitMapper.class})
public interface GameMapper {
//    @Mapping(source = "gameID", target = "gameID")
//    @Mapping(source = "gameName", target = "gameName")
//    @Mapping(source = "QRCodePath", target = "QRCodePath")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "units", target = "units")
    GameDTO gameToGameDTO(Game game);

//    @Mapping(target = "gameImage.imagePath", source = "gameImage")
//    @Mapping(target = "admin", ignore = true)
//    Game gameDTOToGame(GameDTO gameDTO);
}
