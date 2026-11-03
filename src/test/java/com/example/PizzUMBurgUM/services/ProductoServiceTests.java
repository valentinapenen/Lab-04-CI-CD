package com.example.PizzUMBurgUM.services;

import com.example.PizzUMBurgUM.entities.Producto;
import com.example.PizzUMBurgUM.entities.enums.CategoriaProducto;
import com.example.PizzUMBurgUM.entities.enums.TipoProducto;
import com.example.PizzUMBurgUM.repositories.ProductoRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTests {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 25.0})
    void crearAceptaPreciosNoNegativos(double precio) {
        Producto producto = salsaConPrecio(precio);
        when(productoRepository.save(producto)).thenReturn(producto);

        Producto resultado = productoService.crear(producto);

        assertEquals(precio, resultado.getPrecio());
        assertTrue(resultado.isDisponible());
        verify(productoRepository).save(producto);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.01, -1.0})
    void crearRechazaPreciosNegativos(double precio) {
        Producto producto = salsaConPrecio(precio);

        assertThrows(IllegalArgumentException.class, () -> productoService.crear(producto));

        verify(productoRepository, never()).save(any(Producto.class));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 25.0})
    void actualizarAceptaPreciosNoNegativos(double precio) {
        Producto producto = salsaConPrecio(10.0);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(producto)).thenReturn(producto);

        Producto resultado = productoService.actualizarPrecio(1L, precio);

        assertEquals(precio, resultado.getPrecio());
        verify(productoRepository).save(producto);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.01, -1.0})
    void actualizarRechazaPreciosNegativos(double precio) {
        Producto producto = salsaConPrecio(10.0);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        assertThrows(IllegalArgumentException.class, () -> productoService.actualizarPrecio(1L, precio));

        assertEquals(10.0, producto.getPrecio());
        verify(productoRepository, never()).save(any(Producto.class));
    }

    private Producto salsaConPrecio(double precio) {
        return Producto.builder()
                .nombre("Salsa de tomate")
                .tipo(TipoProducto.SALSA)
                .categoria(CategoriaProducto.PIZZA)
                .precio(precio)
                .build();
    }
}
