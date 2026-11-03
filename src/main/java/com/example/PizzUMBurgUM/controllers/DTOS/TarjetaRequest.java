package com.example.PizzUMBurgUM.controllers.DTOS;

import com.example.PizzUMBurgUM.entities.enums.TipoTarjeta;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter

public class TarjetaRequest {
    @NotNull
    private String numero;
    @NotNull
    private String nombreTitular;
    @NotNull
    private long clienteId;
    @NotNull
    private TipoTarjeta tipoTarjeta;
    @NotNull
    private String fechaVencimiento;
    @NotNull
    private Boolean predeterminada;
}
