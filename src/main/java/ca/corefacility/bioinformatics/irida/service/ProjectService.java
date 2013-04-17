package ca.corefacility.bioinformatics.irida.service;

import ca.corefacility.bioinformatics.irida.model.roles.impl.Identifier;
import ca.corefacility.bioinformatics.irida.model.Project;
import ca.corefacility.bioinformatics.irida.model.Role;
import ca.corefacility.bioinformatics.irida.model.Sample;
import ca.corefacility.bioinformatics.irida.model.User;
import java.util.Collection;
import org.springframework.transaction.annotation.Transactional;

/**
 * A specialized service layer for projects.
 *
 * @author Franklin Bristow <franklin.bristow@phac-aspc.gc.ca>
 */
@Transactional
public interface ProjectService extends CRUDService<Identifier, Project> {

    /**
     * Add the specified user to the project with a role. If the user is a
     * manager for the project, then the user should be added to the project
     * with the 'ROLE_MANAGER' role.
     *
     * @param project the project to add the user to.
     * @param user the user to add to the project.
     * @param role the role that the user plays on the project.
     */
    public void addUserToProject(Project project, User user, Role role);

    /**
     * Add the specified sample to the project.
     *
     * @param project the project to add the sample to.
     * @param sample the sample to add to the project.
     */
    public void addSampleToProject(Project project, Sample sample);

    /**
     * Get all users associated with a particular project.
     *
     * @param project the project to get users for.
     * @return the users associated with the project.
     */
    public Collection<User> getUsersForProject(Project project);

    /**
     * Get all {@link Project}s associated with a particular {@link User}.
     *
     * @param user the user to get projects for.
     * @return the projects associated with the user.
     */
    public Collection<Project> getProjectsForUser(User user);
}
