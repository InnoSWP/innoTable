package org.innoswp.innotable.model.user;

import java.util.List;

public record User(String email, String password, List<Group> group, Role role) {
}
