package com.moviewatch.movie_watch_backend.security;

import com.moviewatch.movie_watch_backend.entity.User;
import com.moviewatch.movie_watch_backend.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

// Carrega os dados de um usuário para o Spring Security.
//
// A autenticação da API é feita por token, mas este bean é
// necesssário para que o Spring Security NÃO crie o usuário
// padrão com senha gerada automaticamente.
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String normalized = email == null ? "" : email.trim().toLowerCase();

        User user = userRepository.findByEmail(normalized)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));

        List<SimpleGrantedAuthority> authorities = Boolean.TRUE.equals(user.getIsBlocked())
                ? List.of()
                : List.of(new SimpleGrantedAuthority("ROLE_USER"));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .accountLocked(Boolean.TRUE.equals(user.getIsBlocked()))
                .build();
    }
}