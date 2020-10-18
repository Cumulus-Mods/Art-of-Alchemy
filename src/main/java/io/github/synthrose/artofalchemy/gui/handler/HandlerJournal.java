package io.github.synthrose.artofalchemy.gui.handler;

import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.synthrose.artofalchemy.ArtOfAlchemy;
import io.github.synthrose.artofalchemy.gui.widget.WFormulaList;
import io.github.synthrose.artofalchemy.item.AbstractItemFormula;
import io.github.synthrose.artofalchemy.item.ItemJournal;
import io.github.synthrose.artofalchemy.network.AoAClientNetworking;
import io.github.synthrose.artofalchemy.util.AoAHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class HandlerJournal extends SyncedGuiDescription {

	Hand hand;
	WTextField searchBar;
	WButton clearButton;
	WFormulaList formulaList;
	ItemStack journal;

	Inventory inventory = new SimpleInventory(1) {
		@Override
		public boolean isValid(int slot, ItemStack stack) {
			return (stack.getItem() instanceof AbstractItemFormula) && !(stack.getItem() instanceof ItemJournal);
		}
	};

	@SuppressWarnings("MethodCallSideOnly")
	public HandlerJournal(int syncId, PlayerInventory playerInventory, ScreenHandlerContext ctx, Hand hand) {
		super(AoAHandlers.JOURNAL, syncId, playerInventory);
		blockInventory = inventory;

		this.hand = hand;
		this.journal = playerInventory.player.getStackInHand(hand);

		WGridPanel root = new WGridPanel(1);
		setRootPanel(root);
		root.setSize(160, 128 + 18 * 5);

		WSprite slotIcon = new WSprite(new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/add_formula.png"));
		root.add(slotIcon, 2, 17, 16, 16);

		WItemSlot slot = WItemSlot.of(inventory, 0);
		root.add(slot, 1, 16);

		searchBar = new WTextField() {
			public void setSize(int x, int y) {
				super.setSize(x, y);
			}

			@Override
			public void onKeyPressed(int ch, int key, int modifiers) {
				super.onKeyPressed(ch, key, modifiers);
				formulaList.refresh(journal, this.text);
			}
		};
		root.add(searchBar, 22, 14, 6 * 18 + 6, 12);

		WSprite background = new WSprite(new Identifier(ArtOfAlchemy.MOD_ID, "textures/gui/rune_bg.png"));
		root.add(background, 0, 2 * 18 + 10, 9 * 18, 5 * 18);

		formulaList = new WFormulaList(journal, hand);
		formulaList.refresh();
		root.add(formulaList, 0, 2 * 18, 9 * 18 - 2, 6 * 17 - 1);

		WLabel title = new WLabel(journal.getName());
		title.setHorizontalAlignment(HorizontalAlignment.CENTER);
		root.add(title, 2 * 18, 0, 5 * 18, 18);

		clearButton = new WButton(new LiteralText("❌"));
		clearButton.setAlignment(HorizontalAlignment.CENTER);
		clearButton.setParent(root);
		root.add(clearButton, 7 * 18 + 14, 14, 20, 20);
		clearButton.setOnClick(() -> {
			AoAClientNetworking.sendJournalSelectPacket(Registry.ITEM.getId(Items.AIR), hand);
		});
		clearButton.setEnabled(ItemJournal.getFormula(this.journal) != Items.AIR);

		root.add(this.createPlayerInventoryPanel(), 0, 8 * 18);

		root.validate(this);
	}

	@Override
	public void close(PlayerEntity player) {
		dropInventory(player, world, inventory);
		super.close(player);
	}

	@Override
	public ItemStack onSlotClick(int slotNumber, int button, SlotActionType action, PlayerEntity player) {
		if (slotNumber >= 0 && slotNumber < slots.size()) {
			Slot slot = getSlot(slotNumber);
			if (slot != null) {
				if (slot.getStack().getItem() instanceof ItemJournal) {
					return ItemStack.EMPTY;
				}
			}
		}
		ItemStack stack = super.onSlotClick(slotNumber, button, action, player);
		tryAddPage();
		refresh(journal);
		return stack;
	}

	public void tryAddPage() {
		ItemStack stack = inventory.getStack(0);
		if (stack.getItem() instanceof AbstractItemFormula) {
			if (ItemJournal.addFormula(journal, AoAHelper.getTarget(stack))) {
				stack.decrement(1);
				inventory.markDirty();
				playerInventory.markDirty();
			}
		}
	}

	public void refresh(ItemStack journal) {
		if (journal == null) {
			journal = playerInventory.player.getStackInHand(hand);
		}
		this.journal = journal;
		if (this.journal.getItem() instanceof ItemJournal) {
			formulaList.refresh(this.journal, searchBar.getText());
			clearButton.setEnabled(ItemJournal.getFormula(this.journal) != Items.AIR);
		} else {
			this.close(playerInventory.player);
		}
	}

}
