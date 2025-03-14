package at.htlhl.chess.boardlogic.util;

import java.util.function.Consumer;

public class FieldChange {
    public String changedVariable;
    public Consumer<Object> undo;

    public FieldChange(String changedVariable, Consumer<Object> undo) {
        this.changedVariable = changedVariable;
        this.undo = undo;
    }

    public void undo() {
        undo.accept(null);
    }
}
