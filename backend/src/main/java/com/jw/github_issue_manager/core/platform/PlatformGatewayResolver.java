package com.jw.github_issue_manager.core.platform;

public interface PlatformGatewayResolver {

    PlatformGateway getGateway(PlatformType platformType);
}
