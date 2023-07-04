package ua.com.obox.authserver.auth;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import ua.com.obox.authserver.config.JwtService;
import ua.com.obox.authserver.confirmation.Confirm;
import ua.com.obox.authserver.confirmation.ConfirmRepository;
import ua.com.obox.authserver.mail.EmailService;
import ua.com.obox.authserver.token.Token;
import ua.com.obox.authserver.token.TokenRepository;
import ua.com.obox.authserver.token.TokenType;
import ua.com.obox.authserver.user.User;
import ua.com.obox.authserver.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository repository;
    private final TokenRepository tokenRepository;
    private final ConfirmRepository confirmRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private static final Logger logger = LogManager.getLogger(AuthenticationService.class);
    @Autowired
    private EmailService emailService;

    public StatusResponse register(RegisterRequest request) throws MessagingException, IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        if (this.repository.findByEmail(request.getEmail()).isEmpty()) { // if user not created
            var user = User.builder()
                    .firstname(request.getFirstname())
                    .lastname(request.getLastname())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .enabled(request.getFirstname().equals("Admin") ? true : false) // for admin account enabled (only for testing)
                    .role(request.getRole())
                    .build();
            repository.save(user);
            System.out.println(request.getEmail());
            if (!request.getFirstname().equals("Admin")) {
                var confirmToken = RandomStringUtils.random(20, true, true);
                var confirm = Confirm.builder().confirmationKey(confirmToken).email(request.getEmail()).build();
                confirmRepository.save(confirm);
                emailService.sendSimpleMail(request.getEmail(), confirmToken);
            }

//            var savedUser = repository.save(user);
//            var jwtToken = jwtService.generateToken(user);
//            var refreshToken = jwtService.generateRefreshToken(user);
//            if (user.isEnabled()) saveUserToken(savedUser, jwtToken);
            return StatusResponse.builder()
                    .emailConfirmation("need to email confirm")
//                    .accessToken(jwtToken)
//                    .refreshToken(refreshToken)
                    .build();
        } else {
            logger.error("user already exist");
        }
        return StatusResponse.builder().build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = repository.findByEmail(request.getEmail())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            var user = this.repository.findByEmail(userEmail)
                    .orElseThrow();
            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);
                var authResponse = AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }

    public StatusResponse confirmation(String token) {
        String status = "need to email confirm";
        Optional<Confirm> confirm = confirmRepository.findByConfirmationKey(token);
        if (confirm.isPresent()) {
            System.out.println("find confirmation token");
            var user = repository.findByEmail(confirm.get().getEmail())
                    .orElseThrow();
            user.setEnabled(true);
            status = "confirm success";
            repository.save(user);
            confirmRepository.delete(confirm.get());
        }
        return StatusResponse.builder().emailConfirmation(status).build();
    }
}
