/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cg_project;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBBufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author christiandernehl
 */
public class World {

    private static World instance = new World();
    private Matrix4f objectEntrance;
    private Vector3f direction;
    private Matrix4f movement;
    private Vector3f center;
    private float xlen;
    private float ylen;
    private float zlen;    //! how long the object travels from begin to end position
    private int objectDuration = 500;
    private TreeMap<Integer, Cube> objects;
    private IntBuffer vboObjectIds;
    private final int maxCubes = 100;
    private int cubeId = 0;

    private World() {     
        //setup inital / final position


        center = new Vector3f(0, 0, 0);
        xlen = ylen = zlen = 15;

        objectEntrance = new Matrix4f();
        objectEntrance.m30 = center.x - xlen / 2f + 1;

        direction = new Vector3f(1f / objectDuration, 0, -1f / objectDuration);
        movement = new Matrix4f();
        movement.load(objectEntrance);

        objects = new TreeMap<Integer, Cube>();
        vboObjectIds = IntBuffer.allocate(maxCubes);
        ARBBufferObject.glGenBuffersARB(vboObjectIds);
    }

    public static World getInstance() {
        return instance;
    }

    public synchronized void addCube(Cube c) {
        ARBBufferObject.glBindBufferARB(GL15.GL_ARRAY_BUFFER, vboObjectIds.get(cubeId));
        ARBBufferObject.glBufferDataARB(GL15.GL_ARRAY_BUFFER, Primitives.createCubeData(), GL15.GL_STATIC_DRAW);
        c.setId(cubeId);
        float[] color = c.getColor();
        c.setColor(new float[] {color[0], color[1], color[2], cubeId/(float)maxCubes});
        objects.put(cubeId, c);
        cubeId++;
        if(cubeId >= maxCubes){
            cubeId = 0;
        }
    }
    
    public synchronized void removeCube(int cubeId) {
        IntBuffer b = IntBuffer.allocate(1);
        b.put(0, vboObjectIds.get(cubeId));
        ARBBufferObject.glDeleteBuffersARB(b);
        objects.remove(cubeId);
    }

    public boolean objectInWorld(Cube c) {
        Vector3f objCenter = c.getCenter();
        float objSize = c.getSize();
        if (Math.abs(objCenter.x - center.x) < (xlen - objSize) / 2f) {
            if (Math.abs(objCenter.y - center.y) < (ylen - objSize) / 2f) {
                if (Math.abs(objCenter.z - center.z) < (zlen - objSize) / 2f) {
                    return true;
                }
            }
        }
        return false;
    }

    public void drawObject(float step, Cube c) {
        int objectId = c.getId();
        Matrix4f mv = new Matrix4f();
        mv.load(objectEntrance);
        //move cube
        c.setCenter(Vector3f.add(c.getCenter(), direction, null));  
        mv.translate(c.getCenter());
        GL11.glPushMatrix();
        GL11.glMultMatrix(Converter.getBufferFromMatrix(mv));
        
        final int numberOfTriangles = 12;
        final int SIZE_OF_DATA = 6;
        final int SIZE_OF_FLOAT = 4;
        ARBBufferObject.glBindBufferARB(GL15.GL_ARRAY_BUFFER, vboObjectIds.get(objectId));

        float[] cubeColor = c.getColor();
        GL11.glColor4f(cubeColor[0], cubeColor[1], cubeColor[2], cubeColor[3]);

        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);

        GL11.glVertexPointer(3, GL11.GL_FLOAT, SIZE_OF_DATA * SIZE_OF_FLOAT, 0);
        GL11.glNormalPointer(GL11.GL_FLOAT, SIZE_OF_DATA * SIZE_OF_FLOAT, 3 * SIZE_OF_FLOAT);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3 * numberOfTriangles);
        
        GL11.glPopMatrix();

        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);

        ARBBufferObject.glBindBufferARB(GL15.GL_ARRAY_BUFFER, 0);
        
    }

    public synchronized void draw(float step) {
        Iterator<Cube> it = objects.values().iterator();
        LinkedList<Integer> cubesToRemove = new LinkedList<Integer>();
        while (it.hasNext()) {
            Cube c = it.next();
            if (objectInWorld(c)) {
                drawObject(step, c);
            } else {           
                cubesToRemove.add(c.getId());
            }                        
        }
        if(cubesToRemove.size() == 0){
            return;
        }
        Integer[] cubesArray = new Integer[cubesToRemove.size()];
        cubesToRemove.toArray(cubesArray);
        for(int i = 0; i < cubesArray.length; ++i){
            removeCube(cubesArray[i]);
        }        
    }

    public Cube objectAtScreenPosition(Vector2f pos) {
        FloatBuffer color = BufferUtils.createFloatBuffer(4);
        //in pixels
        final int mouseHeight = 1;
        final int mouseWidth = 1;
        
        GL11.glPushClientAttrib(GL11.GL_CLIENT_PIXEL_STORE_BIT);
        
        GL11.glPixelStorei(GL11.GL_UNPACK_SWAP_BYTES, GL11.GL_FALSE);
        GL11.glPixelStorei(GL11.GL_UNPACK_SWAP_BYTES, GL11.GL_FALSE);
        GL11.glPixelStorei(GL11.GL_UNPACK_LSB_FIRST, GL11.GL_FALSE);
        GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

        GL11.glReadPixels((int) pos.x, (int) pos.y, mouseWidth, mouseHeight, GL11.GL_RGBA, GL11.GL_FLOAT, color);
        GL11.glPopClientAttrib();
        
        Iterator<Cube> it = objects.values().iterator();
        float[] cubeColor;
        Cube c;
        int alpha = Math.round(color.get(3)*maxCubes);
        
        while (it.hasNext()) {
            c = it.next();
            cubeColor = c.getColor();            
            if (alpha == (int) (cubeColor[3] * maxCubes)) {
                return c;
            }
        }
        return null;
    }
}
