package com.br.techbuilderun.controller;

import com.br.techbuilderun.dto.CreateUserRequestDto;
import com.br.techbuilderun.dto.CreateUserResponseDto;
import com.br.techbuilderun.entity.Role;
import com.br.techbuilderun.entity.User;
import com.br.techbuilderun.repository.RoleRepository;
import com.br.techbuilderun.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    @PostMapping("/users")
    @Transactional
    public ResponseEntity<CreateUserResponseDto> createUser(@RequestBody CreateUserRequestDto createUserRequestDto) {
        var basicRole = roleRepository.findByName(Role.Values.BASIC.name());

        var userFromDb = userRepository.findByUsername(createUserRequestDto.username());
        if (userFromDb.isPresent()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "User already exists");
        }

        var user = new User();
        user.setUsername(createUserRequestDto.username());
        user.setPassword(passwordEncoder.encode(createUserRequestDto.password()));
        user.setRoles(Set.of(basicRole));
        userRepository.save(user);
        return ResponseEntity.ok(new CreateUserResponseDto(user.getUsername(), user.getPassword()));
    }

    @GetMapping("/users/list")
    @PreAuthorize("hasAuthority('SCOPE_admin')")
    public ResponseEntity<List<User>> getAllUsers() {
        var users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

}
