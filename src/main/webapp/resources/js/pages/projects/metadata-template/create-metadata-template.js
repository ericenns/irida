import $ from "jquery";
import Sortable from "sortablejs";
import "../../../vendor/plugins/jquery/select2";
import "jquery-validation";
import "../../../../sass/pages/create-linelist-template.scss";

// ****************************************************************************
// FORM VALIDATION - uses jquery-validation plugin
// ****************************************************************************
const saveBtn = $("#save-btn");
const form = $("#template-form");

const setSubmitBtnState = () => {
  const fieldCount = $('[name="fields"]').length;
  saveBtn.prop("disabled", !form.valid() || fieldCount === 0);
};

form.validate({
  errorElement: "em",
  errorPlacement: function(error, element) {
    error.addClass("help-block");
    error.insertAfter(element);
  },
  highlight(element) {
    $(element)
      .parents(".form-group")
      .addClass("has-error")
      .removeClass("has-success");
  },
  unhighlight(element) {
    $(element)
      .parents(".form-group")
      .addClass("has-success")
      .removeClass("has-error");
  }
});

form.find("input").on("keyup blur", setSubmitBtnState);

const list = document.querySelector(".template-fields");

if (window.PAGE.isOwner) {
  Sortable.create(list);
}

// ****************************************************************************
// Current Fields
// Get a list of all the current fields on the page so that we
// do not allow duplicate fields.
// ****************************************************************************
const existingFields = new Set();
document.querySelectorAll(".field-names").forEach(field => {
  existingFields.add(field.value);
});

/**
 * Make sure there is at least 1 field in the template
 * if there is not, display a warning message
 */
function checkFieldsState() {
  const $fieldsWarning = $("#field-count-warning");
  const fieldCount = $('[name="fields"]').length;
  if (fieldCount) {
    $fieldsWarning.addClass("hidden");
  } else {
    $fieldsWarning.removeClass("hidden");
  }
}

/**
 * Get a template for a new field item in the DOM
 * @param {string} field name
 * @return {string} DOM element.
 */
function createNewListGroupItem(field) {
  return `
<li class="list-group-item template-field">
  <i class="fa fa-bars" aria-hidden="true"></i>&nbsp;
  ${field}
  <input class="field-names" name="fields" type="hidden" value="${field}"/>
  <span class="close field-remove pull-right">&times;</span>
</li>
  `;
}

/**
 * Add a new field list item to the page.
 * @param {string} field name
 */
function addNewField(field) {
  const li = createNewListGroupItem(field);
  $(".template-fields").append(li);
  checkFieldsState();
  setSubmitBtnState();
}

/**
 * Remove the field from the page
 * @param {object} btn clicked
 */
function removeFieldFromButton(btn) {
  const li = $(btn).closest("li");
  const term = li.find(".field-names").val();
  existingFields.delete(term);
  li.remove();
}

/**
 * Handle clicking the remove button on the field.
 */
$(".template-fields").on("click", ".field-remove", function() {
  removeFieldFromButton(this);
  setSubmitBtnState();
  checkFieldsState();
});

$("#add-field-modal").on("shown.bs.modal", function() {
  const modal = $(this);
  const fieldSelect2 = $("#field-select")
    .select2({
      dropdownParent: modal,
      theme: "bootstrap",
      minimumInputLength: 1,
      ajax: {
        url: window.PAGE.urls.fields,
        dataType: "json",
        delay: 250,
        processResults(data, params) {
          data = data.filter(item => {
            return !existingFields.has(item);
          });
          const results = data.map(item => ({ id: item, text: item }));
          const searchTerm = params.term;
          if (!data.includes(searchTerm) && !existingFields.has(searchTerm)) {
            results.unshift({
              id: searchTerm,
              isNew: true,
              text: searchTerm
            });
          }
          return { results };
        },
        cache: true
      },
      templateResult(data) {
        if (data.isNew) {
          return $(`
<div>${data.text}<span class="label label-warning pull-right">NEW</span></div>
`);
        }
        return data.text;
      }
    })
    .on("select2:select", function() {
      const value = $(this).val();
      if (value) {
        existingFields.add(value);
        addNewField(value);
      }

      // Reset Select2
      fieldSelect2.val(null).trigger("change");
      modal.modal("hide");
    });

  fieldSelect2.select2("open");
});

// ****************************************************************************
// Set up Delete Template
// ****************************************************************************
$("#delete-checkbox").on("change", function() {
  $("#delete-template-btn").prop("disabled", !this.checked);
});

// Force validation on load
form.validate().form();
checkFieldsState();
