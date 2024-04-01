package org.lwjgl.fusion.glfw;

import java.nio.ByteBuffer;

import org.lwjgl.fusion.input.KeyboardImplementation;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.EventQueue;

/**
 * @author Zarzelcow
 * @created 28/09/2022 - 2:14 PM
 */
public class GLFWKeyboardImplementation implements KeyboardImplementation {
    private GLFWKeyCallback keyCallback;
    private GLFWCharCallback charCallback;
    private long windowHandle;

    private final byte[] key_down_buffer = new byte[Keyboard.KEYBOARD_SIZE];
    private final EventQueue event_queue = new EventQueue(Keyboard.EVENT_SIZE);

    private final ByteBuffer tmp_event = ByteBuffer.allocate(Keyboard.EVENT_SIZE);

    @Override
    public void createKeyboard() {
        // FIXME: NEVER DESTROY MOUSE OR KEYBOARD BECAUSE LINUX SEEMS TO BE UNABLE TO FREE CALLBACK POINTERS PROPERLY
        if(keyCallback != null) {
            return;
        }
        this.keyCallback = GLFWKeyCallback.create((window, glfwKey, scancode, action, mods) -> {
        	
            int key = translateKeyFromGLFW(glfwKey);
            
            if (action == GLFW.GLFW_PRESS) {
                this.key_down_buffer[key] = 1;
            } else if (action == GLFW.GLFW_RELEASE) {
                this.key_down_buffer[key] = 0;
            }
            
            putKeyboardEvent(key, this.key_down_buffer[key], 0, System.nanoTime(), action == GLFW.GLFW_REPEAT);
        });

        this.charCallback = GLFWCharCallback.create((window, codepoint) -> {
            // if the keycode is 0 minecraft instead uses the character code as the key pressed, not sure why
            // but a keycode of -1 is used instead to fix this issue
            putKeyboardEvent(-1, (byte) 1, codepoint, System.nanoTime(), false);
        });

        this.windowHandle = Display.getHandle();
        GLFW.glfwSetKeyCallback(this.windowHandle, this.keyCallback);
        GLFW.glfwSetCharCallback(this.windowHandle, this.charCallback);
    }

    private void putKeyboardEvent(int keycode, byte state, int ch, long nanos, boolean repeat) {
    	
		if (keycode == -1) {
			ByteBuffer lastEvent = event_queue.getLastEvent();

			if (lastEvent.getInt(0) > 0 && lastEvent.getInt(5) == 0) {
				lastEvent.putInt(5, ch);
				return;
			}
		}
		
        this.tmp_event.clear();
        this.tmp_event.putInt(keycode).put(state).putInt(ch).putLong(nanos).put(repeat ? (byte)1 : (byte)0);
        this.tmp_event.flip();
        this.event_queue.putEvent(this.tmp_event);
    }

    @Override
    public void destroyKeyboard() {
        // this.keyCallback.free();
        // this.charCallback.free();
    }

    @Override
    public void pollKeyboard(ByteBuffer keyDownBuffer) {
        int old_position = keyDownBuffer.position();
        keyDownBuffer.put(this.key_down_buffer);
        keyDownBuffer.position(old_position);
    }

    @Override
    public void readKeyboard(ByteBuffer readBuffer) {
        event_queue.copyEvents(readBuffer);
    }
    
    private static final int[] GLFW2LWJGL = new int[Keyboard.KEYBOARD_SIZE];

    public static int translateKeyFromGLFW(int key) {
        if (key == -1) {
        	return GLFW2LWJGL[0];
        } else if (key < GLFW2LWJGL.length) {
        	return GLFW2LWJGL[key];
        } else {
        	return key;
        }
    }
    
    static {
        GLFW2LWJGL[0x00] = Keyboard.KEY_NONE;
        GLFW2LWJGL[GLFW.GLFW_KEY_SPACE] = Keyboard.KEY_SPACE;
        GLFW2LWJGL[GLFW.GLFW_KEY_APOSTROPHE] = Keyboard.KEY_APOSTROPHE;
        GLFW2LWJGL[GLFW.GLFW_KEY_COMMA] = Keyboard.KEY_COMMA;
        GLFW2LWJGL[GLFW.GLFW_KEY_MINUS] = Keyboard.KEY_MINUS;
        GLFW2LWJGL[GLFW.GLFW_KEY_PERIOD] = Keyboard.KEY_PERIOD;
        GLFW2LWJGL[GLFW.GLFW_KEY_SLASH] = Keyboard.KEY_SLASH;
        GLFW2LWJGL[GLFW.GLFW_KEY_0] = Keyboard.KEY_0;
        GLFW2LWJGL[GLFW.GLFW_KEY_1] = Keyboard.KEY_1;
        GLFW2LWJGL[GLFW.GLFW_KEY_2] = Keyboard.KEY_2;
        GLFW2LWJGL[GLFW.GLFW_KEY_3] = Keyboard.KEY_3;
        GLFW2LWJGL[GLFW.GLFW_KEY_4] = Keyboard.KEY_4;
        GLFW2LWJGL[GLFW.GLFW_KEY_5] = Keyboard.KEY_5;
        GLFW2LWJGL[GLFW.GLFW_KEY_6] = Keyboard.KEY_6;
        GLFW2LWJGL[GLFW.GLFW_KEY_7] = Keyboard.KEY_7;
        GLFW2LWJGL[GLFW.GLFW_KEY_8] = Keyboard.KEY_8;
        GLFW2LWJGL[GLFW.GLFW_KEY_9] = Keyboard.KEY_9;
        GLFW2LWJGL[GLFW.GLFW_KEY_SEMICOLON] = Keyboard.KEY_SEMICOLON;
        GLFW2LWJGL[GLFW.GLFW_KEY_EQUAL] = Keyboard.KEY_EQUALS;
        GLFW2LWJGL[GLFW.GLFW_KEY_A] = Keyboard.KEY_A;
        GLFW2LWJGL[GLFW.GLFW_KEY_B] = Keyboard.KEY_B;
        GLFW2LWJGL[GLFW.GLFW_KEY_C] = Keyboard.KEY_C;
        GLFW2LWJGL[GLFW.GLFW_KEY_D] = Keyboard.KEY_D;
        GLFW2LWJGL[GLFW.GLFW_KEY_E] = Keyboard.KEY_E;
        GLFW2LWJGL[GLFW.GLFW_KEY_F] = Keyboard.KEY_F;
        GLFW2LWJGL[GLFW.GLFW_KEY_G] = Keyboard.KEY_G;
        GLFW2LWJGL[GLFW.GLFW_KEY_H] = Keyboard.KEY_H;
        GLFW2LWJGL[GLFW.GLFW_KEY_I] = Keyboard.KEY_I;
        GLFW2LWJGL[GLFW.GLFW_KEY_J] = Keyboard.KEY_J;
        GLFW2LWJGL[GLFW.GLFW_KEY_K] = Keyboard.KEY_K;
        GLFW2LWJGL[GLFW.GLFW_KEY_L] = Keyboard.KEY_L;
        GLFW2LWJGL[GLFW.GLFW_KEY_M] = Keyboard.KEY_M;
        GLFW2LWJGL[GLFW.GLFW_KEY_N] = Keyboard.KEY_N;
        GLFW2LWJGL[GLFW.GLFW_KEY_O] = Keyboard.KEY_O;
        GLFW2LWJGL[GLFW.GLFW_KEY_P] = Keyboard.KEY_P;
        GLFW2LWJGL[GLFW.GLFW_KEY_Q] = Keyboard.KEY_Q;
        GLFW2LWJGL[GLFW.GLFW_KEY_R] = Keyboard.KEY_R;
        GLFW2LWJGL[GLFW.GLFW_KEY_S] = Keyboard.KEY_S;
        GLFW2LWJGL[GLFW.GLFW_KEY_T] = Keyboard.KEY_T;
        GLFW2LWJGL[GLFW.GLFW_KEY_U] = Keyboard.KEY_U;
        GLFW2LWJGL[GLFW.GLFW_KEY_V] = Keyboard.KEY_V;
        GLFW2LWJGL[GLFW.GLFW_KEY_W] = Keyboard.KEY_W;
        GLFW2LWJGL[GLFW.GLFW_KEY_X] = Keyboard.KEY_X;
        GLFW2LWJGL[GLFW.GLFW_KEY_Y] = Keyboard.KEY_Y;
        GLFW2LWJGL[GLFW.GLFW_KEY_Z] = Keyboard.KEY_Z;
        GLFW2LWJGL[GLFW.GLFW_KEY_LEFT_BRACKET] = Keyboard.KEY_LBRACKET;
        GLFW2LWJGL[GLFW.GLFW_KEY_BACKSLASH] = Keyboard.KEY_BACKSLASH;
        GLFW2LWJGL[GLFW.GLFW_KEY_RIGHT_BRACKET] = Keyboard.KEY_RBRACKET;
        GLFW2LWJGL[GLFW.GLFW_KEY_GRAVE_ACCENT] = Keyboard.KEY_GRAVE;
        GLFW2LWJGL[GLFW.GLFW_KEY_WORLD_1] = Keyboard.KEY_WORLD_1;
        GLFW2LWJGL[GLFW.GLFW_KEY_WORLD_2] = Keyboard.KEY_WORLD_2;
        GLFW2LWJGL[GLFW.GLFW_KEY_ESCAPE] = Keyboard.KEY_ESCAPE;
        GLFW2LWJGL[GLFW.GLFW_KEY_ENTER] = Keyboard.KEY_RETURN;
        GLFW2LWJGL[GLFW.GLFW_KEY_TAB] = Keyboard.KEY_TAB;
        GLFW2LWJGL[GLFW.GLFW_KEY_BACKSPACE] = Keyboard.KEY_BACK;
        GLFW2LWJGL[GLFW.GLFW_KEY_INSERT] = Keyboard.KEY_INSERT;
        GLFW2LWJGL[GLFW.GLFW_KEY_DELETE] = Keyboard.KEY_DELETE;
        GLFW2LWJGL[GLFW.GLFW_KEY_RIGHT] = Keyboard.KEY_RIGHT;
        GLFW2LWJGL[GLFW.GLFW_KEY_LEFT] = Keyboard.KEY_LEFT;
        GLFW2LWJGL[GLFW.GLFW_KEY_DOWN] = Keyboard.KEY_DOWN;
        GLFW2LWJGL[GLFW.GLFW_KEY_UP] = Keyboard.KEY_UP;
        GLFW2LWJGL[GLFW.GLFW_KEY_PAGE_UP] = Keyboard.KEY_PRIOR;
        GLFW2LWJGL[GLFW.GLFW_KEY_PAGE_DOWN] = Keyboard.KEY_NEXT;
        GLFW2LWJGL[GLFW.GLFW_KEY_HOME] = Keyboard.KEY_HOME;
        GLFW2LWJGL[GLFW.GLFW_KEY_END] = Keyboard.KEY_END;
        GLFW2LWJGL[GLFW.GLFW_KEY_CAPS_LOCK] = Keyboard.KEY_CAPITAL;
        GLFW2LWJGL[GLFW.GLFW_KEY_SCROLL_LOCK] = Keyboard.KEY_SCROLL;
        GLFW2LWJGL[GLFW.GLFW_KEY_NUM_LOCK] = Keyboard.KEY_NUMLOCK;
        GLFW2LWJGL[GLFW.GLFW_KEY_PRINT_SCREEN] = Keyboard.KEY_PRINT_SCREEN;
        GLFW2LWJGL[GLFW.GLFW_KEY_PAUSE] = Keyboard.KEY_PAUSE;
        GLFW2LWJGL[GLFW.GLFW_KEY_F1] = Keyboard.KEY_F1;
        GLFW2LWJGL[GLFW.GLFW_KEY_F2] = Keyboard.KEY_F2;
        GLFW2LWJGL[GLFW.GLFW_KEY_F3] = Keyboard.KEY_F3;
        GLFW2LWJGL[GLFW.GLFW_KEY_F4] = Keyboard.KEY_F4;
        GLFW2LWJGL[GLFW.GLFW_KEY_F5] = Keyboard.KEY_F5;
        GLFW2LWJGL[GLFW.GLFW_KEY_F6] = Keyboard.KEY_F6;
        GLFW2LWJGL[GLFW.GLFW_KEY_F7] = Keyboard.KEY_F7;
        GLFW2LWJGL[GLFW.GLFW_KEY_F8] = Keyboard.KEY_F8;
        GLFW2LWJGL[GLFW.GLFW_KEY_F9] = Keyboard.KEY_F9;
        GLFW2LWJGL[GLFW.GLFW_KEY_F10] = Keyboard.KEY_F10;
        GLFW2LWJGL[GLFW.GLFW_KEY_F11] = Keyboard.KEY_F11;
        GLFW2LWJGL[GLFW.GLFW_KEY_F12] = Keyboard.KEY_F12;
        GLFW2LWJGL[GLFW.GLFW_KEY_F13] = Keyboard.KEY_F13;
        GLFW2LWJGL[GLFW.GLFW_KEY_F14] = Keyboard.KEY_F14;
        GLFW2LWJGL[GLFW.GLFW_KEY_F15] = Keyboard.KEY_F15;
        GLFW2LWJGL[GLFW.GLFW_KEY_F16] = Keyboard.KEY_F16;
        GLFW2LWJGL[GLFW.GLFW_KEY_F17] = Keyboard.KEY_F17;
        GLFW2LWJGL[GLFW.GLFW_KEY_F18] = Keyboard.KEY_F18;
        GLFW2LWJGL[GLFW.GLFW_KEY_F19] = Keyboard.KEY_F19;
        GLFW2LWJGL[GLFW.GLFW_KEY_F20] = Keyboard.KEY_F20;
        GLFW2LWJGL[GLFW.GLFW_KEY_F21] = Keyboard.KEY_F21;
        GLFW2LWJGL[GLFW.GLFW_KEY_F22] = Keyboard.KEY_F22;
        GLFW2LWJGL[GLFW.GLFW_KEY_F23] = Keyboard.KEY_F23;
        GLFW2LWJGL[GLFW.GLFW_KEY_F24] = Keyboard.KEY_F24;
        GLFW2LWJGL[GLFW.GLFW_KEY_F25] = Keyboard.KEY_F25;
        GLFW2LWJGL[GLFW.GLFW_KEY_KP_0] = Keyboard.KEY_NUMPAD0;
        GLFW2LWJGL[GLFW.GLFW_KEY_KP_1] = Keyboard.KEY_NUMPAD1;
        GLFW2LWJGL[GLFW.GLFW_KEY_KP_2] = Keyboard.KEY_NUMPAD2;
        GLFW2LWJGL[GLFW.GLFW_KEY_KP_3] = Keyboard.KEY_NUMPAD3;
        GLFW2LWJGL[GLFW.GLFW_KEY_KP_4] = Keyboard.KEY_NUMPAD4;
        GLFW2LWJGL[GLFW.GLFW_KEY_KP_5] = Keyboard.KEY_NUMPAD5;
        GLFW2LWJGL[GLFW.GLFW_KEY_KP_6] = Keyboard.KEY_NUMPAD6;
        GLFW2LWJGL[GLFW.GLFW_KEY_KP_7] = Keyboard.KEY_NUMPAD7;
        GLFW2LWJGL[GLFW.GLFW_KEY_KP_8] = Keyboard.KEY_NUMPAD8;
        GLFW2LWJGL[GLFW.GLFW_KEY_KP_9] = Keyboard.KEY_NUMPAD9;
        GLFW2LWJGL[GLFW.GLFW_KEY_KP_DECIMAL] = Keyboard.KEY_DECIMAL;
        GLFW2LWJGL[GLFW.GLFW_KEY_KP_DIVIDE] = Keyboard.KEY_DIVIDE;
        GLFW2LWJGL[GLFW.GLFW_KEY_KP_MULTIPLY] = Keyboard.KEY_MULTIPLY;
        GLFW2LWJGL[GLFW.GLFW_KEY_KP_SUBTRACT] = Keyboard.KEY_SUBTRACT;
        GLFW2LWJGL[GLFW.GLFW_KEY_KP_ADD] = Keyboard.KEY_ADD;
        GLFW2LWJGL[GLFW.GLFW_KEY_KP_ENTER] = Keyboard.KEY_NUMPADENTER;
        GLFW2LWJGL[GLFW.GLFW_KEY_KP_EQUAL] = Keyboard.KEY_NUMPADEQUALS;
        GLFW2LWJGL[GLFW.GLFW_KEY_LEFT_SHIFT] = Keyboard.KEY_LSHIFT;
        GLFW2LWJGL[GLFW.GLFW_KEY_LEFT_CONTROL] = Keyboard.KEY_LCONTROL;
        GLFW2LWJGL[GLFW.GLFW_KEY_LEFT_ALT] = Keyboard.KEY_LMENU;
        GLFW2LWJGL[GLFW.GLFW_KEY_LEFT_SUPER] = Keyboard.KEY_LMETA;
        GLFW2LWJGL[GLFW.GLFW_KEY_RIGHT_SHIFT] = Keyboard.KEY_RSHIFT;
        GLFW2LWJGL[GLFW.GLFW_KEY_RIGHT_CONTROL] = Keyboard.KEY_RCONTROL;
        GLFW2LWJGL[GLFW.GLFW_KEY_RIGHT_ALT] = Keyboard.KEY_RMENU;
        GLFW2LWJGL[GLFW.GLFW_KEY_RIGHT_SUPER] = Keyboard.KEY_RMETA;
        GLFW2LWJGL[GLFW.GLFW_KEY_MENU] = Keyboard.KEY_MENU;
    }
}
