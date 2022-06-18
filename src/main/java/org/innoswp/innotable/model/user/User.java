package org.innoswp.innotable.model.user;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public abstract class User {
    private String email;
    private String password;
    private final Group group;
    private final Role role;
}
