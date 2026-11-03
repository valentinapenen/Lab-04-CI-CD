package com.example.PizzUMBurgUM.controllers.DTOS;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter

public class DomicilioRequest {
    @NotNull
    private String numero;
    @NotNull
    private String calle;
    @NotNull
    private String departamento;
    @NotNull
    private String ciudad;
    private String apartamento;
    @NotNull
    private Boolean predeterminado;
}
