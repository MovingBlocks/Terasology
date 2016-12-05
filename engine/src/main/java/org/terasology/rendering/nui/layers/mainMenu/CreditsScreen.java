/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.layers.mainMenu;

import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.engine.GameEngine;
import org.terasology.engine.module.ModuleManager;
import org.terasology.i18n.TranslationSystem;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.rendering.nui.widgets.UIScrollingText;
import org.terasology.world.generator.internal.WorldGeneratorManager;

public class CreditsScreen extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:creditsScreen");
    public static final String CREDITS_TEXT = "Architects\n \n  Benjamin 'begla' Glatzel\n Immortius\n Kai Kratz\n Andre Herber\n Manuel 'Panserbjoern' Brotz\n Marcin 'MarcinSc' Sciesinski\n Synopia\n Xanhou\n Mike 'mkienenb' Kienenberger\n Gimpanse / shartte\n Flo_K\n emanuele3d\n \n Art Team\n \n  Glasz\n Abraham \"A'nW\" Feldick\n basilix\n Double_A\n eleazzaar\n metouto\n Jan-H. 'Perdemot' B.\n RampageMode\n SuperSnark\n Wolfghard\n Rez 'zproc' N.\n Christoph \"Chrisk\" Köbke\n Maternal\n Trekmarvel\n \n Design Team\n \n  Rasmus 'Cervator' Praestholm\n Overdhose\n Cynthia 'woodspeople' Kurtz\n Mooncalf\n Dei\n UberWaffe\n Christian 'chridal' Dalsvaag\n \n General\n \n  Janred\n Josh\n Stuthulhu\n Igor 't3hk0d3' Yamolov\n AbraCadaver\n Andreas 'ahoehma' Höhmann\n Brokenshakles\n DizzyDragon / LinusVanElswijk\n Esereja\n Julien 'NowNewStart' Gelmar\n Penn 'pencilcheck' Su\n sdab\n Sebastin 'hagish' Dorda\n Philius342\n temsa\n Alex 'nitrix' Belanger\n Austin H. 'R41D3NN' Richards\n Chris 'Aperion' Ritchey\n ilgarma\n Martijn 'mcourteaux' Courteaux\n philip-wernersbach\n Xeano\n Andrew \"Jamoozy\" C. Sabisch\n zriezenman\n Wesley 'NanjoW' Nanjo\n Nick 'SleekoNiko' Caplinger\n Patrick 'Eliwood' Hansberry\n Noah 'nh_99' Howard\n jobernolte\n Elijah C 'emenifee' Menifee\n Marcos 'socram8888' Del Sol Vives\n Marshall 'dataupload' Bradley\n UltimateBudgie\n Michael 'maym86' May\n Aldo 'aldoborrero' Borrero\n PrivateAlpha\n CruzBishop\n JoeClacks\n Nathan \"Nate-Devv\" Moore\n Member1221\n Joseph 'Jtsessions' Sessions\n porl\n Jack 'jacklin213' Lin\n meniku\n GeckoTheGeek42\n IWhoI\n Hugo 'Calinou' Locurcio\n Jakub 'Limeth' Hlusička\n KokPok\n Sebastian 'unpause'\n Quinton 'qreeves' Reeves\n Rui914\n OvermindDL1\n prestidigitator\n chessandgo\n Gustavo 'gtugablue' Silva\n Flávio 'sk0ut' Couto\n Netopya\n André 'andrelago13' Lago\n Sérgio 'sergiomieic' Domingues\n MaloJaffre\n AlexanderGrooff\n janzegers\n Bas 'BdeBock' de Böck\n Arkka 'arkka' Dhiratara\n Rostyslav 'rzats' Zatserkovnyi\n Taha Doğan 'tdgunes' Güneş\n Omer 'oijazsh' Sheikh\n Mehul 'HashCode55' Ahuja\n JeanCGF\n David 'dkambersky' Kamberský\n Mihalache 'Margretor' Octavian\n oniatus\n Skylar 'SkySom' Sommers\n Abhinav 'genuinelucifer' Tripathi\n TobyShaw\n ClintRajaniemi\n Leonardo Akira 'Leonardoas26' Shimabukuro\n Michael 'pollend' Pollind\n Arsenii 'kujiraOo' Kurilov\n Adam 'planetguy32' Price\n portokaliu / Anthodeus\n Jessica 'LadySerenaKitty' Hawkwell\n Milan 'CptCrispyCrunchy' Ender\n Martin 'Vessalix' van Kuik\n Viveret 'viveret' Amant\n tyuiwei / korp\n Ian 'Motta' Maltbey\n joseph-healy\n engiValk\n XTariq\n roshikouhai\n Arcnor\n naiffuR\n Axydlbaaxr\n Waterpicker\n indianajohn\n Kazimir3701\n Translator5\n XroMyla\n Koga Masato\n kaen\n Avalancs\n jchappelle\n qwc\n coty91 / NullThought\n kartikey0303\n nihal111\n segfault802\n rodrigorosa\n triebben\n arpan98\n nclsppr\n namanyadav12\n zeokav\n nahh\n ron-popov\n MaxBorsch\n freyley\n Tropid\n HolixSF\n agsmith\n Stefan-Mitic\n cribsb\n \n GUI Team\n \n  Anton \"small-jeeper\" Kireev\n miniME89\n Lucas 'x3ro' Jenß\n Piotr 'Halamix2' Halama\n \n Logistics Team\n \n  AlbireoX\n Mathias Kalb\n Richard \"rapodaca\" Apodaca\n Stellarfirefly\n Mathias 'mkalb' Kalb\n MrBarsack\n Dennis 'Philaxx' Urban\n 3000Lane\n MiJyn\n Pavel 'neoascetic' Puchkin\n \n World Team\n \n  bi0hax\n ddr2\n Nym Traveel\n Tobias 'Skaldarnar' Nett\n Tenson\n Laurimann\n MatthewPratt\n Martin 'msteiger' Steiger\n Josh 'Josharias' Zacharias\n \n Soundtrack and Sound Effects\n \n Primary soundtrack by Chris Köbke - https://soundcloud.com/chriskoebke\n Sunrise, Afternoon and Sunset composed by Karina Kireev\n Dimlight, Resurface and Other Side composed and produced by Exile.\n Door Open sound by Pagancow, from FreeSound.org\n Door Close sound by Fresco, from FreeSound.org\n Camera Click Noise from Snapper4298, from FreeSound.org\n Other sound effects created by Exile.\n \n Icons from \"Fugue Icons\" by Yusuke Kamiyamane (CC BY 3.0)\n \n editor_array.png\n editor_attribute.png\n editor_object.png\n editor_uibox.png\n editor_uibutton.png\n editor_uicheckbox.png\n editor_uidoubleslider.png\n editor_uidropdown.png\n editor_uidropdownscrollable.png\n editor_uiimage.png\n editor_uilabel.png\n editor_uilist.png\n editor_uiloadbar.png\n editor_uiscrollbar.png\n editor_uislider.png\n editor_uitext.png\n editor_uitextentry.png\n editor_uitooltip.png\n editor_uitreeview.png\n editor_zoomablelayout.png\n \n Icons by Florian Köberle (CC BY 4.0)\n \n editor_cardLayout.png\n editor_columnlayout.png\n editor_flowlayout.png\n editor_miglayout.png\n editor_relativelayout.png\n editor_rowlayout.png\n editor_uispace.png\n contract.png\n contractOver.png\n expand.png\n expandOver.png\n \n Icons prepared by kartikey0303 (CC BY 4.0)\n \n checkboxChecked.png\n checkboxCheckedDisabled.png\n checkboxCheckedHover.png";

    @In
    private Config config;

    private UIScrollingText credits;

    @Override
    @SuppressWarnings("unchecked")
    public void initialise() {
        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());

        WidgetUtil.trySubscribe(this, "back", button -> triggerBackAnimation());

        credits = find("creditsScroll", UIScrollingText.class);
        if (credits != null) {
            credits.setText(CREDITS_TEXT);
            credits.setAutoReset(false);
            credits.setScrollingSpeed(1);
            credits.startScrolling();
        }
    }

    @Override
    public void onOpened() {
        super.onOpened();
        if (credits != null) {
            credits.resetScrolling();
        }
    }
}
