package com.example.PizzUMBurgUM.services;

import com.example.PizzUMBurgUM.entities.Cliente;
import com.example.PizzUMBurgUM.repositories.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreacionServiceTests {

    @Mock
    private CreacionRepository creacionRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private DomicilioRepository domicilioRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private TarjetaRepository tarjetaRepository;

    @Mock
    private PrecioTamanoPizzaService precioTamanoPizzaService;

    @InjectMocks
    private CreacionService creacionService;

    @Test
    void crearPedidoDesdeCarrito_carritoVacio_rechazaYSinGuardarPedido() {
        // Arrange: клиент с пустой корзиной
        Cliente cliente = new Cliente();
        cliente.setId(1L);

        when(creacionRepository.findByClienteIdAndEnCarritoTrue(1L))
                .thenReturn(Collections.emptyList());

        // Act & Assert: проверяем выброс IllegalStateException
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> creacionService.crearPedidoDesdeCarrito(cliente, 10L, 20L)
        );

        assertEquals("El carrito está vacío", exception.getMessage());

        // Verify: заказ не сохранялся в репозиторий
        verify(pedidoRepository, never()).save(any());
    }
}

