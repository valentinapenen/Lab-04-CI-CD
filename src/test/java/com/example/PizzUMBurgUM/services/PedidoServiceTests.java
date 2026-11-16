package com.example.PizzUMBurgUM.services;

import com.example.PizzUMBurgUM.entities.Cliente;
import com.example.PizzUMBurgUM.entities.Pedido;
import com.example.PizzUMBurgUM.entities.enums.EstadoPedido;
import com.example.PizzUMBurgUM.repositories.ClienteRepository;
import com.example.PizzUMBurgUM.repositories.CreacionRepository;
import com.example.PizzUMBurgUM.repositories.DomicilioRepository;
import com.example.PizzUMBurgUM.repositories.PedidoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTests {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private CreacionRepository creacionRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private DomicilioRepository domicilioRepository;

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    void cambiarEstado_avanzaElPedido() {
        Pedido pedido = pedidoEn(EstadoPedido.EN_COLA);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(pedido)).thenReturn(pedido);

        Pedido resultado = pedidoService.cambiarEstado(1L, EstadoPedido.EN_PREPARACION);

        assertEquals(EstadoPedido.EN_PREPARACION, resultado.getEstado());
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void cambiarEstado_permiteCancelarUnPedidoEnCola() {
        Pedido pedido = pedidoEn(EstadoPedido.EN_COLA);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(pedido)).thenReturn(pedido);

        Pedido resultado = pedidoService.cambiarEstado(1L, EstadoPedido.CANCELADO);

        assertEquals(EstadoPedido.CANCELADO, resultado.getEstado());
    }

    @Test
    void cambiarEstado_rechazaRetrocederElEstado() {
        Pedido pedido = pedidoEn(EstadoPedido.EN_CAMINO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThrows(IllegalArgumentException.class,
                () -> pedidoService.cambiarEstado(1L, EstadoPedido.EN_PREPARACION));

        assertEquals(EstadoPedido.EN_CAMINO, pedido.getEstado());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void cambiarEstado_rechazaModificarUnPedidoCancelado() {
        Pedido pedido = pedidoEn(EstadoPedido.CANCELADO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThrows(IllegalStateException.class,
                () -> pedidoService.cambiarEstado(1L, EstadoPedido.EN_PREPARACION));

        assertEquals(EstadoPedido.CANCELADO, pedido.getEstado());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void cancelarPedido_elClienteNoPuedeCancelarUnPedidoEnPreparacion() {
        Pedido pedido = pedidoEn(EstadoPedido.EN_PREPARACION);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThrows(IllegalStateException.class,
                () -> pedidoService.cancelarPedido(10L, 1L));

        assertEquals(EstadoPedido.EN_PREPARACION, pedido.getEstado());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void cancelarPedido_rechazaUnPedidoDeOtroCliente() {
        Pedido pedido = pedidoEn(EstadoPedido.EN_COLA);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThrows(IllegalArgumentException.class,
                () -> pedidoService.cancelarPedido(99L, 1L));

        assertEquals(EstadoPedido.EN_COLA, pedido.getEstado());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    /** Pedido 1 del cliente 10, en el estado indicado y sin creaciones. */
    private Pedido pedidoEn(EstadoPedido estado) {
        Cliente cliente = new Cliente();
        cliente.setId(10L);
        return Pedido.builder()
                .idPedido(1L)
                .cliente(cliente)
                .estado(estado)
                .total(810.0)
                .creaciones(new ArrayList<>())
                .build();
    }
}
