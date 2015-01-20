package ca.corefacility.bioinformatics.irida.ria.unit.web.pipelines;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.MessageSource;
import org.springframework.ui.ExtendedModelMap;

import ca.corefacility.bioinformatics.irida.model.enums.ProjectRole;
import ca.corefacility.bioinformatics.irida.model.project.Project;
import ca.corefacility.bioinformatics.irida.model.sample.Sample;
import ca.corefacility.bioinformatics.irida.model.user.User;
import ca.corefacility.bioinformatics.irida.ria.unit.TestDataFactory;
import ca.corefacility.bioinformatics.irida.ria.web.analysis.CartController;
import ca.corefacility.bioinformatics.irida.ria.web.pipelines.PipelineController;
import ca.corefacility.bioinformatics.irida.service.AnalysisSubmissionService;
import ca.corefacility.bioinformatics.irida.service.ProjectService;
import ca.corefacility.bioinformatics.irida.service.ReferenceFileService;
import ca.corefacility.bioinformatics.irida.service.SequenceFileService;
import ca.corefacility.bioinformatics.irida.service.sample.SampleService;
import ca.corefacility.bioinformatics.irida.service.user.UserService;
import ca.corefacility.bioinformatics.irida.service.workflow.IridaWorkflowsService;

/**
 * Created by josh on 15-01-09.
 */
public class PipelineControllerTest {
	// Dependencies to mock
	private SampleService sampleService;
	private ReferenceFileService referenceFileService;
	private SequenceFileService sequenceFileService;
	private AnalysisSubmissionService analysisSubmissionService;
	private IridaWorkflowsService workflowsService;
	private ProjectService projectService;
	private UserService userService;
	private MessageSource messageSource;
	private CartController cartController;

	// Controller to test
	private PipelineController controller;

	// Constants
	public static final Locale LOCALE = Locale.US;

	@Before
	public void setUp() {
		sampleService = mock(SampleService.class);
		referenceFileService = mock(ReferenceFileService.class);
		sequenceFileService = mock(SequenceFileService.class);
		analysisSubmissionService = mock(AnalysisSubmissionService.class);
		workflowsService = mock(IridaWorkflowsService.class);
		projectService = mock(ProjectService.class);
		userService = mock(UserService.class);
		messageSource = mock(MessageSource.class);
		cartController = mock(CartController.class);

		controller = new PipelineController(sampleService, sequenceFileService, referenceFileService,
				analysisSubmissionService, workflowsService, projectService, userService, cartController, messageSource);
	}

	@Test
	public void testGetPipelineLaunchPage() {
		ExtendedModelMap model = new ExtendedModelMap();
		String response = controller.getPipelineLaunchPage(model, LOCALE);
		assertEquals(PipelineController.URL_LAUNCH, response);
		assertTrue(model.containsKey("counts"));
		assertTrue(model.containsKey("workflows"));
	}

	@Test
	public void testGetPhylogenomicsPageWithEmptyCart() {
		ExtendedModelMap model = new ExtendedModelMap();
		Principal principal = () -> "FRED";
		String response = controller.getPhylogenomicsPage(model, principal);
		assertEquals("If cart is empty user should be redirected.", PipelineController.URL_EMPTY_CART_REDIRECT, response);
	}

	@Test
	public void testGetPhylogenomicsPageWithCart() {
		ExtendedModelMap model = new ExtendedModelMap();
		String username = "FRED";
		Principal principal = () -> username;
		User user = TestDataFactory.constructUser();
		when(userService.getUserByUsername(username)).thenReturn(user);
		when(projectService.userHasProjectRole(any(User.class), any(Project.class), any(ProjectRole.class))).thenReturn(true);
		when(cartController.getSelected()).thenReturn(TestDataFactory.constructCart());
		when(sequenceFileService.getSequenceFilesForSample(any(Sample.class)))
				.thenReturn(TestDataFactory.generateSequenceFilesForSample(TestDataFactory.constructSample()));
		String response = controller.getPhylogenomicsPage(model, principal);
		assertEquals("Response should be the path to the phylogenomics template", PipelineController.URL_PHYLOGENOMICS, response);
		assertTrue("Model should contain the reference files.", model.containsKey("referenceFiles"));
		assertTrue("Model should contain a list of files.", model.containsKey("files"));
	}
}