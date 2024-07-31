package sheba.backend.app.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.checkerframework.common.aliasing.qual.Unique;

import java.util.List;

@Entity
@Table(name = "Object")
@Data
public class ObjectLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long objectID;
    @Unique
    private String name;
    private String description;
//    private String objectImg;

    //many to one with location
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_location_id")
    private Location location;

    // one to many with object image
    @OneToMany(mappedBy = "object", cascade = CascadeType.ALL, orphanRemoval = true)
    //if an object is deleted the images are deleted as well
    private List<ObjectImage> objectImages;


}
