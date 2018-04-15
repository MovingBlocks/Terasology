/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.documentation.apiScraper.util;


import java.util.Objects;

/**
 * Saves information about methods and constructors to be used at the ApiComparator class.
 */
public class ApiMethod {


    private String className;
    private String name;
    private String returnType;
    private String exceptionType;
    private String parametersType;

    /**
     * @param className Name of the class in which the method can be found.
     * @param name Name of the method
     * @param returnType Return type of the method
     * @param exceptionType List of exception types of the method
     * @param parametersType List of the method's parameters' type
     */
    public ApiMethod(String className, String name, String returnType, String exceptionType, String parametersType) {
        this.className = className;
        this.name = name;
        this.returnType = returnType;
        this.exceptionType = exceptionType;
        this.parametersType = parametersType;
    }


    public String getClassName() {
        return className;
    }

    public String getName() {
        return name;
    }

    public String getReturnType() {
        return returnType;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public String getParametersType() {
        return parametersType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ApiMethod apiMethod = (ApiMethod) o;


        return getClassName().equals(apiMethod.getClassName())
                && getName().equals(apiMethod.getName())
                && getReturnType().equals(apiMethod.getReturnType())
                && getExceptionType().equals(apiMethod.getExceptionType())
                && getParametersType().equals(apiMethod.getParametersType());
    }

    @Override
    public int hashCode() {
        return Objects.hash();
    }
}
