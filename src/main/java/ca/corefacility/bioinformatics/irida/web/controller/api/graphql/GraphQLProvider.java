package ca.corefacility.bioinformatics.irida.web.controller.api.graphql;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.io.IOException;
import java.net.URL;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

@Component
public class GraphQLProvider {
	private GraphQL graphQL;
	private IridaWiring iridaWiring;

	@Autowired
	public GraphQLProvider(IridaWiring iridaWiring) {
		this.iridaWiring = iridaWiring;
	}

	@PostConstruct
	public void init() throws IOException {
		URL url = Resources.getResource("graphql/iridaSchemaAnnotated.graphqls");
		String sdl = Resources.toString(url, Charsets.UTF_8);
		GraphQLSchema graphQLSchema = buildSchema(sdl);

		this.graphQL = GraphQL.newGraphQL(graphQLSchema)
				.build();
	}

	private GraphQLSchema buildSchema(String sdl) {
		TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(sdl);
		RuntimeWiring runtimeWiring = buildWiring();
		SchemaGenerator schemaGenerator = new SchemaGenerator();
		return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
	}

	private RuntimeWiring buildWiring() {
		return RuntimeWiring.newRuntimeWiring()
				.type(newTypeWiring("Query").dataFetcher("project", iridaWiring.projectDataFetcher))
				.type(newTypeWiring("Project").dataFetcher("samples", iridaWiring.projectSamplesDataFetcher))
				.type(newTypeWiring("Query").dataFetcher("projects", iridaWiring.projectsDataFetcher))
				.type(newTypeWiring("Query").dataFetcher("sample", iridaWiring.sampleDataFetcher))
				.type(newTypeWiring("Sample").dataFetcher("projects", iridaWiring.sampleProjectsDataFetcher))
				.build();
	}

	@Bean
	public GraphQL graphQL() {
		return graphQL;
	}
}
