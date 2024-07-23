package sheba.backend.app.BL;

import com.google.zxing.WriterException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sheba.backend.app.entities.Admin;
import sheba.backend.app.entities.Game;
import sheba.backend.app.entities.GameImage;
import sheba.backend.app.entities.Unit;
import sheba.backend.app.exceptions.MediaUploadFailed;
import sheba.backend.app.repositories.AdminRepository;
import sheba.backend.app.repositories.GameImageRepository;
import sheba.backend.app.repositories.GameRepository;
import sheba.backend.app.security.CustomAdminDetails;
import sheba.backend.app.util.QRCodeGenerator;
import sheba.backend.app.util.StoragePath;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
public class GameBL {
    private final GameRepository gameRepository;
    private final AdminRepository adminRepository;
    private final GameImageRepository gameImageRepository;
    private final UnitBL unitBL;
    private final GcsBL gcsBL;

    public GameBL(GameRepository gameRepository, AdminRepository adminRepository, GameImageRepository gameImageRepository, UnitBL unitBL, GcsBL gcsBL) {
        this.gameRepository = gameRepository;
        this.adminRepository = adminRepository;
        this.gameImageRepository = gameImageRepository;
        this.unitBL = unitBL;

        this.gcsBL = gcsBL;
    }

    public Game createGame(Game game, MultipartFile image, List<Unit> units) throws IOException, WriterException {
        if(SecurityContextHolder.getContext().getAuthentication() != null){
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomAdminDetails adminDetails = (CustomAdminDetails) authentication.getPrincipal();
            Admin admin = adminRepository.findAdminByUsername(adminDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Admin not found"));
            game.setAdmin(admin);
            game.setAdminID(admin.getAdminID());
        }
        else{
            game.setAdmin(adminRepository.findByAdminID(2));
            game.setAdminID(2);
        }

        Game savedGame = gameRepository.save(game);
        if(units != null){
            Game finalSavedGame = savedGame;
            units.forEach(unit -> unitBL.createUnit(unit, finalSavedGame.getGameID()));
        }
        if (image != null && !image.isEmpty()) {
            try {
                savedGame = saveGameImage(savedGame, image);
            } catch (IOException e) {
                throw new MediaUploadFailed("Failed to upload game image", e);
            }
        }

        String gameIdentifier = String.valueOf(savedGame.getGameID()); //change to URL later
        ByteArrayOutputStream qrOutputStream = new ByteArrayOutputStream();
        QRCodeGenerator.generateQRCode("game-" + savedGame.getGameID(), gameIdentifier, qrOutputStream, StoragePath.GAME_QR_IMG);

        try {
            String qrCodePath = gcsBL.bucketUploadBytes(
                    qrOutputStream.toByteArray(),
                    StoragePath.GAME_QR,
                    "game-" + savedGame.getGameID() + "-QRCODE.png",
                    "image/png"
            );
            savedGame.setQRCodePath(gcsBL.getPublicUrl(qrCodePath));
        } catch (IOException e) {
            throw new MediaUploadFailed("Failed to upload QR code", e);
        }

        return gameRepository.save(savedGame);
    }

    private Game saveGameImage(Game game, MultipartFile image) throws IOException {
        String folderPath = StoragePath.GAME_IMGS_PATH + game.getGameID();
        String objectName = gcsBL.bucketUpload(image, folderPath);
        String publicUrl = gcsBL.getPublicUrl(objectName);

        GameImage gameImage = new GameImage();
        gameImage.setName(image.getOriginalFilename());
        gameImage.setType(image.getContentType());
        gameImage.setImagePath(publicUrl);
        gameImage.setGame(game);

        gameImageRepository.save(gameImage);
        game.setGameImage(gameImage);
        return game;
    }
    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    public Optional<Game> getGameById(Long id) {
        return gameRepository.findById(id);
    }

    public Game updateGame(Long id, Game gameDetails) {
        Game existingGame = gameRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Game not found with id " + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomAdminDetails adminDetails = (CustomAdminDetails) authentication.getPrincipal();

        if (!adminDetails.getUsername().equals(existingGame.getAdmin().getUsername()) &&
                !adminDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MAIN_ADMIN"))) {
            throw new RuntimeException("You do not have permission to update this game");
        }

        existingGame.setGameName(gameDetails.getGameName());
        existingGame.setDescription(gameDetails.getDescription());
        // other fields to update
        return gameRepository.save(existingGame);
    }

    public void deleteGame(Long id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Game not found with id " + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomAdminDetails adminDetails = (CustomAdminDetails) authentication.getPrincipal();

        if (!adminDetails.getUsername().equals(game.getAdmin().getUsername()) &&
                !adminDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MAIN_ADMIN"))) {
            throw new RuntimeException("You do not have permission to delete this game");
        }

        gameRepository.deleteById(id);
    }

}
