package openvrprovider;

import jopenvr.JOpenVRLibrary;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.opengl.GL11.GL_DEPTH_COMPONENT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;

/* This class is designed to manage the framebuffers for the headset. */
public class OpenVRStereoRenderer {
    public OpenVRProvider vrProvider;

    // TextureIDs of framebuffers for each eye
    private int eyeTextureIDs[] = new int[2];
    private int eyeFBOIds[] = new int[2];

    public OpenVRStereoRenderer(OpenVRProvider _vrProvider, int lwidth, int lheight) {
        vrProvider = _vrProvider;
        createRenderTexture(lwidth, lheight);
    }

    public void deleteRenderTextures() {
        if (eyeTextureIDs[0] > 0) GL11.glDeleteTextures(eyeTextureIDs[0]);
    }

    public void createRenderTexture(int lwidth, int lheight) {
        for (int nEye = 0; nEye < 2; nEye++) {
            eyeFBOIds[nEye] = glGenFramebuffersEXT();
            EXTFramebufferObject.glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, eyeFBOIds[nEye]);

            //depth buffer
            int depthbuffer = glGenRenderbuffersEXT();
            glBindRenderbufferEXT(GL_RENDERBUFFER_EXT, depthbuffer);

            //allocate space for the renderbuffer
            glRenderbufferStorageEXT(GL_RENDERBUFFER_EXT, GL_DEPTH_COMPONENT, lwidth, lheight);

            //attach depth buffer to fbo
            glFramebufferRenderbufferEXT(GL_FRAMEBUFFER_EXT, GL_DEPTH_ATTACHMENT_EXT, GL_RENDERBUFFER_EXT, depthbuffer);

            eyeTextureIDs[nEye] = GL11.glGenTextures();
            int boundTextureId = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
            GL11.glBindTexture(GL_TEXTURE_2D, eyeTextureIDs[nEye]);
            GL11.glEnable(GL_TEXTURE_2D);
            GL11.glTexParameterf(GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameterf(GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexImage2D(GL_TEXTURE_2D, 0, GL11.GL_RGBA8, lwidth, lheight, 0, GL11.GL_RGBA, GL11.GL_INT, (java.nio.ByteBuffer) null);
            GL11.glBindTexture(GL_TEXTURE_2D, boundTextureId);

            //attach texture to the fbo
            glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT, GL_TEXTURE_2D, eyeTextureIDs[nEye], 0);

            //check completeness
            if (glCheckFramebufferStatusEXT(GL_FRAMEBUFFER_EXT) != GL_FRAMEBUFFER_COMPLETE_EXT) {
                System.out.println("An error occured creating the frame buffer.");
            }

            vrProvider.texType[nEye].handle = eyeTextureIDs[nEye];
            vrProvider.texType[nEye].eColorSpace = JOpenVRLibrary.EColorSpace.EColorSpace_ColorSpace_Gamma;
            vrProvider.texType[nEye].eType = JOpenVRLibrary.EGraphicsAPIConvention.EGraphicsAPIConvention_API_OpenGL;
            vrProvider.texType[nEye].write();
        }
    }

    public int getTextureHandleForEyeFramebuffer(int nEye) {
        return eyeFBOIds[nEye];
    }

}
