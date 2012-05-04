package org.terasology.components;

import org.terasology.entitySystem.AbstractComponent;
/**
 * Books are Knowledge
 * @author bi0hax
 *
 */
public class BookComponent extends AbstractComponent {
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
