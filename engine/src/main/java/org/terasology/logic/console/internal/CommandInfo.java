/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.console.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.console.Command;
import org.terasology.logic.console.CommandParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Metadata on a command, including the ability to execute it.
 *
 * Created by Limeth on 9.12.2014.
 */
public class CommandInfo implements Comparable<CommandInfo>
{
	private static final Logger logger = LoggerFactory.getLogger(javax.activation.CommandInfo.class);
	public static final Comparator<CommandInfo> COMPARATOR = new Comparator<CommandInfo>()
	{
		@Override
		public int compare(CommandInfo o1, CommandInfo o2)
		{
			if(!o1.endsWithVarargs() && o2.endsWithVarargs())
				return -1;
			else if(o1.endsWithVarargs() && !o2.endsWithVarargs())
				return 1;

			if(o1.getRequiredParameterCount() > o2.getRequiredParameterCount())
				return -1;
			else if(o1.getRequiredParameterCount() < o2.getRequiredParameterCount())
				return 1;

			return 0;
		}
	};

	private final Method method;
	private final Object provider;
	private CommandParameterInfo[] lazyParameters;
	private String lazyUsage;
	private Boolean lazyClientParameterRequired;

	public CommandInfo(Method method, Object provider)
	{
		Objects.requireNonNull(method, "The method must not be null!");
		Objects.requireNonNull(provider, "The provider must not be null!");

		if(method.getAnnotation(Command.class) == null)
			throw new IllegalArgumentException("The provided method is not annotated by the Command annotation.");

		this.method = method;
		this.provider = provider;
	}

	public String execute(List<String> rawParams, EntityRef sender) throws InvalidCommandCallException
	{
		CommandParameterInfo[] params = getParameters();
		int requiredParameterCount = getRequiredParameterCount();
		Object[] processedParams;

		if(isClientParameterRequired())
		{
			processedParams = new Object[params.length + 1];
			processedParams[processedParams.length - 1] = sender;
		}
		else
			processedParams = new Object[params.length];

		for(int i = 0; i < requiredParameterCount; i++)
		{
			String rawParam = rawParams.get(i);
			CommandParameterInfo param = params[i];
			processedParams[i] = param.getValue(rawParam);
		}

		if(endsWithVarargs())
		{
			CommandParameterInfo param = params[params.length - 1];
			Object varargsResult;

			if(rawParams.size() <= requiredParameterCount)
			{
				Class<?> type = param.getType();
				varargsResult = Array.newInstance(type, 0);
			}
			else
			{
				String rawParam = rawParams.get(requiredParameterCount);

				for(int i = requiredParameterCount + 1; i < rawParams.size(); i++)
					rawParam += " " + rawParams.get(i);

				varargsResult = param.getValue(rawParam);
			}

			processedParams[requiredParameterCount] = varargsResult;
		}

		try
		{
			Object result = method.invoke(provider, processedParams);

			return result == null ? null : result.toString();
		}
		catch(Exception e)
		{
			InvalidCommandCallException newE = new InvalidCommandCallException("An error occurred while executing the command.", e);

			e.printStackTrace();

			throw newE;
		}
	}

	private Command getAnnotation()
	{
		return method.getAnnotation(Command.class);
	}

	private String initUsage()
	{
		String name = getName();
		StringBuilder builder = new StringBuilder(name);

		for(CommandParameterInfo param : getParameters())
			builder.append(' ').append(param.getUsage());

		return lazyUsage = builder.toString();
	}

	public String getUsage()
	{
		return lazyUsage != null ? lazyUsage : initUsage();
	}

	public String getName()
	{
		return method.getName();
	}

	private CommandParameterInfo[] initParameters()
	{
		Class<?>[] types = method.getParameterTypes();
		int paramAmount = types.length + (isClientParameterRequired() ? -1 : 0);
		lazyParameters = CommandParameterInfo.valueOf(method, 0, paramAmount);

		for(int i = 0; i < lazyParameters.length; i++)
		{
			CommandParameterInfo param = lazyParameters[i];

			if(!param.hasName())
				logger.warn("Parameter {} in method {} does not have a CommandParam annotation", i, method);

			if(param.getArrayDelimiter() == Command.ARRAY_DELIMITER_VARARGS && i < lazyParameters.length - 1)
				logger.warn("Parameter {} in method {} uses the varargs delimiter, but is not at the end", i, method);
		}

		return lazyParameters;
	}

	public CommandParameterInfo[] getParameters()
	{
		return lazyParameters != null ? lazyParameters : initParameters();
	}

	/**
	 * @return The required amount of parameters for this command. If the last argument is a {@code varargs}, the argument is not counted.
	 */
	public int getRequiredParameterCount()
	{
		return getParameters().length + (endsWithVarargs() ? -1 : 0);
	}

	public boolean endsWithVarargs()
	{
		CommandParameterInfo[] params = getParameters();

		return params.length > 0 && params[params.length - 1].isVarargs();
	}

	private boolean initClientParameterRequired()
	{
		Class<?>[] types = method.getParameterTypes();

		if(types.length <= 0)
			return false;

		if(types[types.length - 1] != EntityRef.class)
			return false;

		Annotation[] annotations = method.getParameterAnnotations()[types.length - 1];

		for(Annotation annotation : annotations)
			if(annotation.getClass() == CommandParam.class)
				return false;

		return true;
	}

	private boolean isClientParameterRequired()
	{
		return lazyClientParameterRequired != null ? lazyClientParameterRequired
		                                           : (lazyClientParameterRequired = initClientParameterRequired());
	}

	public String getDescription()
	{
		return getAnnotation().shortDescription();
	}

	public String getHelpText()
	{
		return getAnnotation().helpText();
	}

	public boolean isRunOnServer()
	{
		return getAnnotation().runOnServer();
	}

	public String getRequiredPermission()
	{
		return getAnnotation().requiredPermission();
	}

	@Override
	public int compareTo(CommandInfo o)
	{
		return COMPARATOR.compare(this, o);
	}
}
