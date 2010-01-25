package cg_project;

import java.nio.IntBuffer;
import java.nio.FloatBuffer;

import java.util.Random;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector4f;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.ARBBufferObject;
import org.lwjgl.opengl.ARBVertexBufferObject;

/**
 * @author Maik Glatki, Christian Dernehl, Dominic Gatzen, huge parts: Lehrstuhl für Informatik VIII exercises 
 */

public class Main {
    private int width = 640;
    private int height = 480;
    private float fov = 60.0f;
    private Vector4f cameraPosition = new Vector4f(-3.0f, 5.0f, 5.0f, 1.0f);
    
    // Specifications from exercise04 solution
    private final int TEXTURE_CUBE_DUMMY = 0;
    private final int TEXTURE_CUBE_METAL = 1;
    private final int TEXTURE_COUNT = 2;
    private float GL_VERSION;
    //3 for position, 3 for normals, 2 for texture coordinates
    private final int SIZE_OF_DATA = 8;
    private final int SIZE_OF_FLOAT = 4;
    
    
    private IntBuffer textureid = IntBuffer.allocate(TEXTURE_COUNT);
    private final int GEOMETRY_CUBE = 0;
    private IntBuffer vboid = IntBuffer.allocate(1);
    
    // Class for player score management
    private Player player = new Player();
    private Random rand;

    
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
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_NORMALIZE);


        GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        
        
        //Generate the VBO buffer
        if(GL_VERSION >= 1.5) {
            GL15.glGenBuffers(vboid);
    	}
        else {        
            ARBVertexBufferObject.glGenBuffersARB(vboid);
    	}
        // MasterCube
        
        ObjImporter objImporter = new ObjImporter("Dummy.obj");
        if(GL_VERSION >= 1.5)
        {
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboid.get(GEOMETRY_CUBE));
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, createData(objImporter), GL15.GL_STATIC_DRAW);
        }
        else
        {
            ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, vboid.get(GEOMETRY_CUBE));
            ARBVertexBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, createData(objImporter), ARBVertexBufferObject.GL_STATIC_DRAW_ARB);
        }
        
        GL_VERSION = Float.valueOf(GL11.glGetString(GL11.GL_VERSION).substring(0, 3)).floatValue();
        System.out.println("OpenGL Version: " + GL_VERSION);
        
        //Create the needed texture contexts
        GL11.glGenTextures(textureid);
        
        TextureImporter textureImporter = null;
        
        //The textures
        
        // Dummy texture
        textureImporter = new TextureImporter("Dummy.jpg");
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureid.get(TEXTURE_CUBE_DUMMY));
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, textureImporter.getWidth(), textureImporter.getHeight(), 0, textureImporter.hasAlpha() ? GL11.GL_RGBA : GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, textureImporter.getData());
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        
        textureImporter = new TextureImporter("metal.jpg");
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureid.get(TEXTURE_CUBE_METAL));
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, textureImporter.getWidth(), textureImporter.getHeight(), 0, textureImporter.hasAlpha() ? GL11.GL_RGBA : GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, textureImporter.getData());
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        

        //Setup Projection and Viewport
        GL11.glViewport(0, 0, width, height);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GLU.gluPerspective(fov, (float) width / (float) height, 0.1f, 100.0f);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        
        // Random generator
        this.rand = new Random();
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
        	
            // spawn additional Cubes if the time has come        	
            time = (System.currentTimeMillis() - startMillis);
            if ((time - oldtime) >= spawnTime) {
            	oldtime = time;
            	// choose random Texture
            	int tex = textureid.get(this.rand.nextInt(TEXTURE_COUNT));
                Cube c = new Cube(tex);                
                
                // c.setColor(new float[]{r.nextFloat(), r.nextFloat(), r.nextFloat(), 0}); 

                World.getInstance().addCube(c);

            }

            //Clear to the background color
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            //Setting the current object transformation
            GL11.glLoadIdentity();

            //View matrix
            GLU.gluLookAt(
                    cameraPosition.x, cameraPosition.y, cameraPosition.z,
                    -3.0f, 0.0f, -5.0f,
                    0.0f, 1.0f, 0.0f);          
            

            // Draw the Master Cube
            // Choose texture

            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureid.get(TEXTURE_CUBE_DUMMY));
            
            if(GL_VERSION >= 1.5)
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboid.get(GEOMETRY_CUBE));
            else
                ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, vboid.get(GEOMETRY_CUBE));
			
            // Definiert ein Array mit Texturkoordinaten 

            GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            GL11.glTexCoordPointer(2, GL11.GL_FLOAT, SIZE_OF_DATA * SIZE_OF_FLOAT, 6 * SIZE_OF_FLOAT);
            GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
			
            if(GL_VERSION >= 1.5) {
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
            } else {
                ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0);
            }
            
            // End of texturing
            GL11.glDisable(GL11.GL_LIGHTING);
            World.getInstance().draw();

            // Zeichne Grid
            GL11.glColor3f(0.8f, 0.8f, 0.8f);
            GL11.glLineWidth(2.0f);
            Primitives.drawGrid(20, 1.0f);
            
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
                Display.setTitle("Computer Graphics (" + ((float) fps / (timeUsed / 1000.0f)) + " fps)" + " Player points : " + player.getPoints());
                fps = 0;
            }
        }
    }



    private void mouseHandler() {
        if (!Mouse.isButtonDown(0)) {
            return;
        }
        // Bestimme Objekt bei Mausklick
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
    
    /**
     * This method creates the VBO data of a loaded obj file.
     * @param objImporter The obj file that should be used for the creation
     * of the data buffer.
     * @return A buffer with vertex positions, normals and texture coordinates
     * that can be used for a VBO.
     */
    private FloatBuffer createData(ObjImporter objImporter)
    {
        FloatBuffer arrayBuffer = FloatBuffer.allocate(SIZE_OF_DATA * 3 * objImporter.getTriangleCount());
        FloatBuffer vertexBuffer = objImporter.getVertexBuffer();
        FloatBuffer normalBuffer = objImporter.getNormalBuffer();
        FloatBuffer texCoordBuffer = objImporter.getTexCoordBuffer();
        IntBuffer dataIndexBuffer = objImporter.getDataIndexBuffer();
        
        int index = 0;
        for(int i = 0; i < 3 * objImporter.getTriangleCount(); i++)
        {
            index = 3 * dataIndexBuffer.get(objImporter.getDataSize() * i);
            arrayBuffer.put(SIZE_OF_DATA * i, vertexBuffer.get(index));
            arrayBuffer.put(SIZE_OF_DATA * i + 1, vertexBuffer.get(index + 1));
            arrayBuffer.put(SIZE_OF_DATA * i + 2, vertexBuffer.get(index + 2));
            if(objImporter.getNormalPosition() < 0)
            {
                arrayBuffer.put(SIZE_OF_DATA * i + 3, 0.0f);
                arrayBuffer.put(SIZE_OF_DATA * i + 4, 0.0f);
                arrayBuffer.put(SIZE_OF_DATA * i + 5, 0.0f);
            }
            else
            {
                index = 3 * dataIndexBuffer.get(objImporter.getDataSize() * i + objImporter.getNormalPosition());
                arrayBuffer.put(SIZE_OF_DATA * i + 3, normalBuffer.get(index));
                arrayBuffer.put(SIZE_OF_DATA * i + 4, normalBuffer.get(index + 1));
                arrayBuffer.put(SIZE_OF_DATA * i + 5, normalBuffer.get(index + 2));
            }
            if(objImporter.getTexCoordPosition() < 0)
            {
                arrayBuffer.put(SIZE_OF_DATA * i + 6, 0.0f);
                arrayBuffer.put(SIZE_OF_DATA * i + 7, 0.0f);
            }
            else
            {
                index = 3 * dataIndexBuffer.get(objImporter.getDataSize() * i + objImporter.getTexCoordPosition());
                arrayBuffer.put(SIZE_OF_DATA * i + 6, texCoordBuffer.get(index));
                arrayBuffer.put(SIZE_OF_DATA * i + 7, texCoordBuffer.get(index + 1));
            }
        }
        return arrayBuffer;
    }
    
}
