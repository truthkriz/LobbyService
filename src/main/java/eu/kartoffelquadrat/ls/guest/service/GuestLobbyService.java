package eu.kartoffelquadrat.ls.guest.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Profile("guest")
public class GuestLobbyService {

  public static class Player {
    public final String id;
    public final String name;
    public final boolean host;
    public final Instant joinedAt = Instant.now();
    public Player(String id, String name, boolean host) {
      this.id = id; this.name = name; this.host = host;
    }
  }

  public static class Room {
    public final String code;
    public final Instant createdAt = Instant.now();
    public final Map<String, Player> players = new ConcurrentHashMap<>();
    public volatile String hostId;
    public Room(String code) { this.code = code; }
  }

  private final Map<String, Room> rooms = new ConcurrentHashMap<>();
  private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
  private final SecureRandom rnd = new SecureRandom();

  private String newCode() {
    String c;
    do {
      StringBuilder sb = new StringBuilder(4);
      for (int i=0;i<4;i++) sb.append(ALPHABET.charAt(rnd.nextInt(ALPHABET.length())));
      c = sb.toString();
    } while (rooms.containsKey(c));
    return c;
  }

  public Room createRoom(String playerName) {
    String code = newCode();
    Room room = new Room(code);
    String pid = UUID.randomUUID().toString();
    Player host = new Player(pid, playerName, true);
    room.players.put(pid, host);
    room.hostId = pid;
    rooms.put(code, room);
    return room;
  }

  public Player join(String code, String playerName) {
    Room room = rooms.get(code.toUpperCase());
    if (room == null) throw new NoSuchElementException("Room not found");
    String pid = UUID.randomUUID().toString();
    Player p = new Player(pid, playerName, false);
    room.players.put(pid, p);
    return p;
  }

  public Room get(String code) {
    Room room = rooms.get(code.toUpperCase());
    if (room == null) throw new NoSuchElementException("Room not found");
    return room;
  }
}
