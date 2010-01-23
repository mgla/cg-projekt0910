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

/**
 * This is the main class. Here you will find the following tasks:
 * <br>
 * {@link #init() Task 1.1 Setup the VBO for a cube}<br>
 * {@link #destroy() Task 1.1* Clean up VBO}<br>
 * {@link #loop() Task 1.2 Render the VBO}<br>
 * {@link #createData(ObjImporter objImporter) Task 2 Convert data from Obj to VBO}<br>
 * {@link #init() Task 2* Load a bunny}<br>
 * {@link #handleEvents() Task 3 Implement a Virtual Trackball to rotate the bunny}<br>
 */
public class Main {
    //private int width = 640;
    private int width = 640;
    private int height = 480;
    private float fov = 60.0f;
    private Vector4f cameraPosition = new Vector4f(0.0f, 0.0f, 5.0f, 1.0f);
    /* ***************************************************************************
     * Additional Code has to be added here. Global variables and some additional
     * functions. Please also state, to which task the additional code
     * belongs like:
     * //Task 1.1, 1.2:
     * private int senseless = 0;
     *
     * Start of additional code
     ****************************************************************************/
    //Task 1.1, 1.2:
    private Player player = new Player();
    private IntBuffer vboid = IntBuffer.allocate(1);

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
     * <br>
     * <b><font color = "#ff0000">Task 1.1:</font></b><br>
     * Create the VBO here by using a data buffer. In order to fill the buffer
     * can use the {@link de.rwth_aachen.graphics.Primitives#createCubeData() createCubeData()}
     * method from the Primitives class.<br>
     * <br>
     * <b><font color = "#ff0000">Task 2*:</font></b><br>
     * Instead of using the Primitive data, you have to use the obj importer and
     * call your implemented function {@link #createData(ObjImporter) createData()}.<br>
     * <br>
     * <i>Hint:</i> Use the bunny geometry which you can find here: 
     * res/geometry/Bunny.obj. The following function might be helpful:
     * {@link de.rwth_aachen.graphics.ObjImporter#getTriangleCount() getTriangleCount()}
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
     * Display is hit.<br>
     * <br>
     * <b><font color = "#ff0000">Task 1.2:</font></b><br>
     * Instead of using the intermediate drawCube method use VBOs to render the 
     * geometry. You can use the SIZE_OF_DATA and SIZE_OF_FLOAT commands to
     * calculate the size in bytes of each element in the array buffer.
     */
    private void loop() {
        long startMillis = System.currentTimeMillis();
        long fpsCheck = System.currentTimeMillis() + 1000;
        long fps = 0;

        while (!Display.isCloseRequested()) {
            //Handle all input events.
            //handleEvents();
            //mouseHandler();

            float step = (float) (System.currentTimeMillis() - startMillis);

            //spawn
            if (step % 500 == 0) {

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
            World.getInstance().draw(step);



            //Setting the current color
			/*GL11.glMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT_AND_DIFFUSE, FloatBuffer.wrap(new float[] {
            (float)Math.sin(0.001f * step) / 2.0f + 0.5f, 
            (float)Math.sin(0.002f * step) / 2.0f + 0.5f, 
            (float)Math.sin(0.004f * step) / 2.0f + 0.5f, 
            1.0f}));*/


            //Draw a cube
            /****************************************************************************
             * Start of task 1.2:
             ****************************************************************************/
            //Primitives.drawCube();absoluteRotation
			/*ARBBufferObject.glBindBufferARB(GL15.GL_ARRAY_BUFFER, vboid.get(0));
            
            GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
            GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
            
            GL11.glVertexPointer(3, GL11.GL_FLOAT, SIZE_OF_DATA * SIZE_OF_FLOAT, 0);
            GL11.glNormalPointer(GL11.GL_FLOAT, SIZE_OF_DATA * SIZE_OF_FLOAT, 3 * SIZE_OF_FLOAT);
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3 * numberOfTriangles);
            
            GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
            GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
            
            ARBBufferObject.glBindBufferARB(GL15.GL_ARRAY_BUFFER, 0);*/
            /****************************************************************************
             * End of task 1.2
             ****************************************************************************/            //Swap the buffers
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
     * <br>
     * <b><font color = "#ff0000">Task 1.1*:</font></b>
     * Clean up any created buffers.
     */
    private void destroy() {
        /****************************************************************************
         * Start of task 1.1*:
         ****************************************************************************/
        ARBBufferObject.glDeleteBuffersARB(vboid);
        /****************************************************************************
         * End of task 1.1*
         ****************************************************************************/
        Display.destroy();
    }
}
