package com.researchmate.auth.service;

import com.researchmate.auth.dto.AuthResponse;
import com.researchmate.auth.dto.RegisterRequest;
import com.researchmate.exception.EmailAlreadyExistsException;
import com.researchmate.user.entity.User;
import com.researchmate.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
     private  final UserRepository userRepository;
     private final PasswordEncoder passwordEncoder;

     public AuthResponse register(RegisterRequest request){
         if(userRepository.existsByEmail(request.email())){
             throw  new EmailAlreadyExistsException("Email is already registered");
         }
         String hashedPassword=passwordEncoder.encode(request.password());
         User user= User.builder().email(request.email()).password(hashedPassword).name(request.name()).build();
         userRepository.save(user);
         return  new AuthResponse(user.getId(),
                 user.getName(),user.getEmail(),"Registration Succesfull");
     }
}
