package io.fintechlabs.testframework.errorhandling;

import org.springframework.boot.autoconfigure.web.AbstractErrorController;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
public class FAPIErrorController extends AbstractErrorController {

	public FAPIErrorController(ErrorAttributes errorAttributes) {
		super(errorAttributes);
	}

	@RequestMapping(value = "/error")
	public ModelAndView handleError(HttpServletRequest request) {
		Map<String, Object> map = getErrorAttributes(request, false);
		return new ModelAndView("error", map);
	}

	@Override
	public String getErrorPath() {
		return "/error";
	}
}
