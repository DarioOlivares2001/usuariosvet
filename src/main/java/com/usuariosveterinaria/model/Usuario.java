package com.usuariosveterinaria.model;

public class Usuario {
    private int id;
    private String nombre;
    private String email;
    private int rol_id;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getRol_id() { return rol_id; }
    public void setRol_id(int rol_id) { this.rol_id = rol_id; }
}
