package com.auditing.audit.model;

public enum Role {
    ADMIN("ROLE_ADMIN"),
    KEPALASPI("ROLE_KEPALASPI"),
    SEKRETARIS("ROLE_SEKRETARIS"),
    KARYAWAN("ROLE_KARYAWAN"),;

    private final String authority;

    Role(String authority) {
        this.authority = authority;
    }

    public String getAuthority() {
        return authority;
    }
}