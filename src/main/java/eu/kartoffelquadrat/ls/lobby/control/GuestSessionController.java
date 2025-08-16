package eu.kartoffelquadrat.ls.lobby.control;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Profile("guest")
@RestController
@CrossOrigin // supaya bisa dipanggil dari Netlify
@RequestMapping("/guest")
public class GuestSessionController {

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    // --- Models sederhana ---
    public static class Player {
        public String name;
        public Instant joined = Instant.now();
        public Player() {}
        public Player(String name) { this.name = name; }
    }
    public static class Room {
        public String code;
        public String host;
        public Map<String, Player> players = new ConcurrentHashMap<>();
    }

    // healthcheck
    @GetMapping("/health")
    public Map<String,String> health() { return Map.of("status","ok"); }

    // host buat room baru
    @PostMapping("/room")
    public Map<String,String> createRoom(@RequestParam String name) {
        String code = genCode(4);
        Room r = new Room();
        r.code = code;
        r.host = name;
        r.players.put(name, new Player(name));
        rooms.put(code, r);
        return Map.of("code", code, "host", name);
    }

    // player join room
    @PostMapping("/join")
    public Room join(@RequestParam String code, @RequestParam String name) {
        Room r = rooms.get(code.toUpperCase());
        if (r == null) throw new RuntimeException("Room not found");
        r.players.putIfAbsent(name, new Player(name));
        return r;
    }

    // lihat state room
    @GetMapping("/room/{code}")
    public Room get(@PathVariable String code) {
        Room r = rooms.get(code.toUpperCase());
        if (r == null) throw new RuntimeException("Room not found");
        return r;
    }

    private String genCode(int len) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(ThreadLocalRandom.current().nextInt(chars.length())));
        }
        String code = sb.toString();
        return rooms.containsKey(code) ? genCode(len) : code;
    }
}
