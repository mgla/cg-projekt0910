package cg_project;

import java.nio.IntBuffer;

import java.util.Random;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector4f;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.ARBBufferObject;

public class Main {
    private int width = 640;
    private int height = 480;
    private float fov = 60.0f;
    private Vector4f cameraPosition = new Vector4f(0.0f, 0.0f, 5.0f, 1.0f);
    
    // Class for player score management
    private Player player = new Player();
    private IntBuffer vboid = IntBuffer.allocate(1);
    
    // Time between spawn of two cubes in milliseconds
    private int spawnTime = 2500;

    /**
     * Entry point for the java program.
     */
    public static void main(String[] args) {
        new Main().execute();
        System.exit(0);
    }

    /**
     * Execution of the initialization and the render loop. The display
     * will be destroyed when the close button is hit and the loop stops.
     */
    private void execute() {
        try {
            init();
        } catch (LWJGLException oLWJGLException) {
            oLWJGLException.printStackTrace();
            System.out.println("Failed to initialize Application.");
            return;
        }

        loop();

        destroy();
    }

    /**
     * This is the initialization of the open gl window. Here we set up
     * the display with its viewport and the projection matrix.<br>
     */
    private void init() throws LWJGLException {
        Display.setLocation(
                (Display.getDisplayMode().getWidth() - width) / 2,
                (Display.getDisplayMode().getHeight() - height) / 2);
        Display.setDisplayMode(new DisplayMode(width, height));
        Display.setTitle("Computer Graphics");
        Display.create();

        //Setup OpenGL
        //GL11.glEnable(GL11.GL_LIGHTING);
        //GL11.glEnable(GL11.GL_BLEND);


        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_NORMALIZE);


        GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        //Setup Projection and Viewport
        GL11.glViewport(0, 0, width, height);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GLU.gluPerspective(fov, (float) width / (float) height, 0.1f, 100.0f);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }

    /**
     * This is the render loop. It will stop, when the close button of the
     * Display is hit.
     */
    private void loop() {
        long startMillis = System.currentTimeMillis();
        long fpsCheck = System.currentTimeMillis() + 1000;
        long fps = 0;

        long time, oldtime;
        time = oldtime = 0;
        
        while (!Display.isCloseRequested()) {
            //Handle all input events.
            //handleEvents();
            //mouseHandler();

            time = (System.currentTimeMillis() - startMillis);
            
            //spawn
            if ((time - oldtime) >= spawnTime) {
            	oldtime = time;
            	
                Cube c = new Cube();
                Random r = new Random();
                c.setColor(new float[]{r.nextFloat(), r.nextFloat(), r.nextFloat(), 0});

                World.getInstance().addCube(c);

            }

            //Clear to the background color
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            //Setting the current object transformation
            GL11.glLoadIdentity();

            //View matrix
            GLU.gluLookAt(
                    cameraPosition.x, cameraPosition.y, cameraPosition.z,
                    0.0f, 0.0f, 0.0f,
                    0.0f, 1.0f, 0.0f);

            //Model matrix
            //GL11.glMultMatrix(Converter.getBufferFromMatrix(rotation));

            //Matrix4f movement = new Matrix4f();
            //movement.m30 = (step / 1000) % 5;
            //GL11.glMultMatrix(Converter.getBufferFromMatrix(movement));
            World.getInstance().draw();



            //Setting the current color
			/*GL11.glMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT_AND_DIFFUSE, FloatBuffer.wrap(new float[] {
            (float)Math.sin(0.001f * step) / 2.0f + 0.5f, 
            (float)Math.sin(0.002f * step) / 2.0f + 0.5f, 
            (float)Math.sin(0.004f * step) / 2.0f + 0.5f, 
            1.0f}));*/


            //Swap the buffers
            Display.update();

            mouseHandler();

            //Count the frames per second
            long currentMillis = System.currentTimeMillis();
            if (fpsCheck > currentMillis) {
                fps++;
            } else {
                float timeUsed = (float) (fpsCheck - currentMillis + 1000);
                fpsCheck = currentMillis + 1000;
                Display.setTitle("Computer Graphics (" + ((float) fps / (timeUsed / 1000.0f)) + " fps)");
                fps = 0;
            }
        }
    }



    private void mouseHandler() {
        if (!Mouse.isButtonDown(0)) {
            return;
        }
        Vector2f mousePos = new Vector2f(Mouse.getX(), Mouse.getY());
        Cube c = World.getInstance().objectAtScreenPosition(mousePos);
        if (c == null) {
            return;
        }
        if (c.getId() == 0) {
            player.addPoints(1);
        } else {
            player.addPoints(2);
        }
        World.getInstance().removeCube(c.getId());

    }

    /**
     * Destroy the display. This is a simple method right now, but later we can
     * clean up other things within this function. <br>
     */
    private void destroy() {
        ARBBufferObject.glDeleteBuffersARB(vboid);
        Display.destroy();
    }
}
