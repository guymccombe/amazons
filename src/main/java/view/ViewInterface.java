package view;

import controller.Controller;

public interface ViewInterface {
    void displayATurn(boolean isWhiteTurn);
    void displayWinner(boolean winnerIsWhite);
    void setController(Controller controller);
}