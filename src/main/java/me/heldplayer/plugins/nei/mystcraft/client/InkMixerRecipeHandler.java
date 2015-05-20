package me.heldplayer.plugins.nei.mystcraft.client;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIClientUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.IRecipeOverlayRenderer;
import codechicken.nei.api.IStackPositioner;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.RecipeInfo;
import codechicken.nei.recipe.TemplateRecipeHandler;
import com.xcompwiz.mystcraft.api.util.Color;
import com.xcompwiz.mystcraft.api.util.ColorGradient;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import me.heldplayer.plugins.nei.mystcraft.Objects;
import me.heldplayer.plugins.nei.mystcraft.client.renderer.InkMixerOverlayRenderer;
import me.heldplayer.plugins.nei.mystcraft.modules.ModuleRecipes;
import me.heldplayer.plugins.nei.mystcraft.modules.ModuleTooltips;
import me.heldplayer.plugins.nei.mystcraft.wrap.MystObjs;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.StatCollector;
import net.minecraftforge.oredict.OreDictionary;
import net.specialattack.forge.core.asm.AccessHelper;
import net.specialattack.forge.core.client.GLState;
import net.specialattack.forge.core.client.gui.GuiHelper;
import org.lwjgl.opengl.GL11;

@SuppressWarnings("rawtypes")
public class InkMixerRecipeHandler extends TemplateRecipeHandler {

    @Override
    public String getRecipeName() {
        return StatCollector.translateToLocal("tile.myst.inkmixer.name");
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (!ModuleRecipes.inkMixerEnabled) {
            return;
        }

        if (outputId.equals("item") || outputId.equals("inkmixer")) {
            if (results.length > 0) {
                this.loadCraftingRecipes((ItemStack) results[0]);
            } else {
                ArrayList recipes = Integrator.getALlInkMixerRecipes();

                for (Object ingr : recipes) {
                    CachedInkMixerRecipe recipe = new CachedInkMixerRecipe(ingr);
                    if (recipe.modifiers != null) {
                        this.arecipes.add(recipe);
                    }
                }
            }
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        if (!ModuleRecipes.inkMixerEnabled) {
            return;
        }

        if (result.getItem() == MystObjs.page) {
            NBTTagCompound compound = result.getTagCompound();
            if (compound == null) {
                return;
            }

            if (!compound.hasKey("linkpanel")) {
                return;
            }
            NBTTagCompound linkPanelCompound = compound.getCompoundTag("linkpanel");

            if (!linkPanelCompound.hasKey("properties")) {
                return;
            }
            NBTTagList list = linkPanelCompound.getTagList("properties", 8);

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
                        this.arecipes.add(recipe);
                    }
                    continue;
                }

                for (int i = 0; i < list.tagCount(); i++) {
                    for (String modifier : recipe.modifiers) {
                        if (!modifier.isEmpty() && list.getStringTagAt(i).equals(modifier) && !this.arecipes.contains(recipe)) {
                            this.arecipes.add(recipe);
                            break;
                        }
                    }
                }
            }
        } else if (result.getItem() == Items.glass_bottle) {
            ArrayList recipes = Integrator.getALlInkMixerRecipes();

            for (Object ingr : recipes) {
                CachedInkMixerRecipe recipe = new CachedInkMixerRecipe(ingr);
                if (recipe.modifiers != null) {
                    this.arecipes.add(recipe);
                }
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        if (!ModuleRecipes.inkMixerEnabled) {
            return;
        }

        if (ingredient.getItem() == Items.paper || ingredient.getItem() == MystObjs.inkVial) {
            ArrayList recipes = Integrator.getALlInkMixerRecipes();

            for (Object ingr : recipes) {
                CachedInkMixerRecipe recipe = new CachedInkMixerRecipe(ingr);
                if (recipe.modifiers != null) {
                    this.arecipes.add(recipe);
                }
            }
        } else {
            ItemStack ingr = ingredient.copy();
            ingr.stackSize = 1;
            CachedInkMixerRecipe recipe = new CachedInkMixerRecipe(ingr);
            if (recipe.modifiers != null) {
                this.arecipes.add(recipe);
            }
        }
    }

    @Override
    public String getGuiTexture() {
        return "mystcraft:gui/inkmixer.png";
    }

    @Override
    public String getOverlayIdentifier() {
        return "inkmixer";
    }

    @Override
    public Class<? extends GuiContainer> getGuiClass() {
        return Integrator.guiInkMixerClass;
    }

    @Override
    public void drawBackground(int recipe) {
        this.renderTank(49, 5, 66, 65, (CachedInkMixerRecipe) this.arecipes.get(recipe));

        GLState.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GuiDraw.changeTexture(this.getGuiTexture());
        GuiDraw.drawTexturedModalRect(0, 0, 5, 11, 166, 76);
    }

    private void renderTank(int left, int top, int width, int height, CachedInkMixerRecipe recipe) {
        GuiHelper.drawFluid(MystObjs.black_ink, left, top, width, height, 0.0F);

        if (recipe != null && recipe.gradient != null && recipe.gradient.getColorCount() > 0) {
            if (!NEIClientUtils.shiftKey()) {
                recipe.frame++;
            }
            Color color = recipe.gradient.getColor((float) recipe.frame / 300.0F);
            int iColor = color.asInt();
            GuiDraw.drawGradientRect(left, top, left + width, top + height, 0x40000000 + iColor, 0xB0000000 + iColor);

            Integrator.renderAPI.drawColorEye(82.5F, 37.5F, 0.0F, 20.0F, color);
        } else {
            int iColor = Objects.rnd.nextInt(0xFFFFFF) & 0xFFFFFF | 0xFF000000;
            Color color = new Color(iColor);

            GuiDraw.drawGradientRect(left, top, left + width, top + height, Objects.rnd.nextInt(0xFFFFFF) & 0xFFFFFF | 0xFF000000, Objects.rnd.nextInt(0xFFFFFF) & 0xFFFFFF | 0xFF000000);

            Integrator.renderAPI.drawColorEye(82.5F, 37.5F, 0.0F, 20.0F, color);
        }
    }

    @Override
    public IRecipeOverlayRenderer getOverlayRenderer(GuiContainer gui, int recipe) {
        IStackPositioner positioner = RecipeInfo.getStackPositioner(gui, this.getOverlayIdentifier());
        if (positioner == null) {
            return null;
        }
        return new InkMixerOverlayRenderer(this.getIngredientStacks(recipe), positioner, this.arecipes.get(recipe).getIngredient());
    }

    @Override
    public int recipiesPerPage() {
        return 1;
    }

    @Override
    public List<String> handleItemTooltip(GuiRecipe gui, ItemStack stack, List<String> currenttip, int recipeId) {
        currenttip = super.handleItemTooltip(gui, stack, currenttip, recipeId);

        if (!ModuleTooltips.inkMixerTooltips || !ModuleRecipes.inkMixerEnabled) {
            return currenttip;
        }

        Point mousepos = GuiDraw.getMousePosition();
        Point relMouse = new Point(mousepos.x - AccessHelper.getGuiLeft(gui), mousepos.y - AccessHelper.getGuiTop(gui));

        Point center = new Point(87, 54);

        if (currenttip.isEmpty() && stack == null && center.distance(relMouse) < 34.0D) {
            currenttip.add(StatCollector.translateToLocal("nei.mystcraft.inkmixer.drop"));
        }

        return currenttip;
    }

    public class CachedInkMixerRecipe extends CachedRecipe {

        public String[] modifiers;
        public ColorGradient gradient;
        public int frame;
        private PositionedStack stack;
        private PositionedStack leftOver;
        private PositionedStack ingredient;
        private ArrayList<PositionedStack> ingredients;

        public CachedInkMixerRecipe(Object ingredient) {
            this.ingredients = new ArrayList<PositionedStack>();

            if (ingredient instanceof ItemStack) {
                this.ingredient = new PositionedStack(ingredient, 74, 29);
                this.ingredients.add(this.ingredient);
            } else if (ingredient instanceof String) {
                ArrayList<ItemStack> list = OreDictionary.getOres((String) ingredient);

                if (list.size() > 0) {
                    this.ingredient = new PositionedStack(list, 74, 29);
                    this.ingredients.add(this.ingredient);
                }
            } else {
                this.ingredient = null;
            }

            if (this.ingredient != null) {
                this.ingredient.setPermutationToRender(Objects.rnd.nextInt(this.ingredient.items.length));
            }

            InkMixerRecipe result = Integrator.getInkMixerRecipe(this.ingredient != null ? this.ingredient.item : null);
            if (result == null) {
                this.modifiers = null;
                this.gradient = null;
            } else {
                this.modifiers = result.modifiers;
                this.gradient = result.gradient;
            }
            this.frame = 0;

            if (this.modifiers != null) {
                ItemStack stack = Integrator.itemFactory.buildLinkPage(this.modifiers);

                this.stack = new PositionedStack(stack, 147, 37);
            } else {
                ItemStack stack = Integrator.itemFactory.buildLinkPage();

                this.stack = new PositionedStack(stack, 147, 37);
            }

            this.ingredients.add(new PositionedStack(new ItemStack(MystObjs.inkVial), 3, 16));
            this.ingredients.add(new PositionedStack(new ItemStack(Items.paper), 3, 37));
            this.leftOver = new PositionedStack(new ItemStack(Items.glass_bottle), 147, 16);
        }

        @Override
        public PositionedStack getResult() {
            return this.stack;
        }

        @Override
        public List<PositionedStack> getIngredients() {
            if (!NEIClientUtils.shiftKey() && InkMixerRecipeHandler.this.cycleticks % 20 == 0) {
                this.ingredient.setPermutationToRender(Objects.rnd.nextInt(this.ingredient.items.length));
            }
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
            if (!this.getOuterType().equals(other.getOuterType())) {
                return false;
            }
            if (this.ingredient == null) {
                if (other.ingredient != null) {
                    return false;
                }
            } else if (this.ingredient.item == null) {
                if (other.ingredient.item != null) {
                    return false;
                }
            } else if (!ItemStack.areItemStacksEqual(this.ingredient.item, other.ingredient.item)) {
                return false;
            }
            if (!Arrays.equals(this.modifiers, other.modifiers)) {
                return false;
            }
            if (this.stack == null) {
                if (other.stack != null) {
                    return false;
                }
            } else if (this.stack.item == null) {
                if (other.stack.item != null) {
                    return false;
                }
            } else if (!ItemStack.areItemStacksEqual(this.stack.item, other.stack.item)) {
                return false;
            }
            return true;
        }

        private InkMixerRecipeHandler getOuterType() {
            return InkMixerRecipeHandler.this;
        }

    }

}
