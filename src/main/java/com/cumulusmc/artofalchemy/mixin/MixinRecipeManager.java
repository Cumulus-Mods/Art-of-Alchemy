package com.cumulusmc.artofalchemy.mixin;

import java.util.Optional;

import com.cumulusmc.artofalchemy.item.AoAItems;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

@Mixin(RecipeManager.class)
public abstract class MixinRecipeManager {

	@Inject(at = @At(value = "RETURN", ordinal = 0), method = "getRemainingStacks", cancellable = true,
		locals = LocalCapture.CAPTURE_FAILHARD)
	private <C extends Inventory, T extends Recipe<C>> void replaceRemainingStacks(RecipeType<T> recipeType, C inventory,
			World world, CallbackInfoReturnable<DefaultedList<ItemStack>> info, Optional<T> optional) {
		if (optional.get().getOutput().getItem() == AoAItems.ALKAHEST_BUCKET) {
			info.setReturnValue(DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY));
		}
	}
}
