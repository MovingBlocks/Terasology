package org.terasology.world.generator;

import com.google.common.base.Objects;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.terasology.asset.AssetUri;

import java.io.IOException;
import java.util.Locale;

/**
 * A URI to identify a map generator. This URI is always in the form: <package-name>:<generator-name>
 *
 * @author synopia
 */
public class MapGeneratorUri implements Comparable<MapGeneratorUri> {
    private String packageName = "";
    private String generatorName = "";

    public static class GsonAdapter extends TypeAdapter<MapGeneratorUri> {
        @Override
        public void write(JsonWriter out, MapGeneratorUri value) throws IOException {
            out.value(value.toString());
        }

        @Override
        public MapGeneratorUri read(JsonReader in) throws IOException {
            return new MapGeneratorUri(in.nextString());
        }
    }

    public MapGeneratorUri() {
    }

    public MapGeneratorUri(String packageName, String generatorName) {
        this.packageName = packageName;
        this.generatorName = generatorName;
    }

    public MapGeneratorUri(String simpleUri) {
        String[] split = simpleUri.toLowerCase(Locale.ENGLISH).split(AssetUri.PACKAGE_SEPARATOR, 2);
        if( split.length>1 ) {
            packageName = split[0];
            generatorName = split[1];
        }
    }


    public String getPackageName() {
        return packageName;
    }

    public String getGeneratorName() {
        return generatorName;
    }

    public boolean isValid() {
        return !packageName.isEmpty() && !generatorName.isEmpty();
    }

    @Override
    public String toString() {
        if( !isValid() ) {
            return "";
        }
        return packageName + AssetUri.PACKAGE_SEPARATOR + generatorName;
    }

    @Override
    public boolean equals(Object obj) {
        if( obj==this ) {
            return true;
        }
        if( obj instanceof MapGeneratorUri) {
            MapGeneratorUri other = (MapGeneratorUri) obj;
            return Objects.equal(packageName, other.packageName) && Objects.equal(generatorName, other.generatorName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(packageName, generatorName);
    }

    @Override
    public int compareTo(MapGeneratorUri o) {
        return toString().compareTo(o.toString());
    }
}
