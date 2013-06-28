package org.terasology.game.types;

import org.terasology.logic.mod.ModAwareManager;

import java.util.*;

/**
 * @author synopia
 */
public class GameTypeManager extends ModAwareManager<GameType, GameTypeUri> {

    @Override
    protected GameTypeUri getUri(GameType item) {
        return item.uri();
    }

    @Override
    protected List<Class> getClasses() {
        return Arrays.asList((Class) GameType.class, BaseGameType.class);
    }

    @Override
    protected Comparator<GameType> getItemComparator() {
        return new Comparator<GameType>() {
            @Override
            public int compare(GameType o1, GameType o2) {
                return o1.name().compareTo(o2.name());
            }
        };
    }

    private GameTypeUri activeGameTypeUri;


    public GameType getActiveGameType() {
        if( activeGameTypeUri!=null ) {
            return getItem(activeGameTypeUri);
        }
        return null;
    }

    public void setActiveGameTypeUri(GameTypeUri activeGameTypeUri) {
        this.activeGameTypeUri = activeGameTypeUri;
    }

    public GameType getGameType(GameTypeUri gameType) {
        return getItem(gameType);
    }
}
