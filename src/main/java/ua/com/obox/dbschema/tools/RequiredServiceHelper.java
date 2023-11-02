package ua.com.obox.dbschema.tools;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.tenant.Tenant;
import ua.com.obox.dbschema.tools.logging.LoggingService;
import ua.com.obox.dbschema.tools.services.UpdateServiceHelper;

@Service
public class RequiredServiceHelper {
    @Autowired
    LoggingService loggingService;
    @Autowired
    UpdateServiceHelper serviceHelper;

    public String updateNameIfNeeded(String name, Tenant tenant, String acceptLanguage) {
        if (name != null) {
            return serviceHelper.updateNameField(tenant::setName, name, acceptLanguage);
        }
        return null;
    }
}
