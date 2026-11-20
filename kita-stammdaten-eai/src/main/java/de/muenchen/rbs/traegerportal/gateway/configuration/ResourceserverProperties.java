package de.muenchen.rbs.traegerportal.gateway.configuration;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties("resourceserver")
public class ResourceserverProperties {

    private final List<ClientAccessRules> clientAccessRules;

    public record ClientAccessRules(
            String pathPattern,
            String clientId) {
    }

}
