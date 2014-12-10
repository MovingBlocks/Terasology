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

import org.terasology.logic.console.Command;
import org.terasology.logic.console.CommandParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by Limeth on 9.12.2014.
 */
public class CommandParameterInfo
{
	private final String name;
	private final Class<?> type;
	private final char arrayDelimiter;

	public CommandParameterInfo(String name, Class<?> type, char arrayDelimiter)
	{
		Objects.requireNonNull(type, "The parameter type must not be null!");

		this.name = name;
		this.type = type;
		this.arrayDelimiter = arrayDelimiter;
	}

	public static CommandParameterInfo[] valueOf(Method method, int paramOffset, int paramLength)
	{
		CommandParameterInfo[] result = new CommandParameterInfo[paramLength];
		Annotation[][] paramAnnotations = method.getParameterAnnotations();
		Class<?>[] types = method.getParameterTypes();

		for(int i = 0; i < paramLength; i++)
		{
			int paramIndex = paramOffset + i;
			Annotation[] currentAnnotations = paramAnnotations[paramIndex];
			Class<?> type = types[paramIndex];
			String name = null;
			Character arrayDelimiter = Command.ARRAY_DELIMITER_DEFAULT;

			for(Annotation annotation : currentAnnotations)
				if(annotation instanceof CommandParam)
				{
					CommandParam paramAnnotation = (CommandParam) annotation;
					name = paramAnnotation.value();
					arrayDelimiter = paramAnnotation.arrayDelimiter();

					break;
				}

			result[i] = new CommandParameterInfo(name, type, arrayDelimiter);
		}

		return result;
	}

	public Object getValue(String param) throws InvalidCommandCallException
	{
		try
		{
			if(!isArray())
				return parseSingle(unescape(param, false).get(0));
			else
			{
				ArrayList<String> params = unescape(param, true);
				Object array = Array.newInstance(getType(), params.size());

				for(int i = 0; i < Array.getLength(array); i++)
					Array.set(array, i, parseSingle(params.get(i)));

				return array;
			}
		}
		catch(CommandParameterParseException e)
		{
			String warning = "Invalid parameter '" + e.getParameter() + "'";
			String message = e.getMessage();

			if(message != null)
				warning += ": " + message;

			throw new InvalidCommandCallException(warning);
		}
		catch(Exception e)
		{
			InvalidCommandCallException newE = new InvalidCommandCallException("An unexpected error occurred while executing the command.", e);

			e.printStackTrace();

			throw newE;
		}
	}

	private ArrayList<String> unescape(String rawParameter, boolean split) throws CommandParameterParseException
	{
		String string = rawParameter;
		ArrayList<String> params = new ArrayList<String>();

		for(int i = 0; i < string.length(); i++)
		{
			char c = string.charAt(i);

			if(c == Command.ARRAY_DELIMITER_ESCAPE_CHARACTER)
			{
				if(i >= string.length() - 1)
					throw new CommandParameterParseException("The command parameter must not end with an escape character.", rawParameter);

				string = string.substring(0, i) + string.substring(i + 1);
				char following = string.charAt(i);

				if(following != Command.ARRAY_DELIMITER_ESCAPE_CHARACTER && (!split || following != arrayDelimiter))
					throw new CommandParameterParseException("Character '" + following + "' cannot be escaped.", rawParameter);

				continue;
			}

			if(split && c == arrayDelimiter)
			{
				String param = string.substring(0, i);
				string = string.substring(i + 1);
				i = -1;

				params.add(param);
			}
		}

		if(string.length() > 0)
			params.add(string);

		return params;
	}

	public Object parseSingle(String string) throws CommandParameterParseException
	{
		//TODO Add a proper, extensible parsing system instead of hardcoding it
		try
		{
			Class<?> type = getType();

			if(type == Long.TYPE)
				return Long.parseLong(string);
			else if(type == Integer.TYPE)
				return Integer.parseInt(string);
			else if(type == Short.TYPE)
				return Short.parseShort(string);
			else if(type == Byte.TYPE)
				return Byte.parseByte(string);
			else if(type == Double.TYPE)
				return Double.parseDouble(string);
			else if(type == Float.TYPE)
				return Float.parseFloat(string);
			else if(type == Character.TYPE)
				return Character.valueOf((char) Integer.parseInt(string));
			else if(type == String.class)
				return string;
		}
		catch(Exception e) {}

		throw new CommandParameterParseException("Cannot parse a " + getType().getCanonicalName(), string);
	}

	public boolean isEscaped(String string, int charIndex, boolean trail)
	{
		return charIndex - 1 >= 0 && string.charAt(charIndex - 1) == Command.ARRAY_DELIMITER_ESCAPE_CHARACTER
		       && (!trail || !isEscaped(string, charIndex - 1, true));
	}

	public String getUsage()
	{
		String typeString = getType().getSimpleName();

		if(isArray())
			typeString += getArrayDelimiter() + typeString;

		return "<" + typeString + (hasName() ? " " + getName() : "") + ">";
	}

	public boolean isArray()
	{
		return type.isArray();
	}

	public boolean isVarargs()
	{
		return isArray() && getArrayDelimiter() == Command.ARRAY_DELIMITER_VARARGS;
	}

	public char getArrayDelimiter()
	{
		return arrayDelimiter;
	}

	public boolean hasName()
	{
		return name != null;
	}

	public String getName()
	{
		return name;
	}

	public Class<?> getType()
	{
		if(type.isArray())
			return type.getComponentType();

		return type;
	}
}
