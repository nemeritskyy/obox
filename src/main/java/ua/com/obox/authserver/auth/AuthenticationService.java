package ua.com.obox.authserver.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import ua.com.obox.authserver.config.JwtService;
import ua.com.obox.authserver.confirmation.Confirm;
import ua.com.obox.authserver.confirmation.ConfirmRepository;
import ua.com.obox.authserver.mail.EmailService;
import ua.com.obox.authserver.token.Token;
import ua.com.obox.authserver.token.TokenRepository;
import ua.com.obox.authserver.token.TokenType;
import ua.com.obox.authserver.user.Role;
import ua.com.obox.authserver.user.User;
import ua.com.obox.authserver.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.tenant.*;
import ua.com.obox.dbschema.tools.State;
import ua.com.obox.dbschema.tools.Validator;
import ua.com.obox.dbschema.tools.response.BadFieldsResponse;
import ua.com.obox.dbschema.tools.response.ResponseErrorMap;
import ua.com.obox.dbschema.tools.services.UpdateServiceHelper;
import ua.com.obox.dbschema.tools.translation.CheckHeader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository repository;
    private final TokenRepository tokenRepository;
    private final ConfirmRepository confirmRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private static final ResourceBundle translation = ResourceBundle.getBundle("translation.messages");

    private final EmailService emailService;
    private final TenantService tenantService;
    private final TenantRepository tenantRepository;
    private final UpdateServiceHelper serviceHelper;

    public StatusResponse register(User request, String acceptLanguage) throws JsonProcessingException {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        Map<String, String> fieldErrors = new ResponseErrorMap<>();
        Optional<User> userExist = repository.findByEmail(request.getEmail());

        {
            fieldErrors.put("language", Validator.validateLanguage(request.getLanguage(), finalAcceptLanguage));
            fieldErrors.put("name", serviceHelper.updateNameField(request::setName, request.getName(), finalAcceptLanguage));
            fieldErrors.put("email", serviceHelper.checkExistUser(userExist, finalAcceptLanguage));
            fieldErrors.put("email", serviceHelper.checkEmail(request.getEmail(), finalAcceptLanguage));
            fieldErrors.put("email", serviceHelper.checkActivate(userExist, finalAcceptLanguage));
            fieldErrors.put("password", serviceHelper.checkPassword(request.getPassword(), finalAcceptLanguage));
        }

        if (fieldErrors.size() > 0) {
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);
        }

        if (userExist.isEmpty()) {
            TenantResponseId responseId = tenantService.createTenant(
                    Tenant.builder()
                            .name(request.getName())
                            .language(request.getLanguage())
                            .build(),
                    finalAcceptLanguage
            );

            Tenant tenantCreated = tenantRepository.findByTenantId(responseId.getTenantId())
                    .orElseThrow(() -> new RuntimeException("Tenant not found"));

            User user = User.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .state(State.DISABLED)
                    .role(Role.USER)
                    .tenant(tenantCreated)
                    .createdAt(Instant.now().getEpochSecond())
                    .updatedAt(Instant.now().getEpochSecond())
                    .build();

            repository.save(user);
            tenantCreated.setUser(user);

//            Tenant tenant = Tenant.builder()
//                    .name(request.getName())
//                    .language(request.getLanguage())
//                    .build();
//
//            TenantResponseId responseId = tenantService.createTenant(tenant, finalAcceptLanguage);
//
//
//            Optional<Tenant> tenantGet = tenantRepository.findByTenantId(responseId.getTenantId());
//            Tenant tenantCreated = null;
//            if (tenantGet.isPresent()) {
//                tenantCreated = tenantGet.get();
//            }
//
//            var user = User.builder()
//                    .email(request.getEmail())
//                    .password(passwordEncoder.encode(request.getPassword()))
//                    .state(State.DISABLED)
//                    .role(Role.USER)
//                    .tenant(tenantCreated)
//                    .createdAt(Instant.now().getEpochSecond())
//                    .updatedAt(Instant.now().getEpochSecond())
//                    .build();
//
//
//            repository.save(user);
//            tenantCreated.setUser(user);

            if (!user.isEnabled()) {
                var confirmToken = RandomStringUtils.random(20, true, true);
                var confirm = Confirm.builder().confirmationKey(confirmToken).email(request.getEmail()).build();
                confirmRepository.save(confirm);
                emailService.sendEmailConfirmation(request.getEmail(), confirmToken);
            }


//            var savedUser = repository.save(user);
//            var jwtToken = jwtService.generateToken(user);
//            var refreshToken = jwtService.generateRefreshToken(user);
//            if (user.isEnabled()) saveUserToken(savedUser, jwtToken);
            return StatusResponse.builder()
//                    .emailConfirmation("need to email confirm")
//                    .accessToken(jwtToken)
//                    .refreshToken(refreshToken)
                    .build();
        } else {
            Optional<Confirm> alreadyExist = confirmRepository.findByEmail(request.getEmail());
            alreadyExist.ifPresent(confirmToken -> emailService.sendEmailConfirmation(confirmToken.getEmail(), confirmToken.getConfirmationKey()));
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
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getUserId());
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

    public AuthenticationResponse confirmation(String token, String acceptLanguage) {
        String errorLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        Optional<Confirm> confirm = confirmRepository.findByConfirmationKey(token);
        if (confirm.isPresent()) {
            var user = repository.findByEmail(confirm.get().getEmail())
                    .orElseThrow();
            user.setState(State.ENABLED);
            repository.save(user);
            confirmRepository.delete(confirm.get());

            var jwtToken = jwtService.generateToken(user);
            var refreshToken = jwtService.generateRefreshToken(user);
            if (user.isEnabled()) saveUserToken(user, jwtToken);

            return AuthenticationResponse.builder()
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken)
                    .build();
        } else {
            Map<String, String> fieldErrors = new ResponseErrorMap<>();
            fieldErrors.put("confirmation", translation.getString(errorLanguage + ".expiredLink"));
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);
        }
    }
}
