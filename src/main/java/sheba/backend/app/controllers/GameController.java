package sheba.backend.app.controllers;

import com.google.zxing.WriterException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sheba.backend.app.BL.GameBL;
import sheba.backend.app.DTO.GameDTO;
import sheba.backend.app.entities.Game;
import sheba.backend.app.entities.Unit;
import sheba.backend.app.exceptions.MediaUploadFailed;
import sheba.backend.app.mappers.GameMapper;
import sheba.backend.app.util.Endpoints;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(Endpoints.GAME_ENDPOINT)
public class GameController {
    private final GameBL gameBL;
    private final GameMapper gameMapper;


    public GameController(GameBL gameBL, GameMapper gameMapper) {
        this.gameBL = gameBL;
        this.gameMapper = gameMapper;
    }

    @PostMapping(value = "create", consumes = {"multipart/form-data" })
    public ResponseEntity<?> createGame(@RequestPart("game") Game game,
                                        @RequestPart(value = "image", required = false) MultipartFile image,
                                        @RequestPart(value = "units", required = false) List<Unit> units) {
        try {
            Game createdGame = gameBL.createGame(game, image, units);
            return ResponseEntity.ok(createdGame);
        } catch (MediaUploadFailed e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading media: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println(("error is " + e.getMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving game: " + e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing request: " + e.getMessage());
        } catch (WriterException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating QR for game: " + e.getMessage());
        }
    }

    @GetMapping("getAll")
    public ResponseEntity<List<Game>> getAllGames() {
        List<Game> games = gameBL.getAllGames();
//        List<GameDTO> gameDTOs = games.stream()
//                .map(gameMapper::gameToGameDTO)
//                .collect(Collectors.toList());
//        System.out.println("game dto is " + gameDTOs);
        return ResponseEntity.ok(games);
    }

    @GetMapping("get/{id}")
    public ResponseEntity<GameDTO> getGameById(@PathVariable Long id) {
        Optional<Game> gameOptional = gameBL.getGameById(id);
        System.out.println("game is  " + gameOptional.get().toString());
        System.out.println("game dto is "+ gameMapper.gameToGameDTO(gameOptional.get()).toString());
        return gameOptional
                .map(game-> ResponseEntity.ok(gameMapper.gameToGameDTO(game)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

//    @PutMapping("update/{id}")
//    public ResponseEntity<?> updateGame(@PathVariable Long id, @RequestPart(value = "game", required = false) Game gameDetails,
//                                        @RequestPart(value = "updatedUnits", required = false) List<Unit> updatedUnits,
//                                        @RequestPart(value = "deletedUnitsIds", required = false) List<Long> deletedUnits,
//                                        @RequestPart(value = "newUnits", required = false) List<Unit> newUnits
//    ) {
//        System.out.println("got request " + id+" " + gameDetails.getGameName());
//        try {
//            Game updatedGame = gameBL.updateGame(id, gameDetails, updatedUnits, newUnits, deletedUnits);
//            return ResponseEntity.ok(updatedGame);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body("Error updating the game: " + e.getMessage());
//        }
//    }

    @PutMapping("update/{id}")
    public ResponseEntity<?> updateGame(@PathVariable Long id, @RequestPart(value = "game", required = false) Game gameDetails,
                                        @RequestPart(value = "units", required = false) List<Unit> units
    ) {
        System.out.println("got request " + id+" " + gameDetails.getGameName());
        try {
            Game updatedGame = gameBL.updateGame(id, gameDetails, units);
            return ResponseEntity.ok(updatedGame);
        } catch (Exception e) {
            System.out.println("Error in updating the game: " + e.getMessage());
            return ResponseEntity.badRequest().body("Error updating the game: " + e.getMessage());
        }
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> deleteGame(@PathVariable Long id) {
        try {
            gameBL.deleteGame(id);
            return ResponseEntity.ok("");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting the game: " + e.getMessage());
        }
    }
}
