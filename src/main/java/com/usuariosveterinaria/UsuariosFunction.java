package com.usuariosveterinaria;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuariosFunction {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @FunctionName("Usuarios")
    public void run(
        @EventGridTrigger(name = "event") String event,
        final ExecutionContext context) {

        context.getLogger().info("Evento recibido desde Event Grid.");

        if (event == null || event.isEmpty()) {
            context.getLogger().severe("Evento vacío recibido.");
            return;
        }

        try (Connection conn = OracleDBConnection.getConnection()) {
            context.getLogger().info("Conexión a la base de datos establecida correctamente.");

            JsonNode eventNode = objectMapper.readTree(event);
            String eventType = eventNode.get("eventType").asText();
            JsonNode usuarioNode = eventNode.get("data");

            context.getLogger().info("Tipo de evento recibido: " + eventType);

            switch (eventType) {
                case "UsuarioCreado":
                    handleCreateUsuario(usuarioNode, conn, context);
                    break;
                case "UsuarioActualizado":
                    handleUpdateUsuario(usuarioNode, conn, context);
                    break;
                case "UsuarioEliminado":
                    handleDeleteUsuario(usuarioNode, conn, context);
                    break;
                default:
                    context.getLogger().warning("Evento no soportado: " + eventType);
                    break;
            }
        } catch (Exception e) {
            context.getLogger().severe("Error procesando evento: " + e.getMessage());
        }
    }

    private void handleCreateUsuario(JsonNode usuarioNode, Connection conn, ExecutionContext context) throws SQLException {
        if (usuarioNode == null || usuarioNode.isEmpty()) {
            context.getLogger().severe("Datos de usuario vacíos en creación.");
            return;
        }

        String nombre = usuarioNode.get("nombre").asText("");
        String email = usuarioNode.get("email").asText("");
        int rol_id = usuarioNode.has("rol_id") ? usuarioNode.get("rol_id").asInt() : 0;

        if (nombre.isEmpty() || email.isEmpty()) {
            context.getLogger().severe("Faltan campos requeridos para crear usuario.");
            return;
        }

        // Buscar rol por defecto si no se entrega rol_id
        if (rol_id == 0) {
            String queryRol = "SELECT id FROM roles WHERE nombre = 'Usuario' FETCH FIRST 1 ROWS ONLY";
            try (PreparedStatement stmt = conn.prepareStatement(queryRol)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    rol_id = rs.getInt("id");
                    context.getLogger().info("Rol por defecto asignado con ID: " + rol_id);
                } else {
                    context.getLogger().severe("No se encontró el rol por defecto.");
                    return;
                }
            }
        }

        String query = "INSERT INTO usuarios (nombre, email, rol_id) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, nombre);
            stmt.setString(2, email);
            stmt.setInt(3, rol_id);
            stmt.executeUpdate();
            context.getLogger().info("Usuario creado exitosamente.");
        }
    }

    private void handleUpdateUsuario(JsonNode usuarioNode, Connection conn, ExecutionContext context) throws SQLException {
        int id = usuarioNode.has("id") ? usuarioNode.get("id").asInt() : 0;
        String nombre = usuarioNode.get("nombre").asText("");
        String email = usuarioNode.get("email").asText("");
        int rol_id = usuarioNode.has("rol_id") ? usuarioNode.get("rol_id").asInt() : 0;

        if (id == 0 || nombre.isEmpty() || email.isEmpty() || rol_id == 0) {
            context.getLogger().severe("Faltan campos requeridos para actualizar usuario.");
            return;
        }

        String query = "UPDATE usuarios SET nombre = ?, email = ?, rol_id = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, nombre);
            stmt.setString(2, email);
            stmt.setInt(3, rol_id);
            stmt.setInt(4, id);
            int updated = stmt.executeUpdate();
            if (updated == 0) {
                context.getLogger().warning("Usuario no encontrado para actualizar.");
            } else {
                context.getLogger().info("Usuario actualizado exitosamente.");
            }
        }
    }

    private void handleDeleteUsuario(JsonNode usuarioNode, Connection conn, ExecutionContext context) throws SQLException {
        int id = usuarioNode.has("id") ? usuarioNode.get("id").asInt() : 0;

        if (id == 0) {
            context.getLogger().severe("Falta el campo 'id' para eliminar usuario.");
            return;
        }

        String query = "DELETE FROM usuarios WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            int deleted = stmt.executeUpdate();
            if (deleted == 0) {
                context.getLogger().warning("Usuario no encontrado para eliminar.");
            } else {
                context.getLogger().info("Usuario eliminado exitosamente.");
            }
        }
    }
}
