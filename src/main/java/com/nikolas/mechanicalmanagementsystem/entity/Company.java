package com.nikolas.mechanicalmanagementsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "company")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id")
    private Long id;

    @NotBlank(message = "Company name is required")
    @Size(min = 3, message = "Company name must be at least 3 characters long")
    @Column(name = "company_name")
    private String companyName;

    @Column(name = "company_description")
    @Size(max = 1024)
    private String description;

    @NotBlank(message = "Country is required")
    @Size(min = 3, message = "Country name must be at least 3 characters long")
    private String country;
    private String location;
    private String street;
    private int postCode;

    @Column(name = "company_ID_number")
    private Long identificationNumber;
    @Column(name = "TIN")
    private Long taxIdentificationNumber;
    @Column(name = "VAT_ID")
    private String valueAddedTaxIdentificationNumber;

    @CreationTimestamp
    @Column(name = "registration_date", updatable = false)
    private LocalDateTime registrationDate;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "company_employees",
            joinColumns = @JoinColumn(name = "company_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> employees = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToOne
    @JoinColumn(name = "deputy_leader_id")
    private User deputyLeader;


    @OneToMany(mappedBy = "company")
    private Set<JobApplication> jobApplications;

    public Set<JobApplication> getJobApplications() {
        return jobApplications;
    }

    @OneToMany(mappedBy = "company")
    private Set<CompanyRole> companyRoles;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    private Set<Machine> machines = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Company company = (Company) o;
        return getId() != null && Objects.equals(getId(), company.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
