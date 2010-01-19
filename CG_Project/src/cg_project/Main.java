package cg_project;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
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
public class Main
{
	//private int width = 640;
        private int width = 640;
	private int height = 480;
	private float fov = 60.0f;
	private Vector4f cameraPosition = new Vector4f(0.0f, 0.0f, 5.0f, 1.0f);
        private Vector4f lightPosition = new Vector4f(3.0f, 3.0f, 3.0f, 1.0f);
	
	//3 for position, 3 for normals
	private final int SIZE_OF_DATA = 6;
	private final int SIZE_OF_FLOAT = 4;
	
	private boolean leftMouseButtonDown = false;
	private Vector2f prevMousePos = new Vector2f(0.0f, 0.0f);

	private Matrix3f rotation = new Matrix3f();
	
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
	private int numberOfTriangles = 0;
	private IntBuffer vboid = IntBuffer.allocate(1);
	
	//Task 3:
	/** 
	* convert Screen coordinates to global coordinate ( on the unit sphere )
	* @param vec2fPos the position in the screen coordinate system 
	* @return the position in the global coordinate system
	*/
	private Vector2f ScrCS_To_GLCS(Vector2f vec2fPos)
	{
		Vector2f glPos = new Vector2f();
		glPos.x = (vec2fPos.x - 0.5f * (float)width) / (float)width * (float)Math.sqrt(2.0f);
		glPos.y = (vec2fPos.y - 0.5f * (float)height) / (float)height * (float)Math.sqrt(2.0f);
		return glPos;
	}
	
	/**
	 * compute rotation axis by the cross product of two vectors
	 * @param v1 vector 1 
	 * @param v2 vector 2
	 * @return the vector of the rotation axis
	 */
	private Vector3f computeRotationAxis(Vector3f v1, Vector3f v2)
	{
		Vector3f result = Vector3f.cross(v1, v2, null);
		return result;
	}
	
	/**
	 * compute rotation angle by the dot product of two vectors
	 * @param v1 vector 1 
	 * @param v2 vector 2
	 * @return the rotation angle
	 */
	private float computeRotationAngle(Vector3f v1, Vector3f v2)
	{
		float result = 2.0f * (float)Math.acos(Vector3f.dot(v1, v2));
		return result;
	}
	
	/**
	 * convert Axis-Angle based rotation to Rotation Matrix using Rodrigues formula
	 * Hint: the vector axis must be normalized
	 * @param axis the vector of the rotation axis
	 * @param angle the rotation angle
	 * @return the matrix representing the rotation around the axis
	 */
	private Matrix3f AxisAngle_To_RotMat(Vector3f axis, float angle)
	{
		float c = (float)Math.cos(angle);
		float s = (float)Math.sin(angle);
		float t = 1.0f - c;
		
		float magnitude = (float)Math.sqrt(axis.x*axis.x + axis.y*axis.y + axis.z*axis.z);
		assert (magnitude!=0);
		axis.x /= magnitude;
		axis.y /= magnitude;
		axis.z /= magnitude;

		Matrix3f m = new Matrix3f();
		
		m.m00 = c + axis.x*axis.x*t;
		m.m11 = c + axis.y*axis.y*t;
		m.m22 = c + axis.z*axis.z*t;

		float tmp1 = axis.x*axis.y*t;
		float tmp2 = axis.z*s;
		m.m01 = tmp1 + tmp2;
		m.m10 = tmp1 - tmp2;
		tmp1 = axis.x*axis.z*t;
		tmp2 = axis.y*s;
		m.m02 = tmp1 - tmp2;
		m.m20 = tmp1 + tmp2;    
		tmp1 = axis.y*axis.z*t;
		tmp2 = axis.x*s;
		m.m12 = tmp1 + tmp2;
		m.m21 = tmp1 - tmp2;

		return m;
	}
	/* ***************************************************************************
	 * End of additional code
	 ****************************************************************************/
	
	/**
	 * Entry point for the java program.
	 */
	public static void main(String[] args) 
	{
		new Main().execute();
		System.exit(0);
	}

	/**
	 * Execution of the initialization and the render loop. The display
	 * will be destroyed when the close button is hit and the loop stops.
	 */
	private void execute() 
	{
		try 
		{
			init();
		} 
		catch (LWJGLException oLWJGLException) 
		{
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
	private void init() throws LWJGLException 
	{
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
		GLU.gluPerspective(fov, (float)width/(float)height, 0.1f, 100.0f);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		
		//Load a geometry
		/****************************************************************************
		 * Start of task 1.1/2*
		 ****************************************************************************/
		//ARBBufferObject.glGenBuffersARB(vboid);
		
		//Load a file from an obj...
		
		//ObjImporter objImporter = new ObjImporter("res/geometry/Bunny.obj");
		//numberOfTriangles = objImporter.getTriangleCount();		
		//GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboid.get(0));
		//GL15.glBufferData(GL15.GL_ARRAY_BUFFER, createData(objImporter), GL15.GL_STATIC_DRAW);
		
		//Or test it with the coded cube
		
                World.getInstance().addCube(new Cube(new Vector3f(0,0,0), 1, new float[]{255,0,0, 0.5f}));
                World.getInstance().addCube(new Cube(new Vector3f(0,0,0), 1, new float[]{255,0,0, 0.5f}));
		/*numberOfTriangles = 12;
		ARBBufferObject.glBindBufferARB(GL15.GL_ARRAY_BUFFER, vboid.get(0));
		ARBBufferObject.glBufferDataARB(GL15.GL_ARRAY_BUFFER, Primitives.createCubeData(), GL15.GL_STATIC_DRAW);
		*/
		/****************************************************************************
		 * End of task 1.1/2*
		 ****************************************************************************/
	}
	
	/**
	 * This is the initialization of the open gl window. Here we set up
	 * the display with its viewport and the projection matrix.<br>
	 * <br>
	 * <b><font color = "#ff0000">Task 2:</font></b><br>
	 * In this task, you will have to re-order the data that is loaded from an
	 * obj file and return the data as a buffer for the VBO. The final data buffer
	 * should contain the vertex position and normals in an alternating manner.
	 * You can use the global constant SIZE_OF_DATA for calculating the data
	 * positions in the buffer.<br>
	 * <br>
	 * <i>Hint:</i> The ObjImporter provides the following important functions:
	 * {@link de.rwth_aachen.graphics.ObjImporter#getVertexBuffer() getVertexBuffer()},
	 * {@link de.rwth_aachen.graphics.ObjImporter#getNormalBuffer() getNormalBuffer()},
	 * {@link de.rwth_aachen.graphics.ObjImporter#getDataIndexBuffer() getDataIndexBuffer()},
	 * {@link de.rwth_aachen.graphics.ObjImporter#getDataSize() getDataSize()},
	 * {@link de.rwth_aachen.graphics.ObjImporter#getNormalPosition() getNormalPosition()}
	 * @param objImporter The obj file that should be used for the creation
	 * of the data buffer.
	 * @return A buffer with vertex positions and normals that can be used 
	 * for a VBO
	 */
	private FloatBuffer createData(ObjImporter objImporter)
	{
		FloatBuffer arrayBuffer = FloatBuffer.allocate(SIZE_OF_DATA * 3 * objImporter.getTriangleCount());
		/****************************************************************************
		 * Start of task 2
		 ****************************************************************************/
		// create a Float Buffer to store the vertices
		FloatBuffer vertexBuffer = objImporter.getVertexBuffer();
		// create a Float Buffer to store the normals
		FloatBuffer normalBuffer = objImporter.getNormalBuffer();
		// create a Int Buffer to store the indices
		IntBuffer dataIndexBuffer = objImporter.getDataIndexBuffer();
		
		// reorder the data for the VBO. The vertices and the normals need to be in an interleaved order. 
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
		}
		/****************************************************************************
		 * End of task 2
		 ****************************************************************************/
		return arrayBuffer;
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
	private void loop() 
	{
		long startMillis = System.currentTimeMillis();
		long fpsCheck = System.currentTimeMillis() + 1000;
		long fps = 0;
		
		while (!Display.isCloseRequested()) 
		{
			//Handle all input events.
			//handleEvents();
                        //mouseHandler();
			
			float step = (float)(System.currentTimeMillis() - startMillis);
			
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
			GL11.glMultMatrix(Converter.getBufferFromMatrix(rotation));
                        
                        //Matrix4f movement = new Matrix4f();
                        //movement.m30 = (step / 1000) % 5;
                        //GL11.glMultMatrix(Converter.getBufferFromMatrix(movement));
                        GL11.glColor4f(100, 0, 0, 0.5f);  
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
			 ****************************************************************************/

			//Swap the buffers
			Display.update();
                        
                        mouseHandler();
			
			//Count the frames per second
			long currentMillis = System.currentTimeMillis();
			if(fpsCheck > currentMillis)
			{
				fps++;
			}
			else
			{
				float timeUsed = (float)(fpsCheck - currentMillis + 1000);
				fpsCheck = currentMillis + 1000;
				Display.setTitle("Computer Graphics (" + ((float)fps / (timeUsed / 1000.0f)) + " fps)");
				fps = 0;
			}
		}
	}



	/**
	 * Handle any event from the mouse or the keyboard. <br>
	 * <br>
	 * <b><font color = "#ff0000">Task 3:</font></b><br>
	 * Implement a virtual trackball. Use the mouse to rotate the object. 
	 * Assume that the camera points into the -z direction for simplicity.<br>
	 * <br>
	 * <i>Hint:</i> Please store the current rotation in the global 
	 * {@link #rotation rotation matrix}.
	 * This will be used in the rendering loop. The order of the elements
	 * in a lwjgl matrix is as follows:
	 * <blockquote>
	 * 00 10 20<br>
	 * 01 11 21<br>
	 * 02 12 22<br>
	 * </blockquote>
	 * You can also use the local variable currMousePos (current mouse 
	 * position) and the global variable prevMousePos (previous mouse 
	 * position) to work with the mouse input. The prevMousePos will
	 * be initialized with the current cursor position whenever the left
	 * mouse button is pressed down.
	 */
	private void handleEvents() 
	{
		// get the current mouse position
		Vector2f currMousePos = new Vector2f(Mouse.getX(), Mouse.getY());

		if(currMousePos.x != prevMousePos.x || currMousePos.y != prevMousePos.y) 
		{			
			if(leftMouseButtonDown)
			{
				/****************************************************************************
				 * Start of task 3:
				 ****************************************************************************/
				// convert the screen coordinates to the global coordinates on the unit sphere
				Vector2f globStartPos2f = ScrCS_To_GLCS(prevMousePos);
				Vector2f globCurrPos2f = ScrCS_To_GLCS(currMousePos);
				
				// get the z coordinate for the point on the unit sphere
				float lastZ = (float)Math.sqrt(1.0f - globStartPos2f.x * globStartPos2f.x - globStartPos2f.y * globStartPos2f.y);
				float currZ = (float)Math.sqrt(1.0f - globCurrPos2f.x * globCurrPos2f.x - globCurrPos2f.y * globCurrPos2f.y);
				
				// make the vectors for the rotation computation
				Vector3f globStartPos3f = new Vector3f(globStartPos2f.x, globStartPos2f.y, lastZ);
				Vector3f globCurrPos3f = new Vector3f(globCurrPos2f.x, globCurrPos2f.y, currZ);
				
				// compute the rotation axis and angle
				Vector3f axis = computeRotationAxis(globStartPos3f, globCurrPos3f);
				float angle = computeRotationAngle(globStartPos3f, globCurrPos3f);
				
				// update the Rotation matrix
				Matrix3f.mul(AxisAngle_To_RotMat(axis, angle), rotation, rotation);
				
				// update the previous mouse position
				prevMousePos = currMousePos;
				/****************************************************************************
				 * End of task 3
				 ****************************************************************************/
			}
		}
		while(Mouse.next()) 
		{
			if(Mouse.getEventButton() == 0 && Mouse.getEventButtonState() == true) 
			{
				// Event : the left mouse button clicked
				leftMouseButtonDown = true;
				prevMousePos.x = Mouse.getX();
				prevMousePos.y = Mouse.getY();
			}
			else if(Mouse.getEventButton() == 0 && Mouse.getEventButtonState() == false) 
			{
				// Event : the left mouse button released
				leftMouseButtonDown = false;
			}
        	}
        }
        
        
        private void mouseHandler(){
            if(!Mouse.isButtonDown(0)){
                return;
            }
            Vector2f mousePos = new Vector2f(Mouse.getX(), Mouse.getY());

            if(World.getInstance().objectAtScreenPosition(mousePos) != null){
                System.out.println("HIT!");
            }
            
            
        }
    
	/**
	 * Destroy the display. This is a simple method right now, but later we can
	 * clean up other things within this function. <br>
	 * <br>
	 * <b><font color = "#ff0000">Task 1.1*:</font></b>
	 * Clean up any created buffers.
	 */
	private void destroy() 
	{
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
