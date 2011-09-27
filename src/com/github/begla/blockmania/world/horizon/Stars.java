package com.github.begla.blockmania.world.horizon;

import com.github.begla.blockmania.blocks.Block;
import com.github.begla.blockmania.generators.*;
import com.github.begla.blockmania.main.Configuration;
import com.github.begla.blockmania.rendering.Primitives;
import com.github.begla.blockmania.rendering.RenderableObject;
import com.github.begla.blockmania.rendering.ShaderManager;
import com.github.begla.blockmania.rendering.TextureManager;
import com.github.begla.blockmania.rendering.particles.BlockParticleEmitter;
import com.github.begla.blockmania.utilities.FastRandom;
import com.github.begla.blockmania.world.characters.Player;
import com.github.begla.blockmania.world.characters.Slime;
import com.github.begla.blockmania.world.chunk.Chunk;
import com.github.begla.blockmania.world.chunk.ChunkCache;
import com.github.begla.blockmania.world.entity.Entity;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.newdawn.slick.util.ResourceLoader;
import org.xml.sax.InputSource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.*;
import java.util.Collections;
import java.util.logging.Level;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;

/*Test*/
import java.nio.IntBuffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;

/**
 * 
 * @author kireev
 */

//Not completed
public class Stars implements RenderableObject {
    Random _random;
    float  _stars_colors[][] = { 
                             { 1.0f, 1.0f, 1.0f, 1.0f    }, // White
                             { 0.67f, 0.68f, 0.82f, 1.0f }, // Blue Stars
                             { 1.0f, 0.5f, 0.5f, 1.0f    }, // Reddish
                             { 1.0f, 0.82f, 0.65f, 1.0f  }  // Orange
                         }; 

    float _stars_position[][];
    private int _num_stars = 100;

    private float _radius = 100.0f;
    
    public  static IntBuffer textureId = createIntBuffer(1);

    private static IntBuffer createIntBuffer(int numElements){
      return ByteBuffer.allocateDirect(4*numElements).order(
             ByteOrder.nativeOrder()).asIntBuffer();
    }

    public Stars(){
        _random = new Random();
        _stars_position = new float[_num_stars][3];

        float pi2 = 6.28f;
        float pi = 3.14f;

        float fi = 0.0f;
        float te = 0.0f;



        for(int i=0; i<_num_stars; i++){
            fi = (float)(pi2*Math.abs(Math.random()-0.5f));
            te = (float)(pi*Math.abs(Math.random()));

            _stars_position[i][0] = _radius*(float)(Math.sin(te)*Math.cos(fi)) - (float)(10*Math.random());//;//*0.1f;
            _stars_position[i][1] = _radius*(float)(Math.sin(te)*Math.sin(fi)) + (float)(10*Math.random());//*0.1f;
            _stars_position[i][2] = _radius*(float)(Math.cos(te)) + (float)(10*Math.random());//*0.1f;

        }

    }
    
    /*Test*/
    public void loadTextures (){
      int internalFormat = GL11.GL_RGBA8,
          format         = GL12.GL_BGR,
          face           = 0;

          GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
          GL11.glGenTextures(textureId);
          GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, textureId.get(0));

          GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_S, GL11.GL_LINEAR);
          GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_T, GL11.GL_LINEAR);
          GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
          GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);

          GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);

          for(int i = 1; i <= 6; i++){

            switch(i){
              case 1: face = GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X; break;
              case 2: face = GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_X; break;
              case 3: face = GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_Y; break;
              case 4: face = GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y; break;
              case 5: face = GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_Z; break;
              case 6: face = GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z; break;
            }

           byte[] data = TextureManager.getInstance().getTexture("stars" + i).getTextureData();
           ByteBuffer byteBuffer = ByteBuffer.allocateDirect(data.length)
                                   .order(ByteOrder.nativeOrder())
                                   .put(data);
            byteBuffer.flip();

            //ByteBuffer.TextureManager.getInstance().getTexture("star" + i).getTextureData();

            GL11.glTexImage2D(face, 0, internalFormat, TextureManager.getInstance().getTexture("stars" + i).getImageWidth(), TextureManager.getInstance().getTexture("stars" + i).getImageHeight(),
                              0, format,  GL11.GL_UNSIGNED_BYTE,  byteBuffer);
          }
    }


    public void render(){


        //glMatrixMode(GL11.GL_MODELVIEW_MATRIX);
        //glLoadIdentity();

        glPushMatrix();
        glPointSize(3.0f);
        glBegin(GL_POINTS);

         try{


            /*color = 0;

            if(random.nextFloat() % 5 == 1.0f)
              color = 1;

          // One in 50 red
            if(random.nextFloat() % 50 == 1.0f)
              color = 2;

          // One in 100 is amber
            if(random.nextFloat() % 100 ==  1.0f)
              color = 3;
          */

            /*
          fi=rand()%360;
            te=rand()%180;

            x=R*sin(te)*cos(fi);
            y=R*sin(te)*sin(fi);
            z=R*cos(te);

            star[i].x=x;
            star[i].y=y;
            star[i].z=z;
            star[i+1].x=-x;
            star[i+1].y=-y;
            star[i+1].z=-z;

             */


          /*  stars_postion0 += 100.1f;
            stars_postion1 += 100.1f;
            stars_postion2 += 100.1f;*/
           for(float position[]: _stars_position){
            glColor3f(1.0f,1.0f,1.0f);
            glVertex3f(position[0], position[1], position[2]);
            System.out.println("x: " + position[0] + " y:" +  position[1] +" z: " + position[2]);
           }

          }catch(NullPointerException e){
            System.out.println("Wtf?!");
          }

        glEnd();

        glPopMatrix();
    }

    public void update(){

    }

}
