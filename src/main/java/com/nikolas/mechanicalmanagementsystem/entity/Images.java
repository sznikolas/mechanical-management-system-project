package com.nikolas.mechanicalmanagementsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "images")
public class Images {

    public static final int MAX_IMAGE_SIZE = 5 * 1024 * 1024; //5MB
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Size(max = 100)
    @Pattern(regexp = "image/[a-zA-Z]+")
    private String type;

    @Lob
    @Column(name = "image", length = MAX_IMAGE_SIZE)
    private byte[] picByte;


    @Column(name = "base64_image", columnDefinition = "LONGTEXT")
    private String base64Image;

    @ManyToOne
    @JoinColumn(name = "machine_id")
    private Machine machine;


}
