package me.josvth.trade.transaction.action.trader.offer;

import me.josvth.trade.transaction.Trader;
import me.josvth.trade.transaction.inventory.offer.MoneyOffer;
import me.josvth.trade.transaction.inventory.offer.OfferList;
import me.josvth.trade.transaction.inventory.slot.MoneySlot;
import net.milkbowl.vault.economy.Economy;

public class ChangeMoneyAction extends ChangeOfferAction {

    public ChangeMoneyAction(Trader trader, OfferList list, int amount) {
        super(trader, list);
        setOffer(MoneyOffer.create(getTrader(), Math.abs(amount)));
        setAddition(amount > 0);
    }

    private Economy getEconomy() {
        return getTrader().getTransaction().getPlugin().getEconomy();
    }

    @Override
    public void execute() {

        if (isAdd()) {

            final double moneyToAdd = (double) getInitialAmount() / Math.pow(10, getEconomy().fractionalDigits());

            if (!getEconomy().has(getTrader().getName(), moneyToAdd)) {
                getTrader().getFormattedMessage("money.insufficient").send(getPlayer(), "%money%", getEconomy().format(moneyToAdd));
                return;
            }

        }

        // Execute super
        super.execute();

        if (isAdd()) {

            // Take money from player
            final int added = getChangedAmount();
            final double addedDouble = added / Math.pow(10, getEconomy().fractionalDigits());

            // Send messages
            getTrader().getFormattedMessage("money.added.self").send(getPlayer(), "%money%", getEconomy().format(addedDouble));

            if (added > 0) {

                // Only send the other trader a message if something actually was changed
                if (getOtherTrader().hasFormattedMessage("money.added.other")) {
                    getOtherTrader().getFormattedMessage("money.added.other").send(getOtherPlayer(), "%player%", getTrader().getName(), "%money%", getEconomy().format(addedDouble));
                }

                getEconomy().withdrawPlayer(getTrader().getName(), addedDouble);

                // Update money slots
                MoneySlot.updateMoneySlots(getTrader().getHolder(), true, getCurrentAmount());

            }

        } else {

            // Deposit money
            final int removed = getChangedAmount();
            final double removedDouble = (double) removed / Math.pow(10, getEconomy().fractionalDigits());

            // Send messages
            getTrader().getFormattedMessage("money.removed.self").send(getPlayer(), "%money%", getEconomy().format(removedDouble));

            if (removed > 0) {

                getEconomy().depositPlayer(getTrader().getName(), removedDouble);

                // Only send the other trader a message if something actually was changed
                getOtherTrader().getFormattedMessage("money.removed.other").send(getOtherPlayer(), "%player%", getTrader().getName(), "%money%", getEconomy().format(removedDouble));

                // Update money slots
                MoneySlot.updateMoneySlots(getTrader().getHolder(), true, getCurrentAmount());

            }

        }

    }

}
