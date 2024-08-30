package sheba.backend.app.BL;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sheba.backend.app.DTO.AdminDTO;
import sheba.backend.app.entities.Admin;
import sheba.backend.app.entities.Game;
import sheba.backend.app.entities.Task;
import sheba.backend.app.enums.UserRole;
import sheba.backend.app.exceptions.AdminAlreadyExists;
import sheba.backend.app.repositories.AdminRepository;
import sheba.backend.app.mappers.AdminMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminBL {

    private final PasswordEncoder passwordEncoder;
    private final AdminRepository adminRepository;
    private final AdminMapper adminMapper = AdminMapper.INSTANCE;

    public AdminDTO createSectorAdmin(Admin admin) throws AdminAlreadyExists {
        if (admin.getUsername() == null || admin.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        if (this.adminRepository.findAdminByUsername(admin.getUsername()).isPresent()) {
            throw new AdminAlreadyExists("Admin with username " + admin.getUsername() + " already exists");
        }

        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        admin.setRole(UserRole.SectorAdmin);
        Admin savedAdmin = this.adminRepository.save(admin);

        return adminMapper.adminToAdminDTO(savedAdmin);
    }

    public List<AdminDTO> getAllAdmins() {
        List<Admin> adminList = adminRepository.findAll();
        return adminList.stream()
                .map(adminMapper::adminToAdminDTO)
                .collect(Collectors.toList());
    }

    public AdminDTO updateAdmin(long adminID, Admin admin, String newPassword) {

        Admin currAdmin = adminRepository.findById(adminID).orElseThrow(() -> new EntityNotFoundException("Admin not found with ID " + adminID));
        if(newPassword != null && !newPassword.isEmpty()){
            currAdmin.setPassword(passwordEncoder.encode(newPassword));
        }
        currAdmin.setUsername(admin.getUsername());
        currAdmin.setSector(admin.getSector());
        currAdmin.setRole(admin.getRole());
        currAdmin.getGamesList().clear();
        currAdmin.setGamesList(admin.getGamesList());
        adminRepository.save(currAdmin);
        return adminMapper.adminToAdminDTO(currAdmin);
    }

    public void deleteAdmin(long id) {
        Admin admin = adminRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Admin not found with ID: " + id));
        List<Game> games = admin.getGamesList();
        List<Task> tasks = admin.getTasksList();
        Admin mainAdmin = adminRepository.findById(1L).orElseThrow(() ->
                new EntityNotFoundException("Could not find MainAdmin with ID: 1" ));
        if(games != null && !games.isEmpty()){
            for (Game game: games){
                game.setAdmin(mainAdmin);
                game.setAdminID(mainAdmin.getAdminID());
            }
            mainAdmin.getGamesList().addAll(games);
        }
        if(tasks != null && !tasks.isEmpty()){
            for (Task task: tasks){
                task.setAdmin(mainAdmin);
                task.setAdminIDAPI(mainAdmin.getAdminID());
            }
            mainAdmin.getTasksList().addAll(tasks);
        }
        adminRepository.delete(admin);
    }
}
