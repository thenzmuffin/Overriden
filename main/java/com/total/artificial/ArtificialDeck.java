package com.total.artificial;

import android.database.Cursor;

import androidx.fragment.app.FragmentManager;

import com.total.overide.OVDatabaseForce;
import com.total.overide.OVHeader;
import com.total.overiden.MainActivity;
import com.total.overiden.TooltipDialogFragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;

public class ArtificialDeck {
    private static class LinkDecks{
        /* This class is only used when reading a deck from a file to remember linkages  */
        public String deck_link;
        public ArtificialDeck deck;
        LinkDecks(String link,ArtificialDeck deck){
            super();
            deck_link = link;
            this.deck = deck;
        }
    }
    private int id;
    private String deckName;
    private OVHeader.UnitRole deckRole;
    private final List<AiUnitCard> cards;
    public ArtificialDeck(Cursor cur){
        super();
        id = OVDatabaseForce.getCursorInt(cur,DatabaseAI.COLUMN_ID);
        deckName = OVDatabaseForce.getCursorString(cur,DatabaseAI.COLUMN_DECK_NAME);
        deckRole = OVHeader.UnitRole.valueOf(OVDatabaseForce.getCursorString(cur,DatabaseAI.COLUMN_DECK_ROLE));
        cards = new ArrayList<>();
    }
    public ArtificialDeck(String[] parts){
        // reading from a file so id is not yet set as this comes from the DB
        super();
        id = -1;
        deckName = parts[0];
        deckRole = OVHeader.UnitRole.valueOf(parts[1]);
        cards = new ArrayList<>();
    }
    public static void readAi(InputStream input) {
        List<LinkDecks> link = new ArrayList<>();
        ArtificialDeck deck = null;
        AiCommander commander = null;
        AiCommanderCard commanderCard = null;
        InputStreamReader inputStreamReader =
                new InputStreamReader(input, StandardCharsets.UTF_8);

        try (BufferedReader reader = new BufferedReader(inputStreamReader) ;
             DatabaseAI dbai = new DatabaseAI(MainActivity.currentActivity)) {
            //
            String line = reader.readLine();
            AiUnitCard card = null;
            AiInstruction instruction = null;
            while (line != null) {
                String[] parts = line.split(">=");
                switch (parts[0]){
                    case "COMMANDER":
                        if (commander != null) dbai.saveCommander(commander);
                        commander = new AiCommander(parts[1]);
                        break;
                    case "LINE_COMM":
                        if (commander!=null){
                            instruction = AiInstruction.newInstance(parts[1]);
                            commander.getTacticAnalysis().add(instruction);
                        }
                        break;
                    case "COMM_CARD":
                        if(commander!=null) {
                            commanderCard = new AiCommanderCard(parts[1], commander.getKey());
                            commander.addCard(commanderCard);
                        }
                        break;
                    case "TARGET":
                        if (commanderCard!=null){
                            commanderCard.addTargetRule(parts[1]);
                        }
                        break;
                    case "COMM_ORDERS":
                        if (commanderCard!=null){
                            commanderCard.getCommanderInstructions().add(new AiCommanderInstructions(parts[1]));
                        }
                        break;
                    case "COMM_CHANGE":
                        if (commanderCard!=null){
                            commanderCard.getChangeUp().add(new AiCommanderChangeRule(parts[1]));
                        }
                        break;
                    case "COMM_PREMOVE":
                        if (commanderCard!=null){
                            instruction = AiInstruction.newInstance(parts[1]);
                            commanderCard.getPreMoves().add(instruction);
                        }
                        break;
                    case "DECK":
                        if (deck!=null)dbai.saveDeck(deck);
                        String[] data = parts[1].split(",");
                        deck = new ArtificialDeck(data);
                        // check if there is a link to the
                        if (data.length>2){
                            link.add(new LinkDecks(data[2],deck));
                        }
                        break;
                    case "ENDDECK":
                        if (deck!=null)dbai.saveDeck(deck);
                        deck = null;
                        break;
                    case "CARD":
                        if (deck!=null) {
                            card = new AiUnitCard(parts[1]);
                            deck.cards.add(card);
                        }
                        break;
                    case "CARD_HEAT":
                        if (card!=null){
                            card.parseHeat(parts[1]);
                        }
                        break;
                    case "WEAPON":
                        if (card!=null){
                            card.parseWeapons(parts[1]);
                        }
                        break;
                    case "LINE_STD":
                        if (card!=null){
                            instruction = AiInstruction.newInstance(parts[1]);
                            card.getMainMoves().add(instruction);
                        }
                        break;
                    case "TARGET_SET":
                        if (card!=null){
                            card.setTargetRules(parts[1]);
                        }
                        break;
                    case "TACTICS":
                        if (card!=null) {
                            card.parseTactics(parts[1]);
                        }
                        break;
                    case "RESULT":
                        if (instruction!=null){
                            String[] bits = parts[1].split(",");
                            AiInstruction.MoveChoice choice = instruction.addResult(bits);
                            // we can also have a link to another deck, instructing the system to
                            // now switch decks due to a change in the units situation
                            if (choice.isDeckLink()){
                                for (LinkDecks item : link){
                                    if (item.deck_link.equals(bits[2])){
                                        choice.setNextInstruction(item.deck.getId());
                                        break;
                                    }
                                }
                            }
                        }
                        break;
                }
                line = reader.readLine();
            }
            if (deck!=null)dbai.saveDeck(deck);
            if (commander != null) dbai.saveCommander(commander);

        } catch (IOException | RuntimeException e) {
            TooltipDialogFragment tooltip = new TooltipDialogFragment(e.toString());

            FragmentManager mgr = MainActivity.currentActivity.getSupportFragmentManager();
            tooltip.show(mgr, "Failed to Load correctly");
        }
        try {
            inputStreamReader.close();
        }catch (IOException | RuntimeException e) {

        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDeckName() {
        return deckName;
    }

    public void setDeckName(String deckName) {
        this.deckName = deckName;
    }

    public OVHeader.UnitRole getDeckRole() {
        return deckRole;
    }
    public void setDeckRole(OVHeader.UnitRole role){
        deckRole = role;
    }
    public List<AiUnitCard> getCards() {
        return cards;
    }
    public int getCardCount(){
        return cards.size();
    }
    public AiUnitCard getCard(int cardId){
        AiUnitCard ret = null;
        for (AiUnitCard card : cards){
            if (card.getId()==cardId){
                ret = card;
                break;
            }
        }
        return ret;
    }
    public void setPilot(ArtificialPilot pilot){
        for (AiUnitCard card : cards){
            card.setPilot(pilot);
        }
    }
}
