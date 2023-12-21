package testFD;

import arc.*;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.net.Administration.*;
import mindustry.ui.Fonts;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.ConstructBlock;
import mindustry.world.blocks.logic.LogicBlock;
import mindustry.world.blocks.storage.*;

import java.time.LocalTime;
import java.util.*;

public class HistoryPlugin extends Plugin{

    private ArrayList<Player> activeHistoryPlayers = new ArrayList<>();
    private ArrayList<Player> activeFullHistoryPlayers = new ArrayList<>();

    public ArrayList[][] worldChatTileHistory;
    public int SizeLogForChat = 11;

    //called when game initializes
    @Override
    public void init(){

        Events.on(WorldLoadEvent.class, worldLoadEvent -> {
            worldChatTileHistory = new ArrayList[Vars.world.width()][Vars.world.height()];

            for (int x = 0; x < Vars.world.width(); x++) {
                for (int y = 0; y < Vars.world.height(); y++) {
                    worldChatTileHistory[x][y] = new ArrayList<String>();
                }
            }
        });

        Events.on(BlockBuildEndEvent.class, be -> {
            if(be.unit.getPlayer() == null) return;
            if(be.tile.build == null) return;
            if(be.breaking) {                           //Удаление блока конец
                if(be.tile.build instanceof ConstructBlock.ConstructBuild cbuild) {
                    String tempct = LocalTime.now().getHour() + ":" +  LocalTime.now().getMinute() + ":" +  LocalTime.now().getSecond();
                    String chatMess = "[" + tempct + "] " + be.unit.getPlayer().name + " [#ff]доломал[white]: " + Fonts.getUnicodeStr(cbuild.previous.name);
                    //BuildHistoryPlan blockPlan = new BuildHistoryPlan(be.tile, cbuild.previous, cbuild.previous.newBuilding(), be.config, be.unit.getPlayer(), true, chatMess);
                    Seq<Tile> linkedTile = be.tile.getLinkedTiles(new Seq<>());
                    for (Tile tile : linkedTile) {
                        worldChatTileHistory[tile.x][tile.y].add(chatMess);
                    }
                }
            } else {                                   //Постройка блока конец
                String tempct = LocalTime.now().getHour() + ":" +  LocalTime.now().getMinute() + ":" +  LocalTime.now().getSecond();
                String chatMess = "[" + tempct + "] " + be.unit.getPlayer().name + " [#00ff]достроил[white]: " + Fonts.getUnicodeStr(be.tile.build.block.name);
                //BuildHistoryPlan blockPlan = new BuildHistoryPlan(be.tile, be.tile.build.block, be.tile.build, be.config, be.unit.getPlayer(), false, chatMess);
                Seq<Tile> linkedTile = be.tile.getLinkedTiles(new Seq<>());
                for (Tile tile : linkedTile) {
                    worldChatTileHistory[tile.x][tile.y].add(chatMess);
                }
            }
        });

        Events.on(BlockBuildBeginEvent.class, be -> {
            if(be.unit.getPlayer() == null) return;
            if(be.tile.build == null) return;
            if(!be.breaking) {                           //Постройка блока начало
                if(be.tile.build instanceof ConstructBlock.ConstructBuild cbuild) {
                    String tempct = LocalTime.now().getHour() + ":" +  LocalTime.now().getMinute() + ":" +  LocalTime.now().getSecond();
                    String chatMess = "[" + tempct + "] " + be.unit.getPlayer().name + " [#00aa]поставил[white]: " + Fonts.getUnicodeStr(cbuild.current.name);
                    //BuildHistoryPlan blockPlan = new BuildHistoryPlan(be.tile, cbuild.previous, cbuild.previous.newBuilding(), be.config, be.unit.getPlayer(), true, chatMess);
                    Seq<Tile> linkedTile = be.tile.getLinkedTiles(new Seq<>());
                    for (Tile tile : linkedTile) {
                        worldChatTileHistory[tile.x][tile.y].add(chatMess);
                    }
                }
            } else {                                   //Удаление блока начало
                if(be.tile.build instanceof ConstructBlock.ConstructBuild cbuild) {
                    String tempct = LocalTime.now().getHour() + ":" +  LocalTime.now().getMinute() + ":" +  LocalTime.now().getSecond();
                    String chatMess = "[" + tempct + "] " + be.unit.getPlayer().name + " [#aa]надломал[white]: " + Fonts.getUnicodeStr(cbuild.previous.name);
                    //BuildHistoryPlan blockPlan = new BuildHistoryPlan(be.tile, cbuild.previous, cbuild.previous.newBuilding(), be.config, be.unit.getPlayer(), true, chatMess);
                    Seq<Tile> linkedTile = be.tile.getLinkedTiles(new Seq<>());
                    for (Tile tile : linkedTile) {
                        worldChatTileHistory[tile.x][tile.y].add(chatMess);
                    }
                }
            }
        });

        Events.on(ConfigEvent.class, be -> { //Изменение блока
            if(be.player == null) return;
            String tempct = LocalTime.now().getHour() + ":" +  LocalTime.now().getMinute() + ":" +  LocalTime.now().getSecond();
            String chatMess = "[" + tempct + "] ";
            if(be.tile.tile.build instanceof LogicBlock.LogicBuild procc){
                chatMess = chatMess + be.player.name + " [#f70077]изменил[white]: " + Fonts.getUnicodeStr(be.tile.block.name);
            } else {
                chatMess = chatMess + be.player.name + " [#f70077]изменил[white]: " + Fonts.getUnicodeStr(be.tile.block.name) + ((be.value == null) ? "" : ("(" +  be.value.toString() + ")"));
            }
            //ConfigHistoryPlan blockPlan = new ConfigHistoryPlan(be.tile, be.player, be.value, chatMess);
            Seq<Tile> linkedTile = be.tile.tile.getLinkedTiles(new Seq<>());
            for (Tile tile : linkedTile) {
                worldChatTileHistory[tile.x][tile.y].add(chatMess);
            }
        });

        Events.on(BuildRotateEvent.class, be -> { //Поворот блока
            if(be.unit == null) return;
            if(be.unit.getPlayer() == null) return;
            String tempct = LocalTime.now().getHour() + ":" +  LocalTime.now().getMinute() + ":" +  LocalTime.now().getSecond();

            String chatMess = "[" + tempct + "] " + be.unit.getPlayer().name + " [#f70077]повернул[white]: " + Fonts.getUnicodeStr(be.build.block.name);
            //RotateHistoryPlan blockPlan = new RotateHistoryPlan(be.build, be.unit.getPlayer(), be.previous, chatMess);
            Seq<Tile> linkedTile = be.build.tile.getLinkedTiles(new Seq<>());
            for (Tile tile : linkedTile) {
                worldChatTileHistory[tile.x][tile.y].add(chatMess);
            }
        });

        Events.on(PickupEvent.class, be -> { //Поднятие блока
            if(be.carrier == null) return;
            if(be.build == null) return;
            if(be.carrier.getPlayer() == null) return;
            String tempct = LocalTime.now().getHour() + ":" +  LocalTime.now().getMinute() + ":" +  LocalTime.now().getSecond();
            String chatMess = "[" + tempct + "] " + be.carrier.getPlayer().name + " [#cc1166]поднял[white]: " + Fonts.getUnicodeStr(be.build.block.name);
            //DragHistoryPlan blockPlan = new DragHistoryPlan(be.build, be.unit.getPlayer(), true, chatMess);
            Seq<Tile> linkedTile = be.build.tile.getLinkedTiles(new Seq<>());
            for (Tile tile : linkedTile) {
                worldChatTileHistory[tile.x][tile.y].add(chatMess);
            }
        });

        Events.on(PayloadDropEvent.class, be -> { //Отпускание блока
            if(be.carrier == null) return;
            if(be.build == null) return;
            if(be.carrier.getPlayer() == null) return;
            String tempct = LocalTime.now().getHour() + ":" +  LocalTime.now().getMinute() + ":" +  LocalTime.now().getSecond();

            String chatMess = "[" + tempct + "] " + be.carrier.getPlayer().name + " [#66ff66]выбросил[white]: " + Fonts.getUnicodeStr(be.build.block.name);
            //DragHistoryPlan blockPlan = new DragHistoryPlan(be.build, be.unit.getPlayer(), false, chatMess);
            Seq<Tile> linkedTile = be.build.tile.getLinkedTiles(new Seq<>());
            for (Tile tile : linkedTile) {
                worldChatTileHistory[tile.x][tile.y].add(chatMess);
            }
        });
        Events.on(BuildingCommandEvent.class, be -> { //Командование фабрикой
            if(be.player == null) return;
            if(be.building == null) return;
            String tempct = LocalTime.now().getHour() + ":" +  LocalTime.now().getMinute() + ":" +  LocalTime.now().getSecond();

            String chatMess = "[" + tempct + "] " + be.player.name + " [#66ff66]указал[white]: " + Fonts.getUnicodeStr(be.building.block.name) +
                    ((be.position == null) ? "" : "(" + Mathf.ceil(be.position.x/8)  + "," + Mathf.ceil(be.position.y/8) + ")");
            //DragHistoryPlan blockPlan = new DragHistoryPlan(be.build, be.unit.getPlayer(), false, chatMess);
            Seq<Tile> linkedTile = be.building.tile.getLinkedTiles(new Seq<>());
            for (Tile tile : linkedTile) {
                worldChatTileHistory[tile.x][tile.y].add(chatMess);
            }
        });


        Events.on(TapEvent.class, tapEvent -> {
            if (activeHistoryPlayers.contains(tapEvent.player)) {
                ArrayList<String> tileHistory = worldChatTileHistory[tapEvent.tile.x][tapEvent.tile.y];
                String message = "[white]Записи блока на  (" + tapEvent.tile.x + "," + tapEvent.tile.y + ")";
                if (tileHistory.isEmpty()) {message += "\n[purple]* [white]Нет записей"; }

                if (tileHistory.size() > SizeLogForChat) {
                    int maxsize = tileHistory.size();
                    message += "\n[white]Слишком много запией, показаны последнии";
                    for(int i = maxsize - SizeLogForChat; i < maxsize; i++){
                        message += "\n" + tileHistory.get(i);
                    }
                } else {
                    for (String historyEntry : tileHistory) {
                        message += "\n" + historyEntry;
                    }
                }
                tapEvent.player.sendMessage(message);
            } else if (activeFullHistoryPlayers.contains(tapEvent.player)) {
                ArrayList<String> tileHistory = worldChatTileHistory[tapEvent.tile.x][tapEvent.tile.y];
                String message = "[white]Записи блока на  (" + tapEvent.tile.x + "," + tapEvent.tile.y + ")";
                if (tileHistory.isEmpty()) {message += "\n[purple]* [white]Нет записей"; }
                for (String historyEntry : tileHistory) {
                    message += "\n" + historyEntry;
                }
                tapEvent.player.sendMessage(message);
            }
        });
    }


    //register commands that player can invoke in-game
    @Override
    public void registerClientCommands(CommandHandler handler){

        handler.<Player>register("h", "Toggle history display when clicking on a tile", (args, player) -> {
                if (activeHistoryPlayers.contains(player)) {
                    activeHistoryPlayers.remove(player);
                    player.sendMessage("[red]Выкл. [white]отображение истории.");
                } else {
                    activeHistoryPlayers.add(player);
                    player.sendMessage("[green]Вкл. [white]отображение истории. Нажмите на блок, чтобы посмотреть логи");
                }
        });
        handler.<Player>register("hf", "Toggle history display when clicking on a tile without limits", (args, player) -> {
                if (activeFullHistoryPlayers.contains(player)) {
                    activeFullHistoryPlayers.remove(player);
                    player.sendMessage("[red]Выкл. [white]отображение истории.");
                } else {
                    activeFullHistoryPlayers.add(player);
                    player.sendMessage("[green]Вкл. [white]отображение истории без ограничения. Нажмите на блок, чтобы посмотреть логи");
                }
        });

    }

}
