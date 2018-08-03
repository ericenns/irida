import React from "react";
import PropTypes from "prop-types";
import ImmutablePropTypes from "react-immutable-proptypes";
import { Select } from "antd";
import { TemplateSelectOption } from "./TemplateSelectOption";
import {
  PopoverContents,
  HelpPopover
} from "../../../../../components/popovers/index";

const { Option } = Select;
const { i18n } = window.PAGE;

/*
The internationalized content of the help popover describing
what a template is and how to use it.
 */
const content = (
  <React.Fragment>
    <p>{i18n.linelist.templates.Popover.content}</p>
    <p>{i18n.linelist.templates.Popover.description}</p>
  </React.Fragment>
);

/**
 * Component to render an [antd Select]{@link https://ant.design/components/select/}
 * component to select a specific Metadata template.
 */
export function TemplateSelect(props) {
  const { current, useTemplate } = props;
  const templates = props.templates.toJS();

  return (
    <React.Fragment>
      <label style={{color: "#707171"}}>
        {i18n.linelist.templates.title}
        <HelpPopover
          content={<PopoverContents contents={content} />}
          title={i18n.linelist.templates.Popover.title}
        />
      </label>
      <Select
        disabled={templates.length === 1}
        value={current}
        style={{ width: "100%" }}
        onSelect={useTemplate}
      >
        {templates.map((template, index) => (
          <Option key={template.id} value={index} title={template.name}>
            <TemplateSelectOption
              {...props}
              index={index}
              template={template}
            />
          </Option>
        ))}
      </Select>
    </React.Fragment>
  );
}

TemplateSelect.propTypes = {
  current: PropTypes.number.isRequired,
  templates: ImmutablePropTypes.list.isRequired,
  useTemplate: PropTypes.func.isRequired
};