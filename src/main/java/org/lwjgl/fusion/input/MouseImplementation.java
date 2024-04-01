package org.lwjgl.fusion.input;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * @author Zarzelcow
 * @created 28/09/2022 - 8:58 PM
 */
public interface MouseImplementation {
	int getNativeCursorCapabilities();
	
    void createMouse();

    void destroyMouse();

    void pollMouse(IntBuffer coord_buffer, ByteBuffer buttons_buffer);

    void readMouse(ByteBuffer readBuffer);

    void setCursorPosition(int x, int y);

    void grabMouse(boolean grab);

    boolean hasWheel();

    int getButtonCount();

    boolean isInsideWindow();
}
