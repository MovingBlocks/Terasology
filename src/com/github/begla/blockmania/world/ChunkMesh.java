package com.github.begla.blockmania.world;

import gnu.trove.iterator.TFloatIterator;
import gnu.trove.list.array.TFloatArrayList;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL13;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;

public class ChunkMesh extends RenderableObject {

    private int _displayListOpaque = -1;
    private int _displayListTranslucent = -1;
    private int _displayListBillboard = -1;

    public TFloatArrayList quadsTranslucent;
    public TFloatArrayList normalsTranslucent;
    public TFloatArrayList texTranslucent;
    public TFloatArrayList colorTranslucent;
    public TFloatArrayList texLightTranslucent;
    public TFloatArrayList quadsOpaque;
    public TFloatArrayList normalsOpaque;
    public TFloatArrayList texOpaque;
    public TFloatArrayList colorOpaque;
    public TFloatArrayList texLightOpaque;
    public TFloatArrayList quadsBillboard;
    public TFloatArrayList texBillboard;
    public TFloatArrayList colorBillboard;
    public TFloatArrayList texLightBillboard;

    boolean _generated;

    public ChunkMesh() {
        quadsTranslucent = new TFloatArrayList();
        normalsOpaque = new TFloatArrayList();
        normalsTranslucent = new TFloatArrayList();
        texTranslucent = new TFloatArrayList();
        colorTranslucent = new TFloatArrayList();
        quadsOpaque = new TFloatArrayList();
        texOpaque = new TFloatArrayList();
        colorOpaque = new TFloatArrayList();
        quadsBillboard = new TFloatArrayList();
        texBillboard = new TFloatArrayList();
        colorBillboard = new TFloatArrayList();

        texLightTranslucent = new TFloatArrayList();
        texLightOpaque = new TFloatArrayList();
        texLightBillboard = new TFloatArrayList();
    }

    /**
     * Generates the display lists from the pre calculated arrays.
     */
    public void generateDisplayLists() throws Exception {
        // IMPORTANT: A mesh can only be generated once.
        if (_generated)
            throw new Exception("A chunk mesh can only be generated once.");

        /*
        * Create the display lists if necessary.
        */
        if (_displayListOpaque == -1) {
            _displayListOpaque = glGenLists(1);
        }

        if (_displayListTranslucent == -1) {
            _displayListTranslucent = glGenLists(1);
        }

        if (_displayListBillboard == -1) {
            _displayListBillboard = glGenLists(1);
        }

        FloatBuffer nb;
        FloatBuffer cb;
        FloatBuffer tb;
        FloatBuffer tb2;
        FloatBuffer vb;

        nb = BufferUtils.createFloatBuffer(normalsOpaque.size());

        for (TFloatIterator it = normalsOpaque.iterator(); it.hasNext(); ) {
            nb.put(it.next());
        }

        vb = BufferUtils.createFloatBuffer(quadsOpaque.size());

        for (TFloatIterator it = quadsOpaque.iterator(); it.hasNext(); ) {
            vb.put(it.next());
        }

        tb = BufferUtils.createFloatBuffer(texOpaque.size());

        for (TFloatIterator it = texOpaque.iterator(); it.hasNext(); ) {
            tb.put(it.next());
        }

        tb2 = BufferUtils.createFloatBuffer(texLightOpaque.size());

        for (TFloatIterator it = texLightOpaque.iterator(); it.hasNext(); ) {
            tb2.put(it.next());
        }

        cb = BufferUtils.createFloatBuffer(colorOpaque.size());

        for (TFloatIterator it = colorOpaque.iterator(); it.hasNext(); ) {
            cb.put(it.next());
        }

        vb.flip();
        tb.flip();
        tb2.flip();
        cb.flip();
        nb.flip();

        generateDisplayList(_displayListOpaque, vb, tb, tb2, cb, nb);

        nb = BufferUtils.createFloatBuffer(normalsTranslucent.size());

        for (TFloatIterator it = normalsTranslucent.iterator(); it.hasNext(); ) {
            nb.put(it.next());
        }

        vb = BufferUtils.createFloatBuffer(quadsTranslucent.size());

        for (TFloatIterator it = quadsTranslucent.iterator(); it.hasNext(); ) {
            vb.put(it.next());
        }

        tb = BufferUtils.createFloatBuffer(texTranslucent.size());

        for (TFloatIterator it = texTranslucent.iterator(); it.hasNext(); ) {
            tb.put(it.next());
        }


        tb2 = BufferUtils.createFloatBuffer(texLightTranslucent.size());

        for (TFloatIterator it = texLightTranslucent.iterator(); it.hasNext(); ) {
            tb2.put(it.next());
        }


        cb = BufferUtils.createFloatBuffer(colorTranslucent.size());

        for (TFloatIterator it = colorTranslucent.iterator(); it.hasNext(); ) {
            cb.put(it.next());
        }

        vb.flip();
        tb.flip();
        tb2.flip();
        cb.flip();
        nb.flip();

        generateDisplayList(_displayListTranslucent, vb, tb, tb2, cb, nb);

        vb = BufferUtils.createFloatBuffer(quadsBillboard.size());

        for (TFloatIterator it = quadsBillboard.iterator(); it.hasNext(); ) {
            vb.put(it.next());
        }


        tb = BufferUtils.createFloatBuffer(texBillboard.size());

        for (TFloatIterator it = texBillboard.iterator(); it.hasNext(); ) {
            tb.put(it.next());
        }


        tb2 = BufferUtils.createFloatBuffer(texLightBillboard.size());

        for (TFloatIterator it = texLightBillboard.iterator(); it.hasNext(); ) {
            tb2.put(it.next());
        }


        cb = BufferUtils.createFloatBuffer(colorBillboard.size());

        for (TFloatIterator it = colorBillboard.iterator(); it.hasNext(); ) {
            cb.put(it.next());
        }

        vb.flip();
        tb.flip();
        tb2.flip();
        cb.flip();

        generateDisplayList(_displayListBillboard, vb, tb, tb2, cb, null);

        // IMPORTANT: Free unused memory!!
        quadsTranslucent.clear();
        normalsOpaque.clear();
        normalsTranslucent.clear();
        texTranslucent.clear();
        colorTranslucent.clear();
        quadsOpaque.clear();
        texOpaque.clear();
        colorOpaque.clear();
        quadsBillboard.clear();
        texBillboard.clear();
        colorBillboard.clear();

        texLightTranslucent.clear();
        texLightOpaque.clear();
        texLightBillboard.clear();

        // Make sure this mesh can not be generated again
        _generated = true;
    }

    private void generateDisplayList(int displayList, FloatBuffer vb, FloatBuffer tb, FloatBuffer tb2, FloatBuffer cb, FloatBuffer nb) {
        glNewList(displayList, GL_COMPILE);

        if (vb == null || tb == null || tb2 == null || cb == null) {
            return;
        }

        glEnableClientState(GL_VERTEX_ARRAY);
        glVertexPointer(3, 0, vb);

        GL13.glClientActiveTexture(GL13.GL_TEXTURE0);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glTexCoordPointer(2, 0, tb);

        GL13.glClientActiveTexture(GL13.GL_TEXTURE1);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glTexCoordPointer(2, 0, tb2);

        glEnableClientState(GL_COLOR_ARRAY);
        glColorPointer(4, 0, cb);

        if (nb != null) {
            glEnableClientState(GL_NORMAL_ARRAY);
            glNormalPointer(0, nb);
        }

        glDrawArrays(GL_QUADS, 0, vb.capacity() / 3);

        glDisableClientState(GL_COLOR_ARRAY);

        if (nb != null) {
            glDisableClientState(GL_NORMAL_ARRAY);
        }

        GL13.glClientActiveTexture(GL13.GL_TEXTURE0);
        glDisableClientState(GL_TEXTURE_COORD_ARRAY);
        glTexCoordPointer(2, 0, tb);

        GL13.glClientActiveTexture(GL13.GL_TEXTURE1);
        glDisableClientState(GL_TEXTURE_COORD_ARRAY);
        glTexCoordPointer(2, 0, tb2);

        glDisableClientState(GL_VERTEX_ARRAY);
        glEndList();
    }

    public void render(boolean translucent) {
        if (!translucent) {
            if (_displayListOpaque != -1)
                glCallList(_displayListOpaque);
        } else {
            glEnable(GL_BLEND);
            glEnable(GL_ALPHA_TEST);
            glAlphaFunc(GL_GREATER, 0.5f);

            if (_displayListTranslucent != -1)
                glCallList(_displayListTranslucent);


            glDisable(GL_CULL_FACE);
            if (_displayListBillboard != -1)
                glCallList(_displayListBillboard);
            glEnable(GL_CULL_FACE);

            glDisable(GL_BLEND);
            glDisable(GL_ALPHA_TEST);
        }
    }

    @Override
    public void render() {
        render(false);
        render(true);
    }

    @Override
    public void update() {
        // Do nothing.
    }
}
