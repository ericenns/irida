package ca.corefacility.bioinformatics.irida.security.permissions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.corefacility.bioinformatics.irida.repositories.ProjectRepository;
import ca.corefacility.bioinformatics.irida.repositories.joins.project.ProjectUserJoinRepository;
import ca.corefacility.bioinformatics.irida.repositories.joins.project.UserGroupProjectJoinRepository;
import ca.corefacility.bioinformatics.irida.repositories.user.UserGroupJoinRepository;
import ca.corefacility.bioinformatics.irida.repositories.user.UserRepository;

@Component
public class ManageProjectMembersPermission extends ModifyProjectPermission {
	
	private static final String PERMISSION_PROVIDED = "manageProjectMembers";

	/**
	 * Construct an instance of {@link ManageProjectMembersPermission}.
	 * 
	 * @param projectRepository
	 *            the project repository.
	 * @param userRepository
	 *            the user repository.
	 * @param pujRepository
	 *            the project user join repository.
	 */
	@Autowired
	public ManageProjectMembersPermission(final ProjectRepository projectRepository,
			final UserRepository userRepository, final ProjectUserJoinRepository pujRepository,
			final UserGroupProjectJoinRepository ugpjRepository, final UserGroupJoinRepository ugRepository) {
		super(projectRepository, userRepository, pujRepository, ugpjRepository, ugRepository);
	}

	@Override
	public String getPermissionProvided() {
		return PERMISSION_PROVIDED;
	}
}
