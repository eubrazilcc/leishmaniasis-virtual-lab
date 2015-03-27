/*
 * Copyright 2014 EUBrazilCC (EU‐Brazil Cloud Connect)
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by 
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *   http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 * 
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 */

package eu.eubrazilcc.lvl.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;

import org.junit.Assume;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * A JUnit rule to conditionally ignore tests.
 * @author Radiger Herrmann - initial API and implementation.
 * @see <a href="http://www.codeaffine.com/2013/11/18/a-junit-rule-to-conditionally-ignore-tests/">A JUnit Rule to Conditionally Ignore Tests</a>
 */
public class ConditionalIgnoreRule implements MethodRule {

	public interface IgnoreCondition {
		boolean isSatisfied();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD})
	public @interface ConditionalIgnore {
		Class<? extends IgnoreCondition> condition();
	}

	public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
		Statement result = base;
		if (hasConditionalIgnoreAnnotation(method)) {
			final IgnoreCondition condition = getIgnoreContition(method, target);
			if (condition.isSatisfied()) {
				result = new IgnoreStatement(condition);
			}
		}
		return result;
	}

	private boolean hasConditionalIgnoreAnnotation(final FrameworkMethod method) {
		return method.getAnnotation(ConditionalIgnore.class) != null;
	}

	private IgnoreCondition getIgnoreContition(final FrameworkMethod method,final Object instance) {
		final ConditionalIgnore annotation = method.getAnnotation(ConditionalIgnore.class);
		return newCondition(annotation, instance);
	}

	private IgnoreCondition newCondition(final ConditionalIgnore annotation, final Object instance) {
		final Class<? extends IgnoreCondition> cond = annotation.condition();
		try {        
			if (cond.isMemberClass()) {
				if (Modifier.isStatic(cond.getModifiers())) {
					return (IgnoreCondition) cond.getDeclaredConstructor(new Class<?>[]{}).newInstance();
				} else if (instance != null && instance.getClass().isAssignableFrom(cond.getDeclaringClass())) {
					return (IgnoreCondition) cond.getDeclaredConstructor(new Class<?>[]{instance.getClass()}).newInstance(instance);
				}
				throw new IllegalArgumentException("Conditional class: " + cond.getName() + " was an inner member class however it was not declared inside the test case using it. Either make this class a static class (by adding static keyword), standalone class (by declaring it in it's own file) or move it inside the test case using it");
			} else {
				return cond.newInstance();
			}
		} catch(RuntimeException re) { 
			throw re;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static class IgnoreStatement extends Statement {
		private IgnoreCondition condition;
		IgnoreStatement(final IgnoreCondition condition) {
			this.condition = condition;
		}
		@Override
		public void evaluate() {
			Assume.assumeTrue("Ignored by " + condition.getClass().getSimpleName(), false);
		}
	}

}