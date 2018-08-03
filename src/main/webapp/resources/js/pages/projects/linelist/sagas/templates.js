import { call, put, take } from "redux-saga/effects";
import { delay } from "redux-saga";
import { types as appTypes } from "../../../../redux/reducers/app";
import { actions, types } from "../reducers/templates";
import {
  fetchTemplates,
  saveTemplate
} from "../../../../apis/metadata/templates";

/**
 * Initialize templates in the line list
 * @returns {IterableIterator<*>}
 */
export function* templatesLoadingSaga() {
  try {
    const { payload } = yield take(appTypes.INIT_APP);
    yield put(actions.load());
    const { data: templates } = yield call(fetchTemplates, payload.id);
    yield put(actions.success(templates));
  } catch (error) {
    yield put(actions.error(error));
  }
}

export function* saveTemplateSaga() {
  while (true) {
    try {
      const { data } = yield take(types.SAVE_TEMPLATE);
      data.fields = data.fields.filter(f => !f.hide);
      const { data: response } = yield call(saveTemplate, data);
      yield put(actions.savedTemplate(response.UIMetadataTemplate));
      // Delay allows for displaying the saved message
      yield call(delay, 2500);
      yield put(actions.savedComplete());
    } catch (error) {
      // TODO: (Josh | 2018-05-14) Handle this in the UI
      console.error("ERROR SAVING TEMPLATE", error);
    }
  }
}