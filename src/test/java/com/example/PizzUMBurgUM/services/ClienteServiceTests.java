package com.example.PizzUMBurgUM.services;

import com.example.PizzUMBurgUM.controllers.DTOS.DomicilioRequest;
import com.example.PizzUMBurgUM.controllers.DTOS.RegistroClienteRequest;
import com.example.PizzUMBurgUM.controllers.DTOS.TarjetaRequest;
import com.example.PizzUMBurgUM.entities.Cliente;
import com.example.PizzUMBurgUM.entities.enums.TipoTarjeta;
import com.example.PizzUMBurgUM.repositories.ClienteRepository;
import com.example.PizzUMBurgUM.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTests {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private DomicilioService domicilioService;

    @Mock
    private TarjetaService tarjetaService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    void registrarCliente_guardaClienteConContrasenaCifradaDomicilioYTarjeta() {
        RegistroClienteRequest req = registroValido();
        when(passwordEncoder.encode("clave123")).thenReturn("hash-de-clave123");
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        Cliente cliente = clienteService.registrarCliente(req);

        assertEquals("hash-de-clave123", cliente.getContrasena());
        assertEquals(1, cliente.getDomicilios().size());
        assertTrue(cliente.getDomicilios().get(0).isPredeterminado());
        assertEquals(1, cliente.getTarjetas().size());
        assertTrue(cliente.getTarjetas().get(0).isPredeterminada());

        // El vencimiento 12/2030 se guarda como el último día de ese mes
        Calendar vencimiento = Calendar.getInstance();
        vencimiento.setTime(cliente.getTarjetas().get(0).getFecha_vencimiento());
        assertEquals(2030, vencimiento.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, vencimiento.get(Calendar.MONTH));
        assertEquals(31, vencimiento.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    void registrarCliente_rechazaContrasenasDistintasSinGuardar() {
        RegistroClienteRequest req = registroValido();
        req.setContrasena2("otraClave");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> clienteService.registrarCliente(req));

        assertEquals("Las contraseñas no coinciden.", error.getMessage());
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void registrarCliente_rechazaCorreoYaRegistradoSinGuardar() {
        RegistroClienteRequest req = registroValido();
        when(usuarioRepository.existsByCorreo("laura.prueba@example.com")).thenReturn(true);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> clienteService.registrarCliente(req));

        assertEquals("El correo ya está registrado.", error.getMessage());
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void registrarCliente_rechazaCedulaYaRegistradaSinGuardar() {
        RegistroClienteRequest req = registroValido();
        when(clienteRepository.existsByCedula("11111111")).thenReturn(true);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> clienteService.registrarCliente(req));

        assertEquals("Ya existe un cliente con esta cédula.", error.getMessage());
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    /** Datos ficticios de registro, iguales a los usados en la prueba manual. */
    private RegistroClienteRequest registroValido() {
        DomicilioRequest domicilio = new DomicilioRequest();
        domicilio.setNumero("742");
        domicilio.setCalle("Av. Siempre Viva");
        domicilio.setDepartamento("Montevideo");
        domicilio.setCiudad("Montevideo");

        TarjetaRequest tarjeta = new TarjetaRequest();
        tarjeta.setNumero("4111111111111111");
        tarjeta.setNombreTitular("Laura Prueba");
        tarjeta.setTipoTarjeta(TipoTarjeta.VISA);
        tarjeta.setFechaVencimiento("12/2030");

        RegistroClienteRequest req = new RegistroClienteRequest();
        req.setNombre("Laura");
        req.setApellido("Prueba");
        req.setCedula("11111111");
        req.setFechaNacimiento(LocalDate.of(1999, 3, 10));
        req.setCorreo("laura.prueba@example.com");
        req.setTelefono("099111111");
        req.setContrasena("clave123");
        req.setContrasena2("clave123");
        req.setDomicilio(domicilio);
        req.setTarjeta(tarjeta);
        return req;
    }
}
