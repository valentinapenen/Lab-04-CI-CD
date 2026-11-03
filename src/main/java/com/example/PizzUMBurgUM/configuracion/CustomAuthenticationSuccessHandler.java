package com.example.PizzUMBurgUM.configuracion;

import com.example.PizzUMBurgUM.entities.Usuario;
import com.example.PizzUMBurgUM.repositories.UsuarioRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // Obtener el usuario completo de la base de datos
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName());

        if (usuario != null) {
            // Guardar el usuario en la sesión con el nombre de atributo esperado por AuthController
            HttpSession session = request.getSession();
            session.setAttribute("usuarioLogueado", usuario);
        } else {
            logger.warn("No se encontró el usuario después de la autenticación.");
        }

        // Redirigir a la URL de éxito configurada en Spring Security
        response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/post-login"));
    }
}
