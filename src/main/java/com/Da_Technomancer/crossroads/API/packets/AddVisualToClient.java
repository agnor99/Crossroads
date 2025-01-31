package com.Da_Technomancer.crossroads.API.packets;

import com.Da_Technomancer.crossroads.render.CRRenderUtil;
import com.Da_Technomancer.crossroads.render.IVisualEffect;
import com.Da_Technomancer.essentials.packets.ClientPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class AddVisualToClient extends ClientPacket{

	public static final ArrayList<IVisualEffect> effectsToRender = new ArrayList<>();//Correct on client side only

	public CompoundTag nbt;

	private static final Field[] FIELDS = fetchFields(AddVisualToClient.class, "nbt");

	@SuppressWarnings("unused")
	public AddVisualToClient(){

	}

	public AddVisualToClient(CompoundTag nbt){
		this.nbt = nbt;
	}

	@Nonnull
	@Override
	protected Field[] getFields(){
		return FIELDS;
	}

	@Override
	protected void run(){
		Level world = SafeCallable.getClientWorld();
		if(world != null){
			effectsToRender.add(CRRenderUtil.visualFactories[nbt.getInt("id")].apply(SafeCallable.getClientWorld(), nbt));
		}
	}
}
