package be.wts.lottosysteem_Ali.controller;

import be.wts.lottosysteem_Ali.dto.InlogRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class LoginController {
    private final AuthenticationManager authManager;

    public LoginController(AuthenticationManager authManager) {
        this.authManager = authManager;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody InlogRequest request, HttpServletRequest httpRequest) {
        try{
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(request.gebruikersnaam(), request.wachtwoord());

            Authentication auth = authManager.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(auth);
            httpRequest.getSession();
            return ResponseEntity.ok().build();
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
