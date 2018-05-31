package ca.corefacility.bioinformatics.irida.pipeline.results.impl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.corefacility.bioinformatics.irida.exceptions.PostProcessingException;
import ca.corefacility.bioinformatics.irida.model.enums.AnalysisType;
import ca.corefacility.bioinformatics.irida.model.joins.Join;
import ca.corefacility.bioinformatics.irida.model.joins.impl.ProjectMetadataTemplateJoin;
import ca.corefacility.bioinformatics.irida.model.project.Project;
import ca.corefacility.bioinformatics.irida.model.sample.MetadataTemplate;
import ca.corefacility.bioinformatics.irida.model.sample.MetadataTemplateField;
import ca.corefacility.bioinformatics.irida.model.sample.Sample;
import ca.corefacility.bioinformatics.irida.model.sample.metadata.MetadataEntry;
import ca.corefacility.bioinformatics.irida.model.sample.metadata.PipelineProvidedMetadataEntry;
import ca.corefacility.bioinformatics.irida.model.workflow.analysis.AnalysisOutputFile;
import ca.corefacility.bioinformatics.irida.model.workflow.submission.AnalysisSubmission;
import ca.corefacility.bioinformatics.irida.pipeline.results.AnalysisSampleUpdater;
import ca.corefacility.bioinformatics.irida.service.ProjectService;
import ca.corefacility.bioinformatics.irida.service.sample.MetadataTemplateService;
import ca.corefacility.bioinformatics.irida.service.sample.SampleService;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * {@link AnalysisSampleUpdater} for bio_hansel results to be written to metadata of {@link Sample}s.
 */
@Component
public class BioHanselSampleUpdater implements AnalysisSampleUpdater {
	private static final Logger logger = LoggerFactory.getLogger(BioHanselSampleUpdater.class);
	private static final String BIO_HANSEL_RESULTS_FILE = "bio_hansel-results.json";
	private static final String SCHEME_KEY = "scheme";
	private static final String VERSION_KEY = "scheme_version";
	private static final String TMPL_NAME_FMT = "bio_hansel/%1$s/v%2$s";
	// @formatter:off
	private static final List<String> TMPL_FIELD_ORDER = ImmutableList.of(
			"subtype",
			"qc_status",
			"qc_message",
			"avg_tile_coverage"
	);
	private static Map<String, String> BIO_HANSEL_RESULTS_FIELDS = ImmutableMap.of(
			"subtype", "Subtype",
			"avg_tile_coverage", "Average Tile Coverage",
			"qc_status", "QC Status",
			"qc_message", "QC Message"
	);
	// @formatter:on
	private MetadataTemplateService metadataTemplateService;
	private SampleService sampleService;
	private ProjectService projectService;

	@Autowired
	public BioHanselSampleUpdater(MetadataTemplateService metadataTemplateService, SampleService sampleService,
			ProjectService projectService) {
		this.metadataTemplateService = metadataTemplateService;
		this.sampleService = sampleService;
		this.projectService = projectService;
	}

	/**
	 * Add bio_hansel results to the metadata of the given {@link Sample}.
	 * <p>
	 * Create a bio_hansel {@link MetadataTemplate} for each {@link Project} of the {@link Sample} if one doesn't exist.
	 *
	 * @param samples  The sample to update (collection should only have one {@link Sample} object).
	 * @param analysis Use the results from this {@link AnalysisSubmission} to update the {@link Sample} metadata.
	 * @throws PostProcessingException if the updater could not complete its processing
	 */
	@Override
	public void update(Collection<Sample> samples, AnalysisSubmission analysis) throws PostProcessingException {
		if (samples.size() != 1) {
			throw new PostProcessingException(
					"Expected one sample; got '" + samples.size() + "' for analysis [id=" + analysis.getId() + "]");
		}
		final Sample sample = samples.iterator()
				.next();

		AnalysisOutputFile aof = analysis.getAnalysis()
				.getAnalysisOutputFile(BIO_HANSEL_RESULTS_FILE);

		Path filePath = aof.getFile();

		Map<String, MetadataEntry> stringEntries = new HashMap<>();
		try {
			@SuppressWarnings("resource") String jsonText = new Scanner(
					new BufferedReader(new FileReader(filePath.toFile()))).useDelimiter("\\Z")
					.next();
			ObjectMapper mapper = new ObjectMapper();
			List<Map<String, Object>> maps = mapper.readValue(jsonText, new TypeReference<List<Map<String, Object>>>() {
			});
			if (maps.size() > 0) {
				Map<String, Object> result = maps.get(0);

				final String scheme = (String) result.get(SCHEME_KEY);
				final String version = (String) result.get(VERSION_KEY);
				final String baseNamespace = getBaseNamespace(scheme, version);
				List<String> templateNamespacedFields = new ArrayList<>();
				TMPL_FIELD_ORDER.forEach(key -> {
					String field = BIO_HANSEL_RESULTS_FIELDS.get(key);
					final String formattedField = getNamespacedField(baseNamespace, field);
					templateNamespacedFields.add(formattedField);
					if (result.containsKey(key)) {
						String value = result.get(key)
								.toString();
						PipelineProvidedMetadataEntry metadataEntry = new PipelineProvidedMetadataEntry(value, "text",
								analysis);
						stringEntries.put(formattedField, metadataEntry);
					} else {
						logger.warn("bio_hansel output file '" + filePath.toFile()
								.getAbsolutePath() + "' does not contain expected key '" + key
								+ "'. Please check the format of this file!");
					}
				});
				Map<MetadataTemplateField, MetadataEntry> metadataMap = metadataTemplateService.getMetadataMap(
						stringEntries);

				sample.mergeMetadata(metadataMap);
				sampleService.updateFields(sample.getId(), ImmutableMap.of("metadata", sample.getMetadata()));

				createBioHanselMetadataTemplateForSampleProjects(sample, baseNamespace, templateNamespacedFields,
						metadataMap);
			} else {
				throw new PostProcessingException(filePath + " not correctly formatted. Expected valid JSON.");
			}

		} catch (IOException e) {
			throw new PostProcessingException("Error parsing JSON from " + filePath, e);
		}
	}

	/**
	 * Gets the {@link AnalysisType} this updater service handles.
	 *
	 * @return The {@link AnalysisType}.
	 */
	@Override
	public AnalysisType getAnalysisType() {
		return AnalysisType.BIO_HANSEL;
	}

	/**
	 * Get the base bio_hansel metadata field namespace.
	 * <p>
	 * For example, `bio_hansel/heidelberg/v0.5.0`, so that metadata fields from different analyses of bio_hansel will
	 * not clash, e.g. `bio_hansel/heidelberg/v0.5.0/Subtype` vs `bio_hansel/enteritidis/v0.7.0/Subtype`.
	 *
	 * @param scheme bio_hansel scheme name.
	 * @param version bio_hansel scheme version.
	 * @return Base bio_hansel metadata field namespace prefix.
	 */
	private String getBaseNamespace(String scheme, String version) {
		return String.format(TMPL_NAME_FMT, scheme, version);
	}

	/**
	 * Given a base bio_hansel metadata field namespace, get the namespaced metadata field name.
	 *
	 * @param baseNamespace The base bio_hansel metadata field namespace, e.g. `bio_hansel/enteritidis/v0.7.0`
	 * @param field Metadata field, e.g. `Subtype`.
	 * @return Namespaced metadata field, e.g. `bio_hansel/enteritidis/v0.7.0/Subtype`.
	 */
	private String getNamespacedField(String baseNamespace, String field) {
		return baseNamespace + "/" + field;
	}

	/**
	 * Create a bio_hansel {@link MetadataTemplate} for each {@link Project} each Sample belongs to if one does not already exist.
	 *
	 * @param sample      {@link Sample} for which a bio_hansel {@link MetadataTemplate} will be added to all {@link Project}s that the {@link Sample} belongs to, if one does not already exist.
	 * @param tmplName    {@link MetadataTemplate} label/name.
	 * @param tmplFields  Ordered list of {@link MetadataTemplateField} labels
	 * @param metadataMap Metadata field to entry map for fields and entries that have been saved to the DB.
	 */
	private void createBioHanselMetadataTemplateForSampleProjects(Sample sample, String tmplName,
			List<String> tmplFields, Map<MetadataTemplateField, MetadataEntry> metadataMap) {
		final List<Project> projects = getProjects(sample);
		final List<MetadataTemplateField> fields = getMetadataTemplateFields(tmplFields, metadataMap);
		for (Project p : projects) {
			createTemplate(p, tmplName, fields);
		}
	}

	/**
	 * Given the {@link MetadataTemplateField}s and {@link MetadataEntry}s added to a {@link Sample}'s metadata, get the ordered list of {@link MetadataTemplateField}s.
	 *
	 * @param tmplFields Ordered metadata template fields.
	 * @param metadataMap The {@link MetadataTemplateField}s and {@link MetadataEntry}s added to a {@link Sample}'s metadata.
	 * @return Ordered list of {@link MetadataTemplateField}s
	 */
	private List<MetadataTemplateField> getMetadataTemplateFields(List<String> tmplFields,
			Map<MetadataTemplateField, MetadataEntry> metadataMap) {
		final Map<String, MetadataTemplateField> fieldsMap = metadataMap.keySet()
				.stream()
				.collect(Collectors.toMap(MetadataTemplateField::getLabel, x -> x));
		return tmplFields.stream()
				.map(fieldsMap::get)
				.collect(Collectors.toList());
	}

	private List<Project> getProjects(Sample sample) {
		return projectService.getProjectsForSample(sample)
				.stream()
				.map(Join::getSubject)
				.collect(Collectors.toList());
	}

	/**
	 * Create and save a {@link MetadataTemplate} for bio_hansel for a {@link Project} if one does not already exist.
	 *
	 * @param project        Project to add template to if an identical one does not already exist
	 * @param templateName   Template name
	 * @param templateFields bio_hansel {@link MetadataTemplate} {@link MetadataTemplateField}s
	 */
	private void createTemplate(Project project, String templateName, List<MetadataTemplateField> templateFields) {
		final List<String> fieldLabels = templateFields.stream()
				.map(MetadataTemplateField::getLabel)
				.collect(Collectors.toList());
		final List<MetadataTemplate> templates = getExistingMetadataTemplates(project, templateName, fieldLabels);
		logger.debug("Project '" + project.getId() + "' found " + templates.size() + " template(s) " + templates
				+ " with name '" + templateName + "' with fields '" + fieldLabels + "'");
		if (!templates.isEmpty()) {
			logger.debug(
					"Metadata template for bio_hansel already exists for project '" + project.getId() + "'. Skipping!");
			return;
		}
		final MetadataTemplate template = new MetadataTemplate(templateName, templateFields);
		logger.debug("No metadata template for project [" + project.getId() + "]. Setting template to ["
				+ template.getLabel() + ", fields=" + template.getFields() + "]");
		metadataTemplateService.createMetadataTemplateInProject(template, project);
		logger.debug("Created template '" + template.getId() + "' for project '" + project.getId() + "'.");
	}

	/**
	 * Get any {@link MetadataTemplate}s that match a metadata template name and list of field names in a given {@link Project}.
	 *
	 * @param project {@link Project} to try to find {@link MetadataTemplate} in.
	 * @param templateName Name of metadata template.
	 * @param fieldLabels Ordered metadata field labels.
	 * @return Any matching {@link MetadataTemplate}s for the {@link Project}.
	 */
	private List<MetadataTemplate> getExistingMetadataTemplates(Project project, String templateName,
			List<String> fieldLabels) {
		return metadataTemplateService.getMetadataTemplatesForProject(project)
				.stream()
				.map(ProjectMetadataTemplateJoin::getObject)
				.filter(t -> Objects.equals(templateName, t.getLabel()))
				.filter(t -> fieldLabels.equals(t.getFields()
						.stream()
						.map(MetadataTemplateField::getLabel)
						.collect(Collectors.toList())))
				.collect(Collectors.toList());
	}

}
