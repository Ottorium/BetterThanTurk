module at.htlhl.chess {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires javafx.swing;

    exports at.htlhl.chess.boardlogic.util;
    opens at.htlhl.chess.boardlogic.util to javafx.fxml;
    exports at.htlhl.chess.boardlogic;
    opens at.htlhl.chess.boardlogic to javafx.fxml;
    exports at.htlhl.chess.gui;
    opens at.htlhl.chess.gui to javafx.fxml;
}