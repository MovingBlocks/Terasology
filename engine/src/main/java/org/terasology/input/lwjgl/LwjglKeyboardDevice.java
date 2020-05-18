/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.input.lwjgl;

import com.google.common.collect.Lists;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.lwjgl.glfw.GLFW;
import org.terasology.input.ButtonState;
import org.terasology.input.Input;
import org.terasology.input.InputType;
import org.terasology.input.Keyboard;
import org.terasology.input.device.KeyboardAction;
import org.terasology.input.device.KeyboardDevice;

import java.util.Queue;

/**
 */
public class LwjglKeyboardDevice implements KeyboardDevice {

    private static final TIntIntMap glfwToTeraMaps = new TIntIntHashMap();

    static {
        //TODO: test and cleanup keys
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_NONE, Keyboard.KeyId.NONE);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_ESCAPE, Keyboard.KeyId.ESCAPE);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_1, Keyboard.KeyId.KEY_1);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_2, Keyboard.KeyId.KEY_2);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_3, Keyboard.KeyId.KEY_3);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_4, Keyboard.KeyId.KEY_4);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_5, Keyboard.KeyId.KEY_5);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_6, Keyboard.KeyId.KEY_6);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_7, Keyboard.KeyId.KEY_7);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_8, Keyboard.KeyId.KEY_8);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_9, Keyboard.KeyId.KEY_9);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_0, Keyboard.KeyId.KEY_0);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_MINUS, Keyboard.KeyId.MINUS);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_EQUAL, Keyboard.KeyId.EQUALS);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_BACKSPACE, Keyboard.KeyId.BACKSPACE);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_TAB, Keyboard.KeyId.TAB);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_Q, Keyboard.KeyId.Q);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_W, Keyboard.KeyId.W);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_E, Keyboard.KeyId.E);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_R, Keyboard.KeyId.R);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_T, Keyboard.KeyId.T);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_Y, Keyboard.KeyId.Y);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_U, Keyboard.KeyId.U);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_I, Keyboard.KeyId.I);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_O, Keyboard.KeyId.O);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_P, Keyboard.KeyId.P);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_LEFT_BRACKET, Keyboard.KeyId.LEFT_BRACKET);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_RIGHT_BRACKET, Keyboard.KeyId.RIGHT_BRACKET);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_ENTER, Keyboard.KeyId.ENTER);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_LEFT_CONTROL, Keyboard.KeyId.LEFT_CTRL);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_A, Keyboard.KeyId.A);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_S, Keyboard.KeyId.S);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_D, Keyboard.KeyId.D);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_F, Keyboard.KeyId.F);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_G, Keyboard.KeyId.G);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_H, Keyboard.KeyId.H);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_J, Keyboard.KeyId.J);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_K, Keyboard.KeyId.K);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_L, Keyboard.KeyId.L);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_SEMICOLON, Keyboard.KeyId.SEMICOLON);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_APOSTROPHE, Keyboard.KeyId.APOSTROPHE);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_GRAVE_ACCENT, Keyboard.KeyId.GRAVE);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_LEFT_SHIFT, Keyboard.KeyId.LEFT_SHIFT);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_BACKSLASH, Keyboard.KeyId.BACKSLASH);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_Z, Keyboard.KeyId.Z);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_X, Keyboard.KeyId.X);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_C, Keyboard.KeyId.C);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_V, Keyboard.KeyId.V);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_B, Keyboard.KeyId.B);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_N, Keyboard.KeyId.N);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_M, Keyboard.KeyId.M);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_COMMA, Keyboard.KeyId.COMMA);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_PERIOD, Keyboard.KeyId.PERIOD);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_SLASH, Keyboard.KeyId.SLASH);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_RIGHT_SHIFT, Keyboard.KeyId.RIGHT_SHIFT);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_KP_MULTIPLY, Keyboard.KeyId.NUMPAD_MULTIPLY);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_LEFT_ALT, Keyboard.KeyId.LEFT_ALT);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_SPACE, Keyboard.KeyId.SPACE);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_CAPS_LOCK, Keyboard.KeyId.CAPS_LOCK);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_F1, Keyboard.KeyId.F1);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_F2, Keyboard.KeyId.F2);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_F3, Keyboard.KeyId.F3);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_F4, Keyboard.KeyId.F4);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_F5, Keyboard.KeyId.F5);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_F6, Keyboard.KeyId.F6);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_F7, Keyboard.KeyId.F7);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_F8, Keyboard.KeyId.F8);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_F9, Keyboard.KeyId.F9);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_F10, Keyboard.KeyId.F10);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_NUM_LOCK, Keyboard.KeyId.NUM_LOCK);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_SCROLL_LOCK, Keyboard.KeyId.SCROLL_LOCK);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_KP_7, Keyboard.KeyId.NUMPAD_7);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_KP_8, Keyboard.KeyId.NUMPAD_8);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_KP_9, Keyboard.KeyId.NUMPAD_9);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_KP_SUBTRACT, Keyboard.KeyId.NUMPAD_MINUS);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_KP_4, Keyboard.KeyId.NUMPAD_4);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_KP_5, Keyboard.KeyId.NUMPAD_5);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_KP_6, Keyboard.KeyId.NUMPAD_6);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_KP_ADD, Keyboard.KeyId.NUMPAD_PLUS);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_KP_1, Keyboard.KeyId.NUMPAD_1);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_KP_2, Keyboard.KeyId.NUMPAD_2);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_KP_3, Keyboard.KeyId.NUMPAD_3);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_KP_0, Keyboard.KeyId.NUMPAD_0);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_KP_DECIMAL, Keyboard.KeyId.NUMPAD_PERIOD);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_F11, Keyboard.KeyId.F11);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_F12, Keyboard.KeyId.F12);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_F13, Keyboard.KeyId.F13);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_F14, Keyboard.KeyId.F14);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_F15, Keyboard.KeyId.F15);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_F16, Keyboard.KeyId.F16);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_F17, Keyboard.KeyId.F17);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_F18, Keyboard.KeyId.F18);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_KANA, Keyboard.KeyId.KANA);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_F19, Keyboard.KeyId.F19);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_CONVERT, Keyboard.KeyId.CONVERT);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_NOCONVERT, Keyboard.KeyId.NOCONVERT);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_YEN, Keyboard.KeyId.YEN);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_KP_EQUAL, Keyboard.KeyId.NUMPAD_EQUALS);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_CIRCUMFLEX, Keyboard.KeyId.CIRCUMFLEX);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_AT, Keyboard.KeyId.AT);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_COLON, Keyboard.KeyId.COLON);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_UNDERLINE, Keyboard.KeyId.UNDERLINE);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_KANJI, Keyboard.KeyId.KANJI);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_STOP, Keyboard.KeyId.STOP);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_AX, Keyboard.KeyId.AX);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_UNLABELED, Keyboard.KeyId.UNLABELED);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_KP_ENTER, Keyboard.KeyId.NUMPAD_ENTER);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_RIGHT_CONTROL, Keyboard.KeyId.RIGHT_CTRL);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_SECTION, Keyboard.KeyId.SECTION);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_KP_COMMA, Keyboard.KeyId.NUMPAD_COMMA);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_KP_DIVIDE, Keyboard.KeyId.NUMPAD_DIVIDE);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_PRINT_SCREEN, Keyboard.KeyId.PRINT_SCREEN);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_RIGHT_ALT, Keyboard.KeyId.RIGHT_ALT);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_FUNCTION, Keyboard.KeyId.FUNCTION);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_PAUSE, Keyboard.KeyId.PAUSE);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_HOME, Keyboard.KeyId.HOME);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_UP, Keyboard.KeyId.UP);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_PAGE_UP, Keyboard.KeyId.PAGE_UP);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_LEFT, Keyboard.KeyId.LEFT);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_RIGHT, Keyboard.KeyId.RIGHT);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_END, Keyboard.KeyId.END);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_DOWN, Keyboard.KeyId.DOWN);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_PAGE_DOWN, Keyboard.KeyId.PAGE_DOWN);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_INSERT, Keyboard.KeyId.INSERT);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_DELETE, Keyboard.KeyId.DELETE);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_CLEAR, Keyboard.KeyId.CLEAR);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_LEFT_SUPER, Keyboard.KeyId.LEFT_META);
        glfwToTeraMaps.put(GLFW.GLFW_KEY_RIGHT_SUPER, Keyboard.KeyId.RIGHT_META);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_APPS, Keyboard.KeyId.APPS);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_POWER, Keyboard.KeyId.POWER);
//        glfwToTeraMaps.put(GLFW.GLFW_KEY_SLEEP, Keyboard.KeyId.SLEEP);
    }

    private Queue<KeyboardAction> queue = Lists.newLinkedList();
    private TIntSet buttonStates = new TIntHashSet();

    public LwjglKeyboardDevice() {
        long window = GLFW.glfwGetCurrentContext();

        //TODO: merge charCallback with keyCallback, or revise key events.
        GLFW.glfwSetKeyCallback(window, this::glfwKeyCallback);
        GLFW.glfwSetCharCallback(window, this::glfwCharCallback);
    }

    private void glfwCharCallback(long window, int chr) {
        queue.offer(new KeyboardAction(Keyboard.Key.NONE, ButtonState.DOWN, (char) chr));
    }

    private void glfwKeyCallback(long window, int key, int scancode, int action, int mods) {
        int teraKey = glfwToTeraMaps.get(key);
        Input input = InputType.KEY.getInput(teraKey);

        ButtonState state;
        if (action == GLFW.GLFW_PRESS) {
            buttonStates.add(teraKey);
            state = ButtonState.DOWN;
        } else if (action == GLFW.GLFW_RELEASE) {
            state = ButtonState.UP;
            buttonStates.remove(teraKey);
        } else /*if (action == GLFW.GLFW_REPEAT)*/ {
            state = ButtonState.REPEAT;
        }

        queue.offer(new KeyboardAction(input, state, (char) 0));
    }

    @Override
    public boolean isKeyDown(int key) {
        return buttonStates.contains(key);
    }

    @Override
    public Queue<KeyboardAction> getInputQueue() {
        Queue<KeyboardAction> keyboardActions = Lists.newLinkedList();
        KeyboardAction action;
        while ((action = queue.poll()) != null) {
            keyboardActions.add(action);
        }
        return keyboardActions;
    }
}
