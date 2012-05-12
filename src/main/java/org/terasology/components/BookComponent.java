package org.terasology.components;

import org.terasology.entitySystem.Component;

/**
 * Books are Knowledge
 * @author bi0hax
 *
 */
public class BookComponent implements Component {
    public enum BookType {
        Blank,
        WContents,
        Recipe         // Not yet implemented
    }
    public BookType type;
    //Book Contents in Array?
    public String page1 = "";
    public String page2 = "";

}
