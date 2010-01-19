/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cg_project;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.ListIterator;
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
    private LinkedList<Cube> objects;
    private IntBuffer vboObjectIds;

    private World() {
        //setup inital / final position


        center = new Vector3f(0, 0, 0);
        xlen = ylen = zlen = 7;

        objectEntrance = new Matrix4f();
        objectEntrance.m30 = center.x - xlen / 2f + 1;

        direction = new Vector3f(1f / objectDuration, 0, -1f / objectDuration);
        movement = new Matrix4f();
        movement.load(objectEntrance);

        objects = new LinkedList<Cube>();
        vboObjectIds = IntBuffer.allocate(2);
        ARBBufferObject.glGenBuffersARB(vboObjectIds);
    }

    public static World getInstance() {
        return instance;
    }

    public void addCube(Cube c) {
        
        ARBBufferObject.glBindBufferARB(GL15.GL_ARRAY_BUFFER, vboObjectIds.get(objects.size()));
        ARBBufferObject.glBufferDataARB(GL15.GL_ARRAY_BUFFER, Primitives.createCubeData(), GL15.GL_STATIC_DRAW);
        objects.add(c);
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

    public void draw(float step) {

        final int numberOfTriangles = 12;
        final int SIZE_OF_DATA = 6;
        final int SIZE_OF_FLOAT = 4;

        int i = 0;
        ListIterator<Cube> it = objects.listIterator();
        while (it.hasNext()) {
            Cube c = it.next();
            if (!objectInWorld(c)) {
                movement.load(objectEntrance);
                c.setToInitialPosition();
            } else {
                movement.translate(direction);
                c.setCenter(c.getCenter().translate(direction.x, direction.y, direction.z));
            }
            GL11.glMultMatrix(Converter.getBufferFromMatrix(movement));

            ARBBufferObject.glBindBufferARB(GL15.GL_ARRAY_BUFFER, vboObjectIds.get(i));
            
            float[] cubeColor = c.getColor();
            GL11.glColor4f(cubeColor[0], cubeColor[1], cubeColor[2], cubeColor[3]);

            GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
            GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);

            GL11.glVertexPointer(3, GL11.GL_FLOAT, SIZE_OF_DATA * SIZE_OF_FLOAT, 0);
            GL11.glNormalPointer(GL11.GL_FLOAT, SIZE_OF_DATA * SIZE_OF_FLOAT, 3 * SIZE_OF_FLOAT);
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3 * numberOfTriangles);

            GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
            GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);

            ARBBufferObject.glBindBufferARB(GL15.GL_ARRAY_BUFFER, 0);
            i++;

        }
    }

    public Cube objectAtScreenPosition(Vector2f pos) {
        FloatBuffer color = BufferUtils.createFloatBuffer(4);
        //in pixels
        final int mouseHeight = 1;
        final int mouseWidth = 1;
        GL11.glReadPixels((int) pos.x, (int) pos.y, mouseWidth, mouseHeight, GL11.GL_RGBA, GL11.GL_FLOAT, color);
        ListIterator<Cube> it = objects.listIterator();
        float[] cubeColor;
        Cube c;
        while (it.hasNext()) {
            c = it.next();
            cubeColor = c.getColor();
            float alpha = color.get(3);
            if ((int) (alpha * 100) == (int) (cubeColor[3] * 100)) {
                return c;
            }
        }
        return null;
    }
}
