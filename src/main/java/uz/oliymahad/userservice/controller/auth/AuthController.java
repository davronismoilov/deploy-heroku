package uz.oliymahad.userservice.controller.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.oliymahad.userservice.dto.request.UserRegisterRequest;
import uz.oliymahad.userservice.exception.custom_ex_model.UserAlreadyRegisteredException;
import uz.oliymahad.userservice.security.jwt.payload.response.JWTokenResponse;
import uz.oliymahad.userservice.service.oauth0.CustomOAuth0UserService;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final CustomOAuth0UserService oAuth0UserService;
    @GetMapping("/login/google")
    public ResponseEntity<?> loginWithGoogle(HttpServletResponse response){
        try {
            response.sendRedirect("/oauth2/authorization/google");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok("success");
    }


    @GetMapping("/login/facebook")
    public ResponseEntity<?> loginWithFacebook(HttpServletResponse response){
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
//        response.setHeader("access_token", null);
//        response.setHeader("refresh_token", null);
        return ResponseEntity.ok(
                new JWTokenResponse(0, accessToken, refreshToken)
        );
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @RequestBody @Valid UserRegisterRequest userRegisterRequest
    ) throws UserAlreadyRegisteredException {
        return ResponseEntity.ok(oAuth0UserService.registerUser(userRegisterRequest));
    }

    @PostMapping("/akdjndn1ad?dand/RE_dqkqekb?FR")
    public ResponseEntity<?> tokenRefresher(
            @RequestBody String jwtRefreshToken
    ){
        return ResponseEntity.ok(oAuth0UserService.validateRefreshToken(jwtRefreshToken));
    }

}
