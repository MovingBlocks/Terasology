package org.terasology.rendering.gui.menus;

import org.terasology.logic.manager.AssetManager;
import org.terasology.rendering.gui.components.UIButton;
import org.terasology.rendering.gui.components.UIImageOverlay;
import org.terasology.rendering.gui.components.UIText;
import org.terasology.rendering.gui.framework.UIDisplayWindow;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

import javax.vecmath.Vector2f;

/**
 * @author Overdhose
 *         Date: 2/07/12
 */
public class UIInputOptions extends UIDisplayWindow {

    final UIImageOverlay overlay;
    final UIGraphicsElement title;

    final UIText ForwardButtontext,
            BackwardButtontext,
            JumpbehaviourButtontext,
            AttackButtontext,
            ConsoleButtontext,
            CrouchButtontext,
            ActivateButtontext,   // (frob)
            HideguiButtontext,
            InventoryButtontext,
            JumpButtontext,
            LeftButtontext,
            MinionmodeButtontext,
            PauzeButtontext,
            RightButtontext,
            RunButtontext,
            ToolnextButtontext,
            ToolprevButtontext,
            Toolslot1Buttontext,
            Toolslot2Buttontext,
            Toolslot3Buttontext,
            Toolslot4Buttontext,
            Toolslot5Buttontext,
            Toolslot6Buttontext,
            Toolslot7Buttontext,
            Toolslot8Buttontext,
            Toolslot9Buttontext,
            UsehelditemButtontext;

    private final UIButton inputToSettingsMenuButton,
            ForwardButton,
            BackwardButton,
            JumpbehaviourButton,
            AttackButton,
            ConsoleButton,
            CrouchButton,
            ActivateButton,    // (frob)
            HideguiButton,
            InventoryButton,
            JumpButton,
            LeftButton,
            MinionmodeButton,
            PauzeButton,
            RightButton,
            RunButton,
            ToolnextButton,
            ToolprevButton,
            Toolslot1Button,
            Toolslot2Button,
            Toolslot3Button,
            Toolslot4Button,
            Toolslot5Button,
            Toolslot6Button,
            Toolslot7Button,
            Toolslot8Button,
            Toolslot9Button,
            UsehelditemButton;

    final UIText subtitle;

    public UIInputOptions() {
        maximize();

        title = new UIGraphicsElement(AssetManager.loadTexture("engine:terasology"));
        title.setVisible(true);
        title.setSize(new Vector2f(512f, 128f));

        subtitle = new UIText("Input options");
        subtitle.setVisible(true);

        overlay = new UIImageOverlay(AssetManager.loadTexture("engine:loadingBackground"));
        overlay.setVisible(true);

        inputToSettingsMenuButton = new UIButton(new Vector2f(256f, 32f));
        inputToSettingsMenuButton.getLabel().setText("Return to Settings Menu");
        inputToSettingsMenuButton.setVisible(true);

        ForwardButton = new UIButton(new Vector2f(64f, 32f));
        ForwardButton.getLabel().setText("");
        ForwardButton.setVisible(true);
        BackwardButton = new UIButton(new Vector2f(64f, 32f));
        BackwardButton.getLabel().setText("");
        BackwardButton.setVisible(true);
        JumpbehaviourButton = new UIButton(new Vector2f(64f, 32f));
        JumpbehaviourButton.getLabel().setText("");
        JumpbehaviourButton.setVisible(true);
        AttackButton = new UIButton(new Vector2f(64f, 32f));
        AttackButton.getLabel().setText("");
        AttackButton.setVisible(true);
        ConsoleButton = new UIButton(new Vector2f(64f, 32f));
        ConsoleButton.getLabel().setText("");
        ConsoleButton.setVisible(true);
        CrouchButton = new UIButton(new Vector2f(64f, 32f));
        CrouchButton.getLabel().setText("");
        CrouchButton.setVisible(true);
        ActivateButton = new UIButton(new Vector2f(64f, 32f));    // (frob)
        ActivateButton.getLabel().setText("");
        ActivateButton.setVisible(true);
        HideguiButton = new UIButton(new Vector2f(64f, 32f));
        HideguiButton.getLabel().setText("");
        HideguiButton.setVisible(true);
        InventoryButton = new UIButton(new Vector2f(64f, 32f));
        InventoryButton.getLabel().setText("");
        InventoryButton.setVisible(true);
        JumpButton = new UIButton(new Vector2f(64f, 32f));
        JumpButton.getLabel().setText("");
        JumpButton.setVisible(true);
        LeftButton = new UIButton(new Vector2f(64f, 32f));
        LeftButton.getLabel().setText("");
        LeftButton.setVisible(true);
        MinionmodeButton = new UIButton(new Vector2f(64f, 32f));
        MinionmodeButton.getLabel().setText("");
        MinionmodeButton.setVisible(true);
        PauzeButton = new UIButton(new Vector2f(64f, 32f));
        PauzeButton.getLabel().setText("");
        PauzeButton.setVisible(true);
        RightButton = new UIButton(new Vector2f(64f, 32f));
        RightButton.getLabel().setText("");
        RightButton.setVisible(true);
        RunButton = new UIButton(new Vector2f(64f, 32f));
        RunButton.getLabel().setText("");
        RunButton.setVisible(true);
        ToolnextButton = new UIButton(new Vector2f(64f, 32f));
        ToolnextButton.getLabel().setText("");
        ToolnextButton.setVisible(true);
        ToolprevButton = new UIButton(new Vector2f(64f, 32f));
        ToolprevButton.getLabel().setText("");
        ToolprevButton.setVisible(true);
        Toolslot1Button = new UIButton(new Vector2f(64f, 32f));
        Toolslot1Button.getLabel().setText("");
        Toolslot1Button.setVisible(true);
        Toolslot2Button = new UIButton(new Vector2f(64f, 32f));
        Toolslot2Button.getLabel().setText("");
        Toolslot2Button.setVisible(true);
        Toolslot3Button = new UIButton(new Vector2f(64f, 32f));
        Toolslot3Button.getLabel().setText("");
        Toolslot3Button.setVisible(true);
        Toolslot4Button = new UIButton(new Vector2f(64f, 32f));
        Toolslot4Button.getLabel().setText("");
        Toolslot4Button.setVisible(true);
        Toolslot5Button = new UIButton(new Vector2f(64f, 32f));
        Toolslot5Button.getLabel().setText("");
        Toolslot5Button.setVisible(true);
        Toolslot6Button = new UIButton(new Vector2f(64f, 32f));
        Toolslot6Button.getLabel().setText("");
        Toolslot6Button.setVisible(true);
        Toolslot7Button = new UIButton(new Vector2f(64f, 32f));
        Toolslot7Button.getLabel().setText("");
        Toolslot7Button.setVisible(true);
        Toolslot8Button = new UIButton(new Vector2f(64f, 32f));
        Toolslot8Button.getLabel().setText("");
        Toolslot8Button.setVisible(true);
        Toolslot9Button = new UIButton(new Vector2f(64f, 32f));
        Toolslot9Button.getLabel().setText("");
        Toolslot9Button.setVisible(true);
        UsehelditemButton = new UIButton(new Vector2f(64f, 32f));
        UsehelditemButton.getLabel().setText("");
        UsehelditemButton.setVisible(true);

        ForwardButtontext = new UIText("forward");
        BackwardButtontext = new UIText("backward");
        RightButtontext = new UIText("strafe right");
        LeftButtontext = new UIText("strafe left");

        UsehelditemButtontext = new UIText("use held item");
        AttackButtontext = new UIText("attack");
        ToolnextButtontext = new UIText("next item");
        ToolprevButtontext = new UIText("previous item");

        ActivateButtontext = new UIText("activate");   // (frob)
        JumpButtontext = new UIText("jump");
        RunButtontext = new UIText("run");
        CrouchButtontext = new UIText("crouch");
        InventoryButtontext = new UIText("open inventory");
        MinionmodeButtontext = new UIText("enter minionmode");
        PauzeButtontext = new UIText("pauze");
        HideguiButtontext = new UIText("hide the gui");
        JumpbehaviourButtontext = new UIText("Jump behaviour");
        ConsoleButtontext = new UIText("open console");

        Toolslot1Buttontext = new UIText("hotkey1");
        Toolslot2Buttontext = new UIText("hotkey2");
        Toolslot3Buttontext = new UIText("hotkey3");
        Toolslot4Buttontext = new UIText("hotkey4");
        Toolslot5Buttontext = new UIText("hotkey5");
        Toolslot6Buttontext = new UIText("hotkey6");
        Toolslot7Buttontext = new UIText("hotkey7");
        Toolslot8Buttontext = new UIText("hotkey8");
        Toolslot9Buttontext = new UIText("hotkey9");

        ForwardButtontext.setVisible(true);
        BackwardButtontext.setVisible(true);
        JumpbehaviourButtontext.setVisible(true);
        AttackButtontext.setVisible(true);
        ConsoleButtontext.setVisible(true);
        CrouchButtontext.setVisible(true);
        ActivateButtontext.setVisible(true);   // (frob)
        HideguiButtontext.setVisible(true);
        InventoryButtontext.setVisible(true);
        JumpButtontext.setVisible(true);
        LeftButtontext.setVisible(true);
        MinionmodeButtontext.setVisible(true);
        PauzeButtontext.setVisible(true);
        RightButtontext.setVisible(true);
        RunButtontext.setVisible(true);
        ToolnextButtontext.setVisible(true);
        ToolprevButtontext.setVisible(true);
        Toolslot1Buttontext.setVisible(true);
        Toolslot2Buttontext.setVisible(true);
        Toolslot3Buttontext.setVisible(true);
        Toolslot4Buttontext.setVisible(true);
        Toolslot5Buttontext.setVisible(true);
        Toolslot6Buttontext.setVisible(true);
        Toolslot7Buttontext.setVisible(true);
        Toolslot8Buttontext.setVisible(true);
        Toolslot9Buttontext.setVisible(true);
        UsehelditemButtontext.setVisible(true);

        addDisplayElement(overlay);
        addDisplayElement(title);
        addDisplayElement(subtitle);

        addDisplayElement(ForwardButtontext);
        addDisplayElement(BackwardButtontext);
        addDisplayElement(JumpbehaviourButtontext);
        addDisplayElement(AttackButtontext);
        addDisplayElement(ConsoleButtontext);
        addDisplayElement(CrouchButtontext);
        addDisplayElement(ActivateButtontext);   // (frob)
        addDisplayElement(HideguiButtontext);
        addDisplayElement(InventoryButtontext);
        addDisplayElement(JumpButtontext);
        addDisplayElement(LeftButtontext);
        addDisplayElement(MinionmodeButtontext);
        addDisplayElement(PauzeButtontext);
        addDisplayElement(RightButtontext);
        addDisplayElement(RunButtontext);
        addDisplayElement(ToolnextButtontext);
        addDisplayElement(ToolprevButtontext);
        addDisplayElement(Toolslot1Buttontext);
        addDisplayElement(Toolslot2Buttontext);
        addDisplayElement(Toolslot3Buttontext);
        addDisplayElement(Toolslot4Buttontext);
        addDisplayElement(Toolslot5Buttontext);
        addDisplayElement(Toolslot6Buttontext);
        addDisplayElement(Toolslot7Buttontext);
        addDisplayElement(Toolslot8Buttontext);
        addDisplayElement(Toolslot9Buttontext);
        addDisplayElement(UsehelditemButtontext);

        addDisplayElement(inputToSettingsMenuButton, "inputToSettingsMenuButton");
        addDisplayElement(ForwardButton, "ForwardButton");
        addDisplayElement(BackwardButton, "BackwardButton");
        addDisplayElement(JumpbehaviourButton, "JumpbehaviourButton");
        addDisplayElement(AttackButton, "AttackButton");
        addDisplayElement(ConsoleButton, "ConsoleButton");
        addDisplayElement(CrouchButton, "CrouchButton");
        addDisplayElement(ActivateButton, "ActivateButton");    // (frob)
        addDisplayElement(HideguiButton, "HideguiButton");
        addDisplayElement(InventoryButton, "InventoryButton");
        addDisplayElement(JumpButton, "JumpButton");
        addDisplayElement(LeftButton, "LeftButton");
        addDisplayElement(MinionmodeButton, "MinionmodeButton");
        addDisplayElement(PauzeButton, "PauzeButton");
        addDisplayElement(RightButton, "RightButton");
        addDisplayElement(RunButton, "RunButton");
        addDisplayElement(ToolnextButton, "ToolnextButton");
        addDisplayElement(ToolprevButton, "ToolprevButton");
        addDisplayElement(Toolslot1Button, "Toolslot1Button");
        addDisplayElement(Toolslot2Button, "Toolslot2Button");
        addDisplayElement(Toolslot3Button, "Toolslot3Button");
        addDisplayElement(Toolslot4Button, "Toolslot4Button");
        addDisplayElement(Toolslot5Button, "Toolslot5Button");
        addDisplayElement(Toolslot6Button, "Toolslot6Button");
        addDisplayElement(Toolslot7Button, "Toolslot7Button");
        addDisplayElement(Toolslot8Button, "Toolslot8Button");
        addDisplayElement(Toolslot9Button, "Toolslot9Button");
        addDisplayElement(UsehelditemButton, "UsehelditemButton");
        update();
    }

    @Override
    public void update() {
        super.update();

        subtitle.centerHorizontally();
        subtitle.getPosition().y = 130f;

        ForwardButtontext.getPosition().x = 40f;
        ForwardButtontext.getPosition().y = 200f;
        ForwardButton.getPosition().x = 40f + 120f;
        ForwardButton.getPosition().y = 200f;

        BackwardButtontext.getPosition().x = 40f;
        BackwardButtontext.getPosition().y = 200f + 40f;
        BackwardButton.getPosition().x = 40f + 120f;
        BackwardButton.getPosition().y = 200f + 40f;

        LeftButtontext.getPosition().x = 40f;
        LeftButtontext.getPosition().y = 200f + 2 * 40f;
        LeftButton.getPosition().x = 40f + 120f;
        LeftButton.getPosition().y = 200f + 2 * 40f;

        RightButtontext.getPosition().x = 40f;
        RightButtontext.getPosition().y = 200f + 3 * 40f;
        RightButton.getPosition().x = 40f + 120f;
        RightButton.getPosition().y = 200f + 3 * 40f;


        AttackButtontext.getPosition().x = 40f;
        AttackButtontext.getPosition().y = 200f + 5 * 40f;
        AttackButton.getPosition().x = 40f + 120f;
        AttackButton.getPosition().y = 200f + 5 * 40f;

        UsehelditemButtontext.getPosition().x = 40f;
        UsehelditemButtontext.getPosition().y = 200f + 6 * 40f;
        UsehelditemButton.getPosition().x = 40f + 120f;
        UsehelditemButton.getPosition().y = 200f + 6 * 40f;

        ToolnextButtontext.getPosition().x = 40f;
        ToolnextButtontext.getPosition().y = 200f + 7 * 40f;
        ToolnextButton.getPosition().x = 40f + 120f;
        ToolnextButton.getPosition().y = 200f + 7 * 40f;

        ToolprevButtontext.getPosition().x = 40f;
        ToolprevButtontext.getPosition().y = 200f + 8 * 40f;
        ToolprevButton.getPosition().x = 40f + 120f;
        ToolprevButton.getPosition().y = 200f + 8 * 40f;


        ActivateButtontext.getPosition().x = 250f;   // (frob)
        ActivateButtontext.getPosition().y = 200f;   // (frob)
        ActivateButton.getPosition().x = 250f + 120f;    // (frob)
        ActivateButton.getPosition().y = 200f;    // (frob)

        InventoryButtontext.getPosition().x = 250f;
        InventoryButtontext.getPosition().y = 200f + 40f;
        InventoryButton.getPosition().x = 250f + 120f;
        InventoryButton.getPosition().y = 200f + 40f;

        JumpButtontext.getPosition().x = 250f;
        JumpButtontext.getPosition().y = 200f + 3 * 40f;
        JumpButton.getPosition().x = 250f + 120f;
        JumpButton.getPosition().y = 200f + 3 * 40f;

        RunButtontext.getPosition().x = 250f;
        RunButtontext.getPosition().y = 200f + 4 * 40f;
        RunButton.getPosition().x = 250f + 120f;
        RunButton.getPosition().y = 200f + 4 * 40f;

        CrouchButtontext.getPosition().x = 250f;
        CrouchButtontext.getPosition().y = 200f + 5 * 40f;
        CrouchButton.getPosition().x = 250f + 120f;
        CrouchButton.getPosition().y = 200f + 5 * 40f;

        PauzeButtontext.getPosition().x = 250f;
        PauzeButtontext.getPosition().y = 200f + 7 * 40f;
        PauzeButton.getPosition().x = 250f + 120f;
        PauzeButton.getPosition().y = 200f + 7 * 40f;

        ConsoleButtontext.getPosition().x = 250f;
        ConsoleButtontext.getPosition().y = 200f + 8 * 40f;
        ConsoleButton.getPosition().x = 250f + 120f;
        ConsoleButton.getPosition().y = 200f + 8 * 40f;


        Toolslot1Buttontext.getPosition().x = 460f;
        Toolslot1Buttontext.getPosition().y = 200f;
        Toolslot1Button.getPosition().x = 460f + 120f;
        Toolslot1Button.getPosition().y = 200f;
        Toolslot2Buttontext.getPosition().x = 460f;
        Toolslot2Buttontext.getPosition().y = 200f + 40f;
        Toolslot2Button.getPosition().x = 460f + 120f;
        Toolslot2Button.getPosition().y = 200f + 40f;
        Toolslot3Buttontext.getPosition().x = 460f;
        Toolslot3Buttontext.getPosition().y = 200f + 2 * 40f;
        Toolslot3Button.getPosition().x = 460f + 120f;
        Toolslot3Button.getPosition().y = 200f + 2 * 40f;
        Toolslot4Buttontext.getPosition().x = 460f;
        Toolslot4Buttontext.getPosition().y = 200f + 3 * 40f;
        Toolslot4Button.getPosition().x = 460f + 120f;
        Toolslot4Button.getPosition().y = 200f + 3 * 40f;
        Toolslot5Buttontext.getPosition().x = 460f;
        Toolslot5Buttontext.getPosition().y = 200f + 4 * 40f;
        Toolslot5Button.getPosition().x = 460f + 120f;
        Toolslot5Button.getPosition().y = 200f + 4 * 40f;
        Toolslot6Buttontext.getPosition().x = 460f;
        Toolslot6Buttontext.getPosition().y = 200f + 5 * 40f;
        Toolslot6Button.getPosition().x = 460f + 120f;
        Toolslot6Button.getPosition().y = 200f + 5 * 40f;
        Toolslot7Buttontext.getPosition().x = 460f;
        Toolslot7Buttontext.getPosition().y = 200f + 6 * 40f;
        Toolslot7Button.getPosition().x = 460f + 120f;
        Toolslot7Button.getPosition().y = 200f + 6 * 40f;
        Toolslot8Buttontext.getPosition().x = 460f;
        Toolslot8Buttontext.getPosition().y = 200f + 7 * 40f;
        Toolslot8Button.getPosition().x = 460f + 120f;
        Toolslot8Button.getPosition().y = 200f + 7 * 40f;
        Toolslot9Buttontext.getPosition().x = 460f;
        Toolslot9Buttontext.getPosition().y = 200f + 8 * 40f;
        Toolslot9Button.getPosition().x = 460f + 120f;
        Toolslot9Button.getPosition().y = 200f + 8 * 40f;

        /*

        HideguiButtontext.getPosition().x = 230f;
        HideguiButtontext.getPosition().y = 230f;
        HideguiButton.getPosition().x = ;
        HideguiButton.getPosition().y = ;



        JumpbehaviourButtontext.getPosition().x = 230f;
        JumpbehaviourButtontext.getPosition().y = 230f;
        JumpbehaviourButton.getPosition().x = ;
        JumpbehaviourButton.getPosition().y = ;

        MinionmodeButtontext.getPosition().x = 230f;
        MinionmodeButtontext.getPosition().y = 230f;
        MinionmodeButton.getPosition().x = ;
        MinionmodeButton.getPosition().y = ;



          */
        inputToSettingsMenuButton.centerHorizontally();
        inputToSettingsMenuButton.getPosition().y = 300f + 7 * 40f;

        title.centerHorizontally();
        title.getPosition().y = 28f;
    }
}
