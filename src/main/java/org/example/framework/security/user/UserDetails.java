package org.example.framework.security.user;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class UserDetails {
    private String name;
    private List<Authority> authorities;

    public UserDetails() {
        this.authorities = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Authority> getAuthorities() {
        return authorities;
    }

    @Override
    public String toString() {
        return "UserDetails{" +
                "name='" + name + '\'' +
                ", authorities={" + authorities.stream().map(Authority::getName).collect(Collectors.joining(", ")) + '}';
    }
}