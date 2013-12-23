package me.josvth.trade.transaction;

import me.josvth.trade.Trade;
import me.josvth.trade.transaction.action.Action;
import me.josvth.trade.transaction.action.ActionExecutor;
import me.josvth.trade.transaction.inventory.Layout;

public class Transaction {

    private final TransactionManager manager;

    private final Layout layout;

    private final Trader traderA;
    private final Trader traderB;

    private Transaction.Stage stage = Transaction.Stage.PRE;
    private TransactionExecutor transactionExecutor = new TransactionExecutor();

    public Transaction(TransactionManager manager, Layout layout, String playerA, String playerB) {

        this.manager = manager;
        this.layout = layout;

        traderA = new Trader(this, playerA, layout.getOfferSize());
        traderB = new Trader(this, playerB, layout.getOfferSize());

        traderA.setOther(traderB);
        traderB.setOther(traderA);
    }

    public TransactionManager getManager() {
        return manager;
    }

    public Trade getPlugin() {
        return manager.getPlugin();
    }

    public Trader getTraderA() {
        return traderA;
    }

    public Trader getTraderB() {
        return traderB;
    }

    public Trader getTrader(String playerName) {
        if (traderA.getName().equals(playerName))
            return traderA;
        if (traderB.getName().equals(playerName))
            return traderB;
        throw new IllegalArgumentException("Player " + playerName + " is not participating in this trade or went offline.");
    }

    public Layout getLayout() {
        return layout;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Transaction.Stage getStage() {
        return stage;
    }

    public boolean isStarted() {
        return stage == Transaction.Stage.IN_PROGRESS;
    }

    public boolean hasEnded() {
        return stage == Transaction.Stage.POST;
    }

    public void start() {

        if (isStarted()) {
            throw new IllegalArgumentException("Cannot start an already started transaction");
        }

        if (hasEnded()) {
            throw new IllegalArgumentException("Cannot start an ended transaction");
        }

        if (layout == null) {
            throw new IllegalStateException("Cannot start transaction without an layout.");
        }

        if (manager.isInTransaction(traderA.getName()) || manager.isInTransaction(traderB.getName())) {
            throw new IllegalArgumentException("One of the traders is already trading!");
        }

        manager.addTransaction(this);

        stage = Transaction.Stage.IN_PROGRESS;

        traderA.openInventory();
        traderB.openInventory();

    }

    public void stop() {

        stage = Transaction.Stage.POST;

        manager.removeTransaction(this);

    }

    public boolean useLogging() {
        return false;
    }

    public void logAction(Action action) {

    }

    public TransactionExecutor getTransactionExecutor() {
        return transactionExecutor;
    }

    private class TransactionExecutor implements ActionExecutor {

        @Override
        public String getName() {
            return "TRANSACTION";
        }

    }

    public enum Stage {
        PRE,
        IN_PROGRESS,
        POST;
    }
}
