package ca.corefacility.bioinformatics.irida.web.controller.api.graphql;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import graphql.GraphQL;
import graphql.execution.instrumentation.ChainedInstrumentation;
import graphql.execution.instrumentation.Instrumentation;
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation;
import graphql.execution.instrumentation.tracing.TracingInstrumentation;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.xml.crypto.Data;

import java.io.IOException;
import java.net.URL;

import static graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentationOptions.newOptions;
import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;
import static java.util.Arrays.asList;

@Component
public class GraphQLProvider {
	private GraphQL graphQL;
	private DataLoaderRegistry dataLoaderRegistry;
	private IridaWiring iridaWiring;

	@Autowired
	public GraphQLProvider(DataLoaderRegistry dataLoaderRegistry, IridaWiring iridaWiring) {
		this.dataLoaderRegistry = dataLoaderRegistry;
		this.iridaWiring = iridaWiring;
	}

	@PostConstruct
	public void init() throws IOException {
		URL url = Resources.getResource("graphql/iridaSchemaAnnotated.graphqls");
		String sdl = Resources.toString(url, Charsets.UTF_8);
		GraphQLSchema graphQLSchema = buildSchema(sdl);

		DataLoaderDispatcherInstrumentation dlInstrumentation =
				new DataLoaderDispatcherInstrumentation(newOptions().includeStatistics(true));

		Instrumentation instrumentation = new ChainedInstrumentation(
				asList(new TracingInstrumentation(), dlInstrumentation)
		);

		this.graphQL = GraphQL.newGraphQL(graphQLSchema).instrumentation(instrumentation)
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
				.type(newTypeWiring("Sample").dataFetcher("sequencingFiles", iridaWiring.sampleSequencingFilesDataFetcher))
				.type(newTypeWiring("Query").dataFetcher("sequencingRun", iridaWiring.sequencingRunDataFetcher))
				.type(newTypeWiring("Query").dataFetcher("sequencingRuns", iridaWiring.sequencingRunsDataFetcher))
				.type(newTypeWiring("SequencingRun").dataFetcher("sequencingFiles", iridaWiring.sequencingRunSequencingFilesDataFetcher))
				.type(newTypeWiring("Mutation").dataFetcher("createProject", iridaWiring.createProjectMutationDataFetcher))
				.type(newTypeWiring("Mutation").dataFetcher("createSample", iridaWiring.createSampleMutationDataFetcher))
				.build();
	}

	@Bean
	public GraphQL graphQL() {
		return graphQL;
	}
}
