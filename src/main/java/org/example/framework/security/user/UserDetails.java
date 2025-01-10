package org.example.framework.security.user;

import java.util.*;
import java.util.stream.Collectors;

public class UserDetails {
    private String name;
    private Set<Authority> authorities;

    public UserDetails() {
        this.authorities = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Authority> getAuthorities() {
        return authorities;
    }

    @Override
    public String toString() {
        return "UserDetails{" +
                "name='" + name + '\'' +
                ", authorities={" + authorities.stream().map(Authority::getName).collect(Collectors.joining(", ")) + '}';
    }
}