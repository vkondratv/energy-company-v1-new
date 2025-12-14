package com.energy_company_v1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Entity
@Table(name = "energy_objects")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnergyObject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Название обязательно")
    @Size(max = 100, message = "Название не должно превышать 100 символов")
    private String name;

    @NotBlank(message = "Тип обязателен")
    @Size(max = 50, message = "Тип не должен превышать 50 символов")
    private String type;

    @NotBlank(message = "Местоположение обязательно")
    @Size(max = 100, message = "Местоположение не должно превышать 100 символов")
    private String location;

    @NotNull(message = "Мощность обязательна")
    @Min(value = 0, message = "Мощность не может быть отрицательной")
    private Double power;

    @NotNull(message = "Год ввода в эксплуатацию обязателен")
    @Min(value = 1900, message = "Год должен быть не ранее 1900")
    @Max(value = 2100, message = "Год должен быть не позднее 2100")
    private Integer commissioningYear;

    @NotNull(message = "КПД обязателен")
    @DecimalMin(value = "0.0", message = "КПД не может быть отрицательным")
    @DecimalMax(value = "100.0", message = "КПД не может превышать 100%")
    private Double efficiency;

    @NotNull(message = "Статус обязателен")
    private Boolean active = true;

    private LocalDate lastMaintenanceDate;

    @Size(max = 500, message = "Описание не должно превышать 500 символов")
    private String description;

    @Override
    public String toString() {
        return "EnergyObject{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", location='" + location + '\'' +
                ", power=" + power +
                ", commissioningYear=" + commissioningYear +
                ", efficiency=" + efficiency +
                ", active=" + active +
                ", description='" + description + '\'' +
                ", lastMaintenanceDate=" + lastMaintenanceDate +
                '}';
    }
}