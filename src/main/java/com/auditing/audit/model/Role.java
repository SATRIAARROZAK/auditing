package com.auditing.audit.model;

public enum Role {
    admin("ROLE_Admin"),
    kepalaspi("ROLE_KepalaSPI"),
    sekretaris("ROLE_Sekretaris"),
    pegawai("ROLE_Pegawai"),;

    private final String authority;

    Role(String authority) {
        this.authority = authority;
    }

    public String getAuthority() {
        return authority;
    }
}