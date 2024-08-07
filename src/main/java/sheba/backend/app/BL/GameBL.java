package sheba.backend.app.BL;

import com.google.zxing.WriterException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sheba.backend.app.DTO.GameDTO;
import sheba.backend.app.entities.Admin;
import sheba.backend.app.entities.Game;
import sheba.backend.app.entities.GameImage;
import sheba.backend.app.entities.Unit;
import sheba.backend.app.exceptions.ImageDeleteFailed;
import sheba.backend.app.exceptions.MediaUploadFailed;
import sheba.backend.app.mappers.GameMapper;
import sheba.backend.app.repositories.AdminRepository;
import sheba.backend.app.repositories.GameImageRepository;
import sheba.backend.app.repositories.GameRepository;
import sheba.backend.app.security.CustomAdminDetails;
import sheba.backend.app.util.Endpoints;
import sheba.backend.app.util.QRCodeGenerator;
import sheba.backend.app.util.StoragePath;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class GameBL {
    private final GameRepository gameRepository;
    private final AdminRepository adminRepository;
    private final GameImageRepository gameImageRepository;
    private final UnitBL unitBL;
    private final GcsBL gcsBL;
    private final GameMapper gameMapper;

    public GameBL(GameRepository gameRepository, AdminRepository adminRepository, GameImageRepository gameImageRepository, UnitBL unitBL, GcsBL gcsBL, GameMapper gameMapper) {
        this.gameRepository = gameRepository;
        this.adminRepository = adminRepository;
        this.gameImageRepository = gameImageRepository;
        this.unitBL = unitBL;

        this.gcsBL = gcsBL;
        this.gameMapper = gameMapper;
    }

    @Transactional
    public Game createGame(Game game, MultipartFile image, List<Unit> units) throws IOException, WriterException {
        if(SecurityContextHolder.getContext().getAuthentication() != null){
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomAdminDetails adminDetails = (CustomAdminDetails) authentication.getPrincipal();
            Admin admin = adminRepository.findAdminByUsername(adminDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Admin not found"));
            game.setAdmin(admin);
            game.setAdminID(admin.getAdminID());
        }

        Game savedGame = gameRepository.save(game);
        if(units != null){
            Game finalSavedGame = savedGame;
            units.forEach(unit -> unitBL.createUnit(unit, finalSavedGame.getGameID()));
            savedGame = gameRepository.findById(savedGame.getGameID())
                    .orElseThrow(() -> new RuntimeException("Game not found after unit creation"));
        }
        if (image != null && !image.isEmpty()) {
            try {
                savedGame = saveGameImage(savedGame, image);
            } catch (IOException e) {
                throw new MediaUploadFailed("Failed to upload game image", e);
            }
        }

        try {
            String qrCodePath = generateGameQRCode(savedGame);
            savedGame.setQRCodePath(qrCodePath);
            savedGame.setQRCodeURL(gcsBL.getPublicUrl(qrCodePath));
        } catch (IOException e) {
            throw new MediaUploadFailed("Failed to generate or upload QR code", e);
        }

        return gameRepository.save(savedGame);
    }

    private String generateGameQRCode(Game game) throws IOException {
        String baseUrl = "https://sheba-service-gcp-tm3zus3bzq-uc.a.run.app";
        String gameApiUrl = baseUrl + Endpoints.GAME_ENDPOINT + "/get/"+ game.getGameID();
        System.out.println("game url" + gameApiUrl);
        ByteArrayOutputStream qrOutputStream = new ByteArrayOutputStream();
        try {
            QRCodeGenerator.generateQRCode("game-", gameApiUrl, qrOutputStream, StoragePath.GAME_QR_IMG);
        } catch (WriterException e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }

        String qrCodePath = gcsBL.bucketUploadBytes(
                qrOutputStream.toByteArray(),
                StoragePath.GAME_QR,
                "game-" + game.getGameID() + "-QRCODE.png",
                "image/png"
        );
        return qrCodePath;
    }

    private Game saveGameImage(Game game, MultipartFile image) throws IOException {
        String folderPath = StoragePath.GAME_IMGS_PATH + game.getGameID();
        String objectName = gcsBL.bucketUpload(image, folderPath);
        String publicUrl = gcsBL.getPublicUrl(objectName);

        GameImage gameImage = new GameImage();
        gameImage.setName(image.getOriginalFilename());
        gameImage.setType(image.getContentType());
        gameImage.setImagePath(objectName);
        gameImage.setImageURL(publicUrl);
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
        return gameRepository.save(existingGame);
    }

    public void deleteGame(Long id) throws ImageDeleteFailed {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Game not found with id " + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomAdminDetails adminDetails = (CustomAdminDetails) authentication.getPrincipal();

        if (!adminDetails.getUsername().equals(game.getAdmin().getUsername()) &&
                !adminDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MAIN_ADMIN"))) {
            throw new RuntimeException("You do not have permission to delete this game");
        }
        try {
            gcsBL.bucketDelete(game.getQRCodePath());
            if (game.getGameImage() != null) {
                System.out.println("image path is " + game.getGameImage().getImagePath());
                gcsBL.bucketDelete(game.getGameImage().getImagePath());
//                gcsBL.deleteFolder();
            }
        } catch (Exception e) {
            throw new ImageDeleteFailed("Could not Delete Game's Image or QR Code");
        }
        gameRepository.deleteById(id);
    }

}
