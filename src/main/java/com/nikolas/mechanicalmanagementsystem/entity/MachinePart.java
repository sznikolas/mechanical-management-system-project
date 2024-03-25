package com.nikolas.mechanicalmanagementsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@NoArgsConstructor
@AllArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "machine_parts")
public class MachinePart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Machine part name is required")
    @Column(name = "machine_part")
    private String partName;

    @NotNull
    @Column(name = "machine_part_price")
    private BigDecimal partPrice;

    @Column(name = "tax_In_Percent")
    private BigDecimal taxInPercent;

    @Column(name = "part_Price_With_Tax")
    private BigDecimal partPriceWithTax;

    @Column(name = "unit_Tax")
    private BigDecimal unitTax;

    @CreationTimestamp
    @Column(name = "mpart_registration_date")
    private LocalDateTime mPartRegistrationDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "machine_id", nullable = false)
    private Machine machine;

    public BigDecimal getPartPrice() {
        if (partPrice == null) {
            return BigDecimal.valueOf(0.0);
        }
        return partPrice;
    }

    public BigDecimal getTaxInPercent() {
        if ( taxInPercent == null) {
            return BigDecimal.valueOf(0.0);
        }
        return taxInPercent;
    }

}
