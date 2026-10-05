package com.manager.client.model;

/** Display-only operator account. */
public record UserViewModel(long id, String name, String username, String role, String actions) {
    public Object[] toRow() {
        return new Object[]{id, name, username, role, actions};
    }
}
