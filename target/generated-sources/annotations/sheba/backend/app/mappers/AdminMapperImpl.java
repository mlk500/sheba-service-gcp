package sheba.backend.app.mappers;

import javax.annotation.processing.Generated;
import sheba.backend.app.DTO.AdminDTO;
import sheba.backend.app.entities.Admin;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-08-23T12:04:20+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.7 (Oracle Corporation)"
)
public class AdminMapperImpl implements AdminMapper {

    @Override
    public AdminDTO adminToAdminDTO(Admin admin) {
        if ( admin == null ) {
            return null;
        }

        AdminDTO adminDTO = new AdminDTO();

        adminDTO.setAdminID( admin.getAdminID() );
        adminDTO.setUsername( admin.getUsername() );
        adminDTO.setSector( admin.getSector() );
        adminDTO.setRole( admin.getRole() );

        return adminDTO;
    }

    @Override
    public Admin adminDTOToAdmin(AdminDTO adminDTO) {
        if ( adminDTO == null ) {
            return null;
        }

        Admin.AdminBuilder admin = Admin.builder();

        admin.adminID( adminDTO.getAdminID() );
        admin.username( adminDTO.getUsername() );
        admin.sector( adminDTO.getSector() );
        admin.role( adminDTO.getRole() );

        return admin.build();
    }
}
