package org.terasology.rendering.gui.framework.style;

import org.terasology.logic.manager.AssetManager;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import static org.lwjgl.opengl.GL11.*;

//background-color:    <color hex> <alpha 0.0-1.0>
//background-image:    <texture_name> <texture_size x> <texture_size y> <texture_position x> <texture_position y> (<rotate_angle>)
//background-position: <texture_position x> <texture_position y>
public class UIPropertyBackground extends UIProperty {
    private Vector4f          _color = new Vector4f();
    private UIGraphicsElement _image;

    public void parse(String property, String value){
        String[] properties = property.split("-");
        if(properties.length>1){
            if(properties[1].equals("color")){
                String[] values    = value.split(" ");
                _color   = hexToRGB(values[0]);
                _color.w = parseFloat(values[1]);
                setVisible(true);
            }else if(properties[1].equals("image")){
                parseBackgroundImage(value);
                setVisible(true);
            }else if(properties[1].equals("position")){
                parsePosition(value);
            }
        }else{
            if(value.equals("none")){
                setVisible(false);
                if(_image != null){
                    _image.setVisible(false);
                }
            }
        }
    }

    public void render(){
        if(_image!=null&&_image.isVisible()){
            _image.renderTransformed();
        }else{
            renderSolid();
        }
    }

    private void renderSolid(){
        glPushMatrix();
        glLoadIdentity();
        glTranslatef(calcAbsolutePosition().x, calcAbsolutePosition().y, 0);
        glColor4f(_color.x, _color.y,_color.z, _color.w);
        glBegin(GL_QUADS);
        glVertex2f(0f, 0f);
        glVertex2f(getSize().x, 0f);
        glVertex2f(getSize().x, getSize().y);
        glVertex2f(0f, getSize().y);
        glEnd();
        glPopMatrix();
    }

    public void update(){
        if(_image!=null&&_image.isVisible()){
            _image.update();
        }
    }

    //background-position: <texture_position x> <texture_position y>
    private void parsePosition(String value){
        if(_image == null){
            return;
        }
        String[] values = value.split(" ");

        Vector2f texturePosition = new Vector2f(parseFloat(values[0]),parseFloat(values[1]));
        _image.getTextureOrigin().set(texturePosition.x, texturePosition.y);
    }

    //background-image:    <texture_name> <texture_size x> <texture_size y> <texture_position x> <texture_position y> (<rotate_angle>)
    private void parseBackgroundImage(String value){
        String[] values = value.split(" ");

        String textureName = values[0];
        Vector2f textureSize        = new Vector2f(parseFloat(values[1]),parseFloat(values[2]));
        Vector2f texturePosition    = new Vector2f(parseFloat(values[3]),parseFloat(values[4]));

        _image = new UIGraphicsElement(AssetManager.loadTexture(textureName));
        _image.setSize(getSize());
        _image.setVisible(true);
        _image.setCroped(false);
        _image.getTextureSize().set(textureSize);
        _image.getTextureOrigin().set(texturePosition.x, texturePosition.y);

        if(values.length>5){
            _image.setRotateAngle(parseFloat(values[5]));
        }
    }

}
