package com.hexdragon.enchre.block.enchantingtable;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import java.util.List;
import java.util.Random;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.model.BookModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnchantmentNameParts;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EnchantmentScreenRe extends ContainerScreen<EnchantmentContainerRe> {
    public EnchantmentScreenRe(EnchantmentContainerRe container, PlayerInventory playerInventory, ITextComponent textComponent) {super(container, playerInventory, textComponent); }

    // 资源与模型导入
    private static final ResourceLocation ENCHANTMENT_TABLE_GUI_TEXTURE = new ResourceLocation("textures/gui/container/enchanting_table.png");
    private static final ResourceLocation ENCHANTMENT_TABLE_BOOK_TEXTURE = new ResourceLocation("textures/entity/enchanting_table_book.png");
    private static final BookModel MODEL_BOOK = new BookModel();

    // 鼠标点击事件
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int guiX = (this.width - this.xSize) / 2;
        int guiY = (this.height - this.ySize) / 2;
        for(int id = 0; id < 3; ++id) {
            // 检测是否点击的附魔选项按钮
            double x = mouseX - (double)(guiX + 60);
            double y = mouseY - (double)(guiY + 14 + 19 * id);
            if (x >= 0.0D && y >= 0.0D && x < 108.0D && y < 19.0D && this.container.enchantItem(this.minecraft.player, id)) {
                this.minecraft.playerController.sendEnchantPacket((this.container).windowId, id);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    // GUI 上书本的打开关闭动画，和各种控制书本的杂七杂八的参数
    private final Random bookRandom = new Random();
    public float flip; public float oFlip;
    public float flipT; public float flipA;
    public float open; public float oOpen;
    private ItemStack lastItemStack = ItemStack.EMPTY;
    public void tick() {
        super.tick();
        this.tickBook();
    }
    public void tickBook() {
        ItemStack itemstack = this.container.getSlot(0).getStack();
        if (!ItemStack.areItemStacksEqual(itemstack, this.lastItemStack)) {
            this.lastItemStack = itemstack;
            do {
                this.flipT += (float)(this.bookRandom.nextInt(4) - this.bookRandom.nextInt(4));
            } while(this.flip <= this.flipT + 1.0F && this.flip >= this.flipT - 1.0F);
        }
        this.oFlip = this.flip;
        this.oOpen = this.open;
        boolean shouldBookOpen = false;
        for(int i = 0; i < 3; ++i) {
            if ((this.container).enchantLevels[i] != 0) {
                shouldBookOpen = true;
            }
        }
        if (shouldBookOpen) {
            this.open += 0.2F;
        } else {
            this.open -= 0.2F;
        }
        this.open = MathHelper.clamp(this.open, 0.0F, 1.0F);
        float f1 = (this.flipT - this.flip) * 0.4F;
        float f = 0.2F;
        f1 = MathHelper.clamp(f1, -0.2F, 0.2F);
        this.flipA += (f1 - this.flipA) * 0.9F;
        this.flip += this.flipA;
    }

    // 渲染 ToolTip
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        partialTicks = this.minecraft.getRenderPartialTicks();
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
        // 渲染 ToolTip 文本
        for(int id = 0; id < 3; ++id) {
            int enchantLevels = (this.container).enchantLevels[id];
            Enchantment enchantment = Enchantment.getEnchantmentByID((this.container).guiEnchantId[id]);
            int guiEnchantLevel = (this.container).guiEnchantLevel[id];
            // 判断鼠标是否在按钮上
            if (!this.isPointInRegion(60, 14 + 19 * id, 108, 17, mouseX, mouseY) && enchantLevels > 0) continue;
            List<ITextComponent> list = Lists.newArrayList();
            if(enchantment == null) {
                // 没有可用附魔，显示 limitedEnchantability 信息
                list.add((new TranslationTextComponent("container.enchant.clue", "")).mergeStyle(TextFormatting.WHITE));
                list.add(new StringTextComponent(""));
                list.add(new TranslationTextComponent("forge.container.enchant.limitedEnchantability").mergeStyle(TextFormatting.RED));
            } else if (!this.minecraft.player.abilities.isCreativeMode) {
                // 显示第一条附魔
                list.add((new TranslationTextComponent("container.enchant.clue", enchantment.getDisplayName(guiEnchantLevel))).mergeStyle(TextFormatting.WHITE));
                // 显示附魔消耗（等级、青金石）
                list.add(StringTextComponent.EMPTY);
                if (this.minecraft.player.experienceLevel < enchantLevels) {
                    list.add((new TranslationTextComponent("container.enchant.level.requirement", (this.container).enchantLevels[id])).mergeStyle(TextFormatting.RED));
                } else {
                    IFormattableTextComponent iformattabletextcomponent;
                    int cost = id + 1;
                    if (cost == 1) {
                        iformattabletextcomponent = new TranslationTextComponent("container.enchant.lapis.one");
                    } else {
                        iformattabletextcomponent = new TranslationTextComponent("container.enchant.lapis.many", cost);
                    }
                    list.add(iformattabletextcomponent.mergeStyle(this.container.getLapisAmount() >= cost ? TextFormatting.GRAY : TextFormatting.RED));
                    IFormattableTextComponent iformattabletextcomponent1;
                    if (cost == 1) {
                        iformattabletextcomponent1 = new TranslationTextComponent("container.enchant.level.one");
                    } else {
                        iformattabletextcomponent1 = new TranslationTextComponent("container.enchant.level.many", cost);
                    }
                    list.add(iformattabletextcomponent1.mergeStyle(TextFormatting.GRAY));
                }
            }
            // 提交 ToolTip 渲染
            this.func_243308_b(matrixStack, list, mouseX, mouseY);
            break;
        }
    }

    // 渲染 GUI 组件
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        // 大概是渲染 GUI 上那本书，没注释看着太屎坑了
        RenderHelper.setupGuiFlatDiffuseLighting();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(ENCHANTMENT_TABLE_GUI_TEXTURE);
        int guiX = (this.width - this.xSize) / 2;
        int guiY = (this.height - this.ySize) / 2;
        this.blit(matrixStack, guiX, guiY, 0, 0, this.xSize, this.ySize);
        RenderSystem.matrixMode(5889);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        int k = (int)this.minecraft.getMainWindow().getGuiScaleFactor();
        RenderSystem.viewport((this.width - 320) / 2 * k, (this.height - 240) / 2 * k, 320 * k, 240 * k);
        RenderSystem.translatef(-0.34F, 0.23F, 0.0F);
        RenderSystem.multMatrix(Matrix4f.perspective(90.0D, 1.3333334F, 9.0F, 80.0F));
        RenderSystem.matrixMode(5888);
        matrixStack.push();
        MatrixStack.Entry matrixstack$entry = matrixStack.getLast();
        matrixstack$entry.getMatrix().setIdentity();
        matrixstack$entry.getNormal().setIdentity();
        matrixStack.translate(0.0D, (double)3.3F, 1984.0D);
        matrixStack.scale(5.0F, 5.0F, 5.0F);
        matrixStack.rotate(Vector3f.ZP.rotationDegrees(180.0F));
        matrixStack.rotate(Vector3f.XP.rotationDegrees(20.0F));
        float f1 = MathHelper.lerp(partialTicks, this.oOpen, this.open);
        matrixStack.translate((double)((1.0F - f1) * 0.2F), (double)((1.0F - f1) * 0.1F), (double)((1.0F - f1) * 0.25F));
        matrixStack.rotate(Vector3f.YP.rotationDegrees(-(1.0F - f1) * 90.0F - 90.0F));
        matrixStack.rotate(Vector3f.XP.rotationDegrees(180.0F));
        float f3 = MathHelper.lerp(partialTicks, this.oFlip, this.flip) + 0.25F;
        float f4 = MathHelper.lerp(partialTicks, this.oFlip, this.flip) + 0.75F;
        f3 = (f3 - (float)MathHelper.fastFloor((double)f3)) * 1.6F - 0.3F;
        f4 = (f4 - (float)MathHelper.fastFloor((double)f4)) * 1.6F - 0.3F;
        if (f3 < 0.0F) f3 = 0.0F;
        if (f4 < 0.0F) f4 = 0.0F;
        if (f3 > 1.0F) f3 = 1.0F;
        if (f4 > 1.0F) f4 = 1.0F;
        RenderSystem.enableRescaleNormal();
        MODEL_BOOK.setBookState(0.0F, f3, f4, f1);
        IRenderTypeBuffer.Impl irendertypebuffer$impl = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
        IVertexBuilder ivertexbuilder = irendertypebuffer$impl.getBuffer(MODEL_BOOK.getRenderType(ENCHANTMENT_TABLE_BOOK_TEXTURE));
        MODEL_BOOK.render(matrixStack, ivertexbuilder, 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        irendertypebuffer$impl.finish();
        matrixStack.pop();
        RenderSystem.matrixMode(5889);
        RenderSystem.viewport(0, 0, this.minecraft.getMainWindow().getFramebufferWidth(), this.minecraft.getMainWindow().getFramebufferHeight());
        RenderSystem.popMatrix();
        RenderSystem.matrixMode(5888);
        // 渲染右边三个按钮
        RenderHelper.setupGui3DDiffuseLighting();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        EnchantmentNameParts.getInstance().reseedRandomGenerator((long)this.container.getXpSeed());
        int lapisAmount = this.container.getLapisAmount();
        for(int id = 0; id < 3; id++) {
            int buttonX = guiX + 60;
            int textX = buttonX + 20;
            this.setBlitOffset(0);
            this.minecraft.getTextureManager().bindTexture(ENCHANTMENT_TABLE_GUI_TEXTURE);
            int enchantLevel = (this.container).enchantLevels[id];
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            if (enchantLevel == 0) {
                // 渲染 “不可用” 空白按钮背景
                this.blit(matrixStack, buttonX, guiY + 14 + 19 * id, 0, 185, 108, 19);
            } else {
                String enchantLevelString = String.valueOf(enchantLevel);
                int enchantLevelStringWidth = 86 - this.font.getStringWidth(enchantLevelString);
                ITextProperties itextproperties = EnchantmentNameParts.getInstance().getGalacticEnchantmentName(this.font, enchantLevelStringWidth);
                int stringColor = 6839882;
                if (((lapisAmount < id + 1 || this.minecraft.player.experienceLevel < enchantLevel) && !this.minecraft.player.abilities.isCreativeMode) || this.container.guiEnchantId[id] == -1) {
                    // 渲染 “买不起” 按钮背景
                    this.blit(matrixStack, buttonX, guiY + 14 + 19 * id, 0, 185, 108, 19);
                    // 渲染附魔等级文本与图片
                    this.blit(matrixStack, buttonX + 1, guiY + 15 + 19 * id, 16 * id, 239, 16, 16);
                    this.font.func_238418_a_(itextproperties, textX, guiY + 16 + 19 * id, enchantLevelStringWidth, (stringColor & 16711422) >> 1);
                    stringColor = 4226832;
                } else {
                    // 渲染按钮背景
                    int k2 = mouseX - (guiX + 60);
                    int l2 = mouseY - (guiY + 14 + 19 * id);
                    if (k2 >= 0 && l2 >= 0 && k2 < 108 && l2 < 19) { // 鼠标悬浮检测
                        // 渲染鼠标指向的按钮背景
                        this.blit(matrixStack, buttonX, guiY + 14 + 19 * id, 0, 204, 108, 19);
                        stringColor = 16777088;
                    } else {
                        // 渲染普通按钮背景
                        this.blit(matrixStack, buttonX, guiY + 14 + 19 * id, 0, 166, 108, 19);
                    }
                    // 渲染附魔等级文本与图片
                    this.blit(matrixStack, buttonX + 1, guiY + 15 + 19 * id, 16 * id, 223, 16, 16);
                    this.font.func_238418_a_(itextproperties, textX, guiY + 16 + 19 * id, enchantLevelStringWidth, stringColor);
                    stringColor = 8453920;
                }
                this.font.drawStringWithShadow(matrixStack, enchantLevelString, (float)(textX + 86 - this.font.getStringWidth(enchantLevelString)), (float)(guiY + 16 + 19 * id + 7), stringColor);
            }
        }

    }

}
