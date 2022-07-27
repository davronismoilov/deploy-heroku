package uz.oliymahad.userservice.controller.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import uz.oliymahad.userservice.dto.request.UserLoginRequest;
import uz.oliymahad.userservice.dto.request.UserRegisterRequest;
import uz.oliymahad.userservice.exception.custom_ex_model.UserAlreadyRegisteredException;
import uz.oliymahad.userservice.exception.custom_ex_model.UserAuthenticationException;
import uz.oliymahad.userservice.model.entity.RoleEntity;
import uz.oliymahad.userservice.model.entity.UserEntity;
import uz.oliymahad.userservice.model.entity.UserRegisterDetails;
import uz.oliymahad.userservice.model.entity.course.CourseEntity;
import uz.oliymahad.userservice.model.entity.queue.QueueEntity;
import uz.oliymahad.userservice.model.enums.EGender;
import uz.oliymahad.userservice.model.enums.ERole;
import uz.oliymahad.userservice.model.enums.Status;
import uz.oliymahad.userservice.repository.QueueRepository;
import uz.oliymahad.userservice.repository.UserDetailRepository;
import uz.oliymahad.userservice.repository.UserRepository;
import uz.oliymahad.userservice.security.jwt.UserDetailsServiceImpl;
import uz.oliymahad.userservice.security.jwt.payload.response.JWTokenResponse;
import uz.oliymahad.userservice.service.oauth0.CustomOAuth0UserService;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/auth")
@PreAuthorize(value = "permitAll()")
public class AuthController {

    private final CustomOAuth0UserService oAuth0UserService;
    private final UserRepository userRepository;
    private final UserDetailRepository userDetailRepository;
    private final QueueRepository queueRepository;

    @GetMapping("/login/google")
    public ResponseEntity<?> loginWithGoogle(HttpServletResponse response) {
        try {
            response.sendRedirect("/oauth2/authorization/google");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(OK.name());
    }


    @GetMapping("/login/facebook")
    public ResponseEntity<?> loginWithFacebook(HttpServletResponse response) {
        try {
            response.sendRedirect("/oauth2/authorization/facebook");
//            response.sendRedirect("
//            /oauth2/callback/facebook");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok("success");
    }


    @GetMapping("/success")
    public ResponseEntity<?> signInSuccess(HttpServletResponse response) {
        String accessToken = response.getHeader("access_token");
        String refreshToken = response.getHeader("refresh_token");
        return ResponseEntity.ok(
                new JWTokenResponse(OK.value(), OK.name(), accessToken, refreshToken)

        );
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @RequestBody @Valid UserRegisterRequest userRegisterRequest
    ) throws UserAlreadyRegisteredException, MethodArgumentNotValidException {

        return ResponseEntity.ok(oAuth0UserService.registerUser(userRegisterRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(
            @RequestBody @Valid UserLoginRequest userLoginRequest
    ) throws UserAuthenticationException {
        return ResponseEntity.ok(oAuth0UserService.loginUser(userLoginRequest));
    }

    @GetMapping("/getMe")
    public ResponseEntity<?> getMe() {
        return ResponseEntity.ok(oAuth0UserService.getUser());
    }


    @PostMapping("/akdjndn1ad?dand/RE_dqkqekb?FR")
    public ResponseEntity<?> tokenRefresher(
            @RequestBody String jwtRefreshToken
    ) {
        return ResponseEntity.ok(oAuth0UserService.validateRefreshToken(jwtRefreshToken));
    }


    @GetMapping("/fake")
    public void fake() {

        CourseEntity courseEntity = new CourseEntity(1L, "MATH", "ooo", 10, 10);
        for (int i = 0; i < 1_000; i++) {
            UserEntity userEntity = new UserEntity();
            userEntity.setRoles(Set.of(new RoleEntity(2,ERole.ROLE_ADMIN)));
            userEntity.setUsername(generator(6));
            userEntity.setPassword("root123");
            UserEntity savedUserEntity = userRepository.save(userEntity);

            UserRegisterDetails userRegisterDetails = new UserRegisterDetails();
            userRegisterDetails.setUser(savedUserEntity);
            userRegisterDetails.setFirstName(generator(5));
            userRegisterDetails.setLastName(generator(5));
            if (i % 2 == 0) {
                userRegisterDetails.setGender(EGender.FEMALE);
            } else {
                userRegisterDetails.setGender(EGender.MALE);
            }
            UserRegisterDetails save = userDetailRepository.save(userRegisterDetails);
            userEntity.setUserRegisterDetails(save);
            userRepository.save(userEntity);


            QueueEntity queueEntity = new QueueEntity();
            queueEntity.setUser(userEntity);
            queueEntity.setStatus(Status.PENDING);
            queueEntity.setAppliedDate(LocalDateTime.now());
            queueEntity.setCourse(courseEntity);
            queueRepository.save(queueEntity);


        }

    }

    private String generator(int range){
        String s = UUID.randomUUID().toString();
        s.substring(0,range).replace("_","");
        return s;
    }
}
