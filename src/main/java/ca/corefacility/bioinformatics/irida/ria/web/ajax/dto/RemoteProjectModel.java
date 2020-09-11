package ca.corefacility.bioinformatics.irida.ria.web.ajax.dto;

import ca.corefacility.bioinformatics.irida.model.project.Project;

public class RemoteProjectModel {
	private final String value; // Should be id for the item
	private final String label; // Should be the name for the item;
	private final String key;

	public RemoteProjectModel(Project project) {
		this.value = project.getRemoteStatus()
				.getURL();
		this.label = project.getLabel();
		this.key = "project-" + project.getId();
	}

	public String getValue() {
		return value;
	}

	public String getLabel() {
		return label;
	}

	public String getKey() {
		return key;
	}
}
