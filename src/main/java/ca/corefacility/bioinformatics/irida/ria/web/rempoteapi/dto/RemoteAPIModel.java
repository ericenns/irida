package ca.corefacility.bioinformatics.irida.ria.web.rempoteapi.dto;

import ca.corefacility.bioinformatics.irida.model.RemoteAPI;
import ca.corefacility.bioinformatics.irida.ria.web.components.ant.table.TableModel;

/**
 * Represents a {@link RemoteAPI} in the Remote API table.
 */
public class RemoteAPIModel extends TableModel {
	public RemoteAPIModel(RemoteAPI api) {
		super(api.getId(), api.getName(), api.getCreatedDate(), api.getModifiedDate());
	}
}
