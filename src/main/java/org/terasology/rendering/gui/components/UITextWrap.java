/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.rendering.gui.components;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.vecmath.Vector2f;

import org.lwjgl.opengl.Display;
import org.terasology.logic.manager.PathManager;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.MouseButtonListener;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Simple text element supporting text shadowing.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * 
 * TODO clean this up / write from scratch.
 * TODO this is directly bind to the debug console? If so it should not work this way. Make an abstract widget out of this, which can also be used in other places.
 */
public class UITextWrap extends UIText {

    public final String newLine = System.getProperty("line.separator");
    private long currentpos = 0;

    public UITextWrap() {
        super();
        setup();
    }

    public UITextWrap(String text) {
        super(text);
        setup();

    }

    public UITextWrap(Vector2f position) {
        super(position);
        setup();
    }
    
    private void setup() {
        addMouseButtonListener(new MouseButtonListener() {
            @Override
            public void wheel(UIDisplayElement element, int wheel, boolean intersect) {
                if (wheel != 0) {
                    if (wheel > 0)
                        currentpos++;
                    else
                        currentpos--;
                    
                    try {
                        showFromJson();    //TODO something buggy here :D scroll causes help blockList to be displayed.
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            
            @Override
            public void up(UIDisplayElement element, int button, boolean intersect) {
                
            }
            
            @Override
            public void down(UIDisplayElement element, int button, boolean intersect) {
                
            }
        });
    }

    public void setText(String text) {
        this.text = text;
    }

    public void showFromJson() throws IOException {
        int maxlines = getLineCount();
        int screenlines = getScreenLines();
        long beginpos, endpos, counter;
        if (screenlines > maxlines) {
            beginpos = -1;
        } else {
            if (currentpos < 0) {
                currentpos = 0;
            }
            if (currentpos > maxlines - screenlines) {
                currentpos = maxlines - screenlines;
            }
            beginpos = maxlines - (screenlines + currentpos) - 1;
        }
        endpos = beginpos + screenlines + 1;
        //if(endpos >maxlines){endpos = maxlines +1;}
        counter = 0;
        Gson gson = new Gson();
        String helpFile = PathManager.getInstance().getDataPath() + File.separator + "data" + File.separator + "console" + File.separator + "consolelog.json";
        JsonReader reader = new JsonReader(new FileReader(helpFile));
        reader.beginArray();
        text = "";
        while (reader.hasNext()) {
            if (counter > beginpos && counter < endpos) {
                text += gson.fromJson(reader, String.class);
            } else {
                gson.fromJson(reader, String.class);
            }
            counter++;
        }
        reader.endArray();
        reader.close();

    }

    public void loadHelp() throws IOException {
        Gson gson = new Gson();
        String helpFile = PathManager.getInstance().getDataPath() + File.separator + "data" + File.separator + "console" + File.separator + "help.json";
        JsonReader reader = new JsonReader(new FileReader(helpFile));
        reader.beginArray();
        text = "";
        while (reader.hasNext()) {
            text += gson.fromJson(reader, String.class) + newLine;
        }
        reader.endArray();
        reader.close();
    }

    public void loadError() throws IOException {
        Gson gson = new Gson();
        String helpFile = PathManager.getInstance().getDataPath() + File.separator + "data" + File.separator + "console" + File.separator + "error.json";
        JsonReader reader = new JsonReader(new FileReader(helpFile));
        reader.beginArray();
        text = "";
        while (reader.hasNext()) {
            text += gson.fromJson(reader, String.class) + newLine;
        }
        reader.endArray();
        reader.close();
    }

    public void resetScroll() {
        currentpos = 0;
    }

    public void addText(String addtext) throws IOException {
        String wrappedtext = "";
        int linecounter = 0;
        String[] parts = addtext.split(newLine);
        ArrayList<String> finaltext = new ArrayList<String>();
        int width = Display.getWidth() - 8;
        int charCount = (int) (width / 7);
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].length() > charCount) {
                int endpoint = charCount;
                int beginpoint = 0;
                while (endpoint > beginpoint) {
                    for (int j = endpoint; j > beginpoint; j--) {
                        Character ch = parts[i].charAt(j);
                        if (Character.isSpaceChar(ch)) {
                            endpoint = j;
                            break;
                        } else {
                            switch (ch) {
                                case '.':
                                case '?':
                                case ';':
                                case ':':
                                case '\t':
                                case '!':
                                    endpoint = j;
                                    break;
                            }
                        }
                    }
                    finaltext.add(parts[i].substring(beginpoint, endpoint) + newLine);
                    linecounter++;
                    beginpoint = endpoint + 1;
                    endpoint = beginpoint + charCount;
                    if (endpoint > parts[i].length() - 1) {
                        finaltext.add(parts[i].substring(beginpoint, parts[i].length()) + newLine);
                        linecounter++;
                        endpoint = -1;
                    }
                }
            } else {
                finaltext.add(parts[i] + newLine);
                linecounter++;
            }
        }

        Gson gson = new Gson();
        String helpFile = PathManager.getInstance().getDataPath() + File.separator + "data" + File.separator + "console" + File.separator + "consolelog.json";
        JsonWriter writer = new JsonWriter(new FileWriter(helpFile));
        writer.beginArray();
        Iterator e = finaltext.iterator();
        while (e.hasNext()) {
            gson.toJson(e.next(), String.class, writer);
        }
        writer.endArray();
        writer.close();
    }

    public int getLineCount() throws IOException {
        Gson gson = new Gson();
        String helpFile = PathManager.getInstance().getDataPath() + File.separator + "data" + File.separator + "console" + File.separator + "consolelog.json";
        JsonReader reader = new JsonReader(new FileReader(helpFile));
        int counter = 0;
        reader.beginArray();
        while (reader.hasNext()) {
            counter++;
            gson.fromJson(reader, String.class);
        }
        reader.endArray();
        reader.close();
        return counter;
    }

    private int getScreenLines() {
        int disp = Display.getHeight() - 8 - 70;
        return disp / 16;
    }

    //private String getGsonString(Path path)

}
