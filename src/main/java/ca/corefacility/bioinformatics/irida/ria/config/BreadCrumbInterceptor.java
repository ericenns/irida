package ca.corefacility.bioinformatics.irida.ria.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.MessageSource;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.google.common.collect.ImmutableMap;

/**
 * Interceptor for handling UI BreadCrumbs
 */
public class BreadCrumbInterceptor extends HandlerInterceptorAdapter {
	private final MessageSource messageSource;
	private Map<String, Boolean> BASE = ImmutableMap.of(
			"dashboard", true,
			"projects", true,
			"samples", true,
			"sequenceFiles", true
	);


	public BreadCrumbInterceptor(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		super.postHandle(request, response, handler, modelAndView);

		Locale locale = request.getLocale();
		List<Map<String, String>> crumbs = new ArrayList<>();

		String servletPath = request.getServletPath();
		String[] parts = servletPath.split("/");

		if (!servletPath.contains("ajax") && BASE.containsKey(parts[1])) {

			// Check to ensure that there is some sort of context path.
			String contextPath = request.getContextPath();

			StringBuilder url = new StringBuilder(contextPath);

			for (int i = 1; i < parts.length; i++) {
				// Should be a noun
				String noun = parts[i];
				url.append("/");
				url.append(noun);

				crumbs.add(
						ImmutableMap.of(
								"text", messageSource.getMessage("bc." + noun, null, locale),
								"url", url.toString())
				);

				// Check to see if there is a next part, if there is it is expected to be an id.
				if (parts.length > ++i) {
					String id = parts[i];
					url.append("/");
					url.append(id);
					crumbs.add(
							ImmutableMap.of(
									"text", id,
									"url", url.toString()
							)
					);
				}
			}

			modelAndView.getModelMap().put("crumbs", crumbs);
		}
	}
}
