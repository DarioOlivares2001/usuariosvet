package com.usuariosveterinaria.resolver;

import com.usuariosveterinaria.model.Usuario;
import com.usuariosveterinaria.OracleDBConnection;

import java.sql.*;
import java.util.*;

public class Query {
    public List<Usuario> usuarios() {
        List<Usuario> lista = new ArrayList<>();
        try (Connection conn = OracleDBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, nombre, email, rol_id FROM usuarios");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getInt("id"));
                u.setNombre(rs.getString("nombre"));
                u.setEmail(rs.getString("email"));
                u.setRol_id(rs.getInt("rol_id"));
                lista.add(u);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}
