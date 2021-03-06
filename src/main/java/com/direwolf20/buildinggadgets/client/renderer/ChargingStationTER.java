package com.direwolf20.buildinggadgets.client.renderer;

import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.tiles.ChargingStationTileEntity;
import com.direwolf20.buildinggadgets.common.util.exceptions.CapabilityNotPresentException;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraftforge.energy.CapabilityEnergy;
import org.lwjgl.opengl.GL11;


public class ChargingStationTER extends TileEntityRenderer<ChargingStationTileEntity> {
    private static final float CHARGE_UPDATE_BORDER = 0.005f;

    public ChargingStationTER() {
    }

    @Override
    public void render(ChargingStationTileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {
        ItemStack stack = te.getRenderStack();
        if (! stack.isEmpty()) {
            GlStateManager.pushLightingAttributes();
            GlStateManager.pushMatrix();

            // Translate to the location of our tile entity
            GlStateManager.translated(x, y, z);
            GlStateManager.disableRescaleNormal();
            // Render our item
            renderItem(te);

            //Render our sphere

            renderSphere(te);

            //Render Lightning

            if (te.isChargingItem(stack.getCapability(CapabilityEnergy.ENERGY).orElseThrow(CapabilityNotPresentException::new)) &&
                    //don't render more then 16 Blocks away from the Block
                    SphereSegmentation.BY_DISTANCE.compare(te.getLastRenderedSegmentation(), SphereSegmentation.MEDIUM_SEGMENTATION) < 0)
                renderLightning(te);
            GlStateManager.popMatrix();
            GlStateManager.popAttributes();
        }

    }

    private void renderLightning(ChargingStationTileEntity te) {
        //Just toying with this - i Think the effect i have in my mind is way too complex for my weak programming skills
        //If someone else wants to take a crack at either lightning or particles flowing into the item from the charger, go for it!
        float red = 1f;
        float green = 0f;
        float blue = 0f;
        float alpha = 1f;

        GlStateManager.pushMatrix();
        GlStateManager.pushLightingAttributes();

        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableTexture();
        GlStateManager.translated(.5, 1, .5);
        //GlStateManager.depthMask(false);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.disableDepthTest();
        Tessellator t = Tessellator.getInstance();

        BufferBuilder bufferBuilder = t.getBuffer();

        bufferBuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        GlStateManager.lineWidth(3);
        double x2 = te.getLightningX();
        double z2 = te.getLightningZ();
        bufferBuilder.pos(x2, 0, z2).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(0, 0.5, 0).color(red, green, blue, alpha).endVertex();
        t.draw();

        //GlStateManager.depthMask(true);
        GlStateManager.enableDepthTest();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture();
        GlStateManager.popAttributes();
        GlStateManager.popMatrix();
    }

    private void renderSphere(ChargingStationTileEntity te) {
        if (! Config.CHARGING_STATION.renderSphere.get()) {
            te.updateSegmentation(te.getSegmentation());
            return;
        }
        float lastCharge = te.getLastChargeFactor();
        float charge = te.getChargeFactor();
        //(charge==1f && lastCharge!=1f) || (charge==0f && lastCharge!=0f) are required to enforce an update when it is fully charged or completely empty
        if (lastCharge > charge + CHARGE_UPDATE_BORDER || lastCharge < charge - CHARGE_UPDATE_BORDER || (charge == 1f && lastCharge != 1f) || (charge == 0f && lastCharge != 0f)) {
            te.updateChargeFactor(charge);
            createCallList(te);
        } else {
            SphereSegmentation lastSegmentation = te.getLastRenderedSegmentation();
            SphereSegmentation newSegmentation = te.getSegmentation();
            if (lastSegmentation != newSegmentation) { //if the player moved to a different Distance: recreate the Sphere
                te.updateSegmentation(newSegmentation);
                createCallList(te, newSegmentation);
            } else if (te.getCallList() == 0)
                createCallList(te);
            else
                GlStateManager.callList(te.getCallList());
        }
    }

    private void createCallList(ChargingStationTileEntity te) {
        te.updateSegmentation(te.getSegmentation());
        createCallList(te, te.getSegmentation());
    }

    private void createCallList(ChargingStationTileEntity te, SphereSegmentation segmentation) {
        if (te.getCallList() == 0)
            te.genCallList();
        GlStateManager.newList(te.getCallList(), GL11.GL_COMPILE_AND_EXECUTE);
        performRenderSphere(te, segmentation.getSegments());
        GlStateManager.endList();
    }

    private void performRenderSphere(ChargingStationTileEntity te, int segments) {
        double radius1 = 0;
        double radius2 = 0;

        double radius = .33; //radius of sphere

        double angle = 0;
        double dAngle = (Math.PI / segments);

        float x = 0;
        float y = 0;
        float z = 0;

        float red = Math.min(2 * (1f - te.getLastChargeFactor()), 1f);
        float green = Math.min(2 * te.getLastChargeFactor(), 1f);
        float blue = 0f;
        float alpha = 0.5f;

        GlStateManager.pushMatrix();
        GlStateManager.pushLightingAttributes();

        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableTexture();
        GlStateManager.translated(.5, 1.5, .5);
        //GlStateManager.depthMask(false);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Tessellator t = Tessellator.getInstance();

        BufferBuilder bufferBuilder = t.getBuffer();
        //bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        bufferBuilder.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < segments; i++) // loop latitude
        {
            angle = Math.PI / 2 - i * dAngle;
            radius1 = radius * Math.cos(angle);
            float z1 = (float) (radius * Math.sin(angle));
            float c1 = (float) ((Math.PI / 2 + angle) / Math.PI);   //calculate a colour

            angle = Math.PI / 2 - (i + 1) * dAngle;
            radius2 = radius * Math.cos(angle);
            float z2 = (float) (radius * Math.sin(angle));

            float c2 = (float) ((Math.PI / 2 + angle) / Math.PI);   //calculate a colour


            for (int j = 0; j <= 2 * segments; j++) // loop longitude
            {
                double cda = Math.cos(j * dAngle);
                double sda = Math.sin(j * dAngle);

                x = (float) (radius1 * cda);
                y = (float) (radius1 * sda);
                bufferBuilder.pos(x, y, z1).color(red, green, blue, alpha).endVertex();
                x = (float) (radius2 * cda);
                y = (float) (radius2 * sda);
                bufferBuilder.pos(x, y, z2).color(red, green, blue, alpha).endVertex();
            }

        }
        t.draw();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture();
        GlStateManager.popAttributes();
        GlStateManager.popMatrix();
    }

    private void renderParticles(ChargingStationTileEntity te) {

    }

    private void renderItem(ChargingStationTileEntity te) {
        ItemStack stack = te.getRenderStack();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableLighting();
        GlStateManager.pushMatrix();
        // Translate to the center of the block and .9 points higher
        GlStateManager.translated(.5, 1.5, .5);
        GlStateManager.scalef(.4f, .4f, .4f);
        float rotation = (float) (getWorld().getGameTime() % 80);
        GlStateManager.rotatef(360f * rotation / 80f, 0, 1, 0);
        Minecraft.getInstance().getItemRenderer().renderItem(stack, ItemCameraTransforms.TransformType.NONE);

        GlStateManager.popMatrix();
    }
}
