package orsc;

import com.openrsc.client.entityhandling.EntityHandler;
import com.openrsc.client.entityhandling.defs.ItemDef;
import com.openrsc.client.entityhandling.instances.Item;
import com.openrsc.client.model.Sprite;
import orsc.buffers.RSBufferUtils;
import orsc.buffers.RSBuffer_Bits;
import orsc.enumerations.MessageType;
import orsc.enumerations.ORSCharacterDirection;
import orsc.graphics.gui.KillAnnouncer;
import orsc.graphics.gui.SocialLists;
import orsc.graphics.three.RSModel;
import orsc.multiclient.ClientPort;
import orsc.net.Network_Socket;
import orsc.util.FastMath;
import orsc.util.GenUtil;
import orsc.util.StringUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static orsc.Config.isAndroid;

public class PacketHandler {

	private final RSBuffer_Bits packetsIncoming = new RSBuffer_Bits(30000);
	private Network_Socket clientStream;
	private mudclient mc;

	private static final Map<Integer, String> incomingOpcodeMap = new HashMap<Integer, String>() {{
		put(4, "CLOSE_CONNECTION_NOTIFY");
		put(5, "QUEST_STATUS");
		put(6, "UPDATE_STAKED_ITEMS_OPPONENT");
		put(15, "UPDATE_TRADE_ACCEPTANCE");
		put(20, "SHOW_CONFIRM_TRADE");
		put(25, "FLOOR_SET");
		put(30, "SYNC_DUEL_SETTINGS");
		put(33, "UPDATE_XP");
		put(36, "DISPLAY_TELEPORT_TELEGRAB_BUBBLE");
		put(42, "OPEN_BANK");
		put(48, "SCENERY_HANDLER");
		put(51, "PRIVACY_SETTINGS");
		put(52, "UPDATE_SYSTEM_UPDATE_TIMER");
		put(53, "SET_INVENTORY");
		put(59, "SHOW_APPEARANCE_CHANGE");
		put(79, "NPC_COORDS");
		put(83, "DISPLAY_DEATH_SCREEN");
		put(84, "WAKE_UP");
		put(87, "SEND_PM");
		put(89, "SHOW_DIALOGUE_SERVER_MESSAGE_NOT_TOP");
		put(90, "SET_INVENTORY_SLOT");
		put(91, "BOUNDARY_HANDLER");
		put(92, "INITIATE_TRADE");
		put(97, "UPDATE_ITEMS_TRADED_TO_YOU");
		put(99, "GROUNDITEM_HANDLER");
		put(101, "SHOW_SHOP");
		put(104, "UPDATE_NPC");
		put(109, "SET_IGNORE");
		put(111, "COMPLETED_TUTORIAL");
		put(114, "SET_FATIGUE");
		put(117, "FALL_ASLEEP");
		put(120, "RECEIVE_PM");
		put(123, "REMOVE_INVENTORY_SLOT");
		put(128, "CONCLUDE_TRADE");
		put(131, "SEND_MESSAGE");
		put(137, "EXIT_SHOP");
		put(149, "UPDATE_FRIEND");
		put(153, "SET_EQUIP_STATS");
		put(156, "SET_STATS");
		put(159, "UPDATE_STAT");
		put(162, "UPDATE_TRADE_RECIPIENT_ACCEPTANCE");
		put(165, "CLOSE_CONNECTION");
		put(172, "SHOW_CONFIRM_DUEL");
		put(176, "SHOW_DIALOGUE_DUEL");
		put(182, "SHOW_WELCOME");
		put(183, "DENY_LOGOUT");
		put(191, "PLAYER_COORDS");
		put(194, "INCORRECT_SLEEPWORD");
		put(203, "CLOSE_BANK");
		put(204, "PLAY_SOUND");
		put(206, "SET_PRAYERS");
		put(210, "UPDATE_DUEL_ACCEPTANCE");
		put(211, "UPDATE_ENTITIES");
		put(213, "NO_OP_WHILE_WAITING_FOR_NEW_APPEARANCE");
		put(222, "SHOW_DIALOGUE_SERVER_MESSAGE_TOP");
		put(225, "CANCEL_DUEL_DIALOGUE");
		put(234, "UPDATE_PLAYERS");
		put(237, "UPDATE_IGNORE_BECAUSE_OF_NAME_CHANGE");
		put(240, "GAME_SETTINGS");
		put(244, "SET_FATIGUE_SLEEPING");
		put(245, "SHOW_DIALOGUE_MENU");
		put(249, "UPDATE_BANK_ITEMS_DISPLAY");
		put(252, "DISABLE_OPTION_MENU");
		put(253, "UPDATE_DUEL_OPPONENT_ACCEPTANCE");

		// CUSTOM
		put(19, "SEND_SERVER_CONFIGS");
		put(34, "FREEZE_EXPERIENCE_TOGGLE");
		put(113, "SEND_IRONMAN");
		put(115, "SEND_ON_BLACK_HOLE");
		put(129, "COMBAT_STYLE_CHANGED");
		put(135, "BANK_PIN_INTERFACE");
		put(136, "ONLINE_LIST");
		put(144, "SHOW_POINTS_TO_GP");
		put(147, "SEND_KILLS2");
		put(148, "SET_OPENPK_POINTS");
		put(150, "UPDATE_PRESET");
		put(250, "UPDATE_UNLOCKED_APPEARANCES");
		put(254, "UPDATE_EQUIPMENT");
		put(255, "UPDATE_EQUIPMENT_SLOT");
	}};


	public PacketHandler(mudclient mc) {
		this.mc = mc;
	}

	public Network_Socket getClientStream() {
		return clientStream;
	}

	public void setClientStream(Network_Socket clientStream) {
		this.clientStream = clientStream;
	}

	public void startThread(int andStart, Runnable proc) {
		try {

			Thread var3 = new Thread(proc);
			if (andStart == 1) {
				var3.setDaemon(true);
				var3.start();
			}
		} catch (RuntimeException var4) {
			throw GenUtil.makeThrowable(var4, "e.S(" + andStart + ',' + (proc != null ? "{...}" : "null") + ')');
		}
	}

	public final Socket openSocket(int port, String host) throws IOException {
    System.out.println("Connecting to " + host + ":" + port + " ...");
    Socket s = null;
    try {
        s = new Socket(InetAddress.getByName(host), port);
        //s.setSendBufferSize(25000);
        //s.setReceiveBufferSize(25000);
        s.setSoTimeout(30000);
        s.setTcpNoDelay(true);
        return s;
    } catch (IOException ex) {
        System.err.println("Connection failed: " + ex.getMessage());
        throw ex;
    }
}

