package io.fintechlabs.testframework.condition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Require a set of conditions and/or strings to be in the environment
 * after a condition is evaluated.
 *
 * @author jricher
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PostEnvironment {
	String[] required() default {};

	String[] strings() default {};

}
