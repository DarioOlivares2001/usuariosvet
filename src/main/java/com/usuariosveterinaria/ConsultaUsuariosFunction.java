package com.usuariosveterinaria;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import com.usuariosveterinaria.config.GraphQLProvider;
import graphql.ExecutionResult;
import graphql.GraphQL;
import java.util.Optional;

/**
 * Azure Function - Consulta de usuarios vía GraphQL
 */
public class ConsultaUsuariosFunction {

    @FunctionName("ConsultaUsuarios")
    public HttpResponseMessage run(
        @HttpTrigger(
            name = "req",
            methods = {HttpMethod.POST},
            authLevel = AuthorizationLevel.ANONYMOUS
        )
        HttpRequestMessage<Optional<String>> request,
        final ExecutionContext context) {

        context.getLogger().info("Procesando solicitud GraphQL para consultar usuarios.");

        try {
            String body = request.getBody().orElse("");
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode json = (ObjectNode) mapper.readTree(body);

            String query = json.get("query").asText();
            GraphQL graphQL = GraphQLProvider.buildGraphQL();
            ExecutionResult result = graphQL.execute(query);

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(mapper.writeValueAsString(result.toSpecification()))
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Error procesando GraphQL: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }
}
