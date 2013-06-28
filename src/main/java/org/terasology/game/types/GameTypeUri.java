package org.terasology.game.types;

import com.google.common.base.Objects;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.terasology.asset.AssetUri;

import java.io.IOException;
import java.util.Locale;

/**
 * A URI to identify a game type. This URI is always in the form: <package-name>:<gametype-name>
 *
 * @author synopia
 */
public class GameTypeUri implements Comparable<GameTypeUri> {
    private String packageName = "";
    private String gameTypeName = "";

    public static class GsonAdapter extends TypeAdapter<GameTypeUri> {
        @Override
        public void write(JsonWriter out, GameTypeUri value) throws IOException {
            out.value(value.toString());
        }

        @Override
        public GameTypeUri read(JsonReader in) throws IOException {
            return new GameTypeUri(in.nextString());
        }
    }

    public GameTypeUri() {
    }

    public GameTypeUri(String packageName, String gameTypeName) {
        this.packageName = packageName;
        this.gameTypeName = gameTypeName;
    }

    public GameTypeUri(String simpleUri) {
        String[] split = simpleUri.toLowerCase(Locale.ENGLISH).split(AssetUri.PACKAGE_SEPARATOR, 2);
        if( split.length>1 ) {
            packageName = split[0];
            gameTypeName = split[1];
        }
    }


    public String getPackageName() {
        return packageName;
    }

    public String getGameTypeName() {
        return gameTypeName;
    }

    public boolean isValid() {
        return !packageName.isEmpty() && !gameTypeName.isEmpty();
    }

    @Override
    public String toString() {
        if( !isValid() ) {
            return "";
        }
        return packageName + AssetUri.PACKAGE_SEPARATOR + gameTypeName;
    }

    @Override
    public boolean equals(Object obj) {
        if( obj==this ) {
            return true;
        }
        if( obj instanceof GameTypeUri) {
            GameTypeUri other = (GameTypeUri) obj;
            return Objects.equal(packageName, other.packageName) && Objects.equal(gameTypeName, other.gameTypeName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(packageName, gameTypeName);
    }

    @Override
    public int compareTo(GameTypeUri o) {
        return toString().compareTo(o.toString());
    }
}
