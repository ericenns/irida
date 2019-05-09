package ca.corefacility.bioinformatics.irida.web.controller.api.graphql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.corefacility.bioinformatics.irida.model.joins.Join;
import ca.corefacility.bioinformatics.irida.model.project.Project;
import ca.corefacility.bioinformatics.irida.model.run.SequencingRun;
import ca.corefacility.bioinformatics.irida.model.sample.Sample;
import ca.corefacility.bioinformatics.irida.model.sample.SampleSequencingObjectJoin;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.SequenceFile;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.SequencingObject;
import ca.corefacility.bioinformatics.irida.service.ProjectService;
import ca.corefacility.bioinformatics.irida.service.SequencingObjectService;
import ca.corefacility.bioinformatics.irida.service.SequencingRunService;
import ca.corefacility.bioinformatics.irida.service.sample.SampleService;

import graphql.GraphQLException;
import graphql.schema.DataFetcher;

/**
 * Wiring for GraphQL requests
 */
@Component
public class IridaWiring {

	private static final Logger logger = LoggerFactory.getLogger(IridaWiring.class);
	/**
	 * Reference to {@link ProjectService}.
	 */
	private ProjectService projectService;
	/**
	 * Reference to {@link SampleService}.
	 */
	private SampleService sampleService;
	/**
	 * Reference to {@link SequencingRunService}.
	 */
	private SequencingRunService sequencingRunService;
	/**
	 * Reference to {@link SequencingObjectService}.
	 */
	private SequencingObjectService sequencingObjectService;

	/**
	 * Constructor for {@link IridaWiring}, requires a reference to a {@link ProjectService}
	 * and a {@link SampleService}
	 *
	 * @param projectService
	 * 				the {@link ProjectService} to be used by this wiring.
	 * @param sampleService
	 * 				the {@link SampleService} to be used by this wiring.
	 * @param sequencingRunService
	 * 				the {@link SequencingRunService} to be used by this wiring.
	 */
	@Autowired
	public IridaWiring(ProjectService projectService, SampleService sampleService,
			SequencingRunService sequencingRunService, SequencingObjectService sequencingObjectService) {
		this.projectService = projectService;
		this.sampleService = sampleService;
		this.sequencingRunService = sequencingRunService;
		this.sequencingObjectService = sequencingObjectService;
	}

	DataFetcher projectDataFetcher = environment -> {
		String id = environment.getArgument("id");

		return projectService.read(Long.valueOf(id));
	};

	DataFetcher createProjectMutationDataFetcher = environment -> {
		Project newProject = new Project(environment.getArgument("name"));

		String organism = environment.getArgument("organism");
		String projectDescription = environment.getArgument("projectDescription");
		String remoteUrl =  environment.getArgument("remoteUrl");
		Boolean assembleUploads = environment.getArgument("assembleUploads");

		newProject.setOrganism(organism);
		newProject.setProjectDescription(projectDescription);
		newProject.setRemoteURL(remoteUrl);
		newProject.setAssembleUploads(assembleUploads);

		try {
			newProject = projectService.create(newProject);
		} catch (Exception e) {
			logger.debug("Throwing GraphQLException in createProject");
			throw new GraphQLException(e.getMessage());
		}

		return newProject;
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

	DataFetcher createSampleMutationDataFetcher = environment -> {
		Sample newSample = new Sample(environment.getArgument("sampleName"));

		String organism = environment.getArgument("organism");

		newSample.setOrganism(organism);

		try {
			newSample = sampleService.create(newSample);
		} catch (Exception e) {
			logger.debug("Throwing GraphQLException in createSample");
			throw new GraphQLException(e.getMessage());
		}

		return newSample;
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

	DataFetcher sampleSequencingFilesDataFetcher = environment -> {
		Sample sample = environment.getSource();

		Collection<SampleSequencingObjectJoin> sequencingObjectsForSample = sequencingObjectService.getSequencingObjectsForSample(
				sample);

		List<SequenceFile> sequenceFiles = new ArrayList<>();

		for(SampleSequencingObjectJoin sampleSequencingObjectJoin :  sequencingObjectsForSample) {
			sequenceFiles.addAll(sampleSequencingObjectJoin.getObject().getFiles());
		}

		return sequenceFiles;
	};

	DataFetcher sequencingRunDataFetcher = environment -> {
		String id = environment.getArgument("id");

		return sequencingRunService.read(Long.valueOf(id));
	};

	DataFetcher sequencingRunsDataFetcher = environment -> {
		return sequencingRunService.findAll();
	};

	DataFetcher sequencingRunSequencingFilesDataFetcher = environment -> {
		SequencingRun sequencingRun = environment.getSource();

		Set<SequencingObject> sequencingObjects = sequencingObjectService.getSequencingObjectsForSequencingRun(sequencingRun);
		List<SequenceFile> sequenceFiles = new ArrayList<>();

		for (SequencingObject sequencingObject : sequencingObjects) {
			sequenceFiles.addAll(sequencingObject.getFiles());
		}

		return sequenceFiles;
	};
}
