package com.nikolas.mechanicalmanagementsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "machines")
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Machine brand is required")
    @Column(name = "machine_brand")
    private String machineBrand;

    @NotBlank(message = "Machine model is required")
    @Column(name = "machine_model")
    private String machineModel;

    @Column(name = "machine_description")
    @Size(max = 1024)
    private String description;

    @Column(name = "machine_parts_sum")
    private BigDecimal machinePartsSum;


    @OneToMany(mappedBy = "machine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MachinePart> machineParts = new ArrayList<>();

    @Column(name = "charged_amount")
    private BigDecimal chargedAmount;


    @Column(name = "profit")
    private BigDecimal profit;

    @CreationTimestamp
    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @UpdateTimestamp
    @Column(name = "last_modified_date")
    private LocalDateTime finishedDate;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "tax_in_percent")
    private BigDecimal taxInPercent;

    @OneToMany(mappedBy = "machine", cascade = {CascadeType.ALL})
    private List<Images> images = new ArrayList<>();


    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;


    public BigDecimal getTaxInPercent() {
        if (taxInPercent == null) {
            return BigDecimal.valueOf(0.0);
        }
        return taxInPercent;
    }

    public BigDecimal getChargedAmount() {
        if (chargedAmount == null) {
            return BigDecimal.valueOf(0.0);
        }
        return chargedAmount;
    }

    public BigDecimal getMachinePartsSum() {
        if (machinePartsSum == null) {
            return BigDecimal.valueOf(0.0);
        }
        return machinePartsSum;
    }

    public void setMachinePartsSum(BigDecimal machinePartsSum) {
        this.machinePartsSum = machinePartsSum;
    }

}