/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package org.terasology.rendering.gui.menus;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.terasology.game.Terasology;
import org.terasology.logic.manager.GroovyHelp;
import org.terasology.rendering.gui.components.UIText;
import org.terasology.rendering.gui.components.UITextWrap;
import org.terasology.rendering.gui.framework.UIScrollableDisplayContainer;

import javax.vecmath.Vector2f;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glPopMatrix;

/**
 * The debug console of Terasology.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class UIDebugConsole extends UIScrollableDisplayContainer {

    private final UIText _consoleText;
    private final UITextWrap _helpText;
    public final String newLine = System.getProperty("line.separator");

    private final StringBuffer _consoleInput = new StringBuffer();
    private final ArrayList<String> _ringBuffer = new ArrayList<String>();
    private final int border = 70;
    private int _ringBufferPos = -1;
    private String _helpContent = "";

    /**
     * Init. a new Terasology console.
     */
    public UIDebugConsole() {
        //setModal(true);
        //setOverlay(true);
        setPosition(new Vector2f(0, 0));
        setSize(new Vector2f(Display.getDisplayMode().getWidth(), Display.getDisplayMode().getHeight()));

        //setScrollBarsPosition(new Vector2f(Display.getWidth() / 2,Display.getHeight() / 2),new Vector2f(0.5f,0.5f));

        _consoleText = new UIText();
        _consoleText.setVisible(true);
        addDisplayElement(_consoleText);

        _helpText = new UITextWrap();
        _helpText.setColor(Color.green);
        //_helpText.setSize(new Vector2f(0.5f,0.5f));
        _helpText.setVisible(true);
        addDisplayElement(_helpText);
    }

    /**
     * Processes the given keyboard input.
     *
     * @param key The key
     */
    public void processKeyboardInput(int key) {
        super.processKeyboardInput(key);

        if (!isVisible())
            return;

        if (key == Keyboard.KEY_BACK) {
            int length = _consoleInput.length() - 1;

            if (length < 0) {
                length = 0;
            }
            _consoleInput.setLength(length);
        } else if (key == Keyboard.KEY_RETURN) {
            processConsoleString();
        } else if (key == Keyboard.KEY_UP) {
            rotateRingBuffer(1);
        } else if (key == Keyboard.KEY_DOWN) {
            rotateRingBuffer(-1);
        }

        char c = Keyboard.getEventCharacter();

        if (c >= 'a' && c < 'z' + 1 || c >= '0' && c < '9' + 1 || c >= 'A' && c < 'Z' + 1 || c == ' ' || c == '_' || c == '.' || c == ',' || c == '!' || c == '-' || c == '(' || c == ')' || c == '"' || c == '\'' || c == ';' || c == '+') {
            _consoleInput.append(c);
        }
    }

    /**
     * Rotates the ring buffer in the given direction.
     *
     * @param dir The direction
     */
    public void rotateRingBuffer(int dir) {
        if (_ringBuffer.isEmpty())
            return;

        _ringBufferPos += dir;

        if (_ringBufferPos < 0)
            _ringBufferPos = _ringBuffer.size() - 1;
        else if (_ringBufferPos > _ringBuffer.size() - 1)
            _ringBufferPos = 0;

        _consoleInput.setLength(0);
        _consoleInput.append(_ringBuffer.get(_ringBufferPos));
    }

    /**
     * Adds a new string to the ring buffer.
     */
    public void addToRingBuffer() {
        _ringBufferPos = -1;
        _ringBuffer.add(0, _consoleInput.toString());
    }

    /**
     * Parses the console string and executes the command.
     */
    public void processConsoleString() {
        int success = 0;
        _helpText.resetScroll();
        try {
            success = Terasology.getInstance().getGroovyManager().runGroovyShell(_consoleInput.toString());
        } catch (Exception e) {
            Terasology.getInstance().getLogger().log(Level.INFO, e.getMessage());
        }
        if (success > 1) {
            _consoleText.setText("");
            return;
        }
        if (success > 0) {
            Terasology.getInstance().initOpenGLParams();
            Terasology.getInstance().getLogger().log(Level.INFO, "Console command \"{0}\" accepted.", _consoleInput);

            addToRingBuffer();
            resetDebugConsole();
            setVisible(false);
        } else {
            _helpContent = "error : Type help for help, help <command> for command specific help";
            Terasology.getInstance().getLogger().log(Level.WARNING, "Console command \"{0}\" is invalid.", _consoleInput);
        }
    }

    public String toString() {
        return _consoleInput.toString();
    }

    /**
     * Resets the debug console.
     */
    public void resetDebugConsole() {
        _consoleInput.setLength(0);
    }

    public void setHelpText(String helptekst) throws IOException
    {
        //_helpContent = helptekst;
        _helpText.addText(helptekst);
        _helpText.loadText();
    }

    public void setHelpText(GroovyHelp groovyhelp) throws IOException
    {
        String tempstring = "";
        //_helpContent = groovyhelp.getCommandName() + " : " + groovyhelp.getCommandDesc() + newLine;
        tempstring += groovyhelp.getCommandName() + " : " + groovyhelp.getCommandDesc() + newLine;
        if (groovyhelp.getParameters().length > 0)
        {
            String parameters = "accepted parameters :" + newLine;
            for(int i=0; i<groovyhelp.getParameters().length;i++)
            {
                parameters += groovyhelp.getParameters()[i] + newLine;
            }
            //_helpContent += parameters  + newLine;
            tempstring += parameters  + newLine;
        }
        //_helpContent += "detailed help : " + groovyhelp.getCommandHelp()  + newLine;
        tempstring += "detailed help : " + groovyhelp.getCommandHelp()  + newLine;
        if (groovyhelp.getExamples().length > 0)
        {
            String examples = "Example(s) :" + newLine;
            for(int i=0; i<groovyhelp.getExamples().length;i++)
            {
                examples += groovyhelp.getExamples()[i] + newLine;
            }
            //_helpContent += examples + newLine;
            tempstring += examples + newLine;
            _helpText.addText(tempstring);
            _helpText.loadText();
        }
    }

    public void showHelp()throws IOException{
        _helpText.loadHelp();
    }

    @Override
    public void update() {
        super.update();

        _consoleText.setText(String.format("%s_", this));
        _consoleText.setPosition(new Vector2f(4, Display.getHeight() - 16 - 4));
        //_helpText.setText(String.format("%s", _helpContent));
        _helpText.setPosition(new Vector2f(4, 12));
    }

    public void render(){
        renderOverlay();
        super.render();
    }

    public void renderOverlay(){
        glPushMatrix();
        glLoadIdentity();
        glColor4f(0, 0, 0, 0.75f);
        glBegin(GL_QUADS);
        glVertex2f(0f, 0f);
        glVertex2f((float) Display.getWidth()-border, 0f);
        glVertex2f((float) Display.getWidth()-border, (float) Display.getHeight()-border);
        glVertex2f(0f, (float) Display.getHeight()-border);
        glEnd();
        glBegin(GL_QUADS);
        glVertex2f(0f, Display.getHeight()-24);
        glVertex2f((float) 300, Display.getHeight()-24);
        glVertex2f((float) 300, (float) Display.getHeight());
        glVertex2f(0f, (float) Display.getHeight());
        glEnd();
        glPopMatrix();
    }
}
