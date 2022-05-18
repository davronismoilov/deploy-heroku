package uz.oliymahad.userservice.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.oliymahad.userservice.dto.UserRegisterDto;
import uz.oliymahad.userservice.dto.response.ApiResponse;
import uz.oliymahad.userservice.model.RoleEntity;
import uz.oliymahad.userservice.model.UserEntity;
import uz.oliymahad.userservice.model.enums.ERole;
import uz.oliymahad.userservice.repository.RoleRepository;
import uz.oliymahad.userservice.repository.UserRepository;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    public ApiResponse register(UserRegisterDto registerDto){

        boolean existsByPhoneNumber = userRepository.existsByPhoneNumber(registerDto.getPhoneNumber());

        if (existsByPhoneNumber){
            return new ApiResponse("This phoneNumber is already exist!",false, HttpStatus.BAD_REQUEST);
        }

        UserEntity user = new UserEntity();
        user.setPhoneNumber(registerDto.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        if(registerDto.getRoles() == null || registerDto.getRoles().size() == 0) {
            user.setRoles(Collections.singleton(roleRepository.findByRoleName(ERole.ROLE_USER)));
        }else{
            Set<RoleEntity> roles = new HashSet<>();
            registerDto.getRoles().forEach(role ->
                    roles.add(roleRepository.findByRoleName(ERole.valueOf(role))));
            user.setRoles(roles);
        }

        userRepository.save(user);

        return new ApiResponse("Successfully registered!", true, HttpStatus.OK);
    }
}
