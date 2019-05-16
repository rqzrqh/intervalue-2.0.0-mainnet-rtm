package one.inve.localfullnode2.utilities.http;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import one.inve.localfullnode2.utilities.PkgUtils;
import one.inve.localfullnode2.utilities.ReflectionUtils;
import one.inve.localfullnode2.utilities.http.annotation.RequestMapper;
import one.inve.localfullnode2.utilities.http.annotation.RequestMatchable;

/**
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 *
 * @author Francis.Deng
 * @date 2018年11月3日 下午3:29:38
 *
 */
class HttpHandlers {
	private List<Class> targetClasses;
	private RequestMatchable matcher;

	private Class requestMapperClass;

	HttpHandlers(String pkgName, Class requestMapperClass) {
		this(pkgName, requestMapperClass, null);
	}

	private HttpHandlers(String pkgName, Class requestMapperClass, RequestMatchable matcher) {
		try {
			Class[] allClasses = PkgUtils.getClasses(pkgName);

			for (Class clazz : allClasses) {
				if (ReflectionUtils.hasAnnotatedMethod(clazz, requestMapperClass)) {
					if (targetClasses == null) {
						targetClasses = new ArrayList<Class>();
					}

					targetClasses.add(clazz);
				}
			}

			this.requestMapperClass = requestMapperClass;
			this.matcher = matcher;
		} catch (ClassNotFoundException | IOException e) {
			newRunTimeException(pkgName + " is incorrect", e);
			e.printStackTrace();
		}
	}

	/**
	 * if actualMatcher is passed and func isCalculationApproved
	 */
	IFunction enrichFunc(HttpServiceImplsDependent dep, RequestMatchable requestMatcher, IFunction func,
			Object... params) {
		RequestMatchable actualMatcher = (requestMatcher != null) ? requestMatcher : matcher;

		for (Class c : targetClasses) {
			List<Method> annotatedMethods = ReflectionUtils.findAnnotatedMethods(c, requestMapperClass);
			Constructor constructor = ReflectionUtils.findConstructor(c, new Class[1]);
			if (constructor == null) { // 参数
				// ensure that http service has at least a constructor without parameter.
				continue;
			}

			for (Method method : annotatedMethods) {
				RequestMapper requestMapper = (RequestMapper) method.getAnnotation(requestMapperClass);

				if (actualMatcher.isMatched(requestMapper)) {
					Object instance = ReflectionUtils.newInstance(constructor, dep);

					if (func.isCalculationApproved(instance, method, params)) {
						return func;
					}
				}
			}
		}

		return null;
	}

	private void newRunTimeException(String des, Throwable t) {
		throw new RuntimeException(des, t);
	}
}