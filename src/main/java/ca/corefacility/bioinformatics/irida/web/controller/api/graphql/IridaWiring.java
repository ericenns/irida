package ca.corefacility.bioinformatics.irida.web.controller.api.graphql;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.corefacility.bioinformatics.irida.model.joins.Join;
import ca.corefacility.bioinformatics.irida.model.project.Project;
import ca.corefacility.bioinformatics.irida.model.sample.Sample;
import ca.corefacility.bioinformatics.irida.service.ProjectService;
import ca.corefacility.bioinformatics.irida.service.sample.SampleService;

import graphql.schema.DataFetcher;

/**
 * Wiring for GraphQL requests
 */
@Component
public class IridaWiring {

	/**
	 * Reference to {@link ProjectService}.
	 */
	private ProjectService projectService;
	/**
	 * Reference to {@link SampleService}.
	 */
	private SampleService sampleService;

	/**
	 * Constructor for {@link IridaWiring}, requires a reference to a {@link ProjectService}
	 * and a {@link SampleService}
	 *
	 * @param projectService
	 * 				the {@link ProjectService} to be used by this wiring.
	 * @param sampleService
	 * 				the {@link SampleService} to be used by this wiring.
	 */
	@Autowired
	public IridaWiring(ProjectService projectService, SampleService sampleService) {
		this.projectService = projectService;
		this.sampleService = sampleService;
	}

	DataFetcher projectDataFetcher = environment -> {
		String id = environment.getArgument("id");

		return projectService.read(Long.valueOf(id));
	};

	DataFetcher projectSamplesDataFetcher = environment -> {
		Project project = environment.getSource();

		return sampleService.getSamplesForProjectShallow(project);
	};

	DataFetcher projectsDataFetcher = environment -> {
		return projectService.findAll();
	};

	DataFetcher sampleDataFetcher = environment -> {
		String id = environment.getArgument("id");

		return sampleService.read(Long.valueOf(id));
	};

	DataFetcher sampleProjectsDataFetcher = environment -> {
		Sample sample = environment.getSource();

		List<Join<Project, Sample>> projectsForSample = projectService.getProjectsForSample(sample);

		List<Project> projects = new ArrayList<>();

		for (Join<Project, Sample> join : projectsForSample) {
			projects.add(join.getSubject());
		}

		return projects;
	};

}
