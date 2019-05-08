package ca.corefacility.bioinformatics.irida.web.controller.api.graphql;

import com.google.common.collect.ImmutableMap;
import graphql.schema.DataFetcher;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class GraphQLDataFetchers {
	private static List<Map<String, String>> projects = Arrays.asList(
			ImmutableMap.of("id", "1", "name", "proj-1", "organism", "Ecoli"),
			ImmutableMap.of("id", "2", "name", "proj-2", "organism", "Salmonella"));

	public DataFetcher projectDataFetcher() {
		return dataFetchingEnvironment -> {
			String projectId = dataFetchingEnvironment.getArgument("id");
			return projects.stream()
					.filter(project -> project.get("id")
							.equals(projectId))
					.findFirst()
					.orElse(null);
		};
	}
}
