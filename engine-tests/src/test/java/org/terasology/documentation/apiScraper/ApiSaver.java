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
package org.terasology.documentation.apiScraper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;


public final class ApiSaver {

    /**
     * @param args (ignored)
     * @throws Exception if the module environment cannot be loaded
     */
    public static void main(String[] args) throws Exception {
        StringBuffer api = CompleteApiScraper.getApi();
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File("API_file.txt")));
        writer.write(api.toString());
        writer.flush();
        writer.close();
        System.out.println("API file is ready!");
    }
}
