package ca.corefacility.bioinformatics.irida.model.sample.metadata;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

@Entity
@Audited
@Table(name = "metadata_entry")
public class MetadataEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String value;
	private String type;

	public MetadataEntry(String value, String type) {
		this.value = value;
		this.type = type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public String getValue() {
		return value;
	}
}
