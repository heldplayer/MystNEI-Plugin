
package me.heldplayer.plugins.NEI.mystcraft;

import java.util.ArrayList;
import java.util.Arrays;

import me.heldplayer.util.HeldCore.client.GuiHelper;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.liquids.LiquidDictionary;
import net.minecraftforge.liquids.LiquidStack;
import net.minecraftforge.oredict.OreDictionary;

import org.lwjgl.opengl.GL11;

import codechicken.nei.PositionedStack;
import codechicken.nei.api.IRecipeOverlayRenderer;
import codechicken.nei.api.IStackPositioner;
import codechicken.nei.forge.GuiContainerManager;
import codechicken.nei.recipe.RecipeInfo;
import codechicken.nei.recipe.TemplateRecipeHandler;

import com.xcompwiz.mystcraft.api.MystObjects;
import com.xcompwiz.mystcraft.api.internals.Color;
import com.xcompwiz.mystcraft.api.internals.ColorGradient;

/**
 * NEI integration class for ink mixer recipes
 * 
 * @author heldplayer
 * 
 */
@SuppressWarnings("rawtypes")
public class InkMixerRecipeHandler extends TemplateRecipeHandler {

    public class CachedInkMixerRecipe extends CachedRecipe {

        public String[] modifiers;
        public Float[] percentages;
        public ColorGradient gradient;
        public long frame;
        private PositionedStack stack;
        private PositionedStack leftOver;
        private PositionedStack ingredient;
        private ArrayList<PositionedStack> ingredients;

        public CachedInkMixerRecipe(Object ingredient) {
            this.ingredients = new ArrayList<PositionedStack>();

            if (ingredient instanceof ItemStack) {
                this.ingredient = new PositionedStack(ingredient, 74, 29);
                this.ingredients.add(this.ingredient);
            }
            else if (ingredient instanceof String) {
                ArrayList<ItemStack> list = OreDictionary.getOres((String) ingredient);

                if (list.size() > 0) {
                    this.ingredient = new PositionedStack(list, 74, 29);
                    this.ingredients.add(this.ingredient);
                }
            }
            else if (ingredient instanceof Integer) {
                this.ingredient = new PositionedStack(new ItemStack((Integer) ingredient, 1, OreDictionary.WILDCARD_VALUE), 74, 29);
                this.ingredients.add(this.ingredient);
            }
            else {
                this.ingredient = null;
            }

            InkMixerRecipe result = Integrator.getInkMixerRecipe(this.ingredient != null ? this.ingredient.item : null);
            if (result == null) {
                this.modifiers = null;
                this.percentages = null;
                this.gradient = null;
            }
            else {
                this.modifiers = result.modifiers;
                this.percentages = result.percentages;
                this.gradient = result.gradient;
            }
            this.frame = 0;

            ItemStack stack = new ItemStack(MystObjects.page, 1, 0);

            NBTTagCompound compound = new NBTTagCompound("tag");
            NBTTagCompound linkPanelCompound = new NBTTagCompound("linkpanel");

            NBTTagList list = new NBTTagList("properties");

            if (modifiers != null) {
                for (String modifier : modifiers) {
                    list.appendTag(new NBTTagString("", modifier));
                }
            }

            linkPanelCompound.setTag("properties", list);

            compound.setTag("linkpanel", linkPanelCompound);

            stack.setTagCompound(compound);

            this.stack = new PositionedStack(stack, 147, 37);

            this.ingredients.add(new PositionedStack(new ItemStack(MystObjects.inkvial), 3, 16));
            this.ingredients.add(new PositionedStack(new ItemStack(Item.paper), 3, 37));
            this.leftOver = new PositionedStack(new ItemStack(Item.glassBottle), 147, 16);
        }

        @Override
        public PositionedStack getResult() {
            return stack;
        }

        @Override
        public ArrayList<PositionedStack> getIngredients() {
            return this.ingredients;
        }

        @Override
        public PositionedStack getIngredient() {
            return this.ingredients.get(0);
        }

        @Override
        public PositionedStack getOtherStack() {
            return this.leftOver;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof CachedInkMixerRecipe)) {
                return false;
            }
            CachedInkMixerRecipe other = (CachedInkMixerRecipe) obj;
            if (!getOuterType().equals(other.getOuterType())) {
                return false;
            }
            if (ingredient == null) {
                if (other.ingredient != null) {
                    return false;
                }
            }
            else if (ingredient.item == null) {
                if (other.ingredient.item != null) {
                    return false;
                }
            }
            else if (!ItemStack.areItemStacksEqual(ingredient.item, other.ingredient.item)) {
                return false;
            }
            if (!Arrays.equals(modifiers, other.modifiers)) {
                return false;
            }
            if (stack == null) {
                if (other.stack != null) {
                    return false;
                }
            }
            else if (stack.item == null) {
                if (other.stack.item != null) {
                    return false;
                }
            }
            else if (!ItemStack.areItemStacksEqual(stack.item, other.stack.item)) {
                return false;
            }
            return true;
        }

        private InkMixerRecipeHandler getOuterType() {
            return InkMixerRecipeHandler.this;
        }

    }

    private LiquidStack liquid;

    public InkMixerRecipeHandler() {
        this.liquid = LiquidDictionary.getCanonicalLiquid("Liquid Black Dye");
    }

    @Override
    public String getRecipeName() {
        return "Ink Mixer";
    }

    @Override
    public String getGuiTexture() {
        return "/mods/mystcraft/gui/inkmixer.png";
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (MystObjects.page == null) {
            return;
        }

        if (outputId.equals("item")) {
            loadCraftingRecipes((ItemStack) results[0]);
            return;
        }

        ArrayList recipes = Integrator.getALlInkMixerRecipes();

        for (Object recipe : recipes) {
            arecipes.add(new CachedInkMixerRecipe(recipe));
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        if (MystObjects.page == null) {
            return;
        }

        if (result.getItem() == MystObjects.page) {
            NBTTagCompound compound = result.getTagCompound();
            if (compound == null)
                return;

            NBTTagCompound linkPanelCompound = compound.getCompoundTag("linkpanel");
            if (linkPanelCompound == null)
                return;

            NBTTagList list = linkPanelCompound.getTagList("properties");
            if (list == null)
                return;

            ArrayList recipes = Integrator.getALlInkMixerRecipes();

            for (Object recipeObj : recipes) {
                CachedInkMixerRecipe recipe = new CachedInkMixerRecipe(recipeObj);

                if (recipe.modifiers == null) {
                    continue;
                }
                if (list.tagCount() == 0) {
                    recipe:
                    {
                        for (String modifier : recipe.modifiers) {
                            if (!modifier.isEmpty()) {
                                break recipe;
                            }
                        }
                        arecipes.add(recipe);
                    }
                    continue;
                }

                for (int i = 0; i < list.tagCount(); i++) {
                    for (String modifier : recipe.modifiers) {
                        if (!modifier.isEmpty() && ((NBTTagString) list.tagAt(i)).data.equals(modifier) && !arecipes.contains(recipe)) {
                            arecipes.add(recipe);
                            break;
                        }
                    }
                }
            }
        }
        else if (result.getItem() == Item.glassBottle) {
            ArrayList recipes = Integrator.getALlInkMixerRecipes();

            for (Object ingr : recipes) {
                CachedInkMixerRecipe recipe = new CachedInkMixerRecipe(ingr);
                if (recipe.modifiers != null) {
                    arecipes.add(recipe);
                }
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        if (MystObjects.page == null) {
            return;
        }

        if (ingredient.getItem() == Item.paper || ingredient.getItem() == MystObjects.inkvial) {
            ArrayList recipes = Integrator.getALlInkMixerRecipes();

            for (Object ingr : recipes) {
                CachedInkMixerRecipe recipe = new CachedInkMixerRecipe(ingr);
                if (recipe.modifiers != null) {
                    arecipes.add(recipe);
                }
            }
        }
        else {
            CachedInkMixerRecipe recipe = new CachedInkMixerRecipe(ingredient);
            if (recipe.modifiers != null) {
                arecipes.add(recipe);
            }
        }
    }

    @Override
    public void loadTransferRects() {}

    @Override
    public int recipiesPerPage() {
        return 1;
    }

    @Override
    public void drawBackground(GuiContainerManager gui, int recipe) {
        renderTank(49, 5, 66, 65, gui, (CachedInkMixerRecipe) arecipes.get(recipe));

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        gui.bindTexture(getGuiTexture());
        gui.drawTexturedModalRect(0, 0, 5, 11, 166, 76);
    }

    @Override
    public void drawExtras(GuiContainerManager gui, int recipeId) {
        CachedInkMixerRecipe recipe = (CachedInkMixerRecipe) arecipes.get(recipeId);

        if (recipe.modifiers == null || recipe.modifiers.length == 0) {
            gui.drawText(5, 80, "No effects", 0x404040, false);
        }
        else {
            for (int i = 0; i < recipe.modifiers.length; i++) {
                String message = recipe.modifiers[i];

                if (message.isEmpty()) {
                    message = "Clears modifiers";
                }

                gui.drawText(5, 80 + 8 * i, message, 0x404040, false);

                if (PluginNEIMystcraft.percentages.getValue()) {
                    int percentage = (int) (recipe.percentages[i].floatValue() * 100);
                    message = "IT'S OVER 9000!";

                    if (percentage <= 15) {
                        message = "Tiny chance";
                    }
                    else if (percentage <= 30) {
                        message = "Small chance";
                    }
                    else if (percentage <= 60) {
                        message = "Medium chance";
                    }
                    else if (percentage <= 75) {
                        message = "Big chance";
                    }
                    else if (percentage <= 90) {
                        message = "Huge chance";
                    }

                    gui.drawText(166 - gui.getStringWidth(message), 80 + 8 * i, message, 0x404040, false);
                }
            }
        }
    }

    private void renderTank(int left, int top, int width, int height, GuiContainerManager gui, CachedInkMixerRecipe recipe) {
        GuiHelper.drawLiquid(liquid.itemID, liquid.itemMeta, left, top, width, height);

        if (!PluginNEIMystcraft.percentages.getValue() && recipe.gradient != null && recipe.gradient.getColorCount() > 0) {
            recipe.frame++;
            if (recipe.frame > recipe.gradient.getLength()) {
                recipe.frame = 0;
            }
            Color color = recipe.gradient.getColor(recipe.frame);
            int iColor = color.asInt();
            gui.drawGradientRect(left, top, left + width, top + height, 0x40000000 + iColor, 0xB0000000 + iColor);
        }
    }

    @Override
    public Class<? extends GuiContainer> getGuiClass() {
        return PluginNEIMystcraft.guiInkMixerClass;
    }

    @Override
    public String getOverlayIdentifier() {
        return "inkmixer";
    }

    @Override
    public IRecipeOverlayRenderer getOverlayRenderer(GuiContainer gui, int recipe) {
        IStackPositioner positioner = RecipeInfo.getStackPositioner(gui, getOverlayIdentifier());
        if (positioner == null) {
            return null;
        }
        return new InkMixerOverlayRenderer(getIngredientStacks(recipe), positioner, arecipes.get(recipe).getIngredient());
    }

}
