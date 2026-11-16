package com.example.PizzUMBurgUM.services;

import com.example.PizzUMBurgUM.entities.*;
import com.example.PizzUMBurgUM.entities.enums.*;
import com.example.PizzUMBurgUM.repositories.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
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
class CreacionPedidoIntegrationTests {

    @Autowired
    private CreacionService creacionService;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private DomicilioRepository domicilioRepository;

    @Autowired
    private TarjetaRepository tarjetaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CreacionRepository creacionRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Test
    void crearPedidoDesdeCarrito_conDatosFicticios_guardaPedidoEnColaYLimpiaCarrito() {
        // 1. Preparación: cliente, domicilio, tarjeta, productos y dos creaciones en el carrito
        Cliente cliente = Cliente.builder()
                .nombre("Ana")
                .apellido("Gomez")
                .cedula("12345678")
                .fechaNacimiento(LocalDate.of(1998, 5, 15))
                .correo("ana.test@example.com")
                .telefono("099123456")
                .contrasena("password123")
                .domicilios(new ArrayList<>())
                .tarjetas(new ArrayList<>())
                .pedidos(new ArrayList<>())
                .build();
        cliente = clienteRepository.save(cliente);

        Domicilio domicilio = Domicilio.builder()
                .numero("1234")
                .calle("Av. 18 de Julio")
                .departamento("Montevideo")
                .ciudad("Montevideo")
                .apartamento("101")
                .predeterminado(true)
                .cliente(cliente)
                .build();
        domicilio = domicilioRepository.save(domicilio);

        Tarjeta tarjeta = Tarjeta.builder()
                .numero("4111111111111111")
                .nombreTitular("Ana Gomez")
                .cliente(cliente)
                .tipoTarjeta(TipoTarjeta.VISA)
                .fecha_vencimiento(new Date(System.currentTimeMillis() + 86400000L * 365))
                .predeterminada(true)
                .build();
        tarjeta = tarjetaRepository.save(tarjeta);

        Producto masa = Producto.builder()
                .nombre("Masa Tradicional")
                .precio(250.0)
                .tipo(TipoProducto.MASA)
                .categoria(CategoriaProducto.PIZZA)
                .disponible(true)
                .build();
        masa = productoRepository.save(masa);

        Producto carne = Producto.builder()
                .nombre("Carne vacuna")
                .precio(120.0)
                .tipo(TipoProducto.CARNE)
                .categoria(CategoriaProducto.HAMBURGUESA)
                .disponible(true)
                .build();
        carne = productoRepository.save(carne);

        Creacion item1 = Creacion.builder()
                .tipo(TipoCreacion.PIZZA)
                .productos(new ArrayList<>(List.of(masa)))
                .tamanoPizza(TamanoPizza.MEDIANA)
                .precioTotal(350.0)
                .favorito(false)
                .enCarrito(true)
                .cliente(cliente)
                .build();
        creacionRepository.save(item1);

        Creacion item2 = Creacion.builder()
                .tipo(TipoCreacion.HAMBURGUESA)
                .productos(new ArrayList<>(List.of(carne)))
                .precioTotal(150.0)
                .favorito(true)
                .enCarrito(true)
                .cliente(cliente)
                .build();
        creacionRepository.save(item2);

        double totalEsperado = 500.0; // 350.0 + 150.0

        // 2. Ejecución: se confirma el pedido desde el carrito
        Pedido pedidoGuardado = creacionService.crearPedidoDesdeCarrito(
                cliente,
                domicilio.getId(),
                tarjeta.getId()
        );

        // 3. Verificación: pedido guardado, total correcto, estado EN_COLA y carrito vacío
        assertNotNull(pedidoGuardado);
        assertTrue(pedidoGuardado.getIdPedido() > 0);
        assertEquals(totalEsperado, pedidoGuardado.getTotal(), 0.001);
        assertEquals(EstadoPedido.EN_COLA, pedidoGuardado.getEstado());

        // El pedido quedó guardado en la base H2
        Pedido pedidoEnBd = pedidoRepository.findById(pedidoGuardado.getIdPedido()).orElseThrow();
        assertEquals(totalEsperado, pedidoEnBd.getTotal(), 0.001);
        assertEquals(EstadoPedido.EN_COLA, pedidoEnBd.getEstado());

        // El carrito del cliente quedó vacío
        List<Creacion> carritoDespues = creacionRepository.findByClienteIdAndEnCarritoTrue(cliente.getId());
        assertTrue(carritoDespues.isEmpty(), "El carrito debe quedar vacío después de confirmar el pedido");
    }
}
