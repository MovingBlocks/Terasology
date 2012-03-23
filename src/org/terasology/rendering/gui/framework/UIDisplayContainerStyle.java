package org.terasology.rendering.gui.framework;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.terasology.game.Terasology;

import javax.vecmath.Vector4f;
import javax.vecmath.Vector2f;
import java.util.HashMap;
import java.util.logging.Level;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glPopMatrix;

public class UIDisplayContainerStyle extends UIDisplayElement{

    private Color    _textColor       = Color.black;
    private Vector4f _backgroundColor = new Vector4f();

    private UIGraphicsElement _backgroundImage = null;

    //Borders colors
    private final HashMap<String, Vector4f> _bordersColors = new HashMap<String, Vector4f>();


    //Textured borders positions
    private final HashMap<String, UIGraphicsElement> _bordersTexture = new HashMap<String, UIGraphicsElement>();
    private final HashMap<String, Float> _bordersWidth               = new HashMap<String, Float>();

    private boolean _showBackground        = false;
    private boolean _showBackgroundImage   = false;
    private boolean _showBorders           = false;
    private boolean _showTexturedBorders   = false;


    UIDisplayContainerStyle(Vector2f size){
        setSize(size);
    }

    public void render() {
        if(_backgroundImage!=null&&_backgroundImage.isVisible()){
            renderBackgroundImage();
        }
        if(_showBackground&&!_showBackgroundImage){
            renderBackgroundSolid();
        }

        if(_showTexturedBorders){
            renderTexturedBolders();
        }else if(_showBorders){
            renderBorderSolid();
        }

    }

    private void renderTexturedBolders(){
        for(String borderType: _bordersTexture.keySet()){
            _bordersTexture.get(borderType).renderTransformed();
        }
    }

    private void renderBackgroundImage(){
        _backgroundImage.renderTransformed();
    }

    private void renderBackgroundSolid(){
        glPushMatrix();
            glLoadIdentity();
            glTranslatef(calcAbsolutePosition().x, calcAbsolutePosition().y, 0);
            glColor4f(_backgroundColor.x, _backgroundColor.y,_backgroundColor.z, _backgroundColor.w);
            glBegin(GL_QUADS);
                glVertex2f(0f, 0f);
                glVertex2f(getSize().x, 0f);
                glVertex2f(getSize().x, getSize().y);
                glVertex2f(0f, getSize().y);
            glEnd();
        glPopMatrix();
    }

    private void renderBorderSolid(){
        glPushMatrix();
        glLoadIdentity();
        glTranslatef(calcAbsolutePosition().x, calcAbsolutePosition().y, 0);

        glLineWidth(_bordersWidth.get("top"));
        glBegin(GL11.GL_LINE);
            glColor4f(_bordersColors.get("top").x, _bordersColors.get("top").y,_bordersColors.get("top").z, _bordersColors.get("top").w);
            glVertex2f(getPosition().x, getPosition().y);
            glVertex2f(getPosition().x + getSize().x, getPosition().y);
        glEnd();

        glLineWidth(_bordersWidth.get("right"));
        glBegin(GL11.GL_LINE);
            glColor4f(_bordersColors.get("right").x, _bordersColors.get("right").y,_bordersColors.get("right").z, _bordersColors.get("right").w);
            glVertex2f(getPosition().x + getSize().x, getPosition().y);
            glVertex2f(getPosition().x + getSize().x, getPosition().y + getSize().y);
        glEnd();

        glLineWidth(_bordersWidth.get("bottom"));
        glBegin(GL11.GL_LINE);
            glColor4f(_bordersColors.get("bottom").x, _bordersColors.get("bottom").y,_bordersColors.get("bottom").z, _bordersColors.get("bottom").w);
            glVertex2f(getPosition().x + getSize().x, getPosition().y + getSize().y);
            glVertex2f(getPosition().x, getPosition().y + getSize().y);
        glEnd();

        glLineWidth(_bordersWidth.get("left"));
        glBegin(GL11.GL_LINE);
            glColor4f(_bordersColors.get("left").x, _bordersColors.get("left").y,_bordersColors.get("left").z, _bordersColors.get("left").w);
            glVertex2f(getPosition().x, getPosition().y + getSize().y);
            glVertex2f(getPosition().x, getPosition().y);
            glEnd();
        glPopMatrix();
    }

    public void update() {
        if(_showTexturedBorders){
            for(String borderType: _bordersTexture.keySet()){
                if(borderType.equals("top")  || borderType.equals("bottom")){
                    _bordersTexture.get(borderType).setSize(new Vector2f(getSize().x, _bordersWidth.get(borderType).floatValue()));
                }else{
                    _bordersTexture.get(borderType).setSize(new Vector2f(getSize().y, _bordersWidth.get(borderType).floatValue()));
                }
                _bordersTexture.get(borderType).update();
            }
        }
        
        if(_backgroundImage!=null&&_backgroundImage.isVisible()){
            _backgroundImage.update();
        }
    }
    
    public void parseStyle(String property, String value){
        String[] subProperty = property.split("-");
        if(property.indexOf("border")>=0){
            if(subProperty.length>1){
                if(subProperty[1].equals("image")){
                    if(subProperty.length>2){
                        parseForOneBorder(property, value);
                    }else{
                        parseForAllBorders(property, value);
                    }
                    _showTexturedBorders = true;
                }
            }else{
                System.out.println("border -one ");
                //border none
                if(value.equals("none")){
                    _showBorders = false;
                    _showTexturedBorders = false;
                }else{
                    //border <border_width> <color hex>
                    String[] values = value.split(" ");
                    //System.out.println("border -one  get ");
                    if(values.length==2){
                        try{
                            float borderWidth = parseFloat(values[0]);
                            _bordersWidth.put("top",    borderWidth);
                            _bordersWidth.put("right",  borderWidth);
                            _bordersWidth.put("bottom", borderWidth);
                            _bordersWidth.put("left",   borderWidth);

                            _bordersColors.put("top",    hexToRGB(values[1]));
                            _bordersColors.put("right",  hexToRGB(values[1]));
                            _bordersColors.put("bottom", hexToRGB(values[1]));
                            _bordersColors.put("left",   hexToRGB(values[1]));

                            _showBorders = true;
                        }catch(NumberFormatException e){
                            Terasology.getInstance().getLogger().log(Level.WARNING, "Bad value for border width: " + values[0]);
                        }
                    }
                }
            }
        }else if(property.indexOf("background")>=0){
            if(subProperty.length>1){
                parseBackground(subProperty, value);
            }else{
                if(value.equals("none")){
                    _showBackground      = false;
                    if(_backgroundImage != null){
                        _backgroundImage.setVisible(false);
                    }
                }
            }
        }
    }

    //background-color:    <color hex> <alpha 0.0-1.0>
    //background-image:    <texture_name> <texture_size x> <texture_size y> <texture_position x> <texture_position y> (<rotate_angle>)
    //background-position: <texture_position x> <texture_position y>
    private void parseBackground(String[] properties, String value){
        if(properties[1].equals("color")){
            String[] values    = value.split(" ");
            _backgroundColor   = hexToRGB(values[0]);
            _backgroundColor.w = parseFloat(values[1]);
            _showBackground    = true;
        }else if(properties[1].equals("image")){
            parseBackgroundImage(value);
        }else if(properties[1].equals("position")){
            parseBackgroundPostion(value);
        }
    }

    //background-position: <texture_position x> <texture_position y>
    private void parseBackgroundPostion(String value){
        if(_backgroundImage == null){
            return;
        }
        String[] values = value.split(" ");

        Vector2f texturePosition = new Vector2f(parseFloat(values[0]),parseFloat(values[1]));
        _backgroundImage.getTextureOrigin().set(texturePosition.x, texturePosition.y);
    }

    //background-image:    <texture_name> <texture_size x> <texture_size y> <texture_position x> <texture_position y> (<rotate_angle>)
    private void parseBackgroundImage(String value){
        String[] values = value.split(" ");
        
        String textureName = values[0];
        Vector2f textureSize        = new Vector2f(parseFloat(values[1]),parseFloat(values[2]));
        Vector2f texturePosition    = new Vector2f(parseFloat(values[3]),parseFloat(values[4]));

        _backgroundImage = new UIGraphicsElement(textureName);
        _backgroundImage.setSize(getSize());
        _backgroundImage.setVisible(true);
        _backgroundImage.setCroped(false);
        _backgroundImage.getTextureSize().set(textureSize);
        _backgroundImage.getTextureOrigin().set(texturePosition.x, texturePosition.y);

        if(values.length>5){
            _backgroundImage.setRotateAngle(parseFloat(values[5]));
        }
    }

    private void parseForAllBorders(String property, String value){
        parseForOneBorder(property + "-top", value);
        parseForOneBorder(property + "-right", value + " 90");
        parseForOneBorder(property + "-bottom", value + " 180");
        parseForOneBorder(property + "-left", value + " 90");
    }

    //border-image-left <texture_name> <texture_size x> <texture_size y> <texture_position x> <texture_position y> <border_width> (<rotate_angle>)
    private void parseForOneBorder(String property, String value){
        String[] subProperty = property.split("-");
        String[] values = value.split(" ");
        if(values.length<6){
            Terasology.getInstance().getLogger().log(Level.WARNING, "Bad value in border-" + subProperty[1] + "image: " + value);
            return;
        }else{
            String borderType  = subProperty[2];

            //values
            String   textureName        = values[0];
            Vector2f textureSize        = new Vector2f(parseFloat(values[1]),parseFloat(values[2]));
            Vector2f texturePosition    = new Vector2f(parseFloat(values[3]),parseFloat(values[4]));
            float    borderWidth        = parseFloat(values[5]);

            _bordersTexture.put(borderType, new UIGraphicsElement(textureName));
            _bordersTexture.get(borderType).setVisible(true);
            _bordersTexture.get(borderType).setCroped(false);
            setBorderPosition(borderType);
            _bordersTexture.get(borderType).getTextureSize().set(textureSize);
            _bordersTexture.get(borderType).getTextureOrigin().set(texturePosition.x, texturePosition.y);
            _bordersWidth.put(borderType, borderWidth);
            if(values.length>6){
                _bordersTexture.get(borderType).setRotateAngle(parseFloat(values[6]));
            }
        }

    }
    
    private void setBorderPosition(String borderType){
         if(borderType.equals("right")){
             _bordersTexture.get(borderType).setPosition(new Vector2f(getPosition().x + getSize().x, getPosition().y));
         }else if(borderType.equals("bottom")){
             _bordersTexture.get(borderType).setPosition(getSize());
         }
    }

    //Temporary. Value can have 2 arguments. For example 2/150
    private float parseFloat(String value){
        if(value.indexOf("/")<0){
            try{
                return  Float.parseFloat(value);
            }catch(NumberFormatException e){
                Terasology.getInstance().getLogger().log(Level.WARNING, "Bad float value - " + value);
                return 0f;
            }
        }else{
            try{
                float arg1 = Float.parseFloat(value.split("/")[0]);
                float arg2 = Float.parseFloat(value.split("/")[1]);
                return arg1/arg2;
            }catch(Exception e){
                Terasology.getInstance().getLogger().log(Level.WARNING, e.getMessage());
                return 0f;
            }
        }
    }

    private Vector4f hexToRGB(String hexColor){
        try{
            return new Vector4f((float)Integer.parseInt(checkHex(hexColor).substring(0,2),16)/255f,
                            (float)Integer.parseInt(checkHex(hexColor).substring(2,4),16)/255f,
                            (float)Integer.parseInt(checkHex(hexColor).substring(4,6),16)/255f,
                            1.0f);
        }catch (NumberFormatException e){
            Terasology.getInstance().getLogger().log(Level.WARNING, "Bad Hex color value - " + hexColor);
            return new Vector4f();
        }
    }

    private String checkHex(String h){
        return (h.charAt(0)=='#')?h.substring(1,7):h;
    }
}
