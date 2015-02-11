package ca.corefacility.bioinformatics.irida.model.workflow.description;

import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Defines input parameters for a workflow which can be adjusted.
 * 
 * @author Aaron Petkau <aaron.petkau@phac-aspc.gc.ca>
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class IridaWorkflowParameter {

	@XmlAttribute(name = "name")
	private String name;

	@XmlAttribute(name = "defaultValue", required=true)
	private String defaultValue;

	@XmlElement(name = "toolParameter")
	private List<IridaToolParameter> toolParameters;

	public IridaWorkflowParameter() {
	}

	/**
	 * Creates a new {@link IridaWorkflowParameter} which maps to the given tool
	 * parameters.
	 * 
	 * @param name
	 *            The name of the parameter.
	 * @param defaultValue
	 *            The default value of this parameter.
	 * @param toolParameters
	 *            The tool parameters corresponding to this named parameter.
	 */
	public IridaWorkflowParameter(String name, String defaultValue, List<IridaToolParameter> toolParameters) {
		this.name = name;
		this.defaultValue = defaultValue;
		this.toolParameters = toolParameters;
	}

	/**
	 * Gets the name of this parameter.
	 * 
	 * @return The name of this parameter.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets a list of tools whose values are affected by this parameter.
	 * 
	 * @return A {@link List} of tools whose values are affected by this
	 *         parameter.
	 */
	public List<IridaToolParameter> getToolParameters() {
		return toolParameters;
	}

	/**
	 * Gets the default value for this parameter.
	 * 
	 * @return The default value for this parameter.
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, defaultValue, toolParameters);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		else if (obj instanceof IridaWorkflowParameter) {
			IridaWorkflowParameter other = (IridaWorkflowParameter) obj;

			return Objects.equals(name, other.name) && Objects.equals(defaultValue, other.defaultValue)
					&& Objects.equals(toolParameters, other.toolParameters);
		}

		return false;
	}

	@Override
	public String toString() {
		return "IridaWorkflowParameter [name=" + name + ", defaultValue=" + defaultValue + ", toolParameters="
				+ toolParameters + "]";
	}
}