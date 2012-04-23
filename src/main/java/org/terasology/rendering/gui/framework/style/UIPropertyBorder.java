package org.terasology.rendering.gui.framework.style;


import org.lwjgl.opengl.GL11;
import org.terasology.game.Terasology;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.lwjgl.opengl.GL11.*;

public class UIPropertyBorder extends UIProperty{
    private Logger logger = Logger.getLogger(getClass().getName());

    //Textured borders
    private final HashMap<String, Vector4f>          _colors = new HashMap<String, Vector4f>();
    private final HashMap<String, UIGraphicsElement> _images = new HashMap<String, UIGraphicsElement>();
    private final HashMap<String, Float>             _width  = new HashMap<String, Float>();

    //corners
    private final HashMap<String, UIGraphicsElement> _corners = new HashMap<String, UIGraphicsElement>();

    private boolean _showTextured = false;
    private boolean _showCorners  = false;

    public void parse(String property, String value){
        if(property.split("-").length>1){
            if(property.split("-")[1].equals("image")){
                if(property.split("-").length>2){
                    parseBorder(property, value);
                }else{
                    parseBorders(property, value);
                }
                _showTextured = true;
                setVisible(true);
            }else if(property.split("-")[1].equals("corner")){
                if(property.split("-").length>2){
                    parseCorner(property, value);
                }
            }
        }else{
            //border none
            if(value.equals("none")){
                setVisible(false);
                _showTextured = false;
            }else{
                //border <border_width> <color hex>
                String[] values = value.split(" ");

                if(values.length==2){
                    try{
                        float borderWidth = parseFloat(values[0]);
                        _width.put("top",    borderWidth);
                        _width.put("right",  borderWidth);
                        _width.put("bottom", borderWidth);
                        _width.put("left",   borderWidth);

                        _colors.put("top",    hexToRGB(values[1]));
                        _colors.put("right",  hexToRGB(values[1]));
                        _colors.put("bottom", hexToRGB(values[1]));
                        _colors.put("left",   hexToRGB(values[1]));

                        setVisible(true);
                    }catch(NumberFormatException e){
                        logger.log(Level.WARNING, "Bad value for border width: " + values[0]);
                    }
                }
            }
        }
    }

    private void parseBorders(String property, String value){
        parseBorder(property + "-top", value);
        parseBorder(property + "-right", value + " 90");
        parseBorder(property + "-bottom", value + " 180");
        parseBorder(property + "-left", value + " 90");
    }

    //border-image-<type> <texture_name> <texture_size x> <texture_size y> <texture_position x> <texture_position y> <border_width> (<rotate_angle>)
    private void parseBorder(String property, String value){
        String[] subProperty = property.split("-");
        String[] values = value.split(" ");
        if(values.length<6){
            logger.log(Level.WARNING, "Bad value in border-" + subProperty[1] + "image: " + value);
            return;
        }else{
            String borderType  = subProperty[2];

            //values
            String   textureName        = values[0];
            Vector2f textureSize        = new Vector2f(parseFloat(values[1]),parseFloat(values[2]));
            Vector2f texturePosition    = new Vector2f(parseFloat(values[3]),parseFloat(values[4]));
            float    borderWidth        = parseFloat(values[5]);

            _width.put(borderType, borderWidth);
            _images.put(borderType, new UIGraphicsElement(textureName));
            _images.get(borderType).setVisible(true);
            _images.get(borderType).setCroped(false);
            setBorderPosition(borderType);
            _images.get(borderType).getTextureSize().set(textureSize);
            _images.get(borderType).getTextureOrigin().set(texturePosition.x, texturePosition.y);
            if(values.length>6){
                _images.get(borderType).setRotateAngle(parseFloat(values[6]));
            }
        }
    }

    private void setBorderPosition(String borderType){
        if(borderType.equals("right")){
            _images.get(borderType).setPosition(new Vector2f(getPosition().x + getSize().x, getPosition().y));
        }else if(borderType.equals("left")){
            if(_images.get(borderType).getRotateAngle()==0){
                _images.get(borderType).setPosition(new Vector2f(getPosition().x - _width.get(borderType).floatValue(), getPosition().y));
            }
        }else if(borderType.equals("bottom")){
            if(_images.get(borderType).getRotateAngle()>0){
                _images.get(borderType).setPosition(getSize());
            }else{
                _images.get(borderType).setPosition(new Vector2f(getPosition().x, getSize().y));
            }
        }else if(borderType.equals("top")){
            if(_images.get(borderType).getRotateAngle()==0){
                _images.get(borderType).setPosition(new Vector2f(getPosition().x, getPosition().y - _width.get(borderType).floatValue()));
            }
        }
    }

    private void parseCorner(String property, String value){
        String[] subProperty = property.split("-");
        String[] values = value.split(" ");
        if(values.length<3){
            logger.log(Level.WARNING, "Bad value in border-corner-" + subProperty[1] + ": " + value);
            return;
        }else{
            String cornerType  = subProperty[2];

            if(_images.size()<4||_width.size()<4){
                logger.log(Level.WARNING, "You must initialize all border image first!");
                return;
            }


            //values
            String   textureName     = values[0];
            Vector2f texturePosition = new Vector2f(parseFloat(values[1]),parseFloat(values[2]));
            Vector2f textureSize     = new Vector2f();

            if(cornerType.equals("topleft")){
                textureSize = new Vector2f(_images.get("left").getTextureSize().x,
                                           _images.get("top").getTextureSize().y);
            }else if(cornerType.equals("topright")){
                textureSize = new Vector2f(_images.get("right").getTextureSize().x,
                                           _images.get("top").getTextureSize().y);
            }else if(cornerType.equals("bottomright")){
                textureSize = new Vector2f(_images.get("right").getTextureSize().x,
                                           _images.get("bottom").getTextureSize().y);
            }else if(cornerType.equals("bottomleft")){
                textureSize = new Vector2f(_images.get("left").getTextureSize().x,
                                           _images.get("bottom").getTextureSize().y);
            }

            _corners.put(cornerType, new UIGraphicsElement(textureName));
            _corners.get(cornerType).setVisible(true);
            _corners.get(cornerType).setCroped(false);
            setCornerPosition(cornerType);
            _corners.get(cornerType).getTextureSize().set(textureSize);
            _corners.get(cornerType).getTextureOrigin().set(texturePosition.x, texturePosition.y);
            _showCorners = true;
        }
    }

    private void setCornerPosition(String cornerType){
        if(cornerType.equals("topleft")){
            _corners.get(cornerType).setPosition(new Vector2f(_images.get("left").getPosition().x, _images.get("top").getPosition().y));
        }else if(cornerType.equals("topright")){
            _corners.get(cornerType).setPosition(new Vector2f(_images.get("right").getPosition().x, _images.get("top").getPosition().y));
        }else if(cornerType.equals("bottomright")){
            _corners.get(cornerType).setPosition(new Vector2f(_images.get("right").getPosition().x, _images.get("bottom").getPosition().y));
        }else if(cornerType.equals("bottomleft")){
            _corners.get(cornerType).setPosition(new Vector2f(_images.get("left").getPosition().x, _images.get("bottom").getPosition().y));
        }
    }

    private void setCornerSize(String cornerType){
        if(cornerType.equals("topleft")){
            _corners.get(cornerType).setSize(new Vector2f(_width.get("left").floatValue(), _width.get("top").floatValue()));
        }else if(cornerType.equals("topright")){
            _corners.get(cornerType).setSize(new Vector2f(_width.get("right").floatValue(), _width.get("top").floatValue()));
        }else if(cornerType.equals("bottomright")){
            _corners.get(cornerType).setSize(new Vector2f(_width.get("right").floatValue(), _width.get("bottom").floatValue()));
        }else if(cornerType.equals("bottomleft")){
            _corners.get(cornerType).setSize(new Vector2f(_width.get("left").floatValue(), _width.get("bottom").floatValue()));
        }
    }
    
    public void render(){
        if(_showTextured){
            for(String borderType: _images.keySet()){
                _images.get(borderType).renderTransformed();
            }
            if(_showCorners){
                for(String cornerType: _corners.keySet()){
                    _corners.get(cornerType).renderTransformed();
                }

            }
        }else{
            renderSolid();
        }
    }
    
    public void renderSolid(){
        glPushMatrix();
        glLoadIdentity();
        glTranslatef(calcAbsolutePosition().x, calcAbsolutePosition().y, 0);

        glLineWidth(_width.get("top"));
        glBegin(GL11.GL_LINE);
        glColor4f(_colors.get("top").x, _colors.get("top").y,_colors.get("top").z, _colors.get("top").w);
        glVertex2f(getPosition().x, getPosition().y);
        glVertex2f(getPosition().x + getSize().x, getPosition().y);
        glEnd();

        glLineWidth(_width.get("right"));
        glBegin(GL11.GL_LINE);
        glColor4f(_colors.get("right").x, _colors.get("right").y,_colors.get("right").z, _colors.get("right").w);
        glVertex2f(getPosition().x + getSize().x, getPosition().y);
        glVertex2f(getPosition().x + getSize().x, getPosition().y + getSize().y);
        glEnd();

        glLineWidth(_width.get("bottom"));
        glBegin(GL11.GL_LINE);
        glColor4f(_colors.get("bottom").x, _colors.get("bottom").y,_colors.get("bottom").z, _colors.get("bottom").w);
        glVertex2f(getPosition().x + getSize().x, getPosition().y + getSize().y);
        glVertex2f(getPosition().x, getPosition().y + getSize().y);
        glEnd();

        glLineWidth(_width.get("left"));
        glBegin(GL11.GL_LINE);
        glColor4f(_colors.get("left").x, _colors.get("left").y,_colors.get("left").z, _colors.get("left").w);
        glVertex2f(getPosition().x, getPosition().y + getSize().y);
        glVertex2f(getPosition().x, getPosition().y);
        glEnd();
        glPopMatrix();
    }

    public void update(){
        if(_showTextured){
            for(String borderType: _images.keySet()){
                if(borderType.equals("top")  || borderType.equals("bottom")){
                    _images.get(borderType).setSize(new Vector2f(getSize().x, _width.get(borderType).floatValue()));
                }else{
                    if(_images.get(borderType).getRotateAngle()>0){
                        _images.get(borderType).setSize(new Vector2f(getSize().y, _width.get(borderType).floatValue()));
                    }else{
                        _images.get(borderType).setSize(new Vector2f(_width.get(borderType).floatValue(),getSize().y));
                    }
                }
                _images.get(borderType).update();
            }
        }
        if(_showCorners){
            for(String cornerType: _corners.keySet()){
                setCornerSize(cornerType);
            }
        }
    }
}
