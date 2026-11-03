package com.example.PizzUMBurgUM.configuracion;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationFailureHandler.class);

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String errorMessage = "Error de autenticación: ";

        logger.warn("Error de autenticación de tipo {}.", exception.getClass().getSimpleName());
        if (exception instanceof BadCredentialsException) {
            errorMessage += "Credenciales incorrectas";
        } else if (exception instanceof UsernameNotFoundException) {
            errorMessage += "Usuario no encontrado";
        } else if (exception instanceof LockedException) {
            errorMessage += "Cuenta bloqueada";
        } else if (exception instanceof DisabledException) {
            errorMessage += "Cuenta deshabilitada";
        } else {
            errorMessage += exception.getMessage();
        }

        // Redirigir a la página de login con el mensaje de error
        request.getSession().setAttribute("error", errorMessage);
        response.sendRedirect(request.getContextPath() + "/iniciar-sesion?error");
    }
}
