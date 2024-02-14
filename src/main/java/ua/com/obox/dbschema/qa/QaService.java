package ua.com.obox.dbschema.qa;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.com.obox.authserver.confirmation.Confirm;
import ua.com.obox.authserver.confirmation.ConfirmRepository;
import ua.com.obox.authserver.token.Token;
import ua.com.obox.authserver.token.TokenRepository;
import ua.com.obox.authserver.user.User;
import ua.com.obox.authserver.user.UserRepository;
import ua.com.obox.dbschema.tenant.TenantRepository;
import ua.com.obox.dbschema.tools.exception.ExceptionTools;
import ua.com.obox.security.bucket4j.RateLimitingAspect;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QaService {
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final ConfirmRepository confirmRepository;
    private final TenantRepository tenantRepository;


    public void deleteUserByEmail(String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> ExceptionTools.notFoundException(".userNotFoundEmail", "en-US", userEmail));

        List<Token> tokenList = tokenRepository.findAllValidTokenByUserId(user.getUserId());
        if (!tokenList.isEmpty()) {
            tokenRepository.deleteAll(tokenList);
        }

        List<Confirm> confirmList = confirmRepository.findAllByEmail(userEmail);
        if (!confirmList.isEmpty()) {
            confirmRepository.deleteAll(confirmList);
        }

        tenantRepository.delete(user.getTenant());
        userRepository.delete(user);
    }

    public void unblockByUserIp(String userIp) {
        RateLimitingAspect.blackList.remove(userIp);
    }
}
