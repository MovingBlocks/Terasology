package org.terasology.rendering.opengl;

import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by michaelpollind on 3/22/16.
 *
 * This is an intermediary state machine to avoid additional calls to OpenGL
 */
public class OpenGlStateMachine {

    private static final Logger logger = LoggerFactory.getLogger(OpenGlStateMachine.class);

    //active states
    private HashSet<GLState> activeStates = new HashSet<GLState>();

    //Factors for blending
    private GLBlendState sFactor;
    private GLBlendState dFactor;

    private  GLCull culling = GLCull.T_GL_BACK;

    public  enum  GLCull {
        T_GL_FRONT(GL_FRONT),
        T_GL_BACK(GL_BACK),
        T_GL_FRONT_AND_BACK(GL_FRONT_AND_BACK);

        private final int value;
        private GLCull(int value) {
            this.value = value;
        }

        public  int getValue() {
            return this.value;
        }
    }


    public  enum  GLState {
        T_GL_BLEND(GL_BLEND),
        T_GL_CULL_FACE(GL_CULL_FACE),
        T_GL_DEPTH_TEST(GL_DEPTH_TEST),
        T_GL_DITHER(GL_DITHER),
        T_GL_POLYGON_OFFSET_FILL(GL_POLYGON_OFFSET_FILL),
        T_GL_SCISSOR_TEST(GL_SCISSOR_TEST),
        T_GL_STENCIL_TEST(GL_STENCIL_TEST);

        private final int value;
        private GLState(int value) {
            this.value = value;
        }

        public  int getValue() {
            return this.value;
        }
    };

    public enum GLBlendState {
        T_GL_ZERO(GL_ZERO),
        T_GL_ONE(GL_ONE),
        T_GL_SRC_COLOR(GL_SRC_COLOR),
        T_GL_ONE_MINUS_SRC_COLOR(GL_ONE_MINUS_SRC_COLOR),
        T_GL_DST_COLOR(GL_DST_COLOR),
        T_GL_ONE_MINUS_DST_COLOR(GL_ONE_MINUS_DST_COLOR),
        T_GL_SRC_ALPHA(GL_SRC_ALPHA),
        T_GL_ONE_MINUS_SRC_ALPHA(GL_ONE_MINUS_SRC_ALPHA),
        T_GL_DST_ALPHA(GL_DST_ALPHA),
        T_GL_ONE_MINUS_DST_ALPHA(GL_ONE_MINUS_DST_ALPHA),
        T_GL_CONSTANT_COLOR(GL_CONSTANT_COLOR),
        T_GL_ONE_MINUS_CONSTANT_COLOR(GL_ONE_MINUS_CONSTANT_COLOR),
        T_GL_CONSTANT_ALPHA(GL_CONSTANT_ALPHA),
        T_GL_ONE_MINUS_CONSTANT_ALPHA(GL_ONE_MINUS_CONSTANT_ALPHA),
        T_GL_SRC_ALPHA_SATURATE(GL_SRC_ALPHA_SATURATE);

        private final int value;
        private GLBlendState(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    };

    public void glCull(GLCull cull) {
        if(this.culling != cull) {
            this.culling = cull;
            glCullFace(this.culling.getValue());
        }
    }

    public  GLCull getCull() {
        return  this.culling;
    }


    public void  glBlend(GLBlendState sfactor, GLBlendState dfactor) {
        if(this.sFactor != sfactor || this.dFactor != dfactor) {
            glBlendFunc(sfactor.getValue(),dfactor.getValue());
            this.sFactor = sfactor;
            this.dFactor = dfactor;
        }
    }

    /**
     * Enables any gl states passed to OpenGL
     *
     * @param states the gl state to disable
     */
    public void glEnable(GLState states[]) {
        int result = 0;
        for (int x = 0; x < states.length; x++) {
            if(!activeStates.contains(states[x])) {
                result |= states[x].getValue();
                activeStates.add(states[x]);
            }
        }
        if(result != 0)
            GL11.glEnable(result);
    }

    /**
     * Disable any gl states passed to OpenGL
     *
     * @param states the gl state to disable
     */
    public void glDisable(GLState states[]) {
        int result = 0;
        for (int x = 0; x < states.length; x++) {
            if(activeStates.contains(states[x])) {
                result |= states[x].getValue();
                activeStates.remove(states[x]);
            }
        }
        if(result != 0)
        GL11.glDisable(result);
    }

    public GLState[] getActiveStates() {
        return  (GLState[])activeStates.toArray();
    }


}
