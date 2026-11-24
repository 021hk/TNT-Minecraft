package net.compressedtnt;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

/**
 * Special crafting recipe: 8 TNT around ANY damageable item (armor, tools,
 * swords, shield, ...) in the centre slot -> the same item, marked with the
 * {@link TntInfusedComponent}.
 */
public class TntInfuseRecipe implements CraftingRecipe {
        private final CraftingRecipeCategory category;

        public TntInfuseRecipe(CraftingRecipeCategory category) {
                this.category = category;
        }

        @Override
        public boolean matches(CraftingRecipeInput input, World world) {
                if (input.getSize() != 9) {
                        return false;
                }
                int tntCount = 0;
                ItemStack center = null;
                for (int i = 0; i < input.getSize(); i++) {
                        ItemStack stack = input.getStackInSlot(i);
                        if (i == 4) {
                                center = stack;
                        } else if (stack.isOf(Items.TNT)) {
                                tntCount++;
                        } else if (!stack.isEmpty()) {
                                return false;
                        }
                }
                if (tntCount != 8 || center == null || center.isEmpty() || center.isOf(Items.TNT)) {
                        return false;
                }
                return center.getMaxCount() == 1 && center.getMaxDamage() > 0;
        }

        @Override
        public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
                ItemStack result = input.getStackInSlot(4).copy();
                result.set(CompressedTnt.TNT_INFUSED, TntInfusedComponent.DEFAULT);
                return result;
        }

        @Override
        public boolean fits(int width, int height) {
                return width >= 3 && height >= 3;
        }

        @Override
        public ItemStack getResult(RegistryWrapper.WrapperLookup lookup) {
                return ItemStack.EMPTY;
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
                return CompressedTnt.TNT_INFUSE_SERIALIZER;
        }

        @Override
        public RecipeType<?> getType() {
                return RecipeType.CRAFTING;
        }

        @Override
        public CraftingRecipeCategory getCategory() {
                return this.category;
        }

        @Override
        public boolean isIgnoredInRecipeBook() {
                return true;
        }
}
