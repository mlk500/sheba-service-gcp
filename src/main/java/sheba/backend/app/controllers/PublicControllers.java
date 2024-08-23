package sheba.backend.app.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sheba.backend.app.BL.GameBL;
import sheba.backend.app.BL.ObjectLocationBL;
import sheba.backend.app.DTO.GameDTO;
import sheba.backend.app.DTO.ObjectImageModelDTO;
import sheba.backend.app.entities.Game;
import sheba.backend.app.entities.ObjectLocation;
import sheba.backend.app.mappers.GameMapper;
import sheba.backend.app.mappers.ObjectImageModelMapper;
import sheba.backend.app.util.Endpoints;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(Endpoints.PUBLIC_ENDPOINT)
public class PublicControllers {
    private final ObjectLocationBL objectLocationBL;
    private final ObjectImageModelMapper objectImageModelMapper;
    private final GameBL gameBL;
    private final GameMapper gameMapper;


    public PublicControllers(ObjectLocationBL objectLocationBL, ObjectImageModelMapper objectImageModelMapper, GameBL gameBL, GameMapper gameMapper) {
        this.objectLocationBL = objectLocationBL;
        this.objectImageModelMapper = objectImageModelMapper;
        this.gameBL = gameBL;
        this.gameMapper = gameMapper;
    }

    @GetMapping(value = "get-objects-for-model")
    public ResponseEntity<List<ObjectImageModelDTO>> getAllObjects() {
        System.out.println("got a request");
        List<ObjectLocation> objectsModel = objectLocationBL.getAllObjects();
        List<ObjectImageModelDTO> objectsModelDTO = objectsModel.stream()
                .map(objectImageModelMapper::objectImgToObjectImgDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(objectsModelDTO);
    }

    @GetMapping("get-game/{id}")
    public ResponseEntity<GameDTO> getGameById(@PathVariable Long id) {
        Optional<Game> gameOptional = gameBL.getGameById(id);
        if(gameOptional.isPresent()){
            System.out.println("game is  " + gameOptional.get().getGameID());
            return gameOptional
                    .map(game -> ResponseEntity.ok(gameMapper.gameToGameDTO(game)))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("wakeup")
    public ResponseEntity<?> wakeup() {
        try {
            System.out.println("wake up");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error waking up the server: " + e.getMessage());
        }
    }
}
