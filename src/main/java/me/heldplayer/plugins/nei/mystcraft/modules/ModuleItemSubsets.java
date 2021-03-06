package me.heldplayer.plugins.nei.mystcraft.modules;

import codechicken.nei.SubsetWidget;
import codechicken.nei.api.API;
import codechicken.nei.api.ItemFilter;
import cpw.mods.fml.relauncher.ReflectionHelper;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import me.heldplayer.plugins.nei.mystcraft.Objects;
import me.heldplayer.plugins.nei.mystcraft.PluginNEIMystcraft;
import me.heldplayer.plugins.nei.mystcraft.wrap.MystObjs;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.Level;

@SuppressWarnings("ConstantConditions")
public class ModuleItemSubsets implements IModule {

    private boolean enabled;

    @Override
    public void enable() {
        if (PluginNEIMystcraft.config.addItemRanges) {
            Objects.log.log(Level.DEBUG, "Adding item ranges to NEI");

            API.addSubset("Mod.Mystcraft", Collections.<ItemStack>emptySet());

            HashSet<ItemStack> blocks = new HashSet<ItemStack>();
            blocks.add(new ItemStack(MystObjs.portal));
            blocks.add(new ItemStack(MystObjs.crystal));
            blocks.add(new ItemStack(MystObjs.crystalReceptacle));
            blocks.add(new ItemStack(MystObjs.decay, 1, 0));
            blocks.add(new ItemStack(MystObjs.decay, 1, 1));
            blocks.add(new ItemStack(MystObjs.decay, 1, 3));
            blocks.add(new ItemStack(MystObjs.decay, 1, 4));
            blocks.add(new ItemStack(MystObjs.decay, 1, 6));
            blocks.add(new ItemStack(MystObjs.bookstand));
            blocks.add(new ItemStack(MystObjs.lectern));
            blocks.add(new ItemStack(MystObjs.writingDesk));
            blocks.add(new ItemStack(MystObjs.bookBinder));
            blocks.add(new ItemStack(MystObjs.inkMixer));
            blocks.add(new ItemStack(MystObjs.starFissure));
            blocks.add(new ItemStack(MystObjs.linkModifier));
            blocks.add(new ItemStack(MystObjs.blackInkBlock));
            blocks.add(new ItemStack(MystObjs.writingDeskBlock));

            API.addSubset("Mod.Mystcraft.Blocks", blocks);

            LinkedList<Runnable> delayedTasks = new LinkedList<Runnable>();

            final HashSet<ItemStack> items = new HashSet<ItemStack>();

            items.add(new ItemStack(MystObjs.writingDesk, 1, 0));
            items.add(new ItemStack(MystObjs.writingDesk, 1, 1));
            items.add(new ItemStack(MystObjs.inkVial));
            items.add(new ItemStack(MystObjs.folder));
            items.add(new ItemStack(MystObjs.booster));

            API.addSubset("Mod.Mystcraft.Items", items);

            if (PluginNEIMystcraft.config.addSymbolPages || PluginNEIMystcraft.config.addLinkPanels) {
                if (PluginNEIMystcraft.config.addSymbolPages) {
                    delayedTasks.add(new Runnable() {
                        @Override
                        public void run() {
                            API.addSubset("Mod.Mystcraft.Symbols", new ItemFilter() {

                                @Override
                                public boolean matches(ItemStack itemStack) {
                                    return !(itemStack.getItem() != MystObjs.page || !itemStack.hasTagCompound()) && itemStack.getTagCompound().hasKey("symbol");
                                }
                            });
                        }
                    });
                }
                if (PluginNEIMystcraft.config.addLinkPanels) {
                    delayedTasks.add(new Runnable() {
                        @Override
                        public void run() {
                            API.addSubset("Mod.Mystcraft.Link Panels", new ItemFilter() {

                                @Override
                                public boolean matches(ItemStack itemStack) {
                                    return !(itemStack.getItem() != MystObjs.page || !itemStack.hasTagCompound()) && itemStack.getTagCompound().hasKey("linkpanel");
                                }
                            });
                        }
                    });
                }
            } else {
                items.add(new ItemStack(MystObjs.page));
            }

            if (PluginNEIMystcraft.config.addCreativeNotebooks) {
                delayedTasks.add(new Runnable() {
                    @Override
                    public void run() {
                        API.addSubset("Mod.Mystcraft.Notebooks", new ItemFilter() {

                            @Override
                            public boolean matches(ItemStack itemStack) {
                                return itemStack.getItem() == MystObjs.portfolio;
                            }
                        });
                    }
                });
            } else {
                items.add(new ItemStack(MystObjs.portfolio));
            }

            if (PluginNEIMystcraft.config.addLinkingBooks) {
                delayedTasks.add(new Runnable() {
                    @Override
                    public void run() {
                        API.addSubset("Mod.Mystcraft.Linking Books", new ItemFilter() {

                            @Override
                            public boolean matches(ItemStack itemStack) {
                                return itemStack.getItem() == MystObjs.linkingBook || itemStack.getItem() == MystObjs.linkingBookUnlinked;
                            }
                        });
                    }
                });
            } else {
                items.add(new ItemStack(MystObjs.linkingBook));
                items.add(new ItemStack(MystObjs.linkingBookUnlinked));
            }

            if (PluginNEIMystcraft.config.addAgeList) {
                delayedTasks.add(new Runnable() {
                    @Override
                    public void run() {
                        API.addSubset("Mod.Mystcraft.Descriptive Books", new ItemFilter() {

                            @Override
                            public boolean matches(ItemStack itemStack) {
                                return itemStack.getItem() == MystObjs.descriptiveBook;
                            }
                        });
                    }
                });
            } else {
                items.add(new ItemStack(MystObjs.descriptiveBook));
            }

            delayedTasks.addFirst(new Runnable() {
                @Override
                public void run() {
                    API.addSubset("Mod.Mystcraft.Items", items);
                }
            });

            for (Runnable task : delayedTasks) {
                task.run();
            }

            delayedTasks.clear();

            this.enabled = true;
        }
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    @Override
    public void disable() {
        if (this.enabled) {
            SubsetWidget.SubsetTag root = ReflectionHelper.getPrivateValue(SubsetWidget.class, null, "root");
            synchronized (root) {
                root.children.remove("mystcraft");
            }

            this.enabled = false;
        }
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}
