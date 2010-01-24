package cg_project;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL11;

/**
 * Some provided data for rendering and testing VBOs.
 */
class Primitives
{
	/**
	 * Create vertex data for a cube. The data will be structured as follows: (VxVyVzNxNyNz)* where VxVyVz are the vertex positions and NxNyNz are the normal vectors.
	 * @return Vertices and normals of a cube.
	 */
	public static FloatBuffer createCubeData() 
	{
		return FloatBuffer.wrap(new float[] {
			-1.0f,  1.0f, -1.0f,  0.0f,  1.0f,  0.0f,
			-1.0f,  1.0f,  1.0f,  0.0f,  1.0f,  0.0f,
			 1.0f,  1.0f,  1.0f,  0.0f,  1.0f,  0.0f,
			 
			 1.0f,  1.0f,  1.0f,  0.0f,  1.0f,  0.0f,
			 1.0f,  1.0f, -1.0f,  0.0f,  1.0f,  0.0f,
			-1.0f,  1.0f, -1.0f,  0.0f,  1.0f,  0.0f,
			
			-1.0f, -1.0f, -1.0f,  0.0f, -1.0f,  0.0f,
			 1.0f, -1.0f, -1.0f,  0.0f, -1.0f,  0.0f,
			 1.0f, -1.0f,  1.0f,  0.0f, -1.0f,  0.0f,
			 
			 1.0f, -1.0f,  1.0f,  0.0f, -1.0f,  0.0f,
			-1.0f, -1.0f,  1.0f,  0.0f, -1.0f,  0.0f,
			-1.0f, -1.0f, -1.0f,  0.0f, -1.0f,  0.0f,
			 
			-1.0f, -1.0f,  1.0f,  0.0f,  0.0f,  1.0f,
			 1.0f, -1.0f,  1.0f,  0.0f,  0.0f,  1.0f,
			 1.0f,  1.0f,  1.0f,  0.0f,  0.0f,  1.0f,
			 
			 1.0f,  1.0f,  1.0f,  0.0f,  0.0f,  1.0f,
			-1.0f,  1.0f,  1.0f,  0.0f,  0.0f,  1.0f,
			-1.0f, -1.0f,  1.0f,  0.0f,  0.0f,  1.0f,
			 
			-1.0f, -1.0f, -1.0f,  0.0f,  0.0f, -1.0f,
			-1.0f,  1.0f, -1.0f,  0.0f,  0.0f, -1.0f,
			 1.0f,  1.0f, -1.0f,  0.0f,  0.0f, -1.0f,
			 
			 1.0f,  1.0f, -1.0f,  0.0f,  0.0f, -1.0f,
			 1.0f, -1.0f, -1.0f,  0.0f,  0.0f, -1.0f,
			-1.0f, -1.0f, -1.0f,  0.0f,  0.0f, -1.0f,
			
			 1.0f, -1.0f, -1.0f,  1.0f,  0.0f,  0.0f,
			 1.0f,  1.0f, -1.0f,  1.0f,  0.0f,  0.0f,
			 1.0f,  1.0f,  1.0f,  1.0f,  0.0f,  0.0f,
			 
			 1.0f,  1.0f,  1.0f,  1.0f,  0.0f,  0.0f,
			 1.0f, -1.0f,  1.0f,  1.0f,  0.0f,  0.0f,
			 1.0f, -1.0f, -1.0f,  1.0f,  0.0f,  0.0f,
						
			-1.0f, -1.0f, -1.0f, -1.0f,  0.0f,  0.0f,
			-1.0f, -1.0f,  1.0f, -1.0f,  0.0f,  0.0f,
			-1.0f,  1.0f,  1.0f, -1.0f,  0.0f,  0.0f,
			
			-1.0f,  1.0f,  1.0f, -1.0f,  0.0f,  0.0f,
			-1.0f,  1.0f, -1.0f, -1.0f,  0.0f,  0.0f,
			-1.0f, -1.0f, -1.0f, -1.0f,  0.0f,  0.0f});
	}
	
	/**
	* Render intermediate cube.
	*/
	public static void drawCube() 
	{
		GL11.glBegin(GL11.GL_QUADS);
			GL11.glNormal3f(0.0f, 1.0f, 0.0f);
                        GL11.glTexCoord2f(0f, 0f);
			GL11.glVertex3f(-1.0f, 1.0f, -1.0f);
			GL11.glVertex3f(-1.0f, 1.0f, 1.0f);
			GL11.glVertex3f(1.0f, 1.0f, 1.0f);
			GL11.glVertex3f(1.0f, 1.0f, -1.0f);
			
			GL11.glNormal3f(0.0f, -1.0f, 0.0f);
                        GL11.glTexCoord2f(1f, 0f);
			GL11.glVertex3f(-1.0f, -1.0f, -1.0f);
			GL11.glVertex3f(1.0f, -1.0f, -1.0f);
			GL11.glVertex3f(1.0f, -1.0f, 1.0f);
			GL11.glVertex3f(-1.0f, -1.0f, 1.0f);
			
			GL11.glNormal3f(0.0f, 0.0f, 1.0f);
                        GL11.glTexCoord2f(1f, 1f);
			GL11.glVertex3f(-1.0f, -1.0f, 1.0f);
			GL11.glVertex3f(1.0f, -1.0f, 1.0f);
			GL11.glVertex3f(1.0f, 1.0f, 1.0f);
			GL11.glVertex3f(-1.0f, 1.0f, 1.0f);
			
			GL11.glNormal3f(0.0f, 0.0f, -1.0f);
                        GL11.glTexCoord2f(0f, 1f);
			GL11.glVertex3f(-1.0f, -1.0f, -1.0f);
			GL11.glVertex3f(-1.0f, 1.0f, -1.0f);
			GL11.glVertex3f(1.0f, 1.0f, -1.0f);
			GL11.glVertex3f(1.0f, -1.0f, -1.0f);
			
			GL11.glNormal3f(1.0f, 0.0f, 0.0f);
			GL11.glVertex3f(1.0f, -1.0f, -1.0f);
			GL11.glVertex3f(1.0f, 1.0f, -1.0f);
			GL11.glVertex3f(1.0f, 1.0f, 1.0f);
			GL11.glVertex3f(1.0f, -1.0f, 1.0f);
			
			GL11.glNormal3f(-1.0f, 0.0f, 0.0f);
			GL11.glVertex3f(-1.0f, -1.0f, -1.0f);
			GL11.glVertex3f(-1.0f, -1.0f, 1.0f);
			GL11.glVertex3f(-1.0f, 1.0f, 1.0f);
			GL11.glVertex3f(-1.0f, 1.0f, -1.0f);
		GL11.glEnd();
	}
}
