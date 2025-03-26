package model;
import chess.ChessGame;

import java.util.Objects;

public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
    public GameData {
        if (gameID <= 0) {
            throw new IllegalArgumentException("gameID cannot be negative or zero");
        }
        /*if (gameName == null || game == null) {
            throw new IllegalArgumentException("Fields cannot be null");
        }*/
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GameData gameData = (GameData) o;
        return gameID == gameData.gameID && Objects.equals(whiteUsername, gameData.whiteUsername)
                && Objects.equals(blackUsername, gameData.blackUsername) && Objects.equals(gameName, gameData.gameName)
                && Objects.equals(game, gameData.game);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameID, whiteUsername, blackUsername, gameName, game);
    }

    @Override
    public int gameID() {
        return gameID;
    }

    @Override
    public String whiteUsername() {
        return whiteUsername;
    }

    @Override
    public String blackUsername() {
        return blackUsername;
    }

    @Override
    public String gameName() {
        return gameName;
    }

    @Override
    public ChessGame game() {
        return game;
    }

    @Override
    public String toString() {
        return "GameData{" +
                "gameID=" + gameID +
                ", whiteUsername='" + whiteUsername + '\'' +
                ", blackUsername='" + blackUsername + '\'' +
                ", gameName='" + gameName + '\'' +
                ", game=" + game +
                '}';
    }
}
