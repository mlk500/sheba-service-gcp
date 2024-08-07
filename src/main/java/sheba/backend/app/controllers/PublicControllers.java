package sheba.backend.app.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sheba.backend.app.BL.ObjectLocationBL;
import sheba.backend.app.DTO.ObjectImageModelDTO;
import sheba.backend.app.entities.ObjectLocation;
import sheba.backend.app.mappers.ObjectImageModelMapper;
import sheba.backend.app.util.Endpoints;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(Endpoints.PUBLIC_ENDPOINT)
public class PublicControllers {
    private final ObjectLocationBL objectLocationBL;
    private final ObjectImageModelMapper objectImageModelMapper;

    public PublicControllers(ObjectLocationBL objectLocationBL, ObjectImageModelMapper objectImageModelMapper) {
        this.objectLocationBL = objectLocationBL;
        this.objectImageModelMapper = objectImageModelMapper;
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
}
