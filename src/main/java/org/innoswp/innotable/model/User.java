package org.innoswp.innotable.model;

import java.util.List;

public record User(String email, String password, List<String> groups, String role) {
}
