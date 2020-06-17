package com.Da_Technomancer.crossroads.render;

import com.Da_Technomancer.crossroads.Crossroads;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import org.lwjgl.opengl.GL11;

//Stores all the stitched textures and render types
public class CRRenderTypes extends RenderType{

	//Textures

	//Stitched to block atlas
	public static final ResourceLocation WINDMILL_TEXTURE = new ResourceLocation(Crossroads.MODID, "textures/models/wind_turbine_blade.png");
	public static final ResourceLocation DRILL_TEXTURE = new ResourceLocation("textures/block/iron_block.png");
	public static final ResourceLocation NODE_GIMBAL_TEXTURE = new ResourceLocation(Crossroads.MODID, "textures/models/gimbal.png");
	public static final ResourceLocation COPSHOWIUM_TEXTURE = new ResourceLocation(Crossroads.MODID, "textures/block/block_copshowium.png");
	public static final ResourceLocation QUARTZ_TEXTURE = new ResourceLocation(Crossroads.MODID, "textures/block/block_pure_quartz.png");
	public static final ResourceLocation CAST_IRON_TEXTURE = new ResourceLocation(Crossroads.MODID, "textures/block/block_cast_iron.png");
	public static final ResourceLocation AXLE_ENDS_TEXTURE = new ResourceLocation(Crossroads.MODID, "textures/models/axle_end.png");
	public static final ResourceLocation AXLE_SIDE_TEXTURE = new ResourceLocation(Crossroads.MODID, "textures/models/axle.png");
	public static final ResourceLocation HAMSTER_TEXTURE = new ResourceLocation(Crossroads.MODID, "textures/models/hamster.png");

	//Stitched to beam atlas
	public static final ResourceLocation BEAM_TEXTURE = new ResourceLocation(Crossroads.MODID, "textures/models/beam.png");

	//Stitched to flux sink atlas
	public static final ResourceLocation FLUX_SINK_TEXTURE = new ResourceLocation(Crossroads.MODID, "textures/models/flux_sink.png");

	//Types
	public static final RenderType BEAM_TYPE = RenderType.makeType("beam", DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_QUADS, 256, false, true, RenderType.State.getBuilder().cull(RenderState.CULL_DISABLED).texture(new RenderState.TextureState(BEAM_TEXTURE, false, false)).build(false));
	public static final RenderType FLUX_SINK_TYPE = RenderType.makeType("flux_sink", DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP, GL11.GL_QUADS, 256, false, true, RenderType.State.getBuilder().cull(RenderState.CULL_DISABLED).texture(new RenderState.TextureState(FLUX_SINK_TEXTURE, false, false)).transparency(RenderState.TRANSLUCENT_TRANSPARENCY).build(false));

	public static void stitchTextures(TextureStitchEvent.Pre event){
		//We only need to register textures which are not already part of a block model
		if(event.getMap().getTextureLocation().equals(AtlasTexture.LOCATION_BLOCKS_TEXTURE)) {
			event.addSprite(WINDMILL_TEXTURE);
			event.addSprite(NODE_GIMBAL_TEXTURE);
			event.addSprite(AXLE_ENDS_TEXTURE);
			event.addSprite(AXLE_SIDE_TEXTURE);
			event.addSprite(HAMSTER_TEXTURE);
		}
	}

	//This is a dummy constructor- we need to be in a subclass to access the protected fields
	//This constructor should never be called- everything is done statically
	private CRRenderTypes(){
		super("cr_dummy", DefaultVertexFormats.BLOCK, GL11.GL_QUADS, 256, false, false, () -> {}, () -> {});
		assert false;
	}
}