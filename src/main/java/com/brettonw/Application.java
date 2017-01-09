/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.brettonw;

import com.jogamp.newt.*;
import com.jogamp.newt.event.*;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import glm.vec._2.i.Vec2i;
import glm.vec._4.Vec4;
import glutil.BufferUtils;
import glutil.GlDebugOutput;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;

public class Application implements GLEventListener {

    private final String SHADERS_ROOT = "src/main/shaders";
    private final String VERT_SHADER_SOURCE = "vertex-shader";
    private final String FRAG_SHADER_SOURCE = "fragment-shader";
    private static final int POSITION = 0;

    private final boolean DEBUG = false;
    protected GLWindow glWindow;
    protected Animator animator;
    protected Vec2i windowSize = new Vec2i(500);
    protected FloatBuffer clearColor = GLBuffers.newDirectFloatBuffer(4),
            clearDepth = GLBuffers.newDirectFloatBuffer(1);
    public static FloatBuffer matBuffer = GLBuffers.newDirectFloatBuffer(16),
            vecBuffer = GLBuffers.newDirectFloatBuffer(4);


    public static void main(String[] args) {
        new Application ("Hello World");
    }

    public Application (String title) {
        Display display = NewtFactory.createDisplay(null);
        Screen screen = NewtFactory.createScreen(display, 0);
        GLProfile glProfile = GLProfile.get(GLProfile.GL2ES1);
        GLCapabilities glCapabilities = new GLCapabilities(glProfile);

        glWindow = GLWindow.create(screen, glCapabilities);

        if (DEBUG) {
            glWindow.setContextCreationFlags(GLContext.CTX_OPTION_DEBUG);
        }

        glWindow.setUndecorated(false);
        glWindow.setAlwaysOnTop(false);
        glWindow.setFullscreen(false);
        glWindow.setPointerVisible(true);
        glWindow.confinePointer(false);
        glWindow.setTitle(title);
        glWindow.setSize(windowSize.x, windowSize.y);

        glWindow.setVisible(true);

        if (DEBUG) {
            glWindow.getContext().addGLDebugListener(new GlDebugOutput ());
        }

        glWindow.addGLEventListener(this);

        animator = new Animator();
        animator.add(glWindow);
        animator.start();
    }

    private int theProgram;
    private IntBuffer positionBufferObject = GLBuffers.newDirectIntBuffer(1), vao = GLBuffers.newDirectIntBuffer(1);
    private float[] vertexPositions = {
        +0.75f, +0.75f, 0.0f, 1.0f,
        +0.75f, -0.75f, 0.0f, 1.0f,
        -0.75f, -0.75f, 0.0f, 1.0f};

    @Override
    public final void init(GLAutoDrawable drawable) {
        GL3 gl = drawable.getGL().getGL3();

        initializeProgram(gl);

        initializeVertexBuffer(gl);

        gl.glGenVertexArrays(1, vao);
        gl.glBindVertexArray(vao.get(0));
    }

    private void initializeProgram(GL3 gl) {
        ShaderProgram shaderProgram = new ShaderProgram();

        ShaderCode vertShaderCode = ShaderCode.create(gl, GL_VERTEX_SHADER, this.getClass(), SHADERS_ROOT, null, VERT_SHADER_SOURCE, "vert", null, true);
        ShaderCode fragShaderCode = ShaderCode.create(gl, GL_FRAGMENT_SHADER, this.getClass(), SHADERS_ROOT, null, FRAG_SHADER_SOURCE, "frag", null, true);

        shaderProgram.add(vertShaderCode);
        shaderProgram.add(fragShaderCode);

        shaderProgram.link(gl, System.out);

        theProgram = shaderProgram.program();

        vertShaderCode.destroy(gl);
        fragShaderCode.destroy(gl);
    }

    private void initializeVertexBuffer(GL3 gl) {
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexPositions);

        gl.glGenBuffers(1, positionBufferObject);

        gl.glBindBuffer(GL_ARRAY_BUFFER, positionBufferObject.get(0));
        gl.glBufferData(GL_ARRAY_BUFFER, vertexBuffer.capacity() * Float.BYTES, vertexBuffer, GL_STATIC_DRAW);
        gl.glBindBuffer(GL_ARRAY_BUFFER, 0);

        BufferUtils.destroyDirectBuffer(vertexBuffer);
    }

    @Override
    public final void display(GLAutoDrawable drawable) {
        GL3 gl = drawable.getGL().getGL3();

        gl.glClearBufferfv(GL_COLOR, 0, clearColor.put(0, 0.0f).put(1, 0.0f).put(2, 0.0f).put(3, 1.0f));

        gl.glUseProgram(theProgram);

        gl.glBindBuffer(GL_ARRAY_BUFFER, positionBufferObject.get(0));
        gl.glEnableVertexAttribArray(POSITION);
        gl.glVertexAttribPointer(POSITION, 4, GL_FLOAT, false, Vec4.SIZE, 0);

        gl.glDrawArrays(GL_TRIANGLES, 0, 3);

        gl.glDisableVertexAttribArray(POSITION);
        gl.glUseProgram(0);
    }

    @Override
    public final void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL3 gl = drawable.getGL().getGL3();
        gl.glViewport(0, 0, width, height);
    }

    @Override
    public final void dispose(GLAutoDrawable drawable) {

        GL3 gl = drawable.getGL().getGL3();

        gl.glDeleteProgram(theProgram);
        gl.glDeleteBuffers(1, positionBufferObject);
        gl.glDeleteVertexArrays(1, vao);

        BufferUtils.destroyDirectBuffer(positionBufferObject);
        BufferUtils.destroyDirectBuffer(vao);

        BufferUtils.destroyDirectBuffer(clearColor);
        BufferUtils.destroyDirectBuffer(clearDepth);
        BufferUtils.destroyDirectBuffer(matBuffer);
        BufferUtils.destroyDirectBuffer(vecBuffer);

        System.exit(0);
    }

}
