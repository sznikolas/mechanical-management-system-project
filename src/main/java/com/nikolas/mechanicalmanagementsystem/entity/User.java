package com.nikolas.mechanicalmanagementsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NaturalId;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name="user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Firstname is required")
    @Column(nullable=false)
    private String firstName;

    @NotBlank(message = "Lastname is required")
    @Column(nullable=false)
    private String lastName;

    @NotNull
    @NotBlank(message = "Email is required")
    @Column(nullable=false, unique=true)
    @NaturalId(mutable = true)
    @Email(message = "Invalid email format!")
    private String email;

    @Size(min = 3, message = "PW must be at least 3 characters long")
    @NotBlank(message = "Password is required")
    @Column(nullable=false)
    private String password;

    @Column(name = "verified_email")
    private boolean isEnabled = false;

    @Column(name = "non_locked")
    private boolean isAccountNonLocked = true;

    @CreationTimestamp
    @Column(name = "registration_date")
    private LocalDateTime registrationDate;


    @ManyToMany(fetch = FetchType.EAGER, cascade=CascadeType.PERSIST)
    @JoinTable(
            name="user_roles",
            joinColumns={@JoinColumn(name="user_id", referencedColumnName="id")},
            inverseJoinColumns={@JoinColumn(name="role_id", referencedColumnName="id")})
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "owner")
    private Set<Company> ownedCompanies; // my own companies

    @ManyToMany(mappedBy = "employees")
    private List<Company> workplaces; // my workplaces

    @OneToMany(mappedBy = "employee", fetch = FetchType.EAGER)
    private Set<JobApplication> jobApplications = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        User user = (User) o;
        return getId() != null && Objects.equals(getId(), user.getId())
                && getEmail() != null && Objects.equals(getEmail(), user.getEmail());
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}

