package com.example.PizzUMBurgUM.controllers;

import com.example.PizzUMBurgUM.entities.Cliente;
import com.example.PizzUMBurgUM.entities.Pedido;
import com.example.PizzUMBurgUM.entities.enums.EstadoPedido;
import com.example.PizzUMBurgUM.services.PedidoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:pruebas;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "app.seed-admin=false"
})
class PedidoControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PedidoService pedidoService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void listarPedidos_selectorMuestraElEstadoActualYNoCancelado() throws Exception {
        // Preparación: un pedido en cola, como queda recién confirmado por el cliente
        Cliente cliente = new Cliente();
        cliente.setNombre("Laura");
        Pedido pedido = Pedido.builder()
                .idPedido(1L)
                .cliente(cliente)
                .estado(EstadoPedido.EN_COLA)
                .total(810.0)
                .build();
        when(pedidoService.listarActivos()).thenReturn(List.of(pedido));

        // Ejecución: el administrador abre la lista de pedidos
        String html = mockMvc.perform(get("/pedidos"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Verificación: el selector arranca en el estado actual, no en la primera opción (CANCELADO)
        String opciones = html.replaceAll("\\s+", " ");
        assertTrue(opciones.contains("value=\"EN_COLA\" selected=\"selected\""),
                "El selector debe mostrar seleccionado el estado actual del pedido");
        assertFalse(opciones.contains("value=\"CANCELADO\" selected=\"selected\""),
                "El selector no debe proponer CANCELADO por defecto");
    }
}
