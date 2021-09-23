package fuzs.pickupnotifier.client.gui.entry;

import fuzs.pickupnotifier.config.ConfigHolder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Rarity;

public abstract class DisplayEntry {
    
    protected final Minecraft mc = Minecraft.getInstance(); 

    public static final int ENTRY_HEIGHT = 18;

    private final int textItemMargin = 4;
    private final Rarity rarity;
    protected int displayAmount;
    protected int remainingTicks;

    protected DisplayEntry(int amount, Rarity rarity) {

        this.displayAmount = Math.min(amount, ConfigHolder.getBehaviorConfig().maxCount);
        this.rarity = rarity;
        this.setDefaultDisplayTicks();
    }

    public boolean readyToRemove() {

        return this.remainingTicks <= 0;
    }

    public void tick() {

        if (this.remainingTicks > 0) {

            this.remainingTicks--;
        }
    }

    protected abstract Component getEntryName();

    public MutableComponent getTextComponent() {

        MutableComponent name = new TextComponent("").append(this.getEntryName());
        if (this.displayAmount <= 0) {

            return name;
        } else if (ConfigHolder.getDisplayConfig().position.isMirrored()) {

            name = new TextComponent(this.displayAmount + "x ").append(name);
        } else {

            name.append(" x" + this.displayAmount);
        }

        return name.setStyle(this.getComponentStyle());
    }

    private Style getComponentStyle() {

        if (!ConfigHolder.getDisplayConfig().ignoreRarity && this.rarity != Rarity.COMMON) {

            return Style.EMPTY.withColor(this.rarity.color);
        } else {

            return Style.EMPTY.withColor(ConfigHolder.getDisplayConfig().textColor);
        }
    }

    public float getRemainingTicksRelative(float partialTicks) {

        float moveTime = Math.min(ConfigHolder.getBehaviorConfig().moveTime, ConfigHolder.getBehaviorConfig().displayTime);
        return 1.0F - Mth.clamp((this.remainingTicks - partialTicks) / moveTime, 0.0F, 1.0F);
    }

    public void setDefaultDisplayTicks() {

        this.remainingTicks = ConfigHolder.getBehaviorConfig().displayTime;
    }

    public abstract boolean mayMergeWith(DisplayEntry other);

    public void mergeWith(DisplayEntry other) {

        this.displayAmount = Math.min(this.displayAmount + other.displayAmount, ConfigHolder.getBehaviorConfig().maxCount);
        this.setDefaultDisplayTicks();
    }

    public int getEntryWidth() {

        int textWidth = this.mc.font.width(this.getTextComponent());
        return ConfigHolder.getDisplayConfig().showSprite ? textWidth + this.textItemMargin + 16 : textWidth;
    }

    public void render(PoseStack poseStack, int posX, int posY, float alpha, float scale) {

        boolean mirrorPosition = ConfigHolder.getDisplayConfig().position.isMirrored();
        boolean withSprite = ConfigHolder.getDisplayConfig().showSprite;
        int posXSide = mirrorPosition || !withSprite ? posX : posX + 16 + this.textItemMargin;

        poseStack.pushPose();
        poseStack.scale(scale, scale, 1.0F);

        if (!this.mc.options.backgroundForChatOnly) {

            // copied from Options::getBackgroundColor
            int backgroundOpacity = (int) (this.mc.options.textBackgroundOpacity * (1.0F - alpha) * 255.0F) << 24 & -16777216;
            GuiComponent.fill(poseStack, posX - 2, posY, posX + this.getEntryWidth() + 4, posY + 16, backgroundOpacity);
        }

        int fadeTime = ConfigHolder.getBehaviorConfig().fadeAway ? 255 - (int) (255.0F * alpha) : 255;
        // prevents a bug where names would appear once at the end with full alpha
        if (fadeTime >= 5) {

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            GuiComponent.drawString(poseStack, this.mc.font, this.getTextComponent(), posXSide, posY + 3, 16777215 + (fadeTime << 24));
            if (withSprite) {

                int textWidth = this.mc.font.width(this.getTextComponent());
                this.renderSprite(poseStack, mirrorPosition ? posX + textWidth + this.textItemMargin : posX, posY, scale);
            }

            RenderSystem.disableBlend();
        }

        poseStack.popPose();
    }

    protected abstract void renderSprite(PoseStack matrixstack, int posX, int posY, float scale);

}
