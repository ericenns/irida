package ca.corefacility.bioinformatics.irida.web.controller.api.graphql;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionInput;
import graphql.GraphQL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class GraphQLController {
	private static final Logger logger = LoggerFactory.getLogger(GraphQLController.class);

	@Autowired
	GraphQL graphql;

	@Autowired
	ObjectMapper objectMapper;

	@RequestMapping(value = "/api/graphql", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@CrossOrigin
	public Map<String, Object> graphqlGET(@RequestParam("query") String query,
			@RequestParam(value = "operationName", required = false) String operationName,
			@RequestParam(value = "variables", required = false) String variablesJson) throws IOException {
		logger.debug("Inside graphqlGET");
		if (query == null) {
			query = "";
		}

		Map<String, Object> variables = new LinkedHashMap<>();

		if (variablesJson != null) {
			variables = objectMapper.readValue(variablesJson, new TypeReference<Map<String, Object>>() {
			});
		}

		return executeGraphqlQuery(operationName, query, variables);
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/api/graphql", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@CrossOrigin
	public Map<String, Object> graphql(@RequestBody Map<String, Object> body) {
		logger.debug("Inside graphqlPOST");
		String query = (String) body.get("query");

		if (query == null) {
			query = "";
		}

		String operationName = (String) body.get("operationName");
		Map<String, Object> variables = (Map<String, Object>) body.get("variables");

		if (variables == null) {
			variables = new LinkedHashMap<>();
		}

		return executeGraphqlQuery(operationName, query, variables);
	}

	private Map<String, Object> executeGraphqlQuery(String operationName, String query, Map<String, Object> variables) {
		ExecutionInput executionInput = ExecutionInput.newExecutionInput()
				.query(query)
				.variables(variables)
				.operationName(operationName)
				.build();

		return graphql.execute(executionInput)
				.toSpecification();
	}
}
