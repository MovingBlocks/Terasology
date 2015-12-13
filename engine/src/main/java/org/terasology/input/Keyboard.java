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
package org.terasology.input;

import com.google.common.collect.Maps;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.Locale;
import java.util.Map;

/**
 */
public final class Keyboard {
    private Keyboard() {
    }

    public static class KeyId {
        public static final int NONE = 0x00;
        public static final int ESCAPE = 0x01;
        public static final int KEY_1 = 0x02;
        public static final int KEY_2 = 0x03;
        public static final int KEY_3 = 0x04;
        public static final int KEY_4 = 0x05;
        public static final int KEY_5 = 0x06;
        public static final int KEY_6 = 0x07;
        public static final int KEY_7 = 0x08;
        public static final int KEY_8 = 0x09;
        public static final int KEY_9 = 0x0A;
        public static final int KEY_0 = 0x0B;
        public static final int MINUS = 0x0C;
        public static final int EQUALS = 0x0D;
        public static final int BACKSPACE = 0x0E;
        public static final int TAB = 0x0F;
        public static final int Q = 0x10;
        public static final int W = 0x11;
        public static final int E = 0x12;
        public static final int R = 0x13;
        public static final int T = 0x14;
        public static final int Y = 0x15;
        public static final int U = 0x16;
        public static final int I = 0x17;
        public static final int O = 0x18;
        public static final int P = 0x19;
        public static final int LEFT_BRACKET = 0x1A;
        public static final int RIGHT_BRACKET = 0x1B;
        public static final int ENTER = 0x1C;
        public static final int LEFT_CTRL = 0x1D;
        public static final int A = 0x1E;
        public static final int S = 0x1F;
        public static final int D = 0x20;
        public static final int F = 0x21;
        public static final int G = 0x22;
        public static final int H = 0x23;
        public static final int J = 0x24;
        public static final int K = 0x25;
        public static final int L = 0x26;
        public static final int SEMICOLON = 0x27;
        public static final int APOSTROPHE = 0x28;
        public static final int GRAVE = 0x29;
        public static final int LEFT_SHIFT = 0x2A;
        public static final int BACKSLASH = 0x2B;
        public static final int Z = 0x2C;
        public static final int X = 0x2D;
        public static final int C = 0x2E;
        public static final int V = 0x2F;
        public static final int B = 0x30;
        public static final int N = 0x31;
        public static final int M = 0x32;
        public static final int COMMA = 0x33;
        public static final int PERIOD = 0x34;
        public static final int SLASH = 0x35;
        public static final int RIGHT_SHIFT = 0x36;
        public static final int NUMPAD_MULTIPLY = 0x37;
        public static final int LEFT_ALT = 0x38;
        public static final int SPACE = 0x39;
        public static final int CAPS_LOCK = 0x3A;
        public static final int F1 = 0x3B;
        public static final int F2 = 0x3C;
        public static final int F3 = 0x3D;
        public static final int F4 = 0x3E;
        public static final int F5 = 0x3F;
        public static final int F6 = 0x40;
        public static final int F7 = 0x41;
        public static final int F8 = 0x42;
        public static final int F9 = 0x43;
        public static final int F10 = 0x44;
        public static final int NUM_LOCK = 0x45;
        public static final int SCROLL_LOCK = 0x46;
        public static final int NUMPAD_7 = 0x47;
        public static final int NUMPAD_8 = 0x48;
        public static final int NUMPAD_9 = 0x49;
        public static final int NUMPAD_MINUS = 0x4A;
        public static final int NUMPAD_4 = 0x4B;
        public static final int NUMPAD_5 = 0x4C;
        public static final int NUMPAD_6 = 0x4D;
        public static final int NUMPAD_PLUS = 0x4E;
        public static final int NUMPAD_1 = 0x4F;
        public static final int NUMPAD_2 = 0x50;
        public static final int NUMPAD_3 = 0x51;
        public static final int NUMPAD_0 = 0x52;
        public static final int NUMPAD_PERIOD = 0x53;
        public static final int F11 = 0x57;
        public static final int F12 = 0x58;
        public static final int F13 = 0x64;
        public static final int F14 = 0x65;
        public static final int F15 = 0x66;
        public static final int F16 = 0x67;
        public static final int F17 = 0x68;
        public static final int F18 = 0x69;
        public static final int KANA = 0x70;
        public static final int F19 = 0x71;
        public static final int CONVERT = 0x79;
        public static final int NOCONVERT = 0x7B;
        public static final int YEN = 0x7D;
        public static final int NUMPAD_EQUALS = 0x8D;
        public static final int CIRCUMFLEX = 0x90;
        public static final int AT = 0x91;
        public static final int COLON = 0x92;
        public static final int UNDERLINE = 0x93;
        public static final int KANJI = 0x94;
        public static final int STOP = 0x95;
        public static final int AX = 0x96;
        public static final int UNLABELED = 0x97;
        public static final int NUMPAD_ENTER = 0x9C;
        public static final int RIGHT_CTRL = 0x9D;
        public static final int SECTION = 0xA7;
        public static final int NUMPAD_COMMA = 0xB3;
        public static final int NUMPAD_DIVIDE = 0xB5;
        public static final int PRINT_SCREEN = 0xB7;
        public static final int RIGHT_ALT = 0xB8;
        public static final int FUNCTION = 0xC4;
        public static final int PAUSE = 0xC5;
        public static final int HOME = 0xC7;
        public static final int UP = 0xC8;
        public static final int PAGE_UP = 0xC9;
        public static final int LEFT = 0xCB;
        public static final int RIGHT = 0xCD;
        public static final int END = 0xCF;
        public static final int DOWN = 0xD0;
        public static final int PAGE_DOWN = 0xD1;
        public static final int INSERT = 0xD2;
        public static final int DELETE = 0xD3;
        public static final int CLEAR = 0xDA;
        public static final int LEFT_META = 0xDB;
        public static final int RIGHT_META = 0xDC;
        public static final int APPS = 0xDD;
        public static final int POWER = 0xDE;
        public static final int SLEEP = 0xDF;
    }

    public enum Key implements Input {
        NONE(KeyId.NONE, "KEY_NONE", ""),
        ESCAPE(KeyId.ESCAPE, "KEY_ESCAPE", "Escape"),
        KEY_1(KeyId.KEY_1, "KEY_1", "1"),
        KEY_2(KeyId.KEY_2, "KEY_2", "2"),
        KEY_3(KeyId.KEY_3, "KEY_3", "3"),
        KEY_4(KeyId.KEY_4, "KEY_4", "4"),
        KEY_5(KeyId.KEY_5, "KEY_5", "5"),
        KEY_6(KeyId.KEY_6, "KEY_6", "6"),
        KEY_7(KeyId.KEY_7, "KEY_7", "7"),
        KEY_8(KeyId.KEY_8, "KEY_8", "8"),
        KEY_9(KeyId.KEY_9, "KEY_9", "9"),
        KEY_0(KeyId.KEY_0, "KEY_0", "0"),
        MINUS(KeyId.MINUS, "KEY_MINUS", "-"),
        EQUALS(KeyId.EQUALS, "KEY_EQUALS", "="),
        BACKSPACE(KeyId.BACKSPACE, "KEY_BACK", "Backspace"),
        TAB(KeyId.TAB, "KEY_TAB", "Tab"),
        Q(KeyId.Q, "KEY_Q", "Q"),
        W(KeyId.W, "KEY_W", "W"),
        E(KeyId.E, "KEY_E", "E"),
        R(KeyId.R, "KEY_R", "R"),
        T(KeyId.T, "KEY_T", "T"),
        Y(KeyId.Y, "KEY_Y", "Y"),
        U(KeyId.U, "KEY_U", "U"),
        I(KeyId.I, "KEY_I", "I"),
        O(KeyId.O, "KEY_O", "O"),
        P(KeyId.P, "KEY_P", "P"),
        LEFT_BRACKET(KeyId.LEFT_BRACKET, "KEY_LBRACKET", "["),
        RIGHT_BRACKET(KeyId.RIGHT_BRACKET, "KEY_RBRACKET", "]"),
        ENTER(KeyId.ENTER, "KEY_RETURN", "Enter"),
        LEFT_CTRL(KeyId.LEFT_CTRL, "KEY_LCONTROL", "Left Ctrl"),
        A(KeyId.A, "KEY_A", "A"),
        S(KeyId.S, "KEY_S", "S"),
        D(KeyId.D, "KEY_D", "D"),
        F(KeyId.F, "KEY_F", "F"),
        G(KeyId.G, "KEY_G", "G"),
        H(KeyId.H, "KEY_H", "H"),
        J(KeyId.J, "KEY_J", "J"),
        K(KeyId.K, "KEY_K", "K"),
        L(KeyId.L, "KEY_L", "L"),
        SEMICOLON(KeyId.SEMICOLON, "KEY_SEMICOLON", ";"),
        APOSTROPHE(KeyId.APOSTROPHE, "KEY_APOSTROPHE", "'"),
        GRAVE(KeyId.GRAVE, "KEY_GRAVE", "Grave"),
        LEFT_SHIFT(KeyId.LEFT_SHIFT, "KEY_LSHIFT", "Left Shift"),
        BACKSLASH(KeyId.BACKSLASH, "KEY_BACKSLASH", "\\"),
        Z(KeyId.Z, "KEY_Z", "Z"),
        X(KeyId.X, "KEY_X", "X"),
        C(KeyId.C, "KEY_C", "C"),
        V(KeyId.V, "KEY_V", "V"),
        B(KeyId.B, "KEY_B", "B"),
        N(KeyId.N, "KEY_N", "N"),
        M(KeyId.M, "KEY_M", "M"),
        COMMA(KeyId.COMMA, "KEY_COMMA", ","),
        PERIOD(KeyId.PERIOD, "KEY_PERIOD", "."),
        SLASH(KeyId.SLASH, "KEY_SLASH", "/"),
        RIGHT_SHIFT(KeyId.RIGHT_SHIFT, "KEY_RSHIFT", "Right Shift"),
        NUMPAD_MULTIPLY(KeyId.NUMPAD_MULTIPLY, "KEY_MULTIPLY", "Numpad *"),
        LEFT_ALT(KeyId.LEFT_ALT, "KEY_LMENU", "Left Alt"),
        SPACE(KeyId.SPACE, "KEY_SPACE", "Space"),
        CAPS_LOCK(KeyId.CAPS_LOCK, "KEY_CAPITAL", "Caps Lock"),
        F1(KeyId.F1, "KEY_F1", "F1"),
        F2(KeyId.F2, "KEY_F2", "F2"),
        F3(KeyId.F3, "KEY_F3", "F3"),
        F4(KeyId.F4, "KEY_F4", "F4"),
        F5(KeyId.F5, "KEY_F5", "F5"),
        F6(KeyId.F6, "KEY_F6", "F6"),
        F7(KeyId.F7, "KEY_F7", "F7"),
        F8(KeyId.F8, "KEY_F8", "F8"),
        F9(KeyId.F9, "KEY_F9", "F9"),
        F10(KeyId.F10, "KEY_F10", "F10"),
        NUM_LOCK(KeyId.NUM_LOCK, "KEY_NUMLOCK", "Num Lock"),
        SCROLL_LOCK(KeyId.SCROLL_LOCK, "KEY_SCROLL", "Scroll Lock"),
        NUMPAD_7(KeyId.NUMPAD_7, "KEY_NUMPAD7", "Numpad 7"),
        NUMPAD_8(KeyId.NUMPAD_8, "KEY_NUMPAD8", "Numpad 8"),
        NUMPAD_9(KeyId.NUMPAD_9, "KEY_NUMPAD9", "Numpad 9"),
        NUMPAD_MINUS(KeyId.NUMPAD_MINUS, "KEY_SUBTRACT", "Numpad -"),
        NUMPAD_4(KeyId.NUMPAD_4, "KEY_NUMPAD4", "Numpad 4"),
        NUMPAD_5(KeyId.NUMPAD_5, "KEY_NUMPAD5", "Numpad 5"),
        NUMPAD_6(KeyId.NUMPAD_6, "KEY_NUMPAD6", "Numpad 6"),
        NUMPAD_PLUS(KeyId.NUMPAD_PLUS, "KEY_ADD", "Numpad +"),
        NUMPAD_1(KeyId.NUMPAD_1, "KEY_NUMPAD1", "Numpad 1"),
        NUMPAD_2(KeyId.NUMPAD_2, "KEY_NUMPAD2", "Numpad 2"),
        NUMPAD_3(KeyId.NUMPAD_3, "KEY_NUMPAD3", "Numpad 3"),
        NUMPAD_0(KeyId.NUMPAD_0, "KEY_NUMPAD0", "Numpad 0"),
        NUMPAD_PERIOD(KeyId.NUMPAD_PERIOD, "KEY_DECIMAL", "Numpad ."),
        F11(KeyId.F11, "KEY_F11", "F11"),
        F12(KeyId.F12, "KEY_F12", "F12"),
        F13(KeyId.F13, "KEY_F13", "F13"),
        F14(KeyId.F14, "KEY_F14", "F14"),
        F15(KeyId.F15, "KEY_F15", "F15"),
        F16(KeyId.F16, "KEY_F16", "F16"),
        F17(KeyId.F17, "KEY_F17", "F17"),
        F18(KeyId.F18, "KEY_F18", "F18"),
        KANA(KeyId.KANA, "KEY_KANA", "Kana"), // Japanese Keyboard key (for switching between roman and japanese characters?
        F19(KeyId.F19, "KEY_F19", "F19"),
        CONVERT(KeyId.CONVERT, "KEY_CONVERT", "Convert"), // Japanese Keyboard key (for converting Hiragana characters to Kanji?)
        NOCONVERT(KeyId.NOCONVERT, "KEY_NOCONVERT", "No Convert"), // Japanese Keyboard key
        YEN(KeyId.YEN, "KEY_YEN", "\u00A5"), // Japanese keyboard key for yen
        NUMPAD_EQUALS(KeyId.NUMPAD_EQUALS, "KEY_NUMPADEQUALS", "Numpad ="),
        CIRCUMFLEX(KeyId.CIRCUMFLEX, "KEY_CIRCUMFLEX", "^"), // Japanese keyboard
        AT(KeyId.AT, "KEY_AT", "@"), // (NEC PC98)
        COLON(KeyId.COLON, "KEY_COLON", ":"), // (NEC PC98)
        UNDERLINE(KeyId.UNDERLINE, "KEY_UNDERLINE", "_"), // (NEC PC98)
        KANJI(KeyId.KANJI, "KEY_KANJI", "Kanji"), // (Japanese keyboard)
        STOP(KeyId.STOP, "KEY_STOP", "Stop"), // (NEC PC98)
        AX(KeyId.AX, "KEY_AX", "AX"), // (Japan AX)
        UNLABELED(KeyId.UNLABELED, "KEY_UNLABELED", "Unlabelled"), // (J3100) (a mystery button?)
        NUMPAD_ENTER(KeyId.NUMPAD_ENTER, "KEY_NUMPADENTER", "Numpad Enter"),
        RIGHT_CTRL(KeyId.RIGHT_CTRL, "KEY_RCONTROL", "Right Ctrl"),
        SECTION(KeyId.SECTION, "KEY_SECTION", "\u00A7"),
        NUMPAD_COMMA(KeyId.NUMPAD_COMMA, "KEY_NUMPADCOMMA", "Numpad ,"), // (NEC PC98)
        NUMPAD_DIVIDE(KeyId.NUMPAD_DIVIDE, "KEY_DIVIDE", "Numpad /"),
        PRINT_SCREEN(KeyId.PRINT_SCREEN, "KEY_SYSRQ", "Print Screen"),
        RIGHT_ALT(KeyId.RIGHT_ALT, "KEY_RMENU", "Right Alt"),
        FUNCTION(KeyId.FUNCTION, "KEY_FUNCTION", "Function"),
        PAUSE(KeyId.PAUSE, "KEY_PAUSE", "Pause"),
        HOME(KeyId.HOME, "KEY_HOME", "Home"),
        UP(KeyId.UP, "KEY_UP", "Up"),
        PAGE_UP(KeyId.PAGE_UP, "KEY_PRIOR", "Page Up"),
        LEFT(KeyId.LEFT, "KEY_LEFT", "Left"),
        RIGHT(KeyId.RIGHT, "KEY_RIGHT", "Right"),
        END(KeyId.END, "KEY_END", "End"),
        DOWN(KeyId.DOWN, "KEY_DOWN", "Down"),
        PAGE_DOWN(KeyId.PAGE_DOWN, "KEY_NEXT", "Page Down"),
        INSERT(KeyId.INSERT, "KEY_INSERT", "Insert"),
        DELETE(KeyId.DELETE, "KEY_DELETE", "Delete"),
        CLEAR(KeyId.CLEAR, "KEY_CLEAR", "Clear"), // (Mac)
        LEFT_META(KeyId.LEFT_META, "KEY_LMETA", "Left Meta"), // Left Windows/Option key
        RIGHT_META(KeyId.RIGHT_META, "KEY_RMETA", "Right Meta"), // Right Windows/Option key
        APPS(KeyId.APPS, "KEY_APPS", "Apps"),
        POWER(KeyId.POWER, "KEY_POWER", "Power"),
        SLEEP(KeyId.SLEEP, "KEY_SLEEP", "Sleep");

        private static Map<String, Input> lookupByName;
        private static TIntObjectMap<Input> lookupById;

        private int id;
        private String name;
        private String displayName;

        static {
            lookupByName = Maps.newHashMapWithExpectedSize(Key.values().length);
            lookupById = new TIntObjectHashMap<>(Key.values().length);
            for (Key key : Key.values()) {
                lookupByName.put(key.getName(), key);
                lookupById.put(key.getId(), key);
            }
        }

        private Key(int id, String name, String displayName) {
            this.id = id;
            this.name = name;
            this.displayName = displayName;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public InputType getType() {
            return InputType.KEY;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return name;
        }

        public static Input find(String name) {
            return lookupByName.get(name.toUpperCase(Locale.ENGLISH));
        }

        public static Input find(int id) {
            Input result = lookupById.get(id);
            if (result == null) {
                result = new UnknownInput(InputType.KEY, id);
                lookupById.put(id, result);
                lookupByName.put(result.getName(), result);
            }
            return result;
        }
    }
}

