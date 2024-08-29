package sheba.backend.app.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "location_image")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LocationImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long locationImgID;
    private String name;
    private String type;
    private String gcsObjectName;
    private String imageURL;

    @JsonIgnore
    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "fk_location_id")
    private Location location;

}
