/*
 * Copyright 2017 MovingBlocks
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


public class ApiMethod {

    //public static final String DEFAULT_VALUE = "DEFAULT";

    private String className;
    private String name;
    private String returnType;
    private String exceptionType;
    private String parametersType;

    public ApiMethod(String className, String name, String returnType, String exceptionType, String parametersType){
        this.className = className;
        this.name = name;
        this.returnType = returnType;
        this.exceptionType = exceptionType;
        this.parametersType = parametersType;
    }

    /*public ApiMethod(){
        this(DEFAULT_VALUE,DEFAULT_VALUE,DEFAULT_VALUE,DEFAULT_VALUE,DEFAULT_VALUE);
    }*/

    public String getClassName(){
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ApiMethod apiMethod = (ApiMethod) o;

        if (!getClassName().equals(apiMethod.getClassName())) return false;
        if (!getName().equals(apiMethod.getName())) return false;
        if (!getReturnType().equals(apiMethod.getReturnType())) return false;
        if (!getExceptionType().equals(apiMethod.getExceptionType())) return false;
        return getParametersType().equals(apiMethod.getParametersType());
    }

    @Override
    public int hashCode() {
        int result = getClassName().hashCode();
        result = 31 * result + getName().hashCode();
        result = 31 * result + getReturnType().hashCode();
        result = 31 * result + getExceptionType().hashCode();
        result = 31 * result + getParametersType().hashCode();
        return result;
    }
}
