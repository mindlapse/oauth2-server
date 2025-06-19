package com.projectsea.oauth2.model;

import java.util.List;

public record Client(
    String clientDescription,
    String clientId,
    String clientSecret,
    List<String> scopes,
    List<String> grantTypes,
    List<String> redirectUris
) {}
