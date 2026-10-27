package com.thoonidev.InventoryMgtSystem.services.impl;

import com.thoonidev.InventoryMgtSystem.dtos.LoginRequest;
import com.thoonidev.InventoryMgtSystem.dtos.RegisterRequest;
import com.thoonidev.InventoryMgtSystem.dtos.Response;
import com.thoonidev.InventoryMgtSystem.dtos.UserDTO;
import com.thoonidev.InventoryMgtSystem.enums.UserRole;
import com.thoonidev.InventoryMgtSystem.exceptions.InvalidCredentialsException;
import com.thoonidev.InventoryMgtSystem.exceptions.NotFoundException;
import com.thoonidev.InventoryMgtSystem.models.User;
import com.thoonidev.InventoryMgtSystem.repositories.UserRepository;
import com.thoonidev.InventoryMgtSystem.security.JwtUtils;
import com.thoonidev.InventoryMgtSystem.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final JwtUtils jwtUtils;
    private final MessageSource messageSource;

    @Override
    public Response registerUser(RegisterRequest registerRequest) {

        UserRole role = UserRole.MANAGER;
        if (registerRequest.getRole() != null) {
            role = registerRequest.getRole();
        }

        User userToSave = User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .phoneNumber(registerRequest.getPhoneNumber())
                .role(role)
                .build();

        userRepository.save(userToSave);

        String message = messageSource.getMessage("user.register.success", null, LocaleContextHolder.getLocale());

        return Response.builder()
                .status(200)
                .message(message)
                .build();
    }

    @Override
    public Response loginUser(LoginRequest loginRequest) {

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new NotFoundException(
                        messageSource.getMessage("user.email.not_found", null, LocaleContextHolder.getLocale())));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException(
                    messageSource.getMessage("user.password.mismatch", null, LocaleContextHolder.getLocale()));
        }
        String token = jwtUtils.generateToken(user.getEmail());

        String message = messageSource.getMessage("user.login.success", null, LocaleContextHolder.getLocale());

        return Response.builder()
                .status(200)
                .message(message)
                .role(user.getRole())
                .token(token)
                .expirationTime("6 months")
                .build();
    }

    @Override
    public Response getAllUsers() {

        List<User> users = userRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        users.forEach(user -> user.setTransactions(null));

        List<UserDTO> userDTOS = modelMapper.map(users, new TypeToken<List<UserDTO>>() {
        }.getType());

        String message = messageSource.getMessage("user.list.success", null, LocaleContextHolder.getLocale());

        return Response.builder()
                .status(200)
                .message(message)
                .users(userDTOS)
                .build();
    }

    @Override
    public User getCurrentLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(
                        messageSource.getMessage("user.not_found", null, LocaleContextHolder.getLocale())));
        user.setTransactions(null);

        return user;
    }

    @Override
    public Response getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        messageSource.getMessage("user.not_found", null, LocaleContextHolder.getLocale())));

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);
        userDTO.setTransactions(null);

        String message = messageSource.getMessage("user.detail.success", null, LocaleContextHolder.getLocale());

        return Response.builder()
                .status(200)
                .message(message)
                .user(userDTO)
                .build();
    }

    @Override
    public Response updateUser(Long id, UserDTO userDTO) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        messageSource.getMessage("user.not_found", null, LocaleContextHolder.getLocale())));

        if (userDTO.getEmail() != null) existingUser.setEmail(userDTO.getEmail());
        if (userDTO.getPhoneNumber() != null) existingUser.setPhoneNumber(userDTO.getPhoneNumber());
        if (userDTO.getName() != null) existingUser.setName(userDTO.getName());
        if (userDTO.getRole() != null) existingUser.setRole(userDTO.getRole());

        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        userRepository.save(existingUser);

        String message = messageSource.getMessage("user.update.success", null, LocaleContextHolder.getLocale());

        return Response.builder()
                .status(200)
                .message(message)
                .build();
    }

    @Override
    public Response deleteUser(Long id) {
        userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        messageSource.getMessage("user.not_found", null, LocaleContextHolder.getLocale())));

        userRepository.deleteById(id);

        String message = messageSource.getMessage("user.delete.success", null, LocaleContextHolder.getLocale());

        return Response.builder()
                .status(200)
                .message(message)
                .build();
    }

    @Override
    public Response getUserTransactions(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        messageSource.getMessage("user.not_found", null, LocaleContextHolder.getLocale())));

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        userDTO.getTransactions().forEach(transactionDTO -> {
            transactionDTO.setUser(null);
            transactionDTO.setSupplier(null);
        });

        String message = messageSource.getMessage("user.transactions.success", null, LocaleContextHolder.getLocale());

        return Response.builder()
                .status(200)
                .message(message)
                .user(userDTO)
                .build();
    }
}
