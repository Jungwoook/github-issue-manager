package com.jw.github_issue_manager.core.platform;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class PlatformGatewayResolver {

    private final Map<PlatformType, PlatformGateway> gateways;

    public PlatformGatewayResolver(List<PlatformGateway> gateways) {
        this.gateways = new EnumMap<>(PlatformType.class);
        gateways.forEach(gateway -> this.gateways.put(gateway.getPlatformType(), gateway));
    }

    public PlatformGateway getGateway(PlatformType platformType) {
        PlatformGateway gateway = gateways.get(platformType);
        if (gateway == null) {
            throw new IllegalArgumentException("Unsupported platform: " + platformType);
        }
        return gateway;
    }
}
