package org.lwjgl.opengl;

import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWVidMode.Buffer;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.input.Controllers;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.system.MemoryUtil;

public class Display {
    
    private static final DisplayMode desktop_mode;

    private static String title = "Game";

    private static long handle = MemoryUtil.NULL;

    private static boolean resizable = false;

    private static DisplayMode current_mode;

    private static int width = 0;

    private static int height = 0;

    private static int x = -1;

    private static int y = -1;

    private static boolean fullscreen;

    private static boolean window_resized = false;
    
    private static boolean window_created = false;

    private static GLFWWindowSizeCallback sizeCallback = null;

    private static ByteBuffer[] cached_icons = null;

    private Display() {
    }
    
    static {
        GLFWErrorCallback.createPrint(System.err).set();
        if (GLFW.glfwInit()) {
            new ExceptionInInitializerError("Unable to initialize GLFW");
        }
        GLFWVidMode vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        desktop_mode = new DisplayMode(vidMode.width(), vidMode.height(), vidMode.redBits() + vidMode.greenBits() + vidMode.blueBits(), vidMode.refreshRate());
        current_mode = desktop_mode;
    }

    public static DisplayMode getDisplayMode() {
         return current_mode;
    }

    public static int setIcon(ByteBuffer[] icons) {
        if ( cached_icons != icons ) {
            cached_icons = new ByteBuffer[icons.length];
            for ( int i = 0; i < icons.length; i++ ) {
                cached_icons[i] = BufferUtils.createByteBuffer(icons[i].capacity());
                int old_position = icons[i].position();
                cached_icons[i].put(icons[i]);
                icons[i].position(old_position);
                cached_icons[i].flip();
            }
        }

        if (isCreated()) {
            GLFW.glfwSetWindowIcon(handle, iconsToGLFWBuffer(cached_icons));
            return 1;
        } else {
            return 0;
        }
    }

    public static DisplayMode getDesktopDisplayMode() {
        return desktop_mode;
    }

    private static GLFWImage.Buffer iconsToGLFWBuffer(ByteBuffer[] icons)  {
        GLFWImage.Buffer buffer = GLFWImage.create(icons.length);
        for(ByteBuffer icon : icons) {
            int size = icon.limit() / 4;
            int dimension = (int)Math.sqrt(size);
            GLFWImage image = GLFWImage.malloc();
            buffer.put(image.set(dimension, dimension, icon));
        }
        buffer.flip();
        return buffer;
    }

    public static void update() {
        update(true);
    }

    public static void update(boolean processMessages) {
        window_resized = false;
        GLFW.glfwPollEvents();
        if(processMessages) {
            if (Mouse.isCreated()) {
                Mouse.poll();
//	            Mouse.updateCursor();
            }
            if (Keyboard.isCreated()) {
                Keyboard.poll();
            }
            if (Controllers.isCreated()) {
                Controllers.poll();
            }
        }
        swapBuffers();
    }
    
    public static void swapBuffers() {
        GLFW.glfwSwapBuffers(handle);
    }

    public static void create() throws LWJGLException {
        create(new PixelFormat());
    }

    public static void create(PixelFormat pixelFormat) throws LWJGLException {
    	
    	if (GLFW.glfwPlatformSupported(GLFW.GLFW_PLATFORM_WAYLAND)) {
    		GLFW.glfwInitHint(GLFW.GLFW_PLATFORM, GLFW.GLFW_PLATFORM_WAYLAND);
    	}
    	
		if (GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_WAYLAND){
			GLFW.glfwWindowHint(GLFW.GLFW_FOCUS_ON_SHOW, GLFW.GLFW_FALSE);
		}
		
        GLFW.glfwWindowHint(GLFW.GLFW_ACCUM_ALPHA_BITS, pixelFormat.getAccumulationBitsPerPixel());
        GLFW.glfwWindowHint(GLFW.GLFW_ALPHA_BITS, pixelFormat.getAlphaBits());
        GLFW.glfwWindowHint(GLFW.GLFW_AUX_BUFFERS, pixelFormat.getAuxBuffers());
        GLFW.glfwWindowHint(GLFW.GLFW_DEPTH_BITS, pixelFormat.getDepthBits());
        GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, pixelFormat.getSamples());
        GLFW.glfwWindowHint(GLFW.GLFW_STENCIL_BITS, pixelFormat.getStencilBits());
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        handle = GLFW.glfwCreateWindow(current_mode.getWidth(), current_mode.getHeight(), title, MemoryUtil.NULL, MemoryUtil.NULL);
        if(handle == MemoryUtil.NULL) {
            throw new LWJGLException("Display could not be created");
        }
        sizeCallback = GLFWWindowSizeCallback.create(Display::resizeCallback);
        GLFW.glfwSetWindowSizeCallback(handle, sizeCallback);
        GLFW.glfwMakeContextCurrent(handle);
        createWindow();
        GL.createCapabilities();
    }
    
    public static void setLocation(int new_x, int new_y) {
        x = new_x;
        y = new_y;

        if ( isCreated() && !isFullscreen() ) {
            GLFW.glfwSetWindowPos(handle, x, y);
        }
    }

    public static void setFullscreen(boolean fullscreen) throws LWJGLException {
        setDisplayModeAndFullscreenInternal(fullscreen, current_mode);
    }
    
    public static void setDisplayMode(DisplayMode mode) throws LWJGLException {
        setDisplayModeAndFullscreen(mode);
    }

    public static void setDisplayModeAndFullscreen(DisplayMode mode) throws LWJGLException {
        setDisplayModeAndFullscreenInternal(mode.isFullscreenCapable(), mode);
    }

    private static void setDisplayModeAndFullscreenInternal(boolean fullscreen, DisplayMode mode) throws LWJGLException {
        if ( mode == null )
            throw new NullPointerException("mode must be non-null");
        DisplayMode old_mode = current_mode;
        current_mode = mode;
        boolean was_fullscreen = isFullscreen();
        Display.fullscreen = fullscreen;
        if ( was_fullscreen != isFullscreen() || !mode.equals(old_mode) ) {
            if ( !isCreated() )
                return;
            destroyWindow();
            createWindow();
        }
    }

    private static void createWindow() throws LWJGLException {
        if ( isCreated() ) {
            return;
        }
        // Configure GLFW
        GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_RESIZABLE, resizable ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
        window_created = true;

        if(isFullscreen()) {
            boolean borderlessFullscreen = false;
            if(borderlessFullscreen) {
                GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_DECORATED, 0);
                GLFW.glfwSetWindowSize(handle, current_mode.getWidth(), current_mode.getHeight());
            } else {
                GLFW.glfwSetWindowMonitor(handle, GLFW.glfwGetPrimaryMonitor(), x, y, current_mode.getWidth(), current_mode.getHeight(), current_mode.getFrequency());
            }
        } else {
            GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_DECORATED, 1);
            GLFW.glfwSetWindowMonitor(handle, MemoryUtil.NULL, x, y, current_mode.getWidth(), current_mode.getHeight(), current_mode.getFrequency());
        }
        
        width = current_mode.getWidth();
        height = current_mode.getHeight();
        GLFW.glfwSetWindowPos(handle, getWindowX(), getWindowY());
        // create general callbacks
        initControls();

        // set cached window icon if exists
        if ( cached_icons != null ) {
            setIcon(cached_icons);
        } else {
            setIcon(new ByteBuffer[] { LWJGLUtil.LWJGLIcon32x32, LWJGLUtil.LWJGLIcon16x16 });
        }
        GLFW.glfwShowWindow(handle);
        GLFW.glfwFocusWindow(handle);
    }

    static boolean getPrivilegedBoolean(final String property_name) {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            public Boolean run() {
                return Boolean.getBoolean(property_name);
            }
        });
    }

    private static void initControls() {
        if ( !getPrivilegedBoolean("org.lwjgl.opengl.Display.noinput") ) {
            if ( !Mouse.isCreated() && !getPrivilegedBoolean("org.lwjgl.opengl.Display.nomouse") ) {
                try {
                    Mouse.create();
                } catch (LWJGLException e) {
                    e.printStackTrace(System.err);
                }
            }
            if ( !Keyboard.isCreated() && !getPrivilegedBoolean("org.lwjgl.opengl.Display.nokeyboard") ) {
                try {
                    Keyboard.create();
                } catch (LWJGLException e) {
                    e.printStackTrace(System.err);
                }
            }
        }
    }

    public static DisplayMode[] getAvailableDisplayModes() {
        long primaryMonitor = GLFW.glfwGetPrimaryMonitor();
        if (primaryMonitor == MemoryUtil.NULL) {
            return new DisplayMode[0];
        }
        Buffer videoModes = GLFW.glfwGetVideoModes(primaryMonitor);
        HashSet<DisplayMode> modes = new HashSet<DisplayMode>(videoModes.sizeof());
        for (int i = 0; i < videoModes.sizeof(); i++) {
            GLFWVidMode mode = videoModes.get(i);
            modes.add(new DisplayMode(mode.width(), mode.height(), mode.redBits() + mode.blueBits() + mode.greenBits(), mode.refreshRate()));
        }
        DisplayMode[] filteredModes = new DisplayMode[videoModes.sizeof()];
        modes.toArray(filteredModes);
        
        return filteredModes;
    }

    private static void resizeCallback(long window, int width, int height) {
        if (window == handle) {
            window_resized = true;
            Display.width = width;
            Display.height = height;
        }
    }

    private static void destroyWindow() {
        if ( !isCreated()) {
            return;
        }
        if ( Mouse.isCreated() ) {
            Mouse.destroy();
        }
        if ( Keyboard.isCreated() ) {
            Keyboard.destroy();
        }
        // Hide the window while maintaining it's context
        //GLFW.glfwHideWindow(handle);
        window_created = false;
    }

    public static void destroy() {
        destroyWindow();
        sizeCallback.free();
        // Terminate GLFW and free the error callback
        GLFW.glfwTerminate();
        GLFWErrorCallback callback = GLFW.glfwSetErrorCallback(null);
        if(callback != null) {
            callback.free();
        }
    }

    public static boolean isCreated() {
        return window_created;
    }

    public static boolean isCloseRequested() {
        return GLFW.glfwWindowShouldClose(handle);
    }

    public static boolean isActive() {
        return GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_ICONIFIED) == 0;
    }

    public static void setResizable(boolean isResizable) {
        resizable = isResizable;
        if (isCreated()) {
            GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, resizable ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
        }
    }

    public static void sync(int fps) {
        Sync.sync(fps);
    }

    public static String getTitle() {
        return title;
    }

    public static void setTitle(String newTitle) {
        if ( newTitle == null ) {
            newTitle = "";
        }
        title = newTitle;
        if ( isCreated() )
            GLFW.glfwSetWindowTitle(handle, title);
    }

    public static void setVSyncEnabled(boolean enabled) {
        if(!isCreated()) {
            return;
        }
        GLFW.glfwSwapInterval(enabled ? 1 : 0);
    }

    private static int getWindowX() {
        if ( !isFullscreen() ) {
            // if no display location set, center window
            if ( x == -1 ) {
                return Math.max(0, (desktop_mode.getWidth() - current_mode.getWidth()) / 2);
            } else {
                return x;
            }
        } else {
            return 0;
        }
    }

    private static int getWindowY() {
        if ( !isFullscreen() ) {
            // if no display location set, center window
            if ( y == -1 ) {
                return Math.max(0, (desktop_mode.getHeight() - current_mode.getHeight()) / 2);
            } else {
                return y;
            }
        } else {
            return 0;
        }
    }
    public static int getX() {
        if (isFullscreen()) {
            return 0;
        }
        return x;
    }

    public static int getY() {
        if (isFullscreen()) {
            return 0;
        }
        return y;
    }
    
    public static int getWidth() {
        return width;
    }
    
    public static int getHeight() {
        return height;
    }

    public static boolean isFullscreen() {
        return fullscreen;
    }

    public static boolean wasResized() {
        return window_resized;
    }
    
    public static long getHandle() {
        return handle;
    }
}