// Copyright 2023 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.nui.internal;

import org.joml.Quaternionfc;
import org.joml.Vector2fc;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3fc;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.subsystem.DisplayDevice;
import org.terasology.engine.rendering.assets.font.FontCharacter;
import org.terasology.engine.rendering.assets.material.Material;
import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.engine.rendering.assets.mesh.MeshBuilder;
import org.terasology.engine.rendering.opengl.FrameBufferObject;
import org.terasology.engine.rendering.opengl.WgpuTexture;
import org.terasology.engine.rust.EngineKernel;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.joml.geom.Rectanglef;
import org.terasology.joml.geom.Rectanglei;
import org.terasology.math.TeraMath;
import org.terasology.nui.Border;
import org.terasology.nui.Colorc;
import org.terasology.nui.FontColor;
import org.terasology.nui.FontUnderline;
import org.terasology.nui.HorizontalAlign;
import org.terasology.nui.ScaleMode;
import org.terasology.nui.TextLineBuilder;
import org.terasology.nui.UITextureRegion;
import org.terasology.nui.VerticalAlign;
import org.terasology.nui.asset.font.Font;
import reactor.function.Consumer4;
import reactor.function.Consumer5;

import java.util.List;

public class WgpuCanvasRenderer implements TerasologyCanvasRenderer {
    private DisplayDevice displayDevice;

    public WgpuCanvasRenderer(Context context) {
        this.displayDevice = context.get(DisplayDevice.class);
    }

    @Override
    public void preRender() {

    }

    @Override
    public void postRender() {

    }

    @Override
    public Vector2i getTargetSize() {
        return new Vector2i(displayDevice.getWidth(), displayDevice.getHeight());
    }

    @Override
    public void crop(Rectanglei cropRegion) {
        EngineKernel kernel = EngineKernel.instance();

    }

    @Override
    public void drawLine(int sx, int sy, int ex, int ey, Colorc color) {

    }

    @Override
    public void drawTexture(UITextureRegion texture, Colorc color, ScaleMode mode, Rectanglei absoluteRegion, float ux, float uy, float uw, float uh, float alpha) {
        EngineKernel kernel = EngineKernel.instance();
        if (texture instanceof WgpuTexture) {
            Vector2fc size = ((WgpuTexture) texture).getTeraTexture().getSize();

            kernel.cmdUIDrawTexture(
                    ((WgpuTexture) texture).getTeraTexture(),
                    new Rectanglef(ux, uy , ux + uw, uy + uh),
                    new Rectanglef(absoluteRegion.minX(), absoluteRegion.minY(), absoluteRegion.maxX(), absoluteRegion.maxY()));
        }

    }

    @Override
    public void drawText(String text, Font font, HorizontalAlign hAlign, VerticalAlign vAlign, Rectanglei absoluteRegion, Colorc color, Colorc shadowColor, float alpha, boolean underlined) {

        EngineKernel kernel = EngineKernel.instance();

        List<String> lines = TextLineBuilder.getLines(font, text, absoluteRegion.getSizeX());
        Vector2i offset = new Vector2i(absoluteRegion.minX, absoluteRegion.minY);
        offset.y += vAlign.getOffset(lines.size() * font.getLineHeight(), absoluteRegion.lengthY());

        org.terasology.engine.rendering.assets.font.Font
                fnt = (org.terasology.engine.rendering.assets.font.Font) font;
        int y = 0;

        for (String line : lines) {
            int w = font.getWidth(line);
            int x = hAlign.getOffset(w, absoluteRegion.getSizeX());
            for (char c : line.toCharArray()) {
                FontCharacter character = fnt.getCharacterData(c);
                float top = y + character.getyOffset();
                float bottom = top + character.getHeight();
                float left = x + character.getxOffset();
                float right = left + character.getWidth();

                float texTop = character.getY();
                float texBottom = texTop + character.getTexHeight();
                float texLeft = character.getX();
                float texRight = texLeft + character.getTexWidth();

                kernel.cmdUIDrawTexture(
                        ((WgpuTexture) character.getPage()).getTeraTexture(),
                        new Rectanglef(texLeft, texTop, texRight, texBottom),
                        new Rectanglef(left + offset.x, top + offset.y, right + offset.x, bottom + offset.y));
                x += character.getxAdvance();
            }
            y += font.getLineHeight();
        }
    }


    @Override
    public void drawTextureBordered(UITextureRegion texture, Rectanglei absoluteRegion, Border border, boolean tile, float ux, float uy, float uw, float uh, float alpha) {
        EngineKernel kernel = EngineKernel.instance();
        Vector2i textureSize = new Vector2i(TeraMath.ceilToInt(texture.getWidth() * uw),
                TeraMath.ceilToInt(texture.getHeight() * uh));
        Rectanglei region = new Rectanglei(absoluteRegion);

        if (texture instanceof WgpuTexture) {
            Consumer4<Rectanglei, Rectanglef, Vector2i, Rectanglef> cmdDrawTiles = (Rectanglei drawRegion, Rectanglef subDrawRegion, Vector2i tileSize,
                                                                               Rectanglef subTextureRegion) -> {
                int tileW = tileSize.x;
                int tileH = tileSize.y;
                int horizTiles = TeraMath.fastAbs((drawRegion.getSizeX() - 1) / tileW) + 1;
                int vertTiles = TeraMath.fastAbs((drawRegion.getSizeY() - 1) / tileH) + 1;

                int offsetX = (drawRegion.getSizeX() - horizTiles * tileW) / 2;
                int offsetY = (drawRegion.getSizeY() - vertTiles * tileH) / 2;

                for (int tileY = 0; tileY < vertTiles; tileY++) {
                    for (int tileX = 0; tileX < horizTiles; tileX++) {
                        int left = offsetX + tileW * tileX;
                        int top = offsetY + tileH * tileY;

                        float vertLeft =
                                subDrawRegion.minX + subDrawRegion.getSizeX() * Math.max((float) left / drawRegion.getSizeX(), 0);
                        float vertTop =
                                subDrawRegion.minY + subDrawRegion.getSizeY() * Math.max((float) top / drawRegion.getSizeY(), 0);
                        float vertRight =
                                subDrawRegion.minX + subDrawRegion.getSizeX() * Math.min((float) (left + tileW) / drawRegion.getSizeX(), 1);
                        float vertBottom =
                                subDrawRegion.minY + subDrawRegion.getSizeY() * Math.min((float) (top + tileH) / drawRegion.getSizeY(), 1);
                        float texCoordLeft =
                                subTextureRegion.minX + subTextureRegion.getSizeX() * (Math.max(left, 0) - left) / tileW;
                        float texCoordTop =
                                subTextureRegion.minY + subTextureRegion.getSizeY() * (Math.max(top, 0) - top) / tileH;
                        float texCoordRight = subTextureRegion.minX + subTextureRegion.getSizeX() * (Math.min(left + tileW,
                                drawRegion.getSizeX()) - left) / tileW;
                        float texCoordBottom = subTextureRegion.minY + subTextureRegion.getSizeY() * (Math.min(top + tileH,
                                drawRegion.getSizeY()) - top) / tileH;
                        kernel.cmdUIDrawTexture(
                                ((WgpuTexture) texture).getTeraTexture(),
                                new Rectanglef(texCoordLeft, texCoordTop, texCoordRight, texCoordBottom),
                                new Rectanglef( vertLeft, vertTop, vertRight, vertBottom)
                                        .scale(region.getSizeX(), region.getSizeY())
                                        .translate(region.minX, region.minY));
//                addRectPoly(builder, vertLeft, vertTop, vertRight, vertBottom, texCoordLeft, texCoordTop,
//                        texCoordRight, texCoordBottom);
                    }
                }
            };

            float topTex = (float) border.getTop() / textureSize.y;
            float leftTex = (float) border.getLeft() / textureSize.x;
            float bottomTex = 1f - (float) border.getBottom() / textureSize.y;
            float rightTex = 1f - (float) border.getRight() / textureSize.x;
            int centerHoriz = region.getSizeX() - border.getTotalWidth();
            int centerVert = region.getSizeY() - border.getTotalHeight();

            float top = (float) border.getTop() / region.getSizeY();
            float left = (float) border.getLeft() / region.getSizeX();
            float bottom = 1f - (float) border.getBottom() / region.getSizeY();
            float right = 1f - (float) border.getRight() / region.getSizeX();


            if (border.getTop() != 0) {
                if (border.getLeft() != 0) {
                    kernel.cmdUIDrawTexture(
                            ((WgpuTexture) texture).getTeraTexture(),
                            new Rectanglef(0, 0, leftTex, topTex),
                            new Rectanglef(0, 0, left, top)
                                    .scale(region.getSizeX(), region.getSizeY())
                                    .translate(region.minX, region.minY));
                }
                if (tile) {

                    cmdDrawTiles.accept(new Rectanglei(border.getLeft(), 0).setSize(centerHoriz, border.getTop()),
                            new Rectanglef(left, 0, right, top),
                            new Vector2i(textureSize.x - border.getTotalWidth(), border.getTop()),
                            new Rectanglef(leftTex, 0, rightTex, topTex)
                                    .scale(region.getSizeX(), region.getSizeY())
                                    .translate(region.minX, region.minY));
                } else {
//                    addRectPoly(builder, left, 0, right, top, leftTex, 0, rightTex, topTex);
                    kernel.cmdUIDrawTexture(
                            ((WgpuTexture) texture).getTeraTexture(),
                            new Rectanglef(leftTex, 0, rightTex, topTex),
                            new Rectanglef( left, 0, right, top).translate(region.minX, region.minY)
                                    .scale(region.getSizeX(), region.getSizeY())
                                    .translate(region.minX, region.minY));
                }
                if (border.getRight() != 0) {
//                    addRectPoly(builder, right, 0, 1, top, rightTex, 0, 1, topTex);
                    kernel.cmdUIDrawTexture(
                            ((WgpuTexture) texture).getTeraTexture(),
                            new Rectanglef(rightTex, 0, 1, topTex),
                            new Rectanglef(right, 0, 1, top)
                                    .scale(region.getSizeX(), region.getSizeY())
                                    .translate(region.minX, region.minY));
                }
            }

            if (border.getLeft() != 0) {
                if (tile) {
                    cmdDrawTiles.accept(new Rectanglei(0, border.getTop()).setSize(border.getLeft(), centerVert),
                            new Rectanglef(0, top, left, bottom),
                            new Vector2i(border.getLeft(), textureSize.y - border.getTotalHeight()),
                            new Rectanglef(0, topTex, leftTex, bottomTex));
                } else {
//                    addRectPoly(builder, 0, top, left, bottom, 0, topTex, leftTex, bottomTex);
                    kernel.cmdUIDrawTexture(
                            ((WgpuTexture) texture).getTeraTexture(),
                            new Rectanglef(0, topTex, leftTex, bottomTex),
                            new Rectanglef( 0, top, left, bottom)
                                    .scale(region.getSizeX(), region.getSizeY())
                                    .translate(region.minX, region.minY));
                }
            }

            if (tile) {
                cmdDrawTiles.accept(new Rectanglei(border.getLeft(), border.getTop()).setSize(centerHoriz, centerVert),
                        new Rectanglef(left, top, right, bottom),
                        new Vector2i(textureSize.x - border.getTotalWidth(), textureSize.y - border.getTotalHeight()),
                        new Rectanglef(leftTex, topTex, rightTex, bottomTex));
            } else {
//                addRectPoly(builder, left, top, right, bottom, leftTex, topTex, rightTex, bottomTex);
                kernel.cmdUIDrawTexture(
                        ((WgpuTexture) texture).getTeraTexture(),
                        new Rectanglef(leftTex, topTex, rightTex, bottomTex),
                        new Rectanglef(left, top, right, bottom)
                                .scale(region.getSizeX(), region.getSizeY())
                                .translate(region.minX, region.minY));
            }

            if (border.getRight() != 0) {
                if (tile) {
                    cmdDrawTiles.accept(
                            new Rectanglei(region.getSizeX() - border.getRight(), border.getTop()).setSize(border.getRight(), centerVert),
                            new Rectanglef(right, top, 1, bottom),
                            new Vector2i(border.getRight(), textureSize.y - border.getTotalHeight()),
                            new Rectanglef(rightTex, topTex, 1, bottomTex));
                } else {
                    kernel.cmdUIDrawTexture(
                            ((WgpuTexture) texture).getTeraTexture(),
                            new Rectanglef(rightTex, topTex, 1, bottomTex),
                            new Rectanglef(right, top, 1, bottom)
                                    .scale(region.getSizeX(), region.getSizeY())
                                    .translate(region.minX, region.minY));
                }
            }

            if (border.getBottom() != 0) {
                if (border.getLeft() != 0) {
//                    addRectPoly(builder, 0, bottom, left, 1, 0, bottomTex, leftTex, 1);
                    kernel.cmdUIDrawTexture(
                            ((WgpuTexture) texture).getTeraTexture(),
                            new Rectanglef(0, bottomTex, leftTex, 1),
                            new Rectanglef(0, bottom, left, 1)
                                    .scale(region.getSizeX(), region.getSizeY())
                                    .translate(region.minX, region.minY));
                }
                if (tile) {
                    cmdDrawTiles.accept(
                            new Rectanglei(border.getLeft(), region.getSizeY() - border.getBottom()).setSize(centerHoriz, border.getBottom()),
                            new Rectanglef(left, bottom, right, 1),
                            new Vector2i(textureSize.x - border.getTotalWidth(), border.getBottom()),
                            new Rectanglef(leftTex, bottomTex, rightTex, 1));
                } else {
//                    addRectPoly(builder, left, bottom, right, 1, leftTex, bottomTex, rightTex, 1);
                    kernel.cmdUIDrawTexture(
                            ((WgpuTexture) texture).getTeraTexture(),
                            new Rectanglef(leftTex, bottomTex, rightTex, 1),
                            new Rectanglef(left, bottom, right, 1)
                                    .scale(region.getSizeX(), region.getSizeY())
                                    .translate(region.minX, region.minY));
                }
                if (border.getRight() != 0) {
//                    addRectPoly(builder, right, bottom, 1, 1, rightTex, bottomTex, 1, 1);
                    kernel.cmdUIDrawTexture(
                            ((WgpuTexture) texture).getTeraTexture(),
                            new Rectanglef(rightTex, bottomTex, 1, 1),
                            new Rectanglef(right, bottom, 1, 1)
                                    .scale(region.getSizeX(), region.getSizeY())
                                    .translate(region.minX, region.minY));
                }
            }
        }
    }

    @Override
    public void setUiScale(float uiScale) {

    }

    @Override
    public FrameBufferObject getFBO(ResourceUrn urn, Vector2ic size) {
        return null;
    }

    @Override
    public void drawMesh(Mesh mesh, Material material, Rectanglei drawRegion, Rectanglei cropRegion, Quaternionfc rotation, Vector3fc offset, float scale, float alpha) {

    }

    @Override
    public void drawMaterialAt(Material material, Rectanglei drawRegion) {

    }
}
