package net.dumbcode.dumblibrary.client.gui;

import net.dumbcode.dumblibrary.server.taxidermy.TaxidermyContainer;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;

public abstract class TaxidermyScreen extends Screen implements IHasContainer<TaxidermyContainer> {
    private final TaxidermyContainer container;

    protected TaxidermyScreen(TaxidermyContainer container) {
        super(new StringTextComponent("Taxidermy :)"));
        this.container = container;
    }

    @Override
    public TaxidermyContainer getMenu() {
        return this.container;
    }
}
