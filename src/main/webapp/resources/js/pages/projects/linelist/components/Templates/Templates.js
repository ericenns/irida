import React from "react";
import PropTypes from "prop-types";
import { TemplateSelect } from "./TemplateSelect";
import { SaveTemplateModal } from "./SaveTemplateModal";

export const Templates = props => {
  return (
    <div style={{ marginBottom: "1rem" }}>
      <TemplateSelect {...props} />
      <SaveTemplateModal
        modified={props.modified}
        validating={props.validating}
        validateTemplateName={props.validateTemplateName}
      />
    </div>
  );
};

Templates.propTypes = {
  modified: PropTypes.bool.isRequired,
  validateTemplateName: PropTypes.func.isRequired,
  validating: PropTypes.bool.isRequired
};
