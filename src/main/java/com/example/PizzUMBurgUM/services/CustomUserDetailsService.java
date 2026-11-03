package com.example.PizzUMBurgUM.services;

import com.example.PizzUMBurgUM.entities.Usuario;
import com.example.PizzUMBurgUM.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreo(correo);

        if (usuario == null) {
            throw new UsernameNotFoundException("Usuario no encontrado con correo: " + correo);
        }

        // Determinar el rol basado en el tipo de usuario
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        if (usuario.getClass().getSimpleName().equals("Administrador")) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else if (usuario.getClass().getSimpleName().equals("Cliente")) {
            authorities.add(new SimpleGrantedAuthority("ROLE_CLIENT"));
        }

        return new User(
                usuario.getCorreo(),
                usuario.getContrasena(), // Spring Security comparará esto con la contraseña ingresada
                true, // enabled
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                authorities
        );
    }
}